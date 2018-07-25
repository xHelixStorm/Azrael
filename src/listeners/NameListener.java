package listeners;

import java.awt.Color;

import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.core.exceptions.HierarchyException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.SqlConnect;

public class NameListener extends ListenerAdapter{
	
	@Override
	public void onUserUpdateName(UserUpdateNameEvent e){
		EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE);
		if(IniFileReader.getNameFilter().equals("true")) {
			String oldname = e.getOldName()+"#"+e.getUser().getDiscriminator();
			String newname = e.getUser().getName()+"#"+e.getUser().getDiscriminator();
			long user_id = e.getUser().getIdLong();
			SqlConnect.SQLgetRandomName();
			String nickname = SqlConnect.getName();
			String nameCheck = newname.toLowerCase();
			
			SqlConnect.SQLUpdateUser(user_id, newname);
			SqlConnect.SQLgetNameFilter();
			for(String word : SqlConnect.getNames()){
				if(nameCheck.contains(word)|| nameCheck.contains("["+word+"]")){
					for(Guild guild : e.getJDA().getGuilds()){
						long guild_id = guild.getIdLong();
						Member user = e.getJDA().getGuildById(guild_id).getMemberById(user_id);
						
						if(user.getUser().getIdLong() != 0){
							SqlConnect.SQLgetChannelID(guild_id, "log");
							long channel_id = SqlConnect.getChannelID();
							try {
								e.getJDA().getGuildById(guild_id).getController().setNickname(user, nickname).queue();
								message.setThumbnail(IniFileReader.getCatchedThumbnail()).setTitle("Not allowed name change found!");
								e.getJDA().getTextChannelById(channel_id).sendMessage(message.setDescription("The user **"+oldname+"** with the id number **"+user_id+"**, tried to change his name into **"+newname+"**. Hence, he received the following nickname: **"+nickname+"**").build()).queue();
							} catch (HierarchyException hye){
								message.setThumbnail(IniFileReader.getFalseAlarmThumbnail()).setTitle("You know that you shouldn't do it :/");
								e.getJDA().getTextChannelById(channel_id).sendMessage("The user **"+oldname+"** with the id number **"+user_id+"**, tried to change his name into **"+newname+"** but had a higher role than myself. Hence, name won't be changed").queue();
							}
						}
					}
				}
			}
			SqlConnect.clearNames();
			SqlConnect.clearAllVariables();
		}
		SqlConnect.SQLInsertActionLog("MEMBER_NAME_UPDATE", e.getUser().getIdLong(), 0, e.getUser().getName()+"#"+e.getUser().getDiscriminator());
	}
}
