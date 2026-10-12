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

import java.util.ArrayList;
import java.util.List;

public class EventCommand extends Cattamand {
    @Override
    public int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return 0;
    }

    @Override
    public List<? extends Cattarameter> getArguments() {
        return List.of(
                new Cattarameter(
                        "event",
                        ArgumentTypes.namespacedKey(),
                        (ctx) -> EventTracker.getEventNames()
                )
        );
    }

    @Override
    public String getName() {
        return "event";
    }

    @Override
    public String getPermission() {
        return "";
    }

    @Override
    public List<String> getAliases() {
        return List.of("advent", "adv");
    }

    @Override
    public List<? extends Cattamand> getChildren() {
        ArrayList<Cattamand> subCommands = new ArrayList<>();

        EventTracker.getEvents().forEach(eventBase -> {
            subCommands.addAll(eventBase.getCommandControls());
        });

        return subCommands;
    }

    @Override
    public boolean doExecute() {
        return false;
    }
}
