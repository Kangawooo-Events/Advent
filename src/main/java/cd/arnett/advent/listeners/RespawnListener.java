package cd.arnett.advent.listeners;

import cd.arnett.advent.EventTracker;
import cd.arnett.advent.events.modules.respawn.RespawnModule;
import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class RespawnListener implements Listener {

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e)
    {
        var event = EventTracker.getPlayingEvent(
                e.getPlayer().getUniqueId()
        );

        if(event instanceof RespawnModule respawn)
        {
            e.setRespawnLocation(respawn.getRespawnLocation(e.getPlayer().getUniqueId()));
        }
    }


    @EventHandler
    public void onRespawnSet(PlayerSetSpawnEvent e)
    {
        if (e.isCancelled())
            return;

        var currentEvent = EventTracker.getPlayingEvent(e.getPlayer().getUniqueId());

        if(currentEvent instanceof RespawnModule respawn)
        {
            respawn.setSpawnLocation(e.getPlayer().getUniqueId(), e.getLocation(), !e.isForced());
        }
    }

}
