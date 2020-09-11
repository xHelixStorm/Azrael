package commandsContainer;

import java.awt.Color;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiParser;

import constructors.Cache;
import constructors.RSS;
import core.Hashes;
import enums.Translation;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import rss.BasicModel;
import rss.TwitterModel;
import sql.Azrael;
import timerTask.ParseSubscription;
import util.STATIC;

/**
 * Addition to the subscribe command
 * @author xHelixStorm
 *
 */

public class SubscribeExecution {
	private static final Logger logger = LoggerFactory.getLogger(SubscribeExecution.class);
	
	public static void registerFeed(GuildMessageReceivedEvent e, String feed, int type) {
		EmbedBuilder message = new EmbedBuilder();
		if(type == 1) {
			final int result = Azrael.SQLInsertRSS(feed, e.getGuild().getIdLong(), type);
			if(result > 0) {
				message.setColor(Color.BLUE);
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_RSS_ADDED)).build()).queue();
				if(Hashes.getFeedsSize(e.getGuild().getIdLong()) == 0 && !ParseSubscription.timerIsRunning(e.getGuild().getIdLong()))
					ParseSubscription.runTask(e.getJDA(), e.getGuild().getIdLong());
				Hashes.clearFeeds();
				logger.debug("{} RSS link has been registered for guild {}", feed, e.getGuild().getId());
			}
			else if(result == 0) {
				message.setColor(Color.RED);
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_ALREADY_DONE)).build()).queue();
			}
			else {
				message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("{} RSS link couldn't be registered for guild {}", feed, e.getGuild().getId());
			}
		}
		else if(STATIC.getTwitterFactory() != null) {
			final int result = Azrael.SQLInsertRSS(feed, e.getGuild().getIdLong(), type); 
			if(result > 0) {
				message.setColor(Color.BLUE);
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_HASH_ADDED)).build()).queue();
				if(Hashes.getFeedsSize(e.getGuild().getIdLong()) == 0 && !ParseSubscription.timerIsRunning(e.getGuild().getIdLong()))
					ParseSubscription.runTask(e.getJDA(), e.getGuild().getIdLong());
				Hashes.clearFeeds();
				logger.debug("{} Hashtag has been registered for guild {}", feed, e.getGuild().getId());
			}
			else if(result == 0) {
				message.setColor(Color.RED);
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_ALREADY_DONE)).build()).queue();
			}
			else {
				message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("{} Hashtag couldn't be registered for guild {}", feed, e.getGuild().getId());
			}
		}
		else {
			message.setColor(Color.RED);
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_LOGIN_TWITTER)).build()).queue();
		}
	}
	
	public static void removeFeed(GuildMessageReceivedEvent e, int feed) {
		EmbedBuilder message = new EmbedBuilder();
		ArrayList<RSS> rss = Azrael.SQLgetSubscriptions(e.getGuild().getIdLong());
		if(rss.size() >= feed+1) {
			String url = rss.get(feed).getURL();
			Hashes.clearTempCache("rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
			if(Azrael.SQLDeleteRSSFeed(url, e.getGuild().getIdLong()) > 0) {
				Hashes.removeFeeds(e.getGuild().getIdLong());
				Hashes.removeSubscriptionStatus(e.getGuild().getId()+"_"+url);
				e.getChannel().sendMessage(message.setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_REMOVED)).build()).queue();
				logger.debug("{} rss feed has been deleted from guild {}", url, e.getGuild().getId());
			}
			else {
				message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("{} rss feed couldn't be removed from guild {}", url, e.getGuild().getId());
			}
		}
		else {
			e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_DISPLAYED_NUM)).build()).queue();
		}
	}
	
	public static void currentFormat(GuildMessageReceivedEvent e, int feed, String key) {
		ArrayList<RSS> rss = Azrael.SQLgetSubscriptions(e.getGuild().getIdLong());
		if(rss.size() >= feed+1) {
			Hashes.addTempCache(key, new Cache(180000, "updateformat"+feed));
			if(rss.get(feed).getType() == 1)
				e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_FORMAT_RSS)+rss.get(feed).getFormat()+"```").queue();
			else if(rss.get(feed).getType() == 2)
				e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_FORMAT_HASH)+rss.get(feed).getFormat()+"```").queue();
		}
		else {
			EmbedBuilder message = new EmbedBuilder();
			e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_DISPLAYED_NUM)).build()).queue();
		}
	}
	
	public static void updateFormat(GuildMessageReceivedEvent e, int feed, String format) {
		EmbedBuilder message = new EmbedBuilder();
		ArrayList<RSS> rss = Azrael.SQLgetSubscriptions(e.getGuild().getIdLong());
		if(Azrael.SQLUpdateRSSFormat(rss.get(feed).getURL(), e.getGuild().getIdLong(), EmojiParser.parseToAliases(format)) > 0) {
			Hashes.removeFeeds(e.getGuild().getIdLong());
			e.getChannel().sendMessage(message.setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_FORMAT_UPDATED)).build()).queue();
			logger.debug("{} has updated the format of an rss feed in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
		}
		else {
			e.getChannel().sendMessage(message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Format couldn't be updated for the url {} in guild {}", rss.get(feed).getURL(), e.getGuild().getId());
		}
	}
	
	public static void changeOptions(GuildMessageReceivedEvent e, int feed, String key) {
		EmbedBuilder message = new EmbedBuilder();
		ArrayList<RSS> tweets = Azrael.SQLgetSubscriptions(e.getGuild().getIdLong(), 2);
		if(tweets.size() >= feed+1) {
			Hashes.addTempCache(key, new Cache(180000, "options-page", ""+feed));
			RSS tweet = tweets.get(feed);
			StringBuilder out = new StringBuilder();
			for(final String childTweet : tweet.getChildTweets()) {
				out.append(childTweet+" ");
			}
			e.getChannel().sendMessage(message.setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_OPTIONS_1).replace("{}", tweet.getURL())
					+ STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_OPTIONS_2).replace("{}", (tweet.getPictures() ? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_IS_ENABLED) : STATIC.getTranslation(e.getMember(), Translation.DISPLAY_IS_NOT_ENABLED)))
					+ STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_OPTIONS_3).replace("{}", (tweet.getVideos() ? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_IS_ENABLED) : STATIC.getTranslation(e.getMember(), Translation.DISPLAY_IS_NOT_ENABLED)))
					+ STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_OPTIONS_4).replace("{}", (tweet.getText() ? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_IS_ENABLED) : STATIC.getTranslation(e.getMember(), Translation.DISPLAY_IS_NOT_ENABLED)))
					+ STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_OPTIONS_5).replace("{}", (out.length() > 0 ? out.toString() : STATIC.getTranslation(e.getMember(), Translation.NOT_AVAILABLE)))
					+ STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_OPTIONS_6)).build()).queue();
		}
	}
	
	public static void updateOptions(GuildMessageReceivedEvent e, int feed, String key) {
		ArrayList<RSS> tweets = Azrael.SQLgetSubscriptions(e.getGuild().getIdLong(), 2);
		RSS tweet = tweets.get(feed);
		final String lcMessage = e.getMessage().getContentRaw().toLowerCase();
		boolean printMessage = false;
		if(lcMessage.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE_PICTURES))) {
			if(Azrael.SQLUpdateRSSPictures(tweet.getURL(), e.getGuild().getIdLong(), true) > 0) {
				logger.debug("Pictures enabled for tweet {} in guild {}", tweet.getURL(), e.getGuild().getId());
				Hashes.addTempCache(key, new Cache(180000, "options-page", ""+feed));
				printMessage = true;
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Pictures couldn't be enabled for tweet {} in guild {}", tweet.getURL(), e.getGuild().getId());
				Hashes.clearTempCache(key);
			}
		}
		else if(lcMessage.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE_VIDEOS))) {
			if(Azrael.SQLUpdateRSSVideos(tweet.getURL(), e.getGuild().getIdLong(), true) > 0) {
				logger.debug("Videos enabled for tweet {} in guild {}", tweet.getURL(), e.getGuild().getId());
				Hashes.addTempCache(key, new Cache(180000, "options-page", ""+feed));
				printMessage = true;
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Videos couldn't be enabled for tweet {} in guild {}", tweet.getURL(), e.getGuild().getId());
				Hashes.clearTempCache(key);
			}
		}
		else if(lcMessage.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE_TEXT))) {
			if(Azrael.SQLUpdateRSSText(tweet.getURL(), e.getGuild().getIdLong(), true) > 0) {
				logger.debug("Text enabled for tweet {} in guild {}", tweet.getURL(), e.getGuild().getId());
				Hashes.addTempCache(key, new Cache(180000, "options-page", ""+feed));
				printMessage = true;
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Text couldn't be enabled for tweet {} in guild {}", tweet.getURL(), e.getGuild().getId());
				Hashes.clearTempCache(key);
			}
		}
		else if(lcMessage.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISABLE_PICTURES))) {
			if(Azrael.SQLUpdateRSSPictures(tweet.getURL(), e.getGuild().getIdLong(), false) > 0) {
				logger.debug("Pictures disabled for tweet {} in guild {}", tweet.getURL(), e.getGuild().getId());
				Hashes.addTempCache(key, new Cache(180000, "options-page", ""+feed));
				printMessage = true;
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Pictures couldn't be disabled for tweet {} in guild {}", tweet.getURL(), e.getGuild().getId());
				Hashes.clearTempCache(key);
			}
		}
		else if(lcMessage.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISABLE_VIDEOS))) {
			if(Azrael.SQLUpdateRSSVideos(tweet.getURL(), e.getGuild().getIdLong(), false) > 0) {
				logger.debug("Videos disabled for tweet {} in guild {}", tweet.getURL(), e.getGuild().getId());
				Hashes.addTempCache(key, new Cache(180000, "options-page", ""+feed));
				printMessage = true;
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Videos couldn't be disabled for tweet {} in guild {}", tweet.getURL(), e.getGuild().getId());
				Hashes.clearTempCache(key);
			}
		}
		else if(lcMessage.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISABLE_TEXT))) {
			if(Azrael.SQLUpdateRSSText(tweet.getURL(), e.getGuild().getIdLong(), false) > 0) {
				logger.debug("Text disabled for tweet {} in guild {}", tweet.getURL(), e.getGuild().getId());
				Hashes.addTempCache(key, new Cache(180000, "options-page", ""+feed));
				printMessage = true;
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Text couldn't be disabled for tweet {} in guild {}", tweet.getURL(), e.getGuild().getId());
				Hashes.clearTempCache(key);
			}
		}
		else if(lcMessage.startsWith(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_CHILD)) || lcMessage.startsWith(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE_CHILD))) {
			if(lcMessage.matches(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_CHILD)+" #[a-z0-9]{1,}[^\\s]*")) {
				printMessage = true;
				final String hashtag = lcMessage.substring(10);
				final int result = Azrael.SQLInsertChildTweet(tweet.getURL(), hashtag, e.getGuild().getIdLong()); 
				if(result > 0) {
					logger.debug("Child hashtag {} was added for parent hashtag {} in guild {}", hashtag, tweet.getURL(), e.getGuild().getId());
				}
				else if(result == 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_OPTION_BOUND)+tweet.getURL()).build()).queue();
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Child hashtags couldn't be added for tweet {} in guild {}", tweet.getURL(), e.getGuild().getId());
				}
			}
			else if(lcMessage.matches(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE_CHILD)+" #[a-z0-9]{1,}[^\\s]*")) {
				printMessage = true;
				final String hashtag = lcMessage.substring(13);
				final int result = Azrael.SQLDeleteChildTweet(tweet.getURL(), hashtag, e.getGuild().getIdLong());
				if(result > 0) {
					logger.debug("Child hashtag {} was removed for parent hashtag {} in guild {}", hashtag, tweet.getURL(), e.getGuild().getId());
				}
				else if(result == 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_OPTION_NOT_BOUND)+tweet.getURL()).build()).queue();
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Child hashtags couldn't be removed for tweet {} in guild {}", tweet.getURL(), e.getGuild().getId());
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
			}
		}
		
		if(printMessage) {
			EmbedBuilder message = new EmbedBuilder();
			Hashes.removeFeeds(e.getGuild().getIdLong());
			tweet = Azrael.SQLgetSubscriptions(e.getGuild().getIdLong(), 2).get(feed);
			StringBuilder out = new StringBuilder();
			for(final String childTweet : tweet.getChildTweets()) {
				out.append(childTweet+" ");
			}
			e.getChannel().sendMessage(message.setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_OPTIONS_1).replace("{}", tweet.getURL())
					+ STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_OPTIONS_2).replace("{}", (tweet.getPictures() ? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_IS_ENABLED) : STATIC.getTranslation(e.getMember(), Translation.DISPLAY_IS_NOT_ENABLED)))
					+ STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_OPTIONS_3).replace("{}", (tweet.getVideos() ? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_IS_ENABLED) : STATIC.getTranslation(e.getMember(), Translation.DISPLAY_IS_NOT_ENABLED)))
					+ STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_OPTIONS_4).replace("{}", (tweet.getText() ? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_IS_ENABLED) : STATIC.getTranslation(e.getMember(), Translation.DISPLAY_IS_NOT_ENABLED)))
					+ STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_OPTIONS_5).replace("{}", (out.length() > 0 ? out.toString() : STATIC.getTranslation(e.getMember(), Translation.NOT_AVAILABLE)))
					+ STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_OPTIONS_6)).build()).queue();
		}
	}
	
	public static void setChannel(GuildMessageReceivedEvent e, int feed, String key) {
		EmbedBuilder message = new EmbedBuilder();
		ArrayList<RSS> subscriptions = Azrael.SQLgetSubscriptions(e.getGuild().getIdLong());
		if(subscriptions.size() >= feed+1) {
			final var subscription = subscriptions.get(feed);
			Hashes.addTempCache(key, new Cache(180000, "set-channel", ""+feed));
			e.getChannel().sendMessage(message.setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_CHANNEL_ADD).replace("{}", (subscription.getChannelID() > 0 ? "<#"+subscription.getChannelID()+">" : STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_CHANNEL_DEFAULT)))).build()).queue();
		}
	}
	
	public static void updateAlternativeChannel(GuildMessageReceivedEvent e, int feed, String key) {
		EmbedBuilder message = new EmbedBuilder();
		String channel_id = e.getMessage().getContentRaw().replaceAll("[<>#]*", "");
		if(channel_id.replaceAll("[0-9]*", "").length() == 0) {
			TextChannel textChannel = e.getGuild().getTextChannelById(e.getMessage().getContentRaw().replaceAll("[<>#]*", ""));
			if(textChannel != null) {
				ArrayList<RSS> subscriptions = Azrael.SQLgetSubscriptions(e.getGuild().getIdLong());
				RSS subscription = subscriptions.get(feed);
				final var result = Azrael.SQLUpdateRSSChannel(subscription.getURL(), e.getGuild().getIdLong(), textChannel.getIdLong());
				if(result > 0) {
					e.getChannel().sendMessage(message.setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_CHANNEL_ADDED)).build()).queue();
					Hashes.removeFeeds(e.getGuild().getIdLong());
					Hashes.clearTempCache(key);
				}
				else {
					e.getChannel().sendMessage(message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					Hashes.clearTempCache(key);
				}
			}
			else {
				e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_CHANNEL_ERR_2)).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_CHANNEL_ERR_2)).build()).queue();
		}
	}
	
	public static void runTest(GuildMessageReceivedEvent e, int feed) {
		ArrayList<RSS> rss = Azrael.SQLgetSubscriptions(e.getGuild().getIdLong());
		if(rss.size() >= feed+1) {
			Hashes.clearTempCache("rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
			final var foundRSS = rss.get(feed);
			if(foundRSS.getType() == 1)
				BasicModel.ModelTest(e, foundRSS);
			else if(foundRSS.getType() == 2) {
				TwitterModel.ModelTest(e, foundRSS);
			}
		}
		else {
			EmbedBuilder message = new EmbedBuilder();
			e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_DISPLAYED_NUM)).build()).queue();
		}
	}
}
