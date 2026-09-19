package cd.arnett.advent.events.types.winnable.controls;

import cd.arnett.advent.commandTree.EventTypeCattamand;
import cd.arnett.advent.events.types.winnable.WinnableModule;
import cd.arnett.caddamands.cattamands.arguments.ArgumentHelper;
import cd.arnett.caddamands.cattamands.arguments.Cattarameter;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;

public class RevokeWinCommand extends EventTypeCattamand<WinnableModule> {

    @Override
    public int execute(CommandContext<CommandSourceStack> context, WinnableModule eventModule) throws CommandSyntaxException {
        String targetName = context.getArgument("player", String.class);
        OfflinePlayer player = Bukkit.getOfflinePlayer(targetName);

        eventModule.revokeWin(player.getUniqueId());

        ArgumentHelper.getPlayerSender(context).sendMessage(
                MiniMessage.miniMessage().deserialize("<green> Revoked " + targetName + "'s win")
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
                                .getPlayersWon().stream().map(
                                        id -> Bukkit.getOfflinePlayer(id).getName()
                                ).toList(),

                        null,
                        false
                )
        );
    }

    @Override
    public String getName() {
        return "revoke";
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
