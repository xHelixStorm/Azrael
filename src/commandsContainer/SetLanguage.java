package commandsContainer;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import enums.Translation;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;
import util.STATIC;

public class SetLanguage {
	private final static Logger logger = LoggerFactory.getLogger(SetLanguage.class);
	
	public static void runHelp(GuildMessageReceivedEvent e) {
		final String lang = STATIC.getLanguage(e.getMember());
		final var languages = Azrael.SQLgetTranslatedLanguages(lang);
		if(languages != null && languages.size() > 0) {
			StringBuilder out = new StringBuilder();
			StringBuilder out2 = new StringBuilder();
			for(final var language : languages) {
				final String [] langSplit = language.split("-");
				out.append("**"+langSplit[0]+"**\n");
				out2.append(langSplit[1]+"\n");
			}
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_LANGUAGE_HELP)).addField("", out.toString(), true).addField("", out2.toString(), true).build()).queue();
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Translated languages couldn't be retrieved for language {} in guild {}", lang, e.getGuild().getId());
		}
	}
	
	public static void runTask(GuildMessageReceivedEvent e, String [] args) {
		if(args.length == 2 && args[1].length() == 3) {
			final var lang = args[1];
			if(Azrael.SQLisLanguageTranslated(lang, STATIC.getLanguage(e.getMember()))) {
				if(Azrael.SQLUpdateLanguage(e.getGuild().getIdLong(), lang) != -1) {
					Hashes.setLanguage(e.getGuild().getIdLong(), lang);
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_LANGUAGE_SUCCESS)).build()).queue();
					logger.info("User {} has updated the default server language to {} in guild {}", e.getMember().getUser().getId(), lang, e.getGuild().getId());
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Language {} couldn't be used in guild {}", lang, e.getGuild().getId());
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_LANGUAGE_ERR)).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
		}
	}
}
