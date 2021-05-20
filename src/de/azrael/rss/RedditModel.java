package de.azrael.rss;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.EnumSet;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiParser;

import de.azrael.constructors.Messages;
import de.azrael.constructors.RSS;
import de.azrael.core.Hashes;
import de.azrael.enums.Translation;
import de.azrael.fileManagement.GuildIni;
import de.azrael.fileManagement.IniFileReader;
import de.azrael.sql.Azrael;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class RedditModel {
	private static final Logger logger = LoggerFactory.getLogger(RedditModel.class);
	private static final String TOKEN_REQUEST = "https://www.reddit.com/api/v1/access_token?grant_type=password&username={}&password={}";
	private static final String OAUTH_REQUEST = "https://oauth.reddit.com/";
	
	private static String accessToken = null;
	private static long tokenValidity = 0;
	
	public static synchronized boolean fetchRedditContent(GuildMessageReceivedEvent e, Guild guild, RSS reddit, long rss_channel, boolean defaultChannel) throws IOException {
		if(accessToken == null || tokenValidity - System.currentTimeMillis() < 0) {
			final String [] redditKeys = IniFileReader.getRedditKeys();
			if(redditKeys[0] != null && redditKeys[0].length() > 0 && redditKeys[1] != null && redditKeys[1].length() > 0 && redditKeys[2] != null && redditKeys[2].length() > 0 && redditKeys[3] != null && redditKeys[3].length() > 0) {
				URL url = new URL(TOKEN_REQUEST.replaceFirst("\\{\\}", redditKeys[0]).replace("{}", redditKeys[1]));
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con.setRequestMethod("POST");
				con.setRequestProperty("User-Agent", "de.azrael-bot/0.1 by HelixStorm");
				final String auth = redditKeys[2]+":"+redditKeys[3];
				con.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8)));
				con.connect();
				
				if(con.getResponseCode() == 200) {
					BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
					StringBuilder out = new StringBuilder();
					String line;
					while((line = rd.readLine()) != null) {
						out.append(line+"\n");
					}
					JSONObject json = new JSONObject(out.toString());
					if(json.has("access_token")) {
						accessToken = json.getString("access_token");
						tokenValidity = System.currentTimeMillis() + (json.getLong("expires_in")*1000);
						logger.info("Reddit access token created: {}", accessToken);
					}
					else 
						return false;
					rd.close();
				}
				else
					return false;
			}
			else
				return false;
		}
		
		if(tokenValidity - System.currentTimeMillis() > 0) {
			URL url = new URL(OAUTH_REQUEST+reddit.getURL());
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", "de.azrael-bot/0.1 by HelixStorm");
			con.setRequestProperty("Authorization", "bearer "+accessToken);
			con.connect();
			
			if(con.getResponseCode() == 200) {
				BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
				StringBuilder out = new StringBuilder();
				String line;
				while((line = rd.readLine()) != null) {
					out.append(line+"\n");
				}
				if(e == null)
					return ModelParse(new JSONObject(out.toString()), guild, reddit, rss_channel, defaultChannel);
				else 
					ModelTest(new JSONObject(out.toString()), e, reddit, rss_channel);
			}
		}
		return false;
	}
	
	private static boolean ModelParse(JSONObject response, Guild guild, RSS reddit, long rss_channel, boolean defaultChannel) {
		boolean success = false;
		final TextChannel textChannel = guild.getTextChannelById(rss_channel);
		if(textChannel != null) {
			if(guild.getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY) || STATIC.setPermissions(guild, textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY))) {
				JSONObject parentData = response.getJSONObject("data");
				JSONArray children = parentData.getJSONArray("children");
				if(!children.isEmpty()) {
					success = true;
					final String format = reddit.getFormat();
					final var prohibitedSubscriptions = Azrael.SQLgetSubscriptionBlacklist(guild.getIdLong());
					for(final Object iteration : children) {
						final JSONObject result = (JSONObject)iteration;
						final String kind = result.getString("kind");
						final JSONObject data = result.getJSONObject("data");
						final String permaLink = data.getString("permalink");
						final String author = data.getString("author");
						
						if(prohibitedSubscriptions.parallelStream().filter(f -> author.equals(f)).findAny().orElse(null) != null)
		        			continue;
						
						if(!Azrael.SQLIsSubscriptionDeleted(permaLink)) {
							String message = null;
							switch(kind) {
								case "t1" -> message = buildMessageBodyT1(data, format);
								case "t3" -> message = buildMessageBodyT3(data, format);
							}
							
							final String outMessage = message;
							if(outMessage.length() > 0) {
								MessageHistory history = new MessageHistory(textChannel);
								history.retrievePast(100).queue(historyList -> {
									if(historyList.parallelStream().filter(f -> f.getContentRaw().replaceAll("[^a-zA-Z]", "").equals(outMessage.replaceAll("[^a-zA-Z]", ""))).findAny().orElse(null) == null)
										textChannel.sendMessage(outMessage).queue(m -> {
											Azrael.SQLInsertSubscriptionLog(m.getIdLong(), permaLink);
											if(GuildIni.getCacheLog(guild.getIdLong())) {
												Messages collectedMessage = new Messages();
												collectedMessage.setUserID(0);
												collectedMessage.setUsername(author);
												collectedMessage.setGuildID(guild.getIdLong());
												collectedMessage.setChannelID(rss_channel);
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
						else {
							Azrael.SQLUpdateSubscriptionTimestamp(permaLink);
						}
					}
				}
			}
			else {
				logger.warn("MESSAGE_WRITE and MESSAGE_HISTORY permission required to print into the subscription channel {} in guild {}", rss_channel, guild.getId());
			}
		}
		else {
			//remove not anymore existing channel
			if(Azrael.SQLDeleteChannelConf(rss_channel, guild.getIdLong()) > 0) {
				Azrael.SQLDeleteChannel_Filter(rss_channel);
				Azrael.SQLDeleteChannels(rss_channel);
				if(defaultChannel) {
					logger.info("Not existing subscription channel {} has been removed in guild {}", rss_channel, guild.getIdLong());
				}
				else if(Azrael.SQLUpdateRSSChannel(reddit.getURL(), guild.getIdLong(), 0) > 0) {
					logger.info("Not existing alternative subscription channel {} has been removed in guild {}", rss_channel, guild.getIdLong());
				}
				Hashes.removeFilterLang(rss_channel);
				Hashes.removeChannels(guild.getIdLong());
			}
			else if(Azrael.SQLUpdateRSSChannel(reddit.getURL(), guild.getIdLong(), 0) > 0) {
				logger.info("Not existing alternative subscription channel {} has been removed in guild {}", rss_channel, guild.getIdLong());
			}
		}
		return success;
	}
	
	private static void ModelTest(JSONObject response, GuildMessageReceivedEvent e, RSS reddit, long rss_channel) {
		TextChannel textChannel = e.getGuild().getTextChannelById(rss_channel);
		JSONObject parentData = response.getJSONObject("data");
		JSONArray children = parentData.getJSONArray("children");
		if(!children.isEmpty()) {
			for(final Object iteration : children) {
				final JSONObject result = (JSONObject)iteration;
				final String kind = result.getString("kind");
				final JSONObject data = result.getJSONObject("data");
				
				String message = null;
				switch(kind) {
					case "t1" -> message = buildMessageBodyT1(data, reddit.getFormat());
					case "t3" -> message = buildMessageBodyT3(data, reddit.getFormat());
				}
				
				final String outMessage = message;
				if(outMessage.length() > 0) {
					textChannel.sendMessage(message).queue();
					return;
				}
			}
			textChannel.sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REDDIT_NO_CONTENT)).build()).queue();
		}
	}
	
	private static String buildMessageBodyT1(JSONObject data, String format) {
		if(data.isNull("mod_reason_by")) {
			String subreddit = "";
			String author = "";
			String title = "";
			String description = "";
			String pubDate = "";
			String media = "";
			String url = "https://reddit.com";
			
			if(data.has("subreddit") && !data.isNull("subreddit"))
				subreddit = data.getString("subreddit");
			if(data.has("author") && !data.isNull("author"))
				author = data.getString("author");
			if(data.has("link_title") && !data.isNull("link_title"))
				title = data.getString("link_title");
			if(data.has("body") && data.getString("body").length() > 0) {
				String text = data.getString("body");
				Matcher matcher = Pattern.compile("\\[[\\w\\d*\\/\\\\=:.?&;\\s,\\]\\(]*\\)").matcher(text);
				while(matcher.find()) {
					String subString = matcher.group();
					Matcher matcher2 = Pattern.compile("(?<=\\()(.*?)(?=\\))").matcher(subString);
					if(matcher2.find()) {
						String foundUrl = matcher2.group();
						if(foundUrl.contains("preview.redd.it"))
							text = text.replace(subString, "");
						else
							text = text.replace(subString, foundUrl);
					}
				}
				text = text.trim().replaceAll("&amp;", "").replaceAll("&lt;", "").replaceAll("&gt;", "");
				description = (text.length() > 1800 ? text.substring(0, 1800)+"...": text);
			}
			if(data.has("created_utc") && !data.isNull("created_utc")) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault());
				dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
				pubDate = dateFormat.format(new Timestamp(data.getLong("created_utc")*1000))+" UTC";
			}
			if(data.has("permalink") && data.getString("permalink").length() > 0)
				url += data.getString("permalink");
			
			if(subreddit.length() > 0 || author.length() > 0 || title.length() > 0 || description.length() > 0 || pubDate.length() > 0 || url.length() > 18) {
				String out = format.replace("{title}", title);
				out = out.replace("{subreddit}", subreddit);
				out = out.replace("{author}", author);
				out = out.replace("{description}", description);
				out = out.replace("{pubDate}", pubDate);
				out = out.replace("{media}", media);
				out = out.replace("{url}", url);
				return EmojiParser.parseToUnicode(out);
			}
		}
		return "";
	}
	
	private static String buildMessageBodyT3(JSONObject data, String format) {
		if(data.isNull("removed_by_category") && data.isNull("removed_by")) {
			String subreddit = "";
			String author = "";
			String title = "";
			String description = "";
			String pubDate = "";
			String media = "";
			String url = "https://reddit.com";
			
			if(data.has("subreddit") && !data.isNull("subreddit"))
				subreddit = data.getString("subreddit");
			if(data.has("author") && !data.isNull("author"))
				author = data.getString("author");
			if(data.has("title") && !data.isNull("title"))
				title = data.getString("title");
			if(data.has("selftext") && data.getString("selftext").length() > 0) {
				String text = data.getString("selftext");
				Matcher matcher = Pattern.compile("\\[[\\w\\d*\\/\\\\=:.?&;\\s,\\]\\(]*\\)").matcher(text);
				while(matcher.find()) {
					String subString = matcher.group();
					Matcher matcher2 = Pattern.compile("(?<=\\()(.*?)(?=\\))").matcher(subString);
					if(matcher2.find()) {
						String foundUrl = matcher2.group();
						if(foundUrl.contains("preview.redd.it"))
							text = text.replace(subString, "");
						else
							text = text.replace(subString, foundUrl);
					}
				}
				text = text.trim().replaceAll("&amp;", "").replaceAll("&lt;", "").replaceAll("&gt;", "");
				description = (text.length() > 1800 ? text.substring(0, 1800)+"...": text)+"\n";
				String urlTag = data.getString("url");
				if(!urlTag.contains("www.reddit.com"))
					description += urlTag;
			}
			else if(data.has("url") && !data.isNull("url")) {
				final String urlTag = data.getString("url");
				if(!urlTag.contains("reddit.com"))
					description = urlTag;
			}
			if(data.has("created_utc") && !data.isNull("created_utc")) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault());
				dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
				pubDate = dateFormat.format(new Timestamp(data.getLong("created_utc")*1000))+" UTC";
			}
			if(data.has("media") && !data.isNull("media") && data.has("is_video") && data.getBoolean("is_video")) {
				JSONObject mediaTag = data.getJSONObject("media");
				if(mediaTag.has("reddit_video") && !mediaTag.isNull("reddit_video")) {
					JSONObject reddit_video = mediaTag.getJSONObject("reddit_video");
					media = reddit_video.getString("fallback_url");
				}
			}
			if(data.has("permalink") && data.getString("permalink").length() > 0)
				url += data.getString("permalink");
			
			if(subreddit.length() > 0 || author.length() > 0 || title.length() > 0 || description.length() > 0 || pubDate.length() > 0 || media.length() > 0 || url.length() > 18) {
				String out = format.replace("{title}", title);
				out = out.replace("{subreddit}", subreddit);
				out = out.replace("{author}", author);
				out = out.replace("{description}", description);
				out = out.replace("{pubDate}", pubDate);
				out = out.replace("{media}", media);
				out = out.replace("{url}", url);
				return EmojiParser.parseToUnicode(out);
			}
		}
		return "";
	}
}
