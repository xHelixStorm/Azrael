package de.azrael.commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.util.Hashes;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Language implements CommandPublic {
	final private static Logger logger = LoggerFactory.getLogger(Language.class);

	@Override
	public boolean called(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.LANGUAGE);
	}

	@Override
	public boolean action(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		if(args.length == 0) {
			final String lang = STATIC.getLanguage(e.getMember());
			final var languages = Azrael.SQLgetTranslatedLanguages(lang);
			if(languages != null && languages.size() > 0) {
				StringBuilder out = new StringBuilder();
				StringBuilder out2 = new StringBuilder();
				languages.forEach((k,v) -> {
					out.append("**"+k+"**\n");
					out2.append(v+"\n");
				});
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.LANGUAGE_HELP)).addField("", out.toString(), true).addField("", out2.toString(), true).build()).queue();
			}
			else {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Translated languages couldn't be retrieved for lang {} in guild {}", lang, e.getGuild().getId());
			}
		}
		else if(args.length == 1 && args[0].length() == 3) {
			if(Azrael.SQLisLanguageTranslated(args[0])) {
				if(Azrael.SQLUpdateUserLanguage(e.getMember().getUser().getIdLong(), args[0]) != -1) {
					Hashes.setLanguage(e.getMember().getUser().getIdLong(), args[0]);
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.LANGUAGE_CHANGED)).build()).queue();
				}
				else {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Language {} couldn't be used for the whole guild {}", args[0], e.getGuild().getId());
				}
			}
			else {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_LANGUAGE_ERR)).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
		}
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, MessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Language command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.LANGUAGE.getColumn(), out.toString().trim());
		}
	}

}
