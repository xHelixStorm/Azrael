package listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.core.events.guild.member.GuildMemberNickChangeEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.Azrael;

public class NicknameListener extends ListenerAdapter{
	
	@Override
	public void onGuildMemberNickChange(GuildMemberNickChangeEvent e){
		Logger logger = LoggerFactory.getLogger(NameListener.class);
		long user_id = e.getMember().getUser().getIdLong();
		long guild_id = e.getGuild().getIdLong();
		String nickname = e.getNewNick();
		
		if(Azrael.SQLgetNickname(user_id, guild_id).length() > 0 && nickname != null){
			if(Azrael.SQLUpdateNickname(user_id, guild_id, nickname) == 0) {
				logger.error("User nickname of {} couldn't be updated in Azrael.nickname", user_id);
			}
		}
		else if(nickname != null) {
			if(Azrael.SQLInsertNickname(user_id, guild_id, nickname) == 0) {
				logger.error("User nickname of {} couldn't be inserted into Azrael.nickname", user_id);
			}
		}
		else {
			if(Azrael.SQLDeleteNickname(user_id, guild_id) == 0) {
				logger.error("Nickname from {} couldn't be deleted from Azrael.nickname", e.getUser().getId());
			}
		}
		
		logger.debug("{} received the nickname {} in guild {}", e.getUser().getId(), nickname, e.getGuild().getName());
		Azrael.SQLInsertActionLog((nickname != null ? "MEMBER_NICKNAME_UPDATE" : "MEMBER_NICKNAME_CLEAR"), user_id, guild_id, (nickname != null ? nickname : "<cleared name>"));
	}
}
