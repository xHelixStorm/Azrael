package listeners;

import java.awt.Color;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import core.UserPrivs;
import enums.GoogleEvent;
import enums.Translation;
import fileManagement.GuildIni;
import google.GoogleSheets;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;
import sql.Azrael;
import sql.DiscordRoles;
import util.STATIC;

/**
 * This class gets executed when a message has been removed.
 * 
 * For this bot, there are many ways to remove a message. for
 * example by the word or url filter, with H!user delete-messages
 * or manual message deletions. In this class all deleted messages
 * will get printed with the message that was deleted and with the 
 * user who deleted the message, depending on the delete type.
 * @author xHelixStorm
 * 
 */

public class GuildMessageRemovedListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(GuildMessageRemovedListener.class);
	private final static long T5MINUTES = 300;
	
	@Override
	public void onGuildMessageDelete(GuildMessageDeleteEvent e) {
		new Thread(() -> {
			final EmbedBuilder message = new EmbedBuilder();
			//remove reaction messages from db
			DiscordRoles.SQLDeleteReactions(e.getMessageIdLong());
			//verify that the message in cache logger is enabled
			if(GuildIni.getCacheLog(e.getGuild().getIdLong())) {
				//retrieve current message id, the message as a whole that got deleted and delete the message from cache
				long message_id = e.getMessageIdLong();
				final var removed_messages = Hashes.getMessagePool(message_id);
				
				//be sure that the removed message has been cached, else it won't make sense to display a message which the bot doesn't know about
				//reason can be that either the message is too old or all messages that occurred before a reboot aren't known
				if(removed_messages != null) {
					final var firstMessage = removed_messages.get(0);
					Hashes.removeMessagePool(message_id);
					//verify that the bot has permission to view the audit log, else throw an error
					if(e.getGuild().getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS)) {
						//be sure that the message didn't get removed by a language filter, if yes ignore the message
						var cache = Hashes.getTempCache("message-removed-filter_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+firstMessage.getUserID());
						if(cache == null) {
							//be sure that the message didn't get removed from a channel where text input is not allowed
							cache = Hashes.getTempCache("message-removed_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+firstMessage.getUserID());
							if(cache == null) {
								long trigger_user_id = 0;
								String trigger_user_name = "";
								long removed_from = 0;
								boolean send_message = false;
								boolean suppress_deleted = false;
								var counter = 0;
								//retrieve the audit log for deleted messages
								AuditLogPaginationAction logs = e.getGuild().retrieveAuditLogs().type(ActionType.MESSAGE_DELETE);
								for (AuditLogEntry entry : logs)
								{
									//only execute if the current action log hasn't been read before and that the user id of the removed message is the same from the audit log. Also verify that the audit log isn't older than the Bot boot
									final long entryCreated = entry.getTimeCreated().toEpochSecond();
									//retrieve the boot time considering the offset and daylight saving
									final long bootTime = STATIC.getBootTime().toEpochSecond()-(TimeZone.getDefault().useDaylightTime() ? (TimeZone.getDefault().getRawOffset()/1000)+3600 : TimeZone.getDefault().getRawOffset()/1000);
									//System.currentTimeMilis() doesn't consider offset and daylight saving. Hence no convertion required
									if(!Hashes.containsActionlog(entry.getId()+entry.getOptionByName("count")) && (firstMessage.getUserID() == entry.getTargetIdLong() || firstMessage.getUserID() == 0) && entryCreated > bootTime && ((entryCreated - (System.currentTimeMillis())/1000) * -1) < T5MINUTES) {
										//add action log a read and allow a message to be printed afterwards
										Hashes.addActionlog(entry.getId()+entry.getOptionByName("count"));
										send_message = true;
										//confirm that the event and audit log both point to the same text channel from where the message got removed and allow messages to be displayed
										//which have been deleted by an administrator or moderator
										if(e.getChannel().getId().equals(entry.getOptionByName("channel_id").toString()) && (UserPrivs.isUserAdmin(e.getGuild().getMemberById(entry.getUser().getId())) || UserPrivs.isUserMod(e.getGuild().getMemberById(entry.getUser().getId())))) {
											removed_from = entry.getTargetIdLong();
											//be sure to avoid printing a message which got deleted from a bot or by a bot
											Member member = e.getGuild().getMemberById(removed_from);
											if((member != null && !member.getUser().isBot() && !UserPrivs.isUserBot(member)) || firstMessage.getUserID()  == 0) {
												trigger_user_id = entry.getUser().getIdLong();
												trigger_user_name = entry.getUser().getName()+"#"+entry.getUser().getDiscriminator();
												break;
											}
											else if(removed_from != e.getJDA().getSelfUser().getIdLong() && member != null && UserPrivs.isUserBot(member)) {
												suppress_deleted = true;
												break;
											}
										}
									}
									//read the newest 3 audit logs and then stop
									counter ++;
									if(counter == 3)
										break;
								}
								
								//print any deleted message which got removed by an admin or moderator
								if(firstMessage.getUserID() != 0 && send_message == true && removed_from != 0 && trigger_user_id != removed_from) {
									//confirm that we have a message to print and a user who deleted the message
									if(firstMessage.getMessage().length() > 0 && trigger_user_id > 0) {
										suppress_deleted = true;
										//retrieve the trash channel to print the removed message
										var tra_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("tra")).findAny().orElse(null);
										if(tra_channel != null) {
											message.setColor(Color.CYAN);
											//iterate through removed_messages to print the main message and if available, all edited messages belonging to the same message id
											for(final var cachedMessage : removed_messages) {
												message.setTimestamp(cachedMessage.getTime()).setTitle(cachedMessage.getUserName()+" ("+cachedMessage.getUserID()+")").setFooter(e.getChannel().getName()+" ("+e.getChannel().getId()+")");
												final var printMessage = (cachedMessage.isEdit() ? STATIC.getTranslation2(e.getGuild(), Translation.DELETE_EDITED_MESSAGE) : STATIC.getTranslation2(e.getGuild(), Translation.DELETE_MESSAGE))+STATIC.getTranslation2(e.getGuild(), Translation.DELETE_REMOVED_BY)+trigger_user_name+"\n\n"+cachedMessage.getMessage();
												e.getGuild().getTextChannelById(tra_channel.getChannel_ID()).sendMessage(message.setDescription((printMessage.length() <= 2048 ? printMessage : printMessage.substring(0, 2040)+"...")).build()).queue();
											}
										}
									}
								}
								//print removed tweet messages which were removed by an admin or moderator
								else if(firstMessage.getUserID() == 0) {
									//confirm that we have a message to print and a user who deleted the message
									if(firstMessage.getMessage().length() > 0 && trigger_user_id > 0) {
										suppress_deleted = true;
										//retrieve the trash channel to print the removed message
										var tra_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("tra")).findAny().orElse(null);
										if(tra_channel != null) {
											message.setColor(Color.CYAN);
											//print the message
											message.setTimestamp(firstMessage.getTime()).setTitle(firstMessage.getUserName()).setFooter(e.getChannel().getName()+" ("+e.getChannel().getId()+")");
											final var printMessage = STATIC.getTranslation2(e.getGuild(), Translation.DELETE_TWEET)+STATIC.getTranslation2(e.getGuild(), Translation.DELETE_REMOVED_BY)+trigger_user_name+"\n\n"+firstMessage.getMessage();
											e.getGuild().getTextChannelById(tra_channel.getChannel_ID()).sendMessage(message.setDescription((printMessage.length() <= 2048 ? printMessage : printMessage.substring(0, 2040)+"...")).build()).queue(m -> {
												//mark tweet as deleted
												Azrael.SQLUpdateTweetLogDeleted(message_id);
											});
										}
									}
								}
								//if enabled, display all deleted messages which weren't deleted by someone else but from the same user and still isn't a bot
								else if(GuildIni.getSelfDeletedMessage(e.getGuild().getIdLong()) && firstMessage.getUserID() != 0 && !suppress_deleted && !UserPrivs.isUserBot(e.getGuild().getMemberById(firstMessage.getUserID()))) {
									//confirm that we have a message to print
									if(firstMessage.getMessage().length() > 0) {
										//look up for a registered trash and delete channel. If a delete channel has been found, this channel should be used instead of the trash channel
										var traAndDel_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && (f.getChannel_Type().equals("tra") || f.getChannel_Type().equals("del"))).collect(Collectors.toList());
										var tra_channel = traAndDel_channel.parallelStream().filter(f -> f.getChannel_Type().equals("tra")).findAny().orElse(null);
										var del_channel = traAndDel_channel.parallelStream().filter(f -> f.getChannel_Type().equals("del")).findAny().orElse(null);
										if(tra_channel != null || del_channel != null) {
											message.setColor(Color.GRAY);
											//iterate through removed_messages to print the main message and if available, all edited messages belonging to the same message id
											for(final var cachedMessage : removed_messages) {
												message.setTimestamp(cachedMessage.getTime()).setTitle(cachedMessage.getUserName()+" ("+cachedMessage.getUserID()+")").setFooter(e.getChannel().getName()+" ("+e.getChannel().getId()+")");
												final var printMessage = STATIC.getTranslation2(e.getGuild(), Translation.DELETE_SELF)+(cachedMessage.isEdit() ? STATIC.getTranslation2(e.getGuild(), Translation.DELETE_EDITED_MESSAGE) : STATIC.getTranslation2(e.getGuild(), Translation.DELETE_MESSAGE))+"\n\n"+cachedMessage.getMessage();
												e.getGuild().getTextChannelById((del_channel != null ? del_channel.getChannel_ID() : tra_channel.getChannel_ID())).sendMessage(message.setDescription((printMessage.length() <= 2048 ? printMessage : printMessage.substring(0, 2040)+"...")).build()).queue();
											}
										}
									}
								}
							}
							//log messages which were removed from a channel that doesn't allow text input as long there is a message to print
							else if(firstMessage.getMessage().length() > 0) {
								//retrieve the trash channel to print the removed message
								var tra_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("tra")).findAny().orElse(null);
								if(tra_channel != null) {
									message.setColor(Color.ORANGE);
									//iterate through removed_messages to print the main message and if available, all edited messages belonging to the same message id
									for(final var cachedMessage : removed_messages) {
										message.setTimestamp(cachedMessage.getTime()).setTitle(cachedMessage.getUserName()+" ("+cachedMessage.getUserID()+")").setFooter(e.getChannel().getName()+" ("+e.getChannel().getId()+")");
										final var printMessage = (cachedMessage.isEdit() ? STATIC.getTranslation2(e.getGuild(), Translation.DELETE_EDITED_MESSAGE) : STATIC.getTranslation2(e.getGuild(), Translation.DELETE_MESSAGE))+"\n\n"+cachedMessage.getMessage();
										e.getGuild().getTextChannelById(tra_channel.getChannel_ID()).sendMessage(message.setDescription((printMessage.length() <= 2048 ? printMessage : printMessage.substring(0, 2040)+"...")).build()).queue();
									}
								}
								Hashes.clearTempCache("message-removed_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+firstMessage.getUserID());
							}
						}
						else {
							Hashes.clearTempCache("message-removed-filter_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+firstMessage.getUserID());
						}
					}
					else {
						var tra_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("tra")).findAny().orElse(null);
						if(tra_channel != null) e.getGuild().getTextChannelById(tra_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation2(e.getGuild(), Translation.DELETE_PERMISSION_ERR)+Permission.VIEW_AUDIT_LOGS.getName()).build()).queue();
						logger.warn("VIEW AUDIT LOG permission missing in guild {}!", e.getGuild().getId());
					}
					
					//Log additional removed messages from users that are being watched with watch level 1
					var watchedUser = Azrael.SQLgetWatchlist(firstMessage.getUserID(), e.getGuild().getIdLong());
					if(watchedUser != null && watchedUser.getLevel() == 1) {
						message.setColor(Color.DARK_GRAY);
						//iterate through removed_messages to print the main message and if available, all edited messages belonging to the same message id
						for(final var cachedMessage : removed_messages) {
							message.setTimestamp(cachedMessage.getTime()).setTitle(cachedMessage.getUserName()+" ("+cachedMessage.getUserID()+")").setFooter(e.getChannel().getName()+" ("+e.getChannel().getId()+")");
							final var printMessage = STATIC.getTranslation2(e.getGuild(), Translation.DELETE_WATCHED)+(cachedMessage.isEdit() ? STATIC.getTranslation2(e.getGuild(), Translation.DELETE_EDITED_MESSAGE) : STATIC.getTranslation2(e.getGuild(), Translation.DELETE_MESSAGE))+"\n\n"+cachedMessage.getMessage();
							e.getGuild().getTextChannelById(watchedUser.getWatchChannel()).sendMessage(message.setDescription((printMessage.length() <= 2048 ? printMessage : printMessage.substring(0, 2040)+"...")).build()).queue();
						}
					}
				}
			}
			//Run google service if enabled
			if(GuildIni.getGoogleFunctionalitiesEnabled(e.getGuild().getIdLong()) && GuildIni.getGoogleSpreadsheetsEnabled(e.getGuild().getIdLong())) {
				if(Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong() && f.getChannel_Type() != null && f.getChannel_Type().equals("vot")).findAny().orElse(null) != null) {
					final String [] sheet = Azrael.SQLgetGoogleFilesAndEvent(e.getGuild().getIdLong(), 2, GoogleEvent.VOTE.id);
					if(sheet != null && !sheet[0].equals("empty")) {
						final String file_id = sheet[0];
						final String row_start = sheet[1].replaceAll("![A-Z0-9]*", "");
						try {
							final var service = GoogleSheets.getSheetsClientService();
							final var response = GoogleSheets.readWholeSpreadsheet(service, file_id, row_start);
							int currentRow = 0;
							for(var row : response.getValues()) {
								currentRow++;
								if(row.parallelStream().filter(f -> {
									String cell = (String)f;
									if(cell.equals(e.getMessageId()))
										return true;
									else
										return false;
									}).findAny().orElse(null) != null) {
									STATIC.killThread("vote"+e.getMessageId());
									final var file = GoogleSheets.getSpreadsheet(service, file_id);
									final var retrievedSheet = file.getSheets().parallelStream().filter(f -> f.getProperties().getTitle().equals(row_start)).findAny().orElse(null);
									if(retrievedSheet != null) {
										GoogleSheets.deleteRowOnSpreadsheet(service, file_id, currentRow, retrievedSheet.getProperties().getSheetId());
									}
									break;
								}
							}
						} catch (Exception e1) {
							logger.error("Google Spreadsheet webservice error in guild {}", e.getGuild().getIdLong(), e1);
							final var log_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null); 
							if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation2(e.getGuild(), Translation.GOOGLE_WEBSERVICE)+e1.getMessage()).build()).queue();
						}
					}
				}
			}
		}).start();
	}
}
