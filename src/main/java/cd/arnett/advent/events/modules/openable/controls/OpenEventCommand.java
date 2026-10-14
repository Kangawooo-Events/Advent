package cd.arnett.advent.events.modules.openable.controls;

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

public class OpenEventCommand extends EventModuleCattamand<OpenableModule> {

    public OpenEventCommand(EventBase base) {
        super(base);
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(module.isOpen())
        {
            ArgumentHelper.sendSenderMiniMessage(context, "<red>The event has already been opened!");
            return 0;
        }

        module.openEvent();
        ArgumentHelper.sendSenderMiniMessage(context, "<green>Opened Event " + event.eventKey());
        return 0;
    }

    @Override
    public List<? extends Cattarameter> getArguments() {
        return List.of();
    }

    @Override
    public String getName() {
        return "open";
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
