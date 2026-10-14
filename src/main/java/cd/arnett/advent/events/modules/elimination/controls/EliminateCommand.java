package cd.arnett.advent.events.modules.elimination.controls;

import cd.arnett.advent.commands.EventModuleCattamand;
import cd.arnett.advent.events.EventBase;
import cd.arnett.advent.events.modules.elimination.EliminationModule;
import cd.arnett.caddamands.cattamands.arguments.ArgumentHelper;
import cd.arnett.caddamands.cattamands.arguments.Cattarameter;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;

import java.util.Collection;
import java.util.List;

public class EliminateCommand extends EventModuleCattamand<EliminationModule> {

    public EliminateCommand(EventBase base) {
        super(base);
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<PlayerProfile> targets = ArgumentHelper.getPlayerProfilesFromArgs("players", context);

        targets.forEach(playerProfile -> module.eliminate(playerProfile.getId()));

        ArgumentHelper.getPlayerSender(context).sendMessage(
                MiniMessage.miniMessage().deserialize("<green> Eliminated Players")
        );

        return 0;
    }

    @Override
    public List<? extends Cattarameter> getArguments() {
        return List.of(
                new Cattarameter(
                        "players",

                        ArgumentTypes.playerProfiles(),
                        (ctx) -> module.getPlayersRemaining().stream().map(
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
