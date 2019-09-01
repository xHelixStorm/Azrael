package core;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Roles;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.DiscordRoles;

public class UserPrivs {
	final static private Logger logger = LoggerFactory.getLogger(UserPrivs.class);
	
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
			var level = DiscordRoles.SQLgetRole(member.getGuild().getIdLong(), role.getIdLong());
			if(level != null) {
				if(level.getLevel() > highestLevel) {
					highestLevel = level.getLevel();
				}
			}
			else {
				if(DiscordRoles.SQLInsertRole(member.getGuild().getIdLong(), role.getIdLong(), 0, role.getName(), "def") == 0) {
					logger.error("The role id {} couldn't be inserted into DiscordRoles.roles table", role.getId());
				}
			}
		}
		return (highestLevel >= requiredLevel);
	}
	
	public static String retrieveRequiredRoles(int requiredLevel, Guild guild) {
		var out = "";
		for(final var role : guild.getRoles()) {
			try {
				out += (!role.getName().equals("@everyone") && Hashes.getDiscordRole(role.getIdLong()).getLevel() >= requiredLevel ? "`"+role.getName()+"` " : "");
			} catch(NullPointerException npe) {
				if(DiscordRoles.SQLInsertRole(guild.getIdLong(), role.getIdLong(), 0, role.getName(), "def") > 0) {
					Hashes.addDiscordRole(role.getIdLong(), new Roles(role.getIdLong(), role.getName(), 0, "def", "Default"));
				}
				else {
					logger.error("The role id {} couldn't be inserted into DiscordRoles.roles table", role.getId());
				}
			}
		}
		return out;
	}
	
	public static void throwNotEnoughPrivilegeError(GuildMessageReceivedEvent e, int requiredLevel) {
		e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail())
				.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(requiredLevel, e.getGuild())).build()).queue();
	}
}
