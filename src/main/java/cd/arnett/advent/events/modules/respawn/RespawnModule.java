package cd.arnett.advent.events.modules.respawn;

import cd.arnett.advent.Advent;
import cd.arnett.advent.destinations.DestinationTeleportTask;
import cd.arnett.advent.events.annotations.ModuleDataProvider;
import cd.arnett.advent.events.annotations.OnJoinEvent;
import cd.arnett.advent.events.annotations.OnLeaveEvent;
import cd.arnett.advent.events.annotations.data.DestinationTeleportTaskModuleData;
import cd.arnett.advent.events.annotations.data.SpawnLocationModuleData;
import cd.arnett.advent.events.annotations.data.UUIDHashMapModuleData;
import cd.arnett.advent.events.modules.EventModule;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public interface RespawnModule extends EventModule {

    HashMap<UUID, SpawnLocation> nonEventSpawns = new HashMap<>();

    //region Module Data

    /*=================================================================================================
                        -  Module Data  -
    =================================================================================================*/

    String respawnTeleportTaskKey = "RespawnTask";
    String eventRespawnsKey = "eventRespawns";
    String moduleFileName = "Respawn";

    @ModuleDataProvider(key = respawnTeleportTaskKey, fileBasename = moduleFileName)
    @DestinationTeleportTaskModuleData
    default DestinationTeleportTask respawnDestinationTaskData()
    {
        return new DestinationTeleportTask(getEvent().eventKey(), "RESPAWN");
    }


    @ModuleDataProvider(key = eventRespawnsKey, fileBasename = "Relocation")
    @UUIDHashMapModuleData(type = SpawnLocationModuleData.class)
    default HashMap<UUID, SpawnLocation> eventRespawnsData()
    {
        return new HashMap<>();
    }

    /**
     * @return The world the event is taking place in, Null if not set
     */
    default DestinationTeleportTask getRespawnTeleportTask()
    {
        return getModuleData(respawnTeleportTaskKey);
    }



    /**
     * @return The map of player ID and their event locations, where they were when they last left the event
     */
    default HashMap<UUID, SpawnLocation> getEventRespawns()
    {
        return getModuleData(eventRespawnsKey);
    }

    //endregion

    //region Helpers

    /*=================================================================================================
                    -  Helpers  -
    =================================================================================================*/

    default boolean isValidRespawn(SpawnLocation respawn)
    {
        if(respawn.location == null)
        {
            return false;
        }
        else
        {
            if(!respawn.requiresBlock)
            {
                return true;
            }

            //check for nearby valid respawn block
            for (int x = -1; x  <= 1; x++) {
                for (int z = -1; z <= 1; z++){
                    for (int y = -1; y <= 1; y++){

                        BlockData respawnBlockData = respawn.location.add(new Vector(x, y, z)).getBlock().getBlockData();

                        if(respawnBlockData instanceof Bed ||
                                (respawnBlockData instanceof RespawnAnchor anchor && anchor.getCharges() > 0))
                        {
                            return true;
                        }

                    }
                }
            }

            return false;
        }
    }

    default void setSpawnLocation(UUID id, Location location, boolean requireBlock)
    {
        getEventRespawns().put(id, new SpawnLocation(location, requireBlock));
    }

    static void setOutOfEventSpawnLocation(UUID id, Location location, boolean requireBlock)
    {
        nonEventSpawns.put(id, new SpawnLocation(location, requireBlock));
    }


    default Location getRespawnLocation(UUID id)
    {
        var respawns = getEventRespawns();

        //if we have a respawn location for the set it here
        if(respawns.containsKey(id))
        {
            var respawn = respawns.get(id);
            if(isValidRespawn(respawn))
            {
                return respawn.location;
            }
        }

        //if we have their spawn in out of event spawns then apply that and remove it
        else if(nonEventSpawns.containsKey(id))
        {
            var respawn = nonEventSpawns.get(id);
            if(isValidRespawn(respawn))
            {
                return respawn.location;
            }
        }

        //no respawn set to set it here
        return getRespawnTeleportTask().popTeleportDestination();
    }

    //endregion

}
