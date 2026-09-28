package cd.arnett.advent.commands.commandTree;

import cd.arnett.advent.listeners.EventFreezeListener;
import cd.arnett.caddamands.cattamands.arguments.ArgumentHelper;
import cd.arnett.caddamands.cattamands.arguments.Cattarameter;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.entity.Player;

import java.util.List;

public class UnfreezePlayerCommand extends Cattamand {
    @Override
    public int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        List<Player> players = ArgumentHelper.getPlayersFromArgs("targets", context);
        players.forEach(player -> EventFreezeListener.individuallyFrozenPlayers.remove(player.getUniqueId()));
        return 0;
    }

    @Override
    public List<? extends Cattarameter> getArguments() {
        return List.of(
                new Cattarameter(
                        "targets",
                        ArgumentTypes.players()
                )
        );
    }

    @Override
    public String getName() {
        return "unfreeze";
    }

    @Override
    public String getPermission() {
        return "";
    }

    @Override
    public List<String> getAliases() {
        return List.of("thaw");
    }
}
