package de.azrael.commandsContainer;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Guilds;
import de.azrael.core.Hashes;
import de.azrael.enums.Translation;
import de.azrael.sql.RankingSystem;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * Extension of the set command
 * @author xHelixStorm
 *
 */

public class SetMaxExperience {
	private final static Logger logger = LoggerFactory.getLogger(SetMaxExperience.class);
	
	public static void runTask(GuildMessageReceivedEvent e, String _input, Guilds guild_settings) {
		Pattern pattern = Pattern.compile("("+STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE)+"|"+STATIC.getTranslation(e.getMember(), Translation.PARAM_DISABLE)+")");
		Matcher matcher = pattern.matcher(_input);
		if(matcher.find()){
			var editedRows = 0;
			final var newStatus = matcher.group();
			if(newStatus.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE))) {
				guild_settings.setMaxExpEnabled(true);
				editedRows = RankingSystem.SQLUpdateMaxExperience(guild_settings.getMaxExperience(), guild_settings.getMaxExpEnabled(), e.getGuild().getIdLong());
				if(editedRows > 0)
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_MAX_EXPERIENCE_ENABLED)).build()).queue();
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				}
			}
			else if(newStatus.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISABLE))) {
				guild_settings.setMaxExpEnabled(false);
				editedRows = RankingSystem.SQLUpdateMaxExperience(guild_settings.getMaxExperience(), guild_settings.getMaxExpEnabled(), e.getGuild().getIdLong());
				if(editedRows > 0)
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_MAX_EXPERIENCE_DISABLED)).build()).queue();
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				}
			}
			if(editedRows > 0) {
				Hashes.addStatus(e.getGuild().getIdLong(), guild_settings);
				logger.info("User {} has updated the max experience status to {} in guild {}", e.getMember().getUser().getId(), newStatus, e.getGuild().getId());
			}
			else {
				logger.error("Max experience status couldn't be updated in guild {}", e.getGuild().getId());
			}
		}
		else {
			if(_input.replaceAll("[0-9]*", "").length() == 0) {
				guild_settings.setMaxExperience(Long.parseLong(_input));
				guild_settings.setMaxExpEnabled(true);
				if(RankingSystem.SQLUpdateMaxExperience(guild_settings.getMaxExperience(), guild_settings.getMaxExpEnabled(), e.getGuild().getIdLong()) > 0) {
					Hashes.addStatus(e.getGuild().getIdLong(), guild_settings);
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_MAX_EXPERIENCE_ADDED)+ "**"+guild_settings.getMaxExperience()+"**").build()).queue();
					logger.info("User {} has set the max experience limit to {} experience points in guild {}", e.getMember().getUser().getId(), guild_settings.getMaxExperience(), e.getGuild().getId());
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("The max experience limit of {} exprience points couldn't be updated in guild {}", guild_settings.getMaxExperience(), e.getGuild().getId());
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
			}
		}
	}
}
