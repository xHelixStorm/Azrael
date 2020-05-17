package listeners;

import fileManagement.IniFileReader;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * This class gets executed when a user changed his status (e.g. offline to online)
 * 
 * This class will either count all available members or it will display a game message
 * @author xHelixStorm
 * 
 */

public class StatusListener extends ListenerAdapter{
	
	public void onUserUpdateOnlineStatus(UserUpdateOnlineStatusEvent e) {
		new Thread(() -> {
			//if enabled in config file, check how many users are online and set it as currently playing
			if((e.getNewOnlineStatus().toString().equals("OFFLINE") || e.getNewOnlineStatus().toString().equals("ONLINE")) && IniFileReader.getCountMembers()) {
				e.getJDA().getPresence().setActivity(Activity.of(ActivityType.DEFAULT, e.getGuild().getMembers().parallelStream().filter(f -> f.getOnlineStatus() == OnlineStatus.ONLINE || f.getOnlineStatus() == OnlineStatus.IDLE || f.getOnlineStatus() == OnlineStatus.DO_NOT_DISTURB).count()+" Discord Members online"));
			}
			else {
				//set the game message if provided
				final String message = IniFileReader.getGameMessage();
				if(message != null && message.length() > 0)
					e.getJDA().getPresence().setActivity(Activity.of(ActivityType.DEFAULT, IniFileReader.getGameMessage()));
			}
		}).start();
	}
}
