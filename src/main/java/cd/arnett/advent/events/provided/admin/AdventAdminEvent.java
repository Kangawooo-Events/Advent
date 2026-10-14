package cd.arnett.advent.events.provided.admin;

import cd.arnett.advent.Advent;
import cd.arnett.advent.events.EventBase;
import cd.arnett.advent.events.modules.playerState.PlayerState;
import cd.arnett.advent.events.modules.playerState.SeparateStateModule;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public class AdventAdminEvent extends EventBase implements SeparateStateModule {
    @Override
    public NamespacedKey eventKey() {
        return new NamespacedKey(Advent.singleton, "admin");
    }

    @Override
    public boolean preventLeavingWhilePlaying() {
        return false;
    }

    @Override
    public void onEventStart() {

    }

    @Override
    public void onEventStop() {

    }

    @Override
    public void onJoinEvent(Player player) {

    }

    @Override
    public void onLeaveEvent(Player player) {

    }

    @Override
    public boolean canJoin(Player player) {
        if(!player.isOp())
            return false;

        return super.canJoin(player);
    }

    @Override
    public PlayerState defaultJoinState() {
        return new PlayerState().setGamemode(GameMode.SPECTATOR);
    }
}
