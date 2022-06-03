package de.azrael.commandsContainer;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.enums.Translation;
import de.azrael.sql.Competitive;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class SetMap {
	private static Logger logger = LoggerFactory.getLogger(SetMap.class);
	
	public static void runHelp(GuildMessageReceivedEvent e) {
		e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_MAP_HELP)).build()).queue();
	}
	
	public static void runTask(GuildMessageReceivedEvent e, String [] args) {
		final int lastIndex = args.length-1;
		boolean endsInUrl = false;
		if(args[lastIndex].startsWith("http") && (args[lastIndex].endsWith("jpg") || args[lastIndex].endsWith("png") || args[lastIndex].endsWith("gif"))) {
			endsInUrl = true;
		}
		
		StringBuilder map = new StringBuilder();
		for(int i = 1; i < args.length; i++) {
			if(i == args.length-1 && endsInUrl)
				break;
			map.append(args[i]+" ");
		}
		final String insertMap = map.toString().trim();
		
		String url = null;
		if(endsInUrl)
			url = args[lastIndex];
		
		if(Competitive.SQLInsertMap(e.getGuild().getIdLong(), insertMap, url) > 0) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_MAP_SUCCESS)).build()).queue();
			logger.info("User {} has saved the new competitive map {} in guild {}", e.getMember().getUser().getId(), insertMap, e.getGuild().getId());
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Competitive map {} couldn't be saved in guild {}", insertMap, e.getGuild().getId());
		}
	}
}
