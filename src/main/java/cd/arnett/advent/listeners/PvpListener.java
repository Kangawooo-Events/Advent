package cd.arnett.advent.listeners;

import cd.arnett.advent.EventTracker;
import cd.arnett.advent.events.EventBase;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashMap;
import java.util.UUID;

public class PvpListener implements Listener {

    public static HashMap<UUID, Boolean> individuallySetPvpRules = new HashMap<>();


    @EventHandler
    public void OnEntityDamage(EntityDamageByEntityEvent e)
    {
        if(e.isCancelled() || !(e.getEntity() instanceof Player player))
        {
            return;
        }

        //really basic pvp check but may be expanded later
        boolean isPvP = e.getDamager() instanceof Player;
        var other = (Player)e.getDamager();

        if(isPvP)
        {
            //check for attacked player
            if(individuallySetPvpRules.containsKey(player.getUniqueId()))
            {
                e.setCancelled(individuallySetPvpRules.get(player.getUniqueId()));
            }
            else
            {
                EventBase currentEvent = EventTracker.getPlayingEvent(player.getUniqueId());
                if(currentEvent != null && !currentEvent.playersInvulnerable() && isPvP && !currentEvent.isPVPAllowed())
                {
                    e.setCancelled(true);
                }
            }

            if(e.isCancelled())
                return;

            //check for attacking player
            if(individuallySetPvpRules.containsKey(other.getUniqueId()))
            {
                e.setCancelled(individuallySetPvpRules.get(other.getUniqueId()));
            }
            else
            {
                EventBase currentEvent = EventTracker.getPlayingEvent(other.getUniqueId());
                if(currentEvent != null && !currentEvent.playersInvulnerable() && isPvP && !currentEvent.isPVPAllowed())
                {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void OnEntityDamage(EntityDamageEvent e)
    {

        if((e.isCancelled() || !(e.getEntity() instanceof Player player)))
        {
            return;
        }

        EventBase currentEvent = EventTracker.getPlayingEvent(player.getUniqueId());
        if(currentEvent != null && currentEvent.playersInvulnerable())
        {
            e.setCancelled(true);
        }

    }
}
