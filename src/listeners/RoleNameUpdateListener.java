package listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.events.role.update.RoleUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.DiscordRoles;

/**
 * Update role name on table
 * @author xHelixStorm
 *
 */

public class RoleNameUpdateListener extends ListenerAdapter {
	Logger logger = LoggerFactory.getLogger(RoleNameUpdateListener.class);
	
	@Override
	public void onRoleUpdateName(RoleUpdateNameEvent e) {
		if(DiscordRoles.SQLUpdateRoleName(e.getGuild().getIdLong(), e.getRole().getIdLong(), e.getNewName()) == 0) {
			logger.error("Role name of role {} couldn't be updated in guild {}", e.getRole().getId(), e.getNewName());
		}
	}
}
