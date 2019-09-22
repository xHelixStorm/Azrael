package timerTask;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import constructors.RSS;
import core.Hashes;
import fileManagement.GuildIni;
import net.dv8tion.jda.api.events.ReadyEvent;
import rss.ParseModel;
import sql.Azrael;
import util.STATIC;

public class ParseRSS extends TimerTask{
	private static final Logger logger = LoggerFactory.getLogger(ParseRSS.class);
	
	private ReadyEvent e;
	private long guild_id;
	
	public ParseRSS(ReadyEvent _e, long _guild_id) {
		this.e = _e;
		this.guild_id = _guild_id;
	}

	@Override
	public void run() {
		try {
			if(Hashes.getFeed(guild_id) != null) {
				logger.info("task running for guild {}", e.getJDA().getGuildById(guild_id).getName());
				var rss_channel = Azrael.SQLgetChannels(guild_id).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("rss")).findAny().orElse(null);
				if(rss_channel != null) {
					for(RSS rss : Hashes.getFeed(guild_id)) {
						new Thread(() -> {
							try {
								logger.debug("Retrieving rss feed for {} in guild {}", rss.getURL(), e.getJDA().getGuildById(guild_id).getName());
								if(rss.getType() == 1)
									ParseModel.BasicModelParse(STATIC.retrieveWebPageCode(rss.getURL()), e, rss, guild_id, rss_channel);
								else if(rss.getType() == 2)
									ParseModel.TwitterModelParse(/*STATIC.retrieveWebPageCode(rss.getURL()),*/ e, rss, guild_id, rss_channel);
							} catch (Exception e1) {
								logger.error("Error on retrieving feed", e1);
							}
						}).start();
					}
				}
				else {
					Azrael.SQLgetChannels(guild_id);
				}
			}
			else {
				if(GuildIni.getRssCommand(guild_id)) {
					Azrael.SQLgetRSSFeeds(guild_id);
					Azrael.SQLgetChannels(guild_id);
				}
			}
		} catch(NullPointerException npe) {
			logger.error("Exception while handling ParseRss. Is Bot ready?", npe);
		}
	}
	
	public static void runTask(ReadyEvent _e, long _guild_id){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		Timer time = new Timer("ParseRSS");
		time.schedule(new ParseRSS(_e, _guild_id), calendar.getTime(), TimeUnit.MINUTES.toMillis(10));
	}
}
