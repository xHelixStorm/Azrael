package listeners;

import java.awt.Color;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;
import sql.Azrael;

public class UnbanListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(UnbanListener.class);
	private final static EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE).setThumbnail(IniFileReader.getUnbanThumbnail()).setTitle("User unbanned!");
	
	@Override
	public void onGuildUnban(GuildUnbanEvent e) {
		String trigger_user_name = "NaN";
		AuditLogPaginationAction logs = e.getGuild().retrieveAuditLogs();
		for (AuditLogEntry entry : logs)
		{
			if(entry.getType() == ActionType.UNBAN && entry.getGuild().getIdLong() == e.getGuild().getIdLong() && entry.getTargetIdLong() == e.getUser().getIdLong()) {
				trigger_user_name = entry.getUser().getAsMention();
			}
			break;
		}
		
		var log_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
		long user_id = e.getUser().getIdLong();
		long guild_id = e.getGuild().getIdLong();
		String user_name = e.getUser().getName()+"#"+e.getUser().getDiscriminator();
		
		if(log_channel != null){
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			e.getJDA().getGuildById(e.getGuild().getIdLong()).getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription("["+timestamp+"] **" + user_name + "** with the ID number **" + user_id + "** has been unbanned!\nUnbanned by: "+trigger_user_name).build()).queue();}
		if(Azrael.SQLDeleteData(user_id, guild_id) == 0) {
			logger.error("The user's ban of {} couldn't be cleared from Azrael.bancollect in guild {}", e.getUser().getId(), e.getGuild().getId());
			if(log_channel != null)e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occurred. The unban couldn't be cleared in table Azrael.bancollect").queue();
		}
		logger.debug("{} has been unbanned from guild {}", user_id, e.getGuild().getName());
		Azrael.SQLInsertActionLog("MEMBER_BAN_REMOVE", user_id, guild_id, "User Unbanned");
	}
}
