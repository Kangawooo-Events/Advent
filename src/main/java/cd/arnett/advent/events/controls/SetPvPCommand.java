package cd.arnett.advent.events.controls;

import cd.arnett.advent.commands.EventBaseCattamand;
import cd.arnett.advent.events.EventBase;
import cd.arnett.caddamands.cattamands.arguments.ArgumentHelper;
import cd.arnett.caddamands.cattamands.arguments.Cattarameter;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.List;

public class SetPvPCommand extends EventBaseCattamand {
    @Override
    public int execute(CommandContext<CommandSourceStack> context, EventBase event) throws CommandSyntaxException {
        boolean pvp = context.getArgument("dopvp", boolean.class);
        if(pvp)
            event.allowPVP();
        else
            event.preventPVP();

        ArgumentHelper.sendSenderMiniMessage(context,
                pvp ?
                "<color:#12ff88>Allowed PVP" :
                "<color:#ff597a>Disallowed PVP");

        return 0;
    }

    @Override
    public List<? extends Cattarameter> getArguments() {
        return List.of(
                new Cattarameter(
                        "dopvp",
                        BoolArgumentType.bool()
                )
        );
    }

    @Override
    public String getName() {
        return "pvp";
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
