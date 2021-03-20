package de.azrael.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.core.Hashes;
import de.azrael.sql.Azrael;
import net.dv8tion.jda.api.events.channel.category.CategoryCreateEvent;
import net.dv8tion.jda.api.events.channel.category.CategoryDeleteEvent;
import net.dv8tion.jda.api.events.channel.category.update.CategoryUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CategoryListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(CategoryListener.class);
	
	@Override
	public void onCategoryCreate(CategoryCreateEvent e) {
		if(Azrael.SQLInsertCategory(e.getCategory().getIdLong(), e.getCategory().getName()) == 0) {
			logger.error("New category {} couldn't be saved in guild {}", e.getCategory().getId(), e.getGuild().getId());
		}
	}
	
	@Override
	public void onCategoryDelete(CategoryDeleteEvent e) {
		if(Azrael.SQLDeleteCategoryConf(e.getCategory().getIdLong()) != -1) {
			if(Azrael.SQLDeleteCategory(e.getCategory().getIdLong()) == -1) {
				logger.error("Category {} couldn't be deleted in guild {}", e.getCategory().getId(), e.getGuild().getId());
			}
		}
		else {
			logger.error("Configuration for category {} couldn't be removed in guild {}");
		}
		Hashes.removeCategories(e.getGuild().getIdLong());
	}
	
	@Override
	public void onCategoryUpdateName(CategoryUpdateNameEvent e) {
		final int result = Azrael.SQLUpdateCategoryName(e.getCategory().getIdLong(), e.getCategory().getName());
		if(result == 0) {
			if(Azrael.SQLInsertCategory(e.getCategory().getIdLong(), e.getCategory().getName()) == 0) {
				logger.error("Missing category {} couldn't be saved in guild {}", e.getCategory().getId(), e.getGuild().getId());
			}
		}
		else {
			logger.error("Name of category {} couldn't be updated in guild {}", e.getCategory().getId(), e.getGuild().getId());
		}
	}
}
