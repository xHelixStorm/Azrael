package listeners;

import java.awt.Color;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.audit.AuditLogEntry;
import net.dv8tion.jda.core.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.restaction.pagination.AuditLogPaginationAction;
import sql.DiscordRoles;
import sql.Azrael;

public class UnbanListener extends ListenerAdapter{
	
	@Override
	public void onGuildUnban(GuildUnbanEvent e){
		EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE).setThumbnail(IniFileReader.getUnbanThumbnail()).setTitle("User unbanned!");
		
		String trigger_user_name = "";
		AuditLogPaginationAction logs = e.getGuild().getAuditLogs();
		first_entry: for (AuditLogEntry entry : logs)
		{
			DiscordRoles.SQLgetRole(e.getGuild().getIdLong(), "mut");
			if(entry.getType().toString().equals("UNBAN") && entry.getGuild().getIdLong() == e.getGuild().getIdLong() && entry.getTargetIdLong() == e.getUser().getIdLong()) {
				trigger_user_name = entry.getUser().getName()+"#"+entry.getUser().getDiscriminator();
			}
			break first_entry;
		}
		
		long guild = e.getGuild().getIdLong();
		Azrael.SQLgetChannelID(guild, "log");
		long channel_id = Azrael.getChannelID();
		long user_id = e.getUser().getIdLong();
		long guild_id = e.getGuild().getIdLong();
		String user_name = e.getUser().getName()+"#"+e.getUser().getDiscriminator();
		
		if(channel_id != 0){
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			e.getJDA().getGuildById(e.getGuild().getIdLong()).getTextChannelById(channel_id).sendMessage(message.setDescription("["+timestamp+"] **"+trigger_user_name+"** has unbanned **" + user_name + "** with the ID number **" + user_id + "**!").build()).queue();}
		Azrael.SQLDeleteData(user_id, guild_id);
		Logger logger = LoggerFactory.getLogger(UnbanListener.class);
		logger.debug("{} has been unbanned from guild {}", user_id, e.getGuild().getName());
		Azrael.SQLInsertActionLog("MEMBER_BAN_REMOVE", user_id, guild_id, "User Unbanned");
		Azrael.clearAllVariables();
	}
}
