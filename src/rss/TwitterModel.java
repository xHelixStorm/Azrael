package rss;

import java.awt.Color;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiParser;

import constructors.Channels;
import constructors.Messages;
import constructors.RSS;
import core.Hashes;
import enums.Translation;
import fileManagement.GuildIni;
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
			List<Status> tweets;
			if(result.getTweets().size() >= 30)
				tweets = result.getTweets().subList(0, 29);
			else
				tweets = result.getTweets().subList(0, result.getTweets().size());
	        final String pattern = "https:\\/\\/t.co\\/[\\w\\d]*";
	        for (Status tweet : tweets) {
	        	if(!tweet.isRetweet()) {
	        		if(!Azrael.SQLIsTweetDeleted(tweet.getId())) {
	        			String message = tweet.getText();
		        		boolean tweetProhibited = false;
		        		final String fullName = tweet.getUser().getName();
		        		final String username = "@"+tweet.getUser().getScreenName();
		        		final String pubDate = tweet.getCreatedAt().toString();
		        		
		        		if(rss.getChildTweets().size() > 0) {
		        			var reviewMessage = message.toLowerCase();
		        			boolean tweetFound = false;
		        			search: for(final String childTweet : rss.getChildTweets()) {
		        				Matcher matcher = Pattern.compile(childTweet.toLowerCase()+"(\\w{0,}|\\d{0,})").matcher(reviewMessage);
		        				while(matcher.find()) {
		        					if(matcher.group().equalsIgnoreCase(childTweet)) {
		        						tweetFound = true;
		        						break search;
		        					}
		        				}
		        			}
		        			if(!tweetFound)
		        				tweetProhibited = true;
		        		}
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
		        			var picturePosted = false;
		        			var videoPosted = false;
		        			
		        			for(MediaEntity media : tweet.getMediaEntities()) {
		        				//replace url if it starts with https://t.co
		                		if(message.contains("https://t.co")) {
		                			if((media.getType().equals("photo") || media.getType().equals("animated_gif")) && rss.getPictures()) {
		                				message = message.replaceFirst(pattern, media.getExpandedURL());
		                				picturePosted = true;
		                			}
		                			else if((media.getType().equals("photo") || media.getType().equals("animated_gif")) && !rss.getPictures())
		                				message = message.replaceFirst(pattern, "");
		                			else if(media.getType().equals("video") && rss.getVideos()) {
		                				message = message.replaceFirst(pattern, media.getExpandedURL());
		                				videoPosted = true;
		                			}
		                			else if(media.getType().equals("video") && !rss.getVideos())
		                				message = message.replaceFirst(pattern, "");
		                		}
		                		//parse url
		                		else {
		                			if((media.getType().equals("photo") || media.getType().equals("animated_gif")) && rss.getPictures()) {
		                				message += "\n"+media.getExpandedURL();
		                				picturePosted = true;
		                			}
		                			else if(media.getType().equals("video") && rss.getVideos()) {
		                				message += "\n"+media.getExpandedURL();
		                				videoPosted = true;
		                			}
		                		}
		                	}
		                	for(URLEntity url : tweet.getURLEntities()) {
		                		//replace url if it starts with https://t.co
		                		if(message.contains("https://t.co")) {
		                			if((url.getExpandedURL().endsWith(".jpg") || url.getExpandedURL().endsWith(".jpeg") || url.getExpandedURL().endsWith(".png") || url.getExpandedURL().endsWith(".gif"))) {
		                				message = message.replaceFirst(pattern, url.getExpandedURL());
		                				picturePosted = true;
		                			}
		                			else if((url.getExpandedURL().endsWith(".jpg") || url.getExpandedURL().endsWith(".jpeg") || url.getExpandedURL().endsWith(".png") || url.getExpandedURL().endsWith(".gif")) && !rss.getPictures())
		                				message = message.replaceFirst(pattern, "");
		                			else if((url.getDisplayURL().startsWith("youtu.be") || url.getDisplayURL().startsWith("youtube.com") || url.getDisplayURL().startsWith("m.youtube.com")) && rss.getVideos()) {
		                				message = message.replaceFirst(pattern, url.getExpandedURL());
		                				videoPosted = true;
		                			}
		                			else if((url.getDisplayURL().startsWith("youtu.be") || url.getDisplayURL().startsWith("youtube.com") || url.getDisplayURL().startsWith("m.youtube.com")) && !rss.getVideos())
		                				message = message.replaceFirst(pattern, "");
		                			else if(rss.getText())
		                				message = message.replaceFirst(pattern, url.getExpandedURL());
		                			else
		                				message = message.replaceFirst(pattern, "");
		                		}
		                		//parse url
		                		else {
		                			if((url.getExpandedURL().endsWith(".jpg") || url.getExpandedURL().endsWith(".jpeg") || url.getExpandedURL().endsWith(".png") || url.getExpandedURL().endsWith(".gif"))) {
		                				if(rss.getPictures()) {
		                					message += "\n"+url.getExpandedURL();
			                				picturePosted = true;
		                				}
		                			}
		                			else if((url.getExpandedURL().startsWith("https://youtu.be") || url.getExpandedURL().startsWith("https://youtube.com") || url.getExpandedURL().startsWith("https://m.youtube.com"))) {
		                				if(rss.getVideos()) {
		                					message += "\n"+url.getExpandedURL();
			                				videoPosted = true;
		                				}
		                			}
		                			else if(rss.getText()) {
		                				message += "\n"+url.getExpandedURL();
		                			}
		                		}
		                	}
		                	
		                	boolean printMessage = false;
		                	if(rss.getPictures() && rss.getVideos() && rss.getText())
		                		printMessage = true;
		                	else if(!rss.getPictures() && rss.getVideos() && rss.getText()) {
		                		printMessage = true;
		                	}
		                	else if(rss.getPictures() && !rss.getVideos() && rss.getText()) {
		                		printMessage = true;
		                	}
		                	else if(rss.getPictures() && rss.getVideos() && !rss.getText() && (picturePosted || videoPosted)) {
		                		printMessage = true;
		                	}
		                	else if(!rss.getPictures() && !rss.getVideos() && rss.getText()) {
		                		printMessage = true;
		                	}
		                	else if(!rss.getPictures() && rss.getVideos() && !rss.getText() && !picturePosted && videoPosted) {
		                		printMessage = true;
		                	}
		                	else if(rss.getPictures() && !rss.getVideos() && !rss.getText() && picturePosted && !videoPosted) {
		                		printMessage = true;
		                	}
		                	
		                	if(printMessage) {
		                		String format = rss.getFormat();
			                	String out = format.replace("{description}", message);
								out = out.replace("{pubDate}", pubDate);
								out = out.replace("{fullName}", fullName);
								out = out.replace("{username}", username);
			                	final String outMessage = EmojiParser.parseToUnicode(out);
			                	MessageHistory history = new MessageHistory(e.getJDA().getGuildById(guild_id).getTextChannelById(rss_channel.getChannel_ID()));
								history.retrievePast(100).queue(historyList -> {
									Message historyMessage = historyList.parallelStream().filter(f -> f.getContentRaw().replaceAll("[^a-zA-Z]", "").contains(outMessage.replaceAll("[^a-zA-Z]", ""))).findAny().orElse(null);
									if(historyMessage == null)
										e.getJDA().getGuildById(guild_id).getTextChannelById(rss_channel.getChannel_ID()).sendMessage(outMessage).queue(m -> {
											Azrael.SQLInsertTweetLog(m.getIdLong(), tweet.getId());
											if(GuildIni.getCacheLog(guild_id)) {
												Messages collectedMessage = new Messages();
												collectedMessage.setUserID(0);
												collectedMessage.setUsername(fullName + " ("+username+")");
												collectedMessage.setGuildID(guild_id);
												collectedMessage.setChannelID(rss_channel.getChannel_ID());
												collectedMessage.setChannelName(rss_channel.getChannel_Name());
												collectedMessage.setMessage(outMessage);
												collectedMessage.setMessageID(m.getIdLong());
												collectedMessage.setTime(ZonedDateTime.now());
												collectedMessage.setIsEdit(false);
												ArrayList<Messages> cacheMessage = new ArrayList<Messages>();
												cacheMessage.add(collectedMessage);
												Hashes.addMessagePool(m.getIdLong(), cacheMessage);
											}
										});
								});
		                	}
		        		}
	        		}
	        		else {
	        			Azrael.SQLUpdateTweetTimestamp(tweet.getId());
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
					e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.TWITTER_NO_TWEET)).queue();
				}
			} catch (TwitterException e1) {
				logger.error("Error on retrieving feed!", e1);
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_LOGIN_TWITTER)).build()).queue();
		}
	}
}
