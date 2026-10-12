package cd.arnett.advent.events.modules.respawn;

import org.bukkit.Location;

public class SpawnLocation {
    public final Location location;
    public final boolean requiresBlock;

    public SpawnLocation(Location location, boolean requiresBlock) {
        this.location = location;
        this.requiresBlock = requiresBlock;
    }
}
