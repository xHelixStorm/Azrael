package listeners;

/**
 * This class gets executed when a user updates his avatar.
 * 
 * The updated avatar will be inserted into the Azrael.users
 * table.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.events.user.update.UserUpdateAvatarEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.Azrael;

public class AvatarUpdateListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(AvatarUpdateListener.class);
	
	@Override
	public void onUserUpdateAvatar(UserUpdateAvatarEvent e) {
		new Thread(() -> {
			//update user avatar with the newest avatar
			if(Azrael.SQLUpdateAvatar(e.getUser().getIdLong(), e.getUser().getEffectiveAvatarUrl()) > 0) {
				logger.trace("{} has updated his/her avatar: {}", e.getUser().getId(), e.getUser().getEffectiveAvatarUrl());
			}
			else
				logger.error("Avatar of {} couldn't be updated in Azrael.users", e.getUser().getId());
		}).start();
	}
}
