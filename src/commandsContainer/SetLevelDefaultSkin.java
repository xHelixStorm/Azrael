package commandsContainer;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingDB;

public class SetLevelDefaultSkin {
	public static void runTask(MessageReceivedEvent _e, int _default_skin, int _last_theme){
		if(_default_skin <= _last_theme){
			RankingDB.SQLUpdateLevelDefaultSkin(_e.getGuild().getIdLong(), _e.getGuild().getName(), _default_skin);
			_e.getTextChannel().sendMessage("**The default skin is now the theme number "+_default_skin+"!**").queue();
		}
		else{
			_e.getTextChannel().sendMessage("**"+_e.getMember().getAsMention()+" please choose one available theme out of "+_last_theme+" available themes!**").queue();
		}
	}
}
