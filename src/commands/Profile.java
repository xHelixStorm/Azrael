package commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import constructors.Guilds;
import constructors.Ranking;
import core.Hashes;
import core.UserPrivs;
import enums.Channel;
import enums.Translation;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import rankingSystem.RankingMethods;
import sql.Azrael;
import sql.RankingSystem;
import util.STATIC;

/**
 * The Profile command prints the current profile of a user
 * depending on the selected skin. It's possible to display
 * the profile page of someone else by mentioning
 * @author xHelixStorm
 *
 */

public class Profile implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Profile.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		//check if the command is enabled and that the user has enough permissions
		if(GuildIni.getProfileCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getProfileLevel(e.getGuild().getIdLong());
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
		
		//don't print the profile image, if a bot channel exists but the current channel isn't a bot channel
		if(this_channel == null && bot_channels.size() > 0) {
			e.getChannel().sendMessage(e.getMember().getAsMention()+STATIC.getTranslation(e.getMember(), Translation.NOT_BOT_CHANNEL)+STATIC.getChannels(bot_channels)).queue();
		}
		else {
			long user = 0;
			//check if the user has mentioned a different member and if yes, display the mentioned member's profile
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
			int rank = 0;
			
			//retrieve the guild settings and confirm that the ranking system is enabled
			Guilds guild_settings = RankingSystem.SQLgetGuild(guild_id);
			if(guild_settings.getRankingState()) {
				//verify if the command has been used from this user already and if yes, don't print the profile
				//until the timeout elapses 
				var cache = Hashes.getTempCache("profileDelay_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
				if(cache == null || cache.getExpiration() - System.currentTimeMillis() <= 0) {
					//put the command in timeout for the user
					Hashes.addTempCache("profileDelay_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId(), new Cache(30000));
					
					Ranking user_details = RankingSystem.SQLgetWholeRankView(user_id, guild_id);
					//check if the default skin had been updated, if yes update profile skin, description and file type
					var old_guild_settings = Hashes.getOldGuildSettings(guild_id);
					if(old_guild_settings != null && old_guild_settings.getProfileID() == user_details.getRankingProfile()) {
						user_details.setRankingProfile(guild_settings.getProfileID());
						Hashes.addRanking(guild_id+"_"+user_id, user_details);
					}
					//then do the same comparison for level icons
					if(old_guild_settings != null && old_guild_settings.getIconID() == user_details.getRankingIcon()) {
						user_details.setRankingIcon(guild_settings.getIconID());
						Hashes.addRanking(guild_id+"_"+user_id, user_details);
					}
					
					float experienceCounter;
					int convertedExperience;
					
					Member member = e.getGuild().getMemberById(user_id);
					String name = member.getEffectiveName();
					String avatar = member.getUser().getEffectiveAvatarUrl();
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
					
					//print the profile page, if the current experience isn't in the negative area
					if(currentExperience >= 0) {
						if(user_details.getRankingProfile() > 0 && user_details.getRankingIcon() > 0)
							RankingMethods.getProfile(e, name, avatar, convertedExperience, rank, (int)currentExperience, (int)rankUpExperience, guild_settings.getThemeID(), user_details);
						else {
							EmbedBuilder message = new EmbedBuilder();
							if(user_details.getRankingRank() > 0 && user_details.getRankingIcon() == 0)
								message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PROFILE_NO_ICONS));
							e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.PROFILE_TITLE))
								.setColor(Color.MAGENTA).setAuthor(name, avatar, avatar)
								.addField(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_LEVEL), "**"+user_details.getLevel()+"**", true)
								.addField(STATIC.getTranslation(e.getMember(), Translation.RANK_RANK), "**"+rank+"**", true)
								.addBlankField(true)
								.addField(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_EXPERIENCE), "**"+(long)currentExperience+"/"+(long)rankUpExperience+"**", true)
								.addField(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_TOT_EXPERIENCE), "**"+user_details.getExperience()+"**", true)
								.addBlankField(true)
								.addField(STATIC.getTranslation(e.getMember(), Translation.USER_INFO_BALANCE), "**"+user_details.getCurrency()+"**", false).build()).queue();
						}
					}
					else {
						EmbedBuilder error = new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						RankingSystem.SQLInsertActionLog("critical", user_id, guild_id, "negative experience value", "The user has less experience points in proportion to his level: "+currentExperience);
						logger.error("Negative experience value for {} in guild {}", user_id, e.getGuild().getName());
					}
				}
				else{
					e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.COOLDOWN)).queue();
				}
			}
			else {
				e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.LEVEL_SYSTEM_NOT_ENABLED)).queue();
			}
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.trace("{} has used Profile command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}
}
