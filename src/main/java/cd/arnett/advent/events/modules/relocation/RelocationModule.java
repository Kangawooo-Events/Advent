package cd.arnett.advent.events.modules.relocation;

import cd.arnett.advent.destinations.DestinationTeleportTask;
import cd.arnett.advent.events.annotations.*;
import cd.arnett.advent.events.annotations.data.DestinationTeleportTaskModuleData;
import cd.arnett.advent.events.annotations.data.LocationModuleData;
import cd.arnett.advent.events.annotations.data.UUIDHashMapModuleData;
import cd.arnett.advent.events.modules.EventModule;
import cd.arnett.advent.events.modules.relocation.controls.ReloadRelocationCommand;
import cd.arnett.advent.events.modules.relocation.controls.TestRelocationCommand;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import cd.arnett.caddamands.cattamands.cattamand.LiteralCattamand;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface RelocationModule extends EventModule {

    HashMap<UUID, NamespacedKey> preTeleportedPlayers = new HashMap<>();


    //region Module Data

    /*=================================================================================================
                        -  Module Data  -
    =================================================================================================*/

    String fileName = "Relocation";
    String joinTeleportTaskKey = "JoinTask";
    String eventLocationsKey = "eventLocations";
    String eventRespawnsKey = "eventRespawns";

    @ModuleDataProvider(key = joinTeleportTaskKey, fileBasename = fileName)
    @DestinationTeleportTaskModuleData
    default DestinationTeleportTask joinDestinationTaskData()
    {
        return new DestinationTeleportTask(
                getEvent().eventKey(), "JOIN"
        );
    }


    @ModuleDataProvider(key = eventLocationsKey, fileBasename = fileName)
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
     * Gets the previous location of a player before they joined this event, NULL if there is none
     * @param player player to check
     * @return location of player when they left the event, NULL if they don't have one
     */
    default Location getEventLocation(Player player)
    {
        if(saveEventLocation() && hasEventLocation(player))
            return getEventLocation(player.getUniqueId());
        else
            return getJoinTeleportTask().popTeleportDestination();
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

    @OnJoinEvent(priority = JoinTaskPriority.EARLY)
    default CompletableFuture<Boolean> teleportOnJoin(Player joined)
    {
        var location = getEventLocation(joined);
        var teleportFuture = new CompletableFuture<Boolean>();

        //eject anything riding the player
        if(!joined.getPassengers().isEmpty())
            joined.eject();


        //if they prejoined this event then don't teleport them
        if(preTeleportedPlayers.getOrDefault(joined.getUniqueId(), null) == getEvent().eventKey())
        {
            teleportFuture.complete(true);
            return teleportFuture;
        }

        //teleport to event location
        var future = joined.teleportAsync(location);
        future.thenAccept(result -> {
            if (!result)
            {
                teleportFuture.complete(joined.teleport(location));
            }
            else
            {
                teleportFuture.complete(true);
            }
        });


        preTeleportedPlayers.remove(joined.getUniqueId());
        return teleportFuture;
    }

    @OnLeaveEvent
    default void saveLocationOnLeave(Player left)
    {
        //save event location
        getEventLocations().put(left.getUniqueId(), left.getLocation());
    }


    //endregion


    //region Info

    /*=================================================================================================
                        -  Info  -
    =================================================================================================*/


    @EventInfoProvider(name = "RelocationJoin", separate = true)
    default HashMap<String, Object> relocationJoinInfoProvider()
    {
        var map = new HashMap<String, Object>();

        map.put("Join Destination Namespace", getJoinTeleportTask().getEventId());
        map.put("Join Destination Tag", getJoinTeleportTask().getTag());
        map.put("Join Destinations found", getJoinTeleportTask().getDestinationCount());
        map.put("Event Locations Stored", getEventLocations().size());

        return map;
    }


    //endregion


    //region Controls

    /*=================================================================================================
                        -  Controls  -
    =================================================================================================*/


    @EventControl
    default Cattamand relocationControls()
    {
        return new LiteralCattamand("relocation",
                List.of(
                        new ReloadRelocationCommand(getEvent()),
                        new TestRelocationCommand(getEvent())
                )
        );
    }


    //endregion


    //region Behaviours

    /*=================================================================================================
                        -  Behaviours  -
    =================================================================================================*/



    /**
     * Used to set spawn location on joining without teleporting on a PlayerSpawnLocationEvent
     * @param joined Player who joined
     * @param e PlayerSpawnLocationEvent event
     * @return was it a success
     */
    default boolean preRelocateForSpawnEvent(Player joined, PlayerSpawnLocationEvent e)
    {
        var location = getEventLocation(joined.getUniqueId());

        //set spawn location for pre-joining
        if(location == null)
        {
            return false;
        }
        else
        {
            e.setSpawnLocation(location);
            preTeleportedPlayers.put(joined.getUniqueId(), getEvent().eventKey());
            return true;
        }
    }


    //endregion


}
