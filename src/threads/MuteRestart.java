package threads;

/**
 * Abbreviation of the RoleTimer class. Works the same way basing on 
 * removing the mute role after a determined time.
 */

import java.awt.Color;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import enums.Channel;
import enums.GoogleEvent;
import enums.Translation;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import google.GoogleUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import sql.Azrael;
import util.STATIC;

public class MuteRestart implements Runnable {
	private final static Logger logger = LoggerFactory.getLogger(MuteRestart.class);
	
	private Member member;
	private Guild guild;
	private String user_name;
	private Role mute_role;
	private long unmute;
	private long assignedRole;
	private boolean ranking_state;
	
	public MuteRestart(Member _member, Guild _guild, String _user_name, Role _mute_role, long _unmute, long _assignedRole, boolean _ranking_state) {
		this.member = _member;
		this.guild = _guild;
		this.user_name = _user_name;
		this.mute_role = _mute_role;
		this.unmute = _unmute;
		this.assignedRole = _assignedRole;
		this.ranking_state = _ranking_state;
	}

	@Override
	public void run() {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.GREEN).setThumbnail(IniFileReader.getUnmuteThumbnail()).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_UNMUTED));
		long user_id = member.getUser().getIdLong();
		String effectiveName = member.getEffectiveName();
		STATIC.addThread(Thread.currentThread(), "mute_gu"+guild.getId()+"us"+user_id);
		try {
			//put the thread to wait for a determined time
			Thread.sleep(unmute);
			
			//check if the user has been banned during the wait and if not, check if he's still muted. If muted, print message that the mute has been lifted
			if(!Azrael.SQLisBanned(user_id, guild.getIdLong())) {
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				if(Azrael.SQLgetMuted(user_id, guild.getIdLong())) {
					STATIC.writeToRemoteChannel(guild, message, STATIC.getTranslation2(guild, Translation.UNMUTE_MESSAGE).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id), Channel.LOG.getType());
				}
				//if the user is still present on the server, remove the mute role and assign back a ranking role, if available
				member = guild.getMemberById(user_id);
				if(member != null) {
					//verify that the user has the manage roles permission before removing the mute role
					if(guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
						guild.removeRoleFromMember(member, mute_role).queue();
						if(assignedRole != 0 && ranking_state == true){guild.addRoleToMember(member, guild.getRoleById(assignedRole)).queue();}
					}
					else {
						STATIC.writeToRemoteChannel(guild, message.setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(guild, Translation.UNMUTE_REMOVE_ERR)+Permission.MANAGE_ROLES, Channel.LOG.getType());
						logger.warn("MANAGE ROLES permission required to remove the mute role in guild {}", guild.getId());
					}
				}
				else {
					if(Azrael.SQLUpdateMutedOnEnd(user_id, guild.getIdLong(), false, false) > 0) {
						if(Azrael.SQLUpdateGuildLeft(user_id, guild.getIdLong(), false) == 0) {
							logger.error("Guild left state couldn't be update in Azrael.bancollect for user {} in guild {}", user_id, guild.getIdLong());
						}
						Azrael.SQLInsertActionLog("MEMBER_MUTE_REMOVE", user_id, guild.getIdLong(), "Mute role removed");
						//Run google service, if enabled
						if(GuildIni.getGoogleFunctionalitiesEnabled(guild.getIdLong()) && GuildIni.getGoogleSpreadsheetsEnabled(guild.getIdLong())) {
							final String NA = STATIC.getTranslation2(guild, Translation.NOT_AVAILABLE);
							GoogleUtils.handleSpreadsheetRequest(guild, ""+user_id, timestamp, user_name, effectiveName, "", "", NA, null, null, "UNMUTED", null, NA, NA, null, null, 0, null, 0, 0, GoogleEvent.UNMUTE.id);
						}
					}
					else
						logger.error("Mute end state couldn't be update in Azrael.bancollect for user {} in guild {}", user_id, guild.getIdLong());
				}
			}			
		} catch (InterruptedException e2) {
			//executed when H!user unmute was used to interrupt the Thread.sleep
			logger.info("The mute of {} ing guild {} has been interrupted!", user_id, guild.getId());
			//verify that the user is not banned and still labeled as muted before printing a message and before updating the unmute time
			if(!Azrael.SQLisBanned(user_id, guild.getIdLong())) {
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				Azrael.SQLUpdateUnmute(user_id, guild.getIdLong(), timestamp);
				STATIC.writeToRemoteChannel(guild, message, STATIC.getTranslation2(guild, Translation.UNMUTE_MESSAGE_2).replaceFirst("\\{\\}", user_name).replace("{}", ""+user_id), Channel.LOG.getType());
				//if the user is still present on the server, remove the mute role and assign back a ranking role, if available
				Role role = null;
				member = guild.getMemberById(user_id);
				if(member != null) {
					//verify that the user has the manage roles permission before removing the mute role
					if(guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
						if(assignedRole != 0) role = guild.getRoleById(assignedRole);
						guild.removeRoleFromMember(member, mute_role).queue();
						if(role != null && ranking_state == true) guild.addRoleToMember(member, guild.getRoleById(assignedRole)).queue();
					}
					else {
						STATIC.writeToRemoteChannel(guild, message.setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(guild, Translation.UNMUTE_REMOVE_ERR), Channel.LOG.getType());
						logger.warn("MANAGE ROLES permission required to remove the mute role in guild {}", guild.getId());
					}
				}
				Azrael.SQLInsertActionLog("MEMBER_MUTE_REMOVE", user_id, guild.getIdLong(), "Mute role removed");
				//Run google service, if enabled
				if(GuildIni.getGoogleFunctionalitiesEnabled(guild.getIdLong()) && GuildIni.getGoogleSpreadsheetsEnabled(guild.getIdLong())) {
					final String NA = STATIC.getTranslation2(guild, Translation.NOT_AVAILABLE);
					final var cache = Hashes.getTempCache("unmute_gu"+guild.getId()+"us"+user_id);
					String reporter_name = NA;
					String reporter_username = NA;
					String role_id = NA;
					String role_name = NA;
					if(cache != null) {
						final var reporter = guild.getMemberById(cache.getAdditionalInfo());
						if(reporter != null) {
							reporter_name = reporter.getUser().getName()+"#"+reporter.getUser().getDiscriminator();
							reporter_username = reporter.getEffectiveName();
						}
						Hashes.clearTempCache("unmute_gu"+guild.getId()+"us"+user_id);
					}
					if(role != null) {
						role_id = role.getId();
						role_name = role.getName();
					}
					GoogleUtils.handleSpreadsheetRequest(guild, ""+user_id, new Timestamp(System.currentTimeMillis()), user_name, effectiveName, reporter_name, reporter_username, NA, null, null, "UNMUTED", null, role_id, role_name, null, null, 0, null, 0, 0, GoogleEvent.UNMUTE.id);
				}
			}
		}
		STATIC.removeThread(Thread.currentThread());
	}
}
