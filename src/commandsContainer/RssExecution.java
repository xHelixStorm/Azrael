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
		if(STATIC.getTwitterFactory() != null) {
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
				Hashes.clearFeeds();
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
			Hashes.clearFeeds();
			e.getChannel().sendMessage(message.setColor(Color.BLUE).setDescription("The format has been updated").build()).queue();
			logger.debug("{} has updated the format of an rss feed in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
		}
		else {
			e.getChannel().sendMessage(message.setColor(Color.RED).setDescription("An internal error occurred. Format couldn't be updated").build()).queue();
			logger.error("Format couldn't be updated for the url {} in guild {}", rss.get(feed).getURL(), e.getGuild().getId());
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
