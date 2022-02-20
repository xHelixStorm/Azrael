package de.azrael.rss;

import java.awt.Color;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.vdurmont.emoji.EmojiParser;

import de.azrael.constructors.Messages;
import de.azrael.constructors.RSS;
import de.azrael.core.Hashes;
import de.azrael.enums.Translation;
import de.azrael.fileManagement.GuildIni;
import de.azrael.google.GoogleYoutube;
import de.azrael.sql.Azrael;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class YouTubeModel {
	private final static Logger logger = LoggerFactory.getLogger(YouTubeModel.class);

	public static boolean ModelParse(Guild guild, RSS rss, long rss_channel, boolean defaultChannel) throws Exception {
		boolean success = false;
		final TextChannel textChannel = guild.getTextChannelById(rss_channel);
		if(textChannel != null) {
			if(guild.getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY) || STATIC.setPermissions(guild, textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY))) {
				final SearchListResponse youtubeSearch = GoogleYoutube.searchYouTubeChannelVideos(GoogleYoutube.getService(), rss.getURL(), 5);
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
							
							String format = rss.getFormat();
							String out = format.replace("{channel}", channelName);
							out = out.replace("{channel_id}", channelId);
							out = out.replace("{pubDate}", pubDate);
							out = out.replace("{title}", title);
							out = out.replace("{description}", description);
							out = out.replace("{url}", url);
							out = out.replace("{video_id}", videoId);
							
							final String outMessage = EmojiParser.parseToUnicode(out);
							MessageHistory history = new MessageHistory(guild.getTextChannelById(rss_channel));
							history.retrievePast(100).queue(historyList -> {
								Message historyMessage = historyList.parallelStream().filter(f -> f.getContentRaw().replaceAll("[^a-zA-Z]", "").contains(outMessage.replaceAll("[^a-zA-Z]", ""))).findAny().orElse(null);
								if(historyMessage == null)
									guild.getTextChannelById(rss_channel).sendMessage(outMessage).queue(m -> {
										Azrael.SQLInsertSubscriptionLog(m.getIdLong(), item.getId().getVideoId());
										if(GuildIni.getCacheLog(guild.getIdLong())) {
											Messages collectedMessage = new Messages();
											collectedMessage.setUserID(0);
											collectedMessage.setUsername(channelName);
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
						else {
							Azrael.SQLUpdateSubscriptionTimestamp(item.getId().getVideoId());
						}
					}
				}
			}
			else {
				logger.warn("MESSAGE_WRITE and MESSAGE_HISTORY permission required to print into the subscription channel {} in guild {}", rss_channel, guild.getId());
			}
		}
		else {
			//Remove not anymore existing channel
			if(Azrael.SQLDeleteChannelConf(rss_channel, guild.getIdLong()) > 0) {
				Azrael.SQLDeleteChannel_Filter(rss_channel);
				Azrael.SQLDeleteChannels(rss_channel);
				if(defaultChannel) {
					logger.info("Not existing subscription channel {} has been removed in guild {}", rss_channel, guild.getIdLong());
				}
				else if(Azrael.SQLUpdateRSSChannel(rss.getURL(), guild.getIdLong(), 0) > 0) {
					logger.info("Not existing alternative subscription channel {} has been removed in guild {}", rss_channel, guild.getIdLong());
				}
				Hashes.removeFilterLang(rss_channel);
				Hashes.removeChannels(guild.getIdLong());
			}
			else if(Azrael.SQLUpdateRSSChannel(rss.getURL(), guild.getIdLong(), 0) > 0) {
				logger.info("Not existing alternative subscription channel {} has been removed in guild {}", rss_channel, guild.getIdLong());
			}
		}
		return success;
	}
	
	public static void ModelTest(GuildMessageReceivedEvent e, RSS rss) {
		SearchListResponse youtubeSearch;
		try {
			youtubeSearch = GoogleYoutube.searchYouTubeChannelVideos(GoogleYoutube.getService(), rss.getURL(), 2);
			if(youtubeSearch != null && youtubeSearch.getItems().size() > 0) {
				for(final SearchResult item : youtubeSearch.getItems()) {
					String channelName = item.getSnippet().getChannelTitle();
					String channelId = item.getSnippet().getChannelId();
					String pubDate = item.getSnippet().getPublishedAt().toString();
					String title = item.getSnippet().getTitle();
					String description = item.getSnippet().getDescription();
					String url = "https://www.youtube.com/watch?v="+item.getId().getVideoId();
					String videoId = item.getId().getVideoId();
					
					String format = rss.getFormat();
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
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_YOUTUBE_ERR_2)).build()).queue();
			}
		} catch (Exception e1) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_WEBSERVICE)+e1.getMessage()).build()).queue();
			logger.error("YouTube subscription couldn't be retrieved from {} in guild {}!", rss.getURL(), e.getGuild().getId(), e1);
		}
	}
}
