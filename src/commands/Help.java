package commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.UserPrivs;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Help implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Help.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		if(GuildIni.getHelpCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getHelpLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		e.getChannel().sendMessage("Here all listed issues about S4. If you got something to add or to ask about a specific point, poke a GM\nhttps://s4league.aeriagames.com/forum/index.php?thread/52-guide-general-technical-issues/").queue();
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("{} has used Help command", e.getMember().getUser().getId());
	}

	@Override
	public String help() {
		return null;
	}

}
