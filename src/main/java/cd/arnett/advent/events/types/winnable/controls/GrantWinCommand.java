package cd.arnett.advent.events.types.winnable.controls;

import cd.arnett.advent.commands.EventTypeCattamand;
import cd.arnett.advent.events.EventBase;
import cd.arnett.advent.events.types.winnable.WinnableModule;
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
import org.bukkit.OfflinePlayer;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class GrantWinCommand extends EventTypeCattamand<WinnableModule> {

    @Override
    public int execute(CommandContext<CommandSourceStack> context, WinnableModule eventModule) throws CommandSyntaxException {
        Collection<PlayerProfile> players = ArgumentHelper.getPlayerProfilesFromArgs("players", context);

        eventModule.grantWin(players.stream().map(PlayerProfile::getId).toArray(UUID[]::new));

        ArgumentHelper.sendSenderMiniMessage(context, "<green> wins Granted"
                + (getEvent(context).requiresRegistration() ? " to Registered player(s)" : ""));
        return 0;
    }

    @Override
    public List<? extends Cattarameter> getArguments() {
        return List.of(
                new Cattarameter(
                        "players",
                        ArgumentTypes.playerProfiles(),
                        (ctx) ->
                        {
                            EventBase base = getEvent(ctx);
                            WinnableModule module = (WinnableModule) base;
                            return base.getRegisteredPlayers().stream()
                                    .filter(
                                            id -> !module.hasPlayerWon(id)
                                    ).map(
                                            id -> Bukkit.getOfflinePlayer(id).getName()
                                    ).toList();
                        },
                        null,
                        false
                )
        );
    }

    @Override
    public String getName() {
        return "grant";
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
