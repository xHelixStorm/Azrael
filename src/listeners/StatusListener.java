package listeners;

import fileManagement.IniFileReader;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Game.GameType;
import net.dv8tion.jda.core.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class StatusListener extends ListenerAdapter{
	
	public void onUserUpdateOnlineStatus(UserUpdateOnlineStatusEvent e){
		//if enabled in config file, check how many users are online and set it as currently playing
		if((e.getNewOnlineStatus().toString().equals("OFFLINE") || e.getNewOnlineStatus().toString().equals("ONLINE")) && IniFileReader.getCountMembers()){
			e.getJDA().getPresence().setGame(Game.of(GameType.DEFAULT, e.getGuild().getMembers().parallelStream().filter(f -> f.getOnlineStatus() == OnlineStatus.ONLINE).count()+" Members online"));
		}
	}
}
