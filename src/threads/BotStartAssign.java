package threads;

import java.awt.Color;
import java.time.format.DateTimeFormatter;

import core.UserPrivs;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.ReadyEvent;
import sql.RankingDB;
import sql.ServerRoles;
import sql.SqlConnect;

public class BotStartAssign implements Runnable{
	ReadyEvent e;
	
	public BotStartAssign(ReadyEvent _e){
		e = _e;
	}
	
	@Override
	public void run() {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.GREEN).setTitle("Roles assigned!");
		boolean updatedUsers = false;
		boolean roleExists = false;
		int i = 0;
		for(Guild g : e.getJDA().getGuilds()){
			long guild_id = g.getIdLong();
			boolean ranking_state;
			
			RankingDB.SQLgetSingleRole();
			ServerRoles.SQLgetRole(guild_id, "com");
			if(RankingDB.getRoleID() != 0 && ServerRoles.getRole_ID() != 0){
				RankingDB.SQLgetGuild(guild_id);
				ranking_state = RankingDB.getRankingState();
				for(Member member : e.getJDA().getGuildById(guild_id).getMembers()){
					if(!UserPrivs.isUserBot(member.getUser(), e.getJDA().getGuildById(guild_id).getIdLong()) && !UserPrivs.isUserMuted(member.getUser(), e.getJDA().getGuildById(guild_id).getIdLong()) && !UserPrivs.isUserCommunity(member.getUser(), e.getJDA().getGuildById(guild_id).getIdLong()) && ranking_state == true){
						RankingDB.SQLInsertUser(member.getUser().getIdLong(), member.getUser().getName()+"#"+member.getUser().getDiscriminator(), RankingDB.getRankingLevel(), RankingDB.getRankingRank(), RankingDB.getRankingProfile(), RankingDB.getRankingIcon());
						RankingDB.SQLgetUserDetails(member.getUser().getIdLong());
						if(RankingDB.getAssignedRole() != 0){
							roleCheck: for(Role role : member.getRoles()){
								if(role.getIdLong() == RankingDB.getAssignedRole()){
									roleExists = false;
									break roleCheck;
								}
								else{
									roleExists = true;
								}
							}
						}
						
						if(roleExists == false && RankingDB.getAssignedRole() != 0){
							e.getJDA().getGuildById(guild_id).getController().addSingleRoleToMember(member, e.getJDA().getGuildById(guild_id).getRoleById(RankingDB.getAssignedRole())).queue();
							SqlConnect.SQLInsertUser(member.getUser().getIdLong(), member.getUser().getName()+"#"+member.getUser().getDiscriminator(), member.getUser().getEffectiveAvatarUrl(), member.getJoinDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
							updatedUsers = true;
							i++;
						}
					}
				}
				
				SqlConnect.SQLgetChannelID(guild_id, "log");
				SqlConnect.getChannelID();
				long channel_id = SqlConnect.getChannelID();
				if(updatedUsers == true){
					e.getJDA().getGuildById(guild_id).getTextChannelById(channel_id).sendMessage(message.setDescription(i+" User(s) received the community role on bot start up").build()).queue();
				}
				RankingDB.clearAllVariables();
				ServerRoles.clearAllVariables();
			}
		}
	}
}
