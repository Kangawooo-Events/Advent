package cd.arnett.advent.events.types.elimination.controls;

import cd.arnett.advent.commandTree.EventTypeCattamand;
import cd.arnett.advent.events.types.elimination.EliminationModule;
import cd.arnett.caddamands.cattamands.arguments.ArgumentHelper;
import cd.arnett.caddamands.cattamands.arguments.Cattarameter;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;

public class EliminateCommand extends EventTypeCattamand<EliminationModule> {


    @Override
    public int execute(CommandContext<CommandSourceStack> context, EliminationModule eventModule) throws CommandSyntaxException {
        String targetName = context.getArgument("player", String.class);
        OfflinePlayer player = Bukkit.getOfflinePlayer(targetName);

        eventModule.eliminate(player.getUniqueId());

        ArgumentHelper.getPlayerSender(context).sendMessage(
                MiniMessage.miniMessage().deserialize("<green> Player(s) Eliminated")
        );

        return 0;
    }

    @Override
    public List<? extends Cattarameter> getArguments() {
        return List.of(
                new Cattarameter(
                        "player",

                        StringArgumentType.string(),
                        (ctx) -> getEventModule(ctx)
                                .elim.stream().map(
                                        id -> Bukkit.getOfflinePlayer(id).getName()
                                ).toList(),

                        null,
                        false
                )
        );
    }

    @Override
    public String getName() {
        return "eliminate";
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
