package cd.arnett.advent.events.types.relocation;

import cd.arnett.advent.destinations.DestinationTeleportTask;
import cd.arnett.advent.events.annotations.*;
import cd.arnett.advent.events.annotations.data.DestinationTeleportTaskModuleData;
import cd.arnett.advent.events.annotations.data.LocationModuleData;
import cd.arnett.advent.events.annotations.data.UUIDHashMapModuleData;
import cd.arnett.advent.events.types.EventType;
import cd.arnett.advent.events.types.relocation.controls.ReloadRelocationCommand;
import cd.arnett.advent.events.types.relocation.controls.TestRelocationCommand;
import cd.arnett.advent.worlds.HubWorld;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import cd.arnett.caddamands.cattamands.cattamand.LiteralCattamand;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public interface RelocationModule extends EventType {


    //region Module Data

    /*=================================================================================================
                        -  Module Data  -
    =================================================================================================*/

    String joinTeleportTaskKey = "JoinTask";
    String previousLocationsKey = "previousLocations";
    String eventLocationsKey = "eventLocations";

    @ModuleDataProvider(key = joinTeleportTaskKey, fileBasename = "Relocation")
    @DestinationTeleportTaskModuleData
    default DestinationTeleportTask joinDestinationTaskData()
    {
        return new DestinationTeleportTask(getEvent().eventKey(), "JOIN");
    }

    @ModuleDataProvider(key = previousLocationsKey, fileBasename = "Relocation")
    @UUIDHashMapModuleData(type = LocationModuleData.class)
    default HashMap<UUID, Location> previousLocationsData()
    {
        return new HashMap<>();
    }

    @ModuleDataProvider(key = eventLocationsKey, fileBasename = "Relocation")
    @UUIDHashMapModuleData(type = LocationModuleData.class)
    default HashMap<UUID, Location> eventLocationsData()
    {
        return new HashMap<>();
    }

    /**
     * @return The world the event is taking place in, Null if not set
     */
    default DestinationTeleportTask getJoinTeleportTask()
    {
        return getModuleData(joinTeleportTaskKey);
    }


    /**
     * @return The map of player ID and their previous locations before joining this event
     */
    default HashMap<UUID, Location> getPreviousLocations()
    {
        return getModuleData(previousLocationsKey);
    }


    /**
     * @return The map of player ID and their event locations, where they were when they last left the event
     */
    default HashMap<UUID, Location> getEventLocations()
    {
        return getModuleData(eventLocationsKey);
    }

    //endregion



    //region Getting

    /*=================================================================================================
                        -  Getting  -
    =================================================================================================*/

    /**
     * Gets the previous location of a player before they joined this event
     * @param id ID of player
     * @return previous location of player
     */
    //todo add hub world as fallback
    default Location getPreviousLocation(UUID id)
    {
        return getPreviousLocations().get(id);
    }

    /**
     * Gets the previous location of a player before they joined this event, NULL if there is none
     * @param id ID of player
     * @return location of player when they left the event, NULL if they don't have one
     */
    default Location getEventLocation(UUID id)
    {
        return getEventLocations().get(id);
    }

    /**
     * @param id ID of player
     * @return Whether they have an event location stored
     */
    default boolean hasEventLocation(UUID id) {
        return eventLocationsData().containsKey(id);
    }

    //region Overloads

    /*=================================================================================================
                        -  Overloads  -
    =================================================================================================*/

    /**
     * Gets the previous location of a player before they joined this event
     * @param player player to check
     * @return previous location of player
     */
    default Location getPreviousLocation(Player player)
    {
        return getPreviousLocation(player.getUniqueId());
    }

    /**
     * Gets the previous location of a player before they joined this event, NULL if there is none
     * @param player player to check
     * @return location of player when they left the event, NULL if they don't have one
     */
    default Location getEventLocation(Player player)
    {
        return getEventLocation(player.getUniqueId());
    }

    /**
     * @param player player to check
     * @return Whether they have an event location stored
     */
    default boolean hasEventLocation(Player player)
    {
        return hasEventLocation(player.getUniqueId());
    }

    //endregion


    //endregion



    //region Abstract Properties

    /*=================================================================================================
                        -  Abstract Properties  -
    =================================================================================================*/


    /**
     * @return Whether this module teleports players to their last stored event location when joining
     * the event or just teleports them to a join destination
     */
    boolean saveEventLocation();


    //endregion



    //region Events

    /*=================================================================================================
                        -  Events  -
    =================================================================================================*/

    //todo add event list checks

    @OnJoinEvent
    default void teleportOnJoin(Player joined)
    {
        //save previous location
        getPreviousLocations().put(joined.getUniqueId(), joined.getLocation());

        //teleport to event location
        if(saveEventLocation() && hasEventLocation(joined))
        {
            joined.teleportAsync(getEventLocation(joined));
        }

        //no event location so to a join destination
        teleportPlayerToJoinDestination(joined);
    }

    @OnLeaveEvent
    default void saveLocationOnLeave(Player left)
    {
        //save event location
        getEventLocations().put(left.getUniqueId(), left.getLocation());

        Location previous = getPreviousLocation(left);
        //teleport to previous location
        if(previous != null)
        {
            left.teleportAsync(previous);
        }
        else {
            HubWorld.sendToHub(left);
        }
    }


    //endregion



    //region Teleporting

    /*=================================================================================================
                        -  Teleporting  -
    =================================================================================================*/

    /**
     * Teleports a player to one of the join destinations
     * @param player player to teleport
     */
    default void teleportPlayerToJoinDestination(Player player)
    {
        getJoinTeleportTask().teleportAsync(player);
    }


    //endregion


    //region Info

    /*=================================================================================================
                        -  Info  -
    =================================================================================================*/


    @EventInfoProvider(name = "RelocationModule")
    default HashMap<String, Object> relocationModuleInfoProvider()
    {
        var map = new HashMap<String, Object>();

        map.put("Event Locations Stored", getEventLocations().size());
        map.put("Previous Locations Stored", getPreviousLocations().size());

        return map;
    }


    @EventInfoProvider(name = "RelocationJoin", separate = true)
    default HashMap<String, Object> relocationJoinInfoProvider()
    {
        var map = new HashMap<String, Object>();

        map.put("Join Destination Namespace", getJoinTeleportTask().getEventId());
        map.put("Join Destination Tag", getJoinTeleportTask().getTag());
        map.put("Join Destinations found", getJoinTeleportTask().getDestinationCount());

        return map;
    }


    //endregion


    //region Commands

    /*=================================================================================================
                        -  Commands  -
    =================================================================================================*/


    @EventControl
    default Cattamand relocationControls()
    {
        return new LiteralCattamand("relocation",
                List.of(
                        new ReloadRelocationCommand(),
                        new TestRelocationCommand()
                )
        );
    }


    //endregion



}
