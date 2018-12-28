package threads;

import java.awt.Color;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Guilds;
import core.Hashes;
import core.UserPrivs;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.ReadyEvent;
import rankingSystem.Rank;
import sql.RankingSystem;
import sql.DiscordRoles;
import sql.Azrael;

public class BotStartAssign implements Runnable{
	private final Logger logger = LoggerFactory.getLogger(BotStartAssign.class);
	ReadyEvent e;
	
	public BotStartAssign(ReadyEvent _e){
		e = _e;
	}
	
	@Override
	public void run() {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.GREEN).setTitle("Roles assigned!");
		int i = 0;
		var errUsers = 0;
		var errUserDetails = 0;
		var errGuildUsers = 0;
		for(Guild g : e.getJDA().getGuilds()){
			long guild_id = g.getIdLong();
			Guilds guild_settings = Hashes.getStatus(guild_id);
			if(guild_settings.getRankingState() == true){
				for(Member member : e.getJDA().getGuildById(g.getIdLong()).getMembers()){
					if(!UserPrivs.isUserBot(member.getUser(), e.getJDA().getGuildById(guild_id).getIdLong()) && !UserPrivs.isUserMuted(member.getUser(), e.getJDA().getGuildById(guild_id).getIdLong()) && !UserPrivs.isUserCommunity(member.getUser(), e.getJDA().getGuildById(guild_id).getIdLong())){
						RankingSystem.SQLgetWholeRankView(member.getUser().getIdLong());
						Rank user_details = Hashes.getRanking(member.getUser().getIdLong());
						if(user_details != null){
							if(user_details.getCurrentRole() != 0){
								e.getJDA().getGuildById(guild_id).getController().addSingleRoleToMember(member, e.getJDA().getGuildById(guild_id).getRoleById(user_details.getCurrentRole())).queue();
								Azrael.SQLInsertUser(member.getUser().getIdLong(), member.getUser().getName()+"#"+member.getUser().getDiscriminator(), member.getUser().getEffectiveAvatarUrl(), member.getJoinDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
								i++;
							}
						}
						else{
							Azrael.SQLInsertUser(member.getUser().getIdLong(), member.getUser().getName()+"#"+member.getUser().getDiscriminator(), member.getUser().getEffectiveAvatarUrl(), member.getJoinDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
							var editedRows = RankingSystem.SQLInsertUser(member.getUser().getIdLong(), member.getUser().getName()+"#"+member.getUser().getDiscriminator(), guild_settings.getLevelID(), guild_settings.getRankID(), guild_settings.getProfileID(), guild_settings.getIconID());
							if(editedRows > 0) {
								var editedRows2 = RankingSystem.SQLInsertUserDetails(member.getUser().getIdLong(), 0, 0, 50000, 0);
								if(editedRows2 > 0) {
									var editedRows3 = RankingSystem.SQLInsertUserGuild(member.getUser().getIdLong(), guild_id);
									if(editedRows3 == 0) {
										RankingSystem.SQLInsertActionLog("high", member.getUser().getIdLong(), "User wasn't inserted into user_details table", "This user couldn't be inserted into the user_details table. Please verify and eventually insert it manually into the table!");
										errGuildUsers++;
									}
								}
								else {
									RankingSystem.SQLInsertActionLog("high", member.getUser().getIdLong(), "User wasn't inserted into user_details table", "This user couldn't be inserted into the user_details table. Please verify and eventually insert it manually into the table!");
									errUserDetails++;
									errGuildUsers++;
								}
							}
							else {
								RankingSystem.SQLInsertActionLog("high", member.getUser().getIdLong(), "User wasn't inserted into user table", "This user couldn't be inserted into the user table. Please verify the name of this user and eventually insert it manually into the table!");
								errUsers++;
								errUserDetails++;
								errGuildUsers++;
							}
						}
						RankingSystem.clearAllVariables();
					}
				}
			}
			logger.info("Start up user registration complete in {}", g.getName());
			Azrael.SQLgetChannelID(guild_id, "log");
			if(i != 0 && Azrael.getChannelID() != 0){
				e.getJDA().getGuildById(guild_id).getTextChannelById(Azrael.getChannelID()).sendMessage(message.setDescription(i+" User(s) received the community role on bot start up").build()).queue();
			}
			if((errUsers != 0 || errUserDetails != 0 || errGuildUsers != 0) && Azrael.getChannelID() != 0) {
				EmbedBuilder err = new EmbedBuilder().setColor(Color.RED).setTitle("Users couldn't be registered!");
				e.getJDA().getGuildById(guild_id).getTextChannelById(Azrael.getChannelID()).sendMessage(err.setDescription("Various users couldn't be inserted into the ranking system tables. Please check the error log for affected users!\n"
						+ "failed insertions in RankingSystem.users: "+errUsers+"\n"
						+ "failed insertions in RankingSystem.user_details: "+errUserDetails+"\n"
						+ "failed insertions in RankingSystem.user_guild: "+errGuildUsers).build()).queue();
			}
			RankingSystem.clearAllVariables();
			DiscordRoles.clearAllVariables();
		}
	}
}
