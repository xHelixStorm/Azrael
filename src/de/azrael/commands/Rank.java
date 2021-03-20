package de.azrael.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Cache;
import de.azrael.constructors.Guilds;
import de.azrael.constructors.Ranking;
import de.azrael.core.Hashes;
import de.azrael.core.UserPrivs;
import de.azrael.enums.Channel;
import de.azrael.enums.Translation;
import de.azrael.fileManagement.GuildIni;
import de.azrael.interfaces.CommandPublic;
import de.azrael.rankingSystem.RankingMethods;
import de.azrael.sql.Azrael;
import de.azrael.sql.RankingSystem;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * The Rank command prints the current rank page of a user
 * depending on the selected skin. It's possible to display
 * the rank page of someone else by mentioning
 * @author xHelixStorm
 *
 */

public class Rank implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Rank.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		//check if the command is enabled and that the user has enough permissions
		if(GuildIni.getRankCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getRankLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		//retrieve all registered bot channels
		var bot_channels = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.BOT.getType())).collect(Collectors.toList());
		var this_channel = bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null);
		
		//don't print the rank image, if a bot channel exists but the current channel isn't a bot channel
		if(this_channel == null && bot_channels.size() > 0) {
			e.getChannel().sendMessage(e.getMember().getAsMention()+STATIC.getTranslation(e.getMember(), Translation.NOT_BOT_CHANNEL)+STATIC.getChannels(bot_channels)).queue();
		}
		else {
			long user = 0;
			//check if the user has mentioned a different member and if yes, display the mentioned member's rank
			if(args.length > 0) {
				String id = args[0];
				id = id.replaceAll("[^0-9]", "");
				user = id.length() > 0 ? Long.parseLong(id) : 0;
				if(e.getGuild().getMemberById(user) == null)
					user = e.getMember().getUser().getIdLong();
			}
			else {
				user = e.getMember().getUser().getIdLong();
			}
			
			final long user_id = user;
			long guild_id = e.getGuild().getIdLong();
			var rank = 0;
			
			//retrieve the guild settings and confirm that the ranking system is enabled
			Guilds guild_settings = RankingSystem.SQLgetGuild(guild_id);
			if(guild_settings.getRankingState()) {
				//verify if the command has been used from this user already and if yes, don't print the rank
				//until the timeout elapses 
				var cache = Hashes.getTempCache("rankDelay_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
				if(cache == null || cache.getExpiration() - System.currentTimeMillis() <= 0) {
					//put the command in timeout for the user
					Hashes.addTempCache("rankDelay_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId(), new Cache(30000));
					
					Ranking user_details = RankingSystem.SQLgetWholeRankView(user_id, guild_id);
					//check if the default skin had been updated, if yes update rank skin, description and file type
					var old_guild_settings = Hashes.getOldGuildSettings(guild_id);
					if(old_guild_settings != null && old_guild_settings.getRankID() == user_details.getRankingRank()) {
						user_details.setRankingRank(guild_settings.getRankID());
						Hashes.addRanking(guild_id, user_id, user_details);
					}
					//then do the same comparison for level icons
					if(old_guild_settings != null && old_guild_settings.getIconID() == user_details.getRankingIcon()) {
						user_details.setRankingIcon(guild_settings.getIconID());
						Hashes.addRanking(guild_id, user_id, user_details);
					}
					
					float experienceCounter;
					int convertedExperience;
					
					Member member = e.getGuild().getMemberById(user_id);
					final String name = member.getEffectiveName();
					final String avatar = member.getUser().getEffectiveAvatarUrl();
					float currentExperience = user_details.getCurrentExperience();
					float rankUpExperience = user_details.getRankUpExperience();
					
					//set a static experience value if the current level is the same as the max level
					if(user_details.getLevel() == guild_settings.getMaxLevel()) {
						currentExperience = 999999; rankUpExperience = 999999;
					}
					
					//calculate the current experience percentage
					experienceCounter = (currentExperience / rankUpExperience)*100;
					convertedExperience = (int) experienceCounter;
					if(convertedExperience > 100) {
						convertedExperience = 100;
					}
					
					//collect the current ranking on the server
					ArrayList<Ranking> rankList = RankingSystem.SQLRanking(guild_id);
					if(rankList.size() > 0) {
						final var ranking = rankList.parallelStream().filter(f -> f.getUser_ID() == user_id).findAny().orElse(null);
						if(ranking != null)
							rank = ranking.getRank();
						else
							rank = 0;
					}
					
					//print the rank page, if the current experience isn't in the negative area
					if(currentExperience >= 0) {
						//draw rank skin if it isn't 0
						if(user_details.getRankingRank() > 0 && user_details.getRankingIcon() > 0) {
							if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.MESSAGE_ATTACH_FILES) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.MESSAGE_ATTACH_FILES))) {
								RankingMethods.getRank(e, name, avatar, convertedExperience, rank, user_details);
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MESSAGE_ATTACH_FILES.getName()).build()).queue();
								logger.error("Permission MESSAGE_ATTACH_FILES required to display the rank page in channel {} in guild {}", e.getChannel().getId(), e.getGuild().getId());
							}
						}
						//send an embed if it's 0
						else {
							EmbedBuilder message = new EmbedBuilder();
							if(user_details.getRankingRank() > 0 && user_details.getRankingIcon() == 0)
								message.setDescription(STATIC.getTranslation(e.getMember(), Translation.RANK_NO_ICONS));
							e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.RANK_TITLE))
								.setColor(Color.MAGENTA).setAuthor(name, avatar, avatar)
								.addField(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_LEVEL), "**"+(user_details.getDisplayLevel() > 0 ? user_details.getDisplayLevel() : user_details.getLevel())+"**", true)
								.addField(STATIC.getTranslation(e.getMember(), Translation.RANK_RANK), "**"+rank+"**", true)
								.addBlankField(true)
								.addField(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_EXPERIENCE), "**"+(long)currentExperience+"/"+(long)rankUpExperience+"**", true)
								.addField(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_TOT_EXPERIENCE), "**"+user_details.getExperience()+"**", true).build()).queue();
						}
					}
					else {
						EmbedBuilder error = new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						RankingSystem.SQLInsertActionLog("critical", user_id, guild_id, "negative experience value", "The user has less experience points in proportion to his level: "+currentExperience);
						logger.error("Negative experience value for user {} in guild {}", user_id, e.getGuild().getId());
					}
				}
				else{
					e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.COOLDOWN)).queue();
				}
			}
			else{
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.LEVEL_SYSTEM_NOT_ENABLED)).build()).queue();
			}
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.trace("{} has used Rank command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}
}
