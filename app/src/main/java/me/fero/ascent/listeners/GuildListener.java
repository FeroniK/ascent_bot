package me.fero.ascent.listeners;

import me.fero.ascent.commands.setup.CommandManager;
import me.fero.ascent.lavaplayer.GuildMusicManager;
import me.fero.ascent.lavaplayer.PlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GuildListener extends BaseListener {

    private final CommandManager manager;
    private static final Logger LOGGER = LoggerFactory.getLogger(GuildListener.class);


    public GuildListener() {
        this.manager = new CommandManager();
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        User user = event.getAuthor();


        if(user.isBot() || event.isWebhookMessage()) {
            return;
        }

        final long guildId = event.getGuild().getIdLong();
        String prefix = this.redis.getPrefix(guildId);

        String raw = event.getMessage().getContentRaw();



        User selfUser = event.getGuild().getSelfMember().getUser();
        String mention = "<@" + selfUser.getId() + ">";

        if(raw.trim().equalsIgnoreCase(mention)) {
            event.getChannel().sendMessageEmbeds(Embeds.createBuilder(null, "My prefix is `" + prefix + "`", null, null, null).build()).queue();
            return;
        }

        if(raw.startsWith(prefix)) {
            manager.handle(event, prefix);
        }
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        TextChannel defaultChannel = event.getGuild().getDefaultChannel();
        if(defaultChannel !=null) {
            defaultChannel.sendMessageEmbeds(Embeds.introEmbed(event.getGuild().getSelfMember(), redis.getPrefix(event.getGuild().getIdLong())).build()).queue();
        }
        PlayerManager.getInstance().getMusicManager(event.getGuild());
        LOGGER.info("Joined " + event.getGuild().getName() + " guild Adding the music manager");
        this.jda.getPresence().setActivity(Activity.listening("help in " + event.getJDA().getGuilds().size() + " Guilds"));
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        this.jda.getPresence().setActivity(Activity.listening("help on " + event.getJDA().getGuilds().size() + " Guilds"));

        Member selfMember = event.getGuild().getSelfMember();
        if(selfMember.getVoiceState().inVoiceChannel()) {
            GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());

            musicManager.scheduler.isRepeating = false;
            musicManager.scheduler.queue.clear();
            musicManager.scheduler.player.stopTrack();

            // **RESET EVERYTHING**
            musicManager.scheduler.totalMembers.clear();
            musicManager.scheduler.votes.clear();
            musicManager.scheduler.votingGoingOn = false;

            AudioManager audioManager = event.getGuild().getAudioManager();
            audioManager.closeAudioConnection();
        }

        PlayerManager.getInstance().removeGuildMusicManager(event.getGuild());

        LOGGER.info("Left " + event.getGuild().getName() + " guild Deleting the music manager");
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {

        if(event.getMember().getUser() != event.getGuild().getSelfMember().getUser()) {
            VoiceChannel channelLeft = event.getChannelLeft();
            if(channelLeft == null) {
                return;
            }

            List<Member> members = channelLeft.getMembers();
            List<Member> copy = new ArrayList<>();


            boolean canClose = false;
            for(Member member : members) {
                if(member.getUser() != event.getGuild().getSelfMember().getUser()) {
                    copy.add(member);
                }
                else {
                    canClose = true;
                }
            }
            GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());

            if(canClose && !copy.isEmpty() && musicManager.scheduler.votingGoingOn){
                musicManager.scheduler.totalMembers = copy;
            }

            if(copy.isEmpty() && canClose) {
                musicManager.scheduler.isRepeating = false;
                musicManager.scheduler.queue.clear();
                musicManager.scheduler.player.stopTrack();
                musicManager.scheduler.deleteLastSongEmbed();

                // **RESET EVERYTHING**
                musicManager.scheduler.totalMembers.clear();
                musicManager.scheduler.votes.clear();
                musicManager.scheduler.votingGoingOn = false;


                AudioManager audioManager = event.getGuild().getAudioManager();
                audioManager.closeAudioConnection();
            }

        }
        else if(event.getMember().getUser() == event.getGuild().getSelfMember().getUser()){
            if(event.getChannelLeft() != null && event.getChannelJoined() != null) {
                VoiceChannel channelJoined = event.getChannelJoined();

                List<Member> members = channelJoined.getMembers();
                List<Member> copy = new ArrayList<>();


                boolean canClose = false;
                for(Member member : members) {
                    if(member.getUser() != event.getGuild().getSelfMember().getUser()) {
                        copy.add(member);
                    }
                    else {
                        canClose = true;
                    }
                }
                GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());

                // **RESET EVERYTHING**
                musicManager.scheduler.totalMembers.clear();
                musicManager.scheduler.votes.clear();
                musicManager.scheduler.votingGoingOn = false;

                if(copy.isEmpty() && canClose) {

                    musicManager.scheduler.isRepeating = false;
                    musicManager.scheduler.queue.clear();
                    musicManager.scheduler.player.stopTrack();
                    musicManager.scheduler.deleteLastSongEmbed();

                    AudioManager audioManager = event.getGuild().getAudioManager();
                    audioManager.closeAudioConnection();

                }
            }
            else if(event.getChannelLeft() != null && event.getChannelJoined() == null){

                GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());

                musicManager.scheduler.isRepeating = false;
                musicManager.scheduler.queue.clear();
                musicManager.scheduler.player.stopTrack();
                musicManager.scheduler.deleteLastSongEmbed();

                // **RESET EVERYTHING**
                musicManager.scheduler.totalMembers.clear();
                musicManager.scheduler.votes.clear();
                musicManager.scheduler.votingGoingOn = false;

            }
        }
    }

}