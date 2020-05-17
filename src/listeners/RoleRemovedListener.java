package listeners;

import java.awt.Color;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Bancollect;
import core.Hashes;
import core.UserPrivs;
import enums.GoogleEvent;
import enums.Translation;
import fileManagement.GuildIni;
import google.GoogleUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.audit.AuditLogKey;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;
import sql.DiscordRoles;
import sql.RankingSystem;
import util.STATIC;
import sql.Azrael;

/**
 * This class gets executed when a role is removed from a user.
 * 
 * The Bot checks if a mute role has been manually removed or
 * or the timer finished. Depending on the situation, table 
 * operations will occur and the fitting message will be printed
 * for the moderators and administrators.
 * @author xHelixStorm
 * 
 */

public class RoleRemovedListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(RoleRemovedListener.class);
	
	@Override
	public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent e) {
		new Thread(() -> {
			String member_name = e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator();
			long user_id = e.getMember().getUser().getIdLong();
			long guild_id = e.getGuild().getIdLong();
			
			//retrieve the warning details of the current user
			Bancollect warnedUser = Azrael.SQLgetData(user_id, guild_id);
			
			//check that the user is not muted anymore either for normal or permanent mutes
			if(!UserPrivs.isUserMuted(e.getGuild().getMember(e.getUser())) && (warnedUser.getUnmute() == null || (warnedUser.getUnmute().getTime() - System.currentTimeMillis()) > 0)  && warnedUser.getMuted()) {
				//check that the bot is allowed to view the audit logs
				final String NA = STATIC.getTranslation2(e.getGuild(), Translation.NOT_AVAILABLE);
				String trigger_user_name = NA;
				String trigger_effective_name = NA;
				final boolean view_audit_log = e.getGuild().getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS);
				if(view_audit_log) {
					//retrieve first audit log
					AuditLogPaginationAction logs = e.getGuild().retrieveAuditLogs();
					for (AuditLogEntry entry : logs)
					{
						//get the user that has removed a role and be sure that the mute role has been removed and that the audit log is affecting the same user as the event
						if(entry.getChangeByKey(AuditLogKey.MEMBER_ROLES_REMOVE) != null) {
							var mute_role = DiscordRoles.SQLgetRoles(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getCategory_ABV().equals("mut")).findAny().orElse(null);
							if(entry.getChangeByKey(AuditLogKey.MEMBER_ROLES_REMOVE).toString().contains(""+(mute_role != null ? mute_role.getRole_ID() : 0)) && entry.getGuild().getIdLong() == e.getGuild().getIdLong() && entry.getTargetIdLong() == e.getMember().getUser().getIdLong()) {
								trigger_user_name = entry.getUser().getName()+"#"+entry.getUser().getDiscriminator();
								trigger_effective_name = e.getGuild().getMember(entry.getUser()).getEffectiveName();
							}
						}
						break;
					}
				}
				else {
					logger.warn("Audit Log permissions missing for guild {}", e.getGuild().getId());
				}
				//retrieve the log channel
				var log_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
				//update the mute status if any warning details have been found
				if(warnedUser.getUserID() != 0) {
					if(Azrael.SQLUpdateMuted(user_id, guild_id, false) == 0) {
						logger.error("Mute information of {} couldn't be updated in Azrael.bancollect in guild {}", user_id, e.getGuild().getId());
						if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation2(e.getGuild(), Translation.ROLE_FLAG_REMOVE)).build()).queue();
					}
				}
				//check if the user had a ranking role assigned before
				Role role = null;
				var assignedRole = RankingSystem.SQLgetAssignedRole(user_id, guild_id);
				if(assignedRole != 0) {
					role = e.getGuild().getRoleById(assignedRole);
					if(role != null) {
						//check if the bot has the manage roles permission
						if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
							//assign back the achieved ranking role
							e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(assignedRole)).queue();
						}
						else {
							if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation2(e.getGuild(), Translation.ROLE_REASSIGN_ERR)+Permission.MANAGE_ROLES.getName()).build()).queue();
							logger.warn("The MANAGE ROLES permission is missing to assign back the ranking role in guild {}", e.getGuild().getId());
						}
					}
				}
				//print message for removing the mute role manually
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.ROLE_MANUAL_REMOVE_TITLE));
				if(log_channel != null) {e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription(STATIC.getTranslation2(e.getGuild(), Translation.ROLE_MANUALLY_REMOVED).replaceFirst("\\{\\}", trigger_user_name).replaceFirst("\\{\\}", member_name).replace("{}", ""+user_id)
					+ (!view_audit_log ? STATIC.getTranslation2(e.getGuild(), Translation.ROLE_MANUALLY_REMOVE_PERM)+Permission.VIEW_AUDIT_LOGS.getName() : "")).build()).queue();}
				logger.debug("{} got the mute role removed before the time expired in guild {}", e.getUser().getId(), e.getGuild().getId());
				Azrael.SQLInsertActionLog("MEMBER_MUTE_REMOVE_HALFWAY", user_id, guild_id, "Mute role removed manually");
				//Run google service, if enabled
				if(GuildIni.getGoogleFunctionalitiesEnabled(guild_id) && GuildIni.getGoogleSpreadsheetsEnabled(guild_id)) {
					String role_id = NA;
					String role_name = NA;
					if(role != null) {
						role_id = role.getId();
						role_name = role.getName();
					}
					GoogleUtils.handleSpreadsheetRequest(e.getGuild(), ""+user_id, timestamp, member_name, e.getMember().getEffectiveName(), trigger_user_name, trigger_effective_name, STATIC.getTranslation2(e.getGuild(), Translation.ROLE_REMOVE_MANUALLY_REASON), null, null, "MANUALLY_UNMUTED", null, role_id, role_name, null, null, GoogleEvent.UNMUTE_MANUAL.id, log_channel);
				}
			}
			//execute this block if the mute time has ended and the user doesn't have the mute role anymore
			else if(!UserPrivs.isUserMuted(e.getGuild().getMember(e.getUser())) && warnedUser.getUserID() != 0 && warnedUser.getMuted()) {
				//retrieve the log channel
				var log_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
				//update mute state
				if(Azrael.SQLUpdateMutedOnEnd(user_id, guild_id, false, false) == 0) {
					if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation2(e.getGuild(), Translation.ROLE_FLAG_REMOVE)).build()).queue();
					logger.error("Mute information of {} couldn't be updated in Azrael.bancollect in guild {}", user_id, e.getGuild().getId());
				}
				else {
					logger.debug("{} has been unmuted in guild {}", e.getUser().getId(), e.getGuild().getId());
					Azrael.SQLInsertActionLog("MEMBER_MUTE_REMOVE", user_id, guild_id, "Mute role removed");
				}
				//Run google service, if enabled
				if(GuildIni.getGoogleFunctionalitiesEnabled(guild_id) && GuildIni.getGoogleSpreadsheetsEnabled(guild_id)) {
					final String NA = STATIC.getTranslation2(e.getGuild(), Translation.NOT_AVAILABLE);
					String role_id = NA;
					String role_name = NA;
					final var cache = Hashes.getTempCache("unmute_gu"+guild_id+"us"+user_id);
					if(cache != null) {
						//retrieve the assigned role, if any were assigned
						if(!cache.getAdditionalInfo2().isBlank()) {
							final var role = e.getGuild().getRoleById(cache.getAdditionalInfo2());
							if(role != null) {
								role_id = role.getId();
								role_name = role.getName();
							}
						}
						Hashes.clearTempCache("unmute_gu"+guild_id+"us"+user_id);
					}
					GoogleUtils.handleSpreadsheetRequest(e.getGuild(), ""+user_id, new Timestamp(System.currentTimeMillis()), member_name, e.getMember().getEffectiveName(), "", "", STATIC.getTranslation2(e.getGuild(), Translation.ROLE_REMOVE_ELAPSED_REASON), null, null, "UNMUTED", null, role_id, role_name, null, null, GoogleEvent.UNMUTE.id, log_channel);
				}
			}
		}).start();
	}
}
