package cd.arnett.advent.commandTree.destinations.create;

import cd.arnett.advent.EventManager;
import cd.arnett.advent.TeleportationManager;
import cd.arnett.advent.destinations.circle.CircleDestination;
import cd.arnett.caddamands.cattamands.arguments.ArgumentHelper;
import cd.arnett.caddamands.cattamands.arguments.Cattarameter;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.math.BlockPosition;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.List;

public class CreateCircleDestinationCommand extends Cattamand {

    @Override
    public int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        NamespacedKey eventId = context.getArgument("event", NamespacedKey.class);
        String tag = context.getArgument("tag", String.class);
        double radius = context.getArgument("radius", double.class);
        BlockPosition position = context.getArgument("position", BlockPosition.class);

        Player sender = ArgumentHelper.getPlayerSender(context);

        TeleportationManager.addDestination(
                new CircleDestination().initialize(
                        eventId, tag,
                        position.toCenter().toLocation(sender.getWorld()),
                        radius, true
                )
        );

        return 0;
    }

    public int fillExecute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        NamespacedKey eventId = context.getArgument("event", NamespacedKey.class);
        String tag = context.getArgument("tag", String.class);
        double radius = context.getArgument("radius", double.class);
        boolean fill = context.getArgument("fill", String.class).equals("full");
        BlockPosition position = context.getArgument("position", BlockPosition.class);

        Player sender = ArgumentHelper.getPlayerSender(context);

        TeleportationManager.addDestination(
                new CircleDestination().initialize(
                        eventId, tag,
                        position.toCenter().toLocation(sender.getWorld()),
                        radius, fill
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
                ),
                new Cattarameter(
                        "position",
                        ArgumentTypes.blockPosition()
                ),
                new Cattarameter(
                        "radius",
                        DoubleArgumentType.doubleArg(.5d)
                ),
                new Cattarameter(
                        "fill",
                        StringArgumentType.string(),
                        _ -> List.of("full", "hollow"),
                        this::fillExecute
                )
        );
    }

    @Override
    public String getName() {
        return "circle";
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
