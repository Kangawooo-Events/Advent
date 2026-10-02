package cd.arnett.advent.listeners;

import cd.arnett.advent.EventTracker;
import cd.arnett.advent.events.EventBase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;

import java.util.HashSet;
import java.util.UUID;

public class FreezeManager implements Listener {
    public static HashSet<UUID> individuallyFrozenPlayers = new HashSet<>();

    public static boolean isFrozen(Player player) {
        UUID playerId = player.getUniqueId();
        EventBase currentEvent = EventTracker.getPlayingEvent(playerId);
        return individuallyFrozenPlayers.contains(player.getUniqueId())
                || EventTracker.limboPlayers.contains(playerId)
                || (currentEvent != null && currentEvent.isFrozen());
    }

    @EventHandler
    public void OnMovement(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (e.isCancelled() || player.isOp())
            return;

        if (isFrozen(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void OnDamage(EntityDamageEvent e) {
        if (e.isCancelled() || !(e.getDamageSource().getCausingEntity() instanceof Player player) || player.isOp()) {
            return;
        }
        if (isFrozen(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void OnInventoryClick(InventoryClickEvent e) {
        if (e.isCancelled() || !(e.getWhoClicked() instanceof Player player) || player.isOp()) {
            return;
        }
        if (isFrozen(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void OnInventoryDrag(InventoryDragEvent e) {
        if (e.isCancelled() || !(e.getWhoClicked() instanceof Player player) || player.isOp()) {
            return;
        }
        if (isFrozen(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void OnInventoryOpen(InventoryOpenEvent e) {
        if (e.isCancelled() || !(e.getPlayer() instanceof Player player) || e.getPlayer().isOp()) {
            return;
        }
        if (isFrozen(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void OnBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        if (e.isCancelled() || player.isOp()) {
            return;
        }
        if (isFrozen(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void OnBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        if (e.isCancelled() || player.isOp()) {
            return;
        }
        if (isFrozen(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void OnInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (e.isCancelled() || player.isOp()) {
            return;
        }
        if (isFrozen(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void OnPortalTravel(PlayerPortalEvent e) {
        Player player = e.getPlayer();
        if (e.isCancelled() || player.isOp()) {
            return;
        }
        if (isFrozen(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void OnPlayerDropItem(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        if (e.isCancelled() || player.isOp()) {
            return;
        }
        if (isFrozen(player)) {
            e.setCancelled(true);
        }
    }
}