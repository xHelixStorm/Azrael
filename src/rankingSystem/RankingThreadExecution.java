package rankingSystem;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.ThreadLocalRandom;

import core.Guilds;
import core.Hashes;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingDB;

public class RankingThreadExecution {
	public static void setProgress(MessageReceivedEvent e, long user_id, long guild_id, String message, int roleAssignLevel, long role_id, int percent_multiplier, Rank user_details, Guilds guild_settings){
		RankingDB.SQLDeleteInventory();
		int multiplier = 1;
		Path path = Paths.get("./files/double.azr");
		
		long experience = user_details.getExperience();
		int currentExperience = user_details.getCurrentExperience();
		long max_experience = guild_settings.getMaxExperience();
		boolean max_experience_enabled = guild_settings.getMaxExpEnabled();
		
		//specific character sequences to edit before experience points will be given out
		message = message.replaceAll("(https|http)[:\\/a-zA-Z0-9-Z.?!=#%&_+-;]*", ""); //Edit Links
		message = message.replaceAll("<:[a-zA-Z0-9]*:[0-9]{18,18}>", ""); //Edit custom images
		message = message.replaceAll("<a:[a-zA-Z0-9]*:[0-9]{18,18}>", ""); //Edit custom animated images
		message = message.replaceAll("<@[0-9!]{18,19}>", ""); //Edit tags
		message = message.replaceAll("[\\s]{2,}", " "); //Edit every multiple whitespace type to a single whitespace
		int messageLength = message.length();
		
		int adder = 0;
		if(messageLength >= 5 && messageLength <= 10){adder = ThreadLocalRandom.current().nextInt(1, 11);}
		else if(messageLength >= 11 && messageLength <= 20){adder = ThreadLocalRandom.current().nextInt(11, 21);}
		else if(messageLength >= 21 && messageLength <= 30){adder = ThreadLocalRandom.current().nextInt(21, 31);}
		else if(messageLength >= 31 && messageLength <= 40){adder = ThreadLocalRandom.current().nextInt(31, 41);}
		else if(messageLength >= 41 && messageLength <= 50){adder = ThreadLocalRandom.current().nextInt(41, 51);}
		else if(messageLength > 50){adder = ThreadLocalRandom.current().nextInt(51, 71);}
		
		if(Files.exists(path)){
			multiplier*=2;
			adder*=2;
		}
		
		multiplier += multiplier*(percent_multiplier/100);
		adder += adder*(percent_multiplier/100);
		
		currentExperience += adder;
		experience += adder;
		
		if(max_experience_enabled == true){
			int daily_experience = user_details.getDailyExperience();
			LocalTime midnight = LocalTime.MIDNIGHT;
			LocalDate today = LocalDate.now();
			LocalDateTime tomorrowMidnight = LocalDateTime.of(today, midnight).plusDays(1);
			Timestamp reset = Timestamp.valueOf(tomorrowMidnight);
			
			if(user_details.getDailyReset() == null || reset.getTime() - user_details.getDailyReset().getTime() != 0){
				RankingDB.SQLDeleteDailyExperience(user_id);
				daily_experience = 0;
			}
			
			if(daily_experience < max_experience+multiplier){
				daily_experience += adder;
				ExperienceGain(e, user_details,  guild_settings, currentExperience, experience, daily_experience, roleAssignLevel, max_experience_enabled, reset);
				if(daily_experience > max_experience*multiplier){
					PrivateChannel pc = e.getMember().getUser().openPrivateChannel().complete();
					pc.sendMessage("You have reached the max possible to gain experience today. More experience points can be collected tomorrow!").queue();
					pc.close();
				}
			}
		}
		else if(max_experience_enabled == false){
			ExperienceGain(e, user_details, guild_settings, currentExperience, experience, 0, roleAssignLevel, max_experience_enabled, null);
		}
	}
	
	private static void ExperienceGain(MessageReceivedEvent e, Rank user_details, Guilds guild_settings, int currentExperience, long experience, int daily_experience, int roleAssignLevel, boolean max_experience_enabled, Timestamp reset){
		int rankUpExperience = user_details.getRankUpExperience();
		int max_level = guild_settings.getMaxLevel();
		int level = user_details.getLevel();
		long currency = user_details.getCurrency();
		
		if(currentExperience >= rankUpExperience && level < max_level){
			level += 1;
			currentExperience -= rankUpExperience;
			currency += Hashes.getRankingLevels(level).getCurrency();
			if(level != max_level){
				rankUpExperience = Hashes.getRankingLevels(level).getExperience() - Hashes.getRankingLevels(level-1).getExperience();
			}
			else{
				rankUpExperience = 0;
			}
			
			long current_role = 0;
			if(level == roleAssignLevel){
				for(Role r : e.getMember().getRoles()){
					for(Rank role : Hashes.getMapOfRankingRoles().values()){
						if(r.getIdLong() == role.getRoleID() && role.getGuildID() == e.getGuild().getIdLong()){
							e.getGuild().getController().removeSingleRoleFromMember(e.getMember(), e.getJDA().getGuildById(e.getGuild().getIdLong()).getRoleById(r.getIdLong())).queue();
						}
					}
				}
				current_role = Hashes.getRankingRoles(e.getGuild().getIdLong()+"_"+level).getRoleID();
				e.getGuild().getController().addSingleRoleToMember(e.getMember(), e.getJDA().getGuildById(e.getGuild().getIdLong()).getRoleById(current_role)).queue();
			}
			
			user_details.setLevel(level);
			user_details.setCurrentExperience(currentExperience);
			user_details.setRankUpExperience(rankUpExperience);
			user_details.setExperience(experience);
			user_details.setCurrency(currency);
			
			if(current_role != 0){
				user_details.setCurrentRole(current_role);
			}
			
			if(max_experience_enabled == true){
				user_details.setDailyExperience(daily_experience);
				user_details.setDailyReset(reset);
				RankingDB.SQLInsertDailyExperience(daily_experience, user_details.getUser_ID(), user_details.getDailyReset());
			}
			
			RankingDB.SQLsetLevelUp(user_details.getUser_ID(), user_details.getLevel(), user_details.getExperience(), user_details.getCurrency(), user_details.getCurrentRole());
			RankingMethods.getRankUp(e, level, user_details.getRankingLevel(), user_details.getRankingIcon(), user_details.getColorRLevel(), user_details.getColorGLevel(), user_details.getColorBLevel(), user_details.getRankXLevel(), user_details.getRankYLevel(), user_details.getRankWidthLevel(), user_details.getRankHeightLevel());
			Hashes.addRanking(e.getMember().getUser().getIdLong(), user_details);
		}
		else{
			user_details.setCurrentExperience(currentExperience);
			user_details.setExperience(experience);
			
			if(max_experience_enabled == true){
				user_details.setDailyExperience(daily_experience);
				user_details.setDailyReset(reset);
				RankingDB.SQLInsertDailyExperience(daily_experience, user_details.getUser_ID(), user_details.getDailyReset());
			}
			
			RankingDB.SQLUpdateExperience(user_details.getUser_ID(), user_details.getExperience());
			Hashes.addRanking(e.getMember().getUser().getIdLong(), user_details);
		}
	}
}
