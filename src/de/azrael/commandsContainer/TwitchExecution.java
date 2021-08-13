package de.azrael.commandsContainer;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiParser;

import de.azrael.constructors.Cache;
import de.azrael.constructors.RSS;
import de.azrael.core.Hashes;
import de.azrael.enums.RedditMethod;
import de.azrael.enums.Translation;
import de.azrael.rss.RedditModel;
import de.azrael.sql.Azrael;
import de.azrael.timerTask.ParseSubscription;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class TwitchExecution {
private static final Logger logger = LoggerFactory.getLogger(TwitchExecution.class);
	
	@SuppressWarnings("unchecked")
	public static void format(GuildMessageReceivedEvent e, Cache cache) {
		if(e.getMessage().getContentRaw().matches("[0-9]*")) {
			final int index = Integer.parseInt(e.getMessage().getContentRaw())-1;
			ArrayList<RSS> twitch = (ArrayList<RSS>)cache.getObject();
			if(index >= 0 && index < twitch.size()) {
				final RSS user = twitch.get(index);
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITCH_FORMAT_STEP_2)+user.getFormat()+"```").build()).queue();
				Hashes.addTempCache("twitch_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000).setObject(user).updateDescription("format2"));
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITCH_REMOVE_ERR_2).replace("{}", ""+twitch.size())).build()).queue();
				Hashes.addTempCache("twitch_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
			}
		}
	}
	
	public static void formatUpdate(GuildMessageReceivedEvent e, Cache cache) {
		final RSS twitch = (RSS)cache.getObject();
		if(Azrael.SQLUpdateRSSFormat(twitch.getURL(), e.getGuild().getIdLong(), EmojiParser.parseToAliases(e.getMessage().getContentRaw())) > 0) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REDDIT_FORMAT_UPDATED).replace("{}", twitch.getURL())).build()).queue();
			Hashes.removeFeeds(e.getGuild().getIdLong());
			logger.info("User {} has updated the format of the twitch subscription {} in guild {}", e.getMember().getUser().getId(), twitch.getName(), e.getGuild().getId());
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("The display format of twitch subscription {} couldn't be updated in guild {}", twitch.getName(), e.getGuild().getId());
		}
		Hashes.clearTempCache("twitch_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
	}
	
	@SuppressWarnings("unchecked")
	public static void channel(GuildMessageReceivedEvent e, Cache cache) {
		if(e.getMessage().getContentRaw().matches("[0-9]*")) {
			final int index = Integer.parseInt(e.getMessage().getContentRaw())-1;
			ArrayList<RSS> twitch = (ArrayList<RSS>)cache.getObject();
			if(index >= 0 && index < twitch.size()) {
				final RSS user = twitch.get(index);
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITCH_CHANNEL_STEP_2)+(user.getChannelID() != 0 ? "<#"+user.getChannelID()+">" : STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_CHANNEL_DEFAULT))).build()).queue();
				Hashes.addTempCache("twitch_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000).setObject(user).updateDescription("channel2"));
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITCH_REMOVE_ERR_2).replace("{}", ""+twitch.size())).build()).queue();
				Hashes.addTempCache("twitch_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
			}
		}
	}
	
	public static void channelUpdate(GuildMessageReceivedEvent e, Cache cache) {
		String channel_id = e.getMessage().getContentRaw().replaceAll("[<>#]*", "");
		if(channel_id.replaceAll("[0-9]*", "").length() == 0) {
			TextChannel textChannel = e.getGuild().getTextChannelById(channel_id);
			if(textChannel != null) {
				final RSS twitch = (RSS)cache.getObject();
				final var result = Azrael.SQLUpdateRSSChannel(twitch.getURL(), e.getGuild().getIdLong(), textChannel.getIdLong());
				if(result > 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITCH_CHANNEL_ADDED).replace("{}", textChannel.getAsMention())).build()).queue();
					logger.info("User {} has redirected the twitch subscription {} to channel {} in guild {}", e.getMember().getUser().getId(), twitch.getName(), textChannel.getId(), e.getGuild().getId());
					Hashes.removeFeeds(e.getGuild().getIdLong());
					Hashes.clearTempCache("twitch_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					if(!ParseSubscription.timerIsRunning(e.getGuild().getIdLong())) {
						ParseSubscription.runTask(e.getJDA(), e.getGuild().getIdLong());
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void remove(GuildMessageReceivedEvent e, Cache cache) {
		if(e.getMessage().getContentRaw().matches("[0-9]*")) {
			final int index = Integer.parseInt(e.getMessage().getContentRaw())-1;
			ArrayList<RSS> twitch = (ArrayList<RSS>)cache.getObject();
			if(index >= 0 && index < twitch.size()) {
				final RSS user = twitch.get(index);
				if(Azrael.SQLDeleteRSSFeed(user.getURL(), e.getGuild().getIdLong()) > 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITCH_REMOVE_DONE).replace("{}", user.getURL())).build()).queue();
					logger.info("User {} has removed the twitch subscription {} in guild {}", e.getMember().getUser().getId(), user.getName(), e.getGuild().getId());
					Hashes.removeFeeds(e.getGuild().getIdLong());
					Hashes.removeSubscriptionStatus(e.getGuild().getId()+"_"+user.getURL());
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Twitch fetcher {} couldn't be removed in guild {}", user.getName(), e.getGuild().getId());
				}
				Hashes.clearTempCache("twitch_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITCH_REMOVE_ERR_2).replace("{}", ""+twitch.size())).build()).queue();
				Hashes.addTempCache("twitch_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void test(GuildMessageReceivedEvent e, Cache cache) {
		if(e.getMessage().getContentRaw().matches("[0-9]*")) {
			final int index = Integer.parseInt(e.getMessage().getContentRaw())-1;
			ArrayList<RSS> twitch = (ArrayList<RSS>)cache.getObject();
			if(index >= 0 && index < twitch.size()) {
				final RSS user = twitch.get(index);
				try {
					//TODO: add TwitchModel for test
					RedditModel.fetchRedditContent(e, e.getGuild(), user, e.getChannel().getIdLong(), false);
				} catch (IOException e1) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("An unexpected error occurred while testing the twitch subscription {} in guild {}", user.getName(), e.getGuild().getId());
				}
				Hashes.clearTempCache("twitch_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TWITCH_REMOVE_ERR_2).replace("{}", ""+twitch.size())).build()).queue();
				Hashes.addTempCache("twitch_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
			}
		}
	}
}
