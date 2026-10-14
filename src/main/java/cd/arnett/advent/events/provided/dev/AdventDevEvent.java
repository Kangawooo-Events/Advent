package cd.arnett.advent.events.provided.dev;

import cd.arnett.advent.Advent;
import cd.arnett.advent.events.annotations.CanJoinEvent;
import cd.arnett.advent.events.modules.playerState.PlayerState;
import cd.arnett.advent.events.types.LobbyEventBase;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public class AdventDevEvent extends LobbyEventBase {

    @Override
    public NamespacedKey eventKey() {
        return new NamespacedKey(Advent.singleton, "dev");
    }

    @Override
    public void onEventStart() {

    }

    @Override
    public void onEventStop() {

    }

    @Override
    public void onJoinEvent(Player player) {
        System.out.println("JOPINED DEAC");
        player.sendMessage(MiniMessage.miniMessage().deserialize(
                "<bold><color:#ff597a>You Joined Dev"
        ));
    }

    @Override
    public void onLeaveEvent(Player player) {
        System.out.println("LEFT DEAC");
    }

    @Override
    public PlayerState defaultJoinState() {
        return new PlayerState().setGamemode(GameMode.CREATIVE);
    }

    @Override
    public boolean saveEventLocation() {
        return true;
    }

    @Override
    public int priority() {
        return 1;
    }

    @CanJoinEvent
    public boolean canJoinDev(Player player)
    {
        return player.isOp();
    }
}
