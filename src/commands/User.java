package commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commandsContainer.UserExecution;
import core.UserPrivs;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * Display information of a user or take disciplinary actions
 * @author xHelixStorm
 *
 */

public class User implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(User.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		if(GuildIni.getUserCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getUserLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		if(args.length == 0) {
			UserExecution.getHelp(e);
		}
		else if(args.length > 0) {
			UserExecution.runTask(e, e.getMessage().getContentRaw().replaceAll("[^0-9]*", ""), args[0]);
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("{} has used User command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}
}
