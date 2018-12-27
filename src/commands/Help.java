package commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fileManagement.IniFileReader;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class Help implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(IniFileReader.getHelpCommand()){
			e.getTextChannel().sendMessage("Here all listed issues about S4. If you got something to add or to ask about a specific point, poke a GM\nhttps://s4league.aeriagames.com/forum/index.php?thread/52-guide-general-technical-issues/").queue();
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		Logger logger = LoggerFactory.getLogger(Help.class);
		logger.info("{} has used Help command", e.getMember().getUser().getId());
	}

	@Override
	public String help() {
		return null;
	}

}
