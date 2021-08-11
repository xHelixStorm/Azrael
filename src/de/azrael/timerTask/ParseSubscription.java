package de.azrael.timerTask;
import java.awt.Color;
import java.net.SocketTimeoutException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.RSS;
import de.azrael.core.Hashes;
import de.azrael.enums.Channel;
import de.azrael.enums.Translation;
import de.azrael.rss.BasicModel;
import de.azrael.rss.RedditModel;
import de.azrael.rss.TwitterModel;
import de.azrael.rss.YouTubeModel;
import de.azrael.sql.Azrael;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

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
					logger.info("Subscription thread interrupted due to the bot leaving the guild in guild {}", guild_id);
					if(timers.containsKey(guild_id)) {
						timers.get(guild_id).cancel();
						timers.remove(guild_id);
					}
					this.cancel();
					return;
				}
				var rss_channel = Azrael.SQLgetChannels(guild_id).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.RSS.getType())).findAny().orElse(null);
				if(rss_channel != null || feeds.parallelStream().filter(f -> f.getChannelID() > 0).findAny().orElse(null) != null) {
					logger.info("Fetching subscriptions in guild {}", guild.getId());
					for(RSS rss : feeds) {
						new Thread(() -> {
							long channel_id;
							boolean defaultChannel = true;
							TextChannel textChannel = guild.getTextChannelById(rss.getChannelID()); 
							if(textChannel != null) {
								channel_id = textChannel.getIdLong();
								defaultChannel = false;
							}
							else if(rss_channel != null) {
								channel_id = rss_channel.getChannel_ID();
							}
							else {
								return;
							}
							try {
								logger.trace("Retrieving subscription {} in guild {}", rss.getURL(), e.getGuildById(guild_id).getId());
								boolean success = false;
								if(rss.getType() == 1)
									success = BasicModel.ModelParse(STATIC.retrieveWebPageCode(rss.getURL()), guild, rss, channel_id, defaultChannel);
								else if(rss.getType() == 2)
									success = TwitterModel.ModelParse(guild, rss, channel_id, defaultChannel);
								else if(rss.getType() == 3)
									success = RedditModel.fetchRedditContent(null, guild, rss, channel_id, defaultChannel);
								else if(rss.getType() == 4)
									success = YouTubeModel.ModelParse(guild, rss, channel_id, defaultChannel);
								if(success)
									Hashes.addSubscriptionStatus(guild.getId()+"_"+rss.getURL(), 0);
								else
									incrementSubscriptionStatus(guild, rss.getURL());
							} catch(SocketTimeoutException e1){
								logger.warn("Timeout on subscription {}", rss.getURL());
								incrementSubscriptionStatus(guild, rss.getURL());
							} catch (Exception e1) {
								logger.error("Error on retrieving subscription {}", rss.getURL(), e1);
								incrementSubscriptionStatus(guild, rss.getURL());
							}
						}).start();
					}
				}
				else {
					logger.info("Subscription task interrupted for not having any subscription channel in guild {}", guild_id);
					if(timers.containsKey(guild_id)) {
						timers.get(guild_id).cancel();
						timers.remove(guild_id);
					}
					this.cancel();
					return;
				}
			}
			else {
				logger.info("Subscription task interrupted for not having any subscriptions in guild {}", guild_id);
				if(timers.containsKey(guild_id)) {
					timers.get(guild_id).cancel();
					timers.remove(guild_id);
				}
				this.cancel();
				return;
			}
		} catch(NullPointerException npe) {
			logger.error("Subscriptions couldn't be fetched in guild {}", npe);
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
	
	private static void incrementSubscriptionStatus(Guild guild, String subscription) {
		Integer count = Hashes.getSubscriptionStatus(guild.getId()+"_"+subscription);
		if(count == null)
			count = 1;
		else
			count++;
		//one full day, if we assume subscriptions are fetched every 10 minutes
		if(count == 144) {
			STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.ORANGE), STATIC.getTranslation2(guild, Translation.SUBSCRIBE_STATUS).replace("{}", subscription), Channel.LOG.getType());
			logger.info("Unreachable subscription {} for the last 24 hours in guild {}", subscription, guild.getId());
			Hashes.addSubscriptionStatus(guild.getId()+"_"+subscription, 0);
		}
		else
			Hashes.addSubscriptionStatus(guild.getId()+"_"+subscription, count);
	}
}
