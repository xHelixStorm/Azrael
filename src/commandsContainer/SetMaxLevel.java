package commandsContainer;

import core.Guilds;
import core.Hashes;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingDB;

public class SetMaxLevel {
	public static void runTask(MessageReceivedEvent _e, int _max_level){
		if(_max_level <= 9999){
			Guilds guild_settings = Hashes.getStatus(_e.getGuild().getIdLong());
			guild_settings.setMaxLevel(_max_level);
			RankingDB.SQLUpdateMaxLevel(_e.getGuild().getIdLong(), _e.getGuild().getName(), guild_settings.getMaxLevel());
			Hashes.addStatus(_e.getGuild().getIdLong(), guild_settings);
			_e.getTextChannel().sendMessage("**The max level for the ranking system is now "+guild_settings.getMaxLevel()+"**").queue();
		}
		else{
			_e.getTextChannel().sendMessage("**"+_e.getMember().getAsMention()+" please choose a level that is lower than 10000!**").queue();
		}
	}
}
