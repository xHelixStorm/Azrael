package listeners;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Channels;
import core.Hashes;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.Azrael;

public class TextChannelListener extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(TextChannelListener.class);
	
	@Override
	public void onTextChannelCreate(TextChannelCreateEvent e) {
		insertOrUpdateChannel(e, null);
	}
	
	@Override
	public void onTextChannelUpdateName(TextChannelUpdateNameEvent e) {
		insertOrUpdateChannel(null, e);
	}
	
	@Override
	public void onTextChannelDelete(TextChannelDeleteEvent e) {
		Azrael.SQLDeleteChannel_Filter(e.getChannel().getIdLong());
		Azrael.SQLDeleteChannelConf(e.getChannel().getIdLong(), e.getGuild().getIdLong());
		Azrael.SQLDeleteChannels(e.getChannel().getIdLong());
		Hashes.clearChannels();
		logger.debug("TextChannel {} for guild {} has been deleted from all tables!", e.getChannel().getId(), e.getGuild().getId());
	}
	
	private static void insertOrUpdateChannel(TextChannelCreateEvent e1, TextChannelUpdateNameEvent e2) {
		var e = (e1 != null ? e1 : e2);
		if(Azrael.SQLInsertChannels(e.getChannel().getIdLong(), e.getChannel().getName()) > 0) {
			Hashes.clearChannels();
			logger.debug("TextChannel {} has been created for guild {}", e.getChannel().getId(), e.getGuild().getId());
		}
		else {
			Channels log_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
			if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("An internal error occurred! TextChannel "+e.getChannel().getName()+" couldn't be inserted or updated into Azrael.channels!").build()).queue();
			logger.error("An internal error occurred! TextChannel {} couldn't be inserted or updated into Azrael.channels for guild {}", e.getChannel().getId(), e.getGuild().getId());
		}
	}
}
