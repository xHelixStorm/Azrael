package de.azrael.commands.util;

import java.awt.Color;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Dailies;
import de.azrael.enums.Translation;
import de.azrael.sql.RankingSystem;
import de.azrael.util.Hashes;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Extension of the set command
 * @author xHelixStorm
 *
 */

public class SetDailyItem {
	private final static Logger logger = LoggerFactory.getLogger(SetDailyItem.class);

	public static void runTask(MessageReceivedEvent e, String[] args) {
		String type = null;
		int probability = 0;
		//be sure that enough parameters are provided
		if(args.length < 4) {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
			return;
		}
		//verify that the item type has been passed correctly
		if(args[1].equalsIgnoreCase("cur") || args[1].equalsIgnoreCase("exp") || args[1].equalsIgnoreCase("cod") || args[1].equalsIgnoreCase("riv")) {
			type = args[1].toLowerCase();
		}
		else {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_DAILY_INVALID_TYPE)).build()).queue();
			return;
		}
		//verify that a numerical probability has been forwarded
		if(args[2].matches("[0-9]{1,}")) {
			probability = Integer.parseInt(args[2]);
		}
		else {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_DAILY_PROBABILITY_ERR)).build()).queue();
			return;
		}
		//build together the item name
		StringBuilder item = new StringBuilder();
		for(int i = 3; i < args.length; i++) {
			if(i == 3)
				item.append(args[i]);
			else
				item.append(" "+args[i]);
		}
		
		final String itemName = item.toString();
		
		//logic to insert a daily item
		if(probability > 0) {
			ArrayList<Dailies> dailies = RankingSystem.SQLgetDailiesAndType(e.getGuild().getIdLong());
			if(dailies == null) {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				return;
			}
			
			//check if this item has been already registered, if yes enter update mode
			boolean updateMode = false;
			Dailies daily = dailies.parallelStream().filter(f -> f.getDescription().equalsIgnoreCase(itemName)).findAny().orElse(null);
			if(daily != null) 
				updateMode = true;
			
			//verify that the probability doesn't exceed the limit
			var weight = dailies.parallelStream().mapToInt(i -> i.getWeight()).sum();
			if(updateMode)
				weight = weight - daily.getWeight();
			final long total = weight+probability;
			if(total <= 100000) {
				if(RankingSystem.SQLInsertDailyItems(itemName, probability, type, e.getGuild().getIdLong()) > 0) {
					Hashes.removeDailyItems(e.getGuild().getIdLong());
					if(!updateMode) {
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_DAILY_ADDED).replace("{}", ""+total)).build()).queue();
						logger.info("User {} has inserted the item {} into the daily item pool with the probability {} in guild {}", e.getMember().getUser().getId(), itemName, probability, e.getGuild().getId());
						RankingSystem.SQLInsertActionLog("low", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Daily item registered", "Daily item "+itemName+" of type "+type+" with the probability "+probability+" has been added");
					}
					else {
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_DAILY_UPDATED).replace("{}", ""+total)).build()).queue();
						logger.info("User {} has updated the probability to obtain item {} with the probability {} in guild {}", e.getMember().getUser().getId(), itemName, probability, e.getGuild().getId());
						RankingSystem.SQLInsertActionLog("low", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Daily item updated", "Daily item "+itemName+" of type "+type+" has been updated with the probability "+probability);
					}
				}
				else {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("The daily item {} couldn't be inserted with the probability {} in guild {}", itemName, probability, e.getGuild().getId());
					RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Daily item registration error", "daily item couldn't be inserted with probability "+probability+" and item "+itemName);
				}
			}
			else {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_DAILY_OVER_THE_LIMIT).replace("{}", ""+weight)).build()).queue();
			}
		}
		//logic to remove a daily item
		else {
			final int result = RankingSystem.SQLDeleteDailyItems(itemName, type, e.getGuild().getIdLong());
			if(result > 0) {
				Hashes.removeDailyItems(e.getGuild().getIdLong());
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_DAILY_REMOVED)).build()).queue();
				logger.info("The daily item {} of type {} has been removed in guild {}", itemName, type, e.getGuild().getId());
				RankingSystem.SQLInsertActionLog("low", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Daily item removed", "Daily item "+itemName+" of type "+type+" with the probability "+probability+" has been removed");
			}
			else if(result == 0) {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_DAILY_NOT_REMOVED)).build()).queue();
			}
			else {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("The daily item {} of type {} couldn't be removed in guild {}", itemName, type, e.getGuild().getId());
				RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Daily item deletion error", "daily item couldn't be removed with type "+type+" and item "+itemName);
			}
		}
	}
}
