package listeners;

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

import core.Hashes;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.Azrael;

public class NicknameListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(NameListener.class);
	
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
					if(Azrael.SQLDeleteNickname(user_id, guild_id) > 0) {
						logger.debug("{} got the nickname {} removed in guild {}", e.getUser().getId(), e.getOldNickname(), e.getGuild().getId());
						Azrael.SQLInsertActionLog("MEMBER_NICKNAME_CLEAR", user_id, guild_id, "<cleared name>");
					}
					else {
						logger.error("The nickname {} for user {} in guild {} couldn't be deleted from Azrael.nickname", nickname, user_id, guild_id);
					}
				}
				//if the nickname has been set or changed, insert/update to table
				else {
					if(nickname != null && Azrael.SQLInsertNickname(user_id, guild_id, nickname) > 0) {
						logger.debug("{} received the nickname {} in guild {}", user_id, nickname, guild_id);
						Azrael.SQLInsertActionLog("MEMBER_NICKNAME_UPDATE", user_id, guild_id, nickname);
					}
					else {
						logger.error("The nickname {} for user {} in guild {} couldn't be updated on the table Azrael.nickname", nickname, user_id, guild_id);
					}
				}
			}
			else
				Hashes.clearTempCache("nickname_add_gu"+guild_id+"us"+user_id);
		}).start();
	}
}
