package listeners;

import java.awt.Color;
import java.time.format.DateTimeFormatter;

import core.Guilds;
import core.Hashes;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import rankingSystem.Rank;
import sql.RankingDB;
import sql.ServerRoles;
import sql.SqlConnect;

public class GuildListener extends ListenerAdapter {
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent e){
		EmbedBuilder message = new EmbedBuilder().setColor(Color.GREEN).setTitle("User joined!");
		EmbedBuilder nick_assign = new EmbedBuilder().setColor(Color.ORANGE).setThumbnail(IniFileReader.getCatchedThumbnail()).setTitle("Not allowed name found!");
		EmbedBuilder err = new EmbedBuilder().setColor(Color.RED).setTitle("An error occurred!");
		
		long user_id = e.getMember().getUser().getIdLong();
		String user_name = e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator();
		long guild_id = e.getGuild().getIdLong();
		long currentTime = System.currentTimeMillis();;
		boolean badName = false;
		long unmute;
		boolean muted;
		
		SqlConnect.SQLgetChannelID(guild_id, "log");
		long channel_id = SqlConnect.getChannelID();
		SqlConnect.SQLInsertUser(user_id, user_name, e.getMember().getUser().getEffectiveAvatarUrl(), e.getMember().getJoinDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
		Guilds guild_settings = Hashes.getStatus(guild_id);
		if(guild_settings.getRankingState() == true){
			var editedRows = RankingDB.SQLInsertUser(user_id, e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), guild_settings.getLevelID(), guild_settings.getRankID(), guild_settings.getProfileID(), guild_settings.getIconID());
			if(editedRows > 0) {
				var editedRows2 = RankingDB.SQLInsertUserDetails(user_id, 0, 0, 50000, 0);
				if(editedRows2 > 0) {
					var editedRows3 = RankingDB.SQLInsertUserGuild(user_id, guild_id);
					if(editedRows3 == 0) {
						if(channel_id != 0) e.getGuild().getTextChannelById(channel_id).sendMessage(err.setDescription("The user **"+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+"** with the ID number **"+user_id+"** couldn't be inserted into **RankingSystem.user_details** table").build()).queue();
						RankingDB.SQLInsertActionLog("high", user_id, "User wasn't inserted into user_details table", "This user couldn't be inserted into the user_details table. Please verify and eventually insert it manually into the table!");
					}
				}
				else {
					if(channel_id != 0) e.getGuild().getTextChannelById(channel_id).sendMessage(err.setDescription("The user **"+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+"** with the ID number **"+user_id+"** couldn't be inserted into **RankingSystem.user_details** table").build()).queue();
					RankingDB.SQLInsertActionLog("high", user_id, "User wasn't inserted into user_details table", "This user couldn't be inserted into the user_details table. Please verify and eventually insert it manually into the table!");
				}
			}
			else {
				if(channel_id != 0) e.getGuild().getTextChannelById(channel_id).sendMessage(err.setDescription("The user **"+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+"** with the ID number **"+user_id+"** couldn't be inserted into **RankingSystem.users** table").build()).queue();
				RankingDB.SQLInsertActionLog("high", user_id, "User wasn't inserted into user table", "This user couldn't be inserted into the user table. Please verify the name of this user and eventually insert it manually into the table!");
			}
		}
		
		SqlConnect.SQLgetData(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong());
		muted = SqlConnect.getMuted();
		boolean custom_time = SqlConnect.getCustomTime();
		if(IniFileReader.getJoinMessage().equals("true")){
			if(channel_id != 0 && muted == false){e.getGuild().getTextChannelById(channel_id).sendMessage(message.setDescription(":warning: The user **" + user_name + "** with the ID Number **" + user_id + "** joined **" + e.getGuild().getName() + "**").build()).queue();}
		}
		
		try{
			unmute = SqlConnect.getUnmute().getTime();
		} catch(NullPointerException npe){			
			unmute = 0;
		} finally {
			SqlConnect.clearAllVariables();
			SqlConnect.clearUnmute();
		}
		
		if((unmute - currentTime) > 0 && (muted == true || custom_time == true)){
			ServerRoles.SQLgetRole(e.getGuild().getIdLong(), "mut");
			long mute_role = ServerRoles.getRole_ID();
			e.getGuild().getController().addSingleRoleToMember(e.getMember(), e.getGuild().getRoleById(mute_role)).queue();
			ServerRoles.clearAllVariables();
		}
		else{
			if(Hashes.getStatus(e.getGuild().getIdLong()).getRankingState()){
				RankingDB.SQLgetWholeRankView(user_id);
				Rank user_details = Hashes.getRanking(user_id);
				if(user_details.getCurrentRole() != 0){e.getGuild().getController().addSingleRoleToMember(e.getMember(), e.getGuild().getRoleById(user_details.getCurrentRole())).queue();}
			}
		}
		
		if(IniFileReader.getNameFilter().equals("true")) {
			String lc_user_name = user_name.toLowerCase();
			SqlConnect.SQLgetStaffNames(guild_id);
			check: for(String name : Hashes.getQuerryResult("staff-names_"+guild_id)){
				if(lc_user_name.matches(name+"#[0-9]{4}")){
					nick_assign.setColor(Color.RED).setTitle("Impersonation attempt found!").setThumbnail(e.getMember().getUser().getEffectiveAvatarUrl());
					SqlConnect.SQLgetRandomName(e.getGuild().getIdLong());
					String nickname = SqlConnect.getName();
					e.getGuild().getController().setNickname(e.getMember(), nickname).queue();
					e.getGuild().getTextChannelById(channel_id).sendMessage(nick_assign.setDescription("**"+user_name+"** joined this server and tried to impersonate a staff member. This nickname had been assigned to him/her: **"+nickname+"**").build()).queue();
					badName = true;
					break check;
				}
			}
			if(badName == false){
				SqlConnect.SQLgetNameFilter(e.getGuild().getIdLong());
				check: for(String word : Hashes.getQuerryResult("bad-names_"+guild_id)){
					if(lc_user_name.contains(word)){
						SqlConnect.SQLgetRandomName(e.getGuild().getIdLong());
						String nickname = SqlConnect.getName();
						e.getGuild().getController().setNickname(e.getMember(), nickname).queue();
						e.getGuild().getTextChannelById(channel_id).sendMessage(nick_assign.setDescription("**"+user_name+"** joined this Server with an unproper name. This nickname had been assigned to him/her: **"+nickname+"**").build()).queue();
						badName = true;
						break check;
					}
				}
			}
			if(badName == false){
				SqlConnect.SQLDeleteNickname(user_id, guild_id);
			}
		}
		
		SqlConnect.SQLInsertActionLog("GUILD_MEMBER_JOIN", user_id, guild_id, user_name);
		SqlConnect.clearAllVariables();
	}
}
