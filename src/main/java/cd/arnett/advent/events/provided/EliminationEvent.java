package cd.arnett.advent.events.provided;

import cd.arnett.advent.Advent;
import cd.arnett.advent.events.EventBase;
import cd.arnett.advent.events.modules.elimination.EliminationModule;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EliminationEvent extends EventBase implements EliminationModule {

    @Override
    public NamespacedKey eventKey() {
        return new NamespacedKey(Advent.singleton, "elimevent");
    }

    @Override
    public boolean requiresRegistration() {
        return true;
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
        player.sendMessage(MiniMessage.miniMessage().deserialize("<green><bold>You joined the event elimevent"));
    }

    @Override
    public void onLeaveEvent(Player player) {
        player.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>You left the event"));
    }

    @Override
    public void onEliminate(UUID eliminatedPlayer) {
        Bukkit.getPlayer(eliminatedPlayer).sendMessage(MiniMessage.miniMessage().deserialize("<bold><red>you were eliminated"));
    }

    @Override
    public void onInstate(UUID instatedPlayer) {
        Bukkit.getPlayer(instatedPlayer).sendMessage(MiniMessage.miniMessage().deserialize("<bold><red>you were instated"));
    }

    @Override
    public void onWin(UUID winningPlayer) {
        Bukkit.getPlayer(winningPlayer).sendMessage(MiniMessage.miniMessage().deserialize("<bold><red>you won"));
    }

    @Override
    public void onWinRevoked(UUID revokedPlayer) {
        Bukkit.getPlayer(revokedPlayer).sendMessage(MiniMessage.miniMessage().deserialize("<bold><red>you got win revoked"));
    }
}
