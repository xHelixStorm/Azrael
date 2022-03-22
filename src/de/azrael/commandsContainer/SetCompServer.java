package de.azrael.commandsContainer;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.enums.Translation;
import de.azrael.sql.Competitive;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class SetCompServer {
	private final static Logger logger = LoggerFactory.getLogger(SetCompServer.class);
	
	public static void runHelp(GuildMessageReceivedEvent e) {
		e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_COMP_SERVER_HELP)
				.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD))
				.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))).build()).queue();
	}
	
	public static void runTask(GuildMessageReceivedEvent e, String [] args) {
		if(args.length == 3) {
			if(args[1].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD))) {
				final String server = args[2].toUpperCase();
				final int result = Competitive.SQLInsertCompServer(e.getGuild().getIdLong(), server);
				if(result > 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_COMP_SERVER_ADD).replace("{}", server)).build()).queue();
					logger.info("User {} has added the competitive server {} in guild {}", e.getMember().getUser().getId(), server, e.getGuild().getId());
				}
				else if(result == 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_COMP_SERVER_READD).replace("{}", server)).build()).queue();
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Comp server name {} couldn't be added in guild {}", server, e.getGuild().getIdLong());
				}
			}
			else if(args[1].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))) {
				final String server = args[2].toUpperCase();
				final int result = Competitive.SQLRemoveCompServer(e.getGuild().getIdLong(), server); 
				if(result > 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_COMP_SERVER_REMOVE).replace("{}", server)).build()).queue();
					logger.info("User {} has removed the competitive server {} in guild {}", e.getMember().getUser().getId(), server, e.getGuild().getId());
					Competitive.SQLUpdateServerFromUserStats(e.getGuild().getIdLong(), server);
				}
				else if(result == 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_COMP_SERVER_NOT_EXISTS).replace("{}", server)).build()).queue();
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Comp server name {} couldn't be removed in guild {}", server, e.getGuild().getIdLong());
				}
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
		}
	}
}
