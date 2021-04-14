package de.azrael.listeners;

import java.awt.Color;
import java.sql.Timestamp;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Bancollect;
import de.azrael.constructors.Channels;
import de.azrael.core.Hashes;
import de.azrael.enums.Channel;
import de.azrael.enums.GoogleEvent;
import de.azrael.enums.Translation;
import de.azrael.fileManagement.GuildIni;
import de.azrael.fileManagement.IniFileReader;
import de.azrael.google.GoogleSheets;
import de.azrael.sql.Azrael;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;

/**
 * This class gets launched when a user in the guild gets banned.
 * 
 * The banned user will be marked as banned on the database and will
 * print a message into the log channel, that a user has been banned.
 * @author xHelixStorm
 * 
 */

public class BanListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(BanListener.class);
	
	@Override
	public void onGuildBan(GuildBanEvent e) {
		long user_id = e.getUser().getIdLong();
		long guild_id = e.getGuild().getIdLong();
		
		var user = Azrael.SQLgetData(user_id, guild_id);
		var log_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.LOG.getType())).findAny().orElse(null);
		
		//either update user as banned or if not available, insert directly as banned
		if(user.getBanID() == 1 && user.getWarningID() > 0) {
			if(Azrael.SQLUpdateBan(user_id, guild_id, 2) > 0) {
				//terminate mute timer for the banned user if it's running
				STATIC.killThread("mute_gu"+e.getGuild().getId()+"us"+e.getUser().getId());
			}
			else {
				STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_ERROR)), STATIC.getTranslation2(e.getGuild(), Translation.BAN_ERR), Channel.LOG.getType());
				logger.error("The banned user {} couldn't be labeled as banned in guild {}", e.getUser().getId(), e.getGuild().getId());
			}
		}
		else{
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			if(Azrael.SQLInsertData(user_id, guild_id, 0, 2, timestamp, timestamp, true, false) > 0) {
				//terminate mute timer for the banned user if it's running
				STATIC.killThread("mute_gu"+e.getGuild().getId()+"us"+e.getUser().getId());
			}
			else {
				STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_ERROR)), STATIC.getTranslation2(e.getGuild(), Translation.BAN_ERR), Channel.LOG.getType());
				logger.error("The banned user {} couldn't be labeled as banned in guild {}", e.getUser().getId(), e.getGuild().getId());
			}
		}
		
		new Thread(() -> {
			//Unwatch the banned user, if he's being watched
			STATIC.handleUnwatch(e, null, (short)1);
			
			logger.info("User {} has been banned in guild {}", e.getUser().getId(), e.getGuild().getId());
			Azrael.SQLInsertActionLog("MEMBER_BAN_ADD", user_id, guild_id, "User Banned");
			
			if(log_channel != null) {
				final TextChannel textChannel = e.getGuild().getTextChannelById(log_channel.getChannel_ID());
				if(textChannel != null && (e.getGuild().getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS) || STATIC.setPermissions(e.getGuild(), textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS)))) {
					//retrieve reason and applier if it has been cached, else retrieve the user from the audit log
					var cache = Hashes.getTempCache("ban_gu"+e.getGuild().getId()+"us"+e.getUser().getId());
					if(cache != null) {
						//apply stored reason and ban applier to variable
						Member member = e.getGuild().getMemberById(cache.getAdditionalInfo());
						var ban_issuer = member.getAsMention();
						var ban_reason = cache.getAdditionalInfo2();
						
						EmbedBuilder ban = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getKickThumbnail()).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.BAN_TITLE));
						//retrieve max allowed warnings per guild and print a message depending on the applied warnings before ban
						int max_warning_id = Azrael.SQLgetMaxWarning(guild_id);
						if(user.getWarningID() == 0) {
							textChannel.sendMessage(ban.setDescription(STATIC.getTranslation2(e.getGuild(), Translation.BAN_MESSAGE_1).replaceFirst("\\{\\}", e.getUser().getName()+"#"+e.getUser().getDiscriminator()).replaceFirst("\\{\\}", ""+user_id).replace("{}", ban_issuer)+ban_reason).build()).queue();
						}
						else if(user.getWarningID() < max_warning_id) {
							textChannel.sendMessage(ban.setDescription(STATIC.getTranslation2(e.getGuild(), Translation.BAN_MESSAGE_2).replaceFirst("\\{\\}", e.getUser().getName()+"#"+e.getUser().getDiscriminator()).replaceFirst("\\{\\}", ""+user_id).replaceFirst("\\{\\}", ""+user.getWarningID()).replace("{}", ban_issuer)+ban_reason).build()).queue();
						}
						else if(user.getWarningID() == max_warning_id) {
							textChannel.sendMessage(ban.setDescription(STATIC.getTranslation2(e.getGuild(), Translation.BAN_MESSAGE_3).replaceFirst("\\{\\}", e.getUser().getName()+"#"+e.getUser().getDiscriminator()).replaceFirst("\\{\\}", ""+user_id).replace("{}", ban_issuer)+ban_reason).build()).queue();
						}
						//clear cache afterwards
						Hashes.clearTempCache("ban_gu"+e.getGuild().getId()+"us"+e.getUser().getId());
						
						//Run google service, if enabled
						if(GuildIni.getGoogleFunctionalitiesEnabled(guild_id) && GuildIni.getGoogleSpreadsheetsEnabled(guild_id)) {
							GoogleSheets.spreadsheetBanRequest(Azrael.SQLgetGoogleFilesAndEvent(guild_id, 2, GoogleEvent.BAN.id, ""), e.getGuild(), "", ""+user_id, new Timestamp(System.currentTimeMillis()), e.getUser().getName()+"#"+e.getUser().getDiscriminator(), e.getUser().getName(), member.getUser().getName()+"#"+member.getUser().getDiscriminator(), member.getEffectiveName(), ban_reason, ""+user.getWarningID());
						}
					}
					else {
						//check if the bot has permission to read the audit logs
						if(e.getGuild().getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS)) {
							getBanAuditLog(e, user_id, guild_id, log_channel, user);
						}
						else {
							EmbedBuilder ban = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getKickThumbnail()).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.BAN_TITLE));
							textChannel.sendMessage(ban.setDescription(STATIC.getTranslation2(e.getGuild(), Translation.BAN_MESSAGE_4).replaceFirst("\\{\\}", e.getUser().getName()+"#"+e.getUser().getDiscriminator()).replaceFirst("\\{\\}", ""+user_id).replaceFirst("\\{\\}", STATIC.getTranslation2(e.getGuild(), Translation.NOT_AVAILABLE)).replace("{}", STATIC.getTranslation2(e.getGuild(), Translation.DEFAULT_REASON))).build()).queue();
							logger.warn("VIEW AUDIT LOGS permission required in guild {}", e.getGuild().getId());
						}
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
					ban_reason = (entry.getReason() != null && entry.getReason().length() > 0 ? entry.getReason() : STATIC.getTranslation2(e.getGuild(), Translation.DEFAULT_REASON));
				
					EmbedBuilder ban = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getKickThumbnail()).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.BAN_TITLE));
					//retrieve max allowed warnings per guild and print a message depending on the applied warnings before ban
					int max_warning_id = Azrael.SQLgetMaxWarning(guild_id);
					if(user.getWarningID() == 0) {
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(ban.setDescription(STATIC.getTranslation2(e.getGuild(), Translation.BAN_MESSAGE_1).replaceFirst("\\{\\}", e.getUser().getName()+"#"+e.getUser().getDiscriminator()).replaceFirst("\\{\\}", ""+user_id).replace("{}", ban_issuer)+ban_reason).build()).queue();
					}
					else if(user.getWarningID() < max_warning_id) {
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(ban.setDescription(STATIC.getTranslation2(e.getGuild(), Translation.BAN_MESSAGE_2).replaceFirst("\\{\\}", e.getUser().getName()+"#"+e.getUser().getDiscriminator()).replaceFirst("\\{\\}", ""+user_id).replaceFirst("\\{\\}", ""+user.getWarningID()).replace("{}", ban_issuer)+ban_reason).build()).queue();
					}
					else if(user.getWarningID() == max_warning_id) {
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(ban.setDescription(STATIC.getTranslation2(e.getGuild(), Translation.BAN_MESSAGE_3).replaceFirst("\\{\\}", e.getUser().getName()+"#"+e.getUser().getDiscriminator()).replaceFirst("\\{\\}", ""+user_id).replace("{}", ban_issuer)+ban_reason).build()).queue();
					}
					
					//Run google service, if enabled
					if(GuildIni.getGoogleFunctionalitiesEnabled(guild_id) && GuildIni.getGoogleSpreadsheetsEnabled(guild_id)) {
						GoogleSheets.spreadsheetBanRequest(Azrael.SQLgetGoogleFilesAndEvent(guild_id, 2, GoogleEvent.BAN.id, ""), e.getGuild(), "", ""+user_id, new Timestamp(System.currentTimeMillis()), e.getUser().getName()+"#"+e.getUser().getDiscriminator(), e.getUser().getName(), entry.getUser().getName()+"#"+entry.getUser().getDiscriminator(), e.getGuild().getMemberById(entry.getUser().getIdLong()).getEffectiveName(), ban_reason, ""+user.getWarningID());
					}
				}
				else {
					try {
						//put thread to sleep and then retry
						Thread.sleep(100);
					} catch (InterruptedException e1) {
						logger.trace("Ban thread interrupted for user {} in guild {}", e.getUser().getId(), e.getGuild().getId(), e1);
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
					logger.trace("Ban thread interrupted for user {} in guild {}", e.getUser().getId(), e.getGuild().getId(), e1);
				}
				//retrieve the log recursively until the newly banned user has been found
				getBanAuditLog(e, user_id, guild_id, log_channel, user);
			}
		});
	}
}
