package cd.arnett.advent.events.types.relocation.controls;

import cd.arnett.advent.commands.EventTypeCattamand;
import cd.arnett.advent.events.types.relocation.RelocationModule;
import cd.arnett.caddamands.cattamands.arguments.ArgumentHelper;
import cd.arnett.caddamands.cattamands.arguments.Cattarameter;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.List;

public class ReloadRelocationCommand extends EventTypeCattamand<RelocationModule> {
    @Override
    public int execute(CommandContext<CommandSourceStack> context, RelocationModule eventModule) throws CommandSyntaxException {
        eventModule.getJoinTeleportTask().reloadDestinations();
        return 0;
    }

    @Override
    public List<? extends Cattarameter> getArguments() {
        return List.of();
    }

    @Override
    public String getName() {
        return "reload";
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
