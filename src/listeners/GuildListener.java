package listeners;

import java.awt.Color;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Bancollect;
import constructors.Cache;
import constructors.Guilds;
import constructors.Rank;
import core.Hashes;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.RankingSystem;
import sql.DiscordRoles;
import sql.Azrael;

public class GuildListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(GuildListener.class);
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent e) {
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
		
		var log_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
		if(Azrael.SQLInsertUser(user_id, user_name, e.getMember().getUser().getEffectiveAvatarUrl(), e.getMember().getTimeJoined().format(DateTimeFormatter.ISO_LOCAL_DATE)) == 0) {
			if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(err.setDescription("The user **"+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+"** with the ID number **"+user_id+"** couldn't be inserted into **Azrael.users** table").build()).queue();
			logger.error("User {} couldn't be inserted into the table Azrael.users for guild {}", e.getMember().getUser().getId(), e.getGuild().getName());
		}
		Guilds guild_settings = RankingSystem.SQLgetGuild(guild_id);
		if(guild_settings.getRankingState() == true) {
			if(RankingSystem.SQLInsertUser(user_id, guild_id, e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), guild_settings.getLevelID(), guild_settings.getRankID(), guild_settings.getProfileID(), guild_settings.getIconID()) > 0) {
				RankingSystem.SQLInsertUserDetails(user_id, guild_id, 0, 0, 50000, 0);
			}
			else {
				if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(err.setDescription("The user **"+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+"** with the ID number **"+user_id+"** couldn't be inserted into **RankingSystem.users** table").build()).queue();
				logger.error("Failed to insert joined user into RankingSystem.users");
				RankingSystem.SQLInsertActionLog("high", user_id, guild_id, "User wasn't inserted into user table", "This user couldn't be inserted into the user table. Please verify the name of this user and eventually insert it manually into the table!");
			}
		}
		
		Bancollect warnedUser = Azrael.SQLgetData(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong());
		muted = warnedUser.getMuted();
		if(GuildIni.getJoinMessage(guild_id)) {
			if(log_channel != null && muted == false) {e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(message.setDescription(":warning: The user **" + user_name + "** with the ID Number **" + user_id + "** joined **" + e.getGuild().getName() + "**").build()).queue();}
		}
		
		try{
			unmute = warnedUser.getUnmute().getTime();
		} catch(NullPointerException npe) {			
			unmute = 0;
		}
		
		if((unmute - currentTime) > 0 && muted) {
			e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(DiscordRoles.SQLgetRole(e.getGuild().getIdLong(), "mut"))).queue();
		}
		else {
			if(guild_settings.getRankingState()) {
				Rank user_details = RankingSystem.SQLgetWholeRankView(user_id, guild_id, guild_settings.getThemeID());
				if(user_details.getCurrentRole() != 0) {e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(user_details.getCurrentRole())).queue();}
			}
		}
		
		var rejoinAction = Hashes.getRejoinTask(e.getGuild().getId()+"_"+e.getMember().getUser().getId());
		if(rejoinAction != null) {
			if(rejoinAction.getType().equals("mute")) {
				if(rejoinAction.getInfo().length() == 0) {
					e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(DiscordRoles.SQLgetRole(e.getGuild().getIdLong(), "mut"))).queue();
					var mute_time = (long)Azrael.SQLgetWarning(guild_id, Azrael.SQLgetData(user_id, guild_id).getWarningID()+1).getTimer();
					Azrael.SQLInsertHistory(user_id, guild_id, "mute", (rejoinAction.getReason().length() > 0 ? rejoinAction.getReason() : "No reason has been provided!"), (mute_time/1000/60));
					Hashes.addTempCache("mute_time_gu"+guild_id+"us"+user_id, new Cache(rejoinAction.getInfo2(), rejoinAction.getReason()));
				}
				else {
					var mute_time = Long.parseLong(rejoinAction.getInfo());
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					Timestamp unmute_timestamp = new Timestamp(System.currentTimeMillis()+mute_time);
					e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(DiscordRoles.SQLgetRole(e.getGuild().getIdLong(), "mut"))).queue();
					Azrael.SQLInsertHistory(user_id, guild_id, "mute", (rejoinAction.getReason().length() > 0 ? rejoinAction.getReason() : "No reason has been provided!"), (mute_time/1000/60));
					if(Azrael.SQLgetData(user_id, guild_id).getWarningID() != 0) {
						if(Azrael.SQLUpdateUnmute(user_id, guild_id, timestamp, unmute_timestamp, true, true) == 0) {
							logger.error("The unmute timer couldn't be updated from user {} in guild {} for the table Azrael.bancollect", user_id, guild_id);
							if(log_channel != null)e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occurred. The unmute time couldn't be updated on Azrael.bancollect").queue();
						}
					}
					else {
						if(Azrael.SQLInsertData(user_id, guild_id, 1, 1, timestamp, unmute_timestamp, true, true) == 0) {
							logger.error("muted user {} couldn't be inserted into Azrael.bancollect for guild {}", user_id, guild_id);
							if(log_channel != null)e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage("An internal error occurred. Muted user couldn't be inserted into Azrael.bancollect").queue();
						}
					}
					Hashes.addTempCache("mute_time_gu"+guild_id+"us"+user_id, new Cache(""+mute_time, rejoinAction.getInfo2(), rejoinAction.getReason()));
				}
				Hashes.removeRejoinTask(e.getGuild().getId()+"_"+e.getMember().getUser().getId());
			}
			else if(rejoinAction.getType().equals("ban")) {
				e.getUser().openPrivateChannel().complete().sendMessage("You have been banned from "+e.getGuild().getName()+". Thank you for your understanding.\n"
						+ "On an important note, this is an automatic reply. You'll receive no reply in any way.\n"
						+ (GuildIni.getBanSendReason(e.getGuild().getIdLong()) ? "Provided reason: "+rejoinAction.getReason() : "")).queue();
				e.getGuild().ban(e.getMember(), 0).reason(rejoinAction.getReason()).queue();
				Azrael.SQLInsertHistory(user_id, guild_id, "ban", rejoinAction.getReason(), 0);
				Hashes.addTempCache("ban_gu"+e.getGuild().getId()+"us"+user_id, new Cache(rejoinAction.getInfo2(), rejoinAction.getReason()));
				Hashes.removeRejoinTask(e.getGuild().getId()+"_"+e.getMember().getUser().getId());
			}
		}
		else {
			String nickname = null;
			String lc_user_name = user_name.toLowerCase();
			check: for(String name : Azrael.SQLgetStaffNames(guild_id)) {
				if(lc_user_name.matches(name+"#[0-9]{4}")) {
					nick_assign.setColor(Color.RED).setTitle("Impersonation attempt found!").setThumbnail(e.getMember().getUser().getEffectiveAvatarUrl());
					nickname = Azrael.SQLgetRandomName(e.getGuild().getIdLong());
					e.getGuild().modifyNickname(e.getMember(), nickname).queue();
					if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(nick_assign.setDescription("**"+user_name+"** joined this server and tried to impersonate a staff member. This nickname had been assigned to him/her: **"+nickname+"**").build()).queue();
					logger.info("Impersonation attempt found from {} in guild {}", user_id, guild_id);
					badName = true;
					break check;
				}
			}
			if(badName == false) {
				Azrael.SQLgetNameFilter(e.getGuild().getIdLong());
				check: for(var word : Hashes.getNameFilter(guild_id)) {
					if(lc_user_name.contains(word.getName())) {
						if(!word.getKick()) {
							nickname = Azrael.SQLgetRandomName(e.getGuild().getIdLong());
							e.getGuild().modifyNickname(e.getMember(), nickname).queue();
							if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(nick_assign.setDescription("**"+user_name+"** joined this server with an unproper name. This nickname had been assigned to him/her: **"+nickname+"**").build()).queue();
							logger.info("Improper name found from {} in guild {}", user_id, guild_id);
							badName = true;
						}
						else {
							e.getMember().getUser().openPrivateChannel().complete().sendMessage("You have been automatically kicked from "+e.getJDA().getGuildById(guild_id).getName()+" for having the word **"+word.getName().toUpperCase()+"** in your name!").complete();
							e.getGuild().kick(e.getMember()).reason("User kicked for having "+word.getName().toUpperCase()+" inside his name").queue();
							nick_assign.setColor(Color.RED).setThumbnail(IniFileReader.getCatchedThumbnail()).setTitle("User kicked for having a not allowed name!");
							if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(nick_assign.setDescription("**"+user_name+"** joined this server with an unproper name. The user has been kicked automatically from the server due to this word: **"+word.getName().toUpperCase()+"**").build()).queue();
						}
						break check;
					}
				}
			}
			if(badName == false) {
				Azrael.SQLDeleteNickname(user_id, guild_id);
			}
			else {
				if(Azrael.SQLgetNickname(user_id, guild_id).length() > 0 && nickname != null) {
					if(Azrael.SQLUpdateNickname(user_id, guild_id, nickname) == 0) {
						logger.error("User nickname of {} couldn't be updated in Azrael.nickname", user_id);
					}
				}
				else if(nickname != null) {
					if(Azrael.SQLInsertNickname(user_id, guild_id, nickname) == 0) {
						logger.error("User nickname of {} couldn't be inserted into Azrael.nickname", user_id);
					}
				}
				logger.debug("{} received the nickname {} in guild {}", user_id, nickname, guild_id);
				Azrael.SQLInsertActionLog("MEMBER_NICKNAME_UPDATE", user_id, guild_id, nickname);
			}
		}
		
		Azrael.SQLInsertActionLog("GUILD_MEMBER_JOIN", user_id, guild_id, user_name);
	}
}
