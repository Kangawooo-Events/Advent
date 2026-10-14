package cd.arnett.advent.commands;

import cd.arnett.advent.EventTracker;
import cd.arnett.advent.events.EventBase;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.NamespacedKey;

/**
 * Extension of Cattamand which provides helpful method(s) for making event base control commands
 */
public abstract class EventBaseCattamand extends Cattamand {

    protected EventBase event;

    public  EventBaseCattamand(EventBase base)
    {
        this.event = base;
    }
}
