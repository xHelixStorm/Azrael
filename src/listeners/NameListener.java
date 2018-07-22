package listeners;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.core.exceptions.HierarchyException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.SqlConnect;
import util.BannedNames;

public class NameListener extends ListenerAdapter{
	
	@Override
	public void onUserUpdateName(UserUpdateNameEvent e){
		String oldname = e.getOldName()+"#"+e.getUser().getDiscriminator();
		String newname = e.getUser().getName()+"#"+e.getUser().getDiscriminator();
		long user_id = e.getUser().getIdLong();
		String nickname = BannedNames.selectRandomName();
		String nameCheck = newname.toLowerCase();
		
		SqlConnect.SQLUpdateUser(user_id, newname);
		
		for(String word : BannedNames.listOfBadnames()){
			if(nameCheck.contains(word)|| nameCheck.contains("["+word+"]")){
				for(Guild guild : e.getJDA().getGuilds()){
					long guild_id = guild.getIdLong();
					Member user = e.getJDA().getGuildById(guild_id).getMemberById(user_id);
					
					if(user.getUser().getIdLong() != 0){
						SqlConnect.SQLgetChannelID(guild_id, "log");
						long channel_id = SqlConnect.getChannelID();
						try {
							e.getJDA().getGuildById(guild_id).getController().setNickname(user, nickname).queue();
							e.getJDA().getTextChannelById(channel_id).sendMessage("The user **"+oldname+"** with the id number **"+user_id+"**, tried to change his name into **"+newname+"**. Hence, he received the following nickname: **"+nickname+"**").queue();
						} catch (HierarchyException hye){
							e.getJDA().getTextChannelById(channel_id).sendMessage("The user **"+oldname+"** with the id number **"+user_id+"**, tried to change his name into **"+newname+"** but had a higher role than myself. Hence, name won't be changed").queue();
						} finally {
							SqlConnect.clearAllVariables();
						}
					}
				}
			}
		}
		SqlConnect.SQLInsertActionLog("MEMBER_NAME_UPDATE", user_id, 0, newname);
	}
}
