package cd.arnett.advent.events.modules.winnable.controls;

import cd.arnett.advent.commands.EventTypeCattamand;
import cd.arnett.advent.events.modules.winnable.WinnableModule;
import cd.arnett.caddamands.cattamands.arguments.ArgumentHelper;
import cd.arnett.caddamands.cattamands.arguments.Cattarameter;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.Bukkit;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class RevokeWinCommand extends EventTypeCattamand<WinnableModule> {

    @Override
    public int execute(CommandContext<CommandSourceStack> context, WinnableModule eventModule) throws CommandSyntaxException {
        Collection<PlayerProfile> players = ArgumentHelper.getPlayerProfilesFromArgs("players", context);

        eventModule.grantWin(players.stream().map(PlayerProfile::getId).toArray(UUID[]::new));

        ArgumentHelper.sendSenderMiniMessage(context, "<green> Revoked wins from "
                + (getEvent(context).requiresRegistration() ? " Registered Player(s)" : "Player(s)"));

        return 0;
    }

    @Override
    public List<? extends Cattarameter> getArguments() {
        return List.of(
                new Cattarameter(
                        "players",

                        ArgumentTypes.playerProfiles(),
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
