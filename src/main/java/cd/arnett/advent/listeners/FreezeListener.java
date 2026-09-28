package cd.arnett.advent.listeners;

import cd.arnett.advent.EventTracker;
import cd.arnett.advent.events.EventBase;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashSet;
import java.util.UUID;

public class FreezeListener implements Listener {
    public static HashSet<UUID> individuallyFrozenPlayers = new HashSet<>();

    @EventHandler
    public void OnMovement(PlayerMoveEvent e)
    {
        Player player = e.getPlayer();
        if(e.isCancelled() || player.isOp() )
            return;

        EventBase currentEvent = EventTracker.getPlayingEvent(player.getUniqueId());
        if(individuallyFrozenPlayers.contains(player.getUniqueId()) ||
                (currentEvent != null && currentEvent.isFrozen()))
        {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void OnDamage(EntityDamageEvent e)
    {
        if(e.isCancelled()|| !(e.getDamageSource().getCausingEntity() instanceof Player player) || player.isOp() )
        {
            return;
        }
        EventBase currentEvent = EventTracker.getPlayingEvent(player.getUniqueId());
        if(individuallyFrozenPlayers.contains(player.getUniqueId()) ||
                (currentEvent != null && currentEvent.isFrozen()))
        {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void OnInventoryClick(InventoryClickEvent e)
    {
        if(e.isCancelled() || !(e.getWhoClicked() instanceof Player player) || player.isOp() )
        {
            return;
        }
        EventBase currentEvent = EventTracker.getPlayingEvent(player.getUniqueId());
        if(individuallyFrozenPlayers.contains(player.getUniqueId()) ||
                (currentEvent != null && currentEvent.isFrozen()))
        {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void OnInventoryDrag(InventoryDragEvent e)
    {
        if(e.isCancelled() || !(e.getWhoClicked() instanceof Player player) || player.isOp())
        {
            return;
        }
        EventBase currentEvent = EventTracker.getPlayingEvent(player.getUniqueId());
        if(individuallyFrozenPlayers.contains(player.getUniqueId()) ||
                (currentEvent != null && currentEvent.isFrozen()))
        {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void OnInventoryDrag(InventoryOpenEvent e)
    {
        if(e.isCancelled() || !(e.getPlayer() instanceof Player player)|| e.getPlayer().isOp())
        {
            return;
        }
        EventBase currentEvent = EventTracker.getPlayingEvent(player.getUniqueId());
        if(individuallyFrozenPlayers.contains(player.getUniqueId()) ||
                (currentEvent != null && currentEvent.isFrozen()))
        {
            e.setCancelled(true);
        }
    }
}
