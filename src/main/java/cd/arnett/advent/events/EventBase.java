package cd.arnett.advent.events;

import cd.arnett.advent.Advent;
import cd.arnett.advent.events.annotations.*;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import com.google.common.reflect.TypeToken;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;


/**
 * Provides data about events of this type, by registering this type you can then create
 * events of this type through commands.
 */
public abstract class EventBase {

    HashMap<UUID, PlayerEventStatus> registeredPlayers = new HashMap<>();
    HashMap<Class<? extends Annotation>, ArrayList<Method>> callbacks = new HashMap<>();
    HashMap<String, ArrayList<UUID>> trackedPlayerLists = new HashMap<>();
    boolean isActive = false;

    public EventBase() {
        //fill callbacks
        loadCallbacks();
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
            //get annotations of overrided methods filtered by there types
            Set<Annotation> annotations = AnnotatedElementUtils.findAllMergedAnnotations(method, Set.of(
                    EventControl.class,
                    ReadYaml.class,
                    WriteYaml.class,
                    OnEventStart.class,
                    OnEventStop.class
            ));

            //convert to class set
            HashSet<Class<? extends Annotation>> annotationClasses = new HashSet<>();
            annotations.forEach(annotation -> {
                annotationClasses.add(annotation.annotationType());
            });

            if(annotationClasses.contains(EventControl.class))
            {
                //check that it is the correct return type
                if(Cattamand.class.isAssignableFrom(method.getReturnType()))
                {
                    callbacks.computeIfAbsent(EventControl.class, (_) -> new ArrayList<>()).add(method);
                }
            }
            else if(annotationClasses.contains(ReadYaml.class))
            {
                //check that it is the correct parameter type
                if(YamlConfiguration.class.isAssignableFrom(method.getParameterTypes()[0]))
                {
                    callbacks.computeIfAbsent(ReadYaml.class, (_) -> new ArrayList<>()).add(method);
                }
            }
            else if(annotationClasses.contains(WriteYaml.class))
            {
                //check that it is the correct return type
                if(YamlConfiguration.class.isAssignableFrom(method.getReturnType()))
                {
                    callbacks.computeIfAbsent(WriteYaml.class, (_) -> new ArrayList<>()).add(method);
                }
            }
            else if(annotationClasses.contains(OnEventStart.class))
            {
                callbacks.computeIfAbsent(OnEventStart.class, (_) -> new ArrayList<>()).add(method);
            }
            else if(annotationClasses.contains(OnEventStop.class))
            {
                callbacks.computeIfAbsent(OnEventStop.class, (_) -> new ArrayList<>()).add(method);
            }
        }

