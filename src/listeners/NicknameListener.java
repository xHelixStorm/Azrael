package listeners;

import net.dv8tion.jda.core.events.guild.member.GuildMemberNickChangeEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.Azrael;

public class NicknameListener extends ListenerAdapter{
	
	@Override
	public void onGuildMemberNickChange(GuildMemberNickChangeEvent e){
		long user_id = e.getMember().getUser().getIdLong();
		long guild_id = e.getGuild().getIdLong();
		String nickname = e.getNewNick();
		
		try {
			Azrael.SQLgetNickname(user_id, guild_id);
			String db_nickname = Azrael.getNickname();

			if(!db_nickname.isEmpty() && !nickname.isEmpty()){
				Azrael.SQLUpdateNickname(user_id, guild_id, nickname);
			}
		} catch (NullPointerException npe){
			try {
				if(!nickname.isEmpty()){
					Azrael.SQLInsertNickname(user_id, guild_id, nickname);
				}
			} catch (NullPointerException npe2){
				Azrael.SQLDeleteNickname(user_id, guild_id);
			}
		} finally {
			Azrael.clearAllVariables();
		}
		Azrael.SQLInsertActionLog("MEMBER_NICKNAME_UPDATE", user_id, guild_id, nickname);
	}
}
