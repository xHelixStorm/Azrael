package commandsContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Guilds;
import core.Hashes;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.RankingSystem;

public class SetIconDefaultSkin {
	public static void runTask(GuildMessageReceivedEvent _e, int _default_skin, int _last_theme){
		Logger logger = LoggerFactory.getLogger(SetLevelDefaultSkin.class);
		if(_default_skin <= _last_theme){
			Guilds guild_settings = RankingSystem.SQLgetGuild(_e.getGuild().getIdLong());
			guild_settings.setIconID(_default_skin);
			if(RankingSystem.SQLUpdateIconDefaultSkin(_e.getGuild().getIdLong(), _e.getGuild().getName(), guild_settings.getIconID()) > 0) {
				logger.debug("{} has set the default icon skin id to {} in guild {}", _e.getMember().getUser().getId(), guild_settings.getIconID(), _e.getGuild().getName());
				Hashes.addStatus(_e.getGuild().getIdLong(), guild_settings);
				_e.getChannel().sendMessage("**The default skin is now the theme number "+guild_settings.getIconID()+"!**").queue();
			}
			else {
				logger.error("The RankingSystem.guilds table couldn't be updated for the default icons skin for the guild {}", _e.getGuild().getName());
				_e.getChannel().sendMessage("An internal error occurred. The table RankingSystem.guilds couldn't be updated for the icons skin").queue();
			}
		}
		else{
			_e.getChannel().sendMessage("**"+_e.getMember().getAsMention()+" please choose one available theme out of "+_last_theme+" available themes!**").queue();
		}
	}
}
