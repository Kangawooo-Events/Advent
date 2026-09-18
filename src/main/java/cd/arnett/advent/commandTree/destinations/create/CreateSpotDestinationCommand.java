package cd.arnett.advent.commandTree.destinations.create;

import cd.arnett.advent.EventManager;
import cd.arnett.advent.TeleportationManager;
import cd.arnett.advent.destinations.spot.SpotDestination;
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

import java.util.List;

public class CreateSpotDestinationCommand extends Cattamand {
    @Override
    public int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        NamespacedKey eventId = context.getArgument("event", NamespacedKey.class);
        String tag = context.getArgument("tag", String.class);

        Player sender = ArgumentHelper.getPlayerSender(context);

        TeleportationManager.addDestination(
                new SpotDestination().initialize(
                        eventId, tag,
                        sender.getWorld(),
                        sender.getX(), sender.getY(), sender.getZ()
                )
        );

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
                        List.of("SPAWN", "POSTEVENT")
                )
        );
    }

    @Override
    public String getName() {
        return "spot";
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
