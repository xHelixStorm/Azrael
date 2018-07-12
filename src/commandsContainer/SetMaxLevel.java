package commandsContainer;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingDB;

public class SetMaxLevel {
	public static void runTask(MessageReceivedEvent _e, int _max_level){
		if(_max_level <= 9999){
			RankingDB.SQLUpdateMaxLevel(_e.getGuild().getIdLong(), _e.getGuild().getName(), _max_level);
			_e.getTextChannel().sendMessage("**The max level for the ranking system is now "+_max_level+"**").queue();
		}
		else{
			_e.getTextChannel().sendMessage("**"+_e.getMember().getAsMention()+" please choose a level that is lower than 10000!**").queue();
		}
	}
}
