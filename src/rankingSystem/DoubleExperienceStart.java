package rankingSystem;

import java.io.File;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Cache;
import core.Hashes;
import enums.Weekday;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.ReadyEvent;
import sql.Azrael;
import sql.RankingSystem;

public class DoubleExperienceStart extends TimerTask{

	private ReadyEvent e;
	
	public DoubleExperienceStart(ReadyEvent _e){
		e = _e;
	}
	
	@Override
	public void run() {
		long guild_id;
		
		if(Hashes.getTempCache("doubleExp") == null || Hashes.getTempCache("doubleExp").getAdditionalInfo().equals("off")) {
			File doubleEvent = new File("./files/RankingSystem/doubleweekend.jpg");
			Hashes.addTempCache("doubleExp", new Cache(0, "on"));
			for(Guild g : e.getJDA().getGuilds()) {
				guild_id = g.getIdLong();
				
				if(RankingSystem.SQLgetGuild(guild_id).getRankingState()) {
					var bot_channel = Azrael.SQLgetChannels(guild_id).parallelStream().filter(f -> f.getChannel_Type().equals("bot")).findAny().orElse(null);
					if(bot_channel != null) {
						e.getJDA().getGuildById(guild_id).getTextChannelById(bot_channel.getChannel_ID()).sendFile(doubleEvent, "doubleweekend.jpg", null).complete();
						e.getJDA().getGuildById(guild_id).getTextChannelById(bot_channel.getChannel_ID()).sendMessage("```css\nThe double EXP weekend is here\nUse the chance to gain more experience points than usual to reach new heights. See you at the top!\nThe event will stay up from Saturday 00:01 cest till Sunday 23:59 cest!```").queue();
					}
				}
			}
		}
		Logger logger = LoggerFactory.getLogger(DoubleExperienceStart.class);
		logger.debug("Double experience is running");
	}
	
	public static void runTask(ReadyEvent _e){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_WEEK, Weekday.getDay(IniFileReader.getDoubleExpStart()));
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		
		Timer time = new Timer();
		time.schedule(new DoubleExperienceStart(_e), calendar.getTime(), TimeUnit.DAYS.toMillis(7));
	}
}
