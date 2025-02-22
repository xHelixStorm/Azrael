package de.azrael.timerTask;
import java.awt.Color;
import java.net.SocketTimeoutException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Subscription;
import de.azrael.enums.Channel;
import de.azrael.enums.Translation;
import de.azrael.sql.Azrael;
import de.azrael.subscription.RSSModel;
import de.azrael.subscription.RedditModel;
import de.azrael.subscription.TwitchModel;
import de.azrael.subscription.TwitterModel;
import de.azrael.subscription.YouTubeModel;
import de.azrael.util.Hashes;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class ParseSubscription extends TimerTask {
	private static final Logger logger = LoggerFactory.getLogger(ParseSubscription.class);
	
	private static Timer timer = null;
	
	private JDA e;
	
	public ParseSubscription(JDA _e) {
		this.e = _e;
	}

	@Override
	public void run() {
		var subscriptions = Azrael.SQLgetSubscriptions();
		try {
			if(subscriptions != null && subscriptions.size() > 0) {
				logger.info("Fetching subscriptions for all guilds");
				final Map<Long, List<Subscription>> groupedSubscriptions = subscriptions.parallelStream().collect(Collectors.groupingBy(Subscription::getGuildId));
				groupedSubscriptions.forEach((k,v) -> {
					new Thread(() -> {
						final Guild guild = e.getGuildById(k);
						for(Subscription subscription : v) {
							if(guild != null) {
								var subscriptionChannel = Azrael.SQLgetChannels(subscription.getGuildId()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.SUB.getType())).findAny().orElse(null);
								if(subscriptionChannel != null || subscriptions.parallelStream().filter(f -> f.getChannelID() > 0).findAny().orElse(null) != null) {
									long channel_id;
									boolean defaultChannel = true;
									TextChannel textChannel = guild.getTextChannelById(subscription.getChannelID()); 
									if(textChannel != null) {
										channel_id = textChannel.getIdLong();
										defaultChannel = false;
									}
									else if(subscriptionChannel != null) {
										channel_id = subscriptionChannel.getChannel_ID();
									}
									else {
										continue;
									}
									boolean success = false;
									try {
										logger.trace("Retrieving subscription {} in guild {}", subscription.getURL(), e.getGuildById(guild.getIdLong()).getId());
										if(subscription.getType() == 1)
											success = RSSModel.ModelParse(STATIC.retrieveWebPageCode(subscription.getURL()), guild, subscription, channel_id, defaultChannel);
										else if(subscription.getType() == 2)
											success = TwitterModel.ModelParse(guild, subscription, channel_id, defaultChannel);
										else if(subscription.getType() == 3)
											success = RedditModel.ModelParse(guild, subscription, channel_id, defaultChannel);
										else if(subscription.getType() == 4) 
											success = YouTubeModel.ModelParse(guild, subscription, channel_id, defaultChannel);
										else if(subscription.getType() == 5)
											success = TwitchModel.ModelParse(guild, subscription, channel_id, defaultChannel);
									} catch(SocketTimeoutException e1){
										logger.warn("Timeout on subscription {}", subscription.getURL());
										success = false;
									} catch (Exception e1) {
										logger.error("Error on retrieving subscription {}", subscription.getURL(), e1);
										success = false;
									}
									if(success)
										Hashes.addSubscriptionStatus(guild.getId()+"_"+subscription.getURL(), 0);
									else
										incrementSubscriptionStatus(guild, subscription);
								}
							}
							else {
								logger.warn("Guild {} not found while fetching subscriptions", k);
							}
						}
					}).start();
				});
			}
			else {
				logger.info("Subscription task interrupted for not having any subscriptions in any guild");
				if(timer != null) {
					timer = null;
				}
				this.cancel();
			}
		} catch(NullPointerException npe) {
			logger.error("Subscriptions couldn't be fetched", npe);
		}
	}
	
	public static void runTask(JDA e) {
		if(timer == null) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			
			Timer timer = new Timer("Subscriptions");
			ParseSubscription.timer = timer;
			timer.schedule(new ParseSubscription(e), calendar.getTime(), TimeUnit.MINUTES.toMillis(10));
		}
	}
	
	public static boolean timerIsRunning() {
		return (timer != null);
	}
	
	public static void deactivateTimer() {
		timer.cancel();
		timer = null;
	}
	
	private static void incrementSubscriptionStatus(Guild guild, Subscription subscription) {
		Integer count = Hashes.getSubscriptionStatus(guild.getId()+"_"+subscription.getURL());
		if(count == null)
			count = 1;
		else
			count ++;
		//one full day, if we assume subscriptions are fetched every 10 minutes
		if(count == 144) {
			STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.ORANGE), STATIC.getTranslation2(guild, Translation.SUBSCRIBE_STATUS).replace("{}", subscription.getURL()), Channel.LOG.getType());
			logger.info("Unreachable subscription {} for the last 24 hours in guild {}", subscription, guild.getId());
			Hashes.addSubscriptionStatus(guild.getId()+"_"+subscription.getURL(), 0);
		}
		else
			Hashes.addSubscriptionStatus(guild.getId()+"_"+subscription.getURL(), count);
	}
}
