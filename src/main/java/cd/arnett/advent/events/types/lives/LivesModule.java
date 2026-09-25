package cd.arnett.advent.events.types.lives;

import cd.arnett.advent.events.annotations.ModuleDataProvider;
import cd.arnett.advent.events.annotations.data.IntegerModuleData;
import cd.arnett.advent.events.annotations.data.UUIDHashMapModuleData;
import cd.arnett.advent.events.types.elimination.EliminationModule;

import java.util.HashMap;
import java.util.UUID;

public interface LivesModule extends EliminationModule {

    String livesMapName = "lives";
    String livesFileName = "Lives";

    //region Automatic Data

    /*=================================================================================================
                        -  Automatic Data  -
    =================================================================================================*/


    /**
     * Provides a list of Remaining players to moduleData
     */
    @ModuleDataProvider(key = livesMapName, fileBasename = livesFileName)
    @UUIDHashMapModuleData(type = IntegerModuleData.class)
    default HashMap<UUID, Integer> playerLives()
    {
        return new HashMap<>();
    }


    //endregion



}
