package listeners;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.Azrael;
import sql.DiscordRoles;
import sql.RankingSystem;

public class RoleDeleteListener extends ListenerAdapter {

	@Override
	public void onRoleDelete(RoleDeleteEvent e) {
		Logger logger = LoggerFactory.getLogger(RoleDeleteListener.class);
		//When a role gets deleted from a guild, delete it from table
		var deleted = DiscordRoles.SQLDeleteRole(e.getRole().getIdLong(), e.getGuild().getIdLong());
		if(deleted > 0) {
			logger.debug("role id {} has been deleted from guild {}", e.getRole().getName(), e.getGuild().getName());
		}
		else {
			logger.error("role id {} couldn't be deleted for guild {} in table DiscordRoles.roles", e.getRole().getName(), e.getGuild().getName() );
		}
		
		//check if a ranking role has been deleted
		if(RankingSystem.SQLgetRoles(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getRoleID() == e.getRole().getIdLong()).findAny().orElse(null) != null) {
			Hashes.removeRankingRoles(e.getGuild().getIdLong());
			if(RankingSystem.SQLUpdateCurrentRole(e.getGuild().getIdLong(), 0) > 0) {
				if(RankingSystem.SQLremoveSingleRole(e.getRole().getIdLong(), e.getGuild().getIdLong()) == 0) {
					EmbedBuilder message = new EmbedBuilder();
					var log_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
					if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setColor(Color.RED).setTitle("Error on role delete!").setDescription("Role couldn't be removed from the RankingSystem database. Please contact an administrator!").build()).queue();
					logger.error("Role {} couldn't be removed from RankingSystem.roles table", e.getRole().getId());
				}
			}
			else {
				EmbedBuilder message = new EmbedBuilder();
				var log_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
				if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setColor(Color.RED).setTitle("Error on role delete!").setDescription("Role couldn't be removed from the RankingSystem database. Please contact an administrator!").build()).queue();
				logger.error("The role {} couldn't be set to 0 in RankingSystem.user_details upon role delete", e.getRole().getId());
			}
		}
	}
}
