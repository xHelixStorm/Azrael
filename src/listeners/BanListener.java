package listeners;

import java.awt.Color;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;
import sql.Azrael;

public class BanListener extends ListenerAdapter{
	
	@Override
	public void onGuildBan(GuildBanEvent e){
		Logger logger = LoggerFactory.getLogger(BanListener.class);
		
		long user_id = e.getUser().getIdLong();
		long guild_id = e.getGuild().getIdLong();
		
		var user = Azrael.SQLgetData(user_id, guild_id);
		var log_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
		
		if(user.getBanID() == 1 && user.getWarningID() > 0) {
			if(Azrael.SQLUpdateBan(user_id, guild_id, 2) == 0) {
				logger.error("banned user {} couldn't be marked as banned on Azrael.bancollect for guild {}", e.getUser().getId(), e.getGuild().getName());
				if(log_channel != null)e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occurred. User couldn't be marked as banned in Azrael.bancollect").queue();
			}
		}
		else{
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			if(Azrael.SQLInsertData(user_id, guild_id, 0, 2, timestamp, timestamp, true, false) == 0) {
				logger.error("muted user {} couldn't be inserted into Azrael.bancollect for guild {}", e.getUser().getId(), e.getGuild().getName());
				if(log_channel != null)e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occurred. Muted user couldn't be inserted into Azrael.bancollect").queue();
			}
		}
		logger.debug("{} has been banned from {}", e.getUser().getId(), e.getGuild().getName());
		Azrael.SQLInsertActionLog("MEMBER_BAN_ADD", user_id, guild_id, "User Banned");
		
		if(log_channel != null) {
			AuditLogPaginationAction banLog = e.getGuild().retrieveAuditLogs().cache(false);
			banLog.limit(1);
			banLog.queue((entries) -> {
				if(entries.get(0).getType() == ActionType.BAN) {
					var cache = Hashes.getTempCache("ban_gu"+e.getGuild().getId()+"us"+e.getUser().getId());
					var ban_issuer = "";
					var ban_reason = "";
					if(!entries.isEmpty() && entries.get(0).getTargetIdLong() == user_id) {
						AuditLogEntry entry = entries.get(0);
						ban_issuer = (cache != null ? cache.getAdditionalInfo() : entry.getUser().getAsMention());
						ban_reason = (cache != null ? cache.getAdditionalInfo2() : (entry.getReason() != null && entry.getReason().length() > 0 ? entry.getReason() : "No reason has been provided!"));
					}
					else {
						ban_issuer = (cache != null ? cache.getAdditionalInfo() : "NaN");
						ban_reason = (cache != null ? cache.getAdditionalInfo2() : "No reason has been provided!");
					}
					
					EmbedBuilder ban = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getKickThumbnail()).setTitle("User banned!");
					int max_warning_id = Azrael.SQLgetMaxWarning(guild_id);
					if(user.getWarningID() == 0) {
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(ban.setDescription("["+new Timestamp(System.currentTimeMillis())+"] **" + e.getUser().getAsMention() + "** with the ID Number **" + user_id + "** has been banned without any protocolled warnings!\nBanned by: "+ban_issuer+"\nReason: "+ban_reason).build()).queue();
					}
					else if(user.getWarningID() < max_warning_id) {
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(ban.setDescription("["+new Timestamp(System.currentTimeMillis())+"] **" + e.getUser().getAsMention() + "** with the ID Number **" + user_id + "** has been banned without enough protocolled warnings! Warnings: "+user.getWarningID()+"\nBanned by: "+ban_issuer+"\nReason: "+ban_reason).build()).queue();
					}
					else if(user.getWarningID() == max_warning_id) {
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(ban.setDescription("["+new Timestamp(System.currentTimeMillis())+"] **" + e.getUser().getAsMention() + "** with the ID Number **" + user_id + "** has been banned!\nBanned by: "+ban_issuer+"\nReason: "+ban_reason).build()).queue();
					}
					Hashes.clearTempCache("ban_gu"+e.getGuild().getId()+"us"+e.getUser().getId());
				}
				else {
					EmbedBuilder ban = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getKickThumbnail()).setTitle("User banned!");
					e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(ban.setDescription("["+new Timestamp(System.currentTimeMillis())+"] **" + e.getUser().getAsMention() + "** with the ID Number **" + user_id + "** has been banned!\nBanned by: NaN\nReason: No reason has been provided!").build()).queue();
				}
			});
		}
	}
}
