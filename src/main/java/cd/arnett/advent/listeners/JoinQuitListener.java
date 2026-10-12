package cd.arnett.advent.listeners;

import cd.arnett.advent.EventTracker;
import cd.arnett.advent.events.EventBase;
import cd.arnett.advent.events.modules.relocation.RelocationModule;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.HashMap;

public class JoinQuitListener implements Listener {

    //the event to be joined is decided in onSpawn while the joining happens in onJoin, this bridges the gap
    HashMap<Player, EventBase> decidedToJoin = new HashMap<>();

    @EventHandler
    public void onSpawn(PlayerSpawnLocationEvent e)
    {
        var player = e.getPlayer();
        var event = EventTracker.getAutoJoinEvent(player);

        if (event == null)
        {
            e.getPlayer().kick(MiniMessage.miniMessage().deserialize(
                    "<color:#ff597a>You did not meed the requirements to join any events in this server."
            ));
            return;
        }

        decidedToJoin.put(player, event);

        if(event instanceof RelocationModule relocation)
        {
            relocation.preRelocateForSpawnEvent(player, e);
        }

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e)
    {
        var joinEvent = decidedToJoin.getOrDefault(e.getPlayer(), EventTracker.getHubEvent());
        joinEvent.join(e.getPlayer());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e)
    {
        Player player = e.getPlayer();
        var event = EventTracker.getPlayingEvent(e.getPlayer().getUniqueId());

        if(event != null)
        {
            event.leave(player);
        }
    }

}
