package listeners;

import java.awt.Color;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import core.Hashes;
import enums.Channel;
import enums.GoogleEvent;
import enums.Translation;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import google.GoogleUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.Azrael;
import util.STATIC;

/**
 * This class gets executed when a user changes his Discord name!
 * 
 * Upon name change, the bot will look up for the user in question in all
 * servers where this bot is present. If the user doesn't on specific servers,
 * the action will be ignored for those servers. For any other server, the
 * user will be compared with all registered staff names first and then 
 * gets compared with not allowed words. If the name contains not allowed
 * names or words, the user will either receive a nickname or will get kicked
 * from the server.
 * @author xHelixStorm
 */

public class NameListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(NameListener.class);
	
	@Override
	public void onUserUpdateName(UserUpdateNameEvent e) {
		new Thread(() -> {
			EmbedBuilder message = new EmbedBuilder();
			Azrael.SQLInsertActionLog("MEMBER_NAME_UPDATE", e.getUser().getIdLong(), 0, e.getUser().getName()+"#"+e.getUser().getDiscriminator());
			
			String oldname = e.getOldName()+"#"+e.getUser().getDiscriminator();
			String newname = e.getUser().getName()+"#"+e.getUser().getDiscriminator();
			long user_id = e.getUser().getIdLong();
			String nameCheck = newname.toLowerCase();
			
			//update the user name in table Azrael.users
			if(Azrael.SQLUpdateUser(user_id, newname) == 0) {
				logger.error("Azrael.users couldn't be updated with a name update from {}",user_id);
			}
			//process every mutual guild in parallel
			e.getUser().getMutualGuilds().parallelStream().forEach(guild -> {
				boolean staff_name = false;
				//verify that the user exists in the current server
				Member member = guild.getMemberById(user_id);
				if(member != null) {
					//lookup if the user is using the same name as a registered staff member name
					final var name = Azrael.SQLgetStaffNames(guild.getIdLong()).parallelStream().filter(f -> nameCheck.matches(f+"#[0-9]{4}")).findAny().orElse(null);
					if(name != null) {
						//verify that the user has lower permissions than the bot
						if(guild.getSelfMember().canInteract(member)) {
							//verify if the bot has the permission to manage nicknames
							if(guild.getSelfMember().hasPermission(Permission.NICKNAME_MANAGE)) {
								//retrieve a random nickname and assign to the user
								String nickname = Azrael.SQLgetRandomName(guild.getIdLong());
								e.getJDA().getGuildById(guild.getIdLong()).modifyNickname(member, nickname).queue();
								Hashes.addTempCache("nickname_add_gu"+guild.getId()+"us"+user_id, new Cache(60000));
								updateNickname(member, nickname);
								message.setColor(Color.RED).setThumbnail(e.getUser().getEffectiveAvatarUrl()).setTitle(STATIC.getTranslation2(guild, Translation.NAME_STAFF_TITLE));
								STATIC.writeToRemoteChannel(guild, message, STATIC.getTranslation2(guild, Translation.NAME_STAFF_2).replaceFirst("\\{\\}", oldname).replaceFirst("\\{\\}", ""+user_id).replaceFirst("\\{\\}", newname).replace("{}", nickname), Channel.LOG.getType());
								Azrael.SQLInsertActionLog("MEMBER_NICKNAME_UPDATE", e.getUser().getIdLong(), guild.getIdLong(), newname);
								//Run google service, if enabled
								if(GuildIni.getGoogleFunctionalitiesEnabled(guild.getIdLong()) && GuildIni.getGoogleSpreadsheetsEnabled(guild.getIdLong())) {
									GoogleUtils.handleSpreadsheetRequest(Azrael.SQLgetGoogleFilesAndEvent(guild.getIdLong(), 2, GoogleEvent.RENAME.id, ""), guild, "", ""+user_id, new Timestamp(System.currentTimeMillis()), e.getUser().getName()+"#"+e.getUser().getDiscriminator(), null, guild.getSelfMember().getUser().getName()+"#"+guild.getSelfMember().getUser().getDiscriminator(), guild.getSelfMember().getEffectiveName(), STATIC.getTranslation2(guild, Translation.NAME_STAFF_IMPERSONATION), null, null, "RENAMED", null, null, null, oldname, newname, 0, null, null, 0, 0, GoogleEvent.RENAME.id);
								}
							}
							else {
								STATIC.writeToRemoteChannel(guild, message.setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(guild, Translation.NAME_STAFF_ERR_2)+Permission.NICKNAME_MANAGE.getName(), Channel.LOG.getType());
								logger.warn("Lacking MANAGE NICKNAME permission in guild {}", guild.getId());
							}
							staff_name = true;
						}
					}
					
					//execute only if the staff name search failed
					if(!staff_name) {
						//look up the name filter for not allowed words
						final var word = Azrael.SQLgetNameFilter(guild.getIdLong()).parallelStream().filter(f -> nameCheck.contains(f.getName())).findAny().orElse(null);
						if(word != null) {
							//verify that the user has lower permissions than the bot
							if(guild.getSelfMember().canInteract(member)) {
								if(!word.getKick()) {
									//verify if the bot has the permission to manage nicknames
									if(guild.getSelfMember().hasPermission(Permission.NICKNAME_MANAGE)) {
										//retrieve a random nickname and assign to the user
										String nickname = Azrael.SQLgetRandomName(guild.getIdLong());
										guild.modifyNickname(member, nickname).queue();
										Hashes.addTempCache("nickname_add_gu"+guild.getId()+"us"+user_id, new Cache(60000));
										updateNickname(member, nickname);
										message.setColor(Color.ORANGE).setThumbnail(IniFileReader.getCaughtThumbnail()).setTitle(STATIC.getTranslation2(guild, Translation.NAME_TITLE));
										STATIC.writeToRemoteChannel(guild, message, STATIC.getTranslation2(guild, Translation.NAME_ASSIGN_2).replaceFirst("\\{\\}", oldname).replaceFirst("\\{\\}", ""+user_id).replaceFirst("\\{\\}", newname).replace("{}", nickname), Channel.LOG.getType());
										Azrael.SQLInsertActionLog("MEMBER_NICKNAME_UPDATE", e.getUser().getIdLong(), guild.getIdLong(), newname);
										//Run google service, if enabled
										if(GuildIni.getGoogleFunctionalitiesEnabled(guild.getIdLong()) && GuildIni.getGoogleSpreadsheetsEnabled(guild.getIdLong())) {
											GoogleUtils.handleSpreadsheetRequest(Azrael.SQLgetGoogleFilesAndEvent(guild.getIdLong(), 2, GoogleEvent.RENAME.id, ""), guild, "", ""+user_id, new Timestamp(System.currentTimeMillis()), e.getUser().getName()+"#"+e.getUser().getDiscriminator(), null, guild.getSelfMember().getUser().getName()+"#"+guild.getSelfMember().getUser().getDiscriminator(), guild.getSelfMember().getEffectiveName(), STATIC.getTranslation2(guild, Translation.NAME_REASON), null, null, "RENAMED", null, null, null, oldname, newname, 0, null, null, 0, 0, GoogleEvent.RENAME.id);
										}
									}
									else {
										STATIC.writeToRemoteChannel(guild, message.setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_PERMISSIONS)).setColor(Color.RED), STATIC.getTranslation2(guild, Translation.NAME_ASSIGN_ERR_2).replaceFirst("\\{\\}", oldname).replaceFirst("\\{\\}", ""+user_id).replace("{}", newname)+Permission.NICKNAME_MANAGE.getName(), Channel.LOG.getType());
										logger.warn("Lacking MANAGE NICKNAME permission in guild {}", guild.getId());
									}
								}
								else {
									//verify if the bot has the permission to kick users
									if(guild.getSelfMember().hasPermission(Permission.KICK_MEMBERS)) {
										//send a private message and then kick the user
										Hashes.addTempCache("kick-ignore_gu"+guild.getId()+"us"+e.getUser().getId(), new Cache(60000));
										member.getUser().openPrivateChannel().queue(channel -> {
											channel.sendMessage(STATIC.getTranslation2(guild, Translation.NAME_KICK_DM).replaceFirst("\\{\\}", guild.getName()).replace("{}", word.getName().toUpperCase())).queue(success -> {
												guild.kick(member).reason(STATIC.getTranslation2(guild, Translation.NAME_KICK_REASON).replace("{}", word.getName().toUpperCase())).queue();
												message.setColor(Color.RED).setThumbnail(IniFileReader.getCaughtThumbnail()).setTitle(STATIC.getTranslation2(guild, Translation.NAME_KICK_TITLE));
												STATIC.writeToRemoteChannel(guild, message, STATIC.getTranslation2(guild, Translation.NAME_KICK_MESSAGE_3).replaceFirst("\\{\\}", oldname).replaceFirst("\\{\\}", ""+user_id).replaceFirst("\\{\\}", newname).replace("{}", word.getName().toUpperCase()), Channel.LOG.getType());
											}, error -> {
												guild.kick(member).reason(STATIC.getTranslation2(guild, Translation.NAME_KICK_REASON).replace("{}", word.getName().toUpperCase())).queue();
												message.setColor(Color.RED).setThumbnail(IniFileReader.getCaughtThumbnail()).setTitle(STATIC.getTranslation2(guild, Translation.NAME_KICK_TITLE));
												STATIC.writeToRemoteChannel(guild, message, STATIC.getTranslation2(guild, Translation.NAME_KICK_MESSAGE_4).replaceFirst("\\{\\}", oldname).replaceFirst("\\{\\}", ""+user_id).replaceFirst("\\{\\}", newname).replace("{}", word.getName().toUpperCase()), Channel.LOG.getType());
											});
										});
										Azrael.SQLInsertHistory(e.getUser().getIdLong(), guild.getIdLong(), "kick", STATIC.getTranslation2(guild, Translation.NAME_KICK_REASON).replace("{}", word.getName().toUpperCase()), 0, "");
										//Run google service, if enabled
										if(GuildIni.getGoogleFunctionalitiesEnabled(guild.getIdLong()) && GuildIni.getGoogleSpreadsheetsEnabled(guild.getIdLong())) {
											GoogleUtils.handleSpreadsheetRequest(Azrael.SQLgetGoogleFilesAndEvent(guild.getIdLong(), 2, GoogleEvent.KICK.id, ""), guild, "", ""+user_id, new Timestamp(System.currentTimeMillis()), e.getUser().getName()+"#"+e.getUser().getDiscriminator(), member.getEffectiveName(), guild.getSelfMember().getUser().getName()+"#"+guild.getSelfMember().getUser().getDiscriminator(), guild.getSelfMember().getEffectiveName(), STATIC.getTranslation2(guild, Translation.NAME_KICK_REASON).replace("{}", word.getName().toUpperCase()), null, null, "KICK", null, null, null, null, null, 0, null, null, 0, 0, GoogleEvent.KICK.id);
										}
									}
									else {
										message.setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_PERMISSIONS));
										STATIC.writeToRemoteChannel(guild, message, STATIC.getTranslation2(guild, Translation.NAME_KICK_PERMISSION_ERR_2).replaceFirst("\\{\\}", oldname).replaceFirst("\\{\\}", ""+user_id).replaceFirst("\\{\\}", newname).replace("{}", word.getName().toUpperCase())+Permission.KICK_MEMBERS.getName(), Channel.LOG.getType());
										logger.warn("Lacking KICK MEMBERS permission in guild {}", guild.getId());
									}
								}
							}
						}
					}
				}
			});
		}).start();
	}
	
	//update Azrael.nickname table with the newly assigned nickname
	private void updateNickname(Member member, String nickname) {
		final var user_id = member.getUser().getIdLong();
		final var guild_id = member.getGuild().getIdLong();
		if(nickname != null && Azrael.SQLgetNickname(user_id, guild_id).length() > 0) {
			if(Azrael.SQLUpdateNickname(user_id, guild_id, nickname) == 0) {
				logger.error("User nickname of {} couldn't be updated in Azrael.nickname", user_id);
			}
			else {
				logger.debug("{} received the nickname {} in guild {}", user_id, nickname, guild_id);
				Azrael.SQLInsertActionLog("MEMBER_NICKNAME_UPDATE", user_id, guild_id, nickname);
			}
		}
		else if(nickname != null) {
			if(Azrael.SQLInsertNickname(user_id, guild_id, nickname) == 0) {
				logger.error("User nickname of {} couldn't be inserted into Azrael.nickname", user_id);
			}
			else {
				logger.debug("{} received the nickname {} in guild {}", user_id, nickname, guild_id);
				Azrael.SQLInsertActionLog("MEMBER_NICKNAME_UPDATE", user_id, guild_id, nickname);
			}
		}
	}
}
