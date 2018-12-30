package commandsContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.Azrael;

public class SetCommandLevel {
	
	public static void runTask(MessageReceivedEvent _e, String _input){
		int level = 0;
		boolean wrongInput = false;
		String message;
		
		switch (_input){
		case "disable":
			level = 0;
			message = "**Commands have been succesfully disabled**";
			break;
		case "bot":
			level = 1;
			message = "**Commands can now be used only in a bot channel**";
			break;
		case "enable":
			level = 2;
			message = "**Commands are now allowed to be used on all channels**";
			break;
		default:
			wrongInput = true;
			message = "**"+_e.getMember().getAsMention()+" Something went wrong. Please recheck the syntax and try again!**";
			break;
		}
		
		if(wrongInput == false){
			Azrael.SQLInsertCommand(_e.getGuild().getIdLong(), level);
			Logger logger = LoggerFactory.getLogger(SetCommandLevel.class);
			logger.debug("{} has changed the command level to {} in guild {}", _e.getMember().getUser().getId(), level, _e.getGuild().getName());
			_e.getTextChannel().sendMessage(message).queue();
		}
		else{
			_e.getTextChannel().sendMessage(message).queue();
		}
	}
}
