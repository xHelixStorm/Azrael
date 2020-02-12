package commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.UserPrivs;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * Google service set up for specific events
 * @author xHelixStorm
 *
 */

public class Google implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Google.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		//check if the command is enabled and that the user has enough permissions
		if(GuildIni.getGoogleCommand(e.getGuild().getIdLong())) {
			var commandLevel = GuildIni.getGoogleLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || e.getMember().getUser().getIdLong() == GuildIni.getAdmin(e.getGuild().getIdLong()))
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("{} has used Google command in guild {}", e.getMember().getUser().getIdLong(), e.getGuild().getId());
	}

}
