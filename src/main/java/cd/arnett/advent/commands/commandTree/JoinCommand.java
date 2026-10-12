package cd.arnett.advent.commands.commandTree;

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

public class JoinCommand extends Cattamand {
    @Override
    public int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = ArgumentHelper.getPlayerSender(context);
        var event = EventTracker.getAutoJoinEvent(player);

        if(event == null)
        {
            ArgumentHelper.sendSenderMiniMessage(context, "<red>No Automatically Joinable Events Available");
            return 0;
        }
        else if (EventTracker.getPlayingEvent(player.getUniqueId()) == event)
        {
            ArgumentHelper.sendSenderMiniMessage(context, "<red>You are already playing in the highest priority event");
            return 0;
        }

        event.join(player);

        return 0;
    }

    public int specificExecute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        NamespacedKey eventKey = context.getArgument("event", NamespacedKey.class);
        var event = EventTracker.getEvent(eventKey);
        var player = ArgumentHelper.getPlayerSender(context);

        if(event == null)
        {
            ArgumentHelper.sendSenderMiniMessage(context, "<red>The event " + eventKey + " does not exist");
            return 0;
        }
        else if (EventTracker.getPlayingEvent(player.getUniqueId()) == event)
        {
            ArgumentHelper.sendSenderMiniMessage(context, "<red>You are already playing in that event");
            return 0;
        }

        event.join(player);

        return 0;
    }

    @Override
    public List<? extends Cattarameter> getArguments() {
        return List.of(
                new Cattarameter(
                        "event",
                        ArgumentTypes.namespacedKey(),
                        (ctx) ->
                                EventTracker.getEventNames(),
                        this::specificExecute,
                        true,
                        (ctx) -> ctx.getSender().isOp()
                )
        );
    }

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public String getPermission() {
        return "none";
    }

    @Override
    public List<String> getAliases() {
        return List.of();
    }
}
