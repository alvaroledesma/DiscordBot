package discordbot.role;

import discordbot.db.model.OGuildMember;
import discordbot.db.table.TGuildMember;
import discordbot.guildsettings.defaults.SettingRoleTimeRanks;
import discordbot.guildsettings.defaults.SettingRoleTimeRanksPrefix;
import discordbot.handler.GuildSettings;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.utils.PermissionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created on 19-9-2016
 */
public class RoleRankings {

	private static final ArrayList<MemberShipRole> roles = new ArrayList<>();
	private static final Set<String> roleNames = new HashSet<>();
	private static final Logger LOGGER = LoggerFactory.getLogger(RoleRankings.class);

	public static void init() {
		roles.add(new MemberShipRole("Spectator", new Color(0xFF6DE1), 0));
		roles.add(new MemberShipRole("Outsider", new Color(0xD7ACFF), TimeUnit.HOURS.toMillis(1L)));
		roles.add(new MemberShipRole("Lurker", new Color(0x61D3FF), TimeUnit.HOURS.toMillis(4L)));
		roles.add(new MemberShipRole("Prospect", new Color(0x53CAFF), TimeUnit.DAYS.toMillis(1L)));
		roles.add(new MemberShipRole("Friendly", new Color(0x4AFF51), TimeUnit.DAYS.toMillis(3L)));
		roles.add(new MemberShipRole("Regular", new Color(0x3CFF39), TimeUnit.DAYS.toMillis(7L)));
		roles.add(new MemberShipRole("Honored", new Color(0xA5FF48), TimeUnit.DAYS.toMillis(14L)));
		roles.add(new MemberShipRole("Veteran", new Color(0xB5FF22), TimeUnit.DAYS.toMillis(30L)));
		roles.add(new MemberShipRole("Revered", new Color(0xDCFF2C), TimeUnit.DAYS.toMillis(60L)));
		roles.add(new MemberShipRole("Herald", new Color(0xFFD000), TimeUnit.DAYS.toMillis(90L)));
		roles.add(new MemberShipRole("Exalted", new Color(0xFF9A00), TimeUnit.DAYS.toMillis(180L)));
		for (MemberShipRole role : roles) {
			roleNames.add(role.getName().toLowerCase());
		}
	}

	/**
	 * retrieves a list of all membership roles
	 *
	 * @return list
	 */
	public static List<MemberShipRole> getAllRoles() {
		return roles;
	}

	/**
	 * Prefixes the role name based on the guild's setting
	 *
	 * @param guild the guild
	 * @param role  the role
	 * @return full name
	 */
	public static String getFullName(Guild guild, MemberShipRole role) {
		return getPrefix(guild) + " " + role.getName();
	}

	public static MemberShipRole getHighestRole(Long memberLengthInMilis) {
		for (int i = roles.size() - 1; i >= 0; i--) {
			if (roles.get(i).getMembershipTime() <= memberLengthInMilis) {
				return roles.get(i);
			}
		}
		return roles.get(0);
	}


	/**
	 * Attempts to fix create the membership roles for a guild
	 *
	 * @param guild the guild to create/modify the roles for
	 */
	public static void fixForServer(Guild guild) {
		for (int i = roles.size() - 1; i >= 0; i--) {
			fixRole(guild, roles.get(i));
		}
	}

	public static String getPrefix(Guild guild) {
		return GuildSettings.get(guild).getOrDefault(SettingRoleTimeRanksPrefix.class);
	}

	/**
	 * Fixes adds/modifies a membership role to match the settings
	 *
	 * @param guild the guild to add/modify the role for
	 * @param rank  the role to add/modify
	 */
	private static void fixRole(Guild guild, MemberShipRole rank) {
		List<Role> rolesByName = guild.getRolesByName(getFullName(guild, rank));
		Role role;
		if (rolesByName.size() > 0) {
			role = rolesByName.get(0);
		} else {
			role = guild.createRole().getRole();
		}
		if (!role.getName().equals(getFullName(guild, rank))) {
			role.getManager().setName(getFullName(guild, rank));
		}
		if (role.getColor() != rank.getColor().getRGB()) {
			role.getManager().setColor(rank.getColor());
		}
		if (!role.isGrouped()) {
			role.getManager().setGrouped(true);
		}
	}

	/**
	 * checks if a user has the manage roles permission
	 *
	 * @param guild   the guild to check
	 * @param ourUser the user to check for
	 * @return has the manage roles premission?
	 */
	public static boolean canModifyRoles(Guild guild, User ourUser) {
		return PermissionUtil.checkPermission(guild, ourUser, Permission.MANAGE_ROLES);
	}

	/**
	 * deletes the created roles
	 *
	 * @param guild   the guild to clean up
	 * @param ourUser the bot user
	 */
	public static void cleanUpRoles(Guild guild, User ourUser) {
		if (!canModifyRoles(guild, ourUser)) {
			return;
		}
		for (Role role : guild.getRoles()) {
			if (role.getName().contains(getPrefix(guild))) {
				role.getManager().delete();
			} else if (roleNames.contains(role.getName().toLowerCase())) {
				role.getManager().delete();
			}
		}
	}

	/**
	 * Attempts to fix create the membership roles for all guilds
	 *
	 * @param guilds   the guilds to fix the roles for
	 * @param instance the bot instance
	 */
	public static void fixRoles(List<Guild> guilds, JDA instance) {
		for (Guild guild : guilds) {
			if (!GuildSettings.get(guild).getOrDefault(SettingRoleTimeRanks.class).equals("true")) {
				continue;
			}
			if (canModifyRoles(guild, instance.getSelfInfo())) {
				fixForServer(guild);
			}
		}
	}

	/**
	 * Asigns the right role to a user based on the Roleranking
	 *
	 * @param guild the guild
	 * @param user  the user
	 */
	public static void assignUserRole(DiscordBot bot, Guild guild, User user) {
		List<Role> roles = guild.getRolesForUser(user);
		OGuildMember membership = TGuildMember.findBy(guild.getId(), user.getId());
		boolean hasTargetRole = false;
		String prefix = RoleRankings.getPrefix(guild);
		if (membership.joinDate == null) {
			membership.joinDate = new Timestamp(System.currentTimeMillis());
			TGuildMember.insertOrUpdate(membership);
		}
		MemberShipRole targetRole = RoleRankings.getHighestRole(System.currentTimeMillis() - membership.joinDate.getTime());
		for (Role role : roles) {
			if (role.getName().startsWith(prefix)) {
				if (role.getName().equals(RoleRankings.getFullName(guild, targetRole))) {
					hasTargetRole = true;
				} else {
					bot.out.removeRole(user, role);
				}
			}
		}
		if (!hasTargetRole) {
			List<Role> roleList = guild.getRolesByName(RoleRankings.getFullName(guild, targetRole));
			if (roleList.size() > 0) {
				bot.out.addRole(user, roleList.get(0));
			} else {
				bot.out.sendErrorToMe(new Exception("Role not found"), "guild", guild.getName(), "user", user.getUsername());
			}
		}
	}
}