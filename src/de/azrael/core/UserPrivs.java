package de.azrael.core;

import java.awt.Color;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Roles;
import de.azrael.enums.Translation;
import de.azrael.sql.BotConfiguration;
import de.azrael.sql.DiscordRoles;
import de.azrael.sql.RankingSystem;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

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
					if(DiscordRoles.SQLInsertRole(member.getGuild().getIdLong(), role.getIdLong(), 0, role.getName(), "def", false) == 0) {
						logger.error("The role id {} couldn't be saved as new role in guild {}", role.getId(), member.getGuild().getId());
					}
				}
			}
		}
		return (highestLevel >= requiredLevel);
	}
	
	/**
	 * Retrieve all roles which are able to use the used command
	 * @param requiredLevel required command level to use the command
	 * @param guild all guild information
	 * @return list of roles
	 */
	
	public static String retrieveRequiredRoles(int requiredLevel, Member member) {
		Guild guild = member.getGuild();
		StringBuilder out = new StringBuilder();
		for(final var role : guild.getRoles()) {
			var currentRole = DiscordRoles.SQLgetRoles(guild.getIdLong()).parallelStream().filter(f -> f.getRole_ID() == role.getIdLong()).findAny().orElse(null);
			if(currentRole != null) {
				out.append((!role.getName().equals("@everyone") && currentRole.getLevel() >= requiredLevel ? role.getAsMention()+" " : ""));
			}
			else {
				if(DiscordRoles.SQLInsertRole(guild.getIdLong(), role.getIdLong(), 0, role.getName(), "def", false) > 0) {
					Hashes.removeDiscordRoles(guild.getIdLong());
				}
				else {
					logger.error("The role id {} couldn't be inserted into DiscordRoles.roles table", role.getId());
				}
			}
		}
		return (out.length() > 0 ? out.toString() : STATIC.getTranslation(member, Translation.NO_ROLES));
	}
	
	/**
	 * Check each registered ranking role, sorted by level, which would be able to use the command and print it as message
	 * @param requiredLevel The required command level to execute the command
	 * @param e event listener to retrieve guild details and to submit messages
	 * @return
	 */
	
	private static boolean evaluateRequiredRole(int requiredLevel, GuildMessageReceivedEvent e) {
		//verify that the ranking state is enabled
		if(RankingSystem.SQLgetGuild(e.getGuild().getIdLong()).getRankingState()) {
			//then continue only when any ranking roles are registered
			var ranking_levels = RankingSystem.SQLgetRoles(e.getGuild().getIdLong());
			if(ranking_levels.size() > 0) {
				var registered_roles = DiscordRoles.SQLgetRoles(e.getGuild().getIdLong());
				//sort the ranking roles by level and iterate through the list
				var sorted_list = ranking_levels.parallelStream().sorted(Comparator.comparing(Roles::getLevel)).collect(Collectors.toList());
				for(final var role : sorted_list) {
					//look through all discord roles to retrieve the command level for the current role
					var currentRole = registered_roles.parallelStream().filter(f -> f.getRole_ID() == role.getRole_ID()).findAny().orElse(null);
					if(currentRole != null) {
						//enter if this is the required role with the least amount of required command level
						if(currentRole.getLevel() >= requiredLevel) {
							Role rankingRole = e.getGuild().getRoleById(currentRole.getRole_ID());
							if(rankingRole != null) {
								//send message with the required role before the command can be used
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setThumbnail(BotConfiguration.SQLgetThumbnails(e.getGuild().getIdLong()).getDenied()).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DENIED))
									.setDescription(STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_RANKING).replace("{}", ""+role.getLevel()) + rankingRole.getAsMention()).build()).queue();
								return false;
							}
						}
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * Throw error message in the text channel because the current achieved role can't use the used command
	 * @param e Event listener to print the message
	 * @param requiredLevel Command level required for the command
	 */
	
	public static void throwNotEnoughPrivilegeError(GuildMessageReceivedEvent e, int requiredLevel) {
		//verify first if a ranking role can use the command, else print message with all allowed roles
		if(evaluateRequiredRole(requiredLevel, e))
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setThumbnail(BotConfiguration.SQLgetThumbnails(e.getGuild().getIdLong()).getDenied()).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DENIED))
				.setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE)+UserPrivs.retrieveRequiredRoles(requiredLevel, e.getMember())).build()).queue();
	}
}
