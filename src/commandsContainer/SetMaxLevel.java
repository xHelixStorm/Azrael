package commandsContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Guilds;
import core.Hashes;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingSystem;

public class SetMaxLevel {
	public static void runTask(MessageReceivedEvent _e, int _max_level){
		if(_max_level <= 9999){
			Guilds guild_settings = Hashes.getStatus(_e.getGuild().getIdLong());
			guild_settings.setMaxLevel(_max_level);
			RankingSystem.SQLUpdateMaxLevel(_e.getGuild().getIdLong(), _e.getGuild().getName(), guild_settings.getMaxLevel());
			
			Logger logger = LoggerFactory.getLogger(SetMaxLevel.class);
			logger.debug("{} has set the max level to {} in guild {}", _e.getMember().getUser().getId(), guild_settings.getMaxLevel(), _e.getGuild().getName());
			Hashes.addStatus(_e.getGuild().getIdLong(), guild_settings);
			_e.getTextChannel().sendMessage("**The max level for the ranking system is now "+guild_settings.getMaxLevel()+"**").queue();
		}
		else{
			_e.getTextChannel().sendMessage("**"+_e.getMember().getAsMention()+" please choose a level that is lower than 10000!**").queue();
		}
	}
}
