package novaz.command;

import novaz.core.AbstractCommand;
import novaz.handler.GuildSettings;
import novaz.handler.TextHandler;
import novaz.handler.guildsettings.DefaultGuildSettings;
import novaz.main.Config;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.Map;

/**
 * !config
 * gets/sets the configuration of the bot
 */
public class SetConfig extends AbstractCommand {
	public SetConfig(NovaBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "Gets/sets the configuration of the bot";
	}

	@Override
	public String getCommand() {
		return "config";
	}

	@Override
	public String getUsage() {
		return "config or config <property> or config <property> <value>";
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		int count = args.length;
		if (channel.getGuild().getOwner().equals(author) || author.getID().equals(Config.CREATOR_ID)) {
			if (count == 0) {
				String ret = "```bash" + Config.EOL;
				ret += "Current Settings" + Config.EOL;
				ret += "---------------------------------------- " + Config.EOL;
				Map<String, String> settings = GuildSettings.get(channel.getGuild(), bot).getSettings();
				for (Map.Entry<String, String> entry : settings.entrySet()) {
					ret += String.format("%16s = %s", entry.getKey(), entry.getValue()) + Config.EOL;
				}
				return ret + "```";
			} else {
				if (!DefaultGuildSettings.isValidKey(args[0])) {
					return TextHandler.get("command_config_key_not_exists");
				}
				if (count >= 2) {
					GuildSettings.get(channel.getGuild(), bot).set(args[0], args[1]);
					return TextHandler.get("command_config_key_modified");
				}
				return "Current value for '" + args[0] + "' = '" + GuildSettings.get(channel.getGuild(), bot).getOrDefault(args[0]) + "'";
			}
		}
		return TextHandler.get("command_config_no_permission");
	}
}