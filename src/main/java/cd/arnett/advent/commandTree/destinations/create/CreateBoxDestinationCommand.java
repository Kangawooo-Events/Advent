package cd.arnett.advent.commandTree.destinations.create;

import cd.arnett.advent.EventManager;
import cd.arnett.advent.TeleportationManager;
import cd.arnett.advent.destinations.box.BoxDestination;
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

public class CreateBoxDestinationCommand extends Cattamand {
    @Override
    public int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        NamespacedKey eventId = context.getArgument("event", NamespacedKey.class);
        String tag = context.getArgument("tag", String.class);
        BlockPosition position = context.getArgument("position", BlockPosition.class);
        double xSize = context.getArgument("xSize", double.class);
        double ySize = context.getArgument("ySize", double.class);
        double zSize = context.getArgument("zSize", double.class);

        Player sender = ArgumentHelper.getPlayerSender(context);

        TeleportationManager.addDestination(
                new BoxDestination().initialize(
                        eventId, tag,
                        position.toCenter().toLocation(sender.getWorld()),
                        xSize, ySize, zSize
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
                        "xSize",
                        DoubleArgumentType.doubleArg(1)
                ),
                new Cattarameter(
                        "ySize",
                        DoubleArgumentType.doubleArg(1)
                ),
                new Cattarameter(
                        "zSize",
                        DoubleArgumentType.doubleArg(1)
                )
        );
    }

    @Override
    public String getName() {
        return "box";
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
