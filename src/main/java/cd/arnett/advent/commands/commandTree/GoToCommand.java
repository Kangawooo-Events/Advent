package cd.arnett.advent.commands.commandTree;

import cd.arnett.advent.DestinationManager;
import cd.arnett.advent.EventTracker;
import cd.arnett.advent.events.EventBase;
import cd.arnett.advent.events.modules.relocation.RelocationModule;
import cd.arnett.caddamands.cattamands.arguments.ArgumentHelper;
import cd.arnett.caddamands.cattamands.arguments.Cattarameter;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.List;

public class GoToCommand extends Cattamand {

    @Override
    public int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        NamespacedKey eventKey = context.getArgument("event", NamespacedKey.class);
        var event = EventTracker.getEvent(eventKey);
        var player = ArgumentHelper.getPlayerSender(context);

        if(event == null)
        {
            ArgumentHelper.sendSenderMiniMessage(context, "<red>The event " + eventKey + " does not exist");
            return 0;
        }

        var joinDestinations = DestinationManager.getDestinations(eventKey, "JOIN");
        var adminDestinations = DestinationManager.getDestinations(eventKey, "ADMIN");

        if(!joinDestinations.isEmpty())
        {
            DestinationManager.teleportToDestination(eventKey, "JOIN", player);
            return 0;
        }
        else if(!adminDestinations.isEmpty())
        {
            DestinationManager.teleportToDestination(eventKey, "ADMIN", player);
            return 0;
        }
        else
        {
            ArgumentHelper.sendSenderMiniMessage(
                    context,
                    "<red>The event " + eventKey
                            + " does not have any \"JOIN\" or \"ADMIN\" destinations associated with it."
            );
            return 0;
        }

    }

    @Override
    public List<? extends Cattarameter> getArguments() {
        return List.of(
                new Cattarameter(
                        "event",
                        ArgumentTypes.namespacedKey(),
                        (ctx) ->
                                EventTracker.getEventNames()
                )
        );
    }

    @Override
    public String getName() {
        return "goto";
    }

    @Override
    public String getPermission() {
        return "";
    }

    @Override
    public List<String> getAliases() {
        return List.of();
    }

    @Override
    public List<? extends Cattamand> getChildren() {
        return List.of();
    }
}
