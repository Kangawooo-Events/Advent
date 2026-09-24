package cd.arnett.advent.commands.commandTree.destinations;

import cd.arnett.advent.EventManager;
import cd.arnett.advent.TeleportationManager;
import cd.arnett.caddamands.cattamands.arguments.ArgumentHelper;
import cd.arnett.caddamands.cattamands.arguments.Cattarameter;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.List;

public class RemoveDestinationCommand extends Cattamand {
    @Override
    public int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        NamespacedKey eventId = context.getArgument("event", NamespacedKey.class);
        String tag = context.getArgument("tag", String.class);
        Player sender = ArgumentHelper.getPlayerSender(context);

        sender.sendMessage(MiniMessage.miniMessage().deserialize(
                "<red><bold>Deleted </bold>" + TeleportationManager.removeDestinations(sender.getLocation(), eventId, tag)
        ));

        return 0;
    }

    @Override
    public List<? extends Cattarameter> getArguments() {
        return List.of(
                new Cattarameter(
                        "event",
                        ArgumentTypes.namespacedKey(),
                        (ctx) -> EventManager.getEventNames()
                ),
                new Cattarameter(
                        "tag",
                        StringArgumentType.string(),
                        (ctx) ->
                        {
                            NamespacedKey eventId = ctx.getLastChild().getArgument("event", NamespacedKey.class);

                            var destinations = TeleportationManager.getDestinations(eventId);

                            return destinations.keySet();
                        }
                )
        );
    }

    @Override
    public String getName() {
        return "remove";
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
