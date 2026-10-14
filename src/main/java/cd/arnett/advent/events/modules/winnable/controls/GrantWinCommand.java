package cd.arnett.advent.events.modules.winnable.controls;

import cd.arnett.advent.commands.EventModuleCattamand;
import cd.arnett.advent.events.EventBase;
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

public class GrantWinCommand extends EventModuleCattamand<WinnableModule> {

    public GrantWinCommand(EventBase base) {
        super(base);
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<PlayerProfile> players = ArgumentHelper.getPlayerProfilesFromArgs("players", context);

        module.grantWin(players.stream().map(PlayerProfile::getId).toArray(UUID[]::new));

        ArgumentHelper.sendSenderMiniMessage(context, "<green> wins Granted");
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
                            WinnableModule module = (WinnableModule) event;
                            return event.getEventPlayers().stream()
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
