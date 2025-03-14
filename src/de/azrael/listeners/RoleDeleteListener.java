package de.azrael.listeners;

import java.awt.Color;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.enums.Channel;
import de.azrael.enums.Translation;
import de.azrael.sql.DiscordRoles;
import de.azrael.sql.RankingSystem;
import de.azrael.util.Hashes;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

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
				logger.info("role {} deleted in guild {}", e.getRole().getId(), e.getGuild().getId());
			}
			else {
				logger.error("role {} couldn't be deleted in guild {}", e.getRole().getId(), e.getGuild().getId() );
			}
			
			//check if a ranking role has been deleted
			if(RankingSystem.SQLgetRoles(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getRole_ID() == e.getRole().getIdLong()).findAny().orElse(null) != null) {
				Hashes.removeRankingRoles(e.getGuild().getIdLong());
				//set the current role of everyone to 0
				if(RankingSystem.SQLUpdateCurrentRole(e.getGuild().getIdLong(), 0) > 0) {
					//then delete role from table
					if(RankingSystem.SQLDeleteRole(e.getRole().getIdLong(), e.getGuild().getIdLong()) == 0) {
						EmbedBuilder message = new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_ERROR));
						STATIC.writeToRemoteChannel(e.getGuild(), message, STATIC.getTranslation2(e.getGuild(), Translation.DELETE_RANK_ROLE_ERR), Channel.LOG.getType());
						logger.error("Ranking role {} couldn't be deleted in guild {}", e.getRole().getId(), e.getGuild().getId());
					}
				}
				else {
					EmbedBuilder message = new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_ERROR));
					STATIC.writeToRemoteChannel(e.getGuild(), message, STATIC.getTranslation2(e.getGuild(), Translation.DELETE_RANK_ROLE_ERR), Channel.LOG.getType());
					logger.error("Unlocked ranking role {} couldn't be removed from users after role deletion in guild {}", e.getRole().getId(), e.getGuild().getId());
				}
			}
			
			STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_ROLE_REMOVED)).addField(STATIC.getTranslation2(e.getGuild(), Translation.UPDATE_REMOVED), e.getRole().getName(), false).setFooter(e.getRole().getId()).setTimestamp(ZonedDateTime.now()), null, Channel.UPD.getType());
		}).start();
	}
}
