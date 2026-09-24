package cd.arnett.advent.commands.commandTree.destinations;

import cd.arnett.advent.TeleportationManager;
import cd.arnett.caddamands.cattamands.arguments.ArgumentHelper;
import cd.arnett.caddamands.cattamands.arguments.Cattarameter;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.entity.Player;

import java.util.List;

public class DestinationViewCommand extends Cattamand {


    @Override
    public int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        TeleportationManager.toggleDevView(ArgumentHelper.getPlayerSender(context).getUniqueId());
        return 0;
    }

    public int executeWithTargets(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        boolean view = context.getArgument("devview", boolean.class);
        List<Player> targets = ArgumentHelper.getPlayersFromArgs("targets", context);
        targets.forEach(target -> setPlayerDevView(target, view));
        return 0;
    }

    void setPlayerDevView(Player target, boolean view)
    {
        if (view)
        {
            TeleportationManager.addToDevView(target.getUniqueId());
        }
        else
        {
            TeleportationManager.removeFromDevView(target.getUniqueId());
        }
    }

    @Override
    public List<? extends Cattarameter> getArguments() {
        return List.of(
                new Cattarameter(
                        "devview",
                        BoolArgumentType.bool(),
                        null,
                        (ctx) -> {
                            boolean view = ctx.getArgument("devview", boolean.class);
                            setPlayerDevView(ArgumentHelper.getPlayerSender(ctx), view);
                            return 0;
                        }
                ),
                new Cattarameter(
                        "targets",
                        ArgumentTypes.players(),
                        null,
                        this::executeWithTargets
                )
        );
    }

    @Override
    public String getName() {
        return "view";
    }

    @Override
    public String getPermission() {
        return "";
    }

    @Override
    public List<String> getAliases() {
        return List.of("");
    }
}
