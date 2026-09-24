package cd.arnett.advent.events;

import cd.arnett.advent.Advent;
import cd.arnett.advent.events.annotations.*;
import cd.arnett.advent.events.annotations.ModuleDataProvider;
import cd.arnett.advent.events.annotations.ReadYaml;
import cd.arnett.advent.events.annotations.WriteYaml;
import cd.arnett.advent.events.annotations.data.*;
import cd.arnett.advent.events.controls.*;
import cd.arnett.advent.events.controls.registration.AddRegistrationCommand;
import cd.arnett.advent.events.controls.registration.RemoveRegistrationCommand;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import cd.arnett.caddamands.cattamands.cattamand.LiteralCattamand;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.collect.HashBasedTable;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
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
    void loadCallbacks() {
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
            }


            if(method.isAnnotationPresent(ModuleDataProvider.class))
            {
                ModuleDataProvider[] trackedListAnnotations = method.getAnnotationsByType(ModuleDataProvider.class);
                for(ModuleDataProvider ann : trackedListAnnotations)
                {
                    //clone field to data list
                    try {
                        moduleData.put(ann.value(), method.invoke(this));
                        registerAutomaticData(ann.value(), method);
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
    void invokeCallbacks(Class<? extends Annotation> type, BiConsumer<Object, Annotation> foreachResult, Object... params)
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
    void invokeCallbacks(Class<? extends Annotation> type) {
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
    public void startEvent()
    {
        isActive = true;
        invokeCallbacks(OnEventStart.class);
    }

    /**
     * Call to stop event
     */
    public void stopEvent()
    {
        isActive = false;
        invokeCallbacks(OnEventStop.class);
    }

    /**
     * Makes a player join the event if registered (Absent -> Playing)
     * @param player player who is joining
     */
    public void join(Player player)
    {
        var id = player.getUniqueId();

        //leave if we require registration and player is not registered
        if(requiresRegistration() && !registeredPlayers.containsKey(id))
        {
            return;
        }

        //set player to playing
        registeredPlayers.put(id, PlayerEventStatus.PLAYING);
        invokeCallbacks(OnJoinEvent.class, null, player);

    }

    /**
     * forces a player to leave the event (if you want to revoke registration this is different)
     * @param player Player who is leaving
     */
    public void leave(Player player)
    {
        var id = player.getUniqueId();
        if(registeredPlayers.containsKey(id))
        {
            registeredPlayers.put(id, PlayerEventStatus.ABSENT);
            invokeCallbacks(OnLeaveEvent.class, null, player);
        }
    }

    /**
     * Registers a cattamand as a control for this event,
     * must be called BEFORE this event is registered with the event manager
     * @param control Cattamand to add
     */
    public void registerCommandControl(Cattamand control)
    {
        commandControls.add(control);
    }

    /**\
     * @return all Cattamand controls provided by the interfaces
     */
    public List<Cattamand> getCommandControls() {

        commandControls.add(new JoinEventCommand());
        commandControls.add(new JoinEventCommand());
        commandControls.add(new LeaveEventCommand());
        commandControls.add(new SaveEventCommand());
        commandControls.add(new StopEventCommand());
        commandControls.add(new StartEventCommand());
        commandControls.add(new EventInfoCommand());
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
    @OnEventStart
    public abstract void onEventStart();

    /**
     * Called when the event is stopped
     */
    @OnEventStop
    public abstract void onEventStop();

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
    public YamlConfiguration writeToYaml()
    {
        YamlConfiguration yaml = new YamlConfiguration();

        //write players
        var playerMap = getPlayerStatusMap();
        yaml.set("players", playerMap.keySet().stream().map(UUID::toString).toList());

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
    public void readFromYaml(YamlConfiguration yaml)
    {
        //read players
        List<String> stringPlayers = yaml.getStringList("players");

        stringPlayers.forEach(player -> {
            registeredPlayers.put(UUID.fromString(player), PlayerEventStatus.ABSENT);
        });

        System.out.println("reading yaml for " + eventKey());
        System.out.println("read size " + callbacks.get(ReadYaml.class).size());
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
    public HashSet<UUID> getRegisteredPlayers()
    {
        return new HashSet<>(registeredPlayers.keySet());
    }

    /**
     * Gets a set of all registered players who are online (not PLAYING, but online)
     */
    public List<Player> getOnlinePlayers()
    {
        return registeredPlayers.keySet().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).toList();
    }

    /**
     * Gets a set of all registered players who are online (not PLAYING, but online)
     */
    public List<Player> getPlayingPlayers()
    {
        return registeredPlayers.entrySet().stream().filter(entry ->
                entry.getValue() == PlayerEventStatus.PLAYING
        ).map(entry ->
                Bukkit.getPlayer(entry.getKey())
        ).filter(Objects::nonNull).toList();
    }

    /**
     * Gets a copy of the registered player status map
     */
    public HashMap<UUID, PlayerEventStatus> getPlayerStatusMap()
    {
        return new HashMap<>(registeredPlayers);
    }

    /**
     * Is a player registered to play in this event
     * @param id Id of Player to check
     * @return Registration status
     */
    public boolean isRegistered(UUID id)
    {
        return registeredPlayers.containsKey(id);
    }

    /**
     * @param id ID of player to check
     * @return Event status of the provided player
     */
    public PlayerEventStatus getStatus(UUID id)
    {
        return registeredPlayers.getOrDefault(id, PlayerEventStatus.OMITTED);
    }


    /**
     * Returns module data for a provided name
     * @param name Name of list
     * @return The object stored in moduleData
     */
    public Object getModuleData(String name)
    {
        return moduleData.get(name);
    }


    //region Overloads

    //----------= Overloads =-----------------------------------------------------------------------------

    /**
     * Is a player registered to play in this event (overload)
     * @param player Player to check
     * @return Registration status
     */
    public boolean isRegistered(Player player)
    {
        return isRegistered(player.getUniqueId());
    }

    /**
     * Is a player registered to play in this event and is of a filtered status (overload)
     * @param id Id of Player to check
     * @param filtered Statuses to filter, if a player is not of one of these it is false
     * @return Registration status
     */
    public boolean isRegistered(UUID id, PlayerEventStatus... filtered)
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
    public PlayerEventStatus getStatus(Player player)
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
    public void register(PlayerProfile player)
    {
        registeredPlayers.put(player.getId(), PlayerEventStatus.ABSENT);
        invokeCallbacks(OnRegistrationAdd.class, null, player);
    }


    /**
     * Revokes Registration from an event (and removes them if they are playing)
     * @param player Profile of player to register
     */
    public void revokeRegistration(PlayerProfile player)
    {
        if(registeredPlayers.get(player.getId()) == PlayerEventStatus.PLAYING)
            leave(Bukkit.getPlayer(player.getId()));

        registeredPlayers.remove(player.getId());
        invokeCallbacks(OnRegistrationAdd.class, null, player);
    }

    //region Overloads

    //----------= Overloads =-----------------------------------------------------------------------------


    /**
     * Registers a player for this event, removes them from the revoked list if present
     * @param id ID of player to register
     */
    public void register(UUID id)
    {
        register(Bukkit.createProfile(id));
    }


    /**
     * Revokes Registration from an event (and removes them if they are playing) (overload)
     * @param id ID of Player to register
     */
    public void revokeRegistration(UUID id)
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
    HashMap<Annotation, ArrayList<String>> automaticDataKeys = new HashMap<>();

    /**
     * A dynamic way to add automated data to the module data, Call BEFORE reading in YAML (OnInitalize)
     * @param annotation Type of data to add
     * @param name Name to put the data under
     * @param data Data to save
     */
    public void registerAutomatedData(Annotation annotation, String name, Object data)
    {
        moduleData.put(name, data);
        automaticDataKeys.computeIfAbsent(annotation, (ann) -> new ArrayList<>()).add(name);
    }

    /**
     * Checks for Module data type annotations which we have yaml read and write definitions for already
     * @param method Method to check
     */
    void registerAutomaticData(String key, Method method)
    {
        for(Annotation ann : method.getAnnotations())
        {
            if(readingMethods.containsKey(ann.annotationType()))
            {
                automaticDataKeys.computeIfAbsent(
                        ann,
                        annn -> new ArrayList<>()
                ).add(key);
            }
        }
    }

    /**
     * @return YamlConfiguration with all automatically written saved to it
     */
    @WriteYaml
    public YamlConfiguration writeAutomaticData()
    {
        YamlConfiguration yaml = new YamlConfiguration();

        automaticDataKeys.forEach((annotation, keys) -> {
            keys.forEach(key -> {
                writingMethods.get(annotation.annotationType()).accept(annotation, yaml, key, moduleData.get(key));
            });
        });

        return yaml;
    }

    /**
     * @param yaml YamlConfiguration to read in automatically handled data
     */
    @ReadYaml
    public void readAutomaticData(YamlConfiguration yaml)
    {
        automaticDataKeys.forEach((annotation, keys) -> {
            keys.forEach(key -> {
                try {
                    moduleData.put(key, readingMethods.get(annotation.annotationType()).apply(annotation, yaml, key));
                }
                catch (Exception e)
                {
                    System.err.println("Advent Unable to read data Module type for: " + annotation.annotationType());
                    e.printStackTrace();
                }
            });
        });
    }


    @FunctionalInterface
    interface QuadConsumer<A, B, C, D> {
        void accept(A a, B b, C c, D d);
    }

    /**
     * Map of writing functions for each tracked annotation type
     */
    static HashMap<Class<? extends Annotation>, QuadConsumer<Annotation, ConfigurationSection, String, Object>> writingMethods = new HashMap<>();

    /**
     * Map of reading functions for each tracked annotation type
     */
    static HashMap<Class<? extends Annotation>, TriFunction<Annotation, ConfigurationSection, String, ?>> readingMethods = new HashMap<>();

    static
    {
        //reading
        readingMethods.put(
                IntegerModuleData.class, (ann, yaml, key)
                        -> yaml.getInt(key)
        );
        readingMethods.put(
                BooleanModuleData.class, (ann, yaml, key)
                        -> yaml.getBoolean(key)
        );
        readingMethods.put(
                FloatModuleData.class, (ann, yaml, key)
                        -> (float)yaml.getDouble(key)
        );
        readingMethods.put(
                DoubleModuleData.class, (ann, yaml, key)
                        -> yaml.getDouble(key)
        );
        readingMethods.put(
                StringModuleData.class, (ann, yaml, key)
                        -> yaml.getString(key)
        );
        readingMethods.put(
                UUIDModuleData.class, (ann, yaml, key)
                        -> UUID.fromString(yaml.getString(key))
        );

        //writing
        writingMethods.put(
                IntegerModuleData.class, (ann, yaml, key, value)
                        -> yaml.set(key, value)
        );
        writingMethods.put(
                BooleanModuleData.class, (ann, yaml, key, value)
                        -> yaml.set(key, value)
        );
        writingMethods.put(
                FloatModuleData.class, (ann, yaml, key, value)
                        -> yaml.set(key, value)
        );
        writingMethods.put(
                DoubleModuleData.class, (ann, yaml, key, value)
                        -> yaml.set(key, value)
        );
        writingMethods.put(
                StringModuleData.class, (ann, yaml, key, value)
                        -> yaml.set(key, value)
        );
        writingMethods.put(
                UUIDModuleData.class, (ann, yaml, key, value)
                        -> yaml.set(key, value.toString())
        );



        //region HashSet

        //----------= HashSet =-----------------------------------------------------------------------------


        readingMethods.put(
                HashSetModuleData.class, (ann, yaml, key)
                        -> {
                    HashSet<Object> set = new HashSet<>();
                    ConfigurationSection section = yaml.getConfigurationSection(key);
                    if(section == null)
                        return set;

                    section.getValues(false).forEach((id, value) -> {
                        set.add(
                                readingMethods.get(
                                        ((HashSetModuleData)ann).value()
                                ).apply(null, section, id)
                        );
                    });
                    return set;
                }
        );

        writingMethods.put(
                HashSetModuleData.class, (ann, yaml, key, value)
                        -> {
                    ConfigurationSection section = yaml.createSection(key);
                    int i = 0;

                    for (Object entry : (HashSet<Object>) value)
                    {
                        writingMethods.get(((HashSetModuleData)ann).value()).accept(
                                null, section, Integer.toString(i), entry
                        );
                        i++;
                    }
                }
        );


        //endregion


        //region UUID HashMap

        //----------= UUID HashMap =-----------------------------------------------------------------------------


        readingMethods.put(
                UUIDHashMapModuleData.class, (ann, yaml, key)
                        -> {
                    HashMap<UUID, Object> map = new HashMap<>();
                    ConfigurationSection section = yaml.getConfigurationSection(key);
                    if(section == null)
                        return map;

                    section.getValues(false).forEach((id, value) -> {
                        map.put(
                                UUID.fromString(id),
                                readingMethods.get(
                                        ((UUIDHashMapModuleData)ann).value()
                                ).apply(null, section, id)
                        );
                    });
                    return map;
                }
        );

        writingMethods.put(
                UUIDHashMapModuleData.class, (ann, yaml, key, value)
                        -> {
                    ConfigurationSection mapSection = yaml.createSection(key);
                    ((HashMap<UUID, Object>) value).forEach((id, obj) -> {
                        writingMethods.get(((UUIDHashMapModuleData)ann).value()).accept(null, mapSection, id.toString(), obj);
                    });
                }
        );


        //endregion


        //region String HashMap

        //----------= String HashMap =-----------------------------------------------------------------------------


        readingMethods.put(
                StringHashMapModuleData.class, (ann, yaml, key)
                        -> {
                    HashMap<String, Object> map = new HashMap<>();
                    ConfigurationSection section = yaml.getConfigurationSection(key);
                    if(section == null)
                        return map;

                    section.getValues(false).forEach((id, value) -> {
                        map.put(
                                id,
                                readingMethods.get(
                                        ((UUIDHashMapModuleData)ann).value()
                                ).apply(null, section, id)
                        );
                    });
                    return map;
                }
        );


        writingMethods.put(
                StringHashMapModuleData.class, (ann, yaml, key, value)
                        -> {
                    ConfigurationSection mapSection = yaml.createSection(key);
                    ((HashMap<String, Object>) value).forEach((id, obj) -> {
                        writingMethods.get(((StringHashMapModuleData)ann).value()).accept(null, mapSection, id, obj);
                    });
                }
        );


        //endregion
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
    public Map<String, Object> registeredInfo()
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
    public Map<String, Object> playingInfo()
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
    public Map<String, Object> basicInfo()
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
