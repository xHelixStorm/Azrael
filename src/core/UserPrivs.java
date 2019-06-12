package core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Roles;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import sql.DiscordRoles;

public class UserPrivs {
	public static boolean isUserAdmin(User user, long _guild_id) {
		for(Role r : user.getJDA().getGuildById(_guild_id).getMember(user).getRoles()) {
			Roles category = Hashes.getDiscordRole(r.getIdLong());
			if(category != null && category.getCategory_Name().length() > 0) {
				if(category.getCategory_Name().equals("Administrator")) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isUserMod(User user, long _guild_id) {
		for(Role r : user.getJDA().getGuildById(_guild_id).getMember(user).getRoles()) {
			Roles category = Hashes.getDiscordRole(r.getIdLong());
			if(category != null && category.getCategory_Name().length() > 0) {
				if(category.getCategory_Name().equals("Moderator")) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isUserBot(User user, long _guild_id) {
		for(Role r : user.getJDA().getGuildById(_guild_id).getMember(user).getRoles()) {
			Roles category = Hashes.getDiscordRole(r.getIdLong());
			if(category != null && category.getCategory_Name().length() > 0) {
				if(category.getCategory_Name().equals("Bot")) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isUserMuted(User user, long _guild_id) {
		for(Role r : user.getJDA().getGuildById(_guild_id).getMember(user).getRoles()) {
			Roles category = Hashes.getDiscordRole(r.getIdLong());
			if(category != null && category.getCategory_Name().length() > 0) {
				if(category.getCategory_Name().equals("Mute")) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isUserCommunity(User user, long _guild_id) {
		for(Role r : user.getJDA().getGuildById(_guild_id).getMember(user).getRoles()) {
			Roles category = Hashes.getDiscordRole(r.getIdLong());
			if(category != null && category.getCategory_ABV().length() > 0) {
				if(category.getCategory_Name().equals("Community")) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean comparePrivilege(Member member, int requiredLevel) {
		var highestLevel = 0;
		for(final var role : member.getRoles()) {
			var level = Hashes.getDiscordRole(role.getIdLong());
			if(level != null) {
				var currentLevel = level.getLevel();
				if(currentLevel > highestLevel) {
					highestLevel = currentLevel;
				}
			}
			else {
				if(DiscordRoles.SQLgetRole(member.getGuild().getIdLong(), role.getIdLong()) != 0) {
					DiscordRoles.SQLgetRoles(member.getGuild().getIdLong());
					level = Hashes.getDiscordRole(role.getIdLong());
					if(level != null) {
						var currentLevel = level.getLevel();
						if(currentLevel > highestLevel) {
							highestLevel = currentLevel;
						}
					}
				}
				else {
					if(DiscordRoles.SQLInsertRole(member.getGuild().getIdLong(), role.getIdLong(), 0, role.getName(), "def") > 0) {
						DiscordRoles.SQLgetRoles(member.getGuild().getIdLong());
					}
					else {
						Logger logger = LoggerFactory.getLogger(UserPrivs.class);
						logger.error("Role {} couldn't be inserted into DiscordRoles.roles table", role.getId());
					}
				}
			}
		}
		return (highestLevel >= requiredLevel);
	}
}
