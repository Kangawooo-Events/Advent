package cd.arnett.advent.events.types.elimination.controls;

import cd.arnett.advent.EventManager;
import cd.arnett.advent.events.EventBase;
import cd.arnett.advent.events.types.elimination.Elimination;
import cd.arnett.caddamands.cattamands.arguments.ArgumentHelper;
import cd.arnett.caddamands.cattamands.arguments.Cattarameter;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.List;

public class InstateCommand extends Cattamand {

    @Override
    public int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        List<Player> targets = ArgumentHelper.getPlayersFromArgs("players", context);
        NamespacedKey eventName = context.getArgument("event", NamespacedKey.class);

        EventBase event = EventManager.getEvent(eventName);

        if(event == null)
        {
            ArgumentHelper.getPlayerSender(context).sendMessage(
                    MiniMessage.miniMessage().deserialize("<red> No Event of name <bold>\""
                            + eventName + "\"</bold> exists")
            );
            return 0;
        }
        if(!(event instanceof Elimination elimEvent))
        {
            ArgumentHelper.getPlayerSender(context).sendMessage(
                    MiniMessage.miniMessage().deserialize("<red> Event of name <bold>\""
                            + eventName + "\"</bold> Is not an Elimination Event")
            );
            return 0;
        }

        targets.forEach(elimEvent::instate);

        ArgumentHelper.getPlayerSender(context).sendMessage(
                MiniMessage.miniMessage().deserialize("<green> Player(s) Instated")
        );


        return 0;
    }

    @Override
    public List<? extends Cattarameter> getArguments() {
        return List.of(
                new Cattarameter(
                        "players",
                        ArgumentTypes.players()
                )
        );
    }

    @Override
    public String getName() {
        return "instate";
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
