package de.azrael.listeners;

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

public class StatusListener extends ListenerAdapter {
	private static String oldActivity = null;
	
	public void onUserUpdateOnlineStatus(UserUpdateOnlineStatusEvent e) {
		new Thread(() -> {
			//if enabled in config file, check how many users are online and set it as currently playing
			if(System.getProperty("COUNT_GUILDS").equals("true") && (e.getNewOnlineStatus().toString().equals("OFFLINE") || e.getNewOnlineStatus().toString().equals("ONLINE"))) {
				e.getJDA().getPresence().setActivity(Activity.of(ActivityType.DEFAULT, e.getJDA().getGuilds().size()+" Discord servers"));
			}
			else {
				//set the game message if provided
				final String message = System.getProperty("STATUS_MESSAGE");
				if(message != null && message.length() > 0 && !message.equals(oldActivity)) {
					oldActivity = message;
					e.getJDA().getPresence().setActivity(Activity.of(ActivityType.DEFAULT, message));
				}
			}
		}).start();
	}
}
