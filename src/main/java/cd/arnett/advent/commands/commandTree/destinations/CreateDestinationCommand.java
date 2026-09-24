package cd.arnett.advent.commands.commandTree.destinations;

import cd.arnett.advent.EventManager;
import cd.arnett.advent.TeleportationManager;
import cd.arnett.caddamands.cattamands.arguments.Cattarameter;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;

import java.util.List;
import java.util.Map;

public class CreateDestinationCommand extends Cattamand {
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
                        (ctx) -> EventManager.getEventNames()
                ),
                new Cattarameter(
                        "tag",
                        StringArgumentType.string(),
                        Map.of(
                                "SPAWN", "Tag for identification, can be any word",
                                "POSTEVENT", "Tag for identification, can be any word"
                        )
                ),
                new Cattarameter(
                        "position",
                        ArgumentTypes.blockPosition()
                )
        );
    }

    @Override
    public String getName() {
        return "create";
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
    public boolean doExecute() {
        return false;
    }

    @Override
    public List<? extends Cattamand> getChildren() {
        return TeleportationManager.getDestinationCreationCommands();
    }
}
