package de.azrael.subscription;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.EnumSet;

import org.json.JSONArray;
import org.json.JSONObject;
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

public class TwitchModel {
	private final static Logger logger = LoggerFactory.getLogger(TwitchModel.class);
	private final static String TOKEN_REQUEST = "https://id.twitch.tv/oauth2/token?client_id={}&client_secret={}&grant_type=client_credentials&scope=user:read:email";
	private final static String USER_REQUEST = "https://api.twitch.tv/helix/users?login=";
	private final static String STREAMS_REQUEST = "https://api.twitch.tv/helix/streams";
	private final static String VIDEOS_REQUEST = "https://api.twitch.tv/helix/videos";
	private final static String USER_ID_ENDPOINT = "?user_id=";
	private final static String FIRST_ENDPOINT = "&first=";
	
	private static final String TWITCH_CLIENT_ID = "TWITCH_CLIENT_ID";
	private static final String TWITCH_CLIENT_SECRET = "TWITCH_CLIENT_SECRET";
	
	private static String accessToken = null;
	private static long tokenValidity = 0;
	
	private static HttpURLConnection buildConnection(URL url, String method, boolean bearer, boolean tokenHeader) throws IOException {
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod(method);
		con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
		if(bearer)
			con.setRequestProperty("Authorization", "Bearer "+accessToken);
		if(tokenHeader) {
			con.setRequestProperty("Client-Id", System.getProperty(TWITCH_CLIENT_ID));
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
			final String clientId = System.getProperty(TWITCH_CLIENT_ID);
			final String clientSecret = System.getProperty(TWITCH_CLIENT_SECRET);
			if(clientId != null && clientId.length() > 0 && clientSecret != null && clientSecret.length() > 0) {
				URL url = new URL(TOKEN_REQUEST.replaceFirst("\\{\\}", clientId).replace("{}", clientSecret));
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
	
	private static JSONArray fetchStreamContent(String user_id, boolean liveVideos) throws IOException {
		if(generateToken()) {
			URL url = null;
			if(liveVideos)
				url = new URL(STREAMS_REQUEST+USER_ID_ENDPOINT+user_id);
			else
				url = new URL(VIDEOS_REQUEST+USER_ID_ENDPOINT+user_id+FIRST_ENDPOINT+"1");
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
	
	public static boolean ModelParse(Guild guild, Subscription subscription, long subscriptionChannel, boolean defaultChannel) throws IOException {
		boolean success = false;
		final TextChannel textChannel = guild.getTextChannelById(subscriptionChannel);
		if(textChannel != null) {
			if(guild.getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY) || STATIC.setPermissions(guild, textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY))) {
				if(generateToken()) {
					JSONArray data = fetchStreamContent(subscription.getURL(), true);
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
								String game = streamer.getString("game_name");
								if(game.trim().length() == 0) {
									game = STATIC.getTranslation2(guild, Translation.NOT_AVAILABLE);
								}
								
								String out = subscription.getFormat().replace("{title}", title);
								out = out.replace("{pubDate}", pubDate);
								out = out.replace("{user}", user);
								out = out.replace("{user_id}", user_id);
								out = out.replace("{title}", title);
								out = out.replace("{url}", url);
								out = out.replace("{language}", language);
								out = out.replace("{game}", game);
								
								final String outMessage = EmojiParser.parseToUnicode(out);
								if(outMessage.length() > 0) {
									SubscriptionUtils.postSubscriptionToServerChannel(guild, textChannel, outMessage, streamId, user);
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
				logger.warn("MESSAGE_WRITE and MESSAGE_HISTORY permission required to print into the subscription channel {} in guild {}", subscriptionChannel, guild.getId());
			}
		}
		else {
			//remove not anymore existing channel
			SubscriptionUtils.deleteRemovedChannel(subscriptionChannel, defaultChannel, subscription.getURL(), guild);
		}
		return success;
	}
	
	public static void ModelTest(GuildMessageReceivedEvent e, Subscription subscription) {
		try {
			if(generateToken()) {
				JSONArray data = fetchStreamContent(subscription.getURL(), false);
				if(data != null && data.length() > 0) {
					JSONObject streamer = data.getJSONObject(0);
					final String pubDate = streamer.getString("published_at");
					final String user = streamer.getString("user_name");
					final String user_id = streamer.getString("user_id");
					final String title = streamer.getString("title");
					final String url = streamer.getString("url");
					final String language = streamer.getString("language");
					final String game = STATIC.getTranslation(e.getMember(), Translation.NOT_AVAILABLE);
					
					String out = subscription.getFormat().replace("{title}", title);
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
		} catch (Exception e1) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Subscription couldn't be retrieved from {} in guild {}", subscription.getURL(), e.getGuild().getId(), e1);
		}
	}
}
