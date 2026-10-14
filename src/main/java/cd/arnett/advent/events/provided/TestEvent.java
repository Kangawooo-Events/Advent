package cd.arnett.advent.events.provided;

import cd.arnett.advent.Advent;
import cd.arnett.advent.events.EventBase;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public class TestEvent extends EventBase {

    @Override
    public NamespacedKey eventKey() {
        return new NamespacedKey(Advent.singleton, "testevent");
    }

    @Override
    public boolean autoJoin() {
        return false;
    }

    @Override
    public boolean autoOpen() {
        return false;
    }

    @Override
    public boolean preventLeavingWhilePlaying() {
        return false;
    }

    @Override
    public void onEventOpen() {
        getOnlinePlayers().forEach(player -> {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green><bold>Event Started"));
        });
    }

    @Override
    public void onEventClose() {
        getPlayingPlayers().forEach(player -> {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>Event Stopped"));
        });
    }

    @Override
    public void onJoinEvent(Player player) {
        player.sendMessage(MiniMessage.miniMessage().deserialize("<green><bold>You joined the event testevent"));
    }

    @Override
    public void onLeaveEvent(Player player) {
        player.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>You left the event"));
    }

}
