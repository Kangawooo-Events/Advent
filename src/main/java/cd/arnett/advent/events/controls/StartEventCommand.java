package cd.arnett.advent.events.controls;

import cd.arnett.advent.commandTree.EventTypeCattamand;
import cd.arnett.advent.events.types.EventType;
import cd.arnett.caddamands.cattamands.arguments.Cattarameter;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.List;

public class StartEventCommand extends EventTypeCattamand {
    @Override
    public int execute(CommandContext context, EventType eventModule) throws CommandSyntaxException {
        return 0;
    }

    @Override
    public List<? extends Cattarameter> getArguments() {
        return List.of();
    }

    @Override
    public String getName() {
        return "";
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
