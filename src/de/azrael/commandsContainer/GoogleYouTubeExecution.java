package de.azrael.commandsContainer;

import java.awt.Color;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import de.azrael.constructors.Cache;
import de.azrael.constructors.Subscription;
import de.azrael.core.Hashes;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.google.GoogleYoutube;
import de.azrael.sql.Azrael;
import de.azrael.subscription.SubscriptionUtils;
import de.azrael.subscription.YouTubeModel;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class GoogleYouTubeExecution {
	private final static Logger logger = LoggerFactory.getLogger(GoogleYouTubeExecution.class);
	private final static EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
	
	public static void runTask(GuildMessageReceivedEvent e, final String key) {
		e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_YOUTUBE_HELP)).build()).queue();
		Hashes.addTempCache(key, new Cache(180000, "youtube-selection"));
	}
	
	public static void add(GuildMessageReceivedEvent e, final String key) {
		e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_YOUTUBE_ADD)).build()).queue();
		Hashes.addTempCache(key, new Cache(180000, "youtube-add"));
	}
	
	public static void add(GuildMessageReceivedEvent e, final String key, String channel_id) {
		if(!channel_id.contains(" ")) {
			SearchListResponse result = null;
			try {
				result = GoogleYoutube.searchYouTubeChannelVideos(GoogleYoutube.getService(), channel_id, 1);
			} catch (Exception e1) {
				logger.error("An error occurred while calling the YouTube webservice in guild {}", e.getGuild().getId(), e1);
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_WEBSERVICE)+e1.getMessage()).build()).queue();
			}
			
			if(result != null) {
				if(result.getItems().size() > 0) {
					final SearchResult item = result.getItems().get(0);
					if(Azrael.SQLInsertSubscription(item.getSnippet().getChannelId(), e.getGuild().getIdLong(), 4, item.getSnippet().getChannelTitle()) > 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_YOUTUBE_ADD_2).replaceFirst("\\{\\}", item.getSnippet().getChannelTitle()).replace("{}", item.getSnippet().getChannelId())).build()).queue();
						logger.info("User {} has subscribed the YouTube channel {} in guild {}", e.getMember().getUser().getId(), item.getSnippet().getChannelId(), e.getGuild().getId());
						Hashes.addTempCache(key, new Cache(180000, "youtube-selection"));
						SubscriptionUtils.startTimer(e.getJDA());
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("YouTube channel id {} couldn't be registered in guild {}", item.getSnippet().getChannelId(), e.getGuild().getId());
						Hashes.clearTempCache(key);
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_YOUTUBE_ADD_ERR)).build()).queue();
					Hashes.addTempCache(key, new Cache(180000, "youtube-add"));
				}
			}
		}
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
	}
	
	public static void remove(GuildMessageReceivedEvent e, final String key) {
		final var channels = Azrael.SQLgetSubscriptions(e.getGuild().getIdLong(), 4);
		if(channels.size() > 0) {
			StringBuilder out = new StringBuilder();
			int count = 0;
			for(final var channel : channels) {
				out.append("**"+(++count)+"**: "+channel.getName()+"\n");
			}
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_YOUTUBE_REMOVE)+out.toString()).build()).queue();
			Hashes.addTempCache(key, new Cache(180000, "youtube-remove").setObject(channels));
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_YOUTUBE_ERR)).build()).queue();
			Hashes.addTempCache(key, new Cache(180000, "youtube-selection"));
		}
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
	}
	
	public static void remove(GuildMessageReceivedEvent e, final String key, String number, Cache cache) {
		if(number.matches("[0-9]*")) {
			@SuppressWarnings("unchecked")
			final ArrayList<Subscription> channels = (ArrayList<Subscription>)cache.getObject();
			final int selection = Integer.parseInt(number)-1;
			if(selection >= 0 && selection < channels.size()) {
				final var channel = channels.get(selection);
				if(Azrael.SQLDeleteSubscription(channel.getURL(), e.getGuild().getIdLong()) > 0) {
					Hashes.clearSubscriptions();
					Hashes.removeSubscriptionStatus(e.getGuild().getId()+"_"+channel.getURL());
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_YOUTUBE_REMOVE_2).replace("{}", channel.getName())).build()).queue();
					logger.info("User {} has removed the YouTube channel subscription {} in guild {}", e.getMember().getUser().getId(), channel.getURL(), e.getGuild().getId());
					Hashes.addTempCache(key, new Cache(180000, "youtube-selection"));
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("YouTube channel id {} couldn't be removed in guild {}", channel.getURL(), e.getGuild().getId());
					Hashes.clearTempCache(key);
				}
			}
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
		}
	}
	
	public static void format(GuildMessageReceivedEvent e, String key) {
		final var channels = Azrael.SQLgetSubscriptions(e.getGuild().getIdLong(), 4);
		if(channels.size() > 0) {
			StringBuilder out = new StringBuilder();
			int count = 0;
			for(final var channel : channels) {
				out.append("**"+(++count)+"**: "+channel.getName()+"\n");
			}
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_YOUTUBE_FORMAT)+out.toString()).build()).queue();
			Hashes.addTempCache(key, new Cache(180000, "youtube-format").setObject(channels));
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_YOUTUBE_ERR)).build()).queue();
			Hashes.addTempCache(key, new Cache(180000, "youtube-selection"));
		}
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
	}
	
	public static void format(GuildMessageReceivedEvent e, final String key, String number, Cache cache) {
		if(number.matches("[0-9]*")) {
			@SuppressWarnings("unchecked")
			final ArrayList<Subscription> channels = (ArrayList<Subscription>)cache.getObject();
			final int selection = Integer.parseInt(number)-1;
			if(selection >= 0 && selection < channels.size()) {
				final var channel = channels.get(selection);
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_YOUTUBE_FORMAT_2)+"```"+channel.getFormat()+"```").build()).queue();
				Hashes.addTempCache(key, cache.setExpiration(180000).updateDescription("youtube-format-update").setObject(channel));
			}
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
		}
	}
	
	public static void formatUpdate(GuildMessageReceivedEvent e, final String key, String format, Cache cache) {
		final Subscription channel = (Subscription)cache.getObject();
		if(Azrael.SQLUpdateSubscriptionFormat(channel.getURL(), e.getGuild().getIdLong(), format) > 0) {
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_YOUTUBE_FORMAT_3).replace("{}", channel.getName())).build()).queue();
			logger.info("User {} has updated the format of the YouTube channel id {} in guild {}", e.getMember().getUser().getId(), channel.getURL(), e.getGuild().getIdLong());
			Hashes.addTempCache(key, new Cache(180000, "youtube-selection"));
			Hashes.clearSubscriptions();
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("The format of YouTube channel id {} couldn't be updated in guild {}", channel.getURL(), e.getGuild().getId());
			Hashes.clearTempCache(key);
		}
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
	}
	
	public static void channel(GuildMessageReceivedEvent e, String key) {
		final var channels = Azrael.SQLgetSubscriptions(e.getGuild().getIdLong(), 4);
		if(channels.size() > 0) {
			StringBuilder out = new StringBuilder();
			int count = 0;
			for(final var channel : channels) {
				out.append("**"+(++count)+"**: "+channel.getName()+"\n");
			}
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_YOUTUBE_CHANNEL)+out.toString()).build()).queue();
			Hashes.addTempCache(key, new Cache(180000, "youtube-channel").setObject(channels));
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_YOUTUBE_ERR)).build()).queue();
			Hashes.addTempCache(key, new Cache(180000, "youtube-selection"));
		}
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
	}
	
	public static void channel(GuildMessageReceivedEvent e, final String key, String number, Cache cache) {
		if(number.matches("[0-9]*")) {
			@SuppressWarnings("unchecked")
			final ArrayList<Subscription> channels = (ArrayList<Subscription>)cache.getObject();
			final int selection = Integer.parseInt(number)-1;
			if(selection >= 0 && selection < channels.size()) {
				final var channel = channels.get(selection);
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_YOUTUBE_CHANNEL_2).replace("{}", (channel.getChannelID() > 0 ? "<#"+channel.getChannelID()+">" : STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_CHANNEL_DEFAULT)))).build()).queue();
				Hashes.addTempCache(key, cache.setExpiration(180000).updateDescription("youtube-channel-update").setObject(channel));
			}
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
		}
	}
	
	public static void channelUpdate(GuildMessageReceivedEvent e, final String key, String textChannel, Cache cache) {
		final Subscription channel = (Subscription)cache.getObject();
		textChannel = textChannel.replaceAll("[<>#]", "");
		long channel_id = 0;
		boolean valid = false;
		if(textChannel.matches("[0-9]{17,18}") && e.getGuild().getTextChannelById(textChannel) != null) {
			channel_id = Long.parseLong(textChannel);
			valid = true;
		}
		else if(textChannel.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_NONE))) {
			valid = true;
		}
		
		if(valid) {
			if(Azrael.SQLUpdateSubscriptionChannel(channel.getURL(), e.getGuild().getIdLong(), channel_id) > 0) {
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_YOUTUBE_CHANNEL_3).replace("{}", channel.getName())).build()).queue();
				logger.info("User {} has updated the display channel of the YouTube channel id {} in guild {}", e.getMember().getUser().getId(), channel.getURL(), e.getGuild().getIdLong());
				SubscriptionUtils.startTimer(e.getJDA());
				Hashes.addTempCache(key, new Cache(180000, "youtube-selection"));
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("The display channel of YouTube channel id {} couldn't be updated in guild {}", channel.getURL(), e.getGuild().getId());
				Hashes.clearTempCache(key);
			}
		}
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
	}
	
	public static void display(GuildMessageReceivedEvent e, String key) {
		final var channels = Azrael.SQLgetSubscriptions(e.getGuild().getIdLong(), 4);
		if(channels.size() > 0) {
			StringBuilder out = new StringBuilder();
			int count = 0;
			for(final var channel : channels) {
				out.append("**"+(++count)+"**: "+channel.getName()+"\n");
			}
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_YOUTUBE_DISPLAY)+out.toString()).build()).queue();
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_YOUTUBE_ERR)).build()).queue();
		}
		Hashes.addTempCache(key, new Cache(180000, "youtube-selection"));
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
	}
	
	public static void test(GuildMessageReceivedEvent e, String key) {
		final var channels = Azrael.SQLgetSubscriptions(e.getGuild().getIdLong(), 4);
		if(channels.size() > 0) {
			StringBuilder out = new StringBuilder();
			int count = 0;
			for(final var channel : channels) {
				out.append("**"+(++count)+"**: "+channel.getName()+"\n");
			}
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_YOUTUBE_TEST)+out.toString()).build()).queue();
			Hashes.addTempCache(key, new Cache(180000, "youtube-test").setObject(channels));
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_YOUTUBE_ERR)).build()).queue();
			Hashes.addTempCache(key, new Cache(180000, "youtube-selection"));
		}
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
	}
	
	public static void test(GuildMessageReceivedEvent e, final String key, String number, Cache cache) {
		if(number.matches("[0-9]*")) {
			@SuppressWarnings("unchecked")
			final ArrayList<Subscription> channels = (ArrayList<Subscription>)cache.getObject();
			final int selection = Integer.parseInt(number)-1;
			if(selection >= 0 && selection < channels.size()) {
				final var channel = channels.get(selection);
				YouTubeModel.ModelTest(e, channel);
				Hashes.addTempCache(key, new Cache(180000, "youtube-selection"));
			}
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
		}
	}
}
