package cd.arnett.advent.listeners;

import cd.arnett.advent.EventTracker;
import cd.arnett.advent.events.EventBase;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinQuitListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e)
    {
        var player = e.getPlayer();
        var hubEvent = EventTracker.getHubEvent();
        var devEvent = EventTracker.getDevEvent();

        //find event to place player in
        for (EventBase event : EventTracker.getRegisteredEvents(player.getUniqueId())) {
            if(event == hubEvent || event == devEvent)
                continue;

            if(event.autoJoin() && event.canJoin(player))
            {
                event.join(player).thenAcceptAsync(result -> {
                    if (!result)
                    {
                        loginToHub(e);
                    }
                });
                return;
            }
        }

        //try to autojoin dev first if they are opposed
        if(player.isOp() && devEvent.canJoin(player))
        {
            devEvent.join(player).thenAcceptAsync(result -> {
                if (!result)
                {
                    loginToHub(e);
                }
            });
        }
        else
        {
            loginToHub(e);
        }
    }

    void loginToHub(PlayerJoinEvent e)
    {
        EventTracker.getHubEvent().join(e.getPlayer()).thenAcceptAsync(result -> {
            if(!result)
            {
                e.getPlayer().kick(MiniMessage.miniMessage().deserialize(
                        "<color:#ff597a>You Failed To Join This Server's Hub"
                ));
            }
        });
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
