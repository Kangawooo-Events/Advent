package cd.arnett.advent.events.types.winnable;

import cd.arnett.advent.events.annotations.EventControl;
import cd.arnett.advent.events.annotations.ModuleDataProvider;
import cd.arnett.advent.events.annotations.data.HashSetModuleData;
import cd.arnett.advent.events.annotations.data.UUIDHashMapModuleData;
import cd.arnett.advent.events.types.EventType;
import cd.arnett.advent.events.types.winnable.controls.GrantWinCommand;
import cd.arnett.advent.events.types.winnable.controls.RevokeWinCommand;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import cd.arnett.caddamands.cattamands.cattamand.LiteralCattamand;
import org.bukkit.entity.Player;

import java.util.*;

public interface WinnableModule extends EventType {

    String winnersListName = "winners";


    //region Automatic Data

    /*=================================================================================================
                        -  Automatic Data  -
    =================================================================================================*/


    /**
     * Provides a list of Remaining players to moduleData
     */
    @ModuleDataProvider(winnersListName)
    @HashSetModuleData(UUIDHashMapModuleData.class)
    default HashSet<UUID> winningPlayers()
    {
        return new HashSet<>();
    }


    //endregion

    //region Queries

    /*=================================================================================================
                        -  Queries  -
    =================================================================================================*/

    /**
     * @param playerId ID of player to check
     * @return true if the player has already won
     */
    default boolean hasPlayerWon(UUID playerId)
    {
        return getModulePlayerList(winnersListName).contains(playerId);
    }

    /**
     * @return List of player IDs of players who have won
     */
    default List<UUID> getPlayersWon()
    {
        return List.copyOf(getModulePlayerList(winnersListName));
    }

    //region Overloads

    //----------= Overloads =-----------------------------------------------------------------------------


    /**
     * @param player player to check
     * @return true if the player has already won
     */
    default boolean hasPlayerWon(Player player)
    {
        return getModulePlayerList(winnersListName).contains(player.getUniqueId());
    }


    //endregion


    //endregion


    //region Controls

    /*=================================================================================================
                        -  Controls  -
    =================================================================================================*/


    /**
     * Sets provided players as having won
     * @param winningPlayers Player Ids who have won
     */
    default void grantWin(UUID... winningPlayers)
    {
        var winners = getModulePlayerList(winnersListName);
        for(UUID id : winningPlayers)
        {
            if(winners.contains(id))
                continue;
            winners.add(id);
            onWin(id);
        }
    }

    /**
     * Sets provided players as not having won
     * @param revokedPlayers Player Ids who are to of won any longer
     */
    default void revokeWin(UUID... revokedPlayers)
    {
        var winners = getModulePlayerList(winnersListName);
        for(UUID id : revokedPlayers)
        {
            if(!winners.contains(id))
                continue;
            winners.remove(id);
            onWinRevoked(id);
        }
    }

    //region Overloads

    //----------= Overloads =-----------------------------------------------------------------------------


    /**
     * Sets provided players as having won (overload)
     * @param winningPlayers Players who have won
     */
    default void grantWin(Player... winningPlayers)
    {
        grantWin(Arrays.stream(winningPlayers).map(Player::getUniqueId).toArray(UUID[]::new));
    }

    /**
     * Sets provided players as not having won (overload)
     * @param revokedPlayers Players who are to of won any longer
     */
    default void revokeWin(Player... revokedPlayers)
    {
        revokeWin(Arrays.stream(revokedPlayers).map(Player::getUniqueId).toArray(UUID[]::new));
    }


    //endregion


    //endregion


    //region Events

    /*=================================================================================================
                        -  Events  -
    =================================================================================================*/


    void onWin(UUID... winningPlayers);

    void onWinRevoked(UUID... revokedPlayers);


    //endregion


    //region Commands

    /*=================================================================================================
                        -  Commands  -
    =================================================================================================*/


    @EventControl
    default Cattamand winnableControls()
    {
        return new LiteralCattamand(
                "win",
                List.of(
                        new GrantWinCommand(),
                        new RevokeWinCommand()
                )
        );
    }


    //endregion

}
