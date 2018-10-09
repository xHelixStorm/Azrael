package threads;

import java.awt.Color;
import java.time.format.DateTimeFormatter;

import core.Guilds;
import core.Hashes;
import core.UserPrivs;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.ReadyEvent;
import rankingSystem.Rank;
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
			Guilds guild_settings = Hashes.getStatus(guild_id);
			if(guild_settings.getRankingState() == true){
				for(Member member : e.getJDA().getGuildById(g.getIdLong()).getMembers()){
					if(!UserPrivs.isUserBot(member.getUser(), e.getJDA().getGuildById(guild_id).getIdLong()) && !UserPrivs.isUserMuted(member.getUser(), e.getJDA().getGuildById(guild_id).getIdLong()) && !UserPrivs.isUserCommunity(member.getUser(), e.getJDA().getGuildById(guild_id).getIdLong())){
						RankingDB.SQLgetWholeRankView(member.getUser().getIdLong());
						Rank user_details = Hashes.getRanking(member.getUser().getIdLong());
						if(user_details != null){
							if(user_details.getCurrentRole() != 0){
								e.getJDA().getGuildById(guild_id).getController().addSingleRoleToMember(member, e.getJDA().getGuildById(guild_id).getRoleById(RankingDB.getAssignedRole())).queue();
								SqlConnect.SQLInsertUser(member.getUser().getIdLong(), member.getUser().getName()+"#"+member.getUser().getDiscriminator(), member.getUser().getEffectiveAvatarUrl(), member.getJoinDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
								i++;
							}
						}
						else{
							RankingDB.SQLInsertUser(member.getUser().getIdLong(), member.getUser().getName()+"#"+member.getUser().getDiscriminator(), guild_settings.getLevelID(), guild_settings.getRankID(), guild_settings.getProfileID(), guild_settings.getIconID());
							RankingDB.SQLInsertUserDetails(member.getUser().getIdLong(), 0, 0, 50000, 0);
							RankingDB.SQLInsertUserGuild(member.getUser().getIdLong(), guild_id);
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
