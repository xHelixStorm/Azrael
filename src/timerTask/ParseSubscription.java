package timerTask;
import java.net.SocketTimeoutException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import constructors.RSS;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import rss.BasicModel;
import rss.TwitterModel;
import sql.Azrael;
import util.STATIC;

public class ParseSubscription extends TimerTask{
	private static final Logger logger = LoggerFactory.getLogger(ParseSubscription.class);
	
	private static ConcurrentHashMap<Long, Timer> timers = new ConcurrentHashMap<Long, Timer>();
	
	private JDA e;
	private long guild_id;
	
	public ParseSubscription(JDA _e, long _guild_id) {
		this.e = _e;
		this.guild_id = _guild_id;
	}

	@Override
	public void run() {
		var feeds = Azrael.SQLgetSubscriptions(guild_id);
		try {
			if(feeds != null && feeds.size() > 0) {
				Guild guild = e.getGuildById(guild_id);
				if(guild == null) {
					logger.info("Subscription thread interrupted due to the bot leaving the guild {}", guild_id);
					timers.get(guild_id).cancel();
					timers.remove(guild_id);
					this.cancel();
					return;
				}
				var rss_channel = Azrael.SQLgetChannels(guild_id).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("rss")).findAny().orElse(null);
				if(rss_channel != null) {
					if(feeds.size() > 0) {
						logger.info("Fetching subscriptions for guild {}", guild.getName());
						for(RSS rss : feeds) {
							new Thread(() -> {
								try {
									logger.trace("Retrieving subscription for {} in guild {}", rss.getURL(), e.getGuildById(guild_id).getName());
									if(rss.getType() == 1)
										BasicModel.ModelParse(STATIC.retrieveWebPageCode(rss.getURL()), guild, rss, rss_channel);
									else if(rss.getType() == 2)
										TwitterModel.ModelParse(guild, rss, rss_channel);
								} catch(SocketTimeoutException e1){
									logger.warn("Timeout on subscription {}", rss.getURL());
								} catch (Exception e1) {
									logger.error("Error on retrieving subscription {}", rss.getURL(), e1);
								}
							}).start();
						}
					}
				}
				else {
					logger.info("Subscription task interrupted for not having any subscription channel in guild {}", guild_id);
					timers.get(guild_id).cancel();
					timers.remove(guild_id);
					this.cancel();
					return;
				}
			}
			else {
				logger.info("Subscription task interrupted for not having any feeds in guild {}", guild_id);
				timers.get(guild_id).cancel();
				timers.remove(guild_id);
				this.cancel();
				return;
			}
		} catch(NullPointerException npe) {
			logger.error("Exception while handling ParseRss. Is Discord REST service available?", npe);
		}
	}
	
	public static void runTask(JDA e, long guild_id) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		Timer timer = new Timer("Subscription_"+guild_id);
		timers.put(guild_id, timer);
		timer.schedule(new ParseSubscription(e, guild_id), calendar.getTime(), TimeUnit.MINUTES.toMillis(10));
	}
	
	public static boolean timerIsRunning(long guild_id) {
		return timers.contains(guild_id);
	}
}
