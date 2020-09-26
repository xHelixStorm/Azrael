package listeners;

import java.awt.Color;
import java.sql.Timestamp;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Bancollect;
import core.Hashes;
import enums.Channel;
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
					STATIC.writeToRemoteChannel(e.getGuild(), kick, STATIC.getTranslation2(e.getGuild(), Translation.KICK_MESSAGE).replaceFirst("\\{\\}", user_name).replaceFirst("\\{\\}", e.getUser().getId()).replace("{}", kick_issuer)+kick_reason, Channel.LOG.getType());
					Azrael.SQLInsertActionLog("MEMBER_KICK", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "User Kicked");
					Hashes.clearTempCache("kick_gu"+e.getGuild().getId()+"us"+e.getUser().getId());
					logger.debug("{} has been kicked from guild {}", e.getUser().getId(), e.getGuild().getId());
					
					//Run google service, if enabled
					if(GuildIni.getGoogleFunctionalitiesEnabled(guild_id) && GuildIni.getGoogleSpreadsheetsEnabled(guild_id)) {
						GoogleUtils.handleSpreadsheetRequest(e.getGuild(), "", ""+user_id, new Timestamp(System.currentTimeMillis()), user_name, e.getMember().getEffectiveName(), member.getUser().getName()+"#"+member.getUser().getDiscriminator(), member.getEffectiveName(), kick_reason, null, null, "KICK", null, null, null, null, null, 0, null, null, 0, 0, GoogleEvent.KICK.id);
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
							STATIC.writeToRemoteChannel(e.getGuild(), kick, STATIC.getTranslation2(e.getGuild(), Translation.KICK_MESSAGE).replaceFirst("\\{\\}", user_name).replaceFirst("\\{\\}", e.getUser().getId()).replace("{}", kick_issuer)+kick_reason, Channel.LOG.getType());
							Azrael.SQLInsertActionLog("MEMBER_KICK", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "User Kicked");
							logger.debug("{} has been kicked from guild {}", e.getUser().getId(), e.getGuild().getId());
							
							//Run google service, if enabled
							if(GuildIni.getGoogleFunctionalitiesEnabled(guild_id) && GuildIni.getGoogleSpreadsheetsEnabled(guild_id)) {
								GoogleUtils.handleSpreadsheetRequest(e.getGuild(), "", ""+user_id, new Timestamp(System.currentTimeMillis()), user_name, e.getMember().getEffectiveName(), entry.getUser().getName()+"#"+entry.getUser().getDiscriminator(), e.getGuild().getMemberById(entry.getUser().getIdLong()).getEffectiveName(), kick_reason, null, null, "KICK", null, null, null, null, null, 0, null, null, 0, 0, GoogleEvent.KICK.id);
							}
							
							//Unwatch the kicked user, if he's being watched
							STATIC.handleUnwatch(null, e, (short)2);
						}
						
						//print message if a user has left the guild while being muted
						else if(warnedUser.getMuted() && warnedUser.getBanID() == 1) {
							EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE).setThumbnail(e.getMember().getUser().getEffectiveAvatarUrl()).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.LEFT_TITLE));
							STATIC.writeToRemoteChannel(e.getGuild(), message, STATIC.getTranslation2(e.getGuild(), Translation.LEFT_MESSAGE_1).replace("{}", user_name), Channel.LOG.getType());
							Azrael.SQLUpdateGuildLeft(user_id, guild_id, true);
						}
						//if leave messages are enabled, print a message that the user has left the server
						else if(GuildIni.getLeaveMessage(guild_id) && warnedUser.getBanID() == 1) {
							EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE).setThumbnail(e.getMember().getUser().getEffectiveAvatarUrl()).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.LEFT_TITLE));
							STATIC.writeToRemoteChannel(e.getGuild(), message, STATIC.getTranslation2(e.getGuild(), Translation.LEFT_MESSAGE_2).replace("{}", user_name), Channel.LOG.getType());
						}
					});
				}
				else {
					//print message if a user has left the guild while being muted
					if(warnedUser.getMuted() && warnedUser.getBanID() == 1) {
						EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE).setThumbnail(e.getMember().getUser().getEffectiveAvatarUrl()).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.LEFT_TITLE));
						STATIC.writeToRemoteChannel(e.getGuild(), message, STATIC.getTranslation2(e.getGuild(), Translation.LEFT_MESSAGE_1).replace("{}", user_name), Channel.LOG.getType());
						Azrael.SQLUpdateGuildLeft(user_id, guild_id, true);
					}
					//if leave messages are enabled, print a message that the user has left the server
					else if(GuildIni.getLeaveMessage(guild_id) && warnedUser.getBanID() == 1) {
						EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE).setThumbnail(e.getMember().getUser().getEffectiveAvatarUrl()).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.LEFT_TITLE));
						STATIC.writeToRemoteChannel(e.getGuild(), message, STATIC.getTranslation2(e.getGuild(), Translation.LEFT_MESSAGE_2).replace("{}", user_name), Channel.LOG.getType());
					}
				}
			}
			else {
				Hashes.clearTempCache("kick-ignore_gu"+e.getGuild().getId()+"us"+e.getUser().getId());
				Azrael.SQLInsertActionLog("MEMBER_KICK", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "User Kicked");
				logger.debug("{} has been kicked from guild {}", e.getUser().getId(), e.getGuild().getId());
			}
			
			//check if a waiting room was set up for this user and if yes, remove the channel
			final var categories = Azrael.SQLgetCategories(e.getGuild().getIdLong());
			if(categories != null) {
				final var verification = categories.parallelStream().filter(f -> f.getType().equals("ver")).findAny().orElse(null);
				if(verification != null) {
					final var textChannel = e.getGuild().getTextChannels().parallelStream().filter(f -> f.getName().equals(e.getMember().getUser().getId())).findAny().orElse(null);
					if(textChannel != null) {
						if(e.getGuild().getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MANAGE_CHANNEL) || STATIC.setPermissions(e.getGuild(), textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MANAGE_CHANNEL))) {
							textChannel.delete().queue();
						}
						else {
							STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MANAGE_CHANNEL.getName())+textChannel.getAsMention(), Channel.LOG.getType());
							logger.error("MANAGE_CHANNEL permission required to remove the text channel {} in guild {}", textChannel.getId(), e.getGuild().getId());
						}
					}
				}
			}
		}).start();
	}
}
