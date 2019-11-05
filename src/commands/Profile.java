package commands;

/**
 * The Profile command prints the current profile of a user
 * depending on the selected skin. It's possible to display
 * the profile page of someone else by mentioning
 */

import java.awt.Color;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import constructors.Guilds;
import core.Hashes;
import core.UserPrivs;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import rankingSystem.RankingMethods;
import sql.Azrael;
import sql.RankingSystem;
import util.STATIC;

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
		var bot_channels = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("bot")).collect(Collectors.toList());
		var this_channel = bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null);
		
		//don't print the profile image, if a bot channel exists but the current channel isn't a bot channel
		if(this_channel == null && bot_channels.size() > 0) {
			e.getChannel().sendMessage(e.getMember().getAsMention()+" I'm not allowed to execute commands in this channel, please write it again in "+STATIC.getChannels(bot_channels)).queue();
		}
		else {
			long user = 0;
			//check if the user has mentioned a different member and if yes, display the mentioned member's profile
			if(args.length > 0) {
				String id = args[0];
				try {
					id = id.replaceAll("[^0-9]", "");
					user = id.length() > 0 ? Long.parseLong(id) : 0;
					e.getGuild().getMemberById(user).getUser();
				} catch(Exception exc) {
					user = e.getMember().getUser().getIdLong();
				}
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
					
					constructors.Rank user_details = RankingSystem.SQLgetWholeRankView(user_id, guild_id);
					//check if the default skin had been updated, if yes update profile skin, description and file type
					var old_guild_settings = Hashes.getOldGuildSettings(guild_id);
					if(old_guild_settings != null && old_guild_settings.getProfileID() == user_details.getRankingProfile()) {
						user_details.setRankingProfile(guild_settings.getProfileID());
						user_details.setProfileDescription(guild_settings.getProfileDescription());
						user_details.setFileTypeProfile(guild_settings.getFileTypeProfile());
						Hashes.addRanking(guild_id+"_"+user_id, user_details);
					}
					//then do the same comparison for level icons
					if(old_guild_settings != null && old_guild_settings.getIconID() == user_details.getRankingIcon()) {
						user_details.setRankingIcon(guild_settings.getIconID());
						user_details.setIconDescription(guild_settings.getIconDescription());
						user_details.setFileTypeIcon(guild_settings.getFileTypeIcon());
						Hashes.addRanking(guild_id+"_"+user_id, user_details);
					}
					
					float experienceCounter;
					int convertedExperience;
					
					String name = e.getGuild().getMemberById(user_id).getEffectiveName();
					String avatar = e.getGuild().getMemberById(user_id).getUser().getEffectiveAvatarUrl();
					float currentExperience = user_details.getCurrentExperience();
					float rankUpExperience = user_details.getRankUpExperience();
					
					//verify that the profile and icon default skins are defined, else throw error
					if(user_details.getRankingProfile() != 0 && user_details.getRankingIcon() != 0) {
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
						ArrayList<constructors.Rank> rankList = RankingSystem.SQLRanking(guild_id);
						if(rankList.size() > 0) {
							final var ranking = rankList.parallelStream().filter(f -> f.getUser_ID() == user_id).findAny().orElse(null);
							if(ranking != null)
								rank = ranking.getRank();
							else
								rank = 0;
						}
						
						//print the profile page, if the current experience isn't in the negative area
						if(currentExperience >= 0) {
							RankingMethods.getProfile(e, name, avatar, convertedExperience, rank, (int)currentExperience, (int)rankUpExperience, guild_settings.getThemeID(), user_details);
						}
						else {
							EmbedBuilder error = new EmbedBuilder().setColor(Color.RED).setTitle("An error occured!");
							e.getChannel().sendMessage(error.setDescription("An error occured on use. Please contact an administrator or moderator!").build()).queue();
							RankingSystem.SQLInsertActionLog("critical", user_id, guild_id, "negative experience value", "The user has less experience points in proportion to his level: "+currentExperience);
							logger.error("Negative experience valur for {} in guild {}", user_id, e.getGuild().getName());
						}
					}
					else {
						EmbedBuilder error = new EmbedBuilder().setColor(Color.RED).setTitle("An error occured!");
						e.getChannel().sendMessage(error.setDescription("Default skins aren't defined. Please contact an administrator!").build()).queue();
						logger.error("Default skins in RankingSystem.guilds are not defined for guild {}", e.getGuild().getName());
					}
				}
				else{
					e.getChannel().sendMessage("This command is currently having a cooldown, please try again later").queue();
				}
			}
			else {
				e.getChannel().sendMessage("**The ranking system is disabled. Please contact an administrator to enable the feature!**").queue();
			}
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("{} has used Profile command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}
}
