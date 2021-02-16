package listeners;

import java.awt.Color;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import enums.Channel;
import enums.Translation;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.role.update.RoleUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.DiscordRoles;
import util.STATIC;

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
			logger.error("Role name {} of role {} couldn't be updated in guild {}", e.getNewName(), e.getRole().getId(), e.getGuild().getId());
		}
		
		STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_ROLE_RENAMED)).addField(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_RENAMED), e.getOldName()+" > "+e.getNewName(), false).setFooter(e.getRole().getId()).setTimestamp(ZonedDateTime.now()), null, Channel.UPD.getType());
	}
}
