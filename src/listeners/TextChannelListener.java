package listeners;

/**
 * This class executed when either a new text channel
 * gets created, the text channel name gets updated or
 * the text channel gets deleted.
 * 
 * The affected text channel will be inserted/updated/
 * removed from all affected tables depending on the 
 * occurrence.
 */

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
		new Thread(() -> {
			//insert channel to table after creation
			insertOrUpdateChannel(e, null);
		}).start();
	}
	
	@Override
	public void onTextChannelUpdateName(TextChannelUpdateNameEvent e) {
		new Thread(() -> {
			//update channel to table, if the name has been updated
			insertOrUpdateChannel(null, e);
		}).start();
	}
	
	@Override
	public void onTextChannelDelete(TextChannelDeleteEvent e) {
		new Thread(() -> {
			//remove the deleted text channel from all affected tables
			Azrael.SQLDeleteChannel_Filter(e.getChannel().getIdLong());
			Azrael.SQLDeleteChannelConf(e.getChannel().getIdLong(), e.getGuild().getIdLong());
			Azrael.SQLDeleteChannels(e.getChannel().getIdLong());
			Hashes.clearChannels();
			logger.debug("TextChannel {} for guild {} has been deleted from all tables!", e.getChannel().getId(), e.getGuild().getId());
		}).start();
	}
	
	private static void insertOrUpdateChannel(TextChannelCreateEvent e1, TextChannelUpdateNameEvent e2) {
		var e = (e1 != null ? e1 : e2);
		//insert text channel into table
		if(Azrael.SQLInsertChannels(e.getChannel().getIdLong(), e.getChannel().getName()) > 0) {
			Hashes.clearChannels();
			logger.debug("TextChannel {} has been created for guild {}", e.getChannel().getId(), e.getGuild().getId());
		}
		else {
			//throw an error, if channel couldn't be inserted/updated
			Channels log_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
			if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Error!").setDescription("An internal error occurred! TextChannel "+e.getChannel().getName()+" couldn't be inserted or updated into Azrael.channels!").build()).queue();
			logger.error("An internal error occurred! TextChannel {} couldn't be inserted or updated into Azrael.channels for guild {}", e.getChannel().getId(), e.getGuild().getId());
		}
	}
}
