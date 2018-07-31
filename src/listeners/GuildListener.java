package listeners;

import java.awt.Color;

import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.RankingDB;
import sql.ServerRoles;
import sql.SqlConnect;

public class GuildListener extends ListenerAdapter {
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent e){
		EmbedBuilder message = new EmbedBuilder().setColor(Color.GREEN).setTitle("User joined!");
		EmbedBuilder nick_assign = new EmbedBuilder().setColor(Color.ORANGE).setThumbnail(IniFileReader.getCatchedThumbnail()).setTitle("Not allowed name change found!");
		
		long user_id = e.getMember().getUser().getIdLong();
		String user_name = e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator();
		long guild_id = e.getGuild().getIdLong();
		long currentTime = System.currentTimeMillis();;
		boolean badName = false;
		long unmute;
		boolean muted;
		
		SqlConnect.SQLgetChannelID(guild_id, "log");
		long channel_id = SqlConnect.getChannelID();
		SqlConnect.SQLgetData(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong());
		muted = SqlConnect.getMuted();
		if(IniFileReader.getJoinMessage().equals("true")){
			if(channel_id != 0 && muted == false){e.getGuild().getTextChannelById(channel_id).sendMessage(message.setDescription(":warning: The user **" + user_name + "** with the ID Number **" + user_id + "** joined **" + e.getGuild().getName() + "**").build()).queue();}
		}
		
		try{
			SqlConnect.SQLgetData(user_id, guild_id);
			unmute = SqlConnect.getUnmute().getTime();
		} catch(NullPointerException npe){			
			unmute = 0;
		} finally {
			SqlConnect.clearAllVariables();
			SqlConnect.clearUnmute();
		}
		
		if((unmute - currentTime) > 0){
			ServerRoles.SQLgetRole(e.getGuild().getIdLong(), "mut");
			long mute_role = ServerRoles.getRole_ID();
			e.getGuild().getController().addSingleRoleToMember(e.getMember(), e.getGuild().getRoleById(mute_role)).queue();
			ServerRoles.clearAllVariables();
		}
		else{
			SqlConnect.SQLInsertUser(user_id, user_name);
			RankingDB.SQLgetGuild(guild_id);
			if(RankingDB.getRankingState() == true){
				RankingDB.SQLInsertUser(user_id, user_name, RankingDB.getRankingLevel(), RankingDB.getRankingRank(), RankingDB.getRankingProfile(), RankingDB.getRankingIcon());
				RankingDB.SQLInsertUserGuild(user_id, guild_id);
				RankingDB.SQLgetUserDetails(user_id);
				if(RankingDB.getUserID() == 0){RankingDB.SQLInsertUserDetails(user_id, 0, 0, 300, 0, 50000, 0);}
				if(RankingDB.getAssignedRole() != 0){e.getGuild().getController().addSingleRoleToMember(e.getMember(), e.getGuild().getRoleById(RankingDB.getAssignedRole())).queue();}
			}
			RankingDB.clearAllVariables();
		}
		
		if(IniFileReader.getNameFilter().equals("true")) {
			String lc_user_name = user_name.toLowerCase();
			SqlConnect.SQLgetNameFilter();
			check: for(String word : SqlConnect.getNames()){
				if(lc_user_name.contains(word) || lc_user_name.contains("["+word+"]")){
					SqlConnect.SQLgetRandomName();
					String nickname = SqlConnect.getName();
					e.getGuild().getController().setNickname(e.getMember(), nickname).queue();
					e.getGuild().getTextChannelById(channel_id).sendMessage(nick_assign.setDescription("**"+user_name+"** joined this Server with an unproper name. This nickname had been assigned to him/her: **"+nickname+"**").build()).queue();
					badName = true;
					break check;
				}
				else{
					badName = false;
				}
			}
			SqlConnect.clearNames();
			if(badName == false){
				SqlConnect.SQLDeleteNickname(user_id, guild_id);
			}
		}
		
		SqlConnect.SQLInsertActionLog("GUILD_MEMBER_JOIN", user_id, guild_id, user_name);
		SqlConnect.clearAllVariables();
	}
}
