package listeners;

import net.dv8tion.jda.core.events.user.update.UserUpdateAvatarEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.SqlConnect;

public class AvatarUpdateListener extends ListenerAdapter{
	
	@Override
	public void onUserUpdateAvatar(UserUpdateAvatarEvent e){
		SqlConnect.SQLUpdateAvatar(e.getUser().getIdLong(), e.getUser().getEffectiveAvatarUrl());
	}
}
