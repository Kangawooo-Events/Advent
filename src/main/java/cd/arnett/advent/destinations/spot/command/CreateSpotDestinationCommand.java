package cd.arnett.advent.destinations.spot.command;

import cd.arnett.advent.TeleportationManager;
import cd.arnett.advent.destinations.TeleportDestination;
import cd.arnett.advent.destinations.spot.SpotDestination;
import cd.arnett.caddamands.cattamands.arguments.ArgumentHelper;
import cd.arnett.caddamands.cattamands.arguments.Cattarameter;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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

public class CreateSpotDestinationCommand extends Cattamand {


    public TeleportDestination createDestination(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        NamespacedKey eventId = context.getArgument("event", NamespacedKey.class);
        String tag = context.getArgument("tag", String.class);
        BlockPosition position = context.getArgument("position", BlockPositionResolver.class).resolve(context.getSource());

        Player sender = ArgumentHelper.getPlayerSender(context);

        return new SpotDestination().initialize(
                eventId, tag, position.toCenter().toLocation(sender.getWorld())
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
