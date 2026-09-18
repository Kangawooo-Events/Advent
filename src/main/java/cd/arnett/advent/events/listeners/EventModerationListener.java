package cd.arnett.advent.events.listeners;

import cd.arnett.advent.Advent;
import cd.arnett.advent.EventManager;
import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class EventModerationListener implements Listener {

    @EventHandler
    public void onBan(PlayerQuitEvent e)
    {
        if (e.getPlayer().isBanned())
        {
            EventManager.revokeRegistrations(e.getPlayer());
        }
    }
}
