package de.azrael.commandsContainer;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Thumbnails;
import de.azrael.core.Hashes;
import de.azrael.core.UserPrivs;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.sql.RankingSystem;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * Extension of the register command
 * @author xHelixStorm
 *
 */

public class RegisterRankingRole {
	private final static Logger logger = LoggerFactory.getLogger(RegisterRankingRole.class);
	
	public static void RegisterRankingRoleHelper(GuildMessageReceivedEvent e, Thumbnails thumbnails) {
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(thumbnails.getSettings());
		e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_RANK_ROLE_HELP)).build()).queue();
	}
	
	public static boolean runCommand(GuildMessageReceivedEvent e, String [] args, Thumbnails thumbnails) {
		long guild_id = e.getGuild().getIdLong();
		int level_requirement = 0;
		Role role = null;
		
		var commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.REGISTER_RANKING_ROLE);
		if(UserPrivs.comparePrivilege(e.getMember(), commandLevel)) {
			if(args.length == 3 && args[2].matches("[0-9]*")) {
				role = e.getGuild().getRoleById(args[2]);
				if(role == null) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.NO_ROLE_ID)).build()).queue();
					return true;
				}
				if(args[1].matches("[0-9]*")) {
					level_requirement = Integer.parseInt(args[1]);
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_RANK_ROLE_NO_LEVEL)).build()).queue();
					return true;
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
				return true;
			}
			if(level_requirement < 1 || level_requirement > 10000) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_RANK_ROLE_NO_LEVEL)).build()).queue();
			}
			else {
				if(RankingSystem.SQLInsertRole(role.getIdLong(), role.getName(), level_requirement, guild_id) != -1) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_RANK_ROLE_ADDED).replaceFirst("\\{\\}", role.getName()).replace("{}", ""+level_requirement)).build()).queue();
					logger.info("User {} has registered the ranking role {} with the level requirement {} in guild {}", e.getMember().getUser().getId(), role.getName(), level_requirement, e.getGuild().getId());
					Hashes.removeRankingRoles(guild_id);
					if(RankingSystem.SQLgetRoles(guild_id).size() > 0) {
						if(RankingSystem.SQLgetLevels(guild_id).size() == 0) {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_WARNING)).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_RANK_ROLE_WARN)).build()).queue();
							logger.warn("RankingSystem level table is not available in guild {}", e.getGuild().getId());
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Ranking roles couldn't be called and cached in guild {}", e.getGuild().getId());
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Ranking role {} couldn't be registered in guild {}", role.getId(), e.getGuild().getId());
					RankingSystem.SQLInsertActionLog("High", role.getIdLong(), guild_id, "Role couldn't be registered as ranking role", "The role "+role.getName()+" couldn't be inserted into the RankingSystem.roles table");
				}
			}
			return true;
		}
		else {
			EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(thumbnails.getDenied()).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DENIED));
			e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(commandLevel, e.getMember())).build()).queue();
		}
		return false;
	}
}
