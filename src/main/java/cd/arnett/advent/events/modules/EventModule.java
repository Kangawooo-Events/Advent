package cd.arnett.advent.events.modules;

import cd.arnett.advent.Advent;
import cd.arnett.advent.events.EventBase;

public interface EventModule {

    /**
     * @return The EventBase this interface should be assigned to
     */
    default EventBase getEvent()
    {
        try {
            return (EventBase) this;
        }
        catch (Exception e)
        {
            Advent.logger.warning("Event Type Modules can only be used on Event Base Classes");
            return null;
        }
    }


    //region Module Data

    /*=================================================================================================
                        -  Module Data  -
    =================================================================================================*/

    /**
     * Returns module data object stored under provided name
     */
    default <T> T getModuleData(String name)
    {
        return (T)getEvent().getModuleData(name);
    }

    /**
     * Returns module data object stored under provided name
     */
    default void setModuleData(String name, Object value)
    {
        getEvent().setModuleData(name, value);
    }

    //endregion


}
