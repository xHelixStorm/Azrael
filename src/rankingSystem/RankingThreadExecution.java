package rankingSystem;

import java.awt.Color;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Guilds;
import constructors.Rank;
import core.Hashes;
import fileManagement.FileSetting;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.RankingSystem;

public class RankingThreadExecution {
	private final static Logger logger = LoggerFactory.getLogger(RankingThreadExecution.class);
	
	public static void setProgress(GuildMessageReceivedEvent e, long user_id, long guild_id, String message, int roleAssignLevel, long role_id, int percent_multiplier, Rank user_details, Guilds guild_settings){
		RankingSystem.SQLDeleteInventory();
		int multiplier = 1;
		
		long experience = user_details.getExperience();
		int currentExperience = user_details.getCurrentExperience();
		long max_experience = guild_settings.getMaxExperience();
		boolean max_experience_enabled = guild_settings.getMaxExpEnabled();
		
		//specific character sequences to edit before experience points will be given out
		message = message.replaceAll("(https|http)[:\\/a-zA-Z0-9-Z.?!=#%&_+-;]*", ""); //Edit Links
		message = message.replaceAll("<:[a-zA-Z0-9]*:[0-9]{18,18}>", ""); //Edit custom images
		message = message.replaceAll("<a:[a-zA-Z0-9]*:[0-9]{18,18}>", ""); //Edit custom animated images
		message = message.replaceAll("<@[0-9!]{18,19}>", ""); //Edit tags
		message = message.replaceAll("[^\\w\\d\\s]", ""); //Edit all special characters
		message = message.replaceAll("[_]", ""); // Edit all underscores
		message = message.replaceAll("[\\s]{2,}", " "); //Edit every multiple whitespace type to a single whitespace
		int messageLength = message.length();
		
		int adder = 0;
		if(messageLength >= 5 && messageLength <= 10){adder = ThreadLocalRandom.current().nextInt(1, 11);}
		else if(messageLength >= 11 && messageLength <= 20){adder = ThreadLocalRandom.current().nextInt(11, 21);}
		else if(messageLength >= 21 && messageLength <= 30){adder = ThreadLocalRandom.current().nextInt(21, 31);}
		else if(messageLength >= 31 && messageLength <= 40){adder = ThreadLocalRandom.current().nextInt(31, 41);}
		else if(messageLength >= 41 && messageLength <= 50){adder = ThreadLocalRandom.current().nextInt(41, 51);}
		else if(messageLength > 50){adder = ThreadLocalRandom.current().nextInt(51, 71);}
		
		var doubleExperience = Hashes.getTempCache("doubleExp");
		var doubleExperienceGuild = Hashes.getTempCache("doubleExp_gu"+guild_id);
		if((doubleExperience != null && doubleExperience.getAdditionalInfo().equals("on")) || (doubleExperienceGuild != null && doubleExperienceGuild.getAdditionalInfo().equals("on"))) {
			if(doubleExperienceGuild == null || (doubleExperienceGuild != null && !doubleExperienceGuild.getAdditionalInfo().equals("off"))) {
				multiplier*=2;
				adder*=2;
			}
		}
		
		multiplier += multiplier*(percent_multiplier/100);
		adder *= multiplier;
		
		currentExperience += adder;
		experience += adder;
		
		if(adder != 0) {
			if(max_experience_enabled == true) {
				int daily_experience = user_details.getDailyExperience();
				if(user_details.getDailyReset() == null || user_details.getDailyReset().getTime() - System.currentTimeMillis() <= 0) {
					RankingSystem.SQLDeleteDailyExperience(user_id, guild_id);
					daily_experience = 0;
				}
				if(daily_experience <= max_experience*multiplier) {
					LocalTime midnight = LocalTime.MIDNIGHT;
					LocalDate today = LocalDate.now();
					LocalDateTime tomorrowMidnight = LocalDateTime.of(today, midnight).plusDays(1);
					Timestamp reset = Timestamp.valueOf(tomorrowMidnight);
					daily_experience += adder;
					ExperienceGain(e, user_details,  guild_settings, currentExperience, experience, daily_experience, roleAssignLevel, max_experience_enabled, reset);
					if(daily_experience > max_experience*multiplier) {
						logger.info("{} has reached the limit of today's max experience points gain", e.getMember().getUser().getId());
						RankingSystem.SQLInsertActionLog("medium", user_id, guild_id, "Experience limit reached", "User reached the limit of experience points");
						PrivateChannel pc = e.getMember().getUser().openPrivateChannel().complete();
						pc.sendMessage("You have reached the limit of experience points for today. More experience points can be collected tomorrow!").queue();
						pc.close();
					}
				}
			}
			else if(max_experience_enabled == false) {
				ExperienceGain(e, user_details, guild_settings, currentExperience, experience, 0, roleAssignLevel, max_experience_enabled, null);
			}
			if(guild_settings.getMessageTimeout() != 0)
				Hashes.addCommentedUser(e.getMember().getUser().getId()+"_"+e.getGuild().getId(), e.getMember().getEffectiveName());
		}
	}
	
