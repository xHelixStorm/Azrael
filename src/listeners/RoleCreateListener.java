package listeners;

import java.awt.Color;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import enums.Channel;
import enums.Translation;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.DiscordRoles;
import util.STATIC;

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
			var inserted = DiscordRoles.SQLInsertRole(e.getGuild().getIdLong(), e.getRole().getIdLong(), 1, e.getRole().getName(), Channel.DEF.getType(), false);
			if(inserted > 0) {
				DiscordRoles.SQLgetRoles(e.getGuild().getIdLong());
				logger.info("Role {} has been saved in guild {}", e.getRole().getId(), e.getGuild().getId());
			}
			else {
				logger.error("Role {} couldn't be saved in guild {}", e.getRole().getId(), e.getGuild().getId());
			}
			
			STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_ROLE_CREATED)).addField(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_CREATED), e.getRole().getAsMention(), false).setFooter(e.getRole().getId()).setTimestamp(ZonedDateTime.now()), null, Channel.UPD.getType());
		}).start();
	}
}
