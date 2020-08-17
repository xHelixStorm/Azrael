package listeners;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import enums.Channel;
import enums.Translation;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.DiscordRoles;
import sql.RankingSystem;
import util.STATIC;

/**
 * This class gets executed when a role gets deleted.
 * 
 * The deleted role will be removed from DiscordRoles.roles
 * and from RankingSystem.roles + current assigned ranking
 * role.
 * @author xHelixStorm
 * 
 */

public class RoleDeleteListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(RoleDeleteListener.class);
	
	@Override
	public void onRoleDelete(RoleDeleteEvent e) {
		new Thread(() -> {
			//When a role gets deleted from a guild, delete it from table
			var deleted = DiscordRoles.SQLDeleteRole(e.getRole().getIdLong(), e.getGuild().getIdLong());
			if(deleted > 0) {
				logger.debug("role id {} has been deleted from guild {}", e.getRole().getName(), e.getGuild().getName());
			}
			else {
				logger.error("role id {} couldn't be deleted for guild {} in table DiscordRoles.roles", e.getRole().getName(), e.getGuild().getName() );
			}
			
			//check if a ranking role has been deleted
			if(RankingSystem.SQLgetRoles(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getRole_ID() == e.getRole().getIdLong()).findAny().orElse(null) != null) {
				Hashes.removeRankingRoles(e.getGuild().getIdLong());
				//set the current role of everyone to 0
				if(RankingSystem.SQLUpdateCurrentRole(e.getGuild().getIdLong(), 0) > 0) {
					//then delete role from table
					if(RankingSystem.SQLremoveSingleRole(e.getRole().getIdLong(), e.getGuild().getIdLong()) == 0) {
						EmbedBuilder message = new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_ERROR));
						STATIC.writeToRemoteChannel(e.getGuild(), message, STATIC.getTranslation2(e.getGuild(), Translation.DELETE_RANK_ROLE_ERR), Channel.LOG.getType());
						logger.error("Role {} couldn't be removed from RankingSystem.roles table in guild {}", e.getRole().getId(), e.getGuild().getId());
					}
				}
				else {
					EmbedBuilder message = new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_ERROR));
					STATIC.writeToRemoteChannel(e.getGuild(), message, STATIC.getTranslation2(e.getGuild(), Translation.DELETE_RANK_ROLE_ERR), Channel.LOG.getType());
					logger.error("The role {} couldn't be set to 0 in RankingSystem.user_details upon role delete in guild {}", e.getRole().getId(), e.getGuild().getId());
				}
			}
		}).start();
	}
}
