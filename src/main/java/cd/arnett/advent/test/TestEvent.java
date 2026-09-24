package cd.arnett.advent.test;

import cd.arnett.advent.Advent;
import cd.arnett.advent.events.EventBase;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TestEvent extends EventBase {

    @Override
    public NamespacedKey eventKey() {
        return new NamespacedKey(Advent.singleton, "testevent");
    }

    @Override
    public boolean requiresRegistration() {
        return true;
    }

    @Override
    public void onEventStart() {
        getOnlinePlayers().forEach(player -> {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green><bold>Event Started"));
        });
    }

    @Override
    public void onEventStop() {
        getPlayingPlayers().forEach(player -> {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>Event Stopped"));
        });
    }

    @Override
    public void onJoinEvent(Player player) {
        player.sendMessage(MiniMessage.miniMessage().deserialize("<green><bold>You joined the event"));
    }

    @Override
    public void onLeaveEvent(Player player) {
        player.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>You left the event"));
    }

}
