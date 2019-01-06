package threads;

import java.awt.Color;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.ReadyEvent;
import rankingSystem.Rank;
import sql.RankingSystem;
import sql.DiscordRoles;
import sql.Azrael;

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
		Role mute_role = e.getJDA().getGuildById(guild_id).getRoleById(DiscordRoles.SQLgetRole(guild_id, "mut"));
		long channel_id = Azrael.SQLgetChannelID(guild_id, "log");
		int i = 0;
		
		for(Member member : e.getJDA().getGuildById(guild_id).getMembersWithRoles(mute_role)){
			long unmute;
			try{
				unmute = (Azrael.SQLgetData(member.getUser().getIdLong(), guild_id).getUnmute().getTime() - System.currentTimeMillis());
			} catch(NullPointerException npe){
				unmute = 0;
			}
			if(unmute < 0){unmute = 0;}
			long assignedRole = 0;
			boolean rankingState = false;
			Rank user_details = RankingSystem.SQLgetWholeRankView(member.getUser().getIdLong(), guild_id);
			if(user_details != null){
				assignedRole = user_details.getCurrentRole();
				rankingState = Hashes.getStatus(guild_id).getRankingState();
			}
			users.add(member);
			banHammerFound = true;
			new Thread(new MuteRestart(e, member, guild_id, channel_id, mute_role, unmute, assignedRole, rankingState)).start();
			i++;
		}
		if(banHammerFound == true && channel_id != 0){
			Logger logger = LoggerFactory.getLogger(RoleExtend.class);
			logger.debug("Found muted users on start up in {}", e.getJDA().getGuildById(guild_id).getName());
			e.getJDA().getGuildById(guild_id).getTextChannelById(channel_id).sendMessage(message.setDescription(i+" users were found muted on start up. The mute timer is restarting from where it stopped!").build()).queue();
		}
	}
}
