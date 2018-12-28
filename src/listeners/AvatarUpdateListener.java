package listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.core.events.user.update.UserUpdateAvatarEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.Azrael;

public class AvatarUpdateListener extends ListenerAdapter{
	
	@Override
	public void onUserUpdateAvatar(UserUpdateAvatarEvent e){
		Logger logger = LoggerFactory.getLogger(AvatarUpdateListener.class);
		logger.info("{} has updated his/her avatar: {}", e.getUser().getId(), e.getUser().getEffectiveAvatarUrl());
		Azrael.SQLUpdateAvatar(e.getUser().getIdLong(), e.getUser().getEffectiveAvatarUrl());
	}
}
