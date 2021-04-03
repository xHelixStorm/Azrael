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

public class RedditExecution {
	private static final Logger logger = LoggerFactory.getLogger(RedditExecution.class);
	
	public static void register(GuildMessageReceivedEvent e, Cache cache) {
		RedditMethod method = RedditMethod.valueOfType(e.getMessage().getContentRaw());
		if(method != null) {
			final String username = cache.getAdditionalInfo2()+"/"+method.type;
			final int result = Azrael.SQLInsertRSS(username, e.getGuild().getIdLong(), 3);
			if(result > 0) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REDDIT_REGISTER_STEP_2).replace("{}", username)).build()).queue();
				logger.info("User {} has subscribed to the reddit username and method {} in guild {}", e.getMember().getUser().getId(), username, e.getGuild().getId());
				if(Hashes.getFeedsSize(e.getGuild().getIdLong()) == 0 && !ParseSubscription.timerIsRunning(e.getGuild().getIdLong()))
					ParseSubscription.runTask(e.getJDA(), e.getGuild().getIdLong());
				Hashes.removeFeeds(e.getGuild().getIdLong());
			}
			else if(result == 0) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REDDIT_REGISTER_ERR)).build()).queue();
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Reddit fetcher {} couldn't be registered in guild {}", username, e.getGuild().getId());
			}
			Hashes.clearTempCache("reddit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void format(GuildMessageReceivedEvent e, Cache cache) {
		if(e.getMessage().getContentRaw().matches("[0-9]*")) {
			final int index = Integer.parseInt(e.getMessage().getContentRaw())-1;
			ArrayList<RSS> reddit = (ArrayList<RSS>)cache.getObject();
			if(index >= 0 && index < reddit.size()) {
				final RSS user = reddit.get(index);
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REDDIT_FORMAT_STEP_2)+user.getFormat()+"```").build()).queue();
				Hashes.addTempCache("reddit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000).setObject(user).updateDescription("format2"));
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REDDIT_REMOVE_ERR_2).replace("{}", ""+reddit.size())).build()).queue();
				Hashes.addTempCache("reddit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
			}
		}
	}
	
	public static void formatUpdate(GuildMessageReceivedEvent e, Cache cache) {
		final RSS user = (RSS)cache.getObject();
		if(Azrael.SQLUpdateRSSFormat(user.getURL(), e.getGuild().getIdLong(), EmojiParser.parseToAliases(e.getMessage().getContentRaw())) > 0) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REDDIT_FORMAT_UPDATED).replace("{}", user.getFormat())).build()).queue();
			Hashes.removeFeeds(e.getGuild().getIdLong());
			logger.info("User {} has updated the format of the Reddit subscription {} in guild {}", e.getMember().getUser().getId(), user.getURL(), e.getGuild().getId());
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("The display format of Reddit subscription {} couldn't be updated in guild {}", user.getURL(), e.getGuild().getId());
		}
		Hashes.clearTempCache("reddit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
	}
	
	@SuppressWarnings("unchecked")
	public static void channel(GuildMessageReceivedEvent e, Cache cache) {
		if(e.getMessage().getContentRaw().matches("[0-9]*")) {
			final int index = Integer.parseInt(e.getMessage().getContentRaw())-1;
			ArrayList<RSS> reddit = (ArrayList<RSS>)cache.getObject();
			if(index >= 0 && index < reddit.size()) {
				final RSS user = reddit.get(index);
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REDDIT_CHANNEL_STEP_2)+(user.getChannelID() != 0 ? "<#"+user.getChannelID()+">" : STATIC.getTranslation(e.getMember(), Translation.SUBSCRIBE_CHANNEL_DEFAULT))).build()).queue();
				Hashes.addTempCache("reddit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000).setObject(user).updateDescription("channel2"));
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REDDIT_REMOVE_ERR_2).replace("{}", ""+reddit.size())).build()).queue();
				Hashes.addTempCache("reddit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
			}
		}
	}
	
	public static void channelUpdate(GuildMessageReceivedEvent e, Cache cache) {
		String channel_id = e.getMessage().getContentRaw().replaceAll("[<>#]*", "");
		if(channel_id.replaceAll("[0-9]*", "").length() == 0) {
			TextChannel textChannel = e.getGuild().getTextChannelById(channel_id);
			if(textChannel != null) {
				final RSS user = (RSS)cache.getObject();
				final var result = Azrael.SQLUpdateRSSChannel(user.getURL(), e.getGuild().getIdLong(), textChannel.getIdLong());
				if(result > 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REDDIT_CHANNEL_ADDED).replace("{}", textChannel.getAsMention())).build()).queue();
					logger.info("User {} has redirected the Reddit subscription {} to channel {} in guild {}", e.getMember().getUser().getId(), user.getURL(), textChannel.getId(), e.getGuild().getId());
					Hashes.removeFeeds(e.getGuild().getIdLong());
					Hashes.clearTempCache("reddit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
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
			ArrayList<RSS> reddit = (ArrayList<RSS>)cache.getObject();
			if(index >= 0 && index < reddit.size()) {
				final RSS user = reddit.get(index);
				if(Azrael.SQLDeleteRSSFeed(user.getURL(), e.getGuild().getIdLong()) > 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REDDIT_REMOVE_DONE).replace("{}", user.getURL())).build()).queue();
					logger.info("User {} has removed the reddit subscription {} in guild {}", e.getMember().getUser().getId(), user.getURL(), e.getGuild().getId());
					Hashes.removeFeeds(e.getGuild().getIdLong());
					Hashes.removeSubscriptionStatus(e.getGuild().getId()+"_"+user.getURL());
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Reddit fetcher {} couldn't be removed in guild {}", user.getURL(), e.getGuild().getId());
				}
				Hashes.clearTempCache("reddit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REDDIT_REMOVE_ERR_2).replace("{}", ""+reddit.size())).build()).queue();
				Hashes.addTempCache("reddit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void test(GuildMessageReceivedEvent e, Cache cache) {
		if(e.getMessage().getContentRaw().matches("[0-9]*")) {
			final int index = Integer.parseInt(e.getMessage().getContentRaw())-1;
			ArrayList<RSS> reddit = (ArrayList<RSS>)cache.getObject();
			if(index >= 0 && index < reddit.size()) {
				final RSS user = reddit.get(index);
				try {
					RedditModel.fetchRedditContent(e, e.getGuild(), user, e.getChannel().getIdLong(), false);
				} catch (IOException e1) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("An unexpected error occurred while testing the reddit subscription {} in guild {}", user.getURL(), e.getGuild().getId());
				}
				Hashes.clearTempCache("reddit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REDDIT_REMOVE_ERR_2).replace("{}", ""+reddit.size())).build()).queue();
				Hashes.addTempCache("reddit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
			}
		}
	}
}
