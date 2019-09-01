package commandsContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Guilds;
import core.Hashes;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.RankingSystem;

public class SetRankDefaultSkin {
	public static void runTask(GuildMessageReceivedEvent _e, int _default_skin, int _last_theme){
		Logger logger = LoggerFactory.getLogger(SetRankDefaultSkin.class);
		if(_default_skin <= _last_theme){
			Guilds guild_settings = RankingSystem.SQLgetGuild(_e.getGuild().getIdLong());
			guild_settings.setRankID(_default_skin);
			if(RankingSystem.SQLUpdateRankDefaultSkin(_e.getGuild().getIdLong(), _e.getGuild().getName(), guild_settings.getRankID()) > 0) {
				logger.debug("{} has set the default rank skin id to {} in guild {}", _e.getMember().getUser().getId(), guild_settings.getRankID(), _e.getGuild().getName());
				Hashes.addStatus(_e.getGuild().getIdLong(), guild_settings);
				_e.getChannel().sendMessage("**The default skin is now the theme number "+guild_settings.getRankID()+"!**").queue();
			}
			else {
				logger.error("The table RankingSystem.guilds couldn't be updated with the default rank skin for guild {}", _e.getGuild().getName());
				_e.getChannel().sendMessage("An internal error occurred. The table RankingSystem.guilds couldn't be updated with the default rank skin").queue();
			}
		}
		else{
			_e.getChannel().sendMessage("**"+_e.getMember().getAsMention()+" please choose one available theme out of "+_last_theme+" available themes!**").queue();
		}
	}
}
