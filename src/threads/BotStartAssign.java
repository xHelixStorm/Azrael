package threads;

import java.awt.Color;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Guilds;
import core.UserPrivs;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.ReadyEvent;
import rankingSystem.Rank;
import sql.RankingSystem;
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
		var errAzraelUsers = 0;
		for(Guild g : e.getJDA().getGuilds()){
			long guild_id = g.getIdLong();
			Guilds guild_settings = RankingSystem.SQLgetGuild(guild_id);
			if(guild_settings.getRankingState() == true){
				for(Member member : e.getJDA().getGuildById(g.getIdLong()).getMembers()){
					if(!UserPrivs.isUserBot(member.getUser(), e.getJDA().getGuildById(guild_id).getIdLong()) && !UserPrivs.isUserMuted(member.getUser(), e.getJDA().getGuildById(guild_id).getIdLong()) && !UserPrivs.isUserCommunity(member.getUser(), e.getJDA().getGuildById(guild_id).getIdLong())){
						Rank user_details = RankingSystem.SQLgetWholeRankView(member.getUser().getIdLong(), g.getIdLong(), guild_settings.getThemeID());
						if(user_details != null){
							if(user_details.getCurrentRole() != 0){
								e.getJDA().getGuildById(guild_id).getController().addSingleRoleToMember(member, e.getJDA().getGuildById(guild_id).getRoleById(user_details.getCurrentRole())).queue();
								if(Azrael.SQLInsertUser(member.getUser().getIdLong(), member.getUser().getName()+"#"+member.getUser().getDiscriminator(), member.getUser().getEffectiveAvatarUrl(), member.getJoinDate().format(DateTimeFormatter.ISO_LOCAL_DATE)) == 0) {
									logger.error("User {} couldn't be inserted into the table Azrael.users for guild {}", member.getUser().getId(), g.getName());
									errAzraelUsers++;
								}
								i++;
							}
						}
						else{
							if(Azrael.SQLInsertUser(member.getUser().getIdLong(), member.getUser().getName()+"#"+member.getUser().getDiscriminator(), member.getUser().getEffectiveAvatarUrl(), member.getJoinDate().format(DateTimeFormatter.ISO_LOCAL_DATE)) == 0) {
								logger.error("User {} couldn't be updated into the table Azrael.users for guild {}", member.getUser().getId(), g.getName());
							}
							if(RankingSystem.SQLInsertUser(member.getUser().getIdLong(), guild_id, member.getUser().getName()+"#"+member.getUser().getDiscriminator(), guild_settings.getLevelID(), guild_settings.getRankID(), guild_settings.getProfileID(), guild_settings.getIconID()) > 0) {
								if(RankingSystem.SQLInsertUserDetails(member.getUser().getIdLong(), guild_id, 0, 0, 50000, 0) > 0) {
									RankingSystem.SQLInsertActionLog("high", member.getUser().getIdLong(), guild_id, "User wasn't inserted into user_details table", "This user couldn't be inserted into the user_details table. Please verify and eventually insert it manually into the table!");
									logger.error("Failed to insert joined user into RankingSystem.user_details user {} in guild {}", member.getUser().getId(), guild_id);
									errUserDetails++;
								}
							}
							else {
								RankingSystem.SQLInsertActionLog("high", member.getUser().getIdLong(), guild_id, "User wasn't inserted into user table", "This user couldn't be inserted into the user table. Please verify the name of this user and eventually insert it manually into the table!");
								logger.error("Failed to insert joined user into RankingSystem.users user {} in guild {}", member.getUser().getId(), guild_id);
								errUsers++;
								errUserDetails++;
							}
						}
					}
				}
			}
			logger.debug("Start up user registration complete in {}", g.getName());
			var log_channel = Azrael.SQLgetChannels(guild_id).parallelStream().filter(f -> f.getChannel_Type().equals("log")).findAny().orElse(null);
			if(i != 0 && log_channel != null){
				e.getJDA().getGuildById(guild_id).getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription(i+" User(s) received the community role on bot start up").build()).queue();
			}
			if((errUsers != 0 || errUserDetails != 0 || errAzraelUsers != 0) && log_channel != null) {
				EmbedBuilder err = new EmbedBuilder().setColor(Color.RED).setTitle("Users couldn't be registered!");
				e.getJDA().getGuildById(guild_id).getTextChannelById(log_channel.getChannel_ID()).sendMessage(err.setDescription("Various users couldn't be inserted into the ranking system tables. Please check the error log for affected users!\n"
						+ "failed insertions in RankingSystem.users: "+errUsers+"\n"
						+ "failed insertions in RankingSystem.user_details: "+errUserDetails+"\n"
						+ "failed insertions in Azrael.users: "+errAzraelUsers).build()).queue();
			}
		}
	}
}
