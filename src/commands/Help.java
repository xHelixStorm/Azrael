package commands;

import fileManagement.IniFileReader;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class Help implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(IniFileReader.getHelpCommand().equals("true")){
			e.getTextChannel().sendMessage("Here all listed issues about S4. If you got something to add or to ask about a specific point, poke a GM\n http://www.aeriagames.com/forums/en/viewtopic.php?t=2560215").queue();
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		
	}

	@Override
	public String help() {
		return null;
	}

}
