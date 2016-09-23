package discordbot.command.economy;

import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

public class BankCommand extends AbstractCommand {
	public BankCommand(DiscordBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "For all your banking needs";
	}

	@Override
	public String getCommand() {
		return "bank";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"bank                       //shows current balance",
				"bank history               //shows last transactions",
				"bank donate @user <amount> //donates <amount> to @user ",
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public boolean isListed() {
		return false;
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {

		return Template.get("command_not_implemented");
	}
}
