package de.azrael.commands.util;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.enums.Translation;
import de.azrael.sql.Azrael;
import de.azrael.util.Hashes;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SetLanguage {
	private final static Logger logger = LoggerFactory.getLogger(SetLanguage.class);
	
	public static void runHelp(MessageReceivedEvent e) {
		final String lang = STATIC.getLanguage(e.getMember());
		final var languages = Azrael.SQLgetTranslatedLanguages(lang);
		if(languages != null && languages.size() > 0) {
			StringBuilder out = new StringBuilder();
			StringBuilder out2 = new StringBuilder();
			languages.forEach((k,v) -> {
				out.append("**"+k+"**\n");
				out2.append(v+"\n");
			});
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_LANGUAGE_HELP)).addField("", out.toString(), true).addField("", out2.toString(), true).build()).queue();
		}
		else {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Translated languages couldn't be retrieved for language {} in guild {}", lang, e.getGuild().getId());
		}
	}
	
	public static void runTask(MessageReceivedEvent e, String [] args) {
		if(args.length == 2 && args[1].length() == 3) {
			final var lang = args[1];
			if(Azrael.SQLisLanguageTranslated(lang)) {
				if(Azrael.SQLUpdateLanguage(e.getGuild().getIdLong(), lang) != -1) {
					Hashes.setLanguage(e.getGuild().getIdLong(), lang);
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_LANGUAGE_SUCCESS)).build()).queue();
					logger.info("User {} has updated the default server language to {} in guild {}", e.getMember().getUser().getId(), lang, e.getGuild().getId());
				}
				else {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Language {} couldn't be used in guild {}", lang, e.getGuild().getId());
				}
			}
			else {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_LANGUAGE_ERR)).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
		}
	}
}
