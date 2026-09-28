package cd.arnett.advent.events;

import cd.arnett.advent.Advent;
import cd.arnett.advent.EventTracker;
import cd.arnett.advent.events.annotations.*;
import cd.arnett.advent.events.annotations.ReadYaml;
import cd.arnett.advent.events.annotations.WriteYaml;
import cd.arnett.advent.events.controls.*;
import cd.arnett.advent.events.controls.registration.AddRegistrationCommand;
import cd.arnett.advent.events.controls.registration.RemoveRegistrationCommand;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import cd.arnett.caddamands.cattamands.cattamand.LiteralCattamand;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.collect.HashBasedTable;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;


/**
 * Provides data about events of this type, by registering this type you can then create
 * events of this type through commands.
 */
public abstract class EventBase {

    HashMap<UUID, PlayerEventStatus> registeredPlayers = new HashMap<>();
    HashMap<Class<? extends Annotation>, HashMap<Method, Annotation>> callbacks = new HashMap<>();

    /**
     * stores data from modules
     */
    HashMap<String, Object> moduleData = new HashMap<>();

    boolean isActive = false;
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


            for (Annotation annotation : annotations)
            {
                callbacks.computeIfAbsent(annotation.annotationType(),
                        ann -> new HashMap<>()).put(method, annotation);


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
    final void invokeCallbacks(Class<? extends Annotation> type, BiConsumer<Object, Annotation> foreachResult, Object... params)
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
    final void invokeCallbacks(Class<? extends Annotation> type) {
        invokeCallbacks(type, null);
    }


    //endregion

    //endregion



    //region Abstract properties

    /*=================================================================================================
                        -  Abstract properties  -
    =================================================================================================*/


    /**
     * @return Identification of this event's type, used for specifying registered event types creating events
     */
    public abstract NamespacedKey eventKey();

    /**
     * @return Whether players have to be registered with the event beforehand to join. If false,
     * players will be registered if not already when attempting to join
     */
    public abstract boolean requiresRegistration();

    /**
     * @return Whether players should automatically be set to join this event when they
     * join the server if they are registered and this event is running
     */
    public abstract boolean autoJoin();

    /**
     * @return Should this event automatically start when it is registered with the EventTracker
     */
    public abstract boolean autoOpen();

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
    public final void openEvent()
    {
        if(isActive)
            return;

        isActive = true;
        invokeCallbacks(OnEventOpen.class);
    }

    /**
     * Call to stop event
     */
    public final void closeEvent()
    {
        if(!isActive)
            return;

        //make all players leave
        getPlayingPlayers().forEach(this::leave);

        isActive = false;
        invokeCallbacks(OnEventClose.class);
    }

    /**
     * Makes a player join the event if registered (Absent -> Playing)
     * @param player Player who joined
     * @return Whether the player was allowed to join
     */
    public final boolean join(Player player)
    {
        var id = player.getUniqueId();
        var lastEvent = EventTracker.getPlayingEvent(player.getUniqueId());

        //check if player can join first
        if(!canJoin(player))
        {
            return false;
        }

        //leave the last event they were in
        if(lastEvent != null)
        {
            lastEvent.leave(player);
        }

        //set player to playing
        registeredPlayers.put(id, PlayerEventStatus.PLAYING);
        EventTracker.setPlayingEvent(id, this);
        invokeCallbacks(OnJoinEvent.class, null, player);
        return true;
    }

    /**
     * forces a player to leave the event (if you want to revoke registration this is different)
     * @param player Player who left
     */
    public final void leave(Player player)
    {
        var id = player.getUniqueId();

        if(isPlaying(id))
        {
            //remove this from their event stack
            registeredPlayers.put(id, PlayerEventStatus.ABSENT);
            invokeCallbacks(OnLeaveEvent.class, null, player);
        }
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
    public final List<Cattamand> getCommandControls() {

        commandControls.add(new JoinEventCommand());
        commandControls.add(new SaveEventCommand());
        commandControls.add(new CloseEventCommand());
        commandControls.add(new OpenEventCommand());
        commandControls.add(new EventInfoCommand());
        commandControls.add(new FreezeEventCommand());
        commandControls.add(new UnfreezeEventCommand());
        commandControls.add(new SetPvPCommand());
        commandControls.add(new LiteralCattamand("registrations", List.of(
                new AddRegistrationCommand(),
                new RemoveRegistrationCommand()
        )));

        invokeCallbacks(EventControl.class, (result, annotation) -> {
            commandControls.add((Cattamand)result);
        });

        return commandControls;

    }


    //endregion



    //region Events

    /*=================================================================================================
                        -  Events  -
    =================================================================================================*/

    /**
     * Called when the event is started
     */
    @OnEventOpen
    public abstract void onEventOpen();

    /**
     * Called when the event is stopped
     */
    @OnEventClose
    public abstract void onEventClose();

    /**
     * Called when a player joins an event
     * @param player player who joined
     */
    @OnJoinEvent
    public abstract void onJoinEvent(Player player);

    /**
     * Called when a player leaves an event
     * @param player player who left
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
        yaml.set("active", isActive);
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
            registeredPlayers.put(UUID.fromString(player), PlayerEventStatus.ABSENT);
        });

        isActive = yaml.getBoolean("active");
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
     * Gets a set of all registered players
     */
    public final HashSet<UUID> getRegisteredPlayers()
    {
        return new HashSet<>(registeredPlayers.keySet());
    }

    /**
     * Gets a set of all registered players who are online (not PLAYING, but online)
     */
    public final List<Player> getOnlinePlayers()
    {
        return registeredPlayers.keySet().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).toList();
    }

    /**
     * Gets a set of all registered players who are online (not PLAYING, but online)
     */
    public final List<Player> getPlayingPlayers()
    {
        return registeredPlayers.entrySet().stream().filter(entry ->
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
        return new HashMap<>(registeredPlayers);
    }

    /**
     * Is a player registered to play in this event
     * @param id Id of Player to check
     * @return Registration status
     */
    public final boolean isRegistered(UUID id)
    {
        return registeredPlayers.containsKey(id);
    }

    /**
     * Is a player currently playing in this event
     * @param id Id of Player to check
     * @return whether they are playing
     */
    public final boolean isPlaying(UUID id)
    {
        return registeredPlayers.get(id) == PlayerEventStatus.PLAYING;
    }


    public boolean canJoin(Player player)
    {
        var currentEvent = EventTracker.getPlayingEvent(player.getUniqueId());

        if(!(player.isOp() ||
            (
                    isActive &&
                            (currentEvent == null || !currentEvent.preventLeavingWhilePlaying()) &&
                            !isPlaying(player.getUniqueId()) &&
                            ((
                                    !requiresRegistration() ||
                                            registeredPlayers.containsKey(player.getUniqueId())
                            ))
            )))
        {
            return false;
        }

        AtomicBoolean allSuccess = new AtomicBoolean(true);

        invokeCallbacks(CanJoinEvent.class, (result, ann) -> {
            if(!((boolean) result))
                allSuccess.set(false);
        }, player);

        //leave if we require registration and player is not registered, or they are already playing another event
        return allSuccess.get();
    }

    /**
     * @param id ID of player to check
     * @return Event status of the provided player
     */
    public final PlayerEventStatus getStatus(UUID id)
    {
        return registeredPlayers.getOrDefault(id, PlayerEventStatus.OMITTED);
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
    public final boolean isActive()
    {
        return isActive;
    }


    //region Overloads

    //----------= Overloads =-----------------------------------------------------------------------------

    /**
     * Is a player registered to play in this event (overload)
     * @param player Player to check
     * @return Registration status
     */
    public final boolean isRegistered(Player player)
    {
        return isRegistered(player.getUniqueId());
    }

    /**
     * Is a player registered to play in this event and is of a filtered status (overload)
     * @param id Id of Player to check
     * @param filtered Statuses to filter, if a player is not of one of these it is false
     * @return Registration status
     */
    public final boolean isRegistered(UUID id, PlayerEventStatus... filtered)
    {
        var status = registeredPlayers.get(id);

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



    //region Registration

    /*=================================================================================================
                        -  Registration  -
    =================================================================================================*/

    /**
     * Registers a player for this event (overload)
     * @param player Profile of player to register
     */
    public final void register(PlayerProfile player)
    {
        registeredPlayers.put(player.getId(), PlayerEventStatus.ABSENT);
        invokeCallbacks(OnRegistrationAdd.class, null, player);
    }


    /**
     * Revokes Registration from an event (and removes them if they are playing)
     * @param player Profile of player to register
     */
    public final void revokeRegistration(PlayerProfile player)
    {
        if(isPlaying(player.getId()))
        {
            Player playerObj = Bukkit.getPlayer(player.getId());
            leave(playerObj);
            EventTracker.getHubEvent().join(playerObj);
        }

        registeredPlayers.remove(player.getId());
        invokeCallbacks(OnRegistrationRemove.class, null, player);
    }

    //region Overloads

    //----------= Overloads =-----------------------------------------------------------------------------


    /**
     * Registers a player for this event, removes them from the revoked list if present
     * @param id ID of player to register
     */
    public final void register(UUID id)
    {
        register(Bukkit.createProfile(id));
    }


    /**
     * Revokes Registration from an event (and removes them if they are playing) (overload)
     * @param id ID of Player to register
     */
    public final void revokeRegistration(UUID id)
    {
        revokeRegistration(Bukkit.createProfile(id));
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
            System.out.println("reading " + annotation.annotationType());
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
    @EventInfoProvider(name = "Registered", separate = true)
    public final Map<String, Object> registeredInfo()
    {
        HashMap<String, Object> sectionMap = new HashMap<>();
        StringBuilder output = new StringBuilder();
        var players = getRegisteredPlayers();

        AtomicInteger counter = new AtomicInteger(0);
        players.stream().map(Bukkit::getOfflinePlayer).map(OfflinePlayer::getName).forEach(name -> {
            boolean even = counter.getAndIncrement() % 2 == 0;

            output.append(even ? "<color:#c4c4c4>" : "</color:#c4c4c4>").append(name).append(' ');

        });

        sectionMap.put("Count", players.size());
        sectionMap.put("Players", output.toString());

        return sectionMap;
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

        int registeredSize = getRegisteredPlayers().size();

        sectionMap.put("Registered Count", registeredSize);
        sectionMap.put("Playing Count", getPlayingPlayers().size() + " out of " + registeredSize + " registered");
        sectionMap.put("<hover:show_text:'Whether the event is currentley running'>Event State</hover>",
                isActive ?  "<bold><green>ACTIVE<reset>" : "<bold><red>INACTIVE<reset>");

        return sectionMap;
    }

    //endregion

}
