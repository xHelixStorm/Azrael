package de.azrael.commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Cache;
import de.azrael.core.Hashes;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Schedule implements CommandPublic {
	private static Logger logger = LoggerFactory.getLogger(Schedule.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.SCHEDULE);
	}

	@Override
	public boolean action(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SCHEDULE_HELP)
				.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_EXIT))+"\n\n"
			+ "**"+STATIC.getTranslation(e.getMember(), Translation.PARAM_DISPLAY)+"**\n"
			+ "**"+STATIC.getTranslation(e.getMember(), Translation.PARAM_CREATE)+"**\n"
			+ "**"+STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE)+"**").build()).queue();
		Hashes.addTempCache("schedule_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000));
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Schedule command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SCHEDULE.getColumn(), out.toString().trim());
		}
	}

}
