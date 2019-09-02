package commandsContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;

public class SetCommandLevel {
	
	@SuppressWarnings("preview")
	public static void runTask(GuildMessageReceivedEvent _e, String _input){
		int level = 0;
		boolean wrongInput = false;
		String message;
		
		switch (_input) {
			case "disable" -> {
				level = 0;
				message = "**Commands have been succesfully disabled**";
			}
			case "bot" -> {
				level = 1;
				message = "**Commands can now be used only in a bot channel**";
			}
			case "enable" -> {
				level = 2;
				message = "**Commands are now allowed to be used on all channels**";
			}
			default -> {
				wrongInput = true;
				message = "**"+_e.getMember().getAsMention()+" Something went wrong. Please recheck the syntax and try again!**";
			}
		}
		
		if(wrongInput == false){
			Logger logger = LoggerFactory.getLogger(SetCommandLevel.class);
			if(Azrael.SQLInsertCommand(_e.getGuild().getIdLong(), level) > 0) {
				logger.debug("{} has changed the command level to {} in guild {}", _e.getMember().getUser().getId(), level, _e.getGuild().getId());
				_e.getChannel().sendMessage(message).queue();
			}
			else {
				logger.error("The execution id for guild {} couldn't be updated on table Azrael.commands", _e.getGuild().getName());
				_e.getChannel().sendMessage("An internal error occurred.The execution id couldn't be set or updated in Azrael.commands").queue();
			}
		}
		else{
			_e.getChannel().sendMessage(message).queue();
		}
	}
}
