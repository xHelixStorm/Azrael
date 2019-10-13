package commandsContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Guilds;
import core.Hashes;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.RankingSystem;

public class SetIconDefaultSkin {
	private final static Logger logger = LoggerFactory.getLogger(SetLevelDefaultSkin.class);
	
	public static void runTask(GuildMessageReceivedEvent _e, int _default_skin, int _last_theme) {
		if(_default_skin > 0 && _default_skin <= _last_theme) {
			if(RankingSystem.SQLUpdateIconDefaultSkin(_e.getGuild().getIdLong(), _e.getGuild().getName(), _default_skin) > 0) {
				logger.debug("{} has set the default icon skin id to {} in guild {}", _e.getMember().getUser().getId(), _default_skin, _e.getGuild().getId());
				Guilds guild_settings = RankingSystem.SQLgetGuild(_e.getGuild().getIdLong());
				if(RankingSystem.SQLUpdateUsersDefaultIconSkin(guild_settings.getIconID(), _default_skin, _e.getGuild().getIdLong()) > 0) {
					logger.debug("The default icon skin has been updated for everyone who used the previous icon skin for guild {}", _e.getGuild().getId());
					_e.getChannel().sendMessage("**The default icon skin is now the theme number "+_default_skin+"!**").queue();
				}
				else if(guild_settings.getRankingState()) {
					logger.warn("default icon skin couldn't be updated for all users for guild {}", _e.getGuild().getId());
					_e.getChannel().sendMessage("**The default icon skin couldn't be updated for all users but the server icon skin has been set!**").queue();
				}
				else if(!guild_settings.getRankingState()) {
					_e.getChannel().sendMessage("**The default icon skin is now the theme number "+_default_skin+"!**").queue();
				}
				Hashes.removeStatus(_e.getGuild().getIdLong());
				Hashes.addOldGuildSettings(_e.getGuild().getIdLong(), guild_settings);
			}
			else {
				logger.error("RankingSystem.guilds couldn't be updated with the default icon skin in guild {}", _e.getGuild().getId());
				_e.getChannel().sendMessage("An internal error occurred! The table RankingSystem.guilds couldn't be updated with the default icon skin").queue();
			}
		}
		else{
			_e.getChannel().sendMessage("**"+_e.getMember().getAsMention()+" please choose one available skin out of "+_last_theme+" available skins!**").queue();
		}
	}
}
