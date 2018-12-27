package commandsContainer;

import core.Guilds;
import core.Hashes;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingSystem;

public class SetLevelDefaultSkin {
	public static void runTask(MessageReceivedEvent _e, int _default_skin, int _last_theme){
		if(_default_skin <= _last_theme){
			Guilds guild_settings = Hashes.getStatus(_e.getGuild().getIdLong());
			guild_settings.setLevelID(_default_skin);
			RankingSystem.SQLUpdateLevelDefaultSkin(_e.getGuild().getIdLong(), _e.getGuild().getName(), guild_settings.getLevelID());
			Hashes.addStatus(_e.getGuild().getIdLong(), guild_settings);
			_e.getTextChannel().sendMessage("**The default skin is now the theme number "+guild_settings.getLevelID()+"!**").queue();
		}
		else{
			_e.getTextChannel().sendMessage("**"+_e.getMember().getAsMention()+" please choose one available theme out of "+_last_theme+" available themes!**").queue();
		}
	}
}
