package cd.arnett.advent.events.controls;

import cd.arnett.advent.commands.EventBaseCattamand;
import cd.arnett.advent.events.EventBase;
import cd.arnett.caddamands.cattamands.arguments.ArgumentHelper;
import cd.arnett.caddamands.cattamands.arguments.Cattarameter;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.entity.Player;

import java.util.List;

public class JoinEventCommand extends EventBaseCattamand {
    @Override
    public int execute(CommandContext<CommandSourceStack> context, EventBase event) throws CommandSyntaxException {
        Player sender = ArgumentHelper.getPlayerSender(context);

        if(event.requiresRegistration() && !event.isRegistered(sender))
        {
            ArgumentHelper.sendSenderMiniMessage(
                    context,
                    "<color:#ff597a> You are not <bold>Registered</bold> for this Event"
                            + event.eventKey().toString()
            );
            return 0;
        }

        event.join(sender);
        ArgumentHelper.sendSenderMiniMessage(context, "<color:#5cff98><bold>Joined</bold> Event " + event.eventKey().toString());

        return 0;
    }

    public int groupExecute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        List<Player> targets = ArgumentHelper.getPlayersFromArgs("targets", context);
        EventBase base = getEvent(context);
        targets.forEach(base::join);
        ArgumentHelper.sendSenderMiniMessage(context, "<color:#5cff98>"
                + (getEvent(context).requiresRegistration() ? "Registered Player(s)" : "Player(s)")
                +" <bold>Joined</bold> Event "
                + base.eventKey().toString());


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
