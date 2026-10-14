package cd.arnett.advent.commands.commandTree;

import cd.arnett.advent.DestinationManager;
import cd.arnett.advent.EventTracker;
import cd.arnett.caddamands.cattamands.arguments.ArgumentHelper;
import cd.arnett.caddamands.cattamands.arguments.Cattarameter;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.NamespacedKey;

import java.util.List;

public class CheckEventCommand extends Cattamand {

    @Override
    public int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        System.out.println(ArgumentHelper.getPlayerSender(context).getUniqueId());
        var event = EventTracker.getPlayingEvent(ArgumentHelper.getPlayerSender(context).getUniqueId());

        if (event == null)
        {
            ArgumentHelper.sendSenderMiniMessage(context, "<bold><red>Event : NONE</bold> ");
            return 0;
        }
        else
        {
            ArgumentHelper.sendSenderMiniMessage(context, "<bold>Event :</bold> " + event.eventKey());
            return 0;
        }
    }

    public int altexecute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = ArgumentHelper.getPlayerFromArgs("player", context);
        var event = EventTracker.getPlayingEvent(player.getUniqueId());

        if (event == null)
        {
            ArgumentHelper.sendSenderMiniMessage(context, "<bold><red>Event : NONE</bold> ");
            return 0;
        }
        else
        {
            ArgumentHelper.sendSenderMiniMessage(context, "<bold>Event :</bold> " + event.eventKey());
            return 0;
        }
    }

    @Override
    public List<? extends Cattarameter> getArguments() {
        return List.of(
                new Cattarameter(
                        "player",
                        ArgumentTypes.player(),
                        null,
                        this::altexecute
                )
        );
    }

    @Override
    public String getName() {
        return "checkevent";
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
