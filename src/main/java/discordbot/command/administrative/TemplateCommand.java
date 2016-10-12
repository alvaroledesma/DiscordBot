package discordbot.command.administrative;

import com.vdurmont.emoji.EmojiParser;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.util.Misc;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

/**
 * !template
 * manages the templates
 */
public class TemplateCommand extends AbstractCommand {
	public TemplateCommand(DiscordBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "adds/removes templates";
	}

	@Override
	public String getCommand() {
		return "template";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"template list                        //lists all keyphrases",
				"template list <keyphrase> <page>     //lists all options for keyphrase",
				"template remove <keyphrase> <index>  //removes selected template for keyphrase",
				"template add <keyphrase> <text...>   //adds a template for keyphrase",
				"template toggledebug                 //shows keyphrases instead of text"};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"tpl"
		};
	}

	@Override
	public String execute(String[] args, TextChannel channel, User author) {
		if (bot.isOwner(channel, author)) {
			if (args.length == 0) {
				String usage = ":gear: **Options**:```php" + Config.EOL;
				for (String line : getUsage()) {
					usage += line + Config.EOL;
				}
				return usage + "```";
			}
			switch (args[0]) {
				case "toggledebug":
					if (bot.isCreator(author)) {
						Config.SHOW_KEYPHRASE = !Config.SHOW_KEYPHRASE;
						if (Config.SHOW_KEYPHRASE) {
							return "Keyphrases shown ";
						} else {
							return "Keyphrases are being translated";
						}
					}
					return discordbot.handler.Template.get("no_permission");
				case "add":
					if (args.length >= 3) {
						String text = args[2];
						for (int i = 3; i < args.length; i++) {
							text += " " + args[i];
						}
						discordbot.handler.Template.getInstance().add(args[1], EmojiParser.parseToAliases(text));
						return discordbot.handler.Template.get("command_template_added");
					}
					return discordbot.handler.Template.get("command_template_added_failed");
				case "remove":
				case "list":
					int currentPage = 0;
					int itemsPerPage = 25;
					int maxPage = 1 + Template.uniquePhraseCount() / itemsPerPage;
					if (args.length >= 2) {
						currentPage = Math.min(Math.max(0, Integer.parseInt(args[1]) - 1), maxPage - 1);
					}
					return String.format("All keyphrases: [page %s/%s]", currentPage + 1, maxPage) + Config.EOL +
							Misc.makeTable(Template.getAllKeyphrases(itemsPerPage, currentPage * itemsPerPage), 45, 2);
				default:
					return discordbot.handler.Template.get("command_template_invalid_option");
			}
		}
		return discordbot.handler.Template.get("no_permission");
	}
}