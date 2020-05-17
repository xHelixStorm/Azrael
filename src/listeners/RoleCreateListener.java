package listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.DiscordRoles;

/**
 * This class gets executed when a new role gets created.
 * 
 * The newly created role will be inserted into the 
 * DiscordRoles.roles table.
 * @author xHelixStorm
 */

public class RoleCreateListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(RoleCreateListener.class);
	
	@Override
	public void onRoleCreate(RoleCreateEvent e) {
		new Thread(() -> {
			//When a new role gets created, insert it into table with the default role attribute
			var inserted = DiscordRoles.SQLInsertRole(e.getGuild().getIdLong(), e.getRole().getIdLong(), 1, e.getRole().getName(), "def", false);
			if(inserted > 0) {
				DiscordRoles.SQLgetRoles(e.getGuild().getIdLong());
				logger.debug("role id {} has been registered from guild {}", e.getRole().getName(), e.getGuild().getName());
			}
			else {
				logger.error("role id {} couldn't be registered for guild {} in table DiscordRoles.roles", e.getRole().getName(), e.getGuild().getName() );
			}
		}).start();
	}
}
