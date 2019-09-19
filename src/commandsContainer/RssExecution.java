package commandsContainer;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiParser;

import constructors.Cache;
import constructors.RSS;
import core.Hashes;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;

public class RssExecution {
	private static final Logger logger = LoggerFactory.getLogger(RssExecution.class);
	private static final EmbedBuilder message = new EmbedBuilder();
	
	public static void registerFeed(GuildMessageReceivedEvent e, String feed, int type) {
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
	
	public static boolean removeFeed(GuildMessageReceivedEvent e, int feed) {
		ArrayList<RSS> rss = Hashes.getFeed(e.getGuild().getIdLong());
		if(rss.size() >= feed+1) {
			String url = rss.get(feed).getURL();
			if(Azrael.SQLDeleteRSSFeed(url, e.getGuild().getIdLong()) > 0) {
				Hashes.clearFeeds();
				e.getChannel().sendMessage(message.setColor(Color.BLUE).setDescription("Feed has been succesfully removed").build()).queue();
				logger.debug("{} rss feed has been deleted from guild {}", url, e.getGuild().getId());
				return true;
			}
			else {
				e.getChannel().sendMessage(message.setColor(Color.RED).setDescription("An internal error occurred. The rss feed couldn't be removed. Please confirm if the feed isn't already removed").build()).queue();
				logger.error("{} rss feed couldn't be removed from guild {}", url, e.getGuild().getId());
				return false;
			}
		}
		else {
			e.getChannel().sendMessage(message.setColor(Color.RED).setDescription("Please select an available digit").build()).queue();
			return false;
		}
	}
	
	public static void currentFormat(GuildMessageReceivedEvent e, int feed, String key) {
		ArrayList<RSS> rss = Hashes.getFeed(e.getGuild().getIdLong());
		if(rss.size() >= feed+1) {
			if(rss.get(feed).getType() == 1) {
				e.getChannel().sendMessage("This is the curring setting for this feed. Type your desired template for this feed or type exit to interrupt. Key values are `{pubDate}`, `{title}`, `{description}`, `{link}`\n```"+rss.get(feed).getFormat()+"```").queue();
				Hashes.addTempCache(key, new Cache(180000, "updateformat"+feed));
			}
			else if(rss.get(feed).getType() == 2) {
				e.getChannel().sendMessage("Different formats for Twitter feeds are not available for the moment! Please choose a different feed!").queue();
				
			}
		}
		else {
			e.getChannel().sendMessage(message.setColor(Color.RED).setDescription("Please select an available digit").build()).queue();
		}
	}
	
	public static boolean updateFormat(GuildMessageReceivedEvent e, int feed, String format) {
		ArrayList<RSS> rss = Hashes.getFeed(e.getGuild().getIdLong());
		if(Azrael.SQLUpdateRSSFormat(rss.get(feed).getURL(), e.getGuild().getIdLong(), EmojiParser.parseToAliases(format)) > 0) {
			Hashes.clearFeeds();
			e.getChannel().sendMessage(message.setColor(Color.BLUE).setDescription("The format has been updated").build()).queue();
			logger.debug("{} has updated the format of an rss feed in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			return true;
		}
		else {
			e.getChannel().sendMessage(message.setColor(Color.RED).setDescription("An internal error occurred. Format couldn't be updated").build()).queue();
			logger.error("Format couldn't be updated for the url {} in guild {}", rss.get(feed).getURL(), e.getGuild().getId());
			return false;
		}
	}
	
	public static boolean runTest(GuildMessageReceivedEvent e, int feed) {
		var rss_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("rss")).findAny().orElse(null);
		if(rss_channel != null) {
			ArrayList<RSS> rss = Hashes.getFeed(e.getGuild().getIdLong());
			if(rss.size() >= feed+1) {
				try {
					String format = rss.get(feed).getFormat();
					URL rssUrl = new URL(rss.get(feed).getURL());
					BufferedReader in = new BufferedReader(new InputStreamReader(rssUrl.openStream()));
					
					boolean itemTagFound = false;
					String title = "";
					String description = "";
					String pubDate = "";
					String link = "";
					
					String line;
					while((line = in.readLine()) != null) {
						if(line.startsWith("<item>"))
							itemTagFound = true;
						else if(line.endsWith("</item>"))
							break;
						if(itemTagFound == true) {
							if(line.contains("<title>")) {
								int firstPos = line.indexOf("<title>");
								int lastPos = line.indexOf("</title>");
								title = line.substring(firstPos, lastPos).replaceAll("(<title>|</title>)", "");
							}
							if(line.contains("<description>")) {
								int firstPos = line.indexOf("<description>");
								int lastPos = line.indexOf("</description>");
								description = line.substring(firstPos, lastPos).replaceAll("(<description>|</descrption>)", "");
							}
							if(line.contains("<pubDate>")) {
								int firstPos = line.indexOf("<pubDate>");
								int lastPos = line.indexOf("</pubDate>");
								pubDate = line.substring(firstPos, lastPos).replaceAll("(<pubDate>|</pubDate>)", "");
							}
							if(line.contains("<link>")) {
								int firstPos = line.indexOf("<link>");
								int lastPos = line.indexOf("</link>");
								link = line.substring(firstPos, lastPos).replaceAll("(<link>|</link>)", "");
							}
						}
					}
					if(title.length() > 0 || description.length() > 0 || pubDate.length() > 0 || link.length() > 0) {
						String out = format.replace("{title}", title);
						out = out.replace("{description}", description);
						out = out.replace("{pubDate}", pubDate);
						out = out.replace("{link}", link);
						out = out.replaceAll("&#039;", "'");
						final String outMessage = EmojiParser.parseToUnicode(out);
						e.getGuild().getTextChannelById(rss_channel.getChannel_ID()).sendMessage(outMessage).queue();
					}
					else {
						e.getChannel().sendMessage("No feed could be found").queue();
					}
					in.close();
					return true;
				} catch (IOException e1) {
					logger.error("Error on reading the BufferedReader on rss test", e1);
					return false;
				}
			}
			else {
				e.getChannel().sendMessage(message.setColor(Color.RED).setDescription("Please select an available digit").build()).queue();
				return false;
			}
		}
		else {
			e.getChannel().sendMessage(message.setColor(Color.RED).setDescription("No rss channel has been registered. Please register a rss channel before testing").build()).queue();
			return true;
		}
	}
}
