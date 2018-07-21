package rankingSystem;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.ThreadLocalRandom;

import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingDB;
import util.RankingSystemPreferences;

public class RankingThreadExecution implements Runnable{
	static MessageReceivedEvent e;
	
	private static long user_id;
	private static int level;
	private static int currentExperience;
	private static int rankUpExperience;
	private static long experience;
	private static long currency;
	private static long max_experience;
	private static long daily_experience;
	private static boolean max_experience_enabled;
	private static int max_level;
	private static long role_id;
	private static int roleAssignLevel;
	private static long guild_id;
	private static String message;
	private static int level_skin;
	private static int icon_skin;
	private static int percent_multiplier;
	private static int color_r;
	private static int color_g;
	private static int color_b;
	private static int rankx;
	private static int ranky;
	private static int rank_width;
	private static int rank_height;
	private static Timestamp reset;
	
	public RankingThreadExecution(MessageReceivedEvent _e, long _user_id, long _guild_id, String _message, int _rankUpExperience, int _max_level, int _level, int _currentExperience, long _currency, long _max_experience, boolean _max_experience_enabled, int _roleAssignLevel, long _role_id, int _level_skin, int _icon_skin, int _percent_multiplier, int _color_r, int _color_g, int _color_b, int _rankx, int _ranky, int _rank_width, int _rank_height){
		e = _e;
		user_id = _user_id;
		guild_id = _guild_id;
		message = _message;
		rankUpExperience = _rankUpExperience;
		max_level = _max_level;
		level = _level;
		currentExperience = _currentExperience;
		currency = _currency;
		max_experience = _max_experience;
		max_experience_enabled = _max_experience_enabled;
		roleAssignLevel = _roleAssignLevel;
		role_id = _role_id;
		level_skin = _level_skin;
		icon_skin = _icon_skin;
		percent_multiplier = _percent_multiplier;
		color_r = _color_r;
		color_g = _color_g;
		color_b = _color_b;
		rankx = _rankx;
		ranky = _ranky;
		rank_width = _rank_width;
		rank_height = _rank_height;
	}

	@Override
	public void run() {
		RankingDB.SQLDeleteInventory();
		int multiplier = 1;
		Path path = Paths.get("./files/double.azr");
		
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
		
		multiplier+= multiplier*percent_multiplier/100;
		adder+= adder*percent_multiplier/100;
		
		currentExperience += adder;
		experience = RankingSystemPreferences.getTotalExperience(level)+currentExperience;
		
		if(max_experience_enabled == true){
			RankingDB.SQLgetDailyExperience(user_id, guild_id);
			daily_experience = RankingDB.getDailyExperience();
			LocalTime midnight = LocalTime.MIDNIGHT;
			LocalDate today = LocalDate.now();
			LocalDateTime tomorrowMidnight = LocalDateTime.of(today, midnight).plusDays(1);
			reset = Timestamp.valueOf(tomorrowMidnight);
			
			try{
				if(reset.getTime() - RankingDB.getDailyReset().getTime() != 0){
					RankingDB.SQLDeleteDailyExperience(user_id, guild_id);
					daily_experience = 0;
				}
			} catch(NullPointerException npe){
				//ignore the if sequence, if entry doesn't exist
			}
			
			if(daily_experience < max_experience*multiplier){
				daily_experience += adder;
				ExperienceGainWithDaily();
				if(daily_experience > max_experience*multiplier) {
					PrivateChannel pc = e.getMember().getUser().openPrivateChannel().complete();
					pc.sendMessage("You have reached the max possible to gain experience today. More experience points can be collected tomorrow!").queue();
					pc.close();
				}
			}
		}
		else if(max_experience_enabled == false){
			ExperienceGain();
		}
	}
	
	private static void ExperienceGainWithDaily(){
		if(currentExperience >= rankUpExperience && level < max_level){
			level += 1;
			currentExperience = currentExperience - rankUpExperience;
			currency = currency + RankingSystemPreferences.getCurrencyForRankUp(level);
			if(level != max_level){rankUpExperience = RankingSystemPreferences.getExperienceForRankUp(level);}
			else{rankUpExperience = 0;}
			
			if(level == roleAssignLevel){
				try {
					RankingDB.SQLgetRoles(guild_id);
					for(Role r : e.getMember().getRoles()){
						for(Rank role : RankingDB.getRankList()){
							if(r.getIdLong() == role.getRoleID()){
								e.getGuild().getController().removeSingleRoleFromMember(e.getMember(), e.getJDA().getGuildById(guild_id).getRoleById(r.getIdLong())).queue();
							}
						}
					}
					RankingDB.clearArrayList();
					Thread.sleep(1000);
					e.getGuild().getController().addSingleRoleToMember(e.getMember(), e.getJDA().getGuildById(guild_id).getRoleById(role_id)).queue();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			RankingDB.SQLsetLevelUp(user_id, guild_id, level, currentExperience, rankUpExperience, experience, currency, role_id);
			RankingDB.SQLInsertDailyExperience(daily_experience, user_id, guild_id, reset);
			RankingMethods.getRankUp(e, level, level_skin, icon_skin, color_r, color_g, color_b, rankx, ranky, rank_width, rank_height);
		}
		else{
			RankingDB.SQLUpdateExperience(user_id, guild_id, currentExperience, experience);
			RankingDB.SQLInsertDailyExperience(daily_experience, user_id, guild_id, reset);
		}
	}
	
	private static void ExperienceGain(){
		if(currentExperience >= rankUpExperience && level < max_level){
			level += 1;
			currentExperience = currentExperience - rankUpExperience;
			currency = currency + RankingSystemPreferences.getCurrencyForRankUp(level);
			if(level != max_level){rankUpExperience = RankingSystemPreferences.getExperienceForRankUp(level);}
			else{rankUpExperience = 0;}
			
			if(level == roleAssignLevel){
				try {
					RankingDB.SQLgetRoles(guild_id);
					for(Role r : e.getMember().getRoles()){
						for(Rank role : RankingDB.getRankList()){
							if(r.getIdLong() == role.getRoleID()){
								e.getGuild().getController().removeSingleRoleFromMember(e.getMember(), e.getJDA().getGuildById(guild_id).getRoleById(r.getIdLong())).queue();
							}
						}
					}
					RankingDB.clearArrayList();
					Thread.sleep(1000);
					e.getGuild().getController().addSingleRoleToMember(e.getMember(), e.getJDA().getGuildById(guild_id).getRoleById(role_id)).queue();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			
			RankingDB.SQLsetLevelUp(user_id, guild_id, level, currentExperience, rankUpExperience, experience, currency, role_id);
			RankingMethods.getRankUp(e, level, level_skin, icon_skin, color_r, color_g, color_b, rankx, ranky, rank_width, rank_height);
		}
		else{
			RankingDB.SQLUpdateExperience(user_id, guild_id, currentExperience, experience);
		}
	}
}
