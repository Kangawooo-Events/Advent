package cd.arnett.advent.destinations.box.command;

import cd.arnett.advent.EventManager;
import cd.arnett.advent.TeleportationManager;
import cd.arnett.advent.destinations.TeleportDestination;
import cd.arnett.advent.destinations.box.BoxDestination;
import cd.arnett.caddamands.cattamands.arguments.ArgumentHelper;
import cd.arnett.caddamands.cattamands.arguments.Cattarameter;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import io.papermc.paper.math.BlockPosition;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class CreateBoxDestinationCommand extends Cattamand {


    public TeleportDestination createDestination(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        NamespacedKey eventId = context.getArgument("event", NamespacedKey.class);
        String tag = context.getArgument("tag", String.class);
        BlockPosition position = context.getArgument("position", BlockPositionResolver.class).resolve(context.getSource());

        double xSize = context.getArgument("xSize", double.class);
        double ySize = context.getArgument("ySize", double.class);
        double zSize = context.getArgument("zSize", double.class);

        Player sender = ArgumentHelper.getPlayerSender(context);

        return new BoxDestination().initialize(
                        eventId, tag,
                        position.toCenter().toLocation(sender.getWorld()),
                        xSize, ySize, zSize
                );
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        TeleportationManager.addDestination(createDestination(context));
        return 0;
    }


    public TeleportDestination createWithColorCheck(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        Color color = Color.fromRGB(context.getArgument("color", NamedTextColor.class).value());
        return createDestination(context).setColor(color);
    }

    public TeleportDestination createWithCapacityCheck(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        int capacity = context.getArgument("capacity", Integer.class);
        return createWithColorCheck(context).setCapacity(capacity);
    }



    @Override
    public List<? extends Cattarameter> getArguments() {
        return List.of(
                new Cattarameter(
                        "xSize",
                        DoubleArgumentType.doubleArg(.5d),
                        Map.of(
                                ".5", "x length",
                                "1", "x length"
                        )
                ),
                new Cattarameter(
                        "ySize",
                        DoubleArgumentType.doubleArg(.5d),
                        Map.of(
                                ".5", "y height",
                                "1", "y height"
                        )
                ),
                new Cattarameter(
                        "zSize",
                        DoubleArgumentType.doubleArg(.5d),
                        Map.of(
                                ".5", "z width",
                                "1", "z width"
                        )
                ),
                new Cattarameter(
                        "color",
                        ArgumentTypes.namedColor(),
                        null,
                        (ctx) -> {
                            TeleportationManager.addDestination(createWithColorCheck(ctx));
                            return 0;
                        }
                ),
                new Cattarameter(
                        "capacity",
                        IntegerArgumentType.integer(),
                        null,
                        (ctx) -> {
                            TeleportationManager.addDestination(createWithCapacityCheck(ctx));
                            return 0;
                        }
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
