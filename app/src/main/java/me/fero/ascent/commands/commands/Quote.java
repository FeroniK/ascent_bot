package me.fero.ascent.commands.commands;

import me.duncte123.botcommons.web.WebUtils;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.entities.TextChannel;

public class Quote implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();

        WebUtils.ins.getJSONObject("https://api.kanye.rest/").async((json) -> {
                if(!json.has("quote")) {
                    channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Cannot send a quote right now!", null, null, null).build()).queue();
                    return;
                }

                channel.sendMessageEmbeds(Embeds.createBuilder("Quote", json.get("quote").asText(), null, null, null).build()).queue();
            }, (e) -> {
                channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Cannot send a quote right now!", null, null, null).build()).queue();

            });

    }

    @Override
    public String getName() {
        return "quote";
    }

    @Override
    public String getHelp() {
        return "Sends a random quote";
    }

    @Override
    public int cooldownInSeconds() {
        return 10;
    }
}