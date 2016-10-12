package discordbot.command.music;

import com.google.common.base.Joiner;
import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.db.model.OMusic;
import discordbot.db.table.TMusic;
import discordbot.handler.MusicPlayerHandler;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.util.YTSearch;
import discordbot.util.YTUtil;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import sx.blah.discord.handle.obj.*;

import java.io.File;

/**
 * !play
 * plays a youtube link
 * yea.. play is probably not a good name at the moment
 */
public class Play extends AbstractCommand {
	YTSearch ytSearch;

	public Play(DiscordBot b) {
		super(b);
		ytSearch = new YTSearch(Config.GOOGLE_API_KEY);
	}

	@Override
	public String getDescription() {
		return "Plays a song from youtube";
	}

	@Override
	public String getCommand() {
		return "play";
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"play <youtubelink>    //download and plays song",
				"play <part of title>  //shows search results",
				"play                  //just start playing something"
		};
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	private boolean isInVoiceWith(IGuild guild, IUser author) {
		for (IVoiceChannel voice : bot.client.getConnectedVoiceChannels()) {
			if (voice.getGuild().equals(guild)) {
				for (IUser user : voice.getConnectedUsers()) {
					if (user.equals(author)) {
						return true;
					}
				}
				return false;
			}
		}
		return false;
	}

	@Override
	public String execute(String[] args, TextChannel channel, User author) {
		if (!isInVoiceWith(channel.getGuild(), author)) {
			String joinOutput = bot.commands.getCommand("join").execute(new String[]{}, channel, author);
			try {
				Thread.sleep(500L);// ¯\_(ツ)_/¯
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (!isInVoiceWith(channel.getGuild(), author)) {
				return joinOutput;
			}
		}
		if (MusicPlayerHandler.getFor(channel.getGuild(), bot).getUsersInVoiceChannel().size() == 0) {
			return Template.get("music_no_users_in_channel");
		}
		if (args.length > 0) {
			boolean justDownloaded = false;

			String videocode = YTUtil.extractCodeFromUrl(args[0]);
			if (!YTUtil.isValidYoutubeCode(videocode)) {
				videocode = ytSearch.getResults(Joiner.on(" ").join(args));
			}
			if (YTUtil.isValidYoutubeCode(videocode)) {

				final File filecheck = new File(YTUtil.getOutputPath(videocode));
				if (!filecheck.exists()) {
					String finalVideocode = videocode;
					bot.out.sendAsyncMessage(channel, Template.get("music_downloading_hang_on"), message -> {
						if (YTUtil.downloadfromYoutubeAsMp3(finalVideocode)) {
							message.updateMessageAsync(Template.get("music_resampling"), null);
							YTUtil.resampleToWav(finalVideocode);
						}
						if (filecheck.exists()) {
							OMusic rec = TMusic.findByYoutubeId(finalVideocode);
							rec.youtubeTitle = YTUtil.getTitleFromPage(finalVideocode);
							rec.youtubecode = finalVideocode;
							rec.filename = filecheck.getAbsolutePath();
							TMusic.update(rec);
							bot.addSongToQueue(filecheck.getAbsolutePath(), channel.getGuild());
							message.updateMessageAsync(":notes: Found *" + rec.youtubeTitle + "* And added it to the queue", null);
						} else {
							message.deleteMessage();
						}
					});
				} else if (filecheck.exists()) {
					bot.addSongToQueue(filecheck.getAbsolutePath(), channel.getGuild());
					return Template.get("music_added_to_queue");
				}
			} else {

				return Template.get("command_play_no_results");

			}
		} else {
			if (bot.playRandomSong(channel.getGuild())) {
				return Template.get("music_started_playing_random");
			} else {
				return Template.get("music_failed_to_start");
			}
		}
		return Template.get("music_not_added_to_queue");
	}
}