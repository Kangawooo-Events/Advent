package cd.arnett.advent.events.types.lives;

import cd.arnett.advent.events.annotations.ModuleDataProvider;
import cd.arnett.advent.events.annotations.data.PlayerIdArrayList;
import cd.arnett.advent.events.annotations.data.PlayerIntegerHashMap;
import cd.arnett.advent.events.types.elimination.EliminationModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public interface LivesModule extends EliminationModule {

    String livesMapName = "lives";

    //region Automatic Data

    /*=================================================================================================
                        -  Automatic Data  -
    =================================================================================================*/


    /**
     * Provides a list of Remaining players to moduleData
     */
    @ModuleDataProvider(livesMapName)
    @PlayerIntegerHashMap()
    default HashMap<UUID, Integer> playerLives()
    {
        return new HashMap<>();
    }


    //endregion



}
