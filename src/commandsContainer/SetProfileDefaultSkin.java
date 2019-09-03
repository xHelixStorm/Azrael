package commandsContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Guilds;
import core.Hashes;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.RankingSystem;

public class SetProfileDefaultSkin {
	private final static Logger logger = LoggerFactory.getLogger(SetProfileDefaultSkin.class);
	
	public static void runTask(GuildMessageReceivedEvent _e, int _default_skin, int _last_theme) {
		if(_default_skin <= _last_theme){
			Guilds guild_settings = RankingSystem.SQLgetGuild(_e.getGuild().getIdLong());
			guild_settings.setProfileID(_default_skin);
			if(RankingSystem.SQLUpdateProfileDefaultSkin(_e.getGuild().getIdLong(), _e.getGuild().getName(), guild_settings.getProfileID()) > 0) {
				logger.debug("{} has set the default profile skin id to {} in guild {}", _e.getMember().getUser().getId(), guild_settings.getProfileID(), _e.getGuild().getId());
				Hashes.addStatus(_e.getGuild().getIdLong(), guild_settings);
				_e.getChannel().sendMessage("**The default skin is now the theme number "+guild_settings.getProfileID()+"!**").queue();
			}
			else {
				logger.error("The RankingSystem.guilds table couldn't be updated for the default profile skin for the guild {}", _e.getGuild().getName());
				_e.getChannel().sendMessage("An internal error occurred. The default profile skin couldn't be updated on the RankingSystem.guilds table").queue();
			}
		}
		else{
			_e.getChannel().sendMessage("**"+_e.getMember().getAsMention()+" please choose one available theme out of "+_last_theme+" available themes!**").queue();
		}
	}
}
