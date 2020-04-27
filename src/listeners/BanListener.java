package listeners;

/**
 * This class gets launched when a user in the guild gets banned.
 * 
 * The banned user will be marked as banned on the database and will
 * print a message into the log channel, that a user has been banned.
 */

import java.awt.Color;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Bancollect;
import constructors.Channels;
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
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;
import sql.Azrael;
import util.STATIC;

public class BanListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(BanListener.class);
	
	@Override
	public void onGuildBan(GuildBanEvent e) {
		long user_id = e.getUser().getIdLong();
		long guild_id = e.getGuild().getIdLong();
		
		var user = Azrael.SQLgetData(user_id, guild_id);
		var log_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
		
		//either update user as banned or if not available, insert directly as banned
		if(user.getBanID() == 1 && user.getWarningID() > 0) {
			if(Azrael.SQLUpdateBan(user_id, guild_id, 2) > 0) {
				//terminate mute timer for the banned user if it's running
				STATIC.killThread("mute_gu"+e.getGuild().getId()+"us"+e.getUser().getId());
			}
			else {
				logger.error("banned user {} couldn't be marked as banned on Azrael.bancollect for guild {}", e.getUser().getId(), e.getGuild().getName());
				if(log_channel != null)e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occurred. User couldn't be marked as banned in Azrael.bancollect").queue();
			}
		}
		else{
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			if(Azrael.SQLInsertData(user_id, guild_id, 0, 2, timestamp, timestamp, true, false) > 0) {
				//terminate mute timer for the banned user if it's running
				STATIC.killThread("mute_gu"+e.getGuild().getId()+"us"+e.getUser().getId());
			}
			else {
				logger.error("muted user {} couldn't be inserted into Azrael.bancollect for guild {}", e.getUser().getId(), e.getGuild().getName());
				if(log_channel != null)e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occurred. Muted user couldn't be inserted into Azrael.bancollect").queue();
			}
		}
		
		new Thread(() -> {
			//Unwatch the banned user, if he's being watched
			STATIC.handleUnwatch(e, null, (short)1);
			
			logger.debug("{} has been banned from {}", e.getUser().getId(), e.getGuild().getName());
			Azrael.SQLInsertActionLog("MEMBER_BAN_ADD", user_id, guild_id, "User Banned");
			
			if(log_channel != null) {
				//retrieve reason and applier if it has been cached, else retrieve the user from the audit log
				var cache = Hashes.getTempCache("ban_gu"+e.getGuild().getId()+"us"+e.getUser().getId());
				if(cache != null) {
					//apply stored reason and ban applier to variable
					Member member = e.getGuild().getMemberById(cache.getAdditionalInfo());
					var ban_issuer = member.getAsMention();
					var ban_reason = cache.getAdditionalInfo2();
					
					EmbedBuilder ban = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getKickThumbnail()).setTitle("User banned!");
					//retrieve max allowed warnings per guild and print a message depending on the applied warnings before ban
					int max_warning_id = Azrael.SQLgetMaxWarning(guild_id);
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					if(user.getWarningID() == 0) {
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(ban.setDescription("["+timestamp+"] **" + e.getUser().getName()+"#"+e.getUser().getDiscriminator() + "** with the ID Number **" + user_id + "** has been banned without any protocolled warnings!\nBanned by: "+ban_issuer+"\nReason: "+ban_reason).build()).queue();
					}
					else if(user.getWarningID() < max_warning_id) {
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(ban.setDescription("["+timestamp+"] **" + e.getUser().getName()+"#"+e.getUser().getDiscriminator() + "** with the ID Number **" + user_id + "** has been banned without enough protocolled warnings! Warnings: "+user.getWarningID()+"\nBanned by: "+ban_issuer+"\nReason: "+ban_reason).build()).queue();
					}
					else if(user.getWarningID() == max_warning_id) {
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(ban.setDescription("["+timestamp+"] **" + e.getUser().getName()+"#"+e.getUser().getDiscriminator() + "** with the ID Number **" + user_id + "** has been banned!\nBanned by: "+ban_issuer+"\nReason: "+ban_reason).build()).queue();
					}
					//clear cache afterwards
					Hashes.clearTempCache("ban_gu"+e.getGuild().getId()+"us"+e.getUser().getId());
					
					//Run google service, if enabled
					if(GuildIni.getGoogleFunctionalitiesEnabled(guild_id)) {
						GoogleUtils.handleSpreadsheetRequest(e.getGuild(), ""+user_id, timestamp, e.getUser().getName()+"#"+e.getUser().getDiscriminator(), e.getUser().getName(), member.getUser().getName()+"#"+member.getUser().getDiscriminator(), member.getEffectiveName(), ban_reason, null, ""+user.getWarningID(), "BAN", null, null, null, GoogleEvent.BAN.id, log_channel);
					}
				}
				else {
					//check if the bot has permission to read the audit logs
					if(e.getGuild().getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS)) {
						getBanAuditLog(e, user_id, guild_id, log_channel, user);
					}
					else {
						EmbedBuilder ban = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getKickThumbnail()).setTitle("User banned!");
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(ban.setDescription("["+new Timestamp(System.currentTimeMillis())+"] **" + e.getUser().getAsMention() + "** with the ID Number **" + user_id + "** has been banned!\nBanned by: NaN\nReason: No reason has been provided!\n"
								+ "**Audit log permission required to display more details**").build()).queue();
						logger.warn("VIEW AUDIT LOGS permission missing in guild {}!", e.getGuild().getId());
					}
				}
			}
		}).start();
	}
	
	private static void getBanAuditLog(GuildBanEvent e, long user_id, long guild_id, Channels log_channel, Bancollect user) {
		//retrieve latest audit log
		AuditLogPaginationAction banLog = e.getGuild().retrieveAuditLogs().cache(false);
		banLog.limit(1);
		banLog.queue((entries) -> {
			//verify that a ban occurred
			if(entries.get(0).getType() == ActionType.BAN) {
				var ban_issuer = "";
				var ban_reason = "";
				//verify that the audit log is about the same user that has been banned
				if(!entries.isEmpty() && entries.get(0).getTargetIdLong() == user_id) {
					AuditLogEntry entry = entries.get(0);
					//retrieve all details from the audit logs
					ban_issuer = entry.getUser().getAsMention();
					ban_reason = (entry.getReason() != null && entry.getReason().length() > 0 ? entry.getReason() : "No reason has been provided!");
				
					EmbedBuilder ban = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getKickThumbnail()).setTitle("User banned!");
					//retrieve max allowed warnings per guild and print a message depending on the applied warnings before ban
					int max_warning_id = Azrael.SQLgetMaxWarning(guild_id);
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					if(user.getWarningID() == 0) {
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(ban.setDescription("["+timestamp+"] **" + e.getUser().getName()+"#"+e.getUser().getDiscriminator() + "** with the ID Number **" + user_id + "** has been banned without any protocolled warnings!\nBanned by: "+ban_issuer+"\nReason: "+ban_reason).build()).queue();
					}
					else if(user.getWarningID() < max_warning_id) {
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(ban.setDescription("["+timestamp+"] **" + e.getUser().getName()+"#"+e.getUser().getDiscriminator() + "** with the ID Number **" + user_id + "** has been banned without enough protocolled warnings! Warnings: "+user.getWarningID()+"\nBanned by: "+ban_issuer+"\nReason: "+ban_reason).build()).queue();
					}
					else if(user.getWarningID() == max_warning_id) {
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(ban.setDescription("["+timestamp+"] **" + e.getUser().getName()+"#"+e.getUser().getDiscriminator() + "** with the ID Number **" + user_id + "** has been banned!\nBanned by: "+ban_issuer+"\nReason: "+ban_reason).build()).queue();
					}
					
					//Run google service, if enabled
					if(GuildIni.getGoogleFunctionalitiesEnabled(guild_id)) {
						GoogleUtils.handleSpreadsheetRequest(e.getGuild(), ""+user_id, timestamp, e.getUser().getName()+"#"+e.getUser().getDiscriminator(), e.getUser().getName(), entry.getUser().getName()+"#"+entry.getUser().getDiscriminator(), e.getGuild().getMemberById(entry.getIdLong()).getEffectiveName(), ban_reason, null, ""+user.getWarningID(), "BAN", null, null, null, GoogleEvent.BAN.id, log_channel);
					}
				}
				else {
					try {
						//put thread to sleep and then retry
						Thread.sleep(100);
					} catch (InterruptedException e1) {
						logger.warn("Ban thread interrupted", e1);
					}
					//retrieve the log recursively until the newly banned user has been found
					getBanAuditLog(e, user_id, guild_id, log_channel, user);
				}
			}
			else {
				try {
					//put thread to sleep and then retry
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					logger.warn("Ban thread interrupted", e1);
				}
				//retrieve the log recursively until the newly banned user has been found
				getBanAuditLog(e, user_id, guild_id, log_channel, user);
			}
		});
	}
}
