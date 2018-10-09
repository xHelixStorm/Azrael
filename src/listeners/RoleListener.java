package listeners;

import java.awt.Color;
import java.sql.Timestamp;

import core.Hashes;
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
		RankingDB.SQLgetWholeRankView(e.getMember().getUser().getIdLong());
		EmbedBuilder message = new EmbedBuilder().setColor(Color.RED).setThumbnail(e.getMember().getUser().getEffectiveAvatarUrl()).setTitle("A user has been muted!");
		EmbedBuilder message2 = new EmbedBuilder().setColor(Color.GREEN).setTitle("Mute Retracted!");
		EmbedBuilder message3 = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getBanThumbnail()).setTitle("User banned!");
		
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
			if(unmute_time - System.currentTimeMillis() > 0 && SqlConnect.getCustomTime() == false){
				if(channel_id != 0){
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					e.getGuild().getTextChannelById(channel_id).sendMessage(message.setDescription("["+timestamp.toString()+"] **"+user_name+ "** with the ID number **"+e.getMember().getUser().getId()+"** got his mute role reassigned before the mute time elapsed! Reason may be due to rejoining or manual role reassignment!").build()).queue();
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
					int warning_id=SqlConnect.getWarningID();
					long assignedRole = Hashes.getRanking(user_id).getCurrentRole();
					
					if(SqlConnect.getCustomTime()) {
						SqlConnect.SQLUpdateMuted(e.getUser().getIdLong(), e.getGuild().getIdLong(), true, true);;
						mute_time = Long.parseLong(FileSetting.readFile(IniFileReader.getTempDirectory()+"AutoDelFiles/mute_time_"+e.getMember().getUser().getId()));
						long hours = (mute_time/1000/60/60);
						long minutes = (mute_time/1000/60%60);
						String hour_add = hours != 0 ? hours+" hours" : "";
						String minute_add = minutes != 0 ? minutes+" minutes" : "";
						String and_add = minutes != 0 && hours != 0 ? " and " : "";
						
						PrivateChannel pc = e.getUser().openPrivateChannel().complete();
						pc.sendMessage("You have been muted on "+e.getGuild().getName()+" due to bad behaviour. Your current mute will last for **"+hour_add+and_add+minute_add+"** . Except for the first mute, your warning counter won't increase.\nPlease, refrain from rejoining the server, since it will result in consequences.\n"
								+ "On a important note, this is an automated reply. You'll receive no reply in any way.").queue();
						pc.close();
						new Thread(new RoleTimer(e, guild_id, name_id, user_name, mute_time, channel_id, mute_id, assignedRole, hour_add, and_add, minute_add, 0, 0)).start();
						FileSetting.deleteFile(IniFileReader.getTempDirectory()+"AutoDelFiles/mute_time_"+e.getMember().getUser().getId());
					}
					else {
						SqlConnect.SQLgetWarning(e.getGuild().getIdLong(), (warning_id+1));
						mute_time = (long) SqlConnect.getTimer();
						unmute = SqlConnect.getTimer();
						long hours = (long) (unmute/1000/60/60);
						long minutes = (long) (unmute/1000/60%60);
						String hour_add = hours != 0 ? hours+" hours" : "";
						String minute_add = minutes != 0 ? minutes+" minutes" : "";
						String and_add = minutes != 0 && hours != 0 ? " and " : "";
						Timestamp timestamp = new Timestamp(time);
						Timestamp unmute_timestamp = new Timestamp(time+mute_time);
						
						SqlConnect.SQLgetMaxWarning(e.getGuild().getIdLong());
						int max_warning = SqlConnect.getWarningID();
						if((warning_id+1) <= max_warning) {
							SqlConnect.SQLInsertData(user_id, guild_id, (warning_id+1), 1, timestamp, unmute_timestamp, true, false);
							PrivateChannel pc = e.getUser().openPrivateChannel().complete();
							pc.sendMessage("You have been muted on "+e.getGuild().getName()+" due to bad behaviour. Your current mute will last for **"+hour_add+and_add+minute_add+"** for being your "+SqlConnect.getDescription()+". Warning **"+(warning_id+1)+"**/**"+max_warning+"**\nPlease, refrain from rejoining the server, since it will result in consequences.\n"
									+ "On a important note, this is an automated reply. You'll receive no reply in any way.").queue();
							pc.close();
							new Thread(new RoleTimer(e, guild_id, name_id, user_name, mute_time, channel_id, mute_id, assignedRole, hour_add, and_add, minute_add, (warning_id+1), max_warning)).start();
						}
						else if((warning_id+1) > max_warning) {
							PrivateChannel pc = e.getUser().openPrivateChannel().complete();
							pc.sendMessage("You have been banned from "+e.getGuild().getName()+", since you have exceeded the max amount of allowed mutes on this server. Thank you for your understanding.\n"
									+ "On a important note, this is an automated reply. You'll receive no reply in any way.").queue();
							pc.close();
							e.getJDA().getGuildById(guild_id).getController().ban(e.getMember(), 0).reason("User has been muted after reaching the limit of max allowed mutes!").queue();
							if(channel_id != 0){e.getGuild().getTextChannelById(channel_id).sendMessage(message3.setDescription("["+timestamp.toString()+"] **" + user_name + " with the ID Number " + user_id + " Has been banned after reaching the limit of allowed mutes on this server!**").build()).queue();}
						}
					}
				} catch (HierarchyException hye) {
					hye.printStackTrace();
					e.getJDA().getGuildById(guild_id).getController().removeSingleRoleFromMember(e.getMember(), e.getGuild().getRoleById(mute_id)).queue();
					e.getGuild().getTextChannelById(channel_id).sendMessage(message2.setDescription("The mute role has been set on someone with higher privileges. Mute role removed!").build()).queue();
				}
			}
			if(unmute_time - System.currentTimeMillis() < 0){
				SqlConnect.SQLInsertActionLog("MEMBER_MUTE_ADD", user_id, guild_id, "User Muted");
			}
			ServerRoles.clearAllVariables();
			SqlConnect.clearAllVariables();
			SqlConnect.clearUnmute();
			SqlConnect.clearTimestamp();
		}
	}
}