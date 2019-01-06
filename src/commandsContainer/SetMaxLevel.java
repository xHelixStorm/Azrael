package commandsContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Guilds;
import core.Hashes;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingSystem;

public class SetMaxLevel {
	public static void runTask(MessageReceivedEvent _e, int _max_level){
		Logger logger = LoggerFactory.getLogger(SetMaxLevel.class);
		if(_max_level <= 9999){
			Guilds guild_settings = Hashes.getStatus(_e.getGuild().getIdLong());
			guild_settings.setMaxLevel(_max_level);
			if(RankingSystem.SQLUpdateMaxLevel(_e.getGuild().getIdLong(), _e.getGuild().getName(), guild_settings.getMaxLevel()) > 0) {
				logger.debug("{} has set the max level to {} in guild {}", _e.getMember().getUser().getId(), guild_settings.getMaxLevel(), _e.getGuild().getName());
				Hashes.addStatus(_e.getGuild().getIdLong(), guild_settings);
				_e.getTextChannel().sendMessage("**The max level for the ranking system is now "+guild_settings.getMaxLevel()+"**").queue();
			}
			else {
				logger.error("RankingSystem.guilds table couldn't be updated with the max level for the guild {}", _e.getGuild().getName());
				_e.getTextChannel().sendMessage("An internal error occurred on registering the max level. Table RankingSystem.guilds couldn't be updated").queue();
			}
		}
		else{
			_e.getTextChannel().sendMessage("**"+_e.getMember().getAsMention()+" please choose a level that is lower than 10000!**").queue();
		}
	}
}
