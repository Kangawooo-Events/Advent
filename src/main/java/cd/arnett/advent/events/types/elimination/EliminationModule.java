package cd.arnett.advent.events.types.elimination;

import cd.arnett.advent.events.annotations.EventControl;
import cd.arnett.advent.events.annotations.ModuleDataProvider;
import cd.arnett.advent.events.annotations.data.HashSetModuleData;
import cd.arnett.advent.events.annotations.data.UUIDHashMapModuleData;
import cd.arnett.advent.events.types.elimination.controls.EliminateCommand;
import cd.arnett.advent.events.types.elimination.controls.InstateCommand;
import cd.arnett.advent.events.types.winnable.WinnableModule;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import cd.arnett.caddamands.cattamands.cattamand.LiteralCattamand;
import org.bukkit.entity.Player;

import java.util.*;

public interface EliminationModule extends WinnableModule {

    String remainingListName = "remaining";
    String eliminatedListName = "eliminated";

    //region Module Data

    /*=================================================================================================
                        -  Module Data  -
    =================================================================================================*/

    /**
     * Provides a list of Remaining players to moduleData
     */
    @ModuleDataProvider(remainingListName)
    @HashSetModuleData(UUIDHashMapModuleData.class)
    default HashSet<UUID> remainingPlayers()
    {
        return new HashSet<>();
    }

    /**
     * Provides a list of Eliminated players to moduleData
     */
    @ModuleDataProvider(eliminatedListName)
    @HashSetModuleData(UUIDHashMapModuleData.class)
    default HashSet<UUID> eliminatedPlayers()
    {
        return new HashSet<>();
    }


    //endregion

    //region Events

    /*=================================================================================================
                        -  Events  -
    =================================================================================================*/

    /**
     * Called when a player is eliminated
     * @param eliminatedPlayer
     */
    void onEliminate(UUID eliminatedPlayer);

    /**
     * Called when a player is added to the remaining players
     * @param eliminatedPlayer
     */
    void onInstate(UUID eliminatedPlayer);


    //endregion


    //region Queries

    /*=================================================================================================
                        -  Queries  -
    =================================================================================================*/

    /**
     * @param playerId ID of player to check
     * @return Is the player still in content for winning
     */
    default boolean isPlayerRemaining(UUID playerId)
    {
        return getModulePlayerList(remainingListName).contains(playerId);
    }

    /**
     * @return List of player IDs of remain players
     */
    default List<UUID> getPlayersRemaining()
    {

        return List.copyOf(getModulePlayerList(remainingListName));
    }

    /**
     * @return List of player IDs of eliminated player
     */
    default List<UUID> getPlayersEliminated()
    {
        return List.copyOf(getModulePlayerList(eliminatedListName));
    }

    //region Overloads

    //----------= Overloads =-----------------------------------------------------------------------------


    /**
     * @param player player to check
     * @return Is the player still in content for winning
     */
    default boolean isPlayerRemaining(Player player)
    {
        return getModulePlayerList(remainingListName).contains(player.getUniqueId());
    }


    //endregion


    //endregion


    //region Controls

    /*=================================================================================================
                        -  Controls  -
    =================================================================================================*/


    /**
     * calls onEliminate event for the provided player (removes them from remaining players)
     * @param playerIds IDs of the players to eliminate
     */
    default void eliminate(UUID... playerIds)
    {
        for (UUID playerId : playerIds)
        {
            getModulePlayerList(remainingListName).remove(playerId);
            getModulePlayerList(eliminatedListName).add(playerId);
            onEliminate(playerId);
        }
    }

    /**
     * calls onInstate event for the provided player (adds them to remaining players)
     * @param playerIds IDs of the player to instate
     */
    default void instate(UUID... playerIds)
    {
        for (UUID playerId : playerIds)
        {
            getModulePlayerList(remainingListName).add(playerId);
            getModulePlayerList(eliminatedListName).remove(playerId);
            onInstate(playerId);
        }
    }

    //region Overloads

    //----------= Overloads =-----------------------------------------------------------------------------


    /**
     * calls onEliminate event for the provided players (removes them from remaining players)
     * @param players Players to eliminate
     */
    default void eliminate(Player... players)
    {
        for (Player player : players)
        {
            eliminate(player.getUniqueId());
        }
    }

    /**
     * calls onInstate event for the provided players (adds them to remaining players)
     * @param players Players to instate
     */
    default void instate(Player... players)
    {
        for (Player player : players)
        {
            instate(player.getUniqueId());
        }
    }


    //endregion


    //endregion


    //region Commands

    /*=================================================================================================
                        -  Commands  -
    =================================================================================================*/


    @EventControl
    default Cattamand eliminationControls()
    {
        return new LiteralCattamand(
                "eliminations",
                List.of(
                        new EliminateCommand(),
                        new InstateCommand()
                )
        );
    }


    //endregion

}
