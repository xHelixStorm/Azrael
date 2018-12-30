package commandsContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Guilds;
import core.Hashes;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingSystem;

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
			RankingSystem.SQLUpdateRankingSystem(_e.getGuild().getIdLong(), _e.getGuild().getName(), ranking_state);
			Guilds guild = Hashes.getStatus(_e.getGuild().getIdLong());
			guild.setRankingState(ranking_state);
			
			Hashes.addStatus(_e.getGuild().getIdLong(), guild);
			Logger logger = LoggerFactory.getLogger(SetRankingSystem.class);
			logger.debug("{} has set the ranking system to {} in guild {}", _e.getMember().getUser().getId(), _input, _e.getGuild().getName());
			_e.getTextChannel().sendMessage(message).queue();
		}
		else{
			_e.getTextChannel().sendMessage(message).queue();
		}
	}
}
