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
		AuditLogPaginationAction logs = e.getGuild().getAuditLogs();
		first_entry: for (AuditLogEntry entry : logs)
		{
			if(entry.getType().toString().equals("KICK") && entry.getGuild().getIdLong() == e.getGuild().getIdLong() && entry.getTargetIdLong() == e.getUser().getIdLong()) {
				trigger_user_name = entry.getUser().getName()+"#"+entry.getUser().getDiscriminator();
			}
			break first_entry;
		}
		
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String user_name = e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator();
		long guild_id = e.getGuild().getIdLong();
		String guild_name = e.getGuild().getName();
		
		SqlConnect.SQLgetChannelID(guild_id, "log");
		long channel_id = SqlConnect.getChannelID();
		
		SqlConnect.SQLgetData(e.getMember().getUser().getIdLong(), guild_id);
		
		if(channel_id != 0){
			if(SqlConnect.getBanID() == 2 && SqlConnect.getMuted()){
				System.out.println("["+timestamp.toString()+"] "+user_name+" with the id number "+e.getMember().getUser().getId()+" has been banned!");
			}
			else if(SqlConnect.getMuted()){
				e.getGuild().getTextChannelById(channel_id).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+user_name+"** has left from "+guild_name+" while being muted!").build()).queue();
			}
			else if(trigger_user_name.length() > 0) {
				e.getGuild().getTextChannelById(channel_id).sendMessage(kick.setDescription("["+timestamp.toString()+"] **"+trigger_user_name+"** kicked **"+user_name+"** from **"+guild_name+"**").build()).queue();
			}
			else if(IniFileReader.getLeaveMessage().equals("true")){
				e.getGuild().getTextChannelById(channel_id).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+user_name+"** has left from **"+guild_name+"**").build()).queue();
			}
		}
		SqlConnect.clearAllVariables();
	}
}
