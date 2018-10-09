package listeners;

import java.awt.Color;
import java.sql.Timestamp;

import core.UserPrivs;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.audit.AuditLogEntry;
import net.dv8tion.jda.core.audit.AuditLogKey;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.restaction.pagination.AuditLogPaginationAction;
import sql.ServerRoles;
import sql.SqlConnect;

public class RoleRemovedListener extends ListenerAdapter{
	
	@Override
	public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent e){
		EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE).setTitle("Mute role has been manually removed!");
		
		String trigger_user_name = "";
		AuditLogPaginationAction logs = e.getGuild().getAuditLogs();
		first_entry: for (AuditLogEntry entry : logs)
		{
			ServerRoles.SQLgetRole(e.getGuild().getIdLong(), "mut");
			if(entry.getChangeByKey(AuditLogKey.MEMBER_ROLES_REMOVE) != null){
				if(entry.getChangeByKey(AuditLogKey.MEMBER_ROLES_REMOVE).toString().contains(""+ServerRoles.getRole_ID()) && entry.getGuild().getIdLong() == e.getGuild().getIdLong() && entry.getTargetIdLong() == e.getMember().getUser().getIdLong()) {
					trigger_user_name = entry.getUser().getName()+"#"+entry.getUser().getDiscriminator();
				}
			}
			break first_entry;
		}
		
		String member_name = e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator();
		long user_id = e.getMember().getUser().getIdLong();
		long guild_id = e.getGuild().getIdLong();
		long log_channel_id;
		
		SqlConnect.SQLgetData(user_id, guild_id);
		
		try{
			if(!UserPrivs.isUserMuted(e.getUser(), guild_id) &&(SqlConnect.getUnmute().getTime() - System.currentTimeMillis()) > 0){
				if(SqlConnect.getUser_id() != 0){SqlConnect.SQLUpdateMuted(user_id, guild_id, false, false);}
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				
				SqlConnect.SQLgetChannelID(guild_id, "log");
				log_channel_id = SqlConnect.getChannelID();
				SqlConnect.clearUnmute();
				
				if(log_channel_id != 0){e.getGuild().getTextChannelById(log_channel_id).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+trigger_user_name+"** has manually removed the mute role from **"+member_name+"** with the ID number **"+user_id+"**!").build()).queue();}
				SqlConnect.SQLInsertActionLog("MEMBER_MUTE_REMOVE_HALFWAY", user_id, guild_id, "Mute role removed manually");
			}
			else if(!UserPrivs.isUserMuted(e.getUser(), guild_id) && SqlConnect.getUser_id() != 0){
				SqlConnect.SQLUpdateMuted(user_id, guild_id, false, false);
				SqlConnect.SQLInsertActionLog("MEMBER_MUTE_REMOVE", user_id, guild_id, "Mute role removed");
			}
		} catch(NullPointerException npe){
			//do nothing
		}
		SqlConnect.clearAllVariables();
	}
}
