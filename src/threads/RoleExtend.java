package threads;

import java.awt.Color;
import java.util.ArrayList;

import core.Hashes;
import core.UserPrivs;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.ReadyEvent;
import sql.RankingDB;
import sql.ServerRoles;
import sql.SqlConnect;

public class RoleExtend implements Runnable{
	private ReadyEvent e;
	private long guild_id;
	
	public RoleExtend(ReadyEvent event, long _guild_id){
		e = event;
		guild_id = _guild_id;
	}
	
	@Override
	public void run() {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.RED).setTitle("Warned users can't run away even after a reboot!");
		boolean banHammerFound = false;
		ArrayList<Member> users = new ArrayList<Member>();
		ServerRoles.SQLgetRole(guild_id, "mut");
		Role mute_role = e.getJDA().getGuildById(guild_id).getRoleById(ServerRoles.getRole_ID());
		SqlConnect.SQLgetChannelID(guild_id, "log");
		long channel_id = SqlConnect.getChannelID();
		int i = 0;
		
		for(Member member : e.getJDA().getGuildById(guild_id).getMembers()){
			if(UserPrivs.isUserMuted(member.getUser(), e.getJDA().getGuildById(guild_id).getIdLong())){
				RankingDB.SQLgetWholeRankView(member.getUser().getIdLong());
				SqlConnect.SQLgetData(member.getUser().getIdLong(), guild_id);
				long unmute;
				try{
					unmute = (SqlConnect.getUnmute().getTime() - System.currentTimeMillis());
				} catch(NullPointerException npe){
					unmute = 0;
				}
				if(unmute < 0){unmute = 0;}
				long assignedRole = Hashes.getRanking(member.getUser().getIdLong()).getCurrentRole();
				users.add(member);
				banHammerFound = true;
				new Thread(new MuteRestart(e, member, guild_id, channel_id, mute_role, unmute, assignedRole, Hashes.getStatus(guild_id).getRankingState())).start();
				SqlConnect.clearUnmute();
				i++;
			}
		}
		if(banHammerFound == true && channel_id != 0){
			e.getJDA().getGuildById(guild_id).getTextChannelById(channel_id).sendMessage(message.setDescription(i+" users were found muted on start up. The mute timer is restarting from where it stopped!").build()).queue();
		}
		ServerRoles.clearAllVariables();
		SqlConnect.clearAllVariables();
	}
}
