package de.azrael.subscription;

import java.awt.Color;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
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
	private final static ConcurrentHashMap<String, Integer> hourOfSubscription = new ConcurrentHashMap<String, Integer>();

	public static boolean ModelParse(Guild guild, Subscription subscription, long subscriptionChannel, boolean defaultChannel) throws Exception {
		boolean success = false;
		
		//restrict execution to 4 hours a day because YouTube subscriptions cost a lot.
		Calendar calendar = Calendar.getInstance();
		final int hour = calendar.get(Calendar.HOUR_OF_DAY);
		final Integer hourOfDay = hourOfSubscription.get(guild.getId()+"_"+subscription.getURL());
		if(hour % 4 == 0 && hourOfDay != null && hourOfDay != hour) {
			hourOfSubscription.put(guild.getId()+"_"+subscription.getURL(), hour);
		}
		else
			return false;
		
		final TextChannel textChannel = guild.getTextChannelById(subscriptionChannel);
		if(textChannel != null) {
			if(guild.getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY) || STATIC.setPermissions(guild, textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY))) {
				final SearchListResponse youtubeSearch = GoogleYoutube.searchYouTubeChannelVideos(GoogleYoutube.getService(), subscription.getURL(), 5);
				if(youtubeSearch != null && youtubeSearch.getItems().size() > 0) {
					success = true;
					for(final SearchResult item : youtubeSearch.getItems()) {
						if(!Azrael.SQLIsSubscriptionDeleted(item.getId().getVideoId())) {
							String channelName = item.getSnippet().getChannelTitle();
							String channelId = item.getSnippet().getChannelId();
							String pubDate = item.getSnippet().getPublishedAt().toString();
							String title = item.getSnippet().getTitle();
							String description = item.getSnippet().getDescription();
							String url = "https://www.youtube.com/watch?v="+item.getId().getVideoId();
							String videoId = item.getId().getVideoId();
							
							String format = subscription.getFormat();
							String out = format.replace("{channel}", channelName);
							out = out.replace("{channel_id}", channelId);
							out = out.replace("{pubDate}", pubDate);
							out = out.replace("{title}", title);
							out = out.replace("{description}", description);
							out = out.replace("{url}", url);
							out = out.replace("{video_id}", videoId);
							
							final String outMessage = EmojiParser.parseToUnicode(out);
							if(outMessage.length() > 0) {
								SubscriptionUtils.postSubscriptionToServerChannel(guild, textChannel, outMessage, item.getId().getVideoId(), channelName);
							}
						}
						else {
							Azrael.SQLUpdateSubscriptionTimestamp(item.getId().getVideoId());
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
		SearchListResponse youtubeSearch;
		try {
			youtubeSearch = GoogleYoutube.searchYouTubeChannelVideos(GoogleYoutube.getService(), subscription.getURL(), 2);
			if(youtubeSearch != null && youtubeSearch.getItems().size() > 0) {
				for(final SearchResult item : youtubeSearch.getItems()) {
					String channelName = item.getSnippet().getChannelTitle();
					String channelId = item.getSnippet().getChannelId();
					String pubDate = item.getSnippet().getPublishedAt().toString();
					String title = item.getSnippet().getTitle();
					String description = item.getSnippet().getDescription();
					String url = "https://www.youtube.com/watch?v="+item.getId().getVideoId();
					String videoId = item.getId().getVideoId();
					
					String format = subscription.getFormat();
					String out = format.replace("{channel}", channelName);
					out = out.replace("{channel_id}", channelId);
					out = out.replace("{pubDate}", pubDate);
					out = out.replace("{title}", title);
					out = out.replace("{description}", description);
					out = out.replace("{url}", url);
					out = out.replace("{video_id}", videoId);
					
					final String outMessage = EmojiParser.parseToUnicode(out);
					e.getChannel().sendMessage(outMessage).queue();
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.YOUTUBE_NOT_FOUND)).build()).queue();
			}
		} catch (Exception e1) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_WEBSERVICE)+e1.getMessage()).build()).queue();
			logger.error("YouTube subscription couldn't be retrieved from {} in guild {}", subscription.getURL(), e.getGuild().getId(), e1);
		}
	}
}
