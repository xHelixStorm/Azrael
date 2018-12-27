package commandsContainer;

import java.awt.Color;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingSystem;
import util.Pastebin;

public class SetGiveawayItems {
	public static void runTask(MessageReceivedEvent e, String _link) {
		//verify pastebin link and save the content into array
		if(_link.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _link.startsWith("http")) {
			String [] rewards = Pastebin.readPublicPasteLink(_link).split("[\\r\\n]+");
			
			//calculate the next beginning of the month
			LocalTime midnight = LocalTime.MIDNIGHT;
			LocalDate today = LocalDate.now();
			LocalDate beginningOfMonth = today.with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);
			LocalDateTime beginningOfMonthMidnight = LocalDateTime.of(beginningOfMonth, midnight);
			Timestamp timestamp = Timestamp.valueOf(beginningOfMonthMidnight);
			
			//insert rewards into table and return error with true or false
			boolean err = RankingSystem.SQLBulkInsertGiveawayRewards(rewards, timestamp);
			
			if(err == false) {
				e.getTextChannel().sendMessage("Rewards from pastebin link have been inserted succesfully!").queue();
			}
			else {
				EmbedBuilder error = new EmbedBuilder().setTitle("Invalid url!").setColor(Color.RED);
				e.getTextChannel().sendMessage(error.setDescription("Rewards couldn't be set to table. Please verify the content of the link!").build()).queue();
			}
		}
		else {
			EmbedBuilder error = new EmbedBuilder().setTitle("Invalid url!").setColor(Color.RED);
			e.getTextChannel().sendMessage(error.setDescription("An invalid url has been inserted. Please insert a Pastebin link").build()).queue();
		}
	}
}