	private static void ExperienceGain(GuildMessageReceivedEvent e, Rank user_details, Guilds guild_settings, int currentExperience, long experience, int daily_experience, int roleAssignLevel, boolean max_experience_enabled, Timestamp reset){
		int rankUpExperience = user_details.getRankUpExperience();
		int max_level = guild_settings.getMaxLevel();
		int level = user_details.getLevel();
		long currency = user_details.getCurrency();
		
		if(currentExperience >= rankUpExperience && level < max_level) {
			level += 1;
			currentExperience -= rankUpExperience;
			currency += Hashes.getRankingLevels(e.getGuild().getId()+"_"+level).getCurrency();
			if(level != max_level) {
				rankUpExperience = Hashes.getRankingLevels(e.getGuild().getId()+"_"+(level+1)).getExperience() - Hashes.getRankingLevels(e.getGuild().getId()+"_"+level).getExperience();
			}
			else{
				rankUpExperience = 0;
			}
			
			Rank current_role = null;
			var rankingRoles = RankingSystem.SQLgetRoles(e.getGuild().getIdLong());
			if(level == roleAssignLevel){
				for(Role r : e.getMember().getRoles()){
					for(Rank role : rankingRoles) {
						if(r.getIdLong() == role.getRoleID() && role.getGuildID() == e.getGuild().getIdLong()) {
							e.getGuild().removeRoleFromMember(e.getMember(), e.getJDA().getGuildById(e.getGuild().getIdLong()).getRoleById(r.getIdLong())).queue();
						}
					}
				}
				final var newLevel = level;
				current_role = rankingRoles.parallelStream().filter(f -> f.getLevel_Requirement() == newLevel).findAny().orElse(null);
				if(current_role != null) e.getGuild().addRoleToMember(e.getMember(), e.getJDA().getGuildById(e.getGuild().getIdLong()).getRoleById(current_role.getRoleID())).queue();
			}
			
			user_details.setLevel(level);
			user_details.setCurrentExperience(currentExperience);
			user_details.setRankUpExperience(rankUpExperience);
			user_details.setExperience(experience);
			user_details.setCurrency(currency);
			
			if(current_role != null){
				user_details.setCurrentRole(current_role.getRoleID());
			}
			
			var editedRows = 0;
			if(max_experience_enabled == true){
				user_details.setDailyExperience(daily_experience);
				user_details.setDailyReset(reset);
				editedRows = RankingSystem.SQLInsertDailyExperience(daily_experience, user_details.getUser_ID(), e.getGuild().getIdLong(), user_details.getDailyReset());
			}
			
			if(RankingSystem.SQLsetLevelUp(user_details.getUser_ID(), e.getGuild().getIdLong(), user_details.getLevel(), user_details.getExperience(), user_details.getCurrency(), user_details.getCurrentRole()) > 0) {
				FileSetting.appendFile("./log/rankingdetails.txt", "["+new Timestamp(System.currentTimeMillis())+"] "+user_details.getUser_ID()+" reached level "+user_details.getLevel()+", has "+user_details.getExperience()+" experience and "+user_details.getDailyExperience()+" daily experience from guild "+e.getGuild().getIdLong()+"\n");
				RankingSystem.SQLInsertActionLog("low", user_details.getUser_ID(), e.getGuild().getIdLong(), "Level Up", "User reached level "+user_details.getLevel());
				Hashes.addRanking(e.getGuild().getId()+"_"+e.getMember().getUser().getId(), user_details);
				if(user_details.getRankingLevel() != 0 && user_details.getRankingIcon() != 0) {
					RankingMethods.getRankUp(e, guild_settings.getThemeID(), user_details);
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
		else{
			user_details.setCurrentExperience(currentExperience);
			user_details.setExperience(experience);
			
			var editedRows = 0;
			if(max_experience_enabled == true){
				user_details.setDailyExperience(daily_experience);
				user_details.setDailyReset(reset);
				editedRows = RankingSystem.SQLInsertDailyExperience(daily_experience, user_details.getUser_ID(), e.getGuild().getIdLong(), user_details.getDailyReset());
			}
			
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
