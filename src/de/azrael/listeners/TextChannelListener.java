package de.azrael.listeners;

import java.awt.Color;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.enums.Channel;
import de.azrael.enums.Translation;
import de.azrael.sql.Azrael;
import de.azrael.util.Hashes;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNSFWEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdatePositionEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateSlowmodeEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateTopicEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * This class executed when either a new text channel
 * gets created, the text channel name gets updated,
 * the text channel gets deleted or more.
 * 
 * The affected text channel will be inserted/updated/
 * removed from all affected tables depending on the 
 * occurrence.
 * @author xHelixStorm
 */

public class TextChannelListener extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(TextChannelListener.class);
	
	@Override
	public void onChannelCreate(ChannelCreateEvent e) {
		if(e.getChannelType().equals(ChannelType.TEXT)) {
			new Thread(() -> {
				//insert channel to table after creation
				insertOrUpdateChannel(e);
				
				STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_TEXT_CREATED)).addField(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_CREATED), e.getChannel().getAsMention(), false).setFooter(e.getChannel().getId()).setTimestamp(ZonedDateTime.now()), null, Channel.UPD.getType());
			}).start();
		}
	}
	
	@Override
	public void onChannelUpdateName(ChannelUpdateNameEvent e) {
		if(e.getChannelType().equals(ChannelType.TEXT)) {
			new Thread(() -> {
				//update channel to table, if the name has been updated
				insertOrUpdateChannel(e);
				STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_TEXT_RENAMED)).addField(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_RENAMED), e.getOldValue()+" > "+e.getNewValue(), false).setFooter(e.getChannel().getId()).setTimestamp(ZonedDateTime.now()), null, Channel.UPD.getType());
			}).start();
		}
	}
	
	@Override
	public void onChannelDelete(ChannelDeleteEvent e) {
		if(e.getChannelType().equals(ChannelType.TEXT)) {
			new Thread(() -> {
				//remove the deleted text channel from all affected tables
				Azrael.SQLDeleteChannel_Filter(e.getChannel().getIdLong());
				Azrael.SQLDeleteChannelConf(e.getChannel().getIdLong(), e.getGuild().getIdLong());
				Azrael.SQLDeleteChannels(e.getChannel().getIdLong());
				Hashes.removeChannels(e.getGuild().getIdLong());
				logger.info("TextChannel {} has been removed in guild {}", e.getChannel().getId(), e.getGuild().getId());
				
				STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_TEXT_REMOVED)).addField(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_REMOVED), e.getChannel().getName(), false).setFooter(e.getChannel().getId()).setTimestamp(ZonedDateTime.now()), null, Channel.UPD.getType());
			}).start();
		}
	}
	
	@Override
	public void onChannelUpdateNSFW(ChannelUpdateNSFWEvent e) {
		STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_TEXT_NSFW)+e.getChannel().getAsMention()).addField(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_NSFW), (e.getNewValue() ? STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_ENABLED) : STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_DISABLED)), false).setFooter(e.getChannel().getId()).setTimestamp(ZonedDateTime.now()), null, Channel.UPD.getType());
	}
	
	@Override
	public void onChannelUpdatePosition(ChannelUpdatePositionEvent e) {
		STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_TEXT_POSITION)+e.getChannel().getAsMention()).addField(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_POSITION), e.getOldValue()+" > "+e.getNewValue(), false).setFooter(e.getChannel().getId()).setTimestamp(ZonedDateTime.now()), null, Channel.UPD.getType());
	}
	
	@Override
	public void onChannelUpdateSlowmode(ChannelUpdateSlowmodeEvent e) {
		STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_TEXT_SLOWMODE)+e.getChannel().getAsMention()).addField(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_SLOWMODE), e.getOldValue()+" > "+e.getNewValue(), false).setFooter(e.getChannel().getId()).setTimestamp(ZonedDateTime.now()), null, Channel.UPD.getType());
	}
	
	@Override
	public void onChannelUpdateTopic(ChannelUpdateTopicEvent e) {
		STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_TEXT_TOPIC)+e.getChannel().getAsMention()).addField(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_TOPIC_OLD), (e.getOldValue() != null ? e.getOldValue() : " "), false).addField(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_TOPIC_NEW), (e.getNewValue() != null ? e.getNewValue() : " "), false).setFooter(e.getChannel().getId()).setTimestamp(ZonedDateTime.now()), null, Channel.UPD.getType());
	}
	
	private static void insertOrUpdateChannel(Object e) {
		net.dv8tion.jda.api.entities.channel.Channel channel = null;
		Guild guild = null;
		if(e instanceof ChannelCreateEvent) {
			channel = ((ChannelCreateEvent)e).getChannel();
			guild = ((ChannelCreateEvent)e).getGuild();
		}
		else if(e instanceof ChannelUpdateNameEvent) {
			channel = ((ChannelUpdateNameEvent)e).getChannel();
			guild = ((ChannelUpdateNameEvent)e).getGuild();
		}
		
		if(channel != null && guild != null) {
			//insert text channel into table
			if(Azrael.SQLInsertChannels(channel.getIdLong(), channel.getName()) > 0) {
				Hashes.removeChannels(guild.getIdLong());
				logger.info("TextChannel {} has been saved in guild {}", channel.getId(), guild.getId());
			}
			else {
				//throw an error, if channel couldn't be inserted/updated
				logger.error("New text channel {} couldn't be saved or updated in guild {}", channel.getId(), guild.getId());
			}
		}
	}
}
