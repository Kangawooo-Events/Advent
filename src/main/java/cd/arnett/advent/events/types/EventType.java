package cd.arnett.advent.events.types;

import cd.arnett.advent.Advent;
import cd.arnett.advent.events.EventBase;

import java.util.ArrayList;
import java.util.UUID;

public interface EventType {

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

    default ArrayList<UUID> getPlayerList(String name)
    {
        return getEvent().getTrackedPlayerList(name);
    }

}
