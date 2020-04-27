package listeners;

/**
 * This class gets executed when a unban occurs.
 * 
 * The main task of this class is to retrieve the 
 * user who unbanned a different user and to display
 * it into a log channel. The unbanned user will be 
 * removed from the table where they're marked as 
 * banned.
 */

import java.awt.Color;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import enums.GoogleEvent;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import google.GoogleUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;
import sql.Azrael;

public class UnbanListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(UnbanListener.class);
	private final static EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE).setThumbnail(IniFileReader.getUnbanThumbnail()).setTitle("User unbanned!");
	
	@Override
	public void onGuildUnban(GuildUnbanEvent e) {
		new Thread(() -> {
			String trigger_user_name = "NaN";
			String reason = "No reason has been provided!";
			String append_message = "";
			//retrieve reason and applier if it has been cached, else retrieve the user from the audit log
			var cache = Hashes.getTempCache("unban_gu"+e.getGuild().getId()+"us"+e.getUser().getId());
			Member member = null;
			if(cache != null) {
				member = e.getGuild().getMemberById(cache.getAdditionalInfo());
				trigger_user_name = member.getAsMention();
				reason = cache.getAdditionalInfo2();
				//clear cache afterwards
				Hashes.clearTempCache("unban_gu"+e.getGuild().getId()+"us"+e.getUser().getId());
			}
			else {
				//check if the bot is able to view the audit logs, if not then set a default message at the end of the unban
				if(e.getGuild().getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS)) {
					AuditLogPaginationAction logs = e.getGuild().retrieveAuditLogs();
					for (AuditLogEntry entry : logs)
					{
						if(entry.getType() == ActionType.UNBAN && entry.getGuild().getIdLong() == e.getGuild().getIdLong() && entry.getTargetIdLong() == e.getUser().getIdLong()) {
							member = e.getGuild().getMemberById(entry.getUser().getId());
							trigger_user_name = entry.getUser().getAsMention();
						}
						break;
					}
				}
				else {
					append_message = "\n**The user who unbaned this user couldn't be retrieved because the VIEW AUDIT LOGS permission is not enabled!**";
					logger.warn("VIEW AUDIT LOGS permission is missing to display the user who unbanned a user in guild {}!", e.getGuild().getId());
				}
			}
			
			//retrieve log channel
			var log_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
			long user_id = e.getUser().getIdLong();
			long guild_id = e.getGuild().getIdLong();
			String user_name = e.getUser().getName()+"#"+e.getUser().getDiscriminator();
			
			//print unban message if a log channel has been registered
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			if(log_channel != null) {
				e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription("["+timestamp+"] **" + user_name + "** with the ID number **" + user_id + "** has been unbanned!\nUnbanned by: "+trigger_user_name+"\nReason: "+reason+append_message).build()).queue();
			}
			//remove the affected user from the bancollect table to symbolize that all current warnings have been removed
			if(Azrael.SQLDeleteData(user_id, guild_id) == 0) {
				logger.error("The user's ban of {} couldn't be cleared from Azrael.bancollect in guild {}", e.getUser().getId(), e.getGuild().getId());
				if(log_channel != null)e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occurred. The unban couldn't be cleared in table Azrael.bancollect").queue();
			}
			//log action
			logger.debug("{} has been unbanned from guild {}", user_id, e.getGuild().getName());
			Azrael.SQLInsertActionLog("MEMBER_BAN_REMOVE", user_id, guild_id, "User Unbanned");
			
			//Run google service, if enabled
			if(GuildIni.getGoogleFunctionalitiesEnabled(guild_id)) {
				GoogleUtils.handleSpreadsheetRequest(e.getGuild(), ""+user_id, timestamp, e.getUser().getName()+"#"+e.getUser().getDiscriminator(), e.getUser().getName(), member.getUser().getName()+"#"+member.getUser().getDiscriminator(), member.getEffectiveName(), reason, null, null, "UNBAN", null, null, null, GoogleEvent.UNBAN.id, log_channel);
			}
		}).start();
	}
}
