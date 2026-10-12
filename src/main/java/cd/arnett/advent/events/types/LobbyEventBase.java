package cd.arnett.advent.events.types;

import cd.arnett.advent.events.EventBase;
import cd.arnett.advent.events.modules.playerState.SeparateStateModule;
import cd.arnett.advent.events.modules.relocation.RelocationModule;
import cd.arnett.advent.events.modules.respawn.RespawnModule;

public abstract class LobbyEventBase extends EventBase implements RelocationModule, SeparateStateModule, RespawnModule {

    @Override
    public boolean requiresRegistration() {
        return false;
    }

    @Override
    public final boolean autoJoin() {
        return true;
    }

    @Override
    public final boolean autoOpen() {
        return true;
    }

    @Override
    public boolean preventLeavingWhilePlaying() {
        return false;
    }

    /**
     * @return Priority for what event to join when doing a generic join. Higher number has higher priority.
     * Defaults to 10 in EventBase and 5 in LobbyEventBase
     */
    @Override
    public int priority()
    {
        return 5;
    }
}
