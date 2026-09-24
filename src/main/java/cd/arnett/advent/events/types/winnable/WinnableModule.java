package cd.arnett.advent.events.types.winnable;

import cd.arnett.advent.Advent;
import cd.arnett.advent.events.annotations.EventControl;
import cd.arnett.advent.events.annotations.EventInfoProvider;
import cd.arnett.advent.events.annotations.ModuleDataProvider;
import cd.arnett.advent.events.annotations.data.HashSetModuleData;
import cd.arnett.advent.events.annotations.data.UUIDHashMapModuleData;
import cd.arnett.advent.events.annotations.data.UUIDModuleData;
import cd.arnett.advent.events.types.EventType;
import cd.arnett.advent.events.types.winnable.controls.GrantWinCommand;
import cd.arnett.advent.events.types.winnable.controls.RevokeWinCommand;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import cd.arnett.caddamands.cattamands.cattamand.LiteralCattamand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public interface WinnableModule extends EventType {

    String winnersListName = "winners";


    //region Module Data

    /*=================================================================================================
                        -  Module Data  -
    =================================================================================================*/


    /**
     * Provides a list of Remaining players to moduleData
     */
    @ModuleDataProvider(winnersListName)
    @HashSetModuleData(UUIDModuleData.class)
    default HashSet<UUID> winningPlayers()
    {
        return new HashSet<>();
    }


    /**
     * @return the actual list used to store players who won, not a copy
     */
    default HashSet<UUID> getWinnersListData()
    {
        return this.getModuleData(winnersListName);
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
        return getWinnersListData().contains(playerId);
    }

    /**
     * @return List of player IDs of players who have won
     */
    default List<UUID> getPlayersWon()
    {
        return List.copyOf(getWinnersListData());
    }

    //region Overloads

    //----------= Overloads =-----------------------------------------------------------------------------


    /**
     * @param player player to check
     * @return true if the player has already won
     */
    default boolean hasPlayerWon(Player player)
    {
        return getWinnersListData().contains(player.getUniqueId());
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
        var winners = getWinnersListData();
        var event = getEvent();
        for(UUID id : winningPlayers)
        {
            if(event.requiresRegistration() && !event.isRegistered(id))
            {
                Advent.logger.warning("Attempted Granting win to player who is not registered");
                continue;
            }

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
        var winners = getWinnersListData();
        var event = getEvent();
        for(UUID id : revokedPlayers)
        {
            if(event.requiresRegistration() && !event.isRegistered(id))
            {
                continue;
            }

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
        for(Player player : winningPlayers)
        {
            grantWin(player.getUniqueId());
        }

    }

    /**
     * Sets provided players as not having won (overload)
     * @param revokedPlayers Players who are to of won any longer
     */
    default void revokeWin(Player... revokedPlayers)
    {
        for(Player player : revokedPlayers)
        {
            revokeWin(player.getUniqueId());
        }
    }


    //endregion


    //endregion


    //region Events

    /*=================================================================================================
                        -  Events  -
    =================================================================================================*/


    void onWin(UUID winningPlayer);

    void onWinRevoked(UUID revokedPlayer);


    //endregion


    //region Commands

    /*=================================================================================================
                        -  Commands  -
    =================================================================================================*/


    @EventControl
    default Cattamand winnableControls()
    {
        return new LiteralCattamand(
                "wins",
                List.of(
                        new GrantWinCommand(),
                        new RevokeWinCommand()
                )
        );
    }


    //endregion


    //region Info

    /*=================================================================================================
                        -  Info  -
    =================================================================================================*/


    /**
     * @return Provides information on the winner module
     */
    @EventInfoProvider(name = "WinnableModule", separate = false)
    default Map<String, Object> winnableModuleInfo()
    {
        HashMap<String, Object> sectionMap = new HashMap<>();

        int winnerCount = getPlayersWon().size();

        sectionMap.put("Winner Count", winnerCount);

        return sectionMap;
    }

    /**
     * @return Provides information section on what players have won
     */
    @EventInfoProvider(name = "Winners", separate = true)
    default Map<String, Object> winnersInfo()
    {
        HashMap<String, Object> sectionMap = new HashMap<>();
        StringBuilder output = new StringBuilder();
        var players = getPlayersWon();

        AtomicInteger counter = new AtomicInteger(0);
        players.stream().map(Bukkit::getOfflinePlayer).map(OfflinePlayer::getName).forEach(name -> {
            boolean even = counter.getAndIncrement() % 2 == 0;

            output.append(even ? "<color:#c4c4c4>" : "</color:#c4c4c4>").append(name).append(' ');

        });

        sectionMap.put("Count", players.size());
        sectionMap.put("Players", output.toString());

        return sectionMap;
    }


    //endregion
}
