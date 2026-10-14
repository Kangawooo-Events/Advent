package cd.arnett.advent.events;

import cd.arnett.advent.Advent;
import cd.arnett.advent.EventTracker;
import cd.arnett.advent.commands.commandTree.StartEventCommand;
import cd.arnett.advent.commands.commandTree.StopEventCommand;
import cd.arnett.advent.events.annotations.*;
import cd.arnett.advent.events.annotations.ReadYaml;
import cd.arnett.advent.events.annotations.WriteYaml;
import cd.arnett.advent.events.controls.*;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import cd.arnett.caddamands.cattamands.cattamand.LiteralCattamand;
import com.google.common.collect.HashBasedTable;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;


/**
 * Provides data about events of this type, by registering this type you can then create
 * events of this type through commands.
 */
public abstract class EventBase implements Comparable<EventBase>{

    HashMap<UUID, PlayerEventStatus> eventPlayers = new HashMap<>();
    HashMap<Class<? extends Annotation>, HashMap<Method, Annotation>> callbacks = new HashMap<>();
    EnumMap<JoinTaskPriority, ArrayList<Method>> joinTasks = new EnumMap<>(JoinTaskPriority.class);

    /**
     * stores data from modules
     */
    HashMap<String, Object> moduleData = new HashMap<>();

    boolean isRunning = false;
    boolean isFrozen = false;
    boolean allowPvp = canPvPByDefault();

    public EventBase() {
        //fill callbacks
        loadCallbacks();
        invokeCallbacks(OnInitialize.class, null);
    }


    //region Callbacks

    /*=================================================================================================
                        -  Callbacks  -
    =================================================================================================*/

    /**
     * Finds all annotated callback functions in this object and registers them for their events
     */
    final void loadCallbacks() {
        //go through each method to store callbacks (so it doesn't have to search at runtime)
        for(Method method : getClass().getMethods())
        {

            //get annotations of override methods filtered by there types
            HashSet<Annotation> annotations = new HashSet<>();
            MethodUtils.getOverrideHierarchy(method, ClassUtils.Interfaces.INCLUDE).forEach(higherarchyMethod -> {
                annotations.addAll(Arrays.asList(higherarchyMethod.getAnnotations()));
            });

            outer:
            for (Annotation annotation : annotations)
            {
                callbacks.computeIfAbsent(annotation.annotationType(),
                        ann -> new HashMap<>()).put(method, annotation);

                //if it is a join annotation then add it to the join tasks map
                if(annotation instanceof OnJoinEvent joinAnnotation)
                {
                    var priority = joinAnnotation.priority();
                    joinTasks.computeIfAbsent(priority, p -> new ArrayList<>()).add(method);
                    continue;
                }

                //check for automated data
                ModuleDataProvider[] trackedListAnnotations = method.getAnnotationsByType(ModuleDataProvider.class);
                for(ModuleDataProvider ann : trackedListAnnotations)
                {
                    //clone field to data list
                    try {
                        moduleData.put(ann.key(), method.invoke(this));
                        registerAutomaticData(ann, method);
                    }
                    catch (Exception e)
                    {
                        Advent.logger.warning("Unable to use module data: " + method.getName());
                    }
                    continue outer;
                }
            }
        }
    }


