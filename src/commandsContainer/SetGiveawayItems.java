package commandsContainer;

import java.awt.Color;
import java.net.MalformedURLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import enums.Translation;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.RankingSystem;
import util.Pastebin;
import util.STATIC;

/**
 * Extension of the set command
 * @author xHelixStorm
 *
 */

public class SetGiveawayItems {
	private final static Logger logger = LoggerFactory.getLogger(SetGiveawayItems.class);
	
	public static void runTask(GuildMessageReceivedEvent e, String param) {
		//verify if a parameter has been passed
		if(param.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_EXTEND))) {
			//calculate the next beginning of the month
			Timestamp timestamp = calculateMonth();
			final int result = RankingSystem.SQLUpdateRewardExpiration(e.getGuild().getIdLong(), timestamp);
			if(result > 0) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_GIVEAWAY_EXTENDED)).build()).queue();
			}
			else if(result == 0) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_GIVEAWAY_EXTEND_ERR)).build()).queue();
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			}
		}
		//verify pastebin link and save the content into array
		else if(param.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && param.startsWith("http")) {
			try {
				String [] rewards = Pastebin.readPublicPasteLink(param).split("[\\r\\n]+");
				//calculate the next beginning of the month
				Timestamp timestamp = calculateMonth();
				
				//insert rewards into table and return error with true or false
				boolean err = RankingSystem.SQLBulkInsertGiveawayRewards(rewards, timestamp, e.getGuild().getIdLong());
				
				if(err == false) {
					logger.debug("{} has inserted giveaway items for the daily command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_GIVEAWAY_ADDED)).build()).queue();
				}
				else {
					EmbedBuilder error = new EmbedBuilder().setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED);
					e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Rewards couldn't be uploaded into the RankingSystem.giveaway table in guild {}");
				}
			} catch (MalformedURLException | RuntimeException e1) {
				EmbedBuilder error = new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PASTE_READ_ERR));
				e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR_2)).build()).queue();
				logger.error("Reading paste failed in guild {}!", e.getGuild().getId(), e1);
			}
		}
		else {
			EmbedBuilder error = new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_NOT_PASTE));
			e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
		}
	}
	
	private static Timestamp calculateMonth() {
		//calculate the next beginning of the month
		LocalTime midnight = LocalTime.MIDNIGHT;
		LocalDate today = LocalDate.now();
		LocalDate beginningOfMonth = today.with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);
		LocalDateTime beginningOfMonthMidnight = LocalDateTime.of(beginningOfMonth, midnight);
		return Timestamp.valueOf(beginningOfMonthMidnight);
	}
}
