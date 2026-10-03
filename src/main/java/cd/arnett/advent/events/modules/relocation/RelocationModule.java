package cd.arnett.advent.events.modules.relocation;

import cd.arnett.advent.Advent;
import cd.arnett.advent.destinations.DestinationTeleportTask;
import cd.arnett.advent.events.annotations.*;
import cd.arnett.advent.events.annotations.data.DestinationTeleportTaskModuleData;
import cd.arnett.advent.events.annotations.data.LocationModuleData;
import cd.arnett.advent.events.annotations.data.UUIDHashMapModuleData;
import cd.arnett.advent.events.modules.EventType;
import cd.arnett.advent.events.modules.relocation.controls.ReloadRelocationCommand;
import cd.arnett.advent.events.modules.relocation.controls.TestRelocationCommand;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import cd.arnett.caddamands.cattamands.cattamand.LiteralCattamand;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface RelocationModule extends EventType {


    //region Module Data

    /*=================================================================================================
                        -  Module Data  -
    =================================================================================================*/

    String joinTeleportTaskKey = "JoinTask";
    String eventLocationsKey = "eventLocations";

    @ModuleDataProvider(key = joinTeleportTaskKey, fileBasename = "Relocation")
    @DestinationTeleportTaskModuleData
    default DestinationTeleportTask joinDestinationTaskData()
    {
        return new DestinationTeleportTask(
                getEvent().eventKey(), "JOIN"
        );
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

    @OnJoinEvent(priority = JoinTaskPriority.EARLY)
    default CompletableFuture<Boolean> teleportOnJoin(Player joined)
    {
        //remove mounts
        if(!joined.getPassengers().isEmpty())
            joined.eject();

        //teleport to event location
        if(saveEventLocation() && hasEventLocation(joined))
        {
            var fallbackFuture = new CompletableFuture<Boolean>();
            var location = getEventLocation(joined);
            var future = joined.teleportAsync(location);
            future.thenAccept(result -> {
                if (!result)
                {
                    fallbackFuture.complete(joined.teleport(location));
                    try {
                        System.out.println("Backup Teleport " + fallbackFuture.get());
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }
                else
                {
                    fallbackFuture.complete(true);
                }
            });
            return fallbackFuture;
        }

        //no event location so to a join destination

        return teleportToJoinDestination(joined);
    }

    @OnLeaveEvent
    default void saveLocationOnLeave(Player left)
    {
        //save event location
        getEventLocations().put(left.getUniqueId(), left.getLocation());
    }

    default CompletableFuture<Boolean> teleportToJoinDestination(Player player)
    {
        var fallbackFuture = new CompletableFuture<Boolean>();
        Location destination = getJoinTeleportTask().popTeleportDestination();
        if(destination == null)
        {
            fallbackFuture.complete(false);
            return fallbackFuture;
        }

        player.teleportAsync(destination).thenAccept(result -> {
            if (!result)
            {
                fallbackFuture.complete(player.teleport(destination));
                try {
                    System.out.println("Backup Teleport " + fallbackFuture.get());
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
            else
            {
                fallbackFuture.complete(true);
            }
        });

        return fallbackFuture;
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
