package rss;

import java.awt.Color;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiParser;

import constructors.Channels;
import constructors.RSS;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
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

public class TwitterModel {
	private static final Logger logger = LoggerFactory.getLogger(TwitterModel.class);
	
	public static void ModelParse(ReadyEvent e, RSS rss, long guild_id, Channels rss_channel) throws TwitterException {
		STATIC.loginTwitter();
		TwitterFactory tf = STATIC.getTwitterFactory();
		if(tf != null) {
			Twitter twitter = tf.getInstance();
			Query query = new Query(rss.getURL());
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
	                	final String outMessage = EmojiParser.parseToUnicode(out);
	                	MessageHistory history = new MessageHistory(e.getJDA().getGuildById(guild_id).getTextChannelById(rss_channel.getChannel_ID()));
						List<Message> msg = history.retrievePast(30).complete();
						Message historyMessage = msg.parallelStream().filter(f -> f.getContentRaw().contains(outMessage)).findAny().orElse(null);
						
						if(historyMessage == null)
							e.getJDA().getGuildById(guild_id).getTextChannelById(rss_channel.getChannel_ID()).sendMessage(outMessage).queue();
	        		}
	        	}
	        }
		}
	}
	
	public static void ModelTest(GuildMessageReceivedEvent e, RSS rss) {
		STATIC.loginTwitter();
		TwitterFactory tf = STATIC.getTwitterFactory();
		if(tf != null) {
			try {
				Twitter twitter = tf.getInstance();
				Query query = new Query(rss.getURL());
				QueryResult result;
				result = twitter.search(query);
				List<Status> tweets = result.getTweets();
				if(tweets.size() > 0) {
					final String pattern = "https:\\/\\/t.co\\/[\\w\\d]*";
			        for (Status tweet : tweets) {
			        	if(!tweet.isRetweet()) {
			        		String message = tweet.getText();
			        		final String fullName = tweet.getUser().getName();
			        		final String username = "@"+tweet.getUser().getScreenName();
			        		final String pubDate = tweet.getCreatedAt().toString();
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
		                	final String outMessage = EmojiParser.parseToUnicode(out);
							e.getChannel().sendMessage(outMessage).queue();
							break;
			        	}
			        }
				}
				else {
					e.getChannel().sendMessage("No tweet could be found").queue();
				}
			} catch (TwitterException e1) {
				logger.error("Error on retrieving feed!", e1);
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Error on Twitter login!").setDescription("Please set up the config.ini file after creating a Twitter Bot on https://apps.twitter.com before using this command!").build()).queue();
		}
	}
}
