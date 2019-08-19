package listeners;

import java.awt.Color;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Bancollect;
import core.Hashes;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;
import sql.Azrael;

public class GuildLeaveListener extends ListenerAdapter{
	
	@Override
	public void onGuildMemberLeave(GuildMemberLeaveEvent e){
		Logger logger = LoggerFactory.getLogger(GuildLeaveListener.class);
		logger.debug("{} has left the guild {}", e.getUser().getId(), e.getGuild().getName());
		EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE).setThumbnail(IniFileReader.getLeaveThumbnail()).setTitle("User left!");
		EmbedBuilder kick = new EmbedBuilder().setColor(Color.ORANGE).setThumbnail(IniFileReader.getKickThumbnail()).setTitle("User kicked!");
		EmbedBuilder ban = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getKickThumbnail()).setTitle("User banned!");
		
		//String trigger_user_name = "";
		boolean kickOccurred = false;
		//String kick_reason = "";
		//String ban_reason = "";
		AuditLogPaginationAction logs = e.getGuild().retrieveAuditLogs();
		first_entry: for (AuditLogEntry entry : logs)
		{
			if(entry.getType().toString().equals("KICK") && entry.getGuild().getIdLong() == e.getGuild().getIdLong() && entry.getTargetIdLong() == e.getUser().getIdLong()) {
				kickOccurred = true;
				/*trigger_user_name = entry.getUser().getName()+"#"+entry.getUser().getDiscriminator();
				kick_reason = entry.getReason();
				kick_reason = kick_reason != null ? "\nReason: "+kick_reason : "";*/
			}
			/*else if(entry.getType().toString().equals("BAN") && entry.getGuild().getIdLong() == e.getGuild().getIdLong() && entry.getTargetIdLong() == e.getUser().getIdLong()) {
				trigger_user_name = "**"+entry.getUser().getName()+"#"+entry.getUser().getDiscriminator()+"**";
				ban_reason = entry.getReason();
				ban_reason = ban_reason != null ? "\nReason: "+ban_reason : "";
			}*/
			if(!entry.getType().toString().equals("MEMBER_ROLE_UPDATE")){
				break first_entry;
			}
		}
		
		long user_id = e.getUser().getIdLong();
		String user_name = e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator();
		long guild_id = e.getGuild().getIdLong();
		String guild_name = e.getGuild().getName();
		
		var log_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
		
		Bancollect warnedUser = Azrael.SQLgetData(e.getMember().getUser().getIdLong(), guild_id);
		int warning_id = warnedUser.getWarningID();
		
		int max_warning_id = Azrael.SQLgetMaxWarning(guild_id);
		
		
		if(log_channel != null) {
			var cache = Hashes.getTempCache("kick_gu"+e.getGuild().getId()+"us"+e.getUser().getId());
			var kick_issuer = (cache != null ? cache.getAdditionalInfo() : "NaN");
			var kick_reason = (cache != null ? cache.getAdditionalInfo2() : "No reason has been provided!");
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			if(warnedUser.getBanID() == 2){
				logger.debug("{} with the id number {} has been banned!", user_name, e.getMember().getUser().getId());
			}
			else if(warnedUser.getMuted() && warnedUser.getBanID() == 1) {
				e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+user_name+"** has left from "+guild_name+" while being muted!").build()).queue();
				Azrael.SQLUpdateGuildLeft(user_id, guild_id, true);
			}
			else if(kickOccurred && warnedUser.getBanID() == 1) {
				e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(kick.setDescription("["+timestamp.toString()+"] **"+user_name+"** with the id number **"+e.getUser().getId()+"** got kicked from **"+guild_name+"**!\n Kicked by: "+kick_issuer+"\nReason: "+kick_reason).build()).queue();
			}
			else if(GuildIni.getLeaveMessage(guild_id) && warnedUser.getBanID() == 1) {
				e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+user_name+"** has left from **"+guild_name+"**").build()).queue();
			}
			
			cache = Hashes.getTempCache("ban_gu"+e.getGuild().getId()+"us"+e.getUser().getId());
			var ban_issuer = (cache != null ? cache.getAdditionalInfo() : "NaN");
			var ban_reason = (cache != null ? cache.getAdditionalInfo2() : "No reason has been provided!");
			
			if(warning_id == 0 && warnedUser.getBanID() == 2) {
				e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(ban.setDescription("["+timestamp+"] **" + user_name + "** with the ID Number **" + user_id + "** has been banned without any protocolled warnings!\nBanned by: "+ban_issuer+"\nReason: "+ban_reason).build()).queue();
			}
			else if(warning_id < max_warning_id && warnedUser.getBanID() == 2) {
				e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(ban.setDescription("["+timestamp+"] **" + user_name + "** with the ID Number **" + user_id + "** has been banned without enough protocolled warnings! Warnings: "+warning_id+"\nBanned by: "+ban_issuer+"\nReason: "+ban_reason).build()).queue();
			}
			else if(warning_id == max_warning_id && warnedUser.getBanID() == 2) {
				e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(ban.setDescription("["+timestamp+"] **" + user_name + "** with the ID Number **" + user_id + "** has been banned!\nBanned by: "+ban_issuer+"\nReason: "+ban_reason).build()).queue();
			}
		}
		
		Hashes.clearTempCache("kick_gu"+e.getGuild().getId()+"us"+e.getUser().getId());
		Hashes.clearTempCache("ban_gu"+e.getGuild().getId()+"us"+e.getUser().getId());
		
		if(kickOccurred && warnedUser.getBanID() == 1) {
			Azrael.SQLInsertActionLog("MEMBER_KICK", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "User Kicked");
		}
	}
}
