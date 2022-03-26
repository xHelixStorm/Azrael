package de.azrael.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.commandsContainer.UserExecution;
import de.azrael.constructors.BotConfigs;
import de.azrael.enums.Command;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * Display information of a user or take disciplinary actions
 * @author xHelixStorm
 *
 */

public class User implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(User.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.USER);
	}

	@Override
	public boolean action(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(args.length == 0) {
			UserExecution.getHelp(e);
		}
		else if(args.length > 0) {
			StringBuilder arguments = new StringBuilder();
			for(int i = 0; i < args.length; i++) {
				arguments.append(args[i]+" ");
			}
			UserExecution.runTask(e, e.getMessage().getContentRaw(), arguments.toString().trim(), botConfig);
		}
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used User command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.USER.getColumn(), out.toString().trim());
		}
	}
}
