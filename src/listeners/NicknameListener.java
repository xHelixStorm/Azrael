package listeners;

import net.dv8tion.jda.core.events.guild.member.GuildMemberNickChangeEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.SqlConnect;

public class NicknameListener extends ListenerAdapter{
	
	@Override
	public void onGuildMemberNickChange(GuildMemberNickChangeEvent e){
		long user_id = e.getMember().getUser().getIdLong();
		long guild_id = e.getGuild().getIdLong();
		String nickname = e.getNewNick();
		
		try {
			SqlConnect.SQLgetNickname(user_id, guild_id);
			String db_nickname = SqlConnect.getNickname();

			if(!db_nickname.isEmpty() && !nickname.isEmpty()){
				SqlConnect.SQLUpdateNickname(user_id, guild_id, nickname);
			}
		} catch (NullPointerException npe){
			try {
				if(!nickname.isEmpty()){
					SqlConnect.SQLInsertNickname(user_id, guild_id, nickname);
				}
			} catch (NullPointerException npe2){
				SqlConnect.SQLDeleteNickname(user_id, guild_id);
			}
		} finally {
			SqlConnect.clearAllVariables();
			SqlConnect.SQLInsertActionLog("MEMBER_NICKNAME_UPDATE", user_id, guild_id, nickname);
		}
	}
}
