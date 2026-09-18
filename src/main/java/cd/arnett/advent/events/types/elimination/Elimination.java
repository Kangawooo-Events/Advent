package cd.arnett.advent.events.types.elimination;

import cd.arnett.advent.events.types.annotations.EventControl;
import cd.arnett.advent.events.types.elimination.controls.EliminateCommand;
import cd.arnett.advent.events.types.elimination.controls.InstateCommand;
import cd.arnett.advent.events.types.winnable.Winnable;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import cd.arnett.caddamands.cattamands.cattamand.LiteralCattamand;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import org.bukkit.entity.Player;

import java.util.*;

public interface Elimination extends Winnable {

    HashSet<UUID> remainingPlayers = new HashSet<>();

    //region Events

    /*=================================================================================================
                        -  Events  -
    =================================================================================================*/

    /**
     * Called when a player is eliminated, super is what actually does the removal from the remaining player list
     * @param eliminatedPlayer
     */
    @OverridingMethodsMustInvokeSuper
    default void onEliminate(UUID eliminatedPlayer)
    {
        remainingPlayers.remove(eliminatedPlayer);
    }

    /**
     * Called when a player is added to the remaining players,
     * super is what actually does the adding to the remaining player list, and restoration of player data if
     * @param eliminatedPlayer
     */
    @OverridingMethodsMustInvokeSuper
    default void onInstate(UUID eliminatedPlayer)
    {
        remainingPlayers.remove(eliminatedPlayer);
    }


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
        return remainingPlayers.contains(playerId);
    }

    //region Overloads

    //----------= Overloads =-----------------------------------------------------------------------------


    /**
     * @param player player to check
     * @return Is the player still in content for winning
     */
    default boolean isPlayerRemaining(Player player)
    {
        return remainingPlayers.contains(player.getUniqueId());
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
