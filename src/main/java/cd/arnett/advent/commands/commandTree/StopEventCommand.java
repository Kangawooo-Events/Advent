package cd.arnett.advent.commands.commandTree;

import cd.arnett.advent.commands.EventBaseCattamand;
import cd.arnett.advent.commands.EventModuleCattamand;
import cd.arnett.advent.events.EventBase;
import cd.arnett.advent.events.modules.openable.OpenableModule;
import cd.arnett.caddamands.cattamands.arguments.ArgumentHelper;
import cd.arnett.caddamands.cattamands.arguments.Cattarameter;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.List;

public class StopEventCommand extends EventBaseCattamand {

    public StopEventCommand(EventBase base) {
        super(base);
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(!event.isRunning())
        {
            ArgumentHelper.sendSenderMiniMessage(context, "<red>The event has already been stopped!");
            return 0;
        }

        event.stopEvent();
        ArgumentHelper.sendSenderMiniMessage(context, "<green>Stopping Event " + event.eventKey());
        return 0;
    }

    @Override
    public List<? extends Cattarameter> getArguments() {
        return List.of();
    }

    @Override
    public String getName() {
        return "stop";
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
