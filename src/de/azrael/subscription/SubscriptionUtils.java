package de.azrael.subscription;

import java.time.ZonedDateTime;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Messages;
import de.azrael.core.Hashes;
import de.azrael.sql.Azrael;
import de.azrael.sql.BotConfiguration;
import de.azrael.timerTask.ParseSubscription;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;

public class SubscriptionUtils {
	private final static Logger logger = LoggerFactory.getLogger(SubscriptionUtils.class);
	
	/**
	 * If a subscription channel has been removed during down time, remove it as well from the database
	 * @param subscriptionChannel
	 * @param defaultChannel
	 * @param subscription
	 * @param guild
	 */
	public static void deleteRemovedChannel(long subscriptionChannel, boolean defaultChannel, String subscription, Guild guild) {
		if(Azrael.SQLDeleteChannelConf(subscriptionChannel, guild.getIdLong()) > 0) {
			Azrael.SQLDeleteChannel_Filter(subscriptionChannel);
			Azrael.SQLDeleteChannels(subscriptionChannel);
			if(defaultChannel) {
				logger.info("Not existing subscription channel {} has been removed in guild {}", subscriptionChannel, guild.getIdLong());
			}
			else if(Azrael.SQLUpdateSubscriptionChannel(subscription, guild.getIdLong(), 0) > 0) {
				logger.info("Not existing alternative subscription channel {} has been removed in guild {}", subscriptionChannel, guild.getIdLong());
			}
			Hashes.removeFilterLang(subscriptionChannel);
			Hashes.removeChannels(guild.getIdLong());
		}
		else if(Azrael.SQLUpdateSubscriptionChannel(subscription, guild.getIdLong(), 0) > 0) {
			logger.info("Not existing alternative subscription channel {} has been removed in guild {}", subscriptionChannel, guild.getIdLong());
		}
	}
	
	/**
	 * Identify if the subscription collector timer is already running and if not, start it
	 * @param jda
	 */
	public static void startTimer(JDA jda) {
		if(Hashes.getSubscriptionSize() == 0 && !ParseSubscription.timerIsRunning()) {
			Hashes.clearSubscriptions();
			ParseSubscription.runTask(jda);
		}
		else
			Hashes.clearSubscriptions();
	}
	
	/**
	 * Post subscription message into a text channel and log to message cache
	 * @param guild
	 * @param textChannel
	 * @param outMessage
	 * @param subscriptionId
	 * @param username
	 */
	public static void postSubscriptionToServerChannel(Guild guild, TextChannel textChannel, String outMessage, String subscriptionId, String username) {
		MessageHistory history = new MessageHistory(textChannel);
		history.retrievePast(100).queue(historyList -> {
			Message historyMessage = historyList.parallelStream().filter(f -> f.getContentRaw().replaceAll("[^a-zA-Z]", "").contains(outMessage.replaceAll("[^a-zA-Z]", ""))).findAny().orElse(null);
			if(historyMessage == null)
				textChannel.sendMessage(outMessage).queue(m -> {
					Azrael.SQLInsertSubscriptionLog(m.getIdLong(), subscriptionId);
					if(BotConfiguration.SQLgetBotConfigs(guild.getIdLong()).getCacheLog()) {
						Messages collectedMessage = new Messages();
						collectedMessage.setUserID(0);
						collectedMessage.setUsername(username);
						collectedMessage.setGuildID(guild.getIdLong());
						collectedMessage.setChannelID(textChannel.getIdLong());
						collectedMessage.setChannelName(textChannel.getName());
						collectedMessage.setMessage(outMessage);
						collectedMessage.setMessageID(m.getIdLong());
						collectedMessage.setTime(ZonedDateTime.now());
						collectedMessage.setIsEdit(false);
						collectedMessage.setIsUserBot(true);
						ArrayList<Messages> cacheMessage = new ArrayList<Messages>();
						cacheMessage.add(collectedMessage);
						Hashes.addMessagePool(guild.getIdLong(), m.getIdLong(), cacheMessage);
					}
				});
		});
	}
}
