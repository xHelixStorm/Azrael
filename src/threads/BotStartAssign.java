package threads;

import java.awt.Color;
import java.time.format.DateTimeFormatter;

import core.UserPrivs;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
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
		int i = 0;
		for(Guild g : e.getJDA().getGuilds()){
			long guild_id = g.getIdLong();
			
			RankingDB.SQLgetGuild(g.getIdLong());
			if(RankingDB.getRankingState()){
				for(Member member : e.getJDA().getGuildById(g.getIdLong()).getMembers()){
					if(!UserPrivs.isUserBot(member.getUser(), e.getJDA().getGuildById(guild_id).getIdLong()) && !UserPrivs.isUserMuted(member.getUser(), e.getJDA().getGuildById(guild_id).getIdLong()) && !UserPrivs.isUserCommunity(member.getUser(), e.getJDA().getGuildById(guild_id).getIdLong())){
						RankingDB.SQLgetUserDetails(member.getUser().getIdLong());
						if(RankingDB.getAssignedRole() != 0){
							e.getJDA().getGuildById(guild_id).getController().addSingleRoleToMember(member, e.getJDA().getGuildById(guild_id).getRoleById(RankingDB.getAssignedRole())).queue();
							SqlConnect.SQLInsertUser(member.getUser().getIdLong(), member.getUser().getName()+"#"+member.getUser().getDiscriminator(), member.getUser().getEffectiveAvatarUrl(), member.getJoinDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
							i++;
						}
						RankingDB.clearAllVariables();
					}
				}
			}
			SqlConnect.SQLgetChannelID(guild_id, "log");
			if(i != 0){
				e.getJDA().getGuildById(guild_id).getTextChannelById(SqlConnect.getChannelID()).sendMessage(message.setDescription(i+" User(s) received the community role on bot start up").build()).queue();
			}
			RankingDB.clearAllVariables();
			ServerRoles.clearAllVariables();
		}
	}
}
