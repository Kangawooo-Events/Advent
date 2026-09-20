package cd.arnett.advent.events.types;

import cd.arnett.advent.Advent;
import cd.arnett.advent.events.EventBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

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



    /**
     * Returns module data for specifically Player Integer Maps
     * @param name Name of map
     * @return The map stored in moduleData
     */
    default HashMap<UUID, Integer> getModulePlayerIntegerMap(String name)
    {
        return (HashMap<UUID, Integer>)getEvent().getModuleData(name);
    }

    /**
     * Returns module data for specifically Player Lists
     * @param name Name of list
     * @return The list stored in moduleData
     */
    default ArrayList<UUID> getModulePlayerList(String name)
    {
        return (ArrayList<UUID>)getEvent().getModuleData(name);
    }


    //endregion
    /**
     * Returns module data object stored under provided name
     */
    default <T> T getModuleData(String name)
    {
        return (T)getEvent().getModuleData(name);
    }


}
