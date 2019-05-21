package listeners;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import core.UserPrivs;
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
		for(Guild guild : e.getJDA().getGuilds()){
			check: for(String name : Azrael.SQLgetStaffNames(guild.getIdLong())){
				if(nameCheck.matches(name+"#[0-9]{4}")){
					Member member = e.getJDA().getGuildById(guild.getIdLong()).getMemberById(user_id);
					var log_channel = Azrael.SQLgetChannels(guild.getIdLong()).parallelStream().filter(f -> f.getChannel_Type().equals("log")).findAny().orElse(null);
					try {
						String nickname = Azrael.SQLgetRandomName(guild.getIdLong());
						e.getJDA().getGuildById(guild.getIdLong()).getController().setNickname(member, nickname).queue();
						message.setColor(Color.RED).setThumbnail(e.getUser().getEffectiveAvatarUrl()).setTitle("Impersonation attempt found!");
						if(log_channel != null) e.getJDA().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription("The user **"+oldname+"** with the id number **"+user_id+"**, tried to change his name into **"+newname+"**. Hence, he received the following nickname: **"+nickname+"**\nPlease review in case of impersonation!").build()).queue();
						updateNickname(member, guild, nickname, logger);
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
				check: for(var word : Hashes.getNameFilter(guild_id)){
					if(nameCheck.contains(word.getName())){
						Member member = e.getJDA().getGuildById(guild_id).getMemberById(user_id);
						
						if(member.getUser().getIdLong() != 0){
							var log_channel = Azrael.SQLgetChannels(guild_id).parallelStream().filter(f -> f.getChannel_Type().equals("log")).findAny().orElse(null);
							try {
								if(!word.getKick()) {
									String nickname = Azrael.SQLgetRandomName(guild_id);
									e.getJDA().getGuildById(guild_id).getController().setNickname(member, nickname).queue();
									message.setColor(Color.ORANGE).setThumbnail(IniFileReader.getCatchedThumbnail()).setTitle("Not allowed name change found!");
									if(log_channel != null) e.getJDA().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription("The user **"+oldname+"** with the id number **"+user_id+"**, tried to change his name into **"+newname+"**. Hence, he received the following nickname: **"+nickname+"**").build()).queue();
									updateNickname(member, guild, nickname, logger);
								}
								else {
									if(!UserPrivs.isUserAdmin(member.getUser(), guild_id) && !UserPrivs.isUserMod(member.getUser(), guild_id)) {
										e.getUser().openPrivateChannel().complete().sendMessage("You have been automatically kicked from "+e.getJDA().getGuildById(guild_id).getName()+" for having the word **"+word.getName().toUpperCase()+"** in your name!").complete();
										e.getJDA().getGuildById(guild_id).getController().kick(member).reason("User kicked for having "+word.getName().toUpperCase()+" inside his name").queue();
										message.setColor(Color.RED).setThumbnail(IniFileReader.getCatchedThumbnail()).setTitle("User kicked for having a not allowed name!");
										if(log_channel != null) e.getJDA().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription("The user **"+oldname+"** with the id number **"+user_id+"**, tried to change his name into **"+newname+"** and was kicked for containing the following word in his name: **"+word.getName().toUpperCase()+"**").build()).queue();
									}
								}
								break check;
							} catch (HierarchyException hye) {
								if(!word.getKick()) {
									message.setColor(Color.ORANGE).setThumbnail(IniFileReader.getFalseAlarmThumbnail()).setTitle("You know that you shouldn't do it :/");
									e.getJDA().getTextChannelById(log_channel.getChannel_ID()).sendMessage("The user **"+oldname+"** with the id number **"+user_id+"**, tried to change his name into **"+newname+"** but had a higher role than myself. Hence, name won't be changed").queue();
								}
								else {
									//ignore that anything happened
								}
								break check;
							}
						}
					}
				}
			}
		}
		Azrael.SQLInsertActionLog("MEMBER_NAME_UPDATE", e.getUser().getIdLong(), 0, e.getUser().getName()+"#"+e.getUser().getDiscriminator());
	}
	
	private void updateNickname(Member member, Guild guild, String nickname, Logger logger) {
		var user_id = member.getUser().getIdLong();
		if(Azrael.SQLgetNickname(user_id, guild.getIdLong()).length() > 0){
			if(Azrael.SQLUpdateNickname(user_id, guild.getIdLong(), nickname) == 0) {
				logger.error("User nickname of {} couldn't be updated in Azrael.nickname", user_id);
			}
		}
		else if(Azrael.SQLInsertNickname(user_id, guild.getIdLong(), nickname) == 0) {
			logger.error("User nickname of {} couldn't be inserted into Azrael.nickname", user_id);
		}
		logger.debug("{} received the nickname {} in guild {}", member.getUser().getId(), nickname, guild.getName());
		Azrael.SQLInsertActionLog("MEMBER_NICKNAME_UPDATE", user_id, guild.getIdLong(), nickname);
	}
}
