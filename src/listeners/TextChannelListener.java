package listeners;

import java.awt.Color;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import enums.Channel;
import enums.Translation;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdateNSFWEvent;
import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdatePositionEvent;
import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdateSlowmodeEvent;
import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdateTopicEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.Azrael;
import util.STATIC;

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
	public void onTextChannelCreate(TextChannelCreateEvent e) {
		new Thread(() -> {
			//insert channel to table after creation
			insertOrUpdateChannel(e, null);
			
			STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_TEXT_CREATED)).addField(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_CREATED), e.getChannel().getAsMention(), false).setFooter(e.getChannel().getId()).setTimestamp(ZonedDateTime.now()), null, Channel.UPD.getType());
		}).start();
	}
	
	@Override
	public void onTextChannelUpdateName(TextChannelUpdateNameEvent e) {
		new Thread(() -> {
			//update channel to table, if the name has been updated
			insertOrUpdateChannel(null, e);
			
			STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_TEXT_RENAMED)).addField(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_RENAMED), e.getOldName()+" > "+e.getNewName(), false).setFooter(e.getChannel().getId()).setTimestamp(ZonedDateTime.now()), null, Channel.UPD.getType());
		}).start();
	}
	
	@Override
	public void onTextChannelDelete(TextChannelDeleteEvent e) {
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
	
	@Override
	public void onTextChannelUpdateNSFW(TextChannelUpdateNSFWEvent e) {
		STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_TEXT_NSFW)+e.getChannel().getAsMention()).addField(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_NSFW), (e.getNewValue() ? STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_ENABLED) : STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_DISABLED)), false).setFooter(e.getChannel().getId()).setTimestamp(ZonedDateTime.now()), null, Channel.UPD.getType());
	}
	
	@Override
	public void onTextChannelUpdatePosition(TextChannelUpdatePositionEvent e) {
		STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_TEXT_POSITION)+e.getChannel().getAsMention()).addField(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_POSITION), e.getOldPosition()+" > "+e.getNewPosition(), false).setFooter(e.getChannel().getId()).setTimestamp(ZonedDateTime.now()), null, Channel.UPD.getType());
	}
	
	@Override
	public void onTextChannelUpdateSlowmode(TextChannelUpdateSlowmodeEvent e) {
		STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_TEXT_SLOWMODE)+e.getChannel().getAsMention()).addField(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_SLOWMODE), e.getOldSlowmode()+" > "+e.getNewSlowmode(), false).setFooter(e.getChannel().getId()).setTimestamp(ZonedDateTime.now()), null, Channel.UPD.getType());
	}
	
	@Override
	public void onTextChannelUpdateTopic(TextChannelUpdateTopicEvent e) {
		STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_TEXT_TOPIC)+e.getChannel().getAsMention()).addField(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_TOPIC_OLD), (e.getOldTopic() != null ? e.getOldTopic() : " "), false).addField(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_TOPIC_NEW), (e.getNewTopic() != null ? e.getNewTopic() : " "), false).setFooter(e.getChannel().getId()).setTimestamp(ZonedDateTime.now()), null, Channel.UPD.getType());
	}
	
	private static void insertOrUpdateChannel(TextChannelCreateEvent e1, TextChannelUpdateNameEvent e2) {
		var e = (e1 != null ? e1 : e2);
		//insert text channel into table
		if(Azrael.SQLInsertChannels(e.getChannel().getIdLong(), e.getChannel().getName()) > 0) {
			Hashes.removeChannels(e.getGuild().getIdLong());
			logger.info("TextChannel {} has been saved in guild {}", e.getChannel().getId(), e.getGuild().getId());
		}
		else {
			//throw an error, if channel couldn't be inserted/updated
			logger.error("New text channel {} couldn't be saved or updated in guild {}", e.getChannel().getId(), e.getGuild().getId());
		}
	}
}
