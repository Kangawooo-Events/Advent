package cd.arnett.advent.commands;

import cd.arnett.advent.EventTracker;
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
 * Extension of Cattamand which provides helpful method(s) for making event module control commands
 */
public abstract class EventTypeCattamand<T extends EventType> extends Cattamand {

    public EventBase getEvent(CommandContext<CommandSourceStack> context)
    {
        return EventTracker.getEvent(context.getLastChild().getArgument("event", NamespacedKey.class));
    }

    public T getEventModule(CommandContext<CommandSourceStack> context)
    {
        return (T) EventTracker.getEvent(context.getLastChild().getArgument("event", NamespacedKey.class));
    }

    @Override
    public final int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        NamespacedKey eventName = context.getArgument("event", NamespacedKey.class);

        EventBase event = EventTracker.getEvent(eventName);

        if(event == null)
        {
            ArgumentHelper.getPlayerSender(context).sendMessage(
                    MiniMessage.miniMessage().deserialize("<red> No Event of name <bold>\""
                            + eventName + "\"</bold> exists")
            );
            return 0;
        }
        try {
            return execute(context, (T)event);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            ArgumentHelper.getPlayerSender(context).sendMessage(
                    MiniMessage.miniMessage().deserialize("<red> Event of name <bold>\""
                            + eventName + "\"</bold> Could not run " + getName() + " command for this event")
            );
            return 0;
        }
    }

    public abstract int execute(CommandContext<CommandSourceStack> context, T eventModule) throws CommandSyntaxException;
}
