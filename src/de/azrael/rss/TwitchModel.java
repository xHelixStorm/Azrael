package de.azrael.rss;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumSet;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiParser;

import de.azrael.constructors.Messages;
import de.azrael.constructors.RSS;
import de.azrael.core.Hashes;
import de.azrael.enums.Translation;
import de.azrael.fileManagement.IniFileReader;
import de.azrael.sql.Azrael;
import de.azrael.sql.BotConfiguration;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class TwitchModel {
	private final static Logger logger = LoggerFactory.getLogger(TwitchModel.class);
	private final static String TOKEN_REQUEST = "https://id.twitch.tv/oauth2/token?client_id={}&client_secret={}&grant_type=client_credentials&scope=user:read:email";
	private final static String USER_REQUEST = "https://api.twitch.tv/helix/users?login=";
	private final static String STREAMS_REQUEST = "https://api.twitch.tv/helix/streams";
	private final static String USER_ID_ENDPOINT = "?user_id=";
	
	private static String accessToken = null;
	private static long tokenValidity = 0;
	
	private static HttpURLConnection buildConnection(URL url, String method, boolean bearer, boolean tokenHeader) throws IOException {
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod(method);
		con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
		if(bearer)
			con.setRequestProperty("Authorization", "Bearer "+accessToken);
		if(tokenHeader) {
			final String [] twitchKeys = IniFileReader.getTwitchKeys();
			con.setRequestProperty("Client-Id", twitchKeys[0]);
		}
		con.connect();
		return con;
	}
	
	private static JSONObject responseToJSON(HttpURLConnection con) throws IOException {
		BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
		StringBuilder out = new StringBuilder();
		String line;
		while((line = rd.readLine()) != null) {
			out.append(line+"\n");
		}
		rd.close();
		return new JSONObject(out.toString());
	}
	
	private static synchronized boolean generateToken() throws IOException {
		if(accessToken == null || tokenValidity - System.currentTimeMillis() < 0) {
			final String [] twitchKeys = IniFileReader.getTwitchKeys();
			if(twitchKeys[0] != null && twitchKeys[0].length() > 0 && twitchKeys[1] != null && twitchKeys[1].length() > 0) {
				URL url = new URL(TOKEN_REQUEST.replaceFirst("\\{\\}", twitchKeys[0]).replace("{}", twitchKeys[1]));
				HttpURLConnection con = buildConnection(url, "POST", false, false);
				
				if(con.getResponseCode() == 200) {
					JSONObject json = responseToJSON(con);
					if(json.has("access_token")) {
						accessToken = json.getString("access_token");
						tokenValidity = System.currentTimeMillis() + (json.getLong("expires_in")*1000);
						logger.info("Twitch access token created: {}", accessToken);
						return true;
					}
				}
			}
		}
		else
			return true;
		return false;
	}
	
	public static String[] findUser(String username) throws IOException {
		if(generateToken()) {
			URL url = new URL(USER_REQUEST+username);
			HttpURLConnection con = buildConnection(url, "GET", true, true);
			
			if(con.getResponseCode() == 200) {
				JSONObject json = responseToJSON(con);
				if(json.has("data")) {
					JSONArray array = json.getJSONArray("data");
					if(json.length() > 0) {
						json = array.getJSONObject(0);
						final String [] user = new String[2];
						user[0] = json.getString("id");
						user[1] = json.getString("display_name");
						return user;
					}
				}
			}
		}
		return null;
	}
	
	private static JSONArray fetchLiveStreams(String user_id) throws IOException {
		if(generateToken()) {
			URL url = null;
			if(user_id != null)
				url = new URL(STREAMS_REQUEST+USER_ID_ENDPOINT+user_id);
			else
				url = new URL(STREAMS_REQUEST);
			HttpURLConnection con = buildConnection(url, "GET", true, true);
			
			if(con.getResponseCode() == 200) {
				JSONObject json = responseToJSON(con);
				if(json.has("data")) {
					return json.getJSONArray("data");
				}
			}
		}
		return null;
	}
	
	public static boolean ModelParse(Guild guild, RSS twitch, long rss_channel, boolean defaultChannel) throws IOException {
		boolean success = false;
		final TextChannel textChannel = guild.getTextChannelById(rss_channel);
		if(textChannel != null) {
			if(guild.getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY) || STATIC.setPermissions(guild, textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY))) {
				if(generateToken()) {
					JSONArray data = fetchLiveStreams(twitch.getURL());
					if(data != null) {
						success = true;
						int count = 0;
						for(Object curData : data) {
							JSONObject streamer = (JSONObject)curData;
							final String streamId = streamer.getString("id");
							if(!Azrael.SQLIsSubscriptionDeleted(streamId)) {
								final String pubDate = streamer.getString("started_at");
								final String user = streamer.getString("user_name");
								final String user_id = streamer.getString("user_id");
								final String title = streamer.getString("title");
								final String url = "https://twitch.tv/"+streamer.getString("user_login");
								final String language = streamer.getString("language");
								final String game = streamer.getString("game_name");
								
								String out = twitch.getFormat().replace("{title}", title);
								out = out.replace("{pubDate}", pubDate);
								out = out.replace("{user}", user);
								out = out.replace("{user_id}", user_id);
								out = out.replace("{title}", title);
								out = out.replace("{url}", url);
								out = out.replace("{language}", language);
								out = out.replace("{game}", game);
								
								final String outMessage = EmojiParser.parseToUnicode(out);
								if(outMessage.length() > 0) {
									MessageHistory history = new MessageHistory(textChannel);
									history.retrievePast(100).queue(historyList -> {
										if(historyList.parallelStream().filter(f -> f.getContentRaw().replaceAll("[^a-zA-Z]", "").equals(outMessage.replaceAll("[^a-zA-Z]", ""))).findAny().orElse(null) == null)
											textChannel.sendMessage(outMessage).queue(m -> {
												Azrael.SQLInsertSubscriptionLog(m.getIdLong(), streamId);
												if(BotConfiguration.SQLgetBotConfigs(guild.getIdLong()).getCacheLog()) {
													Messages collectedMessage = new Messages();
													collectedMessage.setUserID(0);
													collectedMessage.setUsername(user);
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
								Azrael.SQLUpdateSubscriptionTimestamp(streamId);
							}
							count++;
							if(count == 5)
								break;
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
				else if(Azrael.SQLUpdateRSSChannel(twitch.getURL(), guild.getIdLong(), 0) > 0) {
					logger.info("Not existing alternative subscription channel {} has been removed in guild {}", rss_channel, guild.getIdLong());
				}
				Hashes.removeFilterLang(rss_channel);
				Hashes.removeChannels(guild.getIdLong());
			}
			else if(Azrael.SQLUpdateRSSChannel(twitch.getURL(), guild.getIdLong(), 0) > 0) {
				logger.info("Not existing alternative subscription channel {} has been removed in guild {}", rss_channel, guild.getIdLong());
			}
		}
		return success;
	}
	
	public static void ModelTest(GuildMessageReceivedEvent e, RSS twitch) throws IOException {
		if(generateToken()) {
			JSONArray data = fetchLiveStreams(null);
			if(data != null && data.length() > 0) {
				JSONObject streamer = data.getJSONObject(0);
				final String pubDate = streamer.getString("started_at");
				final String user = streamer.getString("user_name");
				final String user_id = streamer.getString("user_id");
				final String title = streamer.getString("title");
				final String url = "https://twitch.tv/"+streamer.getString("user_login");
				final String language = streamer.getString("language");
				final String game = streamer.getString("game_name");
				
				String out = twitch.getFormat().replace("{title}", title);
				out = out.replace("{pubDate}", pubDate);
				out = out.replace("{user}", user);
				out = out.replace("{user_id}", user_id);
				out = out.replace("{title}", title);
				out = out.replace("{url}", url);
				out = out.replace("{language}", language);
				out = out.replace("{game}", game);
				
				final String outMessage = EmojiParser.parseToUnicode(out);
				if(outMessage.length() > 0) {
					e.getChannel().sendMessage(outMessage).queue();
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITCH_NO_CONTENT)).build()).queue();
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITCH_NO_CONTENT)).build()).queue();
			}
		}
	}
}
