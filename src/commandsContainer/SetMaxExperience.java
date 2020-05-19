package commandsContainer;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Guilds;
import core.Hashes;
import enums.Translation;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.RankingSystem;
import util.STATIC;

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
			if(matcher.group().equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE))) {
				guild_settings.setMaxExpEnabled(true);
				editedRows = RankingSystem.SQLUpdateMaxExperience(guild_settings.getMaxExperience(), guild_settings.getMaxExpEnabled(), e.getGuild().getIdLong());
				if(editedRows > 0)
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_MAX_EXPERIENCE_ENABLED)).build()).queue();
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				}
			}
			else if(matcher.group().equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISABLE))) {
				guild_settings.setMaxExpEnabled(false);
				editedRows = RankingSystem.SQLUpdateMaxExperience(guild_settings.getMaxExperience(), guild_settings.getMaxExpEnabled(), e.getGuild().getIdLong());
				if(editedRows > 0)
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_MAX_EXPERIENCE_DISABLED)).build()).queue();
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				}
			}
			if(editedRows > 0) {
				logger.debug("{} has set the max experience limitation to {} in guild {}", e.getMember().getUser().getId(), matcher.group(), e.getGuild().getId());
				Hashes.addStatus(e.getGuild().getIdLong(), guild_settings);
			}
			else {
				logger.error("RankingSystem.max_exp couldn't be updated with enable or disable information for guild {}", e.getGuild().getName());
			}
		}
		else {
			if(_input.replaceAll("[0-9]*", "").length() == 0) {
				guild_settings.setMaxExperience(Long.parseLong(_input));
				guild_settings.setMaxExpEnabled(true);
				if(RankingSystem.SQLUpdateMaxExperience(guild_settings.getMaxExperience(), guild_settings.getMaxExpEnabled(), e.getGuild().getIdLong()) > 0) {
					Hashes.addStatus(e.getGuild().getIdLong(), guild_settings);
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_MAX_EXPERIENCE_ADDED)+ "**"+guild_settings.getMaxExperience()+"**").build()).queue();
					logger.debug("{} has set the max experience limitation to {} exp in guild {}", e.getMember().getUser().getId(), guild_settings.getMaxExperience(), e.getGuild().getId());
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Max experience couldn't be updated in table RankingSystem.guilds for guild {}", e.getGuild().getId());
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
			}
		}
	}
}
