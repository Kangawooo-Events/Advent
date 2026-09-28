package cd.arnett.advent.events.modules.playerState.controls;

import cd.arnett.advent.commands.EventModuleCattamand;
import cd.arnett.advent.events.EventBase;
import cd.arnett.advent.events.modules.playerState.SeparateStateModule;
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

public class ClearStatesCommand extends EventModuleCattamand<SeparateStateModule> {
    @Override
    public int execute(CommandContext<CommandSourceStack> context, SeparateStateModule eventModule) throws CommandSyntaxException {

        Collection<PlayerProfile> targets = ArgumentHelper.getPlayerProfilesFromArgs("players", context);

        targets.forEach(target -> {
            eventModule.clearPlayerState(target.getId());
        });

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
                            SeparateStateModule module = (SeparateStateModule) base;
                            return module.getPlayerStates().entrySet().stream().map(
                                    entry -> Bukkit.createProfile(entry.getKey()).getName()
                                    ).toList();
                        },
                        null,
                        false
                )
        );
    }

    @Override
    public String getName() {
        return "clear";
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
