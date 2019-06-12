package listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.core.events.role.RoleCreateEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.DiscordRoles;

public class RoleCreateListener extends ListenerAdapter{
	
	@Override
	public void onRoleCreate(RoleCreateEvent e) {
		Logger logger = LoggerFactory.getLogger(RoleCreateListener.class);
		//When a new role gets created, insert it into table as default role
		var inserted = DiscordRoles.SQLInsertRole(e.getGuild().getIdLong(), e.getRole().getIdLong(), 1, e.getRole().getName(), "def");
		if(inserted > 0) {
			DiscordRoles.SQLgetRoles(e.getGuild().getIdLong());
			logger.debug("role id {} has been registered from guild {}", e.getRole().getName(), e.getGuild().getName());
		}
		else {
			logger.error("role id {} couldn't be registered for guild {} in table DiscordRoles.roles", e.getRole().getName(), e.getGuild().getName() );
		}
	}
}
