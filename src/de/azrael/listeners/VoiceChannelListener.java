package de.azrael.listeners;

import java.awt.Color;
import java.time.ZonedDateTime;

import de.azrael.enums.Channel;
import de.azrael.enums.Translation;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.voice.update.VoiceChannelUpdateBitrateEvent;
import net.dv8tion.jda.api.events.channel.voice.update.VoiceChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.voice.update.VoiceChannelUpdatePositionEvent;
import net.dv8tion.jda.api.events.channel.voice.update.VoiceChannelUpdateUserLimitEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Collection of voice channel events to notify the user of changes
 * @author xHelixStorm
 *
 */

public class VoiceChannelListener extends ListenerAdapter {
	
	@Override
	public void onVoiceChannelCreate(VoiceChannelCreateEvent e) {
		STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_VOICE_CREATED)).addField(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_CREATED), e.getChannel().getName(), false).setFooter(e.getChannel().getId()).setTimestamp(ZonedDateTime.now()), null, Channel.UPD.getType());
	}

	@Override
	public void onVoiceChannelDelete(VoiceChannelDeleteEvent e) {
		STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_VOICE_REMOVED)).addField(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_REMOVED), e.getChannel().getName(), false).setFooter(e.getChannel().getId()).setTimestamp(ZonedDateTime.now()), null, Channel.UPD.getType());
	}
	
	@Override
	public void onVoiceChannelUpdateBitrate(VoiceChannelUpdateBitrateEvent e) {
		STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_VOICE_BITRATE)+e.getChannel().getName()).addField(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_BITRATE), e.getOldBitrate()+" > "+e.getNewBitrate(), false).setFooter(e.getChannel().getId()).setTimestamp(ZonedDateTime.now()), null, Channel.UPD.getType());
	}
	
	@Override
	public void onVoiceChannelUpdateName(VoiceChannelUpdateNameEvent e) {
		STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_VOICE_RENAMED)).addField(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_RENAMED), e.getOldName()+" > "+e.getNewName(), false).setFooter(e.getChannel().getId()).setTimestamp(ZonedDateTime.now()), null, Channel.UPD.getType());
	}
	
	@Override
	public void onVoiceChannelUpdatePosition(VoiceChannelUpdatePositionEvent e) {
		STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_VOICE_POSITION)+e.getChannel().getName()).addField(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_POSITION), e.getOldPosition()+" > "+e.getNewPosition(), false).setFooter(e.getChannel().getId()).setTimestamp(ZonedDateTime.now()), null, Channel.UPD.getType());
	}
	
	@Override
	public void onVoiceChannelUpdateUserLimit(VoiceChannelUpdateUserLimitEvent e) {
		STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_VOICE_USER_LIMIT)+e.getChannel().getName()).addField(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_USER_LIMIT), e.getOldUserLimit()+" > "+e.getNewUserLimit(), false).setFooter(e.getChannel().getId()).setTimestamp(ZonedDateTime.now()), null, Channel.UPD.getType());
	}
}
