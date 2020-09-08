package commandsContainer;

import java.awt.Color;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Dailies;
import core.Hashes;
import enums.Translation;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.RankingSystem;
import util.STATIC;

/**
 * Extension of the set command
 * @author xHelixStorm
 *
 */

public class SetDailyItem {
	private final static Logger logger = LoggerFactory.getLogger(SetDailyItem.class);

	public static void runTask(GuildMessageReceivedEvent e, String[] args, ArrayList<Dailies> _dailies, int _weight) {
		String type = null;
		int probability = 0;
		//be sure that enough parameters are provided
		if(args.length < 4) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
			return;
		}
		//verify that the item type has been passed correctly
		if(args[1].equalsIgnoreCase("cur") || args[1].equalsIgnoreCase("exp") || args[1].equalsIgnoreCase("cod") || args[1].equalsIgnoreCase("riv")) {
			type = args[1].toLowerCase();
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_DAILY_INVALID_TYPE)).build()).queue();
			return;
		}
		//verify that a numerical probability has been forwarded
		if(args[2].replaceAll("[0-9]*", "").length() == 0) {
			probability = Integer.parseInt(args[2]);
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_DAILY_PROBABILITY_ERR)).build()).queue();
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
		//check if this item has been already registered
		if(_dailies.parallelStream().filter(f -> f.getDescription().equalsIgnoreCase(itemName)).findAny().orElse(null) != null) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_DAILY_ALREADY_REGISTERED)).build()).queue();
			return;
		}
		
		//verify that the probability doesn't exceed the limit
		if((_weight+probability) <= 10000) {
			if(RankingSystem.SQLInsertDailyItems(itemName, probability, type, e.getGuild().getIdLong(), RankingSystem.SQLgetGuild(e.getGuild().getIdLong()).getThemeID()) > 0) {
				Hashes.removeDailyItems(e.getGuild().getIdLong());
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_DAILY_ADDED).replace("{}", ""+(_weight+probability))).build()).queue();
				logger.debug("{} has inserted the item {} into the daily items pool with the probability {} in guild {}", e.getMember().getUser().getId(), itemName, probability, e.getGuild().getId());
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Internal error! Daily item couldn't be inserted in guild {}", e.getGuild().getId());
				RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Daily item registration error", "daily item couldn't be inserted with probability "+probability+" and item "+itemName);
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_DAILY_OVER_THE_LIMIT).replace("{}", ""+_weight)).build()).queue();
		}
		
		
	}
}
