package cd.arnett.advent.events;

import cd.arnett.advent.events.types.annotations.EventControl;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Provides data about events of this type, by registering this type you can then create
 * events of this type through commands.
 */
public abstract class EventBase {

    HashMap<UUID, PlayerEventStatus> registeredPlayers = new HashMap<>();

    /**
     * this map is purely here for moderation purposes
     */
    HashMap<UUID, String> revokedPlayers = new HashMap<>();


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


    /**\
     * @return all Cattamand controls provided by the interfaces
     */
    public List<Cattamand> getCommandControls() {
        ArrayList<Cattamand> controls = new ArrayList<>();

        //go through each method to check for the @EventControl annotation
        for(Method method : getClass().getMethods())
        {
            //check if this method has that annotation and returns a Cattamand List or cattamand
            if(method.isAnnotationPresent(EventControl.class))
            {
                if(List.class.isAssignableFrom(method.getReturnType()))
                {
                    //check if it is a list of cattamands
                    if(method.getGenericReturnType() instanceof ParameterizedType parameterizedType)
                    {
                        var types = parameterizedType.getActualTypeArguments();
                        if(types.length > 0)
                        {
                            if(types[0] instanceof Class<?> clazz
                                    && Cattamand.class.isAssignableFrom(clazz))
                            {
                                //it is a list of cattamands so add everything from the return
                                try {
                                    controls.addAll((List<Cattamand>)method.invoke(this));
                                }
                                catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                else if(Cattamand.class.isAssignableFrom(method.getReturnType()))
                {
                    //it's just a cattamand so add the return
                    try {
                        controls.add((Cattamand)method.invoke(this));
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }

        return controls;
    }


    //endregion



    //region Abstract Controls

    /*=================================================================================================
                        -  Abstract Controls  -
    =================================================================================================*/



    /**
     * Adds a player to this event
     * @param player player to add
     */
    public abstract void addToEvent(Player player);

    /**
     * removes a player from this event
     * @param player player to remove
     */
    public abstract void removeFromEvent(Player player);



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
                (entry) -> entry.getValue().name())
        );

        var stringRevokedMap = revokedPlayers.entrySet().stream().collect(Collectors.toMap(
                (entry) -> entry.getKey().toString(),
                (entry) -> entry.getValue().toString())
        );

        yaml.set("players", stringPlayerMap);
        yaml.set("revoked", stringRevokedMap);

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

        yaml.getConfigurationSection("revoked").getValues(false).forEach((id, name) -> {
            revokedPlayers.put(UUID.fromString(id), (String) name);
        });
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

    /**
     * Says whether a player's registration was revoked
     * @param id ID of player to check
     * @return true if they were revoked
     */
    public boolean isRevoked(UUID id)
    {
        return revokedPlayers.containsKey(id);
    }

    //region Overloads

    //----------= Overloads =-----------------------------------------------------------------------------

    /**
     * Is a player registered to play in this event
     * @param player Player to check
     * @return Registration status
     */
    public boolean isRegistered(Player player)
    {
        return isRegistered(player.getUniqueId());
    }

    /**
     * Is a player registered to play in this event and is of a filtered status
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
     * @param player Player to check
     * @return Event status of the provided player
     */
    public PlayerEventStatus getStatus(Player player)
    {
        return getStatus(player.getUniqueId());
    }


    /**
     * Says whether a player's registration was revoked
     * @param player Player to check
     * @return true if they were revoked
     */
    public boolean isRevoked(Player player)
    {
        return isRevoked(player.getUniqueId());
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
        revokedPlayers.remove(id);
    }


    /**
     * Revokes Registration from an event (and removes them if they are playing)
     * @param id ID of player to register
     */
    public void revokeRegistration(UUID id)
    {
        if(registeredPlayers.get(id).isPlaying())
            removeFromEvent(Bukkit.getPlayer(id));

        registeredPlayers.remove(id);
    }

    //region Overloads

    //----------= Overloads =-----------------------------------------------------------------------------


    /**
     * Registers a player for this event
     * @param player Player to register
     */
    public void register(Player player)
    {
        register(player.getUniqueId());
    }


    /**
     * Revokes Registration from an event (and removes them if they are playing)
     * @param player Player to register
     */
    public void revokeRegistration(Player player)
    {
        revokeRegistration(player.getUniqueId());
    }

    //endregion

    //endregion

}
