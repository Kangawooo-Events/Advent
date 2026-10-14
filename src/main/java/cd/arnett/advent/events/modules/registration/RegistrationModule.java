package cd.arnett.advent.events.modules.registration;

import cd.arnett.advent.EventTracker;
import cd.arnett.advent.events.annotations.*;
import cd.arnett.advent.events.annotations.data.HashSetModuleData;
import cd.arnett.advent.events.annotations.data.UUIDModuleData;
import cd.arnett.advent.events.modules.registration.controls.AddRegistrationCommand;
import cd.arnett.advent.events.modules.registration.controls.RemoveRegistrationCommand;
import cd.arnett.advent.events.modules.EventModule;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import cd.arnett.caddamands.cattamands.cattamand.LiteralCattamand;
import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public interface RegistrationModule extends EventModule {

    //region Data

    /*=================================================================================================
                        -  Data  -
    =================================================================================================*/

    String registeredPlayersKey = "registered";
    String registrationFile = "Registration";

    @ModuleDataProvider(key = registeredPlayersKey, fileBasename = registrationFile)
    @HashSetModuleData(type = UUIDModuleData.class)
    default HashSet<UUID> provideRegisteredPlayersData()
    {
        return new HashSet<>();
    }

    default HashSet<UUID> getRegisteredPlayers()
    {
        return getModuleData(registeredPlayersKey);
    }


    //endregion

    //region Queries

    /*=================================================================================================
                        -  Queries  -
    =================================================================================================*/

    /**
     * Is a player registered to play in this event
     * @param id Id of Player to check
     * @return Registration status
     */
    default boolean isRegistered(UUID id)
    {
        return getRegisteredPlayers().contains(id);
    }


    //endregion



    //region Controls

    /*=================================================================================================
                        -  Controls  -
    =================================================================================================*/

    @EventControl
    default Cattamand registrationCommandControl()
    {
        return new LiteralCattamand("registrations", List.of(
                new AddRegistrationCommand(getEvent()),
                new RemoveRegistrationCommand(getEvent())
        ));
    }


    /**
     * Registers a player for this event (overload)
     * @param player Profile of player to register
     */
    default void register(PlayerProfile player)
    {
        getRegisteredPlayers().add(player.getId());
        getEvent().invokeCallbacks(OnRegistrationAdd.class, null, player);
    }


    /**
     * Revokes Registration from an event (and removes them if they are playing)
     * @param player Profile of player to register
     */
    default void revokeRegistration(PlayerProfile player)
    {
        if(getEvent().isPlaying(player.getId()))
        {
            Player playerObj = Bukkit.getPlayer(player.getId());
            EventTracker.getHubEvent().join(playerObj);
        }

        getRegisteredPlayers().remove(player.getId());
        getEvent().invokeCallbacks(OnRegistrationRemove.class, null, player);
    }

    //region Overloads

    //----------= Overloads =-----------------------------------------------------------------------------


    /**
     * Revokes Registration from an event (and removes them if they are playing) (overload)
     * @param id ID of Player to register
     */
    default void revokeRegistration(UUID id)
    {
        revokeRegistration(Bukkit.createProfile(id));
    }

    /**
     * Registers a player for this event, removes them from the revoked list if present
     * @param id ID of player to register
     */
    default void register(UUID id)
    {
        register(Bukkit.createProfile(id));
    }

    //endregion




    //endregion


    //region Info

    /*=================================================================================================
                        -  Info  -
    =================================================================================================*/



    /**
     * @return Provides information section on what players are actively playing in the event
     */
    @EventInfoProvider(name = "Registered", separate = true)
    default Map<String, Object> registeredInfo()
    {
        HashMap<String, Object> sectionMap = new HashMap<>();
        StringBuilder output = new StringBuilder();
        var players = getRegisteredPlayers();

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
