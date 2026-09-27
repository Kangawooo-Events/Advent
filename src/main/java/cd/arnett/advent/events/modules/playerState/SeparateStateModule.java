package cd.arnett.advent.events.modules.playerState;

import cd.arnett.advent.events.annotations.EventInfoProvider;
import cd.arnett.advent.events.annotations.ModuleDataProvider;
import cd.arnett.advent.events.annotations.OnJoinEvent;
import cd.arnett.advent.events.annotations.OnLeaveEvent;
import cd.arnett.advent.events.annotations.data.ItemStackArrayModuleData;
import cd.arnett.advent.events.annotations.data.PlayerStateModuleData;
import cd.arnett.advent.events.annotations.data.UUIDHashMapModuleData;
import cd.arnett.advent.events.modules.EventType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.UUID;

public interface SeparateStateModule extends EventType {

    String playerStatesKey = "PlayerStates";


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

    default HashMap<UUID, PlayerState> getPlayerStates()
    {
        return getModuleData(playerStatesKey);
    }


    //endregion


    //region Abstract Properties

    /*=================================================================================================
                        -  Abstract Properties  -
    =================================================================================================*/


    PlayerState defaultJoinState();


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
        map.put("Default flySpeed", defaultState.getFlySpeed());
        map.put("Default walkSpeed", defaultState.getWalkSpeed());
        map.put("Default fallDist", defaultState.getFallDist());
        map.put("Default saturationRegen", defaultState.getSaturationRegen());
        map.put("Default starvationRate", defaultState.getStarvationRate());
        map.put("Default level", defaultState.getLevel());


        return map;
    }


    //endregion

}
