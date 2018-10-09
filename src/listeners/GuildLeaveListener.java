package listeners;

import java.awt.Color;
import java.sql.Timestamp;

import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.audit.AuditLogEntry;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.restaction.pagination.AuditLogPaginationAction;
import sql.SqlConnect;

public class GuildLeaveListener extends ListenerAdapter{
	
	@Override
	public void onGuildMemberLeave(GuildMemberLeaveEvent e){
		EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE).setThumbnail(IniFileReader.getLeaveThumbnail()).setTitle("User left!");
		EmbedBuilder kick = new EmbedBuilder().setColor(Color.ORANGE).setThumbnail(IniFileReader.getKickThumbnail()).setTitle("User kicked!");
		
		String trigger_user_name = "";
		String kick_reason = "";
		String ban_reason = "";
		boolean banned = false;
		AuditLogPaginationAction logs = e.getGuild().getAuditLogs();
		first_entry: for (AuditLogEntry entry : logs)
		{
			if(entry.getType().toString().equals("KICK") && entry.getGuild().getIdLong() == e.getGuild().getIdLong() && entry.getTargetIdLong() == e.getUser().getIdLong()) {
				trigger_user_name = entry.getUser().getName()+"#"+entry.getUser().getDiscriminator();
				kick_reason = entry.getReason();
				kick_reason = kick_reason != null ? "\nReason: "+kick_reason : "";
			}
			else if(entry.getType().toString().equals("BAN") && entry.getGuild().getIdLong() == e.getGuild().getIdLong() && entry.getTargetIdLong() == e.getUser().getIdLong()) {
				banned = true;
				if(entry.getType().toString().equals("BAN") && entry.getGuild().getIdLong() == e.getGuild().getIdLong() && entry.getTargetIdLong() == e.getUser().getIdLong()) {
					trigger_user_name = "**"+entry.getUser().getName()+"#"+entry.getUser().getDiscriminator()+"**";
					ban_reason = entry.getReason();
					ban_reason = ban_reason != null ? "\nReason: "+ban_reason : "";
				}
			}
			break first_entry;
		}
		
		long user_id = e.getUser().getIdLong();
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String user_name = e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator();
		long guild_id = e.getGuild().getIdLong();
		String guild_name = e.getGuild().getName();
		
		SqlConnect.SQLgetChannelID(guild_id, "log");
		long channel_id = SqlConnect.getChannelID();
		
		SqlConnect.SQLgetData(e.getMember().getUser().getIdLong(), guild_id);
		int warning_id = SqlConnect.getWarningID();
		
		SqlConnect.SQLgetMaxWarning(guild_id);
		int max_warning_id = SqlConnect.getWarningID();
		int ban_id = SqlConnect.getBanID();
		
		if(channel_id != 0){
			if(SqlConnect.getBanID() == 2 && SqlConnect.getMuted()){
				System.out.println("["+timestamp.toString()+"] "+user_name+" with the id number "+e.getMember().getUser().getId()+" has been banned!");
			}
			else if(SqlConnect.getMuted() && banned == false){
				e.getGuild().getTextChannelById(channel_id).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+user_name+"** has left from "+guild_name+" while being muted!").build()).queue();
			}
			else if(trigger_user_name.length() > 0 && banned == false) {
				e.getGuild().getTextChannelById(channel_id).sendMessage(kick.setDescription("["+timestamp.toString()+"] **"+trigger_user_name+"** kicked **"+user_name+"** with the id number **"+e.getUser().getId()+"** from **"+guild_name+"**"+kick_reason).build()).queue();
			}
			else if(IniFileReader.getLeaveMessage().equals("true") && banned == false){
				e.getGuild().getTextChannelById(channel_id).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+user_name+"** has left from **"+guild_name+"**").build()).queue();
			}
			else if(warning_id == 0 && banned == true){
				e.getGuild().getTextChannelById(channel_id).sendMessage(message.setDescription("["+timestamp+"] "+trigger_user_name+" has banned **" + user_name + "** with the ID Number **" + user_id + "** without any protocolled warnings!"+ban_reason).build()).queue();
			}
			else if((warning_id+1) < max_warning_id && banned == true){
				e.getGuild().getTextChannelById(channel_id).sendMessage(message.setDescription("["+timestamp+"] "+trigger_user_name+" has banned **" + user_name + "** with the ID Number **" + user_id + "** without enough protocolled warnings! Warnings: "+warning_id+""+ban_reason).build()).queue();
			}
		}
		
		if(ban_id == 1){
			SqlConnect.SQLUpdateBan(user_id, guild_id, 2);
		}
		else{
			SqlConnect.SQLInsertData(user_id, guild_id, max_warning_id, 2, timestamp, timestamp, true, false);
		}
		
		if(trigger_user_name.length() > 0 && banned == false) {
			SqlConnect.SQLInsertActionLog("MEMBER_KICK", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "User Kicked");
		}
		SqlConnect.clearAllVariables();
	}
}
