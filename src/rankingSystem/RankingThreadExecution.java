package rankingSystem;

/**
 * This class is meant to gain experience points basing on the 
 * retrieved messages from users. 
 * 
 * This class can calculate the daily experience limit, work 
 * with applied experience booster and simulate level ups. 
 * Also it will assign the current unlocked ranking role to the
 * user, as long any are registered.
 */

import java.awt.Color;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Guilds;
import constructors.Rank;
import constructors.Roles;
import core.Hashes;
import fileManagement.FileSetting;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.RankingSystem;

public class RankingThreadExecution {
	private final static Logger logger = LoggerFactory.getLogger(RankingThreadExecution.class);
	
	public static void setProgress(GuildMessageReceivedEvent e, long user_id, long guild_id, String message, int roleAssignLevel, long role_id, int percent_multiplier, Rank user_details, Guilds guild_settings) {
		//delete all expired items from the inventory (e.g experience booster)
		RankingSystem.SQLDeleteInventory();
		int multiplier = 1;
		
		long experience = user_details.getExperience();
		int currentExperience = user_details.getCurrentExperience();
		long max_experience = guild_settings.getMaxExperience();
		boolean max_experience_enabled = guild_settings.getMaxExpEnabled();
		
		//specific character sequences have to be edited before experience points can be given out
		message = message.replaceAll("(https|http)[:\\/a-zA-Z0-9-Z.?!=#%&_+-;]*", ""); //Edit Links
		message = message.replaceAll("<:[a-zA-Z0-9]*:[0-9]{18,18}>", ""); //Edit custom images
		message = message.replaceAll("<a:[a-zA-Z0-9]*:[0-9]{18,18}>", ""); //Edit custom animated images
		message = message.replaceAll("<@[0-9!]{18,19}>", ""); //Edit tags
		message = message.replaceAll("[^\\w\\d\\s]", ""); //Edit all special characters
		message = message.replaceAll("[_]", ""); // Edit all underscores
		message = message.replaceAll("[\\s]{2,}", " "); //Edit every multiple whitespace type to a single whitespace
		int messageLength = message.length();
		
		//set the experience gain range basing on the message length
		int adder = 0;
		if(messageLength >= 5 && messageLength <= 10) {adder = ThreadLocalRandom.current().nextInt(1, 11);}
		else if(messageLength >= 11 && messageLength <= 20) {adder = ThreadLocalRandom.current().nextInt(11, 21);}
		else if(messageLength >= 21 && messageLength <= 30) {adder = ThreadLocalRandom.current().nextInt(21, 31);}
		else if(messageLength >= 31 && messageLength <= 40) {adder = ThreadLocalRandom.current().nextInt(31, 41);}
		else if(messageLength >= 41 && messageLength <= 50) {adder = ThreadLocalRandom.current().nextInt(41, 51);}
		else if(messageLength > 50) {adder = ThreadLocalRandom.current().nextInt(51, 71);}
		
		//increase the multiplier if double experience is enabled 
		var doubleExperience = Hashes.getTempCache("doubleExp");
		var doubleExperienceGuild = Hashes.getTempCache("doubleExp_gu"+guild_id);
		if((doubleExperience != null && doubleExperience.getAdditionalInfo().equals("on")) || (doubleExperienceGuild != null && doubleExperienceGuild.getAdditionalInfo().equals("on"))) {
			if(doubleExperienceGuild == null || (doubleExperienceGuild != null && !doubleExperienceGuild.getAdditionalInfo().equals("off"))) {
				multiplier*=2;
			}
		}
		
		//if a booster is enabled, increase the multiplier and multiply the experience to gain
		multiplier += multiplier*(percent_multiplier/100);
		adder *= multiplier;
		
		//increase the current experience and total experience with the gained experience
		currentExperience += adder;
		experience += adder;
		
		//execute as long there is any experience to gain
		if(adder != 0) {
			//take different routes in case a daily experience limit is set or not
			if(max_experience_enabled == true) {
				int daily_experience = user_details.getDailyExperience();
				//if the saved reset time was yesterday, reset the users daily experience gain
				if(user_details.getDailyReset() == null || user_details.getDailyReset().getTime() - System.currentTimeMillis() <= 0) {
					RankingSystem.SQLDeleteDailyExperience(user_id, guild_id);
					daily_experience = 0;
				}
				//execute if the daily exp limit hasn't been reached yet. More experience can be gained depending on the multiplier
				if(daily_experience <= max_experience*multiplier) {
					//define next midnight time
					LocalTime midnight = LocalTime.MIDNIGHT;
					LocalDate today = LocalDate.now();
					LocalDateTime tomorrowMidnight = LocalDateTime.of(today, midnight).plusDays(1);
					Timestamp reset = Timestamp.valueOf(tomorrowMidnight);
					daily_experience += adder;
					//gather experience points and save into RankingSystem.user_details table
					ExperienceGain(e, user_details,  guild_settings, currentExperience, experience, daily_experience, roleAssignLevel, max_experience_enabled, reset);
					//notify the user in private message if the experience limit for the day has been reached
					if(daily_experience > max_experience*multiplier) {
						logger.info("{} has reached the limit of today's max experience points gain", e.getMember().getUser().getId());
						RankingSystem.SQLInsertActionLog("medium", user_id, guild_id, "Experience limit reached", "User reached the limit of experience points");
						e.getMember().getUser().openPrivateChannel().queue(channel -> {
							channel.sendMessage("You have reached the limit of experience points for today. More experience points can be collected tomorrow!").queue(success -> channel.close().queue(), error -> channel.close().queue());
						});
					}
				}
			}
			else if(max_experience_enabled == false) {
				//gather experience points and save into RankingSystem.user_details table
				ExperienceGain(e, user_details, guild_settings, currentExperience, experience, 0, roleAssignLevel, max_experience_enabled, null);
			}
			//remember the user for a determined time, if there should be delays between gaining experience points
			if(guild_settings.getMessageTimeout() != 0)
				Hashes.addCommentedUser(e.getMember().getUser().getId()+"_"+e.getGuild().getId(), e.getMember().getEffectiveName());
		}
	}
	
