package listeners;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import core.*;
import fileManagement.IniFileReader;

public class CommandListener extends ListenerAdapter{
	
	public void onMessageReceived(MessageReceivedEvent e){
		
		if(e.getMessage().getContentRaw().startsWith(IniFileReader.getCommandPrefix()) && e.getMessage().getAuthor().getId() != e.getJDA().getSelfUser().getId()){
			CommandHandler.handleCommand(CommandParser.parser(e.getMessage().getContentRaw(), e));
		}
	}

}