    /**
     * Invokes callbacks of type
     * @param type type of callbacks to invoke
     * @param foreachResult What to do with the result
     * @param params What parameters to pass
     */
    public final void invokeCallbacks(Class<? extends Annotation> type, BiConsumer<Object, Annotation> foreachResult, Object... params)
    {
        callbacks.getOrDefault(type, new HashMap<>(0)).forEach((method, annotation) -> {
            try {
                if(foreachResult != null)
                    foreachResult.accept(method.invoke(this, params), annotation);
                else
                    method.invoke(this, params);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }

    //region Overloads

    /*=================================================================================================
                        -  Overloads  -
    =================================================================================================*/

    /**
     * Invokes callbacks of type
     * @param type type of callbacks to invoke
     */
    public final void invokeCallbacks(Class<? extends Annotation> type) {
        invokeCallbacks(type, null);
    }


    //endregion

    //endregion



    //region Properties

    /*=================================================================================================
                        -  Properties  -
    =================================================================================================*/


    /**
     * @return Identification of this event's type, used for specifying registered event types creating events
     */
    public abstract NamespacedKey eventKey();

    /**
     * @return Whether players should automatically be set to join this event when they
     * join the server if they are registered and this event is running
     */
    public boolean autoJoin() {return false;}

    /**
     * @return Should players be ale to pvp without you having to do /pvp enable
     */
    public boolean canPvPByDefault()
    {
        return true;
    }
    /**
     * @return Should players be able to take damage while playing in this event
     */
    public boolean playersInvulnerable()
    {
        return false;
    }

    /**
     * @return Should players be able to return to hub when playing this event, players may abuse /hub
     * if some plugins don't account for that in their tracking
     */
    public abstract boolean preventLeavingWhilePlaying();

    /**
     * @return Priority for what event to join when doing a generic join. Higher number has higher priority.
     * Defaults to 10 in EventBase
     */
    public int priority()
    {
        return 10;
    }

    //endregion



    //region Join / Leave

    /*=================================================================================================
                        -  Join / Leave  -
    =================================================================================================*/

    /**
     * Makes a player join the event
     * @param player Player who joined
     * @return Whether the player was allowed to join
     */
    public final CompletableFuture<Boolean> join(Player player)
    {
        var id = player.getUniqueId();
        var lastEvent = EventTracker.getPlayingEvent(player.getUniqueId());

        //check if player can join first
        if(!canJoin(player))
        {
            joinFallback(player);
            var future = new CompletableFuture<Boolean>();
            future.complete(false);
            return future;
        }

        //leave the last event they were in
        if(lastEvent != null)
        {
            lastEvent.leave(player);
        }


        //process joinTasks
        try {
            CompletableFuture<Boolean> joinTask = processJoinTasks(player);

            //define the process after the task completes
            joinTask.thenAcceptAsync(result -> {
                if(result)
                {
                    //if they successfully joined this event set them as such in the tracker
                    EventTracker.setPlayingEvent(id, this);
                    eventPlayers.put(id, PlayerEventStatus.PLAYING);
                    EventTracker.limboPlayers.remove(id);
                }
                else
                {
                    joinFallback(player);
                }
            });

            return joinTask;
        }

        //fail joining
        catch (Exception e)
        {
            Advent.logger.warning("Unable To join Event " + eventKey());
            e.printStackTrace();
            joinFallback(player);
            var future = new CompletableFuture<Boolean>();
            future.complete(false);
            return future;
        }
    }

    /**
     * Called when the join task fails
     * @param player Player who failed to join
     */
    public void joinFallback(Player player)
    {
        System.out.println("Joining Fallback Event");
        EventTracker.getHubEvent().join(player);
    }

    /**
     * Processes the join tasks for a given player recursively
     * @param player Player to process
     * @return Future to be completed with a true if successfully joining or false with a failed joining
     */
    final CompletableFuture<Boolean> processJoinTasks(Player player)
            throws InvocationTargetException, IllegalAccessException {

        var iterator = joinTasks.entrySet().iterator();
        var joinFuture = new CompletableFuture<Boolean>();

        processJoinTasks(
                player,
                iterator,
                iterator.next().getValue().iterator(),
                joinFuture
        );

        return joinFuture;
    }

    /**
     * Processes the join tasks for a given player recursively
     * @param player Player to process
     * @param priorityIterator iterator through join task priorities (gotten by joinTasks.entrySet().iterator())
     * @param methodIterator iterator through the methods in the join task priorities
     * @param processFuture Future to be completed with a true if successfully joining or false with a failed joining
     */
    final void processJoinTasks(
            Player player,
            Iterator<Map.Entry<JoinTaskPriority, ArrayList<Method>>> priorityIterator,
            Iterator<Method> methodIterator,
            CompletableFuture<Boolean> processFuture
    ) throws InvocationTargetException, IllegalAccessException {

        CompletableFuture<Boolean> joinFuture = null;
        //run through methods that execute immediately which cant fail
        while(joinFuture == null && methodIterator.hasNext()){
            joinFuture = (CompletableFuture<Boolean>)methodIterator.next().invoke(this, player);
        }


        //if we found a future that needs to be completed first
        if(joinFuture != null)
        {
            joinFuture.thenAcceptAsync(result -> {
                if(result)
                {
                    //task completed successfully so continue processing
                    try {
                        processJoinTasks(
                                player,
                                priorityIterator,
                                methodIterator,
                                processFuture
                        );
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    //failed joining
                    processFuture.complete(false);
                }
            });
        }

        //if there is no other method for this priority then go to the next
        else {
            if(!priorityIterator.hasNext())
            {
                //out of elements so complete the process future and end processing
                processFuture.complete(true);
            }
            else
            {
                //still more priorities to process
                processJoinTasks(
                        player,
                        priorityIterator,
                        priorityIterator.next().getValue().iterator(),
                        processFuture
                );
            }
        }

    }

    /**
     * forces a player to leave the event. This DOES NOT send them to fallback so only be sure to call this if
     * they aren't going to be sent to a between event state or are leaving server
     * @param player Player who left
     */
    public void leave(Player player)
    {
        var id = player.getUniqueId();
        if(isPlaying(id))
        {
            //clear tracked event
            EventTracker.clearPlayingEvent(id);
            eventPlayers.put(id, PlayerEventStatus.ABSENT);
            invokeCallbacks(OnLeaveEvent.class, null, player);
        }
    }


    //endregion



    //region Controls

    /*=================================================================================================
                        -  Controls  -
    =================================================================================================*/

    /**
     * Tracks what command controls this event has
     */
    ArrayList<Cattamand> commandControls = new ArrayList<>();

    /**
     * Call to start event
     */
    public final void startEvent()
    {
        if(isRunning)
            return;

        isRunning = true;
        invokeCallbacks(OnEventStart.class);
    }

    /**
     * Call to stop event
     */
    public final void stopEvent()
    {
        if(!isRunning)
            return;

        //make all players leave to fallback
        getPlayingPlayers().forEach(this::joinFallback);

        isRunning = false;
        invokeCallbacks(OnEventStop.class);
    }

    /**
     * Call to freeze the event
     */
    public final void freeze()
    {
        isFrozen = true;
        invokeCallbacks(OnFreezeEvent.class);
    }

    /**
     * Call to unfreeze the event
     */
    public final void unfreeze()
    {
        isFrozen = false;
        invokeCallbacks(OnUnfreezeEvent.class);
    }

    /**
     * @return Whether the event is currently frozen
     */
    public final boolean isFrozen()
    {
        return isFrozen;
    }

    /**
     * Call to allow pvp the event
     */
    public final void allowPVP()
    {
        allowPvp = true;
    }

    /**
     * Call to prevent pvp in the event
     */
    public final void preventPVP()
    {
        allowPvp = false;
    }

    /**
     * @return Whether pvp is allowed in the event
     */
    public final boolean isPVPAllowed()
    {
        return allowPvp;
    }


    /**
     * Registers a cattamand as a control for this event,
     * must be called BEFORE this event is registered with the event manager
     * @param control Cattamand to add
     */
    public final void registerCommandControl(Cattamand control)
    {
        commandControls.add(control);
    }

    /**\
     * @return all Cattamand controls provided by the interfaces
     */
    public final Cattamand getEventCommandTree() {

        commandControls.add(new JoinEventCommand(this));
        commandControls.add(new SaveEventCommand(this));
        commandControls.add(new EventInfoCommand(this));
        commandControls.add(new FreezeEventCommand(this));
        commandControls.add(new StartEventCommand(this));
        commandControls.add(new StopEventCommand(this));
        commandControls.add(new UnfreezeEventCommand(this));
        commandControls.add(new SetPvPCommand(this));

        invokeCallbacks(EventControl.class, (result, annotation) -> {
            commandControls.add((Cattamand)result);
        });

        return new LiteralCattamand(eventKey().toString(), commandControls);

    }


    //endregion



    //region Events

    /*=================================================================================================
                        -  Events  -
    =================================================================================================*/

    /**
     * Called when the event is started
     */
    @OnEventStart
    public abstract void onEventStart();

    /**
     * Called when the event is stopped
     */
    @OnEventStop
    public abstract void onEventStop();

    /**
     * Called when a player joins an event
     * @param player Player who joined
     */
    @OnJoinEvent
    public abstract void onJoinEvent(Player player);

    /**
     * Called when a player leaves an event
     * @param player Player who left
     */
    @OnLeaveEvent
    public abstract void onLeaveEvent(Player player);

    //endregion



    //region YAML

    /*=================================================================================================
                        -  YAML  -
    =================================================================================================*/

    /**
     * Writes data to yaml data for saving
     */
    @OverridingMethodsMustInvokeSuper
    public final YamlConfiguration writeToYaml()
    {
        YamlConfiguration yaml = new YamlConfiguration();

        //write players
        var playerMap = getPlayerStatusMap();
        yaml.set("players", playerMap.keySet().stream().map(UUID::toString).toList());
        yaml.set("running", isRunning);
        yaml.set("frozen", isFrozen);
        yaml.set("pvp", allowPvp);

        //write modules
        invokeCallbacks(WriteYaml.class, (result, annotation) -> {
            YamlConfiguration moduleYaml = ((YamlConfiguration)result);

            moduleYaml.getKeys(false).forEach(key -> {
                if(!yaml.contains(key))
                    yaml.set(key, moduleYaml.get(key));
            });
        });

        return yaml;
    }

    /**
     * Reads data from a yaml data for loading save data
     * @param yaml yaml to read from
     */
    @OverridingMethodsMustInvokeSuper
    public final void readFromYaml(YamlConfiguration yaml)
    {
        //read players
        List<String> stringPlayers = yaml.getStringList("players");

        stringPlayers.forEach(player -> {
            eventPlayers.put(UUID.fromString(player), PlayerEventStatus.ABSENT);
        });

        isRunning = yaml.getBoolean("running");
        isFrozen = yaml.getBoolean("frozen");
        allowPvp = yaml.getBoolean("pvp");

        //read modules
        invokeCallbacks(ReadYaml.class, null, yaml);

    }


    //endregion



    //region Getting Data

    /*=================================================================================================
                        -  Getting Data  -
    =================================================================================================*/

    /**
     * Gets a set of all tracked players
     */
    public final HashSet<UUID> getEventPlayers()
    {
        return new HashSet<>(eventPlayers.keySet());
    }

    /**
     * Gets a set of all event players who are online (not PLAYING, but online)
     */
    public final List<Player> getOnlinePlayers()
    {
        return eventPlayers.keySet().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).toList();
    }

    /**
     * Gets a set of all registered players who are online (not PLAYING, but online)
     */
    public final List<Player> getPlayingPlayers()
    {
        return eventPlayers.entrySet().stream().filter(entry ->
                isPlaying(entry.getKey())
        ).map(entry ->
                Bukkit.getPlayer(entry.getKey())
        ).filter(Objects::nonNull).toList();
    }

    /**
     * Gets a copy of the registered player status map
     */
    public final HashMap<UUID, PlayerEventStatus> getPlayerStatusMap()
    {
        return new HashMap<>(eventPlayers);
    }


    /**
     * Is a player currently playing in this event
     * @param id Id of Player to check
     * @return whether they are playing
     */
    public final boolean isPlaying(UUID id)
    {
        return eventPlayers.get(id) == PlayerEventStatus.PLAYING;
    }


    public boolean canJoin(Player player)
    {
        var currentEvent = EventTracker.getPlayingEvent(player.getUniqueId());

        if(!isRunning ||
            !(player.isOp() ||
                (
                    (currentEvent == null || !currentEvent.preventLeavingWhilePlaying()) &&
                    !isPlaying(player.getUniqueId())
                )
            ))
        {
            return false;
        }

        AtomicBoolean allSuccess = new AtomicBoolean(true);

        invokeCallbacks(CanJoinEvent.class, (result, ann) -> {
            if(!((boolean) result))
                allSuccess.set(false);
        }, player);

        return allSuccess.get();
    }

    /**
     * @param id ID of player to check
     * @return Event status of the provided player
     */
    public final PlayerEventStatus getStatus(UUID id)
    {
        return eventPlayers.getOrDefault(id, PlayerEventStatus.OMITTED);
    }


    /**
     * Returns module data for a provided name
     * @param name Name of key
     * @return The object stored in moduleData
     */
    public final Object getModuleData(String name)
    {
        return moduleData.get(name);
    }

    /**
     * Overwrites module data for a provided name
     * @param name Name of key
     * @param value The object to be stored in moduleData
     */
    public final void setModuleData(String name, Object value)
    {
        moduleData.put(name, value);
    }

    /**
     * @return Whether this event is active
     */
    public final boolean isRunning()
    {
        return isRunning;
    }


    //region Overloads

    //----------= Overloads =-----------------------------------------------------------------------------

    /**
     * Is a player registered to play in this event and is of a filtered status (overload)
     * @param id Id of Player to check
     * @param filtered Statuses to filter, if a player is not of one of these it is false
     * @return Registration status
     */
    public final boolean isEventPlayer(UUID id, PlayerEventStatus... filtered)
    {
        var status = eventPlayers.get(id);

        //if not registered don't search
        if(status == null) return false;

        //filter
        for (var stat : filtered)
        {
            if(status == stat)
                return true;
        }

        return false;
    }

    /**
     * @param player Player to check (overload)
     * @return Event status of the provided player
     */
    public final PlayerEventStatus getStatus(Player player)
    {
        return getStatus(player.getUniqueId());
    }



    //endregion


    //endregion



    //region Automatic Module Data

    /*=================================================================================================
                        -  Automatic Module Data  -
    =================================================================================================*/

    /**
     * tracks keys to data which are of a special type which get automatic yaml reading and writing
     */
    final HashMap<Annotation, ArrayList<String>> automaticDataKeys = new HashMap<>();

    /**
     * Tracks the ModuleDataProvider of each automated data element by their String keys. Used to get data
     * tracked on the ModuleDataProvider like File Name
     */
    final HashMap<String, ModuleDataProvider> automaticModuleAnnotations = new HashMap<>();

    /**
     * A dynamic way to add automated data to the module data, Call BEFORE reading in YAML (OnInitalize)
     * @param annotation Type of data to add
     * @param dataProvider Data Provider Annotation
     * @param data Data to save
     */
    public final void registerAutomatedData(Annotation annotation, ModuleDataProvider dataProvider, Object data)
    {
        moduleData.put(dataProvider.key(), data);
        automaticModuleAnnotations.put(dataProvider.key(), dataProvider);
        automaticDataKeys.computeIfAbsent(annotation, (ann) -> new ArrayList<>()).add(dataProvider.key());
    }

    /**
     * Checks for Module data type annotations which we have yaml read and write definitions for already
     * @param method Method to check
     */
    final void registerAutomaticData(ModuleDataProvider dataProvider, Method method)
    {
        for(Annotation ann : method.getAnnotations())
        {
            if(AutomaticModuleDataManager.readingMethods.containsKey(ann.annotationType()))
            {
                automaticDataKeys.computeIfAbsent(
                        ann,
                        annn -> new ArrayList<>()
                ).add(dataProvider.key());
                automaticModuleAnnotations.put(dataProvider.key(), dataProvider);
            }
        }
    }

    /**
     * @return YamlConfiguration with all automatically written saved to it. File Creation is handled here
     * separate because of the different files some data can be under
     */
    @WriteYaml
    public final YamlConfiguration writeAutomaticData()
    {
        HashMap<String, YamlConfiguration> configurationFiles = new HashMap<>();
        configurationFiles.put("event", new YamlConfiguration());

        //get event data file path
        Path eventDirectory = Advent.singleton.getDataFolder().toPath().resolve("Events")
                .resolve(eventKey().getNamespace()).resolve(eventKey().getKey());

        automaticDataKeys.forEach((annotation, keys) -> {
            keys.forEach(key -> {

                var dataProvider = automaticModuleAnnotations.get(key);

                //if it is for the normal event file then send in the yaml we are returning
                if(dataProvider.fileBasename().equalsIgnoreCase("event"))
                {
                    AutomaticModuleDataManager.writingMethods.get(annotation.annotationType())
                            .accept(annotation, configurationFiles.get("event"), key, moduleData.get(key));
                }
                else
                {
                    String fileBasename = dataProvider.fileBasename();

                    //it is a custom yaml file so we have to create and save it here
                    if(!Files.exists(eventDirectory))
                    {
                        try {
                            Files.createDirectories(eventDirectory);
                        } catch (IOException e) {
                            Advent.logger.warning("UNABLE TO SAVE " + key);
                            e.printStackTrace();
                        }
                    }

                    //write to yaml
                    YamlConfiguration separateYaml = configurationFiles.computeIfAbsent(
                            fileBasename
                            , (k) -> new YamlConfiguration()
                    );
                    AutomaticModuleDataManager.writingMethods.get(annotation.annotationType())
                            .accept(annotation, separateYaml, key, moduleData.get(key));

                }
            });
        });

        configurationFiles.forEach((baseName, yaml) -> {
            if(baseName.equalsIgnoreCase("event"))
            {
                return;
            }

            Path yamlFile = eventDirectory.resolve(baseName + ".yaml");
            if(!Files.exists(yamlFile))
            {
                try {
                    Files.createFile(yamlFile);
                } catch (IOException e) {
                    Advent.logger.warning("UNABLE TO SAVE " + baseName);
                    e.printStackTrace();
                }
            }

            try {
                yaml.save(yamlFile.toFile());
            } catch (IOException e) {
                Advent.logger.warning("UNABLE TO SAVE " + baseName);
                e.printStackTrace();
            }
        });

        return configurationFiles.get("event");
    }

    /**
     * @param yaml YamlConfiguration to read in automatically handled data
     */
    @ReadYaml
    public final void readAutomaticData(YamlConfiguration yaml)
    {
        //get the path to the Menus directory
        Path eventDirectory = Advent.singleton.getDataFolder()
                .toPath()
                .resolve("Events")
                .resolve(eventKey().getNamespace())
                .resolve(eventKey().getKey());


        automaticDataKeys.forEach((annotation, keys) -> {
            keys.forEach(key -> {
                var dataProvider = automaticModuleAnnotations.get(key);
                if(dataProvider.key().equalsIgnoreCase("event"))
                {
                    try {
                        var storedData = AutomaticModuleDataManager.readingMethods
                                .get(annotation.annotationType()).apply(annotation, yaml, key);
                        if(storedData != null)
                            moduleData.put(key, storedData);
                    }
                    catch (Exception e)
                    {
                        System.err.println("Advent Unable to read data Module type for: " + annotation.annotationType());
                        e.printStackTrace();
                    }
                }
                else
                {
                    Path sectionFile = eventDirectory.resolve(dataProvider.fileBasename() + ".yaml");

                    //no data to read
                    if(!Files.exists(sectionFile))
                    {
                        System.out.println("NO FILE TO READ " + sectionFile.toString());
                        return;
                    }

                    var sectionYaml = YamlConfiguration.loadConfiguration(sectionFile.toFile());

                    var storedData = AutomaticModuleDataManager.readingMethods
                            .get(annotation.annotationType()).apply(annotation, sectionYaml, key);
                    if(storedData != null)
                        moduleData.put(key, storedData);
                }
            });
        });
    }

    //endregion



    //region Event Info

    /*=================================================================================================
                        -  Event Info  -
    =================================================================================================*/


    /**
     * @return A list of event information sections found by functions tagged with @ModuleInfoProvider
     */
    public final HashBasedTable<EventInfoProvider, String, Object> getEventInfo()
    {
        HashBasedTable<EventInfoProvider, String, Object> infoSections = HashBasedTable.create();

        invokeCallbacks(EventInfoProvider.class, (result, annotation) -> {
            EventInfoProvider info = ((EventInfoProvider)annotation);

            infoSections.row(info).putAll((Map<String, Object>) result);
        });

        return infoSections;
    }



    /**
     * @return Provides information section on what players are actively playing in the event
     */
    @EventInfoProvider(name = "Playing", separate = true)
    public final Map<String, Object> playingInfo()
    {
        HashMap<String, Object> sectionMap = new HashMap<>();
        StringBuilder output = new StringBuilder();
        var players = getPlayingPlayers();

        AtomicInteger counter = new AtomicInteger(0);
        players.stream().map(OfflinePlayer::getName).forEach(name -> {
            boolean even = counter.getAndIncrement() % 2 == 0;

            output.append(even ? "<color:#c4c4c4>" : "</color:#c4c4c4>").append(name).append(' ');

        });

        sectionMap.put("Count", players.size());
        sectionMap.put("Players", output.toString());

        return sectionMap;
    }


    /**
     * @return Provides information on the state of the event
     */
    @EventInfoProvider(name = "Basic", separate = false)
    public final Map<String, Object> basicInfo()
    {
        HashMap<String, Object> sectionMap = new HashMap<>();

        int registeredSize = getEventPlayers().size();

        sectionMap.put("Registered Count", registeredSize);
        sectionMap.put("Playing Count", getPlayingPlayers().size() + " out of " + registeredSize + " registered");
        sectionMap.put("<hover:show_text:'Whether the event is currentley running'>Event State</hover>",
                isRunning ?  "<bold><green>STARTED<reset>" : "<bold><red>STOPPED<reset>");

        return sectionMap;
    }

    //endregion



    //region Comparability

    /*=================================================================================================
                        -  Comparability  -
    =================================================================================================*/

    @Override
    public int compareTo(@NotNull EventBase o) {
        int diff = Integer.compare(priority(), o.priority());
        if(diff == 0)
            diff = o.eventKey().toString().compareTo(eventKey().toString());
        return diff;
    }

    //endregion

}
