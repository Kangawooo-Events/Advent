package cd.arnett.advent.events.modules.elimination;

import cd.arnett.advent.Advent;
import cd.arnett.advent.events.annotations.*;
import cd.arnett.advent.events.annotations.data.HashSetModuleData;
import cd.arnett.advent.events.annotations.data.UUIDModuleData;
import cd.arnett.advent.events.modules.elimination.controls.EliminateCommand;
import cd.arnett.advent.events.modules.elimination.controls.InstateCommand;
import cd.arnett.advent.events.modules.winnable.WinnableModule;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import cd.arnett.caddamands.cattamands.cattamand.LiteralCattamand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public interface EliminationModule extends WinnableModule {

    String yamlFileName = "Elimination";
    String remainingListName = "remaining";
    String eliminatedListName = "eliminated";


    //region Module Data

    /*=================================================================================================
                        -  Module Data  -
    =================================================================================================*/

    /**
     * Provides a list of Remaining players to moduleData
     */
    @ModuleDataProvider(key = remainingListName, fileBasename = yamlFileName)
    @HashSetModuleData(type = UUIDModuleData.class)
    default HashSet<UUID> provideRemainingPlayers()
    {
        return new HashSet<>();
    }

    /**
     * Provides a list of Eliminated players to moduleData
     */
    @ModuleDataProvider(key = eliminatedListName, fileBasename = yamlFileName)
    @HashSetModuleData(type = UUIDModuleData.class)
    default HashSet<UUID> provideEliminatedPlayers()
    {
        return new HashSet<>();
    }


    /**
     * @return the actual list used to store eliminated players, not a copy
     */
    default HashSet<UUID> getEliminatedListData()
    {
        return this.getModuleData(eliminatedListName);
    }

    /**
     * @return the actual list used to store remaining players, not a copy
     */
    default HashSet<UUID> getRemainingListData()
    {
        return this.getModuleData(remainingListName);
    }

    //endregion


    //region Listeners

    /*=================================================================================================
                        -  Listeners  -
    =================================================================================================*/

    @OnJoinEvent
    default void trackEliminationStatus(Player player)
    {
        UUID id = player.getUniqueId();
        if (!isPlayerEliminated(id))
        {
            getRemainingListData().add(id);
        }
    }


    //endregion


    //region Events

    /*=================================================================================================
                        -  Events  -
    =================================================================================================*/

    /**
     * Called when a player is eliminated
     */
    void onEliminate(UUID eliminatedPlayer);

    /**
     * Called when a player is added to the remaining players
     */
    void onInstate(UUID instatedPlayer);



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
        return getRemainingListData().contains(playerId);
    }

    /**
     * @return List of player IDs of remain players
     */
    default List<UUID> getPlayersRemaining()
    {
        return List.copyOf(getRemainingListData());
    }

    /**
     * @return List of player IDs of eliminated player
     */
    default List<UUID> getPlayersEliminated()
    {
        return List.copyOf(getEliminatedListData());
    }

    /**
     *
     * @param playerId ID of player to check
     * @return has the player been eliminated
     */
    default boolean isPlayerEliminated(UUID playerId)
    {
        return getPlayersEliminated().contains(playerId);
    }

    //region Overloads

    //----------= Overloads =-----------------------------------------------------------------------------


    /**
     * @param player player to check
     * @return Is the player still in content for winning
     */
    default boolean isPlayerRemaining(Player player)
    {
        return getRemainingListData().contains(player.getUniqueId());
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
        var event = getEvent();
        for (UUID playerId : playerIds)
        {
            if(!event.isEventPlayer(playerId))
            {
                continue;
            }

            getRemainingListData().remove(playerId);
            getEliminatedListData().add(playerId);
            onEliminate(playerId);
        }
    }

    /**
     * calls onInstate event for the provided player (adds them to remaining players)
     * @param playerIds IDs of the player to instate
     */
    default void instate(UUID... playerIds)
    {
        var event = getEvent();
        for (UUID playerId : playerIds)
        {
            if(!event.isEventPlayer(playerId))
            {
                Advent.logger.warning("Attempted instating player who is not registered");
                continue;
            }

            getRemainingListData().add(playerId);
            getEliminatedListData().remove(playerId);
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
                        new EliminateCommand(getEvent()),
                        new InstateCommand(getEvent())
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
    @EventInfoProvider(name = "EliminationModule", separate = false)
    default Map<String, Object> eliminationsModuleInfo()
    {
        HashMap<String, Object> sectionMap = new HashMap<>();

        int remainingCount = getPlayersRemaining().size();
        int eliminatedCount = getPlayersEliminated().size();

        sectionMap.put("Remaining Count", remainingCount);
        sectionMap.put("Eliminated Count", eliminatedCount);

        return sectionMap;
    }

    /**
     * @return Provides information section on what players have won
     */
    @EventInfoProvider(name = "Eliminated", separate = true)
    default Map<String, Object> eliminatedInfo()
    {
        HashMap<String, Object> sectionMap = new HashMap<>();
        StringBuilder output = new StringBuilder();
        var players = getPlayersEliminated();

        AtomicInteger counter = new AtomicInteger(0);
        players.stream().map(Bukkit::getOfflinePlayer).map(OfflinePlayer::getName).forEach(name -> {
            boolean even = counter.getAndIncrement() % 2 == 0;

            output.append(even ? "<color:#c4c4c4>" : "</color:#c4c4c4>").append(name).append(' ');

        });

        sectionMap.put("Count", players.size());
        sectionMap.put("Players", output.toString());

        return sectionMap;
    }

    /**
     * @return Provides information section on what players have won
     */
    @EventInfoProvider(name = "Remaining", separate = true)
    default Map<String, Object> remainingInfo()
    {
        HashMap<String, Object> sectionMap = new HashMap<>();
        StringBuilder output = new StringBuilder();
        var players = getPlayersRemaining();

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
