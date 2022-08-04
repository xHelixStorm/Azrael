package de.azrael.subscription;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiParser;

import de.azrael.constructors.Subscription;
import de.azrael.enums.Translation;
import de.azrael.sql.Azrael;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import twitter4j.MediaEntity;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.URLEntity;

public class TwitterModel {
	private static final Logger logger = LoggerFactory.getLogger(TwitterModel.class);
	
	public static boolean ModelParse(Guild guild, Subscription subscription, long subscriptionChannel, boolean defaultChannel) throws TwitterException {
		boolean success = false;
		final TextChannel textChannel = guild.getTextChannelById(subscriptionChannel);
		if(textChannel != null) {
			if(guild.getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY) || STATIC.setPermissions(guild, textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY))) {
				STATIC.loginTwitter();
				TwitterFactory tf = STATIC.getTwitterFactory();
				if(tf != null) {
					Twitter twitter = tf.getInstance();
					List<Status> tweets = new ArrayList<Status>();
					if(subscription.getURL().startsWith("#")) {
						Query query = new Query(subscription.getURL());
						QueryResult result;
						result = twitter.search(query);
						if(result.getTweets().size() > 0)
							success = true;
						if(result.getTweets().size() >= 30)
							tweets = result.getTweets().subList(0, 29);
						else
							tweets = result.getTweets().subList(0, result.getTweets().size());
					}
					else if(subscription.getURL().startsWith("@")) {
						tweets = twitter.getUserTimeline(subscription.getURL());
						if(tweets.size() > 0)
							success = true;
						if(tweets.size() > 30)
							tweets.subList(0, 29);
					}
					else {
						return success;
					}
			        final String pattern = "https:\\/\\/t.co\\/[\\w\\d]*";
			        final var prohibitedSubscriptions = Azrael.SQLgetSubscriptionBlacklist(guild.getIdLong());
			        for (Status tweet : tweets) {
			        	if(!tweet.isRetweet()) {
			        		if(!Azrael.SQLIsSubscriptionDeleted(tweet.getId()+"", guild.getIdLong())) {
			        			String message = tweet.getText();
				        		boolean tweetProhibited = false;
				        		final String fullName = tweet.getUser().getName();
				        		final String username = "@"+tweet.getUser().getScreenName();
				        		final String pubDate = tweet.getCreatedAt().toString();
				        		
				        		if(subscription.getChildTweets().size() > 0) {
				        			var reviewMessage = message.toLowerCase();
				        			boolean tweetFound = false;
				        			search: for(final String childTweet : subscription.getChildTweets()) {
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
				        		if(prohibitedSubscriptions.parallelStream().filter(f -> username.equals(f) || username.substring(1).equals(f)).findAny().orElse(null) != null)
				        			tweetProhibited = true;
				        		
				        		if(!tweetProhibited) {
				        			final String compareMessage = message.toLowerCase();
				        			find: for(var filter : Azrael.SQLgetChannel_Filter(subscriptionChannel)) {
				        				Optional<String> option = Azrael.SQLgetFilter(filter, guild.getIdLong()).parallelStream()
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
				                			if((media.getType().equals("photo") || media.getType().equals("animated_gif")) && subscription.getPictures()) {
				                				message = message.replaceFirst(pattern, media.getExpandedURL());
				                				picturePosted = true;
				                			}
				                			else if((media.getType().equals("photo") || media.getType().equals("animated_gif")) && !subscription.getPictures())
				                				message = message.replaceFirst(pattern, "");
				                			else if(media.getType().equals("video") && subscription.getVideos()) {
				                				message = message.replaceFirst(pattern, media.getExpandedURL());
				                				videoPosted = true;
				                			}
				                			else if(media.getType().equals("video") && !subscription.getVideos())
				                				message = message.replaceFirst(pattern, "");
				                		}
				                		//parse url
				                		else {
				                			if((media.getType().equals("photo") || media.getType().equals("animated_gif")) && subscription.getPictures()) {
				                				message += "\n"+media.getExpandedURL();
				                				picturePosted = true;
				                			}
				                			else if(media.getType().equals("video") && subscription.getVideos()) {
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
				                			else if((url.getExpandedURL().endsWith(".jpg") || url.getExpandedURL().endsWith(".jpeg") || url.getExpandedURL().endsWith(".png") || url.getExpandedURL().endsWith(".gif")) && !subscription.getPictures())
				                				message = message.replaceFirst(pattern, "");
				                			else if((url.getDisplayURL().startsWith("youtu.be") || url.getDisplayURL().startsWith("youtube.com") || url.getDisplayURL().startsWith("m.youtube.com")) && subscription.getVideos()) {
				                				message = message.replaceFirst(pattern, url.getExpandedURL());
				                				videoPosted = true;
				                			}
				                			else if((url.getDisplayURL().startsWith("youtu.be") || url.getDisplayURL().startsWith("youtube.com") || url.getDisplayURL().startsWith("m.youtube.com")) && !subscription.getVideos())
				                				message = message.replaceFirst(pattern, "");
				                			else if(subscription.getText())
				                				message = message.replaceFirst(pattern, url.getExpandedURL());
				                			else
				                				message = message.replaceFirst(pattern, "");
				                		}
				                		//parse url
				                		else {
				                			if((url.getExpandedURL().endsWith(".jpg") || url.getExpandedURL().endsWith(".jpeg") || url.getExpandedURL().endsWith(".png") || url.getExpandedURL().endsWith(".gif"))) {
				                				if(subscription.getPictures()) {
				                					message += "\n"+url.getExpandedURL();
					                				picturePosted = true;
				                				}
				                			}
				                			else if((url.getExpandedURL().startsWith("https://youtu.be") || url.getExpandedURL().startsWith("https://youtube.com") || url.getExpandedURL().startsWith("https://m.youtube.com"))) {
				                				if(subscription.getVideos()) {
				                					message += "\n"+url.getExpandedURL();
					                				videoPosted = true;
				                				}
				                			}
				                			else if(subscription.getText()) {
				                				message += "\n"+url.getExpandedURL();
				                			}
				                		}
				                	}
				                	
				                	boolean printMessage = false;
				                	if(subscription.getPictures() && subscription.getVideos() && subscription.getText())
				                		printMessage = true;
				                	else if(!subscription.getPictures() && subscription.getVideos() && subscription.getText()) {
				                		printMessage = true;
				                	}
				                	else if(subscription.getPictures() && !subscription.getVideos() && subscription.getText()) {
				                		printMessage = true;
				                	}
				                	else if(subscription.getPictures() && subscription.getVideos() && !subscription.getText() && (picturePosted || videoPosted)) {
				                		printMessage = true;
				                	}
				                	else if(!subscription.getPictures() && !subscription.getVideos() && subscription.getText()) {
				                		printMessage = true;
				                	}
				                	else if(!subscription.getPictures() && subscription.getVideos() && !subscription.getText() && !picturePosted && videoPosted) {
				                		printMessage = true;
				                	}
				                	else if(subscription.getPictures() && !subscription.getVideos() && !subscription.getText() && picturePosted && !videoPosted) {
				                		printMessage = true;
				                	}
				                	
				                	if(printMessage) {
				                		String format = subscription.getFormat();
					                	String out = format.replace("{description}", message);
										out = out.replace("{pubDate}", pubDate);
										out = out.replace("{fullName}", fullName);
										out = out.replace("{username}", username);
					                	final String outMessage = EmojiParser.parseToUnicode(out);
					                	SubscriptionUtils.postSubscriptionToServerChannel(guild, textChannel, outMessage, (tweet.getId()+""), (fullName + " ("+username+")"));
				                	}
				        		}
			        		}
			        	}
			        }
				}
			}
			else {
				logger.warn("MESSAGE_WRITE and MESSAGE_HISTORY permission required to print into the subscription channel {} in guild {}", subscriptionChannel, guild.getId());
			}
		}
		else {
			//Remove not anymore existing channel
			SubscriptionUtils.deleteRemovedChannel(subscriptionChannel, defaultChannel, subscription.getURL(), guild);
		}
		return success;
	}
	
	public static void ModelTest(GuildMessageReceivedEvent e, Subscription subscription) {
		STATIC.loginTwitter();
		TwitterFactory tf = STATIC.getTwitterFactory();
		if(tf != null) {
			try {
				Twitter twitter = tf.getInstance();
				Query query = new Query(subscription.getURL());
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
		                	String format = subscription.getFormat();
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
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITTER_NO_TWEET)).build()).queue();
				}
			} catch (TwitterException e1) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Subscription couldn't be retrieved from {} in guild {}!", subscription.getURL(), e.getGuild().getId(), e1);
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITTER_LOGIN)).build()).queue();
		}
	}
}
