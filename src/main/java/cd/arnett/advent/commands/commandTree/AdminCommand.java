package cd.arnett.advent.commands.commandTree;

import cd.arnett.advent.DestinationManager;
import cd.arnett.advent.EventTracker;
import cd.arnett.caddamands.cattamands.arguments.ArgumentHelper;
import cd.arnett.caddamands.cattamands.arguments.Cattarameter;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.NamespacedKey;

import java.util.List;

public class AdminCommand extends Cattamand {
    @Override
    public int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        var player = ArgumentHelper.getPlayerSender(context);
        EventTracker.getAdminEvent().join(player);
        return 0;
    }

    public int destinationExecute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        NamespacedKey eventKey = context.getArgument("event", NamespacedKey.class);
        var event = EventTracker.getEvent(eventKey);
        var player = ArgumentHelper.getPlayerSender(context);

        //teleport to destination
        if(event == null)
        {
            ArgumentHelper.sendSenderMiniMessage(context, "<red>The event " + eventKey + " does not exist," +
                    " so no destination could be found to teleport to");
        }
        else
        {
            var joinDestinations = DestinationManager.getDestinations(eventKey, "JOIN");
            var adminDestinations = DestinationManager.getDestinations(eventKey, "ADMIN");

            if(!adminDestinations.isEmpty())
            {
                DestinationManager.teleportToDestination(eventKey, "ADMIN", player);
            }
            else if(!joinDestinations.isEmpty())
            {
                DestinationManager.teleportToDestination(eventKey, "JOIN", player);
            }
            else
            {
                ArgumentHelper.sendSenderMiniMessage(
                        context,
                        "<red>The event " + eventKey
                                + " does not have any \"JOIN\" or \"ADMIN\" destinations associated with it."
                );
            }
        }

        //do the joining now
        return execute(context);
    }

    @Override
    public List<? extends Cattarameter> getArguments() {
        return List.of(
                new Cattarameter(
                        "event",
                        ArgumentTypes.namespacedKey(),
                        (ctx) ->
                                EventTracker.getEventNames(),
                        this::destinationExecute
                )
        );
    }

    @Override
    public String getName() {
        return "admin";
    }

    @Override
    public String getPermission() {
        return "";
    }

    @Override
    public List<String> getAliases() {
        return List.of();
    }
}
