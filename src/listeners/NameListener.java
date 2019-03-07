package listeners;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.core.exceptions.HierarchyException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.Azrael;

public class NameListener extends ListenerAdapter{
	
	@Override
	public void onUserUpdateName(UserUpdateNameEvent e){
		EmbedBuilder message = new EmbedBuilder();
		Logger logger = LoggerFactory.getLogger(NameListener.class);
		String oldname = e.getOldName()+"#"+e.getUser().getDiscriminator();
		String newname = e.getUser().getName()+"#"+e.getUser().getDiscriminator();
		long user_id = e.getUser().getIdLong();
		String nameCheck = newname.toLowerCase();
		boolean staff_name = false;
		
		if(Azrael.SQLUpdateUser(user_id, newname) == 0) {
			logger.error("Azrael.users couldn't be updated with a name update from {}",user_id);
		}
		for(Guild g : e.getJDA().getGuilds()){
			check: for(String name : Azrael.SQLgetStaffNames(g.getIdLong())){
				if(nameCheck.matches(name+"#[0-9]{4}")){
					Member member = e.getJDA().getGuildById(g.getIdLong()).getMemberById(user_id);
					long channel_id = Azrael.SQLgetChannelID(g.getIdLong(), "log");
					try {
						String nickname = Azrael.SQLgetRandomName(g.getIdLong());
						e.getJDA().getGuildById(g.getIdLong()).getController().setNickname(member, nickname).queue();
						message.setColor(Color.RED).setThumbnail(e.getUser().getEffectiveAvatarUrl()).setTitle("Impersonation attempt found!");
						e.getJDA().getTextChannelById(channel_id).sendMessage(message.setDescription("The user **"+oldname+"** with the id number **"+user_id+"**, tried to change his name into **"+newname+"**. Hence, he received the following nickname: **"+nickname+"**\nPlease review in case of impersonation!").build()).queue();
						logger.debug("{} got renamed into {} in guild {}. Impersonation", e.getUser().getId(), nickname, g.getName());
						staff_name = true;
						break check;
					} catch(HierarchyException hye){
						break check;
					}
				}
			}
		}
		
		if(staff_name == false){
			for(Guild guild : e.getJDA().getGuilds()){
				long guild_id = guild.getIdLong();
				Azrael.SQLgetNameFilter(guild_id);
				check: for(String word : Hashes.getQuerryResult("bad-names_"+guild_id)){
					if(nameCheck.contains(word)){
						Member user = e.getJDA().getGuildById(guild_id).getMemberById(user_id);
						
						if(user.getUser().getIdLong() != 0){
							long channel_id = Azrael.SQLgetChannelID(guild_id, "log");
							try {
								String nickname = Azrael.SQLgetRandomName(guild_id);
								e.getJDA().getGuildById(guild_id).getController().setNickname(user, nickname).queue();
								message.setColor(Color.ORANGE).setThumbnail(IniFileReader.getCatchedThumbnail()).setTitle("Not allowed name change found!");
								e.getJDA().getTextChannelById(channel_id).sendMessage(message.setDescription("The user **"+oldname+"** with the id number **"+user_id+"**, tried to change his name into **"+newname+"**. Hence, he received the following nickname: **"+nickname+"**").build()).queue();
								logger.debug("{} got renamed into {} in guild {}", e.getUser().getId(), nickname, guild.getName());
								break check;
							} catch (HierarchyException hye){
								message.setColor(Color.ORANGE).setThumbnail(IniFileReader.getFalseAlarmThumbnail()).setTitle("You know that you shouldn't do it :/");
								e.getJDA().getTextChannelById(channel_id).sendMessage("The user **"+oldname+"** with the id number **"+user_id+"**, tried to change his name into **"+newname+"** but had a higher role than myself. Hence, name won't be changed").queue();
								break check;
							}
						}
					}
				}
			}
		}
		Azrael.SQLInsertActionLog("MEMBER_NAME_UPDATE", e.getUser().getIdLong(), 0, e.getUser().getName()+"#"+e.getUser().getDiscriminator());
	}
}
