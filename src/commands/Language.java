package commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import core.UserPrivs;
import enums.Translation;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;
import util.STATIC;

public class Language implements CommandPublic {
	final private static Logger logger = LoggerFactory.getLogger(Language.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		//check if the command is enabled and that the user has enough permissions
		if(GuildIni.getLanguageCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getLanguageLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		if(args.length == 0) {
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
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.LANGUAGE_HELP)).addField("", out.toString(), true).addField("", out2.toString(), true).build()).queue();
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Translated languages couldn't be retrieved for lang {} in guild {}", lang, e.getGuild().getId());
			}
		}
		else if(args.length == 1 && args[0].length() == 3) {
			if(Azrael.SQLisLanguageTranslated(args[0], STATIC.getLanguage(e.getMember()))) {
				if(Azrael.SQLUpdateUserLanguage(e.getMember().getUser().getIdLong(), args[0]) != -1) {
					Hashes.setLanguage(e.getMember().getUser().getIdLong(), args[0]);
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.LANGUAGE_CHANGED)).build()).queue();
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Language {} couldn't be used for the whole guild {}", args[0], e.getGuild().getId());
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

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.trace("{} has used Language command for guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}

}
