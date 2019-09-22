package rss;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.vdurmont.emoji.EmojiParser;

import constructors.Channels;
import constructors.RSS;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.events.ReadyEvent;
import sql.Azrael;
import twitter4j.MediaEntity;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.URLEntity;
import util.STATIC;

public class ParseModel {
	
	public static void BasicModelParse(BufferedReader in, ReadyEvent e, RSS rss, long guild_id, Channels rss_channel) throws IOException {
		String format = rss.getFormat();
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
				if(line.contains("<title>") && line.contains("</title>")) {
					int firstPos = line.indexOf("<title>");
					int lastPos = line.indexOf("</title>");
					title = line.substring(firstPos, lastPos).replaceAll("(<title>|</title>)", "");
				}
				if(line.contains("<description>") && line.contains("</description>")) {
					int firstPos = line.indexOf("<description>");
					int lastPos = line.indexOf("</description>");
					description = line.substring(firstPos, lastPos).replaceAll("(<description>|</descrption>)", "");
				}
				if(line.contains("<pubDate>") && line.contains("</pubDate>")) {
					int firstPos = line.indexOf("<pubDate>");
					int lastPos = line.indexOf("</pubDate>");
					pubDate = line.substring(firstPos, lastPos).replaceAll("(<pubDate>|</pubDate>)", "");
				}
				if(line.contains("<link>") && line.contains("</link>")) {
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
			MessageHistory history = new MessageHistory(e.getJDA().getGuildById(guild_id).getTextChannelById(rss_channel.getChannel_ID()));
			List<Message> msg = history.retrievePast(30).complete();
			Message historyMessage = msg.parallelStream().filter(f -> f.getContentRaw().equals(outMessage)).findAny().orElse(null);
			if(historyMessage == null)
				e.getJDA().getGuildById(guild_id).getTextChannelById(rss_channel.getChannel_ID()).sendMessage(outMessage).queue();
		}
		in.close();
	}
	
	public static void TwitterModelParse(ReadyEvent e, RSS rss, long guild_id, Channels rss_channel) throws TwitterException {
		TwitterFactory tf = STATIC.getTwitterFactory();
		if(tf != null) {
			Twitter twitter = tf.getInstance();
			Query query = new Query("#CelebrateArcheAge");
			QueryResult result;
			result = twitter.search(query);
	        List<Status> tweets = result.getTweets();
	        final String pattern = "https:\\/\\/t.co\\/[\\w\\d]*";
	        for (Status tweet : tweets) {
	        	if(!tweet.isRetweet()) {
	        		String message = tweet.getText();
	        		final String fullName = tweet.getUser().getName();
	        		final String username = "@"+tweet.getUser().getScreenName();
	        		final String pubDate = tweet.getCreatedAt().toString();
	        		boolean tweetProhibited = false;
	        		if(Azrael.SQLgetTweetBlacklist(guild_id).parallelStream().filter(f -> username.equals(f)).findAny().orElse(null) != null)
	        			tweetProhibited = true;
	        		if(!tweetProhibited) {
	        			final String compareMessage = message.toLowerCase();
	        			find: for(var filter : Azrael.SQLgetChannel_Filter(rss_channel.getChannel_ID())) {
	        				Optional<String> option = Azrael.SQLgetFilter(filter, guild_id).parallelStream()
									.filter(word -> compareMessage.equals(word) || compareMessage.matches("[!\"$%&/()=?.@#^*+\\-={};':,<>]"+word+"(?!\\w\\d\\s)") || compareMessage.matches("[!\"$%&�/()=?.@#^*+\\-={};':,<>\\w\\d\\s]*\\s" + word + "(?!\\w\\d\\s)") || compareMessage.matches("[!\"$%&/()=?.@#^*+\\-={};':,<>\\w\\d\\s]*\\s" + word + "[!\"$%&/()=?.@#^*+\\-={};':,<>]") || compareMessage.matches(word+"\\s[!\"$%&/()=?.@#^*+\\-={};':,<>\\w\\d\\s]*") || compareMessage.matches("[!\"$%&/()=?.@#^*+\\-={};':,<>]"+word+"\\s[!\"$%&/()=?.@#^*+\\-={};':,<>\\w\\d\\s]*") || compareMessage.contains(" "+word+" "))
									.findAny();
							if(option.isPresent()) {
								tweetProhibited = true;
								break find;
							}
						}
	        		}
	        		if(!tweetProhibited) {
	        			for(MediaEntity media : tweet.getMediaEntities()) {
	                		if(message.contains("https://t.co"))
	                			message = message.replaceFirst(pattern, media.getExpandedURL());
	                		else
	                			message += "\n"+media.getExpandedURL();
	                	}
	                	for(URLEntity url : tweet.getURLEntities()) {
	                		if(message.contains("https://t.co"))
	                			message = message.replaceFirst(pattern, url.getExpandedURL());
	                		else
	                			message += "\n"+url.getExpandedURL();
	                	}
	                	String format = rss.getFormat();
	                	String out = format.replace("{description}", message);
						out = out.replace("{pubDate}", pubDate);
						out = out.replace("{fullName}", fullName);
						out = out.replace("{username}", username);
	                	final String outMessage = out;
	                	MessageHistory history = new MessageHistory(e.getJDA().getGuildById(guild_id).getTextChannelById(rss_channel.getChannel_ID()));
						List<Message> msg = history.retrievePast(30).complete();
						Message historyMessage = msg.parallelStream().filter(f -> f.getContentRaw().contains(outMessage)).findAny().orElse(null);
						
						if(historyMessage == null)
							e.getJDA().getGuildById(guild_id).getTextChannelById(rss_channel.getChannel_ID()).sendMessage(outMessage).queue();
	        		}
	        	}
	        }
		}
		else {
			//throw error in log channel that the authentication to Twitter is missing
		}
	}
}
