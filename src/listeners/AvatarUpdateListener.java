package listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.events.user.update.UserUpdateAvatarEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.Azrael;

/**
 * This class gets executed when a user updates his avatar.
 * 
 * The updated avatar will be inserted into the Azrael.users
 * table.
 * @author xHelixStorm
 * 
 */

public class AvatarUpdateListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(AvatarUpdateListener.class);
	
	@Override
	public void onUserUpdateAvatar(UserUpdateAvatarEvent e) {
		new Thread(() -> {
			//update user avatar with the newest avatar
			final var result = Azrael.SQLUpdateAvatar(e.getUser().getIdLong(), e.getUser().getEffectiveAvatarUrl());
			if(result > 0) {
				logger.trace("User {} has updated the avatar to {}", e.getUser().getId(), e.getUser().getEffectiveAvatarUrl());
			}
			else if(result == -1)
				logger.error("Avatar of user {} couldn't be updated", e.getUser().getId());
		}).start();
	}
}
