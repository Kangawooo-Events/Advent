package cd.arnett.advent.events.modules;

import cd.arnett.advent.Advent;
import cd.arnett.advent.events.EventBase;

public interface EventType {

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


    //region Get Data

    /*=================================================================================================
                        -  Get Data  -
    =================================================================================================*/


    //endregion
    /**
     * Returns module data object stored under provided name
     */
    default <T> T getModuleData(String name)
    {
        return (T)getEvent().getModuleData(name);
    }


}
