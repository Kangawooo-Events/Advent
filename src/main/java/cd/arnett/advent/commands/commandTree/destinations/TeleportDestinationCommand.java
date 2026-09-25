package cd.arnett.advent.commands.commandTree.destinations;

import cd.arnett.advent.EventTracker;
import cd.arnett.advent.DestinationManager;
import cd.arnett.advent.worlds.HubWorld;
import cd.arnett.caddamands.cattamands.arguments.ArgumentHelper;
import cd.arnett.caddamands.cattamands.arguments.Cattarameter;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TeleportDestinationCommand extends Cattamand {
    @Override
    public int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        NamespacedKey eventId = context.getArgument("event", NamespacedKey.class);
        String tag = context.getArgument("tag", String.class);
        List<Player> targets = ArgumentHelper.getPlayersFromArgs("targets", context);

        int destinationCount = DestinationManager.teleportToDestination(eventId, tag, targets);

        if(destinationCount > 0)
        {
            ArgumentHelper.sendSenderMiniMessage(
                    context, "<green> Found " + destinationCount + " Destinations!"
            );
        }
        else
        {
            ArgumentHelper.sendSenderMiniMessage(context, "<red> Failed to find Destination!");
        }

        return 0;
    }

    @Override
    public List<? extends Cattarameter> getArguments() {
        return List.of(
                new Cattarameter(
                        "event",
                        ArgumentTypes.namespacedKey(),
                        (ctx) -> {
                            var keys = new ArrayList<>(EventTracker.getEventNames());
                            keys.add(HubWorld.destinationKey);
                            return keys;
                        }
                ),
                new Cattarameter(
                        "tag",
                        StringArgumentType.string(),
                        (ctx) ->
                        {
                            NamespacedKey eventId = ctx.getLastChild().getArgument("event", NamespacedKey.class);

                            var destinations = DestinationManager.getDestinations(eventId);

                            return destinations.keySet();
                        },
                        (ctx) -> {
                            NamespacedKey eventId = ctx.getArgument("event", NamespacedKey.class);
                            String tag = ctx.getArgument("tag", String.class);
                            DestinationManager.teleportToDestination(eventId, tag, ArgumentHelper.getPlayerSender(ctx));
                            return 0;
                        }
                ),
                new Cattarameter(
                        "targets",
                        ArgumentTypes.players()
                )
        );
    }

    @Override
    public String getName() {
        return "teleport";
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
