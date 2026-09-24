package cd.arnett.advent.events.controls;

import cd.arnett.advent.Advent;
import cd.arnett.advent.commands.EventBaseCattamand;
import cd.arnett.advent.events.EventBase;
import cd.arnett.advent.events.PlayerEventStatus;
import cd.arnett.caddamands.cattamands.arguments.ArgumentHelper;
import cd.arnett.caddamands.cattamands.arguments.Cattarameter;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class LeaveEventCommand extends EventBaseCattamand {

    @Override
    public int execute(CommandContext<CommandSourceStack> context, EventBase event) throws CommandSyntaxException {
        event.leave(ArgumentHelper.getPlayerSender(context));
        ArgumentHelper.sendSenderMiniMessage(context, "<color:#5cff98><bold>Left</bold> Event " + event.eventKey().toString());
        return 0;
    }

    public int groupExecute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        List<Player> targets = ArgumentHelper.getPlayersFromArgs("targets", context);
        EventBase base = getEvent(context);
        targets.forEach(base::leave);
        ArgumentHelper.sendSenderMiniMessage(context, "<color:#5cff98>Player(s) <bold>Left</bold> Event " + base.eventKey().toString());

        return 0;
    }

    @Override
    public List<? extends Cattarameter> getArguments() {
        return List.of(
                new Cattarameter(
                        "targets",
                        ArgumentTypes.players(),
                        null,
                        this::groupExecute
                )
        );
    }

    @Override
    public String getName() {
        return "leave";
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
