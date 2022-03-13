package de.azrael.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.commandsContainer.FilterExecution;
import de.azrael.constructors.BotConfigs;
import de.azrael.enums.Command;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * The Filter command allows the user to edit various lists
 * for words, names and urls which are used to filter or 
 * assign names in different circumstances.
 * @author xHelixStorm
 *
 */

public class Filter implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Filter.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.FILTER);
	}

	@Override
	public boolean action(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		//run help if no arguments have been added
		if(args.length == 0)
			FilterExecution.runHelp(e);
		//execute task
		else if(args.length > 0)
			FilterExecution.runTask(e, args[0]);
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Filter command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER.getColumn(), out.toString().trim());
		}
	}
}
