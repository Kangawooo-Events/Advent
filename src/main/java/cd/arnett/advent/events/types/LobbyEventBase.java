package cd.arnett.advent.events.types;

import cd.arnett.advent.events.EventBase;
import cd.arnett.advent.events.modules.playerState.SeparateStateModule;
import cd.arnett.advent.events.modules.relocation.RelocationModule;
import org.bukkit.entity.Player;

public abstract class LobbyEventBase extends EventBase implements RelocationModule, SeparateStateModule {

    @Override
    public final boolean requiresRegistration() {
        return false;
    }

    @Override
    public final boolean autoJoin() {
        return false;
    }

    @Override
    public final boolean autoOpen() {
        return true;
    }

    @Override
    public final boolean preventLeavingWhilePlaying() {
        return false;
    }

    @Override
    protected abstract void failJoinFallback(Player player);
}
