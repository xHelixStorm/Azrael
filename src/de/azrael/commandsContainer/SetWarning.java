package de.azrael.commandsContainer;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Cache;
import de.azrael.core.Hashes;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.sql.Azrael;
import de.azrael.sql.BotConfiguration;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class SetWarning {
	private static final Logger logger = LoggerFactory.getLogger(SetWarning.class);
	
	public static void runHelp(GuildMessageReceivedEvent e) {
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(BotConfiguration.SQLgetThumbnails(e.getGuild().getIdLong()).getSettings());
		e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_WARNING_HELP)).build()).queue();
	}
	
	public static void runTask(GuildMessageReceivedEvent e, String message) {
		int warningValue = 0;
		if(message.matches("[0-9]{1,}")) {
			warningValue = Integer.parseInt(message);
		}
		
		if(warningValue > 0 && warningValue <= 30) {
			var editedRows = 0;
			if(warningValue < Azrael.SQLgetMaxWarning(e.getGuild().getIdLong())) {
				editedRows = Azrael.SQLLowerTotalWarning(e.getGuild().getIdLong(), warningValue);
			}
			else {
				editedRows = Azrael.SQLInsertWarning(e.getGuild().getIdLong(), warningValue);
			}
			
			if(editedRows > 0) {
				logger.info("User {} has updated the warning level to {} total warnings in guild {}", e.getMember().getUser().getId(), warningValue, e.getGuild().getId());
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_WARNING_1).replace("{}", ""+warningValue)).build()).queue();
				Hashes.addTempCache("warnings_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "1", ""+warningValue));
			}
			else {
				logger.error("The warning level couldn't be updated to {} total warnings in guild {}", warningValue, e.getGuild().getId());
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_WARNING_NOT_VALID)).build()).queue();
		}
	}
	
	public static void performUpdate(GuildMessageReceivedEvent e, String message, Cache cache, String key) {
		if(message.matches("[0-9]{1,}")) {
			if(cache.getExpiration() - System.currentTimeMillis() > 0) {
				var value = Integer.parseInt(cache.getAdditionalInfo());
				var maxWarning = Integer.parseInt(cache.getAdditionalInfo2());
				if(value <= maxWarning) {
					if(Azrael.SQLUpdateMuteTimeOfWarning(e.getGuild().getIdLong(), value, (Long.parseLong(message)*60*1000)) > 0) {
						if(value < maxWarning) {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_WARNING_2).replaceFirst("\\{\\}", ""+value).replace("{}", ""+(value+1))).build()).queue();
							logger.info("The timer of warning {} has been updated to {} in guild {}", value, message, e.getGuild().getId());
							cache.updateDescription(""+(value+1)).setExpiration(180000);
							Hashes.addTempCache(key, cache);
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_WARNINIG_ADDED)).build()).queue();
							logger.info("The timer of warning {} has been updated to {} in guild {}", value, message, e.getGuild().getId());
							Hashes.clearTempCache(key);
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("The Timer of warning {} couldn't be updated to {} in guild {}", value, message, e.getGuild().getId());
					}
				}
			}
			else {
				Hashes.clearTempCache(key);
			}
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SET_WARNINGS.getColumn(), message);
		}
	}
}
