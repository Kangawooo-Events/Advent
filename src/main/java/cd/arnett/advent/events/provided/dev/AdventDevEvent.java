package cd.arnett.advent.events.provided.dev;

import cd.arnett.advent.Advent;
import cd.arnett.advent.events.EventBase;
import cd.arnett.advent.events.modules.playerState.PlayerState;
import cd.arnett.advent.events.modules.playerState.SeparateStateModule;
import cd.arnett.advent.events.modules.relocation.RelocationModule;
import cd.arnett.advent.events.types.HubEventBase;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public class AdventDevEvent extends HubEventBase {

    @Override
    public NamespacedKey eventKey() {
        return new NamespacedKey(Advent.singleton, "dev");
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
                "<bold><color:#ff597a>You Joined Dev"
        ));
    }

    @Override
    public void onLeaveEvent(Player player) {

    }

    @Override
    public PlayerState defaultJoinState() {
        return new PlayerState().setGamemode(GameMode.CREATIVE);
    }

    @Override
    public boolean saveEventLocation() {
        return false;
    }

}
