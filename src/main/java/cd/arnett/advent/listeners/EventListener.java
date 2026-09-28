package cd.arnett.advent.listeners;

import cd.arnett.advent.events.EventBase;
import org.bukkit.event.Listener;

public class EventListener implements Listener {

    final EventBase event;

    public EventListener(EventBase event)
    {
        this.event = event;
    }

}
