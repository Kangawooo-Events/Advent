package cd.arnett.advent.events.modules.playerState;

import cd.arnett.advent.events.PlayerEventStatus;
import cd.arnett.advent.events.annotations.*;
import cd.arnett.advent.events.annotations.data.PlayerStateModuleData;
import cd.arnett.advent.events.annotations.data.UUIDHashMapModuleData;
import cd.arnett.advent.events.modules.EventType;
import cd.arnett.advent.events.modules.playerState.controls.ClearStatesCommand;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import cd.arnett.caddamands.cattamands.cattamand.LiteralCattamand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public interface SeparateStateModule extends EventType {

    String playerStatesKey = "PlayerStates";
    String defaultPlayerStateKey = "DefaultPlayerState";


    //region ModuleData

    /*=================================================================================================
                        -  ModuleData  -
    =================================================================================================*/

    @ModuleDataProvider(key = playerStatesKey, fileBasename = "PlayerStates")
    @UUIDHashMapModuleData(type = PlayerStateModuleData.class)
    default HashMap<UUID, PlayerState> playerStates()
    {
        return new HashMap<>();
    }


    @ModuleDataProvider(key = defaultPlayerStateKey, fileBasename = "DefaultPlayerState")
    @PlayerStateModuleData()
    PlayerState defaultJoinState();

    default HashMap<UUID, PlayerState> getPlayerStates()
    {
        return getModuleData(playerStatesKey);
    }

    default void clearPlayerState(UUID id)
    {
        if(getEvent().isRegistered(id, PlayerEventStatus.PLAYING))
        {
            getEvent().leave(Bukkit.getPlayer(id));
        }

        getPlayerStates().remove(id);
    }


    //endregion



    //region Event

    /*=================================================================================================
                        -  Event  -
    =================================================================================================*/


    @OnJoinEvent
    default void swapStateOnJoin(Player joined)
    {
        getPlayerStates().getOrDefault(joined.getUniqueId(), defaultJoinState()).applyToPlayer(joined);
    }

    @OnLeaveEvent
    default void saveStateOnLeave(Player joined)
    {
        getPlayerStates().put(joined.getUniqueId(), new PlayerState(joined));
    }


    //endregion


    //region Info

    /*=================================================================================================
                        -  Info  -
    =================================================================================================*/


    @EventInfoProvider(name = "PlayerStates", separate = true)
    default HashMap<String, Object> getInfo()
    {
        var map = new HashMap<String, Object>();

        var defaultState = defaultJoinState();

        map.put("Count Saved", getPlayerStates().size());
        map.put("Default air", defaultState.getAir());
        map.put("Default maxAir", defaultState.getMaxAir());
        map.put("Default health", defaultState.getHealth());
        map.put("Default exp", defaultState.getExp());
        map.put("Default food", defaultState.getSaturation());
        map.put("Default absorption", defaultState.getAbsorption());
        map.put("Default fireTicks", defaultState.getFireTicks());
        map.put("Default freezeTicks", defaultState.getFreezeTicks());
        map.put("Default saturationRegen", defaultState.getSaturationRegen());
        map.put("Default starvationRate", defaultState.getStarvationRate());
        map.put("Default level", defaultState.getLevel());


        return map;
    }


    //endregion


    //region Command Control

    /*=================================================================================================
                        -  Command Control  -
    =================================================================================================*/


    @EventControl
    default Cattamand getRemoveStateControl()
    {
        return new LiteralCattamand("states", List.of(new ClearStatesCommand())) ;
    }


    //endregion

}
