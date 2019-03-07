package listeners;

import java.awt.Color;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Bancollect;
import core.Guilds;
import core.Hashes;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import rankingSystem.Rank;
import sql.RankingSystem;
import sql.DiscordRoles;
import sql.Azrael;

public class GuildListener extends ListenerAdapter {
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent e){
		Logger logger = LoggerFactory.getLogger(GuildListener.class);
		logger.debug("{} has joined the guild {}", e.getUser().getId(), e.getGuild().getName());
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
		
		long channel_id = Azrael.SQLgetChannelID(guild_id, "log");
		if(Azrael.SQLInsertUser(user_id, user_name, e.getMember().getUser().getEffectiveAvatarUrl(), e.getMember().getJoinDate().format(DateTimeFormatter.ISO_LOCAL_DATE)) == 0) {
			if(channel_id != 0) e.getGuild().getTextChannelById(channel_id).sendMessage(err.setDescription("The user **"+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+"** with the ID number **"+user_id+"** couldn't be inserted into **Azrael.users** table").build()).queue();
			logger.error("User {} couldn't be inserted into the table Azrael.users for guild {}", e.getMember().getUser().getId(), e.getGuild().getName());
		}
		Guilds guild_settings = Hashes.getStatus(guild_id);
		if(guild_settings.getRankingState() == true){
			if(RankingSystem.SQLInsertUser(user_id, guild_id, e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), guild_settings.getLevelID(), guild_settings.getRankID(), guild_settings.getProfileID(), guild_settings.getIconID()) > 0) {
				if(RankingSystem.SQLInsertUserDetails(user_id, guild_id, 0, 0, 50000, 0) == 0) {
					if(channel_id != 0) e.getGuild().getTextChannelById(channel_id).sendMessage(err.setDescription("The user **"+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+"** with the ID number **"+user_id+"** couldn't be inserted into **RankingSystem.user_details** table").build()).queue();
					logger.error("Failed to insert joined user into RankingSystem.user_details");
					RankingSystem.SQLInsertActionLog("high", user_id, guild_id, "User wasn't inserted into user_details table", "This user couldn't be inserted into the user_details table. Please verify and eventually insert it manually into the table!");
				}
			}
			else {
				if(channel_id != 0) e.getGuild().getTextChannelById(channel_id).sendMessage(err.setDescription("The user **"+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+"** with the ID number **"+user_id+"** couldn't be inserted into **RankingSystem.users** table").build()).queue();
				logger.error("Failed to insert joined user into RankingSystem.users");
				RankingSystem.SQLInsertActionLog("high", user_id, guild_id, "User wasn't inserted into user table", "This user couldn't be inserted into the user table. Please verify the name of this user and eventually insert it manually into the table!");
			}
		}
		
		Bancollect warnedUser = Azrael.SQLgetData(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong());
		muted = warnedUser.getMuted();
		boolean custom_time = warnedUser.getCustomTime();
		if(GuildIni.getJoinMessage(guild_id)){
			if(channel_id != 0 && muted == false){e.getGuild().getTextChannelById(channel_id).sendMessage(message.setDescription(":warning: The user **" + user_name + "** with the ID Number **" + user_id + "** joined **" + e.getGuild().getName() + "**").build()).queue();}
		}
		
		try{
			unmute = warnedUser.getUnmute().getTime();
		} catch(NullPointerException npe){			
			unmute = 0;
		}
		
		if((unmute - currentTime) > 0 && (muted == true || custom_time == true)){
			e.getGuild().getController().addSingleRoleToMember(e.getMember(), e.getGuild().getRoleById(DiscordRoles.SQLgetRole(e.getGuild().getIdLong(), "mut"))).queue();
		}
		else{
			if(Hashes.getStatus(e.getGuild().getIdLong()).getRankingState()){
				Rank user_details = RankingSystem.SQLgetWholeRankView(user_id, guild_id);
				if(user_details.getCurrentRole() != 0){e.getGuild().getController().addSingleRoleToMember(e.getMember(), e.getGuild().getRoleById(user_details.getCurrentRole())).queue();}
			}
		}
		
		String lc_user_name = user_name.toLowerCase();
		check: for(String name : Azrael.SQLgetStaffNames(guild_id)){
			if(lc_user_name.matches(name+"#[0-9]{4}")){
				nick_assign.setColor(Color.RED).setTitle("Impersonation attempt found!").setThumbnail(e.getMember().getUser().getEffectiveAvatarUrl());
				String nickname = Azrael.SQLgetRandomName(e.getGuild().getIdLong());
				e.getGuild().getController().setNickname(e.getMember(), nickname).queue();
				e.getGuild().getTextChannelById(channel_id).sendMessage(nick_assign.setDescription("**"+user_name+"** joined this server and tried to impersonate a staff member. This nickname had been assigned to him/her: **"+nickname+"**").build()).queue();
				logger.info("Impersonation attempt found from {} in guild {}", e.getMember().getUser().getId(), e.getGuild().getName());
				badName = true;
				break check;
			}
		}
		if(badName == false){
			Azrael.SQLgetNameFilter(e.getGuild().getIdLong());
			check: for(String word : Hashes.getQuerryResult("bad-names_"+guild_id)){
				if(lc_user_name.contains(word)){
					String nickname = Azrael.SQLgetRandomName(e.getGuild().getIdLong());
					e.getGuild().getController().setNickname(e.getMember(), nickname).queue();
					e.getGuild().getTextChannelById(channel_id).sendMessage(nick_assign.setDescription("**"+user_name+"** joined this Server with an unproper name. This nickname had been assigned to him/her: **"+nickname+"**").build()).queue();
					logger.info("Improper name found from {} in guild {}", e.getMember().getUser().getId(), e.getGuild().getName());
					badName = true;
					break check;
				}
			}
		}
		if(badName == false){
			Azrael.SQLDeleteNickname(user_id, guild_id);
		}
		
		Azrael.SQLInsertActionLog("GUILD_MEMBER_JOIN", user_id, guild_id, user_name);
	}
}
