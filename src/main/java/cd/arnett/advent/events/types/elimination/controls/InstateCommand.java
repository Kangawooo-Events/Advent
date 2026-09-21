package cd.arnett.advent.events.types.elimination.controls;

import cd.arnett.advent.EventManager;
import cd.arnett.advent.commandTree.EventTypeCattamand;
import cd.arnett.advent.events.EventBase;
import cd.arnett.advent.events.types.elimination.EliminationModule;
import cd.arnett.caddamands.cattamands.arguments.ArgumentHelper;
import cd.arnett.caddamands.cattamands.arguments.Cattarameter;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public class InstateCommand extends EventTypeCattamand<EliminationModule> {


    @Override
    public int execute(CommandContext<CommandSourceStack> context, EliminationModule eventModule) throws CommandSyntaxException {
        Collection<PlayerProfile> targets = ArgumentHelper.getPlayerProfilesFromArgs("players", context);

        targets.forEach(playerProfile -> eventModule.instate(playerProfile.getId()));

        ArgumentHelper.getPlayerSender(context).sendMessage(
                MiniMessage.miniMessage().deserialize("<green> Player(s) Instated")
        );

        return 0;
    }

    @Override
    public List<? extends Cattarameter> getArguments() {
        return List.of(
                new Cattarameter(
                        "players",
                        ArgumentTypes.playerProfiles(),
                        (ctx) -> getEventModule(ctx)
                                .getPlayersEliminated().stream().map(
                                        id -> Bukkit.getOfflinePlayer(id).getName()
                                ).toList(),

                        null,
                        false
                )
        );
    }

    @Override
    public String getName() {
        return "instate";
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