        if(getClass().isAnnotationPresent(TrackedPlayerList.class))
        {
            TrackedPlayerList[] trackedListAnnotations = getClass().getAnnotationsByType(TrackedPlayerList.class);
            for(TrackedPlayerList ann : trackedListAnnotations)
            {
                trackedPlayerLists.put(ann.value(), new ArrayList<>());
            }
        }
    }

    /**
     * Invokes callbacks of type
     * @param type type of callbacks to invoke
     * @param foreachResult What to do with the result
     * @param params What parameters to pass
     */
    void invokeCallbacks(Class<? extends Annotation> type, Consumer<Object> foreachResult, Object... params)
    {
        callbacks.get(type).forEach(method -> {
            try {
                if(foreachResult != null)
                    foreachResult.accept(method.invoke(this, params));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }


    //endregion


    //region Abstract properties

    /*=================================================================================================
                        -  Abstract properties  -
    =================================================================================================*/


    /**
     * @return Identification of this event's type, used for specifying registered event types creating events
     */
    public abstract NamespacedKey eventKey();


    //endregion



    //region Controls

    /*=================================================================================================
                        -  Controls  -
    =================================================================================================*/

    /**
     * Call to start event
     */
    public void startEvent()
    {
        isActive = true;
        invokeCallbacks(OnEventStart.class, null);
    }

    /**
     * Call to stop event
     */
    public void stopEvent()
    {
        isActive = false;
        invokeCallbacks(OnEventStop.class, null);
    }

    /**
     * Makes a player join the event (Absent -> Playing)
     * @param player player to add
     */
    public void joinEvent(Player player)
    {
        registeredPlayers.put(player.getUniqueId(), PlayerEventStatus.PLAYING);
    }

    /**
     * removes a player from this event
     * @param id ID of player to remove
     */
    public void removeFromEvent(UUID id)
    {
        registeredPlayers.remove(id);
    }

    /**\
     * @return all Cattamand controls provided by the interfaces
     */
    public List<Cattamand> getCommandControls() {

        return callbacks.get(EventControl.class).stream().map(method -> {
            try {
                return (Cattamand) method.invoke(this);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }).toList();

    }

    //region Overloads



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
     * @param player player which was added
     */
    public abstract void onJoinEvent(Player player);

    /**
     * Called when a player was removed from an event
     * @param id ID of removed player
     */
    public abstract void onRemoveFromEvent(UUID id);


    //endregion



    //region YAML

    /*=================================================================================================
                        -  YAML  -
    =================================================================================================*/

    /**
     * Writes data to yaml data for saving, Override thi
     */
    @OverridingMethodsMustInvokeSuper
    public YamlConfiguration writeToYaml()
    {
        YamlConfiguration yaml = new YamlConfiguration();

        var playerMap = getPlayerStatusMap();

        var stringPlayerMap = playerMap.entrySet().stream().collect(Collectors.toMap(
                (entry) -> entry.getKey().toString(),
                (entry) -> entry.getValue().disconnected().name())
        );

        yaml.set("players", stringPlayerMap);

        //write modules
        invokeCallbacks(WriteYaml.class, (result) -> {
            YamlConfiguration otherYaml = ((YamlConfiguration)result);

            otherYaml.getKeys(false).forEach(key -> {
                if(!yaml.contains(key))
                    yaml.set(key, otherYaml.get(key));
            });
        }, yaml);

        //write tracked player lists
        trackedPlayerLists.forEach((key, list) -> {
            yaml.set(key, list.stream().map(UUID::toString).toList());
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
        yaml.getConfigurationSection("players").getValues(false).forEach((id, stat) -> {
            registeredPlayers.put(UUID.fromString(id), PlayerEventStatus.valueOf((String) stat));
        });

        //write tracked player lists
        trackedPlayerLists.forEach((key, list) ->
                yaml.getStringList(key).forEach(string -> list.add(UUID.fromString(string)))
        );

        invokeCallbacks(WriteYaml.class, null, yaml);
    }


    //endregion



    //region Getting Data

    /*=================================================================================================
                        -  Getting Data  -
    =================================================================================================*/

    /**
     * Gets a set of all registered players
     */
    public HashSet<UUID> getPlayers()
    {
        return new HashSet<>(registeredPlayers.keySet());
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

    public ArrayList<UUID> getTrackedPlayerList(String name)
    {
        return trackedPlayerLists.get(name);
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
     * Registers a player for this event, removes them from the revoked list if present
     * @param id ID of player to register
     */
    public void register(UUID id)
    {
        registeredPlayers.put(id, PlayerEventStatus.ABSENT);
    }


    /**
     * Revokes Registration from an event (and removes them if they are playing)
     * @param id ID of player to register
     */
    public void revokeRegistration(UUID id)
    {
        if(registeredPlayers.get(id) == PlayerEventStatus.PLAYING)
            removeFromEvent(id);

        registeredPlayers.remove(id);
    }

    //region Overloads

    //----------= Overloads =-----------------------------------------------------------------------------


    /**
     * Registers a player for this event (overload)
     * @param player Player to register
     */
    public void register(Player player)
    {
        register(player.getUniqueId());
    }


    /**
     * Revokes Registration from an event (and removes them if they are playing) (overload)
     * @param player Player to register
     */
    public void revokeRegistration(Player player)
    {
        revokeRegistration(player.getUniqueId());
    }

    //endregion

    //endregion

}
