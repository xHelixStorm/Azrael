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
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.DiscordRoles;

public class UserPrivs {
	final static private Logger logger = LoggerFactory.getLogger(UserPrivs.class);
	
	public static boolean isUserAdmin(Member member) {
		if(member != null) {
			for(Role r : member.getRoles()) {
				Roles category = DiscordRoles.SQLgetRoles(member.getGuild().getIdLong()).parallelStream().filter(f -> f.getRole_ID() == r.getIdLong()).findAny().orElse(null);
				if(category != null && category.getCategory_Name().length() > 0) {
					if(category.getCategory_Name().equals("Administrator")) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static boolean isUserMod(Member member) {
		if(member != null) {
			for(Role r : member.getRoles()) {
				Roles category = DiscordRoles.SQLgetRoles(member.getGuild().getIdLong()).parallelStream().filter(f -> f.getRole_ID() == r.getIdLong()).findAny().orElse(null);
				if(category != null && category.getCategory_Name().length() > 0) {
					if(category.getCategory_Name().equals("Moderator")) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static boolean isUserBot(Member member) {
		if(member != null) {
			for(Role r : member.getRoles()) {
				Roles category = DiscordRoles.SQLgetRoles(member.getGuild().getIdLong()).parallelStream().filter(f -> f.getRole_ID() == r.getIdLong()).findAny().orElse(null);
				if(category != null && category.getCategory_Name().length() > 0) {
					if(category.getCategory_Name().equals("Bot")) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static boolean isUserMuted(Member member) {
		if(member != null) {
			for(Role r : member.getRoles()) {
				Roles category = DiscordRoles.SQLgetRoles(member.getGuild().getIdLong()).parallelStream().filter(f -> f.getRole_ID() == r.getIdLong()).findAny().orElse(null);
				if(category != null && category.getCategory_Name().length() > 0) {
					if(category.getCategory_Name().equals("Mute")) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static boolean isUserCommunity(Member member) {
		if(member != null) {
			for(Role r : member.getRoles()) {
				Roles category = DiscordRoles.SQLgetRoles(member.getGuild().getIdLong()).parallelStream().filter(f -> f.getRole_ID() == r.getIdLong()).findAny().orElse(null);
				if(category != null && category.getCategory_ABV().length() > 0) {
					if(category.getCategory_Name().equals("Community")) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static boolean comparePrivilege(Member member, int requiredLevel) {
		var highestLevel = 0;
		if(member != null) {
			for(final var role : member.getRoles()) {
				var level = DiscordRoles.SQLgetRoles(member.getGuild().getIdLong()).parallelStream().filter(f -> f.getRole_ID() == role.getIdLong()).findAny().orElse(null);
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
		}
		return (highestLevel >= requiredLevel);
	}
	
	public static String retrieveRequiredRoles(int requiredLevel, Guild guild) {
		StringBuilder out = new StringBuilder();
		for(final var role : guild.getRoles()) {
			var currentRole = DiscordRoles.SQLgetRoles(guild.getIdLong()).parallelStream().filter(f -> f.getRole_ID() == role.getIdLong()).findAny().orElse(null);
			if(currentRole != null) {
				out.append((!role.getName().equals("@everyone") && currentRole.getLevel() >= requiredLevel ? "`"+role.getName()+"` " : ""));
			}
			else {
				if(DiscordRoles.SQLInsertRole(guild.getIdLong(), role.getIdLong(), 0, role.getName(), "def") > 0) {
					Hashes.removeDiscordRoles(guild.getIdLong());
				}
				else {
					logger.error("The role id {} couldn't be inserted into DiscordRoles.roles table", role.getId());
				}
			}
		}
		return (out.length() > 0 ? out.toString() : "No available role!");
	}
	
	public static void throwNotEnoughPrivilegeError(GuildMessageReceivedEvent e, int requiredLevel) {
		e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail())
				.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(requiredLevel, e.getGuild())).build()).queue();
	}
}
