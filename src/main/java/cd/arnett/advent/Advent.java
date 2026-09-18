package cd.arnett.advent;

import cd.arnett.advent.commandTree.EventCommand;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Logger;

public final class Advent extends JavaPlugin {

    public static JavaPlugin singleton;
    public static Logger logger;

    @Override
    public void onEnable() {
        // Plugin startup logic

        singleton = this;
        logger = getLogger();

        try {
            TeleportationManager.readData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        TeleportationManager.startDisplayTask();


        /*
        Call registration here with lower priority (higher number) so that other plugins have time to add their events
        to the manager before the command is compiled
         */
        getLifecycleManager().registerEventHandler(
                LifecycleEvents.COMMANDS.newHandler(registration -> {
                    new EventCommand().registerAsRoot(this);
                }).priority(69420)
        );
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        try {
            TeleportationManager.writeData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Registers Listeners from the event Manager
     * @param listener Listener to register
     */
    static void registerListener(Listener listener)
    {
        singleton.getServer().getPluginManager().registerEvents(listener, singleton);
    }
}
