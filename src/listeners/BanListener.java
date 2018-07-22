package listeners;

import java.awt.Color;
import java.sql.Timestamp;

import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.audit.AuditLogEntry;
import net.dv8tion.jda.core.events.guild.GuildBanEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.restaction.pagination.AuditLogPaginationAction;
import sql.ServerRoles;
import sql.SqlConnect;

public class BanListener extends ListenerAdapter{
	private static EmbedBuilder message = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getBanThumbnail()).setTitle("User banned!");
	
	@Override
	public void onGuildBan(GuildBanEvent e){
		
		String trigger_user_name = "";
		AuditLogPaginationAction logs = e.getGuild().getAuditLogs();
		first_entry: for (AuditLogEntry entry : logs)
		{
			ServerRoles.SQLgetRole(e.getGuild().getIdLong(), "mut");
			if(entry.getType().toString().equals("MEMBER_BAN_ADD") && entry.getGuild().getIdLong() == e.getGuild().getIdLong() && entry.getTargetIdLong() == e.getUser().getIdLong()) {
				trigger_user_name = entry.getUser().getName()+"#"+entry.getUser().getDiscriminator();
			}
			break first_entry;
		}
		
		long user_id = e.getUser().getIdLong();
		String user_name = e.getUser().getName()+"#"+e.getUser().getDiscriminator();
		long guild_id = e.getGuild().getIdLong();
		
		SqlConnect.SQLgetChannelID(guild_id, "log");
		long channel_id = SqlConnect.getChannelID();
		
		SqlConnect.SQLgetData(user_id, guild_id);
		int warning_id = SqlConnect.getWarningID();
		int ban_id = SqlConnect.getBanID();
		
		SqlConnect.SQLgetMaxWarning(guild_id);
		int max_warning_id = SqlConnect.getWarningID();
		
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		if(channel_id != 0){
			if(warning_id == 0){
				e.getGuild().getTextChannelById(channel_id).sendMessage(message.setDescription("**["+timestamp+"] **"+trigger_user_name+"** has banned " + user_name + " with the ID Number " + user_id + " without any protocolled warnings!**").build()).queue();
			}
			else if((warning_id+1) < max_warning_id){
				e.getGuild().getTextChannelById(channel_id).sendMessage(message.setDescription("**["+timestamp+"] **"+trigger_user_name+"** has banned " + user_name + " with the ID Number " + user_id + " without enough protocolled warnings! Warnings: "+warning_id+"**").build()).queue();
			}
		}
		
		if(ban_id == 1){
			SqlConnect.SQLUpdateBan(user_id, guild_id, 2);
		}
		else{
			SqlConnect.SQLInsertData(user_id, guild_id, 4, 2, timestamp, timestamp, true);
		}
		SqlConnect.SQLUpdateMuted(user_id, guild_id, true);
		SqlConnect.SQLInsertActionLog("MEMBER_BAN_ADD", user_id, "the user "+user_name+" has been banned from "+e.getGuild().getName());
		SqlConnect.clearAllVariables();
	}
}
