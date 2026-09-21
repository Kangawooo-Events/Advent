package cd.arnett.advent.events.controls;

import cd.arnett.advent.EventManager;
import cd.arnett.advent.commandTree.EventBaseCattamand;
import cd.arnett.advent.commandTree.EventTypeCattamand;
import cd.arnett.advent.events.EventBase;
import cd.arnett.advent.events.types.EventType;
import cd.arnett.caddamands.cattamands.arguments.Cattarameter;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.List;

public class StartEventCommand extends EventBaseCattamand {
    @Override
    public int execute(CommandContext<CommandSourceStack> context, EventBase event) throws CommandSyntaxException {
        event.startEvent();
        return 0;
    }

    @Override
    public List<? extends Cattarameter> getArguments() {
        return List.of();
    }

    @Override
    public String getName() {
        return "start";
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