	private static void ExperienceGain(GuildMessageReceivedEvent e, Rank user_details, Guilds guild_settings, int currentExperience, long experience, int daily_experience, int roleAssignLevel, boolean max_experience_enabled, Timestamp reset) {
		//check if the default skin had been updated, if yes update level skin, description and file type
		var old_guild_settings = Hashes.getOldGuildSettings(e.getGuild().getIdLong());
		if(old_guild_settings != null && old_guild_settings.getLevelID() == user_details.getRankingLevel()) {
			user_details.setRankingLevel(guild_settings.getLevelID());
			user_details.setLevelDescription(guild_settings.getLevelDescription());
			user_details.setFileTypeLevel(guild_settings.getFileTypeLevel());
			Hashes.addRanking(e.getGuild().getId()+"_"+e.getMember().getUser().getId(), user_details);
		}
		//then do the same comparison for level icons
		if(old_guild_settings != null && old_guild_settings.getIconID() == user_details.getRankingIcon()) {
			user_details.setRankingIcon(guild_settings.getIconID());
			user_details.setIconDescription(guild_settings.getIconDescription());
			user_details.setFileTypeIcon(guild_settings.getFileTypeIcon());
			Hashes.addRanking(e.getGuild().getId()+"_"+e.getMember().getUser().getId(), user_details);
		}
		
		int rankUpExperience = user_details.getRankUpExperience();
		int max_level = guild_settings.getMaxLevel();
		int level = user_details.getLevel();
		long currency = user_details.getCurrency();
		
		//user level up. update currency and retrieve the required experience points to reach the next level
		if(currentExperience >= rankUpExperience && level < max_level) {
			level += 1;
			final var newLevel = level;
			currentExperience -= rankUpExperience;
			var levels = RankingSystem.SQLgetLevels(e.getGuild().getIdLong(), guild_settings.getThemeID());
			var levelDetails = levels.parallelStream().filter(f -> f.getLevel() == newLevel).findAny().orElse(null);
			int rankIcon = levelDetails.getRankIcon();
			currency += levelDetails.getCurrency();
			if(level != max_level) {
				rankUpExperience = levels.parallelStream().filter(f -> f.getLevel() == (newLevel+1)).findAny().orElse(null).getExperience() - levelDetails.getExperience();
			}
			else {
				rankUpExperience = 0;
			}
			
			Roles current_role = null;
			//check that the bot has the manage roles permission to remove and assign roles
			if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
				//if a new ranking role has been unlocked, remove old ones and assign the newest one
				final var rankingRoles = RankingSystem.SQLgetRoles(e.getGuild().getIdLong());
				if(level == roleAssignLevel) {
					for(final Role r : e.getMember().getRoles()) {
						for(final var role : rankingRoles) {
							if(r.getIdLong() == role.getRole_ID()) {
								e.getGuild().removeRoleFromMember(e.getMember(), e.getJDA().getGuildById(e.getGuild().getIdLong()).getRoleById(r.getIdLong())).queue();
							}
						}
					}
					current_role = rankingRoles.parallelStream().filter(f -> f.getLevel() == newLevel).findAny().orElse(null);
					if(current_role != null) e.getGuild().addRoleToMember(e.getMember(), e.getJDA().getGuildById(e.getGuild().getIdLong()).getRoleById(current_role.getRole_ID())).queue();
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Permission error!").setDescription("The newest unlocked role couldn't be assigned because the MANAGE ROLES permission is missing!").build()).queue();
				logger.warn("MANAGE ROLES permission for assigning a ranking role is missing for guild {}!", e.getGuild().getId());
			}
			
			//update all user details regarding the level up
			user_details.setLevel(level);
			user_details.setCurrentExperience(currentExperience);
			user_details.setRankUpExperience(rankUpExperience);
			user_details.setExperience(experience);
			user_details.setCurrency(currency);
			
			if(current_role != null) {
				user_details.setCurrentRole(current_role.getRole_ID());
			}
			
			//update the daily experience if the daily experience limit is enabled
			var editedRows = 0;
			if(max_experience_enabled == true) {
				user_details.setDailyExperience(daily_experience);
				user_details.setDailyReset(reset);
				editedRows = RankingSystem.SQLInsertDailyExperience(daily_experience, user_details.getUser_ID(), e.getGuild().getIdLong(), user_details.getDailyReset());
			}
			
			//update all level up details to table and log the details
			if(RankingSystem.SQLsetLevelUp(user_details.getUser_ID(), e.getGuild().getIdLong(), user_details.getLevel(), user_details.getExperience(), user_details.getCurrency(), user_details.getCurrentRole()) > 0) {
				FileSetting.appendFile("./log/rankingdetails.txt", "["+new Timestamp(System.currentTimeMillis())+"] "+user_details.getUser_ID()+" reached level "+user_details.getLevel()+", has "+user_details.getExperience()+" experience and "+user_details.getDailyExperience()+" daily experience from guild "+e.getGuild().getIdLong()+"\n");
				RankingSystem.SQLInsertActionLog("low", user_details.getUser_ID(), e.getGuild().getIdLong(), "Level Up", "User reached level "+user_details.getLevel());
				Hashes.addRanking(e.getGuild().getId()+"_"+e.getMember().getUser().getId(), user_details);
				if(user_details.getRankingLevel() != 0 && user_details.getRankingIcon() != 0) {
					//Upload level up image
					RankingMethods.getRankUp(e, guild_settings.getThemeID(), user_details, rankIcon);
				}
				else {
					EmbedBuilder error = new EmbedBuilder().setColor(Color.RED).setTitle("An error occured!");
					e.getChannel().sendMessage(error.setDescription("Default skins aren't defined. Please contact an administrator!").build()).queue();
					logger.error("Default skins in RankingSystem.guilds are not defined for guild {}", e.getGuild().getName());
				}
			}
			else {
				logger.error("RankingSystem.user_details table couldn't be updated with the latest experience and level information for the user {}", user_details.getUser_ID());
			}
			if(max_experience_enabled == true && editedRows == 0) {
				logger.error("RankingSystem.daily_experience table couldn't be updated with the latest experience information for the user {}", user_details.getUser_ID());
			}
		}
		else {
			//update all regular user details regarding the gain of experience
			user_details.setCurrentExperience(currentExperience);
			user_details.setExperience(experience);
			
			//update the daily experience if the daily experience limit is enabled
			var editedRows = 0;
			if(max_experience_enabled == true) {
				user_details.setDailyExperience(daily_experience);
				user_details.setDailyReset(reset);
				editedRows = RankingSystem.SQLInsertDailyExperience(daily_experience, user_details.getUser_ID(), e.getGuild().getIdLong(), user_details.getDailyReset());
			}
			
			//check that the bot has the manage roles permission to remove and assign roles
			if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
				//verify that the current set ranking role is correct and if it's not, assign it to the user
				final var rankingRoles = RankingSystem.SQLgetRoles(e.getGuild().getIdLong());
				List<Role> current_roles = e.getMember().getRoles().parallelStream().filter(f -> f.getIdLong() != user_details.getCurrentRole() && rankingRoles.parallelStream().filter(r -> r.getRole_ID() == f.getIdLong()).findAny().orElse(null) != null).collect(Collectors.toList());
				if(current_roles.size() > 0) {
					current_roles.parallelStream().forEach(role -> {
						e.getGuild().removeRoleFromMember(e.getMember(), e.getGuild().getRoleById(role.getIdLong())).queue();
					});
					final Role role = e.getGuild().getRoleById(user_details.getCurrentRole());
					if(role != null)
						e.getGuild().addRoleToMember(e.getMember(), role).queue();
				}
				else if(user_details.getCurrentRole() != 0 && rankingRoles.size() > 0 && e.getMember().getRoles().parallelStream().filter(f -> f.getIdLong() == user_details.getCurrentRole()).findAny().orElse(null) == null) {
					final Role role = e.getGuild().getRoleById(user_details.getCurrentRole());
					if(role != null)
						e.getGuild().addRoleToMember(e.getMember(), role).queue();
				}
			}
			else {
				logger.warn("MANAGE ROLES permission for assigning a ranking role is missing for guild {}!", e.getGuild().getId());
			}
			
			//update the gained experience points on table
			if(RankingSystem.SQLUpdateExperience(user_details.getUser_ID(), e.getGuild().getIdLong(), user_details.getExperience()) > 0) {
				Hashes.addRanking(e.getGuild().getId()+"_"+e.getMember().getUser().getId(), user_details);
				FileSetting.appendFile("./log/rankingdetails.txt", "["+new Timestamp(System.currentTimeMillis())+"] "+user_details.getUser_ID()+" reached level "+user_details.getLevel()+", has "+user_details.getExperience()+" experience and "+user_details.getDailyExperience()+" daily experience from guild "+e.getGuild().getIdLong()+"\n");
			}
			else {
				logger.error("Experience points for the user {} in the guild {} couldn't be updated in the table RankingSystem.user_details", e.getMember().getUser().getId(), e.getGuild().getName());
			}
			if(max_experience_enabled == true && editedRows == 0) {
				logger.error("RankingSystem.daily_experience table couldn't be updated with the latest experience information for the user {}", user_details.getUser_ID());
			}
		}
	}
}
