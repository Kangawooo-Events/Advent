package cd.arnett.advent.events.modules.playerState.controls.def;

import cd.arnett.advent.commands.EventModuleCattamand;
import cd.arnett.advent.events.modules.playerState.SeparateStateModule;
import cd.arnett.caddamands.cattamands.arguments.ArgumentHelper;
import cd.arnett.caddamands.cattamands.arguments.Cattarameter;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.List;

public class resetDefaultStateCommand extends EventModuleCattamand<SeparateStateModule> {

    @Override
    public List<? extends Cattarameter> getArguments() {
        return List.of();
    }

    @Override
    public String getName() {
        return "reset";
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
    public int execute(CommandContext<CommandSourceStack> context, SeparateStateModule eventModule) throws CommandSyntaxException {
        var player = ArgumentHelper.getPlayerSender(context);
        eventModule.setModuleData(SeparateStateModule.defaultPlayerStateKey, eventModule.defaultJoinState());
        return 0;
    }
}