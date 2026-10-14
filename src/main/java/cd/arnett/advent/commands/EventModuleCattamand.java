package cd.arnett.advent.commands;

import cd.arnett.advent.EventTracker;
import cd.arnett.advent.events.EventBase;
import cd.arnett.advent.events.modules.EventModule;
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
public abstract class EventModuleCattamand<T extends EventModule> extends EventBaseCattamand {

    protected T module;

    public EventModuleCattamand(EventBase base)
    {
        super(base);
        module = (T)base;
    }
}
