package commandsContainer;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingDB;

public class SetRankingSystem {
	public static void runTask(MessageReceivedEvent _e, String _input){
		boolean ranking_state = false;
		boolean wrongInput = false;
		String message;
		
		switch(_input){
			case "enable":
				ranking_state = true;
				message = "**Ranking system has been succesfully enabled!**";
				break;
			case "disable":
				ranking_state = false;
				message = "**Ranking system has been succesfully disabled!**";
				break;
			default:
				wrongInput = true;
				message = "**"+_e.getMember().getAsMention()+" Something went wrong. Please recheck the syntax and try again!**";
				break;
		}
		
		if(wrongInput == false){
			RankingDB.SQLUpdateRankingSystem(_e.getGuild().getIdLong(), _e.getGuild().getName(), ranking_state);
			_e.getTextChannel().sendMessage(message).queue();
		}
		else{
			_e.getTextChannel().sendMessage(message).queue();
		}
	}
}
