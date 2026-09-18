package cd.arnett.advent.commandTree.destinations;

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

public class DevViewCommand extends Cattamand {

    @Override
    public int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        boolean view = context.getArgument("devview", boolean.class);
        List<Player> targets = ArgumentHelper.getPlayersFromArgs("targets", context);

        if (view)
        {
            targets.forEach(target -> TeleportationManager.addToDevView(target.getUniqueId()));
        }
        else
        {
            targets.forEach(target -> TeleportationManager.removeFromDevView(target.getUniqueId()));
        }

        return 0;
    }

    @Override
    public List<? extends Cattarameter> getArguments() {
        return List.of(
                new Cattarameter(
                        "devview",
                        BoolArgumentType.bool()
                ),
                new Cattarameter(
                        "targets",
                        ArgumentTypes.players()
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
        return List.of();
    }
}
