package cd.arnett.advent.events.controls;

import cd.arnett.advent.commands.EventBaseCattamand;
import cd.arnett.advent.events.EventBase;
import cd.arnett.caddamands.cattamands.arguments.ArgumentHelper;
import cd.arnett.caddamands.cattamands.arguments.Cattarameter;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.List;

public class FreezeEventCommand extends EventBaseCattamand {

    @Override
    public List<? extends Cattarameter> getArguments() {
        return List.of();
    }

    @Override
    public String getName() {
        return "freeze";
    }

    @Override
    public String getPermission() {
        return "";
    }

    @Override
    public List<String> getAliases() {
        return List.of();
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> context, EventBase event) throws CommandSyntaxException {
        event.freeze();

        ArgumentHelper.sendSenderMiniMessage(context,
                        "<bold><shadow:#17dff><color:#78d4ff>Event Frozen");
        return 0;
    }
}
