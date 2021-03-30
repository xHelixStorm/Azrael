package de.azrael.listeners;

import java.sql.Timestamp;

/**
 * This class gets executed when the nickname of a user gets updated!
 * 
 * Depending on the nickname change, the nickname will either get deleted
 * from or inserted into the Azrael.nickname table. The Azrael.nickname
 * table is meant to keep the administrators updated regarding users,
 * that have a nickname.  
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.core.Hashes;
import de.azrael.enums.GoogleEvent;
import de.azrael.enums.Translation;
import de.azrael.fileManagement.GuildIni;
import de.azrael.google.GoogleUtils;
import de.azrael.sql.Azrael;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * This class will update the nickname table on name change
 * and eventually log to google spreadsheets, if available
 * @author xHelixStorm
 *
 */

public class NicknameListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(NicknameListener.class);
	
	@Override
	public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent e) {
		new Thread(() -> {
			long user_id = e.getMember().getUser().getIdLong();
			long guild_id = e.getGuild().getIdLong();
			//don't handle nicknames if it was handled somewhere else already
			if(Hashes.getTempCache("nickname_add_gu"+guild_id+"us"+user_id) == null) {
				String nickname = e.getNewNickname();
				//if the nickname has been removed, delete from table
				if(nickname == null) {
					final var result = Azrael.SQLDeleteNickname(user_id, guild_id);
					if(result > 0) {
						logger.info("User {} got the nickname {} removed in guild {}", e.getUser().getId(), e.getOldNickname(), e.getGuild().getId());
						Azrael.SQLInsertActionLog("MEMBER_NICKNAME_CLEAR", user_id, guild_id, "<cleared name>");
					}
					else if(result == -1) {
						logger.error("The nickname {} couldn't be deleted for user {} in guild {}", nickname, user_id, guild_id);
					}
				}
				//if the nickname has been set or changed, insert/update to table
				else {
					if(Azrael.SQLInsertNickname(user_id, guild_id, nickname) > 0) {
						logger.info("User {} received the nickname {} in guild {}", user_id, nickname, guild_id);
						Azrael.SQLInsertActionLog("MEMBER_NICKNAME_UPDATE", user_id, guild_id, nickname);
					}
					else {
						logger.error("The nickname {} couldn't be updated for user {} in guild {}", nickname, user_id, guild_id);
					}
				}
				//Run google service, if enabled
				if(GuildIni.getGoogleFunctionalitiesEnabled(guild_id) && GuildIni.getGoogleSpreadsheetsEnabled(guild_id)) {
					final String [] array = Azrael.SQLgetGoogleFilesAndEvent(e.getGuild().getIdLong(), 2, GoogleEvent.RENAME_MANUAL.id, "");
					if(array != null && !array[0].equals("empty")) {
						final String NA = STATIC.getTranslation2(e.getGuild(), Translation.NOT_AVAILABLE);
						String reporter_name = NA;
						String reporter_effectivename = NA;
						if(e.getGuild().getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS)) {
							var roleLog = e.getGuild().retrieveAuditLogs();
							//iterate through the log
							for(final var entry : roleLog) {
								//retrieve the first log about a role update
								if(entry.getType() == ActionType.MEMBER_UPDATE) {
									reporter_name = entry.getUser().getName()+"#"+entry.getUser().getDiscriminator();
									reporter_effectivename = e.getGuild().getMemberById(entry.getUser().getIdLong()).getEffectiveName();
									break;
								}
							}
						}
						else {
							logger.warn("VIEW_AUDIT_LOGS permission required to run the google event RENAMED in guild {}", guild_id);
						}
						GoogleUtils.handleSpreadsheetRequest(array, e.getGuild(), "", ""+user_id, new Timestamp(System.currentTimeMillis()), e.getUser().getName()+"#"+e.getUser().getDiscriminator(), null, reporter_name, reporter_effectivename, null, null, null, "RENAMED", null, null, null, e.getOldValue(), e.getNewValue(), 0, null, null, 0, 0, 0, GoogleEvent.RENAME_MANUAL.id);
					}
				}
			}
			else
				Hashes.clearTempCache("nickname_add_gu"+guild_id+"us"+user_id);
		}).start();
	}
}
