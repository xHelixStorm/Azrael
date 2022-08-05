package de.azrael.subscription;

import java.awt.Color;
import java.util.EnumSet;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiParser;

import de.azrael.constructors.Subscription;
import de.azrael.enums.Translation;
import de.azrael.google.GoogleYoutube;
import de.azrael.sql.Azrael;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class YouTubeModel {
	private final static Logger logger = LoggerFactory.getLogger(YouTubeModel.class);

	public static boolean ModelParse(Guild guild, Subscription subscription, long subscriptionChannel, boolean defaultChannel) throws Exception {
		boolean success = false;
		final TextChannel textChannel = guild.getTextChannelById(subscriptionChannel);
		if(textChannel != null) {
			if(guild.getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY) || STATIC.setPermissions(guild, textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY))) {
				JSONArray contents = GoogleYoutube.collectYouTubeVideos(subscription.getName());
				if(contents != null && contents.length() > 0) {
					String channelId = "";
					String channelName = "";
					for(int i = 0; i < contents.length(); i++) {
						JSONObject curJson = contents.getJSONObject(i);
						if(!success && curJson.has("channelRenderer") && curJson.getJSONObject("channelRenderer").getString("channelId").equals(subscription.getURL())) {
							JSONObject channel = curJson.getJSONObject("channelRenderer");
							channelId = channel.getString("channelId");
							channelName = channel.getJSONObject("title").getString("simpleText");
							success = true;
						}
						if(success && curJson.has("shelfRenderer")) {
							JSONObject json = curJson.getJSONObject("shelfRenderer");
							json = json.getJSONObject("content");
							json = json.getJSONObject("verticalListRenderer");
							JSONArray items = json.getJSONArray("items");
							for(int index = 0; index < 10 && index < items.length(); index++) {
								json = items.getJSONObject(index);
								if(json.has("videoRenderer")) {
									json = json.getJSONObject("videoRenderer");
									final String browseId = findBrowseId(json);
									if(browseId != null && browseId.equals(channelId) && !Azrael.SQLIsSubscriptionDeleted(json.getString("videoId"), guild.getIdLong())) {
										String title = json.getJSONObject("title").getJSONArray("runs").getJSONObject(0).getString("text");
										String description = json.getJSONArray("detailedMetadataSnippets").getJSONObject(0).getJSONObject("snippetText").getJSONArray("runs").getJSONObject(0).getString("text");
										String videoId = json.getString("videoId");
										String url = "https://www.youtube.com/watch?v="+videoId;
										
										String format = subscription.getFormat();
										String out = format.replace("{channel}", channelName);
										out = out.replace("{channel_id}", channelId);
										out = out.replace("{title}", title);
										out = out.replace("{description}", description);
										out = out.replace("{url}", url);
										out = out.replace("{video_id}", videoId);
										
										final String outMessage = EmojiParser.parseToUnicode(out);
										if(outMessage.length() > 0) {
											SubscriptionUtils.postSubscriptionToServerChannel(guild, textChannel, outMessage, videoId, channelName);
										}
									}
								}
							}
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
			//Remove not anymore existing channel
			SubscriptionUtils.deleteRemovedChannel(subscriptionChannel, defaultChannel, subscription.getURL(), guild);
		}
		return success;
	}
	
	private static String findBrowseId(JSONObject parentJson) {
		String browseId = null;
		if(parentJson.has("longBylineText")) {
			var json = parentJson.getJSONObject("longBylineText");
			return json.getJSONArray("runs").getJSONObject(0).getJSONObject("navigationEndpoint").getJSONObject("browseEndpoint").getString("browseId");
		}
		if(parentJson.has("ownerText")) {
			var json = parentJson.getJSONObject("ownerText");
			return json.getJSONArray("runs").getJSONObject(0).getJSONObject("navigationEndpoint").getJSONObject("browseEndpoint").getString("browseId");
		}
		if(parentJson.has("shortBylineText")) {
			var json = parentJson.getJSONObject("shortBylineText");
			return json.getJSONArray("runs").getJSONObject(0).getJSONObject("navigationEndpoint").getJSONObject("browseEndpoint").getString("browseId");
		}
		return browseId;
	}
	
	public static void ModelTest(GuildMessageReceivedEvent e, Subscription subscription) {
		try {
			JSONArray contents = GoogleYoutube.collectYouTubeVideos(subscription.getName());
			if(contents != null && contents.length() > 0) {
				String channelId = "";
				String channelName = "";
				boolean success = false;
				for(int i = 0; i < 2; i++) {
					JSONObject curJson = contents.getJSONObject(i);
					if(!success && curJson.has("channelRenderer") && curJson.getJSONObject("channelRenderer").getString("channelId").equals(subscription.getURL())) {
						JSONObject channel = curJson.getJSONObject("channelRenderer");
						channelId = channel.getString("channelId");
						channelName = channel.getJSONObject("title").getString("simpleText");
						success = true;
					}
					if(success && curJson.has("shelfRenderer")) {
						JSONObject json = curJson.getJSONObject("shelfRenderer");
						json = json.getJSONObject("content");
						json = json.getJSONObject("verticalListRenderer");
						JSONArray items = json.getJSONArray("items");
						for(int index = 0; index < 1; index++) {
							json = items.getJSONObject(index);
							if(json.has("videoRenderer")) {
								json = json.getJSONObject("videoRenderer");
								String title = json.getJSONObject("title").getJSONArray("runs").getJSONObject(0).getString("text");
								String description = json.getJSONArray("detailedMetadataSnippets").getJSONObject(0).getJSONObject("snippetText").getJSONArray("runs").getJSONObject(0).getString("text");
								String videoId = json.getString("videoId");
								String url = "https://www.youtube.com/watch?v="+videoId;
								
								String format = subscription.getFormat();
								String out = format.replace("{channel}", channelName);
								out = out.replace("{channel_id}", channelId);
								out = out.replace("{title}", title);
								out = out.replace("{description}", description);
								out = out.replace("{url}", url);
								out = out.replace("{video_id}", videoId);
								
								final String outMessage = EmojiParser.parseToUnicode(out);
								e.getChannel().sendMessage(outMessage).queue();
							}
						}
					}
					else if(success && curJson.has("videoRenderer")) {
						JSONObject json = curJson.getJSONObject("videoRenderer");
						String title = json.getJSONObject("title").getJSONArray("runs").getJSONObject(0).getString("text");
						String description = json.getJSONArray("detailedMetadataSnippets").getJSONObject(0).getJSONObject("snippetText").getJSONArray("runs").getJSONObject(0).getString("text");
						String videoId = json.getString("videoId");
						String url = "https://www.youtube.com/watch?v="+videoId;
						
						String format = subscription.getFormat();
						String out = format.replace("{channel}", channelName);
						out = out.replace("{channel_id}", channelId);
						out = out.replace("{title}", title);
						out = out.replace("{description}", description);
						out = out.replace("{url}", url);
						out = out.replace("{video_id}", videoId);
						
						final String outMessage = EmojiParser.parseToUnicode(out);
						e.getChannel().sendMessage(outMessage).queue();
					}
				}
				if(!success) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.YOUTUBE_NOT_FOUND)).build()).queue();
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.YOUTUBE_NOT_FOUND)).build()).queue();
			}
		} catch (Exception e1) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("YouTube subscription couldn't be retrieved from {} in guild {}", subscription.getURL(), e.getGuild().getId(), e1);
		}
	}
}
