package listeners;

import java.awt.Color;
import java.sql.Timestamp;

import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.audit.AuditLogEntry;
import net.dv8tion.jda.core.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.restaction.pagination.AuditLogPaginationAction;
import sql.ServerRoles;
import sql.SqlConnect;

public class UnbanListener extends ListenerAdapter{
	
	@Override
	public void onGuildUnban(GuildUnbanEvent e){
		EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE).setThumbnail(IniFileReader.getUnbanThumbnail()).setTitle("User unbanned!");
		
		String trigger_user_name = "";
		AuditLogPaginationAction logs = e.getGuild().getAuditLogs();
		first_entry: for (AuditLogEntry entry : logs)
		{
			ServerRoles.SQLgetRole(e.getGuild().getIdLong(), "mut");
			if(entry.getType().toString().equals("MEMBER_BAN_REMOVE") && entry.getGuild().getIdLong() == e.getGuild().getIdLong() && entry.getTargetIdLong() == e.getUser().getIdLong()) {
				trigger_user_name = entry.getUser().getName()+"#"+entry.getUser().getDiscriminator();
			}
			break first_entry;
		}
		
		long guild = e.getGuild().getIdLong();
		SqlConnect.SQLgetChannelID(guild, "log");
		long channel_id = SqlConnect.getChannelID();
		long user_id = e.getUser().getIdLong();
		long guild_id = e.getGuild().getIdLong();
		String user_name = e.getUser().getName()+"#"+e.getUser().getDiscriminator();
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		
		if(channel_id != 0){e.getJDA().getGuildById(e.getGuild().getIdLong()).getTextChannelById(channel_id).sendMessage(message.setDescription("**["+timestamp+"] **"+trigger_user_name+"** has unbanned **" + user_name + "** with the ID number **" + user_id + "**!**").build()).queue();}
		SqlConnect.SQLDeleteData(user_id, guild_id);
		SqlConnect.clearAllVariables();
	}
}
