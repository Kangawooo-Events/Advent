package cd.arnett.advent.events.modules.relocation.controls;

import cd.arnett.advent.commands.EventModuleCattamand;
import cd.arnett.advent.events.EventBase;
import cd.arnett.advent.events.modules.relocation.RelocationModule;
import cd.arnett.caddamands.cattamands.arguments.Cattarameter;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.List;

public class ReloadRelocationCommand extends EventModuleCattamand<RelocationModule> {
    public ReloadRelocationCommand(EventBase base) {
        super(base);
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        module.getJoinTeleportTask().reloadDestinations();
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
