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
		
		if(nickname == null) {
			if(Azrael.SQLDeleteNickname(user_id, guild_id) == 0) {
				logger.error("Nickname from {} couldn't be deleted from Azrael.nickname", e.getUser().getId());
			}
			else {
				logger.debug("{} received the nickname {} in guild {}", e.getUser().getId(), nickname, e.getGuild().getName());
				Azrael.SQLInsertActionLog("MEMBER_NICKNAME_CLEAR", user_id, guild_id, "<cleared name>");
			}
		}
	}
}
