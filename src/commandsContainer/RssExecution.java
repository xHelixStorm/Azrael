package commandsContainer;

import java.awt.Color;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiParser;

import constructors.Cache;
import constructors.RSS;
import core.Hashes;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import rss.BasicModel;
import rss.TwitterModel;
import sql.Azrael;
import util.STATIC;

public class RssExecution {
	private static final Logger logger = LoggerFactory.getLogger(RssExecution.class);
	private static final EmbedBuilder message = new EmbedBuilder();
	
	public static void registerFeed(GuildMessageReceivedEvent e, String feed, int type) {
		if(type == 1) {
			if(Azrael.SQLInsertRSS(feed, e.getGuild().getIdLong(), type) > 0) {
				message.setColor(Color.BLUE);
				e.getChannel().sendMessage(message.setDescription("RSS has been registered").build()).queue();
				Hashes.clearFeeds();
				logger.debug("{} RSS link has been registered for guild {}", feed, e.getGuild().getId());
			}
			else {
				message.setColor(Color.RED);
				e.getChannel().sendMessage(message.setDescription("RSS link couldn't be registered. Either the link has been already registered or an internal error occurred").build()).queue();
				logger.error("{} RSS link couldn't be registered for guild {}", feed, e.getGuild().getId());
			}
		}
		else if(STATIC.getTwitterFactory() != null) {
			if(Azrael.SQLInsertRSS(feed, e.getGuild().getIdLong(), type) > 0) {
				message.setColor(Color.BLUE);
				e.getChannel().sendMessage(message.setDescription("Tweet has been registered").build()).queue();
				Hashes.clearFeeds();
				logger.debug("{} hashtag has been registered for guild {}", feed, e.getGuild().getId());
			}
			else {
				message.setColor(Color.RED);
				e.getChannel().sendMessage(message.setDescription("Hashtag couldn't be registered. Either the hashtag has been already registered or an internal error occurred").build()).queue();
				logger.error("{} hashtag couldn't be registered for guild {}", feed, e.getGuild().getId());
			}
		}
		else {
			message.setColor(Color.RED);
			e.getChannel().sendMessage(message.setDescription("A tweet RSS can't be registered as long a Twitter Bot hasn't been created and configured from within the config.ini file!").build()).queue();
		}
	}
	
