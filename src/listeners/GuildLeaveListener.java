package listeners;

import java.awt.Color;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Bancollect;
import core.Hashes;
import enums.GoogleEvent;
import enums.Translation;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import google.GoogleUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;
import sql.Azrael;
import util.STATIC;

/**
 * This class gets executed when a user leaves the server
 * 
 * With this class, there are 3 different server leave 
 * messages. A kick message, a leave while muted message
 * and a regular leave message. The one to use, will be
 * identified below.
 * @author xHelixStorm
 * 
 */

public class GuildLeaveListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(GuildLeaveListener.class);
	
	@Override
	public void onGuildMemberLeave(GuildMemberLeaveEvent e) {
		new Thread(() -> {
			//retrieve the log channel before anything is being done
			var log_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
			if(log_channel != null) {
				if(Hashes.getTempCache("kick-ignore_gu"+e.getGuild().getId()+"us"+e.getUser().getId()) == null) {
					final long user_id = e.getUser().getIdLong();
					final String user_name = e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator();
					final long guild_id = e.getGuild().getIdLong();
					
					//retrieve warn and ban information of the current user
					Bancollect warnedUser = Azrael.SQLgetData(user_id, guild_id);
					//check if the user has been kicked with the user command
					var cache = Hashes.getTempCache("kick_gu"+e.getGuild().getId()+"us"+e.getUser().getId());
					if(cache != null) {
						//retrieve the user who kicked and the reason is available from cache
						Member member = e.getGuild().getMemberById(cache.getAdditionalInfo());
						var kick_issuer = member.getAsMention();
						var kick_reason = cache.getAdditionalInfo2();
						EmbedBuilder kick = new EmbedBuilder().setColor(Color.ORANGE).setThumbnail(IniFileReader.getKickThumbnail()).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.KICK_TITLE));
						e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(kick.setDescription(STATIC.getTranslation2(e.getGuild(), Translation.KICK_MESSAGE).replaceFirst("\\{\\}", user_name).replaceFirst("\\{\\}", e.getUser().getId()).replace("{}", kick_issuer)+kick_reason).build()).queue();
						Azrael.SQLInsertActionLog("MEMBER_KICK", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "User Kicked");
						Hashes.clearTempCache("kick_gu"+e.getGuild().getId()+"us"+e.getUser().getId());
						logger.debug("{} has been kicked from guild {}", e.getUser().getId(), e.getGuild().getId());
						
						//Run google service, if enabled
						if(GuildIni.getGoogleFunctionalitiesEnabled(guild_id) && GuildIni.getGoogleSpreadsheetsEnabled(guild_id)) {
							GoogleUtils.handleSpreadsheetRequest(e.getGuild(), ""+user_id, new Timestamp(System.currentTimeMillis()), user_name, e.getMember().getEffectiveName(), member.getUser().getName()+"#"+member.getUser().getDiscriminator(), member.getEffectiveName(), kick_reason, null, null, "KICK", null, null, null, null, null, 0, null, 0, 0, GoogleEvent.KICK.id, log_channel);
						}
						
						//Unwatch the kicked user, if he's being watched
						STATIC.handleUnwatch(null, e, (short)2);
					}
					//check if the bot has permission to view the audit logs
					else if(e.getGuild().getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS)) {
						//retrieve the newest audit log
						AuditLogPaginationAction kickLog = e.getGuild().retrieveAuditLogs().cache(false);
						kickLog.limit(1);
						kickLog.queue((entries) -> {
							//verify that the newest log is about a kick and that it's about the same user that has left the server
							if(!entries.isEmpty() && entries.get(0).getType() == ActionType.KICK && entries.get(0).getTargetIdLong() == user_id && !Hashes.containsActionlog(entries.get(0).getId())) {
								AuditLogEntry entry = entries.get(0);
								//set the current action log as read
								Hashes.addActionlog(entry.getId());
								//retrieve the user who kicked and the reason is available from the audit log entry
								var kick_issuer = entry.getUser().getAsMention();
								var kick_reason = (entry.getReason() != null && entry.getReason().length() > 0 ? entry.getReason() : STATIC.getTranslation2(e.getGuild(), Translation.DEFAULT_REASON));
								EmbedBuilder kick = new EmbedBuilder().setColor(Color.ORANGE).setThumbnail(IniFileReader.getKickThumbnail()).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.KICK_TITLE));
								e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(kick.setDescription(STATIC.getTranslation2(e.getGuild(), Translation.KICK_MESSAGE).replaceFirst("\\{\\}", user_name).replaceFirst("\\{\\}", e.getUser().getId()).replace("{}", kick_issuer)+kick_reason).build()).queue();
								Azrael.SQLInsertActionLog("MEMBER_KICK", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "User Kicked");
								logger.debug("{} has been kicked from guild {}", e.getUser().getId(), e.getGuild().getId());
								
								//Run google service, if enabled
								if(GuildIni.getGoogleFunctionalitiesEnabled(guild_id) && GuildIni.getGoogleSpreadsheetsEnabled(guild_id)) {
									GoogleUtils.handleSpreadsheetRequest(e.getGuild(), ""+user_id, new Timestamp(System.currentTimeMillis()), user_name, e.getMember().getEffectiveName(), entry.getUser().getName()+"#"+entry.getUser().getDiscriminator(), e.getGuild().getMemberById(entry.getUser().getIdLong()).getEffectiveName(), kick_reason, null, null, "KICK", null, null, null, null, null, 0, null, 0, 0, GoogleEvent.KICK.id, log_channel);
								}
								
								//Unwatch the kicked user, if he's being watched
								STATIC.handleUnwatch(null, e, (short)2);
							}
							
							//print message if a user has left the guild while being muted
							else if(warnedUser.getMuted() && warnedUser.getBanID() == 1) {
								EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE).setThumbnail(e.getMember().getUser().getEffectiveAvatarUrl()).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.LEFT_TITLE));
								e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription(STATIC.getTranslation2(e.getGuild(), Translation.LEFT_MESSAGE_1).replace("{}", user_name)).build()).queue();
								Azrael.SQLUpdateGuildLeft(user_id, guild_id, true);
							}
							//if leave messages are enabled, print a message that the user has left the server
							else if(GuildIni.getLeaveMessage(guild_id) && warnedUser.getBanID() == 1) {
								EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE).setThumbnail(e.getMember().getUser().getEffectiveAvatarUrl()).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.LEFT_TITLE));
								e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription(STATIC.getTranslation2(e.getGuild(), Translation.LEFT_MESSAGE_2).replace("{}", user_name)).build()).queue();
							}
						});
					}
					else {
						//print message if a user has left the guild while being muted
						if(warnedUser.getMuted() && warnedUser.getBanID() == 1) {
							EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE).setThumbnail(e.getMember().getUser().getEffectiveAvatarUrl()).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.LEFT_TITLE));
							e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription(STATIC.getTranslation2(e.getGuild(), Translation.LEFT_MESSAGE_1).replace("{}", user_name)).build()).queue();
							Azrael.SQLUpdateGuildLeft(user_id, guild_id, true);
						}
						//if leave messages are enabled, print a message that the user has left the server
						else if(GuildIni.getLeaveMessage(guild_id) && warnedUser.getBanID() == 1) {
							EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE).setThumbnail(e.getMember().getUser().getEffectiveAvatarUrl()).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.LEFT_TITLE));
							e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription(STATIC.getTranslation2(e.getGuild(), Translation.LEFT_MESSAGE_2).replace("{}", user_name)).build()).queue();
						}
					}
				}
				else {
					Hashes.clearTempCache("kick-ignore_gu"+e.getGuild().getId()+"us"+e.getUser().getId());
					Azrael.SQLInsertActionLog("MEMBER_KICK", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "User Kicked");
					logger.debug("{} has been kicked from guild {}", e.getUser().getId(), e.getGuild().getId());
				}
			}
		}).start();
	}
}
