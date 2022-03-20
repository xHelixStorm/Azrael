package de.azrael.subscription;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.vdurmont.emoji.EmojiParser;

import de.azrael.constructors.Cache;
import de.azrael.constructors.Messages;
import de.azrael.constructors.Subscription;
import de.azrael.core.Hashes;
import de.azrael.enums.Command;
import de.azrael.enums.RedditMethod;
import de.azrael.enums.Translation;
import de.azrael.google.GoogleYoutube;
import de.azrael.sql.Azrael;
import de.azrael.sql.BotConfiguration;
import de.azrael.timerTask.ParseSubscription;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

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
	
	/**
	 * Display message to register a subscription and then write to cache for the next step
	 * @param e
	 * @param type
	 */
	
	public static void register(GuildMessageReceivedEvent e, String type) {
		String message = "";
		switch(type) {
			case "rss" -> 		message = STATIC.getTranslation(e.getMember(), Translation.RSS_REGISTER)
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTER))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_FORMAT))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_CHANNEL))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_TEST))
									.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_DISPLAY));
			case "twitter" -> 	message = STATIC.getTranslation(e.getMember(), Translation.TWITTER_REGISTER)
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTER))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_FORMAT))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_OPTIONS))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_CHANNEL))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_TEST))
									.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_DISPLAY));
			case "reddit" -> {
				StringBuilder methods = new StringBuilder();
				for(final RedditMethod method : RedditMethod.values()) {
					methods.append("**"+method.type+"**\n");
				}
				message = STATIC.getTranslation(e.getMember(), Translation.REDDIT_REGISTER)
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTER))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_FORMAT))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_CHANNEL))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_TEST))
									.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_DISPLAY))+methods.toString();
			}
			case "youtube" -> 	message = STATIC.getTranslation(e.getMember(), Translation.YOUTUBE_REGISTER)
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTER))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_FORMAT))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_CHANNEL))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_TEST))
									.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_DISPLAY));
			case "twitch" -> 	message = STATIC.getTranslation(e.getMember(), Translation.TWITCH_REGISTER)
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTER))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_FORMAT))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_CHANNEL))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))
									.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_TEST))
									.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_DISPLAY));
		}
		e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(message).build()).queue();
		Hashes.addTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, type, "register"));
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SUBSCRIBE.getColumn(), e.getMessage().getContentRaw());
	}
	
	/**
	 * Validate inserted subscription, save it and clear all related cache
	 * @param e
	 * @param type
	 * @param message
	 */
	
	public static void register(GuildMessageReceivedEvent e, String type, String message) {
		boolean valid = false;
		String output = "";
		String name = null;
		int subscriptionType = 0;
		switch(type) {
			case "rss" -> {
				subscriptionType = 1;
				if(message.startsWith("http://") || message.startsWith("https://")) {
					try {
						BufferedReader br = STATIC.retrieveWebPageCode(message);
						String line = "";
						StringBuilder sb = new StringBuilder();
						while((line = br.readLine()) != null) {
							sb.append(line);
						}
						final String html = sb.toString();
						if(html.contains("<rss") && html.contains("</rss>") && html.contains("<channel>") && html.contains("</channel>") && html.contains("<item>") && html.contains("</item>")) {
							output = STATIC.getTranslation(e.getMember(), Translation.RSS_ADDED).replace("{}", message);
							valid = true;
						}
					} catch (Exception e1) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.RSS_INVALID)).build()).queue();
						logger.warn("An invalid RSS url {} has been used in guild {}", message, e.getGuild().getId(), e1);
					} finally {
						if(!valid) {
							Hashes.clearTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
							Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SUBSCRIBE.getColumn(), e.getMessage().getContentRaw());
						}
					}
				}
			}
			case "twitter" -> {
				subscriptionType = 2;
				if(message.contains("#") || message.contains("@")) {
					STATIC.loginTwitter();
					TwitterFactory tf = STATIC.getTwitterFactory();
					if(tf != null) {
						message = message.replaceAll("[`]*", "");
						try {
							if(message.startsWith("#") && validateHashtag(e, message)) {
								output = STATIC.getTranslation(e.getMember(), Translation.TWITTER_ADDED);
								valid = true;
							}
							else if(message.startsWith("@")) {
								Twitter twitter = tf.getInstance();
								ResponseList<Status> tweets = twitter.getUserTimeline(message);
								if(tweets.size() > 0) {
									output = STATIC.getTranslation(e.getMember(), Translation.TWITTER_ADDED).replace("{}", message);
									valid = true;
								}
							}
						} catch (Exception e1) {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITTER_INVALID)).build()).queue();
							logger.warn("An invalid Twitter hashtag/user {} has been used in guild {}", message, e.getGuild().getId(), e1);
						} finally {
							if(!valid) {
								Hashes.clearTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
								Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SUBSCRIBE.getColumn(), e.getMessage().getContentRaw());
							}
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITTER_LOGIN)).build()).queue();
						logger.error("Log in to Twitter was not possible in guild {}", e.getGuild().getId());
						Hashes.clearTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SUBSCRIBE.getColumn(), e.getMessage().getContentRaw());
					}
				}
			}
			case "reddit" -> {
				subscriptionType = 3;
				String [] input = message.split(" ");
				if(input.length == 2) {
					RedditMethod method = RedditMethod.valueOfType(input[1].toLowerCase());
					if(input[0].matches("[a-zA-Z0-9\s_-]{3,20}") && method != null) {
						message = method.url.replace("{}", input[0]);
						Subscription testSubscription = new Subscription(message, e.getGuild().getIdLong(), "", subscriptionType, false, false, false, 0, "", null);
						try {
							String response = RedditModel.fetchRedditContent(e.getGuild(), testSubscription);
							if(response != null && response.contains("data") && response.contains("children")) {
								JSONObject parentData = new JSONObject(response).getJSONObject("data");
								JSONArray children = parentData.getJSONArray("children");
								if(children.length() > 0) {
									output = STATIC.getTranslation(e.getMember(), Translation.REDDIT_ADDED).replace("{}", message);
									valid = true;
								}
							}
						} catch (Exception e1) {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REDDIT_INVALID)).build()).queue();
							logger.warn("An invalid Reddit subscription {} has been used in guild {}", message, e.getGuild().getId(), e1);
						} finally {
							if(!valid) {
								Hashes.clearTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
								Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SUBSCRIBE.getColumn(), e.getMessage().getContentRaw());
							}
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REDDIT_INVALID)).build()).queue();
						logger.warn("An invalid Reddit subscription {} has been used in guild {}", message, e.getGuild().getId());
						Hashes.clearTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SUBSCRIBE.getColumn(), e.getMessage().getContentRaw());
					}
				}
			}
			case "youtube" -> {
				subscriptionType = 4;
				final String url = "https://www.youtube.com/channel/";
				if(!message.contains(" ") && message.startsWith(url) && message.length() > url.length()) {
					message = message.substring(url.length()+1);
					try {
						SearchListResponse result = GoogleYoutube.searchYouTubeChannelVideos(GoogleYoutube.getService(), message, 1);
						if(result != null && result.getItems().size() > 0) {
							final SearchResult item = result.getItems().get(0);
							name = item.getSnippet().getChannelTitle();
							output = STATIC.getTranslation(e.getMember(), Translation.YOUTUBE_ADDED).replaceFirst("\\{\\}", message).replace("{}", name);
							valid = true;
						}
					} catch (Exception e1) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.YOUTUBE_INVALID)).build()).queue();
						logger.warn("An invalid YouTube subscription {} has been used in guild {}", message, e.getGuild().getId(), e1);
					} finally {
						if(!valid) {
							Hashes.clearTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
							Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SUBSCRIBE.getColumn(), e.getMessage().getContentRaw());
						}
					}
				}
			}
			case "twitch" -> { 
				subscriptionType = 5;
				if(message.matches("[a-zA-Z0-9_-]{4,25}")) {
					try {
						String [] user = TwitchModel.findUser(message);
						if(user != null) {
							message = user[0];
							name = user[1];
							output = STATIC.getTranslation(e.getMember(), Translation.TWITCH_ADDED).replaceFirst("\\{\\}", name).replace("{}", message);
							valid = true;
						}
					} catch (IOException e1) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITCH_INVALID)).build()).queue();
						logger.warn("An invalid Twitch subscription {} has been used in guild {}", message, e.getGuild().getId());
					} finally {
						if(!valid) {
							Hashes.clearTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
							Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SUBSCRIBE.getColumn(), e.getMessage().getContentRaw());
						}
					}
				}
			}
		}
		
		if(valid) {
			final int result = Azrael.SQLInsertSubscription(message, e.getGuild().getIdLong(), subscriptionType, name);
			if(result > 0) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(output).build()).queue();
				SubscriptionUtils.startTimer(e.getJDA());
				logger.info("User {} has subscribed the url {} in guild {}", e.getMember().getUser().getId(), message, e.getGuild().getId());
			}
			else if(result == 0) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_ALREADY_DONE)).build()).queue();
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("The url {} couldn't be subscribed in guild {}", message, e.getGuild().getId());
			}
			Hashes.clearTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SUBSCRIBE.getColumn(), e.getMessage().getContentRaw());
		}
	}
	
	/**
	 * Display message to remove a subscription and then write to cache for the next step
	 * @param e
	 * @param type
	 */
	
	public static void remove(GuildMessageReceivedEvent e, String type) {
		final Object [] object = getSubscriptionList(e, type);
		final String out = (String)object[0];
		if(out.length() > 0) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_REMOVE_HELP)+out.toString()).build()).queue();
			Hashes.addTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, type, "remove").setObject(object[1]));
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_NO_SUBSCRIPTIONS)).build()).queue();
			Hashes.clearTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
		}
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SUBSCRIBE.getColumn(), e.getMessage().getContentRaw());
	}
	
	/**
	 * Select the subscription of choice, remove the subscription and clear all related cache
	 * @param e
	 * @param message
	 * @param subscriptions
	 */
	
	public static void remove(GuildMessageReceivedEvent e, String message, ArrayList<Subscription> subscriptions) {
		if(message.matches("[0-9]{1,}")) {
			final long selection = Long.parseLong(message)-1;
			if(selection <= Integer.MAX_VALUE) {
				ArrayList<Subscription> subscriptionList = subscriptions;
				if(subscriptionList.size() >= selection+1 && selection >= 0) {
					final String url = subscriptionList.get((int)selection).getURL();
					if(Azrael.SQLDeleteSubscription(url, e.getGuild().getIdLong()) > 0) {
						Hashes.clearSubscriptions();
						Hashes.removeSubscriptionStatus(e.getGuild().getId()+"_"+url);
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_REMOVED)).build()).queue();
						logger.info("User {} has removed the subscription {} in guild {}", e.getMember().getUser().getId(), url, e.getGuild().getId());
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("The subscription {} couldn't be removed in guild {}", url, e.getGuild().getId());
					}
					Hashes.clearTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SUBSCRIBE.getColumn(), e.getMessage().getContentRaw());
				}
			}
		}
	}
	
	/**
	 * Display message to format the output message and then write to cache for the next step
	 * @param e
	 * @param type
	 */
	
	public static void format(GuildMessageReceivedEvent e, String type) {
		final Object [] object = getSubscriptionList(e, type);
		final String out = (String)object[0];
		if(out.length() > 0) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_FORMAT_HELP)+out.toString()).build()).queue();
			Hashes.addTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, type, "format").setObject(object[1]));
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_NO_SUBSCRIPTIONS)).build()).queue();
			Hashes.clearTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
		}
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SUBSCRIBE.getColumn(), e.getMessage().getContentRaw());
	}
	
	/**
	 * Display all key values for the selected subscription and then write to cache for the next step
	 * @param e
	 * @param type
	 * @param message
	 * @param subscriptions
	 */
	
	public static void format(GuildMessageReceivedEvent e, String type, String message, ArrayList<Subscription> subscriptions) {
		if(message.matches("[0-9]{1,}")) {
			final long selection = Long.parseLong(message)-1;
			if(selection <= Integer.MAX_VALUE) {
				ArrayList<Subscription> subscriptionList = subscriptions;
				if(subscriptionList.size() >= selection+1 && selection >= 0) {
					final Subscription subscription = subscriptionList.get((int)selection);
					String format = "";
					switch(type) {
						case "rss" -> 		format = STATIC.getTranslation(e.getMember(), Translation.RSS_FORMAT_CURRENT);
						case "twitter" -> 	format = STATIC.getTranslation(e.getMember(), Translation.TWITTER_FORMAT_CURRENT);
						case "reddit" -> 	format = STATIC.getTranslation(e.getMember(), Translation.REDDIT_FORMAT_CURRENT);
						case "youtube" ->	format = STATIC.getTranslation(e.getMember(), Translation.YOUTUBE_FORMAT_CURRENT);
						case "twitch" -> 	format = STATIC.getTranslation(e.getMember(), Translation.TWITCH_FORMAT_CURRENT);
					}
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_FORMAT_CURRENT).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_EXIT))+" "+format).build()).queue();
					e.getChannel().sendMessage(subscription.getFormat());
					Hashes.addTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, type, "formatUpdate").setObject(subscription));
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SUBSCRIBE.getColumn(), e.getMessage().getContentRaw());
				}
			}
		}
	}
	
	/**
	 * Update the output message format for the selected subscription and clear all related cache
	 * @param e
	 * @param message
	 * @param subscription
	 */
	
	public static void format(GuildMessageReceivedEvent e, String message, Subscription subscription) {
		if(message.replaceAll("[\s]*", "").length() > 0) {
			if(Azrael.SQLUpdateSubscriptionFormat(subscription.getURL(), e.getGuild().getIdLong(), EmojiParser.parseToAliases(message)) > 0) {
				Hashes.clearSubscriptions();
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_FORMAT_UPDATED)).build()).queue();
				logger.info("User {} has updated the display format of the RSS url {} in guild {}", e.getMember().getUser().getId(), subscription.getURL(), e.getGuild().getId());
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("The display format of RSS url {} couldn't be updated in guild {}", subscription.getURL(), e.getGuild().getId());
			}
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SUBSCRIBE.getColumn(), e.getMessage().getContentRaw());
		}
	}
	
	/**
	 * Display message to format twitter tweets further and then write to cache for the next step
	 * @param e
	 * @param type
	 */
	
	public static void options(GuildMessageReceivedEvent e, String type) {
		if(type.equals("twitter")) {
			final Object [] object = getSubscriptionList(e, type);
			final String out = (String)object[0];
			if(out.length() > 0) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITTER_OPTIONS)+out.toString()).build()).queue();
				Hashes.addTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, type, "options").setObject(object[1]));
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_NO_SUBSCRIPTIONS)).build()).queue();
				Hashes.clearTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
			}
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SUBSCRIBE.getColumn(), e.getMessage().getContentRaw());
		}
	}
	
	/**
	 * Display the current Twitter subscription options and then write to cache for the next step
	 * @param e
	 * @param type
	 * @param message
	 * @param subscriptions
	 */
	
	public static void options(GuildMessageReceivedEvent e, String type, String message, ArrayList<Subscription> subscriptions) {
		if(message.matches("[0-9]{1,}")) {
			final long selection = Long.parseLong(message)-1;
			if(selection <= Integer.MAX_VALUE) {
				ArrayList<Subscription> subscriptionList = subscriptions;
				if(subscriptionList.size() >= selection+1 && selection >= 0) {
					final Subscription subscription = subscriptionList.get((int)selection);
					printOptionsMessage(e, subscription);
					Hashes.addTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, type, "optionsUpdate").setObject(subscription));
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SUBSCRIBE.getColumn(), e.getMessage().getContentRaw());
				}
			}
		}
	}
	
	/**
	 * Update options values and refresh the cache
	 * @param e
	 * @param type
	 * @param message
	 * @param subscription
	 */
	
	public static void options(GuildMessageReceivedEvent e, String type, String message, Subscription subscription) {
		boolean printMessage = false;
		boolean processAttempt = false;
		if(message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE_PICTURES))) {
			processAttempt = true;
			if(Azrael.SQLUpdateSubscriptionPictures(subscription.getURL(), e.getGuild().getIdLong(), true) > 0) {
				logger.info("User {} has allowed pictures to be displayed for the Twitter hashtag/user {} in guild {}", e.getMember().getUser().getId(), subscription.getURL(), e.getGuild().getId());
				printMessage = true;
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Pictures couldn't be allowed for the Twitter hashtag/user {} in guild {}", subscription.getURL(), e.getGuild().getId());
			}
		}
		else if(message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE_VIDEOS))) {
			processAttempt = true;
			if(Azrael.SQLUpdateSubscriptionVideos(subscription.getURL(), e.getGuild().getIdLong(), true) > 0) {
				logger.info("User {} has allowed videos to be displayed for the Twitter hashtag {} in guild {}", e.getMember().getUser().getId(), subscription.getURL(), e.getGuild().getId());
				printMessage = true;
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Videos couldn't be allowed for Twitter hashtag {} in guild {}", subscription.getURL(), e.getGuild().getId());
			}
		}
		else if(message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE_TEXT))) {
			processAttempt = true;
			if(Azrael.SQLUpdateSubscriptionText(subscription.getURL(), e.getGuild().getIdLong(), true) > 0) {
				logger.info("User {} has allowed text messages to be displayed for Twitter hashtag {} in guild {}", e.getMember().getUser().getId(), subscription.getURL(), e.getGuild().getId());
				printMessage = true;
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Text messages couldn't be allowed for Twitter hashtag {} in guild {}", subscription.getURL(), e.getGuild().getId());
			}
		}
		else if(message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISABLE_PICTURES))) {
			processAttempt = true;
			if(Azrael.SQLUpdateSubscriptionPictures(subscription.getURL(), e.getGuild().getIdLong(), false) > 0) {
				logger.info("User {} has disallowed pictures to be displayed for Twitter hashtag {} in guild {}", e.getMember().getUser().getId(), subscription.getURL(), e.getGuild().getId());
				printMessage = true;
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Pictures couldn't be disallowed for Twitter hashtag {} in guild {}", subscription.getURL(), e.getGuild().getId());
			}
		}
		else if(message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISABLE_VIDEOS))) {
			processAttempt = true;
			if(Azrael.SQLUpdateSubscriptionVideos(subscription.getURL(), e.getGuild().getIdLong(), false) > 0) {
				logger.info("User {} has disallowed videos to be displayed for Twitter hashtag {} in guild {}", e.getMember().getUser().getId(), subscription.getURL(), e.getGuild().getId());
				printMessage = true;
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Videos couldn't be disallowed for Twitter hashtag {} in guild {}", subscription.getURL(), e.getGuild().getId());
			}
		}
		else if(message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISABLE_TEXT))) {
			processAttempt = true;
			if(Azrael.SQLUpdateSubscriptionText(subscription.getURL(), e.getGuild().getIdLong(), false) > 0) {
				logger.info("User {} has disallowed text messages to be displayed for Twitter hashtag {} in guild {}", e.getMember().getUser().getId(), subscription.getURL(), e.getGuild().getId());
				printMessage = true;
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Text messages couldn't be disallowed for Twitter hashtag {} in guild {}", subscription.getURL(), e.getGuild().getId());
			}
		}
		else if(message.startsWith(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_CHILD)) || message.startsWith(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE_CHILD))) {
			processAttempt = true;
			final String addChild = STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_CHILD);
			final String removeChild = STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE_CHILD);
			if(message.matches(addChild+" #[a-zA-Z0-9]{1,}[^\\s]*")) {
				printMessage = true;
				final String hashtag = message.substring(addChild.length()+1);
				boolean valid = false;
				try {
					valid = validateHashtag(e, hashtag);
				} catch (TwitterException e1) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITTER_INVALID)).build()).queue();
					logger.warn("An invalid Twitter hashtag/user {} has been used in guild {}", message, e.getGuild().getId(), e1);
				}
				if(valid) {
					final int result = Azrael.SQLInsertChildSubscription(subscription.getURL(), hashtag, e.getGuild().getIdLong()); 
					if(result > 0) {
						logger.info("User {} has added the child hashtag {} to the parent hashtag/user {} in guild {}", e.getMember().getUser().getId(), hashtag, subscription.getURL(), e.getGuild().getId());
					}
					else if(result == 0) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITTER_OPTION_BOUND)+subscription.getURL()).build()).queue();
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Child hashtag {} couldn't be added to the parent hashtag/user {} in guild {}", hashtag, subscription.getURL(), e.getGuild().getId());
						printMessage = false;
					}
				}
			}
			else if(message.matches(removeChild+" #[a-z0-9]{1,}[^\\s]*")) {
				printMessage = true;
				final String hashtag = message.substring(removeChild.length()+1);
				boolean valid = false;
				try {
					valid = validateHashtag(e, hashtag);
				} catch (TwitterException e1) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITTER_INVALID)).build()).queue();
					logger.warn("An invalid Twitter hashtag/user {} has been used in guild {}", message, e.getGuild().getId(), e1);
				}
				if(valid) {
					final int result = Azrael.SQLDeleteChildSubscription(subscription.getURL(), hashtag, e.getGuild().getIdLong());
					if(result > 0) {
						logger.info("User {} has removed the child hashtag {} from the parent hashtag/user {} in guild {}", hashtag, subscription.getURL(), e.getGuild().getId());
					}
					else if(result == 0) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITTER_OPTION_NOT_BOUND)+subscription.getURL()).build()).queue();
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Child hashtag {} couldn't be removed for the parent hashtag/user {} in guild {}", hashtag, subscription.getURL(), e.getGuild().getId());
						printMessage = false;
					}
				}
			}
		}
		
		if(printMessage) {
			Hashes.clearSubscriptions();
			printOptionsMessage(e, subscription);
			Hashes.addTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, type, "optionsUpdate").setObject(subscription));
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SUBSCRIBE.getColumn(), e.getMessage().getContentRaw());
		}
		else if(processAttempt) {
			Hashes.clearTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SUBSCRIBE.getColumn(), e.getMessage().getContentRaw());
		}
	}
	
	/**
	 * Display message to redirect the subscription to another text channel and then write to cache for the next step
	 * @param e
	 * @param type
	 */
	
	public static void channel(GuildMessageReceivedEvent e, String type) {
		final Object [] object = getSubscriptionList(e, type);
		final String out = (String)object[0];
		if(out.length() > 0) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_CHANNEL_HELP)+out.toString()).build()).queue();
			Hashes.addTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, type, "channel").setObject(object[1]));
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_NO_SUBSCRIPTIONS)).build()).queue();
			Hashes.clearTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
		}
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SUBSCRIBE.getColumn(), e.getMessage().getContentRaw());
	}
	
	/**
	 * Display the current channel, the subscription is writing into and then write to cache for the next step
	 * @param e
	 * @param type
	 * @param message
	 * @param subscriptions
	 */
	
	public static void channel(GuildMessageReceivedEvent e, String type, String message, ArrayList<Subscription> subscriptions) {
		if(message.matches("[0-9]{1,}")) {
			final long selection = Long.parseLong(message)-1;
			if(selection <= Integer.MAX_VALUE) {
				ArrayList<Subscription> subscriptionList = subscriptions;
				if(subscriptionList.size() >= selection+1 && selection >= 0) {
					final Subscription subscription = subscriptionList.get((int)selection);
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_CHANNEL_ADD).replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_NONE)).replace("{}", (subscription.getChannelID() > 0 ? "<#"+subscription.getChannelID()+">" : STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_CHANNEL_DEFAULT)))).build()).queue();
					Hashes.addTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, type, "channelUpdate").setObject(subscription));
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SUBSCRIBE.getColumn(), e.getMessage().getContentRaw());
				}
			}
		}
	}
	
	/**
	 * Update the channel where the selected subscription has to write into and clear all related cache
	 * @param e
	 * @param message
	 * @param subscription
	 */
	
	public static void channel(GuildMessageReceivedEvent e, String message, Subscription subscription) {
		long channelId = -1;
		if(!message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_NONE))) {
			message = message.replaceAll("[<>#]*", "");
			if(message.matches("[0-9]{1,}")) {
				TextChannel textChannel = e.getGuild().getTextChannelById(message);
				if(textChannel != null) {
					channelId = textChannel.getIdLong();
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_CHANNEL_ERR)).build()).queue();
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_CHANNEL_ERR)).build()).queue();
			}
		}
		else {
			channelId = 0;
		}
		if(channelId >= 0) {
			if(Azrael.SQLUpdateSubscriptionChannel(subscription.getURL(), e.getGuild().getIdLong(), channelId) > 0) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_CHANNEL_ADDED)).build()).queue();
				logger.info("User {} has set the alternative print channel {} for the subscription url {} in guild {}", e.getMember().getUser().getId(), channelId, subscription.getURL(), e.getGuild().getId());
				if(channelId > 0)
					SubscriptionUtils.startTimer(e.getJDA());
				else
					Hashes.clearSubscriptions();
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Alternative print channel {} couldn't be set for the subscription url {} in guild {}", channelId, subscription.getURL(), e.getGuild().getId());
			}
		}
		Hashes.clearTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SUBSCRIBE.getColumn(), e.getMessage().getContentRaw());
	}
	
	/**
	 * Display message to test the subscription and then write to cache for the next step
	 * @param e
	 * @param type
	 */
	
	public static void test(GuildMessageReceivedEvent e, String type) {
		final Object [] object = getSubscriptionList(e, type);
		final String out = (String)object[0];
		if(out.length() > 0) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_TEST_HELP)+out.toString()).build()).queue();
			Hashes.addTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, type, "test").setObject(object[1]));
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_NO_SUBSCRIPTIONS)).build()).queue();
			Hashes.clearTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
		}
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SUBSCRIBE.getColumn(), e.getMessage().getContentRaw());
	}
	
	/**
	 * Run a test with the selected subscription and clean all related cache
	 * @param e
	 * @param type
	 * @param message
	 * @param subscriptions
	 */
	
	public static void test(GuildMessageReceivedEvent e, String type, String message, ArrayList<Subscription> subscriptions) {
		if(message.matches("[0-9]{1,}")) {
			final long selection = Long.parseLong(message)-1;
			if(selection <= Integer.MAX_VALUE) {
				ArrayList<Subscription> subscriptionList = subscriptions;
				if(subscriptionList.size() >= selection+1 && selection >= 0) {
					final Subscription subscription = subscriptionList.get((int)selection);
					switch(type) {
						case "rss" -> RSSModel.ModelTest(e, subscription);
						case "twitter" -> TwitterModel.ModelTest(e, subscription);
						case "reddit" -> RedditModel.ModelTest(e, subscription);
					}
					Hashes.clearTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SUBSCRIBE.getColumn(), e.getMessage().getContentRaw());
				}
			}
		}
	}
	
	/**
	 * Display message to display all registered subscriptions and clear all related cache
	 * @param e
	 * @param type
	 */
	
	public static void display(GuildMessageReceivedEvent e, String type) {
		final Object [] object = getSubscriptionList(e, type);
		final String out = (String)object[0];
		if(out.length() > 0) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_DISPLAY_HELP)+out.toString()).build()).queue();
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_NO_SUBSCRIPTIONS)).build()).queue();
		}
		Hashes.clearTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SUBSCRIBE.getColumn(), e.getMessage().getContentRaw());
	}
	
	/**
	 * Interrupt the subscription command
	 * @param e
	 * @param message
	 */
	
	public static void interrupt(GuildMessageReceivedEvent e, String message) {
		e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_EXIT)).build()).queue();
		Hashes.clearTempCache("subscribe_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SUBSCRIBE.getColumn(), message);
	}
	
	/**
	 * Return all registered subscriptions by type 
	 * @param e
	 * @param type
	 * @return
	 */
	
	private static Object[] getSubscriptionList(GuildMessageReceivedEvent e, String type) {
		int counter = 1;
		StringBuilder out = new StringBuilder();
		int subscriptionType = 0;
		switch(type) {
			case "rss" -> subscriptionType = 1;
			case "twitter" -> subscriptionType = 2;
			case "reddit" -> subscriptionType = 3;
			case "youtube" -> subscriptionType = 4;
			case "twitch" -> subscriptionType = 5;
		}
		final ArrayList<Subscription> subscriptions = Azrael.SQLgetSubscriptions(e.getGuild().getIdLong(), subscriptionType);
		for(Subscription rss : subscriptions) {
			out.append((counter++)+": **"+rss.getURL()+"**\n");
		}
		Object [] object = {out.toString(), subscriptions}; 
		return object;
	}
	
	/**
	 * Print the options message for Twitter hashtags/users
	 * @param e
	 * @param subscription
	 */
	
	private static void printOptionsMessage(GuildMessageReceivedEvent e, Subscription subscription) {
		final String [] enablePictures = STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE_PICTURES).split(" ");
		final String [] enableVideos = STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE_VIDEOS).split(" ");
		final String [] enableText = STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE_TEXT).split(" ");
		final String [] disablePictures = STATIC.getTranslation(e.getMember(), Translation.PARAM_DISABLE_PICTURES).split(" ");
		
		StringBuilder out = new StringBuilder();
		for(final String childTweet : subscription.getChildTweets()) {
			out.append(childTweet+" ");
		}
		e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITTER_OPTIONS_1).replace("{}", subscription.getURL())
				+ STATIC.getTranslation(e.getMember(), Translation.TWITTER_OPTIONS_2).replace("{}", (subscription.getPictures() ? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_IS_ENABLED) : STATIC.getTranslation(e.getMember(), Translation.DISPLAY_IS_NOT_ENABLED)))
				+ STATIC.getTranslation(e.getMember(), Translation.TWITTER_OPTIONS_3).replace("{}", (subscription.getVideos() ? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_IS_ENABLED) : STATIC.getTranslation(e.getMember(), Translation.DISPLAY_IS_NOT_ENABLED)))
				+ STATIC.getTranslation(e.getMember(), Translation.TWITTER_OPTIONS_4).replace("{}", (subscription.getText() ? STATIC.getTranslation(e.getMember(), Translation.DISPLAY_IS_ENABLED) : STATIC.getTranslation(e.getMember(), Translation.DISPLAY_IS_NOT_ENABLED)))
				+ STATIC.getTranslation(e.getMember(), Translation.TWITTER_OPTIONS_5).replace("{}", (out.length() > 0 ? out.toString() : STATIC.getTranslation(e.getMember(), Translation.NOT_AVAILABLE)))
				+ STATIC.getTranslation(e.getMember(), Translation.TWITTER_OPTIONS_6)
					.replaceFirst("\\{\\}", enablePictures[0])
					.replaceFirst("\\{\\}", disablePictures[0])
					.replaceFirst("\\{\\}", enablePictures[1])
					.replaceFirst("\\{\\}", enableVideos[1])
					.replaceFirst("\\{\\}", enableText[1])
					.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_CHILD))
					.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE_CHILD))
					.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_EXIT))).build()).queue();
	}
	
	private static boolean validateHashtag(GuildMessageReceivedEvent e, String hashtag) throws TwitterException {
		Twitter twitter = STATIC.getTwitterFactory().getInstance();
		Query query = new Query(hashtag);
		QueryResult result = twitter.search(query);
		if(result.getTweets().size() > 0) {
			return true;
		}
		return false;
	}
}
