package cd.arnett.advent.commandTree;

import cd.arnett.advent.EventManager;
import cd.arnett.advent.events.EventBase;
import cd.arnett.advent.events.types.EventType;
import cd.arnett.caddamands.cattamands.arguments.ArgumentHelper;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;

/**
 * Extension of Cattamand which provides helpful method(s) for making event base control commands
 */
public abstract class EventBaseCattamand extends EventTypeCattamand<EventType> {

    @Override
    public final int execute(CommandContext<CommandSourceStack> context, EventType eventModule) throws CommandSyntaxException {
        return execute(context, getEvent(context));
    }

    public abstract int execute(CommandContext<CommandSourceStack> context, EventBase event) throws CommandSyntaxException;
}
