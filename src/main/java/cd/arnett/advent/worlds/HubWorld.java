package cd.arnett.advent.worlds;

import cd.arnett.advent.Advent;
import cd.arnett.advent.EventTracker;
import cd.arnett.advent.destinations.DestinationTeleportTask;
import cd.arnett.advent.pvp.PvpControl;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class HubWorld {

    public static NamespacedKey defaultHubWorld = new NamespacedKey("minecraft", "overworld");
    public static PvpControl pvpControl = new PvpControl();
    public static NamespacedKey destinationKey = new NamespacedKey(Advent.singleton, "hub");
    public static DestinationTeleportTask teleportTask = new DestinationTeleportTask(destinationKey, "JOIN");

    public static World getHubWorld()
    {
        var world = Bukkit.getWorld(defaultHubWorld);
        return world == null ? Bukkit.getWorlds().get(0) : world;
    }

    public static void sendToHub(Player player)
    {
        var playingEvent = EventTracker.getPlayingEvent(player.getUniqueId());

        if(playingEvent != null)
        {
            playingEvent.leave(player);
        }

        teleportTask.teleportAsync(player);
    }
}
