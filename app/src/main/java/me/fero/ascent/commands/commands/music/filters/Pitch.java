package me.fero.ascent.commands.commands.music.filters;

import me.fero.ascent.audio.GuildMusicManager;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.lavalink.LavalinkPlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class Pitch extends BaseFilter {

    public Pitch() {
        this.name = "pitch";
        this.help = "Sets the pitch of the track";
        this.usage = "pitch <0-100>";
        this.maxAmount = 200;
    }

    @Override
    public void execute(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        Guild guild = ctx.getGuild();

        List<String> args = ctx.getArgs();

        int amount = Integer.parseInt(args.get(0));

        GuildMusicManager musicManager = LavalinkPlayerManager.getInstance().getMusicManager(guild);

        if(musicManager.player.getPlayingTrack() == null) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "There is no track currently playing", null, null, null).build()).queue();
            return;
        }

        musicManager.getScheduler().setPitch(amount / 100F);

        channel.sendMessageEmbeds(Embeds.createBuilder("Success!", "Changed pitch of the current track",
                "NOTE - This may take a while", null, null).build()).queue();
    }
}
