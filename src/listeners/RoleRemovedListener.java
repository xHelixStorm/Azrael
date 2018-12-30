package listeners;

import java.awt.Color;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.UserPrivs;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.audit.AuditLogEntry;
import net.dv8tion.jda.core.audit.AuditLogKey;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.restaction.pagination.AuditLogPaginationAction;
import sql.DiscordRoles;
import sql.Azrael;

public class RoleRemovedListener extends ListenerAdapter{
	
	@Override
	public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent e){
		EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE).setTitle("Mute role has been manually removed!");
		
		String trigger_user_name = "";
		AuditLogPaginationAction logs = e.getGuild().getAuditLogs();
		first_entry: for (AuditLogEntry entry : logs)
		{
			DiscordRoles.SQLgetRole(e.getGuild().getIdLong(), "mut");
			if(entry.getChangeByKey(AuditLogKey.MEMBER_ROLES_REMOVE) != null){
				if(entry.getChangeByKey(AuditLogKey.MEMBER_ROLES_REMOVE).toString().contains(""+DiscordRoles.getRole_ID()) && entry.getGuild().getIdLong() == e.getGuild().getIdLong() && entry.getTargetIdLong() == e.getMember().getUser().getIdLong()) {
					trigger_user_name = entry.getUser().getName()+"#"+entry.getUser().getDiscriminator();
				}
			}
			break first_entry;
		}
		
		String member_name = e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator();
		long user_id = e.getMember().getUser().getIdLong();
		long guild_id = e.getGuild().getIdLong();
		long log_channel_id;
		
		Azrael.SQLgetData(user_id, guild_id);
		
		try{
			if(!UserPrivs.isUserMuted(e.getUser(), guild_id) &&(Azrael.getUnmute().getTime() - System.currentTimeMillis()) > 0){
				if(Azrael.getUser_id() != 0){Azrael.SQLUpdateMuted(user_id, guild_id, false, false);}
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				
				Azrael.SQLgetChannelID(guild_id, "log");
				log_channel_id = Azrael.getChannelID();
				Azrael.clearUnmute();
				
				if(log_channel_id != 0){e.getGuild().getTextChannelById(log_channel_id).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+trigger_user_name+"** has manually removed the mute role from **"+member_name+"** with the ID number **"+user_id+"**!").build()).queue();}
				Logger logger = LoggerFactory.getLogger(RoleRemovedListener.class);
				logger.debug("{} got the mute role removed before the time expired in guild {}", e.getUser().getId(), e.getGuild().getName());
				Azrael.SQLInsertActionLog("MEMBER_MUTE_REMOVE_HALFWAY", user_id, guild_id, "Mute role removed manually");
			}
			else if(!UserPrivs.isUserMuted(e.getUser(), guild_id) && Azrael.getUser_id() != 0){
				Azrael.SQLUpdateMuted(user_id, guild_id, false, false);
				Logger logger = LoggerFactory.getLogger(RoleRemovedListener.class);
				logger.debug("{} has been unmuted in guild {}", e.getUser().getId(), e.getGuild().getName());
				Azrael.SQLInsertActionLog("MEMBER_MUTE_REMOVE", user_id, guild_id, "Mute role removed");
			}
		} catch(NullPointerException npe){
			//do nothing
		}
		Azrael.clearAllVariables();
	}
}
