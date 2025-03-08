package de.azrael.listeners;

import java.awt.Color;
import java.time.ZonedDateTime;

import de.azrael.enums.Channel;
import de.azrael.enums.Translation;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateBitrateEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdatePositionEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateUserLimitEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Collection of voice channel events to notify the user of changes
 * @author xHelixStorm
 *
 */

public class VoiceChannelListener extends ListenerAdapter {
	
	@Override
	public void onChannelCreate(ChannelCreateEvent e) {
		if(e.isFromGuild() && e.getChannelType().equals(ChannelType.VOICE))
			STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_VOICE_CREATED)).addField(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_CREATED), e.getChannel().getName(), false).setFooter(e.getChannel().getId()).setTimestamp(ZonedDateTime.now()), null, Channel.UPD.getType());
	}

	@Override
	public void onChannelDelete(ChannelDeleteEvent e) {
		if(e.isFromGuild() && e.getChannelType().equals(ChannelType.VOICE))
			STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_VOICE_REMOVED)).addField(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_REMOVED), e.getChannel().getName(), false).setFooter(e.getChannel().getId()).setTimestamp(ZonedDateTime.now()), null, Channel.UPD.getType());
	}
	
	@Override
	public void onChannelUpdateBitrate(ChannelUpdateBitrateEvent e) {
		if(e.isFromGuild() && e.getChannelType().equals(ChannelType.VOICE))
			STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_VOICE_BITRATE)+e.getChannel().getName()).addField(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_BITRATE), e.getOldValue()+" > "+e.getNewValue(), false).setFooter(e.getChannel().getId()).setTimestamp(ZonedDateTime.now()), null, Channel.UPD.getType());
	}
	
	@Override
	public void onChannelUpdateName(ChannelUpdateNameEvent e) {
		if(e.isFromGuild() && e.getChannelType().equals(ChannelType.VOICE))
			STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_VOICE_RENAMED)).addField(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_RENAMED), e.getOldValue()+" > "+e.getNewValue(), false).setFooter(e.getChannel().getId()).setTimestamp(ZonedDateTime.now()), null, Channel.UPD.getType());
	}
	
	@Override
	public void onChannelUpdatePosition(ChannelUpdatePositionEvent e) {
		if(e.isFromGuild() && e.getChannelType().equals(ChannelType.VOICE))
			STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_VOICE_POSITION)+e.getChannel().getName()).addField(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_POSITION), e.getOldValue()+" > "+e.getNewValue(), false).setFooter(e.getChannel().getId()).setTimestamp(ZonedDateTime.now()), null, Channel.UPD.getType());
	}
	
	@Override
	public void onChannelUpdateUserLimit(ChannelUpdateUserLimitEvent e) {
		if(e.isFromGuild() && e.getChannelType().equals(ChannelType.VOICE))
			STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_VOICE_USER_LIMIT)+e.getChannel().getName()).addField(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_USER_LIMIT), e.getOldValue()+" > "+e.getNewValue(), false).setFooter(e.getChannel().getId()).setTimestamp(ZonedDateTime.now()), null, Channel.UPD.getType());
	}
}
