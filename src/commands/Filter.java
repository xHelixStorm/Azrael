package commands;

/**
 * The Filter command allows the user to edit various lists
 * for words, names and urls which are used to filter or 
 * assign names in different circumstances.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commandsContainer.FilterExecution;
import core.UserPrivs;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Filter implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Filter.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		//check if the command is enabled and that the user has enough permissions
		if(GuildIni.getFilterCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getFilterLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		//run help if no arguments have been added
		if(args.length == 0)
			FilterExecution.runHelp(e);
		//execute task
		else if(args.length > 0)
			FilterExecution.runTask(e, args[0]);
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("{} has used the Filter command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}
}
