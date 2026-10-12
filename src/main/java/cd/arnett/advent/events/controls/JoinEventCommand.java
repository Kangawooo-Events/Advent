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

        if(!event.isActive())
        {
            ArgumentHelper.sendSenderMiniMessage(context, "<color:#ff597a>That Event is Not Open");
        }
        if(event.requiresRegistration() && !event.isRegistered(sender.getUniqueId()))
        {
            ArgumentHelper.sendSenderMiniMessage(context, "<color:#ff597a>You are not Registered for this event");
        }

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

        EventBase event = getEvent(context);
        ArrayList<CompletableFuture<Boolean>> teleportationTasks = new ArrayList<>();

        if(!event.isActive())
        {
            ArgumentHelper.sendSenderMiniMessage(context, "<color:#ff597a>That Event is Not Open");
        }


        for (Player target : targets) {
            teleportationTasks.add(event.join(target));
        }

        var collectiveFuture = CompletableFuture.allOf(teleportationTasks.toArray(CompletableFuture[]::new));
        collectiveFuture.thenAccept(
                (a) -> {

                    AtomicInteger joinFailures = new AtomicInteger(0);
                    boolean allSuccess = teleportationTasks.stream().map(CompletableFuture::join).allMatch(
                            result -> {
                                if(result) return true;
                                else
                                {
                                    joinFailures.incrementAndGet();
                                    return false;
                                }
                            }
                    );

                    if(allSuccess)
                    {
                        ArgumentHelper.sendSenderMiniMessage(context,
                                "<color:#5cff98>All Player(s) joined Event "
                                + event.eventKey().toString());
                    }
                    else
                    {
                        ArgumentHelper.sendSenderMiniMessage(context,
                                "<color:#ff597a>"
                                        + joinFailures.get()
                                        + " Player(s) <bold>Failed Joining</bold> Event "
                                        + event.eventKey().toString()
                                        + "\nMake sure event is <bold>Open</bold>"
                                        + (
                                                event.requiresRegistration() ?
                                                        " and Players are <bold>Registered</bold>" :
                                                        ""
                                        )
                        );
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
