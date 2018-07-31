package rankingSystem;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import fileManagement.FileSetting;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.ReadyEvent;
import sql.RankingDB;
import sql.SqlConnect;

public class DoubleExperienceStart extends TimerTask{

	private ReadyEvent e;
	
	public DoubleExperienceStart(ReadyEvent _e){
		e = _e;
	}
	
	@Override
	public void run() {
		Path path = Paths.get("./files/double.azr");
		long guild_id;
		long channel_id;
		boolean ranking_state;
		
		if(Files.exists(path)){
			System.out.println("Double experience event is running!");
		}
		else{
			File doubleEvent = new File("./files/RankingSystem/doubleweekend.jpg");
			FileSetting.createFile("files/double.azr", "This file is for the purpose of enabling the double exp event.");
			for(Guild g : e.getJDA().getGuilds()){
				guild_id = g.getIdLong();
				
				RankingDB.SQLgetGuild(guild_id);
				ranking_state = RankingDB.getRankingState();
				
				if(ranking_state == true){
					SqlConnect.SQLgetChannelID(guild_id, "bot");
					channel_id = SqlConnect.getChannelID();
					e.getJDA().getGuildById(guild_id).getTextChannelById(channel_id).sendFile(doubleEvent, "doubleweekend.jpg", null).complete();
					e.getJDA().getGuildById(guild_id).getTextChannelById(channel_id).sendMessage("```css\nThe double EXP weekend is here\nUse the chance to gain more experience points than usual to reach new heights. See you at the top!\nThe event will stay up from Saturday 00:01 cest till Sunday 23:59 cest!```").queue();
				}
			}
			RankingDB.clearAllVariables();
			SqlConnect.clearAllVariables();
		}
	}
	
	public static void runTask(ReadyEvent _e){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 1);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		Timer time = new Timer();
		time.schedule(new DoubleExperienceStart(_e), calendar.getTime(), TimeUnit.DAYS.toMillis(7));
	}
}
