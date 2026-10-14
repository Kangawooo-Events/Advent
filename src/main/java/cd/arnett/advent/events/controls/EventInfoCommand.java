package cd.arnett.advent.events.controls;

import cd.arnett.advent.commands.EventBaseCattamand;
import cd.arnett.advent.events.EventBase;
import cd.arnett.advent.events.annotations.EventInfoProvider;
import cd.arnett.caddamands.cattamands.arguments.ArgumentHelper;
import cd.arnett.caddamands.cattamands.arguments.Cattarameter;
import com.google.common.collect.HashBasedTable;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.List;

public class EventInfoCommand extends EventBaseCattamand {

    public EventInfoCommand(EventBase base) {
        super(base);
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        StringBuilder output = new StringBuilder("\n<bold><color:#ff597a>")
                .append(event.eventKey().toString()).append(" Info: </color></bold>");

        HashBasedTable<EventInfoProvider, String, Object> info = event.getEventInfo();

        info.rowMap().forEach((section, row) -> {

            if(section.separate())
                return;

            output.append("\n<bold><color:#12ff88>").append(section.name()).append(":<reset>");

            row.forEach((key, object) -> {
                output.append("\n    <reset><yellow>").append(key).append(":</yellow> ").append(object);
            });
        });

        ArgumentHelper.sendSenderMiniMessage(context, output.toString());

        return 0;
    }

    public int executeWithSection(CommandContext<CommandSourceStack> context, String sectionKey) throws CommandSyntaxException {
        StringBuilder output = new StringBuilder("<bold><color:#ff597a>")
                .append(event.eventKey().toString()).append("</color></bold>");

        HashBasedTable<EventInfoProvider, String, Object> info = event.getEventInfo();

        info.rowMap().forEach((section, row) -> {
            if(!section.name().equalsIgnoreCase(sectionKey))
                return;

            output.append(" <bold><color:#12ff88>").append(section.name()).append(":<reset>");

            row.forEach((key, object) -> {
                output.append("\n    <reset><yellow>").append(key).append(":</yellow> ").append(object);
            });
        });

        ArgumentHelper.sendSenderMiniMessage(context, output.toString());

        return 0;
    }

    @Override
    public List<? extends Cattarameter> getArguments() {
        return List.of(
                new Cattarameter(
                        "section",
                        StringArgumentType.string(),
                        (ctx -> event.getEventInfo()
                                .rowKeySet().stream().map(ann -> ann.name().toLowerCase()).toList()),
                        (ctx) ->
                                executeWithSection(ctx, ctx.getArgument("section", String.class))
                ));
    }

    @Override
    public String getName() {
        return "info";
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
