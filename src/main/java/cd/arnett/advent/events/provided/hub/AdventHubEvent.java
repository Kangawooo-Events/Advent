package cd.arnett.advent.events.provided.hub;

import cd.arnett.advent.Advent;
import cd.arnett.advent.events.modules.playerState.PlayerState;
import cd.arnett.advent.events.types.LobbyEventBase;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public class AdventHubEvent extends LobbyEventBase {

    @Override
    public NamespacedKey eventKey() {
        return new NamespacedKey(Advent.singleton, "hub");
    }

    @Override
    public void onEventOpen() {

    }

    @Override
    public void onEventClose() {

    }

    @Override
    public void onJoinEvent(Player player) {
        player.sendMessage(MiniMessage.miniMessage().deserialize(
                "<bold><color:#ff597a>You Joined Hub"
        ));
    }

    @Override
    public void onLeaveEvent(Player player) {

    }

    @Override
    public PlayerState defaultJoinState() {
        return new PlayerState().setGamemode(GameMode.SURVIVAL);
    }

    @Override
    public boolean playersInvulnerable() {
        return true;
    }

    @Override
    public boolean canPvPByDefault() {
        return false;
    }

    @Override
    public boolean saveEventLocation() {
        return false;
    }

    @Override
    protected void failJoinFallback(Player player) {

    }
}
