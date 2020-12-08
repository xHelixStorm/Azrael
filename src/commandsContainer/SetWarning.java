package commandsContainer;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import core.Hashes;
import enums.Translation;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;
import util.STATIC;

public class SetWarning {
	private static final Logger logger = LoggerFactory.getLogger(SetWarning.class);
	
	public static void runHelp(GuildMessageReceivedEvent e) {
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(IniFileReader.getSettingsThumbnail());
		e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_WARNING_HELP)).build()).queue();
	}
	
	public static void runTask(GuildMessageReceivedEvent e, String _message) {
		int warning_value = 0;
		if(_message.replaceAll("[0-9]*", "").length() == 0) {
			warning_value = Integer.parseInt(_message);
		}
		
		if(warning_value != 0 && warning_value <= 5) {
			var editedRows = 0;
			if(warning_value < Azrael.SQLgetMaxWarning(e.getGuild().getIdLong())) {
				editedRows = Azrael.SQLLowerTotalWarning(e.getGuild().getIdLong(), warning_value);
			}
			else {
				editedRows = Azrael.SQLInsertWarning(e.getGuild().getIdLong(), warning_value);
			}
			
			if(editedRows > 0) {
				logger.info("User {} has updated the warning level to {} total warnings in guild {}", e.getMember().getUser().getId(), warning_value, e.getGuild().getId());
				EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_WARNING_1).replace("{}", ""+warning_value)).build()).queue();
				Hashes.addTempCache("warnings_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "1"));
			}
			else {
				logger.error("The warning level couldn't be updated to {} total warnings in guild {}", warning_value, e.getGuild().getId());
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_WARNING_NOT_VALID)).build()).queue();
		}
	}
	
	public static void performUpdate(GuildMessageReceivedEvent e, String _message, Cache cache, String key) {
		EmbedBuilder message = new EmbedBuilder();
		if(_message.replaceAll("[0-9]*", "").length() == 0) {
			if(cache.getExpiration() - System.currentTimeMillis() > 0) {
				var max_warning = Azrael.SQLgetMaxWarning(e.getGuild().getIdLong());
				var value = Integer.parseInt(cache.getAdditionalInfo());
				if(value < max_warning) {
					if(Azrael.SQLUpdateMuteTimeOfWarning(e.getGuild().getIdLong(), value, (Long.parseLong(_message)*60*1000)) > 0) {
						message.setColor(Color.BLUE);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_WARNING_2).replaceFirst("\\{\\}", _message).replace("{}", ""+(value+1))).build()).queue();
						logger.info("The timer of warning {} has been updated to {} in guild {}", value, _message, e.getGuild().getId());
						cache.updateDescription(""+(value+1)).setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("The Timer of warning {} couldn't be updated to {} in guild {}", value, _message, e.getGuild().getId());
					}
				}
				else if(value == max_warning) {
					if(Azrael.SQLUpdateMuteTimeOfWarning(e.getGuild().getIdLong(), value, (Long.parseLong(_message)*60*1000)) > 0) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_WARNINIG_ADDED)).build()).queue();
						Hashes.clearTempCache(key);
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("The Timer of warning {} couldn't be updated to {} in guild {}", value, _message, e.getGuild().getId());
					}
				}
			}
			else {
				Hashes.clearTempCache(key);
			}
		}
	}
}
