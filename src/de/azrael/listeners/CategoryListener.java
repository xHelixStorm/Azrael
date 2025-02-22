package de.azrael.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.sql.Azrael;
import de.azrael.util.Hashes;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CategoryListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(CategoryListener.class);
	
	@Override
	public void onChannelCreate(ChannelCreateEvent e) {
		if(e.getChannelType().equals(ChannelType.CATEGORY)) {
			if(Azrael.SQLInsertCategory(e.getChannel().getIdLong(), e.getChannel().getName()) == 0) {
				logger.error("New category {} couldn't be saved in guild {}", e.getChannel().getId(), e.getGuild().getId());
			}
		}
	}
	
	@Override
	public void onChannelDelete(ChannelDeleteEvent e) {
		if(e.getChannelType().equals(ChannelType.CATEGORY)) {
			if(Azrael.SQLDeleteCategoryConf(e.getChannel().getIdLong()) != -1) {
				if(Azrael.SQLDeleteCategory(e.getChannel().getIdLong()) == -1) {
					logger.error("Category {} couldn't be deleted in guild {}", e.getChannel().getId(), e.getGuild().getId());
				}
			}
			else {
				logger.error("Configuration for category {} couldn't be removed in guild {}");
			}
			Hashes.removeCategories(e.getGuild().getIdLong());
		}
	}
	
	@Override
	public void onChannelUpdateName(ChannelUpdateNameEvent e) {
		if(e.getChannelType().equals(ChannelType.CATEGORY)) {
			final int result = Azrael.SQLUpdateCategoryName(e.getChannel().getIdLong(), e.getChannel().getName());
			if(result != -1) {
				if(Azrael.SQLInsertCategory(e.getChannel().getIdLong(), e.getChannel().getName()) == 0) {
					logger.error("Missing category {} couldn't be saved in guild {}", e.getChannel().getId(), e.getGuild().getId());
				}
			}
			else {
				logger.error("Name of category {} couldn't be updated in guild {}", e.getChannel().getId(), e.getGuild().getId());
			}
		}
	}
}
