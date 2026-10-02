package cd.arnett.advent.events.controls;

import cd.arnett.advent.EventTracker;
import cd.arnett.advent.commands.EventBaseCattamand;
import cd.arnett.advent.events.EventBase;
import cd.arnett.caddamands.cattamands.arguments.ArgumentHelper;
import cd.arnett.caddamands.cattamands.arguments.Cattarameter;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class JoinEventCommand extends EventBaseCattamand {
    @Override
    public int execute(CommandContext<CommandSourceStack> context, EventBase event) throws CommandSyntaxException {
        Player sender = ArgumentHelper.getPlayerSender(context);

        if(!event.canJoin(sender))
        {
            ArgumentHelper.sendSenderMiniMessage(
                    context,
                    "<color:#ff597a> You Cannot Join This Event"
                            + event.eventKey().toString()
            );
            return 0;
        }

        event.join(sender).thenAcceptAsync(result -> {
            if(result)
                ArgumentHelper.sendSenderMiniMessage(context, "<color:#5cff98><bold>Joined</bold> Event " + event.eventKey().toString());
            else
                ArgumentHelper.sendSenderMiniMessage(context, "<color:#ff597a>You are <bold>Unable to Join</bold> Event " + event.eventKey().toString());
        });

        return 0;
    }

    public int groupExecute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        List<Player> targets = ArgumentHelper.getPlayersFromArgs("targets", context);

        AtomicInteger joinFailures = new AtomicInteger(0);

        EventBase event = getEvent(context);
        ArrayList<CompletableFuture<Boolean>> teleportationTasks = new ArrayList<>();

        for (Player target : targets) {
            var task = event.join(target);
            task.thenAcceptAsync(result -> {
                if(!result)
                {
                    joinFailures.incrementAndGet();
                }
            });
            teleportationTasks.add(task);
        }

        CompletableFuture.allOf(teleportationTasks.toArray(CompletableFuture[]::new)).thenAccept(
                (a) -> {
                    if(joinFailures.get() > 0)
                    {
                        ArgumentHelper.sendSenderMiniMessage(context, "<color:#5cff98>"
                                + "Player(s) All players <bold>Joined</bold> Event "
                                + event.eventKey().toString());
                    }
                    else
                    {
                        ArgumentHelper.sendSenderMiniMessage(context, "<color:#ff597a>"
                                + joinFailures.get()
                                + " Player(s) were able to Join Event "
                                + event.eventKey().toString());
                    }
                }
        );

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
        return "join";
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
