package discordbot.command.informative;

import discordbot.core.AbstractCommand;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.main.Launcher;
import discordbot.util.UpdateUtil;
import net.dv8tion.jda.entities.TextChannel;


/**
 * !version
 * some general information about the bot
 */
public class VersionCommand extends AbstractCommand {

	public VersionCommand(DiscordBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "Shows what versions I'm using";
	}

	@Override
	public String getCommand() {
		return "version";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"version  //version usage"
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"v"
		};
	}

	@Override
	public String execute(String[] args, TextChannel channel, net.dv8tion.jda.entities.User author) {
		return "Info about the versions:" + Config.EOL +
				"Current version: `" + Launcher.getVersion() + "`" + Config.EOL +
				"Latest  version: `" + UpdateUtil.getLatestVersion() + "`" + Config.EOL;
	}
}