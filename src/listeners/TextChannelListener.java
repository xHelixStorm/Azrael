package listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.Azrael;

/**
 * This class executed when either a new text channel
 * gets created, the text channel name gets updated or
 * the text channel gets deleted.
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
			Hashes.removeChannels(e.getGuild().getIdLong());
			logger.info("TextChannel {} has been removed in guild {}", e.getChannel().getId(), e.getGuild().getId());
		}).start();
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
