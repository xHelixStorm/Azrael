package de.azrael.listeners;

import java.awt.Color;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.sheets.v4.model.DeleteDimensionRequest;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;

import de.azrael.constructors.BotConfigs;
import de.azrael.enums.Channel;
import de.azrael.enums.GoogleEvent;
import de.azrael.enums.Translation;
import de.azrael.google.GoogleSheets;
import de.azrael.google.GoogleUtils;
import de.azrael.sql.Azrael;
import de.azrael.sql.BotConfiguration;
import de.azrael.sql.DiscordRoles;
import de.azrael.threads.DelayedGoogleUpdate;
import de.azrael.util.Hashes;
import de.azrael.util.STATIC;
import de.azrael.util.UserPrivs;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;

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
	public void onMessageDelete(MessageDeleteEvent e) {
		if(e.isFromGuild() && e.getChannelType().equals(ChannelType.TEXT)) {
			new Thread(() -> {
				final EmbedBuilder message = new EmbedBuilder();
				//remove reaction messages from db
				DiscordRoles.SQLDeleteReactions(e.getMessageIdLong());
				BotConfigs botConfig = BotConfiguration.SQLgetBotConfigs(e.getGuild().getIdLong());
				//verify that the message in cache logger is enabled
				if(botConfig.getCacheLog()) {
					//retrieve current message id, the message as a whole that got deleted and delete the message from cache
					long message_id = e.getMessageIdLong();
					final var removed_messages = Hashes.getMessagePool(e.getGuild().getIdLong(), message_id);
					
					//be sure that the removed message has been cached, else it won't make sense to display a message which the bot doesn't know about
					//reason can be that either the message is too old or all messages that occurred before a reboot aren't known
					if(removed_messages != null) {
						final var firstMessage = removed_messages.get(0);
						Hashes.removeMessagePool(e.getGuild().getIdLong(), message_id);
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
										final long bootTime = STATIC.getBootTime().toEpochSecond()-(TimeZone.getDefault().useDaylightTime() ? (Calendar.ZONE_OFFSET/1000) : 0);
										//System.currentTimeMilis() doesn't consider offset and daylight saving. Hence no convertion required
										if(!Hashes.containsActionlog(entry.getId()+entry.getOptionByName("count")) && (firstMessage.getUserID() == entry.getTargetIdLong() || firstMessage.getUserID() == 0) && entryCreated > bootTime && ((entryCreated - (System.currentTimeMillis())/1000) * -1) < T5MINUTES) {
											//add action log a read and allow a message to be printed afterwards
											Hashes.addActionlog(entry.getId()+entry.getOptionByName("count"));
											send_message = true;
											//confirm that the event and audit log both point to the same text channel from where the message got removed and allow messages to be displayed
											//which have been deleted by an administrator or moderator
											Member entryMember = e.getGuild().getMemberById(entry.getUser().getIdLong());
											if(e.getChannel().getId().equals(entry.getOptionByName("channel_id").toString()) && (UserPrivs.isUserAdmin(entryMember) || UserPrivs.isUserMod(entryMember))) {
												removed_from = entry.getTargetIdLong();
												//be sure to avoid printing a message which got deleted from a bot or by a bot. Also print a deleted message, if the user has left the server
												Member member = e.getGuild().getMemberById(removed_from);
												if(member == null || !firstMessage.isUserBot() || firstMessage.getUserID()  == 0) {
													trigger_user_id = entry.getUser().getIdLong();
													trigger_user_name = entry.getUser().getName()+"#"+entry.getUser().getDiscriminator();
													break;
												}
												else if(removed_from != e.getJDA().getSelfUser().getIdLong()) {
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
											message.setColor(Color.CYAN);
											//iterate through removed_messages to print the main message and if available, all edited messages belonging to the same message id
											for(final var cachedMessage : removed_messages) {
												message.setTimestamp(cachedMessage.getTime()).setTitle(cachedMessage.getUserName()+" ("+cachedMessage.getUserID()+")").setFooter(e.getChannel().getName()+" ("+e.getChannel().getId()+")");
												final var printMessage = (cachedMessage.isEdit() ? STATIC.getTranslation2(e.getGuild(), Translation.DELETE_EDITED_MESSAGE) : STATIC.getTranslation2(e.getGuild(), Translation.DELETE_MESSAGE))+STATIC.getTranslation2(e.getGuild(), Translation.DELETE_REMOVED_BY)+trigger_user_name+"\n\n"+cachedMessage.getMessage();
												final boolean result = STATIC.writeToRemoteChannel(e.getGuild(), message, (printMessage.length() <= 2048 ? printMessage : printMessage.substring(0, 2040)+"..."), Channel.TRA.getType());
												if(!result)
													break;
											}
										}
									}
									//print removed subscription messages which were removed by an administrator or moderator
									else if(firstMessage.getUserID() == 0) {
										//confirm that we have a message to print and a user who deleted the message
										if(firstMessage.getMessage().length() > 0 && trigger_user_id > 0) {
											suppress_deleted = true;
											message.setColor(Color.CYAN);
											//print the message
											message.setTimestamp(firstMessage.getTime()).setTitle(firstMessage.getUserName()).setFooter(e.getChannel().getName()+" ("+e.getChannel().getId()+")");
											final var printMessage = STATIC.getTranslation2(e.getGuild(), Translation.DELETE_SUBSCRIPTION)+STATIC.getTranslation2(e.getGuild(), Translation.DELETE_REMOVED_BY)+trigger_user_name+"\n\n"+firstMessage.getMessage();
											STATIC.writeToRemoteChannel(e.getGuild(), message, (printMessage.length() <= 2048 ? printMessage : printMessage.substring(0, 2040)+"..."), Channel.TRA.getType());
											//mark tweet as deleted
											Azrael.SQLUpdateSubscriptionLogDeleted(message_id);
										}
									}
									//if enabled, display all deleted messages which weren't deleted by someone else but from the same user and still isn't a bot
									else if(botConfig.getSelfDeletedMessages() && firstMessage.getUserID() != 0 && !suppress_deleted && !firstMessage.isUserBot()) {
										//confirm that we have a message to print
										if(firstMessage.getMessage().length() > 0) {
											message.setColor(Color.GRAY);
											//iterate through removed_messages to print the main message and if available, all edited messages belonging to the same message id
											for(final var cachedMessage : removed_messages) {
												message.setTimestamp(cachedMessage.getTime()).setTitle(cachedMessage.getUserName()+" ("+cachedMessage.getUserID()+")").setFooter(e.getChannel().getName()+" ("+e.getChannel().getId()+")");
												final var printMessage = STATIC.getTranslation2(e.getGuild(), Translation.DELETE_SELF)+(cachedMessage.isEdit() ? STATIC.getTranslation2(e.getGuild(), Translation.DELETE_EDITED_MESSAGE) : STATIC.getTranslation2(e.getGuild(), Translation.DELETE_MESSAGE))+"\n\n"+cachedMessage.getMessage();
												final boolean result = STATIC.writeToRemoteChannel(e.getGuild(), message, (printMessage.length() <= 2048 ? printMessage : printMessage.substring(0, 2040)+"..."), Channel.DEL.getType(), Channel.TRA.getType());
												if(!result)
													break;
											}
										}
									}
								}
								//log messages which were removed from a channel that doesn't allow text input as long there is a message to print
								else if(firstMessage.getMessage().length() > 0) {
									message.setColor(Color.ORANGE);
									//iterate through removed_messages to print the main message and if available, all edited messages belonging to the same message id
									for(final var cachedMessage : removed_messages) {
										message.setTimestamp(cachedMessage.getTime()).setTitle(cachedMessage.getUserName()+" ("+cachedMessage.getUserID()+")").setFooter(e.getChannel().getName()+" ("+e.getChannel().getId()+")");
										final var printMessage = (cachedMessage.isEdit() ? STATIC.getTranslation2(e.getGuild(), Translation.DELETE_EDITED_MESSAGE) : STATIC.getTranslation2(e.getGuild(), Translation.DELETE_MESSAGE))+"\n\n"+cachedMessage.getMessage();
										final boolean result = STATIC.writeToRemoteChannel(e.getGuild(), message, (printMessage.length() <= 2048 ? printMessage : printMessage.substring(0, 2040)+"..."), Channel.TRA.getType());
										if(!result)
											break;
									}
									Hashes.clearTempCache("message-removed_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+firstMessage.getUserID());
								}
							}
							else {
								Hashes.clearTempCache("message-removed-filter_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+firstMessage.getUserID());
							}
						}
						else {
							STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.DELETE_PERMISSION_ERR)+Permission.VIEW_AUDIT_LOGS.getName(), Channel.TRA.getType());
							logger.warn("VIEW AUDIT LOG permission required to retrieve the user who deleted a message in guild {}", e.getGuild().getId());
						}
						
						//Log additional removed messages from users that are being watched with watch level 1
						var watchedUser = Azrael.SQLgetWatchlist(firstMessage.getUserID(), e.getGuild().getIdLong());
						if(watchedUser != null && watchedUser.getLevel() == 1) {
							message.setColor(Color.DARK_GRAY);
							final TextChannel textChannel = e.getGuild().getTextChannelById(watchedUser.getWatchChannel());
							if(textChannel != null) {
								if(e.getGuild().getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS) || STATIC.setPermissions(e.getGuild(), textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS))) {
									//iterate through removed_messages to print the main message and if available, all edited messages belonging to the same message id
									for(final var cachedMessage : removed_messages) {
										message.setTimestamp(cachedMessage.getTime()).setTitle(cachedMessage.getUserName()+" ("+cachedMessage.getUserID()+")").setFooter(e.getChannel().getName()+" ("+e.getChannel().getId()+")");
										final var printMessage = STATIC.getTranslation2(e.getGuild(), Translation.DELETE_WATCHED)+(cachedMessage.isEdit() ? STATIC.getTranslation2(e.getGuild(), Translation.DELETE_EDITED_MESSAGE) : STATIC.getTranslation2(e.getGuild(), Translation.DELETE_MESSAGE))+"\n\n"+cachedMessage.getMessage();
										textChannel.sendMessageEmbeds(message.setDescription((printMessage.length() <= 2048 ? printMessage : printMessage.substring(0, 2040)+"...")).build()).queue();
									}
								}
								else {
									STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MESSAGE_SEND.getName()+" and "+Permission.MESSAGE_EMBED_LINKS.getName())+textChannel.getAsMention(), Channel.LOG.getType());
									logger.error("MESSAGE_WRITE and MESSAGE_EMBED_LINKS permissions required to display the message of a watched member on channel {} in guild {}", textChannel.getId(), e.getGuild().getId());
								}
							}
						}
					}
				}
				
				//Run google service if enabled
				if(botConfig.getGoogleFunctionalities()) {
					runVoteSpreadsheetService(e);
					runCommentSpreadsheetService(e);
					
				}
			}).start();
		}
	}
	
	private static void runVoteSpreadsheetService(MessageDeleteEvent e) {
		if(e.isFromGuild() && e.getChannelType().equals(ChannelType.TEXT)) {
			if(Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong() && f.getChannel_Type() != null && (f.getChannel_Type().equals(Channel.VOT.getType()) || f.getChannel_Type().equals(Channel.VO2.getType()))).findAny().orElse(null) != null) {
				final String [] sheet = Azrael.SQLgetGoogleFilesAndEvent(e.getGuild().getIdLong(), 2, GoogleEvent.VOTE.id, e.getChannel().getId());
				if(sheet != null && !sheet[0].equals("empty")) {
					final String file_id = sheet[0];
					final String row_start = sheet[1].replaceAll("![A-Z0-9]*", "");
					if((sheet[2] == null || sheet[2].length() == 0) || sheet[2].equals(e.getChannel().getId())) {
						try {
							ValueRange response = DelayedGoogleUpdate.getCachedValueRange("VOTE"+e.getGuild().getId()+e.getChannel().getId());
							if(response == null) {
								final var service = GoogleSheets.getSheetsClientService();
								response = GoogleSheets.readWholeSpreadsheet(service, file_id, row_start);
								DelayedGoogleUpdate.cacheRetrievedSheetValueRange("VOTE"+e.getGuild().getId()+e.getChannel().getId(), response);
							}
							int currentRow = 0;
							boolean rowFound = false;
							for(var row : response.getValues()) {
								currentRow++;
								if(row.parallelStream().filter(f -> {
									String cell = (String)f;
									if(cell.equals(e.getMessageId()))
										return true;
									else
										return false;
									}).findAny().orElse(null) != null) {
									rowFound = true;
									Spreadsheet file = Hashes.getSpreadsheetProperty(file_id);
									if(file == null) {
										file = GoogleSheets.getSpreadsheet(GoogleSheets.getSheetsClientService(), file_id);
										Hashes.addSpreadsheetProperty(file_id, file);
									}
									final var retrievedSheet = file.getSheets().parallelStream().filter(f -> f.getProperties().getTitle().equals(row_start)).findAny().orElse(null);
									if(retrievedSheet != null) {
										ValueRange valueRange = new ValueRange().setValues(Arrays.asList(Arrays.asList(
											new Request().setDeleteDimension(new DeleteDimensionRequest().setRange(new DimensionRange().setDimension("ROWS").setSheetId(retrievedSheet.getProperties().getSheetId()).setStartIndex(currentRow-1).setEndIndex(currentRow)))
										)));
										//execute Runnable
										if(!STATIC.threadExists("VOTE"+e.getGuild().getId()+e.getChannel().getId())) {
											new Thread(new DelayedGoogleUpdate(e.getGuild(), valueRange, e.getMessageIdLong(), file_id, e.getChannel().getId(), "remove", GoogleEvent.VOTE)).start();
										}
										else {
											DelayedGoogleUpdate.handleAdditionalRequest(e.getGuild(), e.getChannel().getId(), valueRange, e.getMessageIdLong(), "remove");
										}
									}
									break;
								}
							}
							if(!rowFound) {
								DelayedGoogleUpdate.handleRequestRemoval(e.getGuild().getId()+"_"+e.getChannel().getId()+"_"+e.getMessageId());
							}
						} catch(SocketTimeoutException e1) {
							if(GoogleUtils.timeoutHandler(e.getGuild(), file_id, GoogleEvent.VOTE.name(), e1)) {
								runVoteSpreadsheetService(e);
							}
						} catch (Exception e1) {
							STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED), STATIC.getTranslation2(e.getGuild(), Translation.GOOGLE_WEBSERVICE)+e1.getMessage(), Channel.LOG.getType());
							logger.error("Google Spreadsheet webservice error for event VOTE in guild {}", e.getGuild().getIdLong(), e1);
						}
					}
				}
			}
		}
	}
	
	private static void runCommentSpreadsheetService(MessageDeleteEvent e) {
		if(e.isFromGuild() && e.getChannelType().equals(ChannelType.TEXT)) {
			final String [] sheet = Azrael.SQLgetGoogleFilesAndEvent(e.getGuild().getIdLong(), 2, GoogleEvent.COMMENT.id, e.getChannel().getId());
			if(sheet != null && !sheet[0].equals("empty")) {
				final String file_id = sheet[0];
				final String row_start = sheet[1].replaceAll("![A-Z0-9]*", "");
				if((sheet[2] == null || sheet[2].length() == 0) || sheet[2].equals(e.getChannel().getId())) {
					try {
						ValueRange response = DelayedGoogleUpdate.getCachedValueRange("COMMENT"+e.getGuild().getId()+e.getChannel().getId());
						if(response == null) {
							final var service = GoogleSheets.getSheetsClientService();
							response = GoogleSheets.readWholeSpreadsheet(service, file_id, row_start);
							DelayedGoogleUpdate.cacheRetrievedSheetValueRange("COMMENT"+e.getGuild().getId()+e.getChannel().getId(), response);
						}
						int currentRow = 0;
						boolean rowFound = false;
						for(var row : response.getValues()) {
							currentRow++;
							if(row.parallelStream().filter(f -> {
								String cell = (String)f;
								if(cell.equals(e.getMessageId()))
									return true;
								else
									return false;
								}).findAny().orElse(null) != null) {
								rowFound = true;
								Spreadsheet file = Hashes.getSpreadsheetProperty(file_id);
								if(file == null) {
									file = GoogleSheets.getSpreadsheet(GoogleSheets.getSheetsClientService(), file_id);
									Hashes.addSpreadsheetProperty(file_id, file);
								}
								final var retrievedSheet = file.getSheets().parallelStream().filter(f -> f.getProperties().getTitle().equals(row_start)).findAny().orElse(null);
								if(retrievedSheet != null) {
									ValueRange valueRange = new ValueRange().setValues(Arrays.asList(Arrays.asList(
										new Request().setDeleteDimension(new DeleteDimensionRequest().setRange(new DimensionRange().setDimension("ROWS").setSheetId(retrievedSheet.getProperties().getSheetId()).setStartIndex(currentRow-1).setEndIndex(currentRow)))
									)));
									//execute Runnable
									if(!STATIC.threadExists("COMMENT"+e.getGuild().getId()+e.getChannel().getId())) {
										new Thread(new DelayedGoogleUpdate(e.getGuild(), valueRange, e.getMessageIdLong(), file_id, e.getChannel().getId(), "remove", GoogleEvent.COMMENT)).start();
									}
									else {
										DelayedGoogleUpdate.handleAdditionalRequest(e.getGuild(), e.getChannel().getId(), valueRange, e.getMessageIdLong(), "remove");
									}
								}
								break;
							}
						}
						if(!rowFound) {
							DelayedGoogleUpdate.handleRequestRemoval(e.getGuild().getId()+"_"+e.getChannel().getId()+"_"+e.getMessageId());
						}
					} catch(SocketTimeoutException e1) {
						if(GoogleUtils.timeoutHandler(e.getGuild(), file_id, GoogleEvent.COMMENT.name(), e1)) {
							runCommentSpreadsheetService(e);
						}
					} catch (Exception e1) {
						STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED), STATIC.getTranslation2(e.getGuild(), Translation.GOOGLE_WEBSERVICE)+e1.getMessage(), Channel.LOG.getType());
						logger.error("Google Spreadsheet webservice error for event COMMENT in guild {}", e.getGuild().getIdLong(), e1);
					}
				}
			}
		}
	}
}
