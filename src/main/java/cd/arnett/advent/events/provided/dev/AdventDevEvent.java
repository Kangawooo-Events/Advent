package cd.arnett.advent.events.provided.dev;

import cd.arnett.advent.Advent;
import cd.arnett.advent.events.EventBase;
import cd.arnett.advent.events.modules.playerState.PlayerState;
import cd.arnett.advent.events.modules.playerState.SeparateStateModule;
import cd.arnett.advent.events.modules.relocation.RelocationModule;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public class AdventDevEvent extends EventBase implements RelocationModule, SeparateStateModule {
    @Override
    public NamespacedKey eventKey() {
        return new NamespacedKey(Advent.singleton, "dev");
    }

    @Override
    public boolean requiresRegistration() {
        return true;
    }

    @Override
    public boolean autoJoin() {
        return true;
    }

    @Override
    public boolean autoOpen() {
        return true;
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
                "<bold><color:#ff597a>You Joined The Dev"
        ));
    }

    @Override
    public void onLeaveEvent(Player player) {

    }

    @Override
    public PlayerState defaultJoinState() {
        return new PlayerState();
    }

    @Override
    public boolean saveEventLocation() {
        return false;
    }
}
