package de.azrael.commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.core.Hashes;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.fileManagement.IniFileReader;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.threads.LowerHeavyCensoring;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * The HeavyCensoring command can be either enabled or disabled
 * to make the overall filter more sensitive.
 * @author xHelixStorm
 *
 */

public class HeavyCensoring implements CommandPublic {
	Logger logger = LoggerFactory.getLogger(HeavyCensoring.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.HEAVY_CENSORING);
	}


	@Override
	public boolean action(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		//run help if no parameter has been added
		if(args.length == 0) {
			e.getChannel().sendMessage(new EmbedBuilder().setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.HEAVY_CENSORING_HELP)).build()).queue();
		}
		//enter if a parameter has been passed
		else if(args.length == 1) {
			//enter this block if the parameter equals to 'enable', 'disable' or 'reset'
			if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE)) || args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISABLE)) || args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_RESET))) {
				//retrieve the current heavy censoring state (e.g. enabled or disabled) in boolean
				var heavyCensoring = Hashes.getHeavyCensoring(e.getGuild().getIdLong());
				//enable the heavy censoring
				if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE))) {
					//only enable if heavy censoring is disabled or empty
					if(heavyCensoring == null || !heavyCensoring) {
						Hashes.addHeavyCensoring(e.getGuild().getIdLong(), true);
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.HEAVY_CENSORING_ENABLED)).setThumbnail(IniFileReader.getHeavyThumbnail()).build()).queue();
						logger.info("User {} has enabled heavy censoring in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
						new Thread(new LowerHeavyCensoring(e)).start();
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.HEAVY_CENSORING_IS_ENABLED)).build()).queue();
					}
				}
				//disable the heavy censoring
				else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISABLE))) {
					//only disable if heavy censoring is enabled
					if(heavyCensoring != null && heavyCensoring) {
						//clear all heavy censoring related HashMaps and thread
						Hashes.addHeavyCensoring(e.getGuild().getIdLong(), false);
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.HEAVY_CENSORING_DISABLED)).setThumbnail(IniFileReader.getHeavyEndThumbnail()).build()).queue();
						logger.info("User {} has disabled heavy censoring in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
						Hashes.removeCensoreMessage(e.getGuild().getIdLong());
						Hashes.removeFilterThreshold(e.getGuild().getIdLong());
						Hashes.getHeavyCensoringThread(e.getGuild().getIdLong()).interrupt();
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.HEAVY_CENSORING_IS_DISABLED)).build()).queue();
					}
				}
				//reset the heavy censoring threshold
				else {
					//only reset if the heavy censoring is enabled
					if(heavyCensoring != null && heavyCensoring) {
						Hashes.addFilterThreshold(e.getGuild().getIdLong(), "0");
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.HEAVY_CENSORING_RESET)).build()).queue();
						logger.info("User {} has resetted the heavy censoring threshold back to 0 in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.HEAVY_CENSORING_RESET_ERR)).build()).queue();
					}
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
		}
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used HeavyCensoring command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.HEAVY_CENSORING.getColumn(), out.toString().trim());
		}
	}

}
