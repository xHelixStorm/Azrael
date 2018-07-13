package listeners;

import java.awt.Color;
import java.sql.Timestamp;

import core.UserPrivs;
import fileManagement.FileSetting;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.core.exceptions.HierarchyException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.RankingDB;
import sql.ServerRoles;
import sql.SqlConnect;
import threads.RoleTimer;

public class RoleListener extends ListenerAdapter{
	
	@Override
	public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent e){
		EmbedBuilder message = new EmbedBuilder().setColor(Color.RED);
		EmbedBuilder message2 = new EmbedBuilder().setColor(Color.GREEN);
		
		long user_id = e.getMember().getUser().getIdLong();
		long guild_id = e.getMember().getGuild().getIdLong();
		String user_name = e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator();
		String name_id = e.getMember().getUser().getId();
		long channel_id;
		long mute_id;
		long mute_time;
		double unmute;
		
		if(UserPrivs.isUserMuted(e.getMember().getUser(), e.getGuild().getIdLong())){
			SqlConnect.SQLgetChannelID(guild_id, "log");
			channel_id = SqlConnect.getChannelID();
			SqlConnect.SQLgetData(user_id, guild_id);
			long unmute_time = 0;
			try {
				if(SqlConnect.getUnmute().getTime() != 0) {
					unmute_time = SqlConnect.getUnmute().getTime();
				}
			} catch(NullPointerException npe) {
				unmute_time = -1;
			}
			if(unmute_time - System.currentTimeMillis() > 0){
				if(channel_id != 0){
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					e.getGuild().getTextChannelById(channel_id).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+e.getMember().getUser().getName()+"#"+user_name+ "** with the ID number **"+e.getMember().getUser().getId()+"** got his mute role reassigned before the mute time elapsed! Reason may be due to rejoining or manual role reassignment!").build()).queue();
				}
			}
			else{
				ServerRoles.SQLgetRole(guild_id, "mut");
				mute_id = ServerRoles.getRole_ID();
				try {
					for(Role r : e.getMember().getRoles()){
						if(r.getIdLong() != mute_id){
							e.getGuild().getController().removeSingleRoleFromMember(e.getMember(), r).queue();
						}
					}
					
					long time = System.currentTimeMillis();
					Timestamp firstTime = new Timestamp(time);
									
					int warning_id=SqlConnect.getWarningID();
					
					RankingDB.SQLgetUserDetails(user_id, guild_id);
					long assignedRole = RankingDB.getAssignedRole();
					
					long sqluser_id = SqlConnect.getUser_id();
					long sqlguild_id = SqlConnect.getGuild_id();
					
					if(user_id == sqluser_id && guild_id == sqlguild_id){
						if(warning_id == 5 || warning_id == 4){
							SqlConnect.SQLgetMuteTimer(guild_id);
							mute_time = (long) SqlConnect.getTimer1();
							unmute = (SqlConnect.getTimer1() / 3600000);
							Timestamp timestamp = new Timestamp(time+mute_time);
							SqlConnect.SQLUpdateWarning(user_id, guild_id, 1);
							SqlConnect.SQLUpdateUnmute(user_id, guild_id, timestamp);
							SqlConnect.SQLUpdateMuted(user_id, guild_id, true);
							PrivateChannel pc = e.getUser().openPrivateChannel().complete();
							pc.sendMessage("You have been muted on "+e.getGuild().getName()+" due to bad behaviour. Your current mute will last for **"+unmute+" hours** for being your first warning. Please, refrain from rejoining the server, since it will result in consequences.\n"
									+ "On a important note, this is an automatic reply. You'll receive no reply in any way.").queue();
							pc.close();
							new Thread(new RoleTimer(e, guild_id, name_id, user_name, unmute, mute_time, channel_id, mute_id, assignedRole)).start();
						}
						else if(warning_id == 1){
							SqlConnect.SQLgetMuteTimer(guild_id);
							mute_time = (long) SqlConnect.getTimer2();
							unmute = (SqlConnect.getTimer2() / 3600000);
							Timestamp timestamp = new Timestamp(time+mute_time);
							SqlConnect.SQLUpdateWarning(user_id, guild_id, 2);
							SqlConnect.SQLUpdateUnmute(user_id, guild_id, timestamp);
							SqlConnect.SQLUpdateMuted(user_id, guild_id, true);
							PrivateChannel pc = e.getUser().openPrivateChannel().complete();
							pc.sendMessage("You have been muted on "+e.getGuild().getName()+" due to bad behaviour. Your current mute will last for **"+unmute+" hours** for being your second warning. Please, refrain from rejoining the server, since it will result in consequences.\n"
									+ "On a important note, this is an automatic reply. You'll receive no reply in any way.").queue();
							pc.close();
							new Thread(new RoleTimer(e, guild_id, name_id, user_name, unmute, mute_time, channel_id, mute_id, assignedRole)).start();
						}
						else if(warning_id == 2){
							FileSetting.createFile(IniFileReader.getTempDirectory(), ""+user_id);
							mute_time = 0;
							Timestamp timestamp = new Timestamp(time+mute_time);
							SqlConnect.SQLUpdateWarning(user_id, guild_id, 3);
							SqlConnect.SQLUpdateUnmute(user_id, guild_id, timestamp);
							SqlConnect.SQLUpdateBan(user_id, guild_id, 2);
							SqlConnect.SQLUpdateMuted(user_id, guild_id, true);
							PrivateChannel pc = e.getUser().openPrivateChannel().complete();
							pc.sendMessage("You have been banned from "+e.getGuild().getName()+" due to your third mute. Thanks for your understanding.\n"
									+ "On a important note, this is an automatic reply. You'll receive no reply in any way.").queue();
							pc.close();
							e.getJDA().getGuildById(guild_id).getController().ban(e.getMember(), 0).reason("User has been muted for the third time due to bad behaviour!").queue();
							if(channel_id != 0){e.getGuild().getTextChannelById(channel_id).sendMessage(message.setDescription("["+timestamp.toString()+"] **" + user_name + " with the ID Number " + user_id + " Has been banned after his/her third warning!**").build()).queue();}
						}
					}
					else{
						SqlConnect.SQLgetMuteTimer(guild_id);
						mute_time = (long) SqlConnect.getTimer1();
						unmute = (SqlConnect.getTimer1() / 3600000);
						Timestamp timestamp = new Timestamp(time+mute_time);
						SqlConnect.SQLInsertData(user_id, guild_id, 1, 1, firstTime, timestamp, true);
						PrivateChannel pc = e.getUser().openPrivateChannel().complete();
						pc.sendMessage("You have been muted on "+e.getGuild().getName()+" due to bad behaviour. Your current mute will last for **"+unmute+" hours** for being your first warning. Please, refrain from rejoining the server, since it will result in consequences.\n"
								+ "On a important note, this is an automatic reply. You'll receive no reply in any way.").queue();
						pc.close();
						new Thread(new RoleTimer(e, guild_id, name_id, user_name, unmute, mute_time, channel_id, mute_id, assignedRole)).start();
					}
				} catch (HierarchyException hye) {
					hye.printStackTrace();
					e.getJDA().getGuildById(guild_id).getController().removeSingleRoleFromMember(e.getMember(), e.getGuild().getRoleById(mute_id)).queue();
					e.getGuild().getTextChannelById(channel_id).sendMessage(message2.setDescription("The mute role has been set on someone with higher privileges. Mute role removed!").build()).queue();
				}
			}
			ServerRoles.clearAllVariables();
			RankingDB.clearAllVariables();
			SqlConnect.clearAllVariables();
			SqlConnect.clearUnmute();
		}
	}
}