	public static void removeFeed(GuildMessageReceivedEvent e, int feed) {
		ArrayList<RSS> rss = Azrael.SQLgetRSSFeeds(e.getGuild().getIdLong());
		if(rss.size() >= feed+1) {
			String url = rss.get(feed).getURL();
			Hashes.clearTempCache("rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId());
			if(Azrael.SQLDeleteRSSFeed(url, e.getGuild().getIdLong()) > 0) {
				Hashes.removeFeeds(e.getGuild().getIdLong());
				e.getChannel().sendMessage(message.setColor(Color.BLUE).setDescription("Feed has been succesfully removed").build()).queue();
				logger.debug("{} rss feed has been deleted from guild {}", url, e.getGuild().getId());
			}
			else {
				e.getChannel().sendMessage(message.setColor(Color.RED).setDescription("An internal error occurred. The rss feed couldn't be removed. Please confirm if the feed isn't already removed").build()).queue();
				logger.error("{} rss feed couldn't be removed from guild {}", url, e.getGuild().getId());
			}
		}
		else {
			e.getChannel().sendMessage(message.setColor(Color.RED).setDescription("Please select an available digit").build()).queue();
		}
	}
	
	public static void currentFormat(GuildMessageReceivedEvent e, int feed, String key) {
		ArrayList<RSS> rss = Azrael.SQLgetRSSFeeds(e.getGuild().getIdLong());
		if(rss.size() >= feed+1) {
			Hashes.addTempCache(key, new Cache(180000, "updateformat"+feed));
			if(rss.get(feed).getType() == 1)
				e.getChannel().sendMessage("This is the current setting for this feed. Type your desired template for this feed or type exit to interrupt. Key values are `{pubDate}`, `{title}`, `{description}`, `{link}`\n```"+rss.get(feed).getFormat()+"```").queue();
			else if(rss.get(feed).getType() == 2)
				e.getChannel().sendMessage("This is the current setting for this hashtag. Type your desired template for this feed or type exit to interrupt. Key values are `{pubDate}`, `{fullName}`, `{username}`, `{description}`\n```"+rss.get(feed).getFormat()+"```").queue();
		}
		else {
			e.getChannel().sendMessage(message.setColor(Color.RED).setDescription("Please select an available digit").build()).queue();
		}
	}
	
	public static void updateFormat(GuildMessageReceivedEvent e, int feed, String format) {
		ArrayList<RSS> rss = Azrael.SQLgetRSSFeeds(e.getGuild().getIdLong());
		if(Azrael.SQLUpdateRSSFormat(rss.get(feed).getURL(), e.getGuild().getIdLong(), EmojiParser.parseToAliases(format)) > 0) {
			Hashes.removeFeeds(e.getGuild().getIdLong());
			e.getChannel().sendMessage(message.setColor(Color.BLUE).setDescription("The format has been updated").build()).queue();
			logger.debug("{} has updated the format of an rss feed in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
		}
		else {
			e.getChannel().sendMessage(message.setColor(Color.RED).setDescription("An internal error occurred. Format couldn't be updated").build()).queue();
			logger.error("Format couldn't be updated for the url {} in guild {}", rss.get(feed).getURL(), e.getGuild().getId());
		}
	}
	
	public static void changeOptions(GuildMessageReceivedEvent e, int feed, String key) {
		ArrayList<RSS> tweets = Azrael.SQLgetRSSFeeds(e.getGuild().getIdLong(), 2);
		if(tweets.size() >= feed+1) {
			Hashes.addTempCache(key, new Cache(180000, "options-page", ""+feed));
			RSS tweet = tweets.get(feed);
			StringBuilder out = new StringBuilder();
			for(final String childTweet : tweet.getChildTweets()) {
				out.append(childTweet+" ");
			}
			e.getChannel().sendMessage(message.setColor(Color.BLUE).setDescription("Options for **"+tweet.getURL()+"**\n\n"
					+ "Display pictures: **"+(tweet.getPictures() ? "enabled" : "disabled")+"**\n"
					+ "Display videos: **"+(tweet.getVideos() ? "enabled" : "disabled")+"**\n"
					+ "Show Tweet only message: **"+(tweet.getText() ? "enabled" : "disabled")+"**\n"
					+ "Child hashtags: **"+(out.length() > 0 ? out.toString() : "no child hashtags available")+"**\n\n"
					+ "Type **enable/disable pictures/videos/text** to either enable or disable one of these options.\n"
					+ "Also, type **add-child/remove-child #<hashtag>** to combine a hashtag with another hashtag or to remove the binding.\n"
					+ "Once complete, type **exit** to complete the set up").build()).queue();
		}
	}
	
	public static void updateOptions(GuildMessageReceivedEvent e, int feed, String key) {
		ArrayList<RSS> tweets = Azrael.SQLgetRSSFeeds(e.getGuild().getIdLong(), 2);
		RSS tweet = tweets.get(feed);
		final String lcMessage = e.getMessage().getContentRaw().toLowerCase();
		boolean printMessage = false;
		if(lcMessage.equalsIgnoreCase("enable pictures")) {
			if(Azrael.SQLUpdateRSSPictures(tweet.getURL(), e.getGuild().getIdLong(), true) > 0) {
				logger.debug("Pictures enabled for tweet {} in guild {}", tweet.getURL(), e.getGuild().getId());
				Hashes.addTempCache(key, new Cache(180000, "options-page", ""+feed));
				printMessage = true;
			}
			else {
				e.getChannel().sendMessage("An internal error occurred! Option couldn't be updated!").queue();
				logger.error("Pictures couldn't be enabled for tweet {} in guild {}", tweet.getURL(), e.getGuild().getId());
				Hashes.clearTempCache(key);
			}
		}
		else if(lcMessage.equalsIgnoreCase("enable videos")) {
			if(Azrael.SQLUpdateRSSVideos(tweet.getURL(), e.getGuild().getIdLong(), true) > 0) {
				logger.debug("Videos enabled for tweet {} in guild {}", tweet.getURL(), e.getGuild().getId());
				Hashes.addTempCache(key, new Cache(180000, "options-page", ""+feed));
				printMessage = true;
			}
			else {
				e.getChannel().sendMessage("An internal error occurred! Option couldn't be updated!").queue();
				logger.error("Videos couldn't be enabled for tweet {} in guild {}", tweet.getURL(), e.getGuild().getId());
				Hashes.clearTempCache(key);
			}
		}
		else if(lcMessage.equalsIgnoreCase("enable text")) {
			if(Azrael.SQLUpdateRSSText(tweet.getURL(), e.getGuild().getIdLong(), true) > 0) {
				logger.debug("Text enabled for tweet {} in guild {}", tweet.getURL(), e.getGuild().getId());
				Hashes.addTempCache(key, new Cache(180000, "options-page", ""+feed));
				printMessage = true;
			}
			else {
				e.getChannel().sendMessage("An internal error occurred! Option couldn't be updated!").queue();
				logger.error("Text couldn't be enabled for tweet {} in guild {}", tweet.getURL(), e.getGuild().getId());
				Hashes.clearTempCache(key);
			}
		}
		else if(lcMessage.equalsIgnoreCase("disable pictures")) {
			if(Azrael.SQLUpdateRSSPictures(tweet.getURL(), e.getGuild().getIdLong(), false) > 0) {
				logger.debug("Pictures disabled for tweet {} in guild {}", tweet.getURL(), e.getGuild().getId());
				Hashes.addTempCache(key, new Cache(180000, "options-page", ""+feed));
				printMessage = true;
			}
			else {
				e.getChannel().sendMessage("An internal error occurred! Option couldn't be updated!").queue();
				logger.error("Pictures couldn't be disabled for tweet {} in guild {}", tweet.getURL(), e.getGuild().getId());
				Hashes.clearTempCache(key);
			}
		}
		else if(lcMessage.equalsIgnoreCase("disable videos")) {
			if(Azrael.SQLUpdateRSSVideos(tweet.getURL(), e.getGuild().getIdLong(), false) > 0) {
				logger.debug("Videos disabled for tweet {} in guild {}", tweet.getURL(), e.getGuild().getId());
				Hashes.addTempCache(key, new Cache(180000, "options-page", ""+feed));
				printMessage = true;
			}
			else {
				e.getChannel().sendMessage("An internal error occurred! Option couldn't be updated!").queue();
				logger.error("Videos couldn't be disabled for tweet {} in guild {}", tweet.getURL(), e.getGuild().getId());
				Hashes.clearTempCache(key);
			}
		}
		else if(lcMessage.equalsIgnoreCase("disable text")) {
			if(Azrael.SQLUpdateRSSText(tweet.getURL(), e.getGuild().getIdLong(), false) > 0) {
				logger.debug("Text disabled for tweet {} in guild {}", tweet.getURL(), e.getGuild().getId());
				Hashes.addTempCache(key, new Cache(180000, "options-page", ""+feed));
				printMessage = true;
			}
			else {
				e.getChannel().sendMessage("An internal error occurred! Option couldn't be updated!").queue();
				logger.error("Text couldn't be disabled for tweet {} in guild {}", tweet.getURL(), e.getGuild().getId());
				Hashes.clearTempCache(key);
			}
		}
		else if(lcMessage.startsWith("add-child") || lcMessage.startsWith("remove-child")) {
			if(lcMessage.matches("add-child #[a-z0-9]{1,}[^\\s]*")) {
				printMessage = true;
				final String hashtag = lcMessage.substring(10);
				if(Azrael.SQLInsertChildTweet(tweet.getURL(), hashtag, e.getGuild().getIdLong()) > 0) {
					logger.debug("Child hashtag {} was added for parent hashtag {} in guild {}", hashtag, tweet.getURL(), e.getGuild().getId());
				}
				else {
					e.getChannel().sendMessage("child hashtag is already bound to "+tweet.getURL()).queue();
				}
			}
			else if(lcMessage.matches("remove-child #[a-z0-9]{1,}[^\\s]*")) {
				printMessage = true;
				final String hashtag = lcMessage.substring(13);
				if(Azrael.SQLDeleteChildTweet(tweet.getURL(), hashtag, e.getGuild().getIdLong()) > 0) {
					logger.debug("Child hashtag {} was removed for parent hashtag {} in guild {}", hashtag, tweet.getURL(), e.getGuild().getId());
				}
				else {
					e.getChannel().sendMessage("child hashtag is already not bound to "+tweet.getURL()).queue();
				}
			}
			else {
				e.getChannel().sendMessage("Please write only one hashtag together with the parameter").queue();
			}
		}
		
		if(printMessage) {
			Hashes.removeFeeds(e.getGuild().getIdLong());
			tweet = Azrael.SQLgetRSSFeeds(e.getGuild().getIdLong(), 2).get(feed);
			StringBuilder out = new StringBuilder();
			for(final String childTweet : tweet.getChildTweets()) {
				out.append(childTweet+" ");
			}
			e.getChannel().sendMessage(message.setColor(Color.BLUE).setDescription("Options for **"+tweet.getURL()+"**\n\n"
					+ "Display pictures: **"+(tweet.getPictures() ? "enabled" : "disabled")+"**\n"
					+ "Display videos: **"+(tweet.getVideos() ? "enabled" : "disabled")+"**\n"
					+ "Show Tweet only message: **"+(tweet.getText() ? "enabled" : "disabled")+"**\n"
					+ "Child hashtags: **"+(out.length() > 0 ? out.toString() : "no child hashtags available")+"**\n\n"
					+ "Type **enable/disable pictures/videos/text** to either enable or disable one of these options.\n"
					+ "Also, type **add-child/remove-child #<hashtag>** to combine a hashtag with another hashtag or to remove the binding.\n"
					+ "Once complete, type **exit** to complete the set up").build()).queue();
		}
	}
	
	public static void runTest(GuildMessageReceivedEvent e, int feed) {
		ArrayList<RSS> rss = Azrael.SQLgetRSSFeeds(e.getGuild().getIdLong());
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
			e.getChannel().sendMessage(message.setColor(Color.RED).setDescription("Please select an available digit").build()).queue();
		}
	}
}
