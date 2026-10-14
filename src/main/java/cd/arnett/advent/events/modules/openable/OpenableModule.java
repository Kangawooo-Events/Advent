package cd.arnett.advent.events.modules.openable;

import cd.arnett.advent.events.annotations.*;
import cd.arnett.advent.events.annotations.data.BooleanModuleData;
import cd.arnett.advent.events.modules.openable.controls.CloseEventCommand;
import cd.arnett.advent.events.modules.openable.controls.OpenEventCommand;
import cd.arnett.advent.events.modules.EventModule;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import cd.arnett.caddamands.cattamands.cattamand.LiteralCattamand;
import org.bukkit.entity.Player;

import java.util.*;

public interface OpenableModule extends EventModule {


    //region Data

    /*=================================================================================================
                        -  Data  -
    =================================================================================================*/

    String OpenStateKey = "openable";

    @ModuleDataProvider(key = OpenStateKey)
    @BooleanModuleData
    default Boolean provideIsOpen()
    {
        return false;
    }

    default Boolean isOpen()
    {
        return getModuleData(OpenStateKey);
    }

    default void setOpen(boolean isOpen)
    {
        setModuleData(OpenStateKey, isOpen);
    }

    //endregion


    //region Events

    /*=================================================================================================
                        -  Events  -
    =================================================================================================*/

    @CanJoinEvent
    default boolean canJoinWhenOpened(Player player)
    {
        return player.isOp() || isOpen();
    }


    //endregion


    //region Controls

    /*=================================================================================================
                        -  Controls  -
    =================================================================================================*/

    @EventControl
    default Cattamand openableCommandControl()
    {
        return new LiteralCattamand("openable", List.of(
                new CloseEventCommand(getEvent()),
                new OpenEventCommand(getEvent())
        ));
    }

    /**
     * Call to open event
     */
    default void openEvent()
    {
        setOpen(true);
        getEvent().invokeCallbacks(OnEventOpen.class);
    }

    /**
     * Call to close event
     */
    default void closeEvent()
    {
        setOpen(false);
        getEvent().invokeCallbacks(OnEventClose.class);
    }

    //endregion


    //region Info

    /*=================================================================================================
                        -  Info  -
    =================================================================================================*/



    /**
     * @return Provides information section on what players are actively playing in the event
     */
    @EventInfoProvider(name = "Openable")
    default Map<String, Object> openableInfo()
    {
        return Map.of("Is Open", isOpen());
    }


    //endregion

}
