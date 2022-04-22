package de.azrael.commands;

import java.awt.Color;
import java.io.File;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Cache;
import de.azrael.core.Hashes;
import de.azrael.enums.Channel;
import de.azrael.enums.Command;
import de.azrael.enums.Directory;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.sql.BotConfiguration;
import de.azrael.sql.RankingSystem;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * The DoubleExperience command will allow a user to 
 * enable/disable the double experience state or to
 * let it enable or disable on its own
 * @author xHelixStorm
 *
 */

public class DoubleExperience implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(DoubleExperience.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.DOUBLE_EXPERIENCE);
	}

	@Override
	public boolean action(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		EmbedBuilder message = new EmbedBuilder();
		//retrieve the guild settings and verify that the ranking system is enabled
		final var guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
		if(guild_settings != null && guild_settings.getRankingState()) {
			//if no parameters have been provided, show the command details
			if(args.length == 0) {
				message.setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS));
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.DOUBLE_EXPERIENCE_HELP).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_STATE))
					+ STATIC.getTranslation(e.getMember(), Translation.DOUBLE_EXPERIENCE_HELP_1).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_AUTO))
					+ STATIC.getTranslation(e.getMember(), Translation.DOUBLE_EXPERIENCE_HELP_2).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_ON))
					+ STATIC.getTranslation(e.getMember(), Translation.DOUBLE_EXPERIENCE_HELP_3).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_OFF)))
					.build()).queue();
			}
			//display the current state of the double experience (enabled/disabled/auto)
			else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_STATE))) {
				e.getChannel().sendMessage(message.setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.DOUBLE_EXPERIENCE_STATE).replace("{}", botConfig.getDoubleExperience())).build()).queue();
			}
			//change the state if either on, off or auto has been added as first parameter
			else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ON)) || args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_OFF)) || args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_AUTO))) {
				//confirm that we don't change this option, if the option we are trying to put is already saved
				if(!botConfig.getDoubleExperience().equalsIgnoreCase(getValue(e, args[0]))) {
					//overwrite the option in the guild ini file
					BotConfiguration.SQLUpdateBotConfigsDoubleExperience(e.getGuild().getIdLong(), getValue(e, args[0]));
					Hashes.removeBotConfiguration(e.getGuild().getIdLong());
					e.getChannel().sendMessage(message.setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.DOUBLE_EXPERIENCE_UPDATE).replace("{}", args[0].toUpperCase())).build()).queue();
					//if it has been enabled, write it in cache and print the double experience message in the bot channel
					if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ON))) {
						Hashes.addTempCache("doubleExp_gu"+e.getGuild().getId(), new Cache("on"));
						File doubleEvent = new File(Directory.BANNERS.getPath()+"doubleweekend.jpg");
						var bot_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.BOT.getType())).findAny().orElse(null);
						if(bot_channel != null) {
							final TextChannel textChannel = e.getGuild().getTextChannelById(bot_channel.getChannel_ID());
							if(textChannel != null && (e.getGuild().getSelfMember().hasPermission(textChannel, Permission.MESSAGE_ATTACH_FILES) || STATIC.setPermissions(e.getGuild(), textChannel, EnumSet.of(Permission.MESSAGE_ATTACH_FILES)))) {
								textChannel.sendFile(doubleEvent, "doubleweekend.jpg").queue();
								textChannel.sendMessage("```css\n"+STATIC.getTranslation(e.getMember(), Translation.DOUBLE_EXPERIENCE_MESSAGE)+"```").queue();
							}
						}
						logger.info("User {} has enabled the double experience mode for guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
					}
					//if it has been disabled, disable it in cache as well
					else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_OFF))) {
						Hashes.addTempCache("doubleExp_gu"+e.getGuild().getId(), new Cache("off"));
						logger.info("User {} has disabled the double experience mode for guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
					}
					//if it has been set to auto, remove the option from the cache
					else {
						Hashes.clearTempCache("doubleExp_gu"+e.getGuild().getId());
						logger.info("User {} has set the double experience mode to auto for guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
					}
				}
				else {
					message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.DOUBLE_EXPERIENCE_ERROR).replace("{}", args[0].toUpperCase())).build()).queue();
				}
			}
			else {
				e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.LEVEL_SYSTEM_NOT_ENABLED)).build()).queue();
		}
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used DoubleExperience command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.DOUBLE_EXPERIENCE.getColumn(), out.toString().trim());
		}
	}
	
	private String getValue(GuildMessageReceivedEvent e, String argument) {
		if(argument.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ON)))
			return "on";
		else if(argument.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_OFF)))
			return "off";
		else
			return "auto";
	}
}
