package commandsContainer;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import core.UserPrivs;
import enums.Translation;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.RankingSystem;
import util.STATIC;

/**
 * Extension of the register command
 * @author xHelixStorm
 *
 */

public class RegisterRankingRole {
	private final static Logger logger = LoggerFactory.getLogger(RegisterRankingRole.class);
	
	public static void RegisterRankingRoleHelper(GuildMessageReceivedEvent e) {
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(IniFileReader.getSettingsThumbnail());
		e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_RANK_ROLE_HELP)).build()).queue();
	}
	
	public static void runCommand(GuildMessageReceivedEvent e, long _guild_id, String [] _args, boolean adminPermission) {
		long guild_id = e.getGuild().getIdLong();
		long role_id = 0;
		String role_name = "";
		String level = "";
		int level_requirement = 0;
		
		var commandLevel = GuildIni.getRegisterRankingRoleLevel(e.getGuild().getIdLong());
		if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
			if(_args.length == 3) {
				if(e.getGuild().getRoleById(_args[1]) != null) {
					role_id = Long.parseLong(_args[1]);
					role_name = e.getGuild().getRoleById(role_id).getName();
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.NO_ROLE_ID)).build()).queue();
					return;
				}
				if(_args[2].replaceAll("[0-9]*", "").length() == 0) {
					level = _args[2];
					level_requirement = Integer.parseInt(level);
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_RANK_ROLE_NO_LEVEL)).build()).queue();
					return;
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
				return;
			}
			if(level_requirement < 1 || level_requirement > 10000) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_RANK_ROLE_NO_LEVEL)).build()).queue();
			}
			else {
				if(RankingSystem.SQLInsertRole(role_id, role_name, level_requirement, guild_id) != -1) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_RANK_ROLE_ADDED).replaceFirst("\\{\\}", role_name).replace("{}", ""+level_requirement)).build()).queue();
					logger.info("User {} has registered the ranking role {} with the level requirement {} in guild {}", e.getMember().getUser().getId(), role_name, level_requirement, e.getGuild().getId());
					Hashes.removeRankingRoles(guild_id);
					if(RankingSystem.SQLgetRoles(guild_id).size() > 0) {
						if(RankingSystem.SQLgetLevels(guild_id, RankingSystem.SQLgetGuild(guild_id).getThemeID()).size() == 0) {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("RankingSystem Levels couldn't be retrieved and cached in guild {}", e.getGuild().getId());
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Ranking roles couldn't be called and cached in guild {}", e.getGuild().getId());
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Ranking role {} couldn't be registered in guild {}", role_id, e.getGuild().getId());
					RankingSystem.SQLInsertActionLog("High", role_id, guild_id, "Role couldn't be registered as ranking role", "The role "+role_name+" couldn't be inserted into the RankingSystem.roles table");
				}
			}
		}
		else {
			EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DENIED));
			e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(commandLevel, e.getMember())).build()).queue();
		}
	}
}
