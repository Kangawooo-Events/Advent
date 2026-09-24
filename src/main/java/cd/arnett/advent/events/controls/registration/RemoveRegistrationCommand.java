package cd.arnett.advent.events.controls.registration;

import cd.arnett.advent.commands.EventBaseCattamand;
import cd.arnett.advent.events.EventBase;
import cd.arnett.caddamands.cattamands.arguments.ArgumentHelper;
import cd.arnett.caddamands.cattamands.arguments.Cattarameter;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;

import java.util.Collection;
import java.util.List;

public class RemoveRegistrationCommand extends EventBaseCattamand {
    @Override
    public int execute(CommandContext<CommandSourceStack> context, EventBase event) throws CommandSyntaxException {
        Collection<PlayerProfile> targets = ArgumentHelper.getPlayerProfilesFromArgs("target", context);
        ArgumentHelper.sendSenderMiniMessage(context, "<color:#5cff98> Revoked Player(s)");
        targets.forEach(target -> event.revokeRegistration(target.getId()));

        return 0;
    }

    @Override
    public List<? extends Cattarameter> getArguments() {
        return List.of(
                new Cattarameter(
                        "target",
                        ArgumentTypes.playerProfiles()
                )
        );
    }

    @Override
    public String getName() {
        return "remove";
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
