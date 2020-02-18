package commandsContainer;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class GoogleDocsExecution {
	private final static Logger logger = LoggerFactory.getLogger(GoogleDocsExecution.class);
	
	public static void runTask(GuildMessageReceivedEvent e) {
		final String key = "google_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId();
		e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription("Google docs is not yet supported!").build()).queue();
		logger.debug("Google docs is not yet supported!");
		Hashes.clearTempCache(key);
	}
}
