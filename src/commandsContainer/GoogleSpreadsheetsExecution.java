package commandsContainer;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import core.Hashes;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class GoogleSpreadsheetsExecution {
	private final static Logger logger = LoggerFactory.getLogger(GoogleSpreadsheetsExecution.class);
	private final static EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);

	public static void runTask(GuildMessageReceivedEvent e) {
		final String key = "google_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId();
		e.getChannel().sendMessage(message.setDescription("Use one of the below parameters to navigate through the spreadsheets setup or type **exit** to close the setup any time\n\n"
			+ "**create**: create a spreadsheets table\n"
			+ "**add**: add an already existing spreadsheet table\n"
			+ "**events**: select an already existing spreadsheet table\n"
			+ "**map**: map the available table fields with values from the bot").build()).queue();
		Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
	}
}
