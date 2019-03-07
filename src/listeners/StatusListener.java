package listeners;

import fileManagement.GuildIni;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Game.GameType;
import net.dv8tion.jda.core.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class StatusListener extends ListenerAdapter{
	
	public void onUserUpdateOnlineStatus(UserUpdateOnlineStatusEvent e){
		//if enabled in config file, check how many users are online and set it as currently playing
		if((e.getNewOnlineStatus().toString().equals("OFFLINE") || e.getNewOnlineStatus().toString().equals("ONLINE")) && GuildIni.getCountMembers(e.getGuild().getIdLong())){
			e.getJDA().getPresence().setGame(Game.of(GameType.DEFAULT, e.getGuild().getMembers().parallelStream().filter(f -> f.getOnlineStatus() == OnlineStatus.ONLINE || f.getOnlineStatus() == OnlineStatus.IDLE || f.getOnlineStatus() == OnlineStatus.DO_NOT_DISTURB).count()+" Members online"));
		}
		else {
			final String message = GuildIni.getGameMessage(e.getGuild().getIdLong());
			if(message != null && message.length() > 0)
				e.getJDA().getPresence().setGame(Game.of(GameType.DEFAULT, GuildIni.getGameMessage(e.getGuild().getIdLong())));
		}
	}
}
