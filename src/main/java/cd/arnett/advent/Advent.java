package cd.arnett.advent;

import cd.arnett.advent.commands.commandTree.EventCommand;
import cd.arnett.advent.commands.commandTree.destinations.CreateDestinationCommand;
import cd.arnett.advent.commands.commandTree.destinations.DestinationViewCommand;
import cd.arnett.advent.commands.commandTree.destinations.RemoveDestinationCommand;
import cd.arnett.advent.commands.commandTree.destinations.TeleportDestinationCommand;
import cd.arnett.advent.destinations.box.command.CreateBoxDestinationCommand;
import cd.arnett.advent.destinations.circle.command.CreateCircleDestinationCommand;
import cd.arnett.advent.destinations.spot.command.CreateSpotDestinationCommand;
import cd.arnett.advent.destinations.box.BoxDestination;
import cd.arnett.advent.destinations.circle.CircleDestination;
import cd.arnett.advent.destinations.spot.SpotDestination;
import cd.arnett.advent.test.EliminationEvent;
import cd.arnett.advent.test.TestEvent;
import cd.arnett.caddamands.cattamands.cattamand.LiteralCattamand;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public final class Advent extends JavaPlugin {

    public static JavaPlugin singleton;
    public static Logger logger;

    @Override
    public void onEnable() {
        // Plugin startup logic

        singleton = this;
        logger = getLogger();

        /*
        Call registration here with lower priority (higher number) so that other plugins have time to add their events
        to the manager before the command is compiled
         */
        getLifecycleManager().registerEventHandler(
                LifecycleEvents.COMMANDS.newHandler(registration -> {
                    new EventCommand().registerAsRoot(this, registration);
                }).priority(69420)
        );

        new LiteralCattamand("destination", List.of(
                new CreateDestinationCommand(),
                new RemoveDestinationCommand(),
                new DestinationViewCommand(),
                new TeleportDestinationCommand()
        )).setAliases(List.of("dest", "eventdestination")).registerAsRoot(this);

        EventManager.registerEvent(new TestEvent());
        EventManager.registerEvent(new EliminationEvent());

        try {
            TeleportationManager.registerType(BoxDestination.class);
            TeleportationManager.registerType(CircleDestination.class);
            TeleportationManager.registerType(SpotDestination.class);
            TeleportationManager.readData();
            TeleportationManager.startDisplayTask();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        try {
            TeleportationManager.writeDataNow();
            EventManager.saveDataNow();
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
