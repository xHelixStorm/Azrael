package listeners;

import java.awt.Color;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiManager;

import constructors.Messages;
import core.Hashes;
import core.UserPrivs;
import enums.Channel;
import enums.GoogleDD;
import enums.GoogleEvent;
import enums.Translation;
import fileManagement.FileSetting;
import fileManagement.GuildIni;
import filter.LanguageEditFilter;
import filter.URLFilter;
import google.GoogleSheets;
import google.GoogleUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.Azrael;
import util.STATIC;

/**
 * this class gets executed when an already printed message
 * gets edited.
 * 
 * after a message has been edited, the new message will be 
 * compared with the filters if languages to filter have
 * been applied or url censoring for that channel has been
 * enabled. Additionally, the edited messages can be added
 * to the system cache and written to file
 * @author xHelixStorm
 * 
 */

public class GuildMessageEditListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(GuildMessageEditListener.class);
	
	@Override
	public void onGuildMessageUpdate(GuildMessageUpdateEvent e) {
		new Thread(() -> {
			long channel_id = e.getChannel().getIdLong();
			final var allChannels = Azrael.SQLgetChannels(e.getGuild().getIdLong());
			//execute only one thread at the same time
			ExecutorService executor = Executors.newSingleThreadExecutor();
			//check if the edited message has been already checked, in case if it's the same message like before an edit
			var messages = Hashes.getMessagePool(e.getGuild().getIdLong(), e.getMessageIdLong());
			var sameMessage = false;
			if(messages != null) {
				if(messages.parallelStream().filter(f -> f.getMessage().equalsIgnoreCase(e.getMessage().getContentRaw())).findAny().orElse(null) != null)
					sameMessage = true;
			}
			if(messages == null || sameMessage == false) {
				executor.execute(() -> {
					//collect all filter languages for the current channel
					var filter_lang = Azrael.SQLgetChannel_Filter(channel_id);
					if(filter_lang != null && filter_lang.size() > 0) {
						if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_MANAGE) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_MANAGE))) {
							//run the word filter, if languages have been found for edited messages
							new Thread(new LanguageEditFilter(e, filter_lang, allChannels)).start();
							//if url censoring is allowed, execute that filter as well
							if(allChannels.parallelStream().filter(f -> f.getChannel_ID() == channel_id && f.getURLCensoring()).findAny().orElse(null) != null)
								new Thread(new URLFilter(null, e, filter_lang, allChannels)).start();
						}
						else {
							STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MESSAGE_WRITE.getName()+" and "+Permission.MESSAGE_MANAGE.getName())+e.getChannel().getName(), Channel.LOG.getType());
							logger.error("MESSAGE_WRITE and MESSAGE_MANAGE permissions required to censor messages inside text channel {} in guild {}", e.getChannel().getId(), e.getGuild().getId());
						}
					}
					//if the url censoring is enabled but no languages to filter have been set, start the url censoring anyway
					else if(allChannels.parallelStream().filter(f -> f.getChannel_ID() == channel_id && f.getURLCensoring()).findAny().orElse(null) != null) {
						if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_MANAGE) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_MANAGE))) {
							new Thread(new URLFilter(null, e, filter_lang, allChannels)).start();
						}
						else {
							STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MESSAGE_WRITE.getName()+" and "+Permission.MESSAGE_MANAGE.getName())+e.getChannel().getName(), Channel.LOG.getType());
							logger.error("MESSAGE_WRITE and MESSAGE_MANAGE permissions required to censor messages inside text channel {} in guild {}", e.getChannel().getId(), e.getGuild().getId());
						}
					}
				});
			}
			
			executor.execute(() -> {
				boolean printEditHistory = false;
				//check if edited messages should be collected and printed in a channel
				if(GuildIni.getEditedMessage(e.getGuild().getIdLong())) {
					//verify if the message history has to be printed and not just the edited message
					if(GuildIni.getEditedMessageHistory(e.getGuild().getIdLong())) {
						printEditHistory = true;
					}
					//print the edited message either in an edit or trash channel
					else {
						STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setAuthor(e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+" ("+e.getMember().getUser().getId()+")").setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EDIT_TITLE))
							, (e.getMessage().getContentRaw().length() <= 2048 ? e.getMessage().getContentRaw() : e.getMessage().getContentRaw().substring(0, 2040)+"...")
							, Channel.EDI.getType(), Channel.TRA.getType());
					}
				}
				
				//check if the channel log and cache log is enabled and if one of the two or bot is/are enabled then write message to file or/and log to system cache
				var log = GuildIni.getChannelAndCacheLog(e.getGuild().getIdLong());
				if((log[0] || log[1]) && !e.getMember().getUser().isBot() && !UserPrivs.isUserBot(e.getMember())) {
					StringBuilder image_url = new StringBuilder();
					for(Attachment attch : e.getMessage().getAttachments()){
						image_url.append((e.getMessage().getContentRaw().length() == 0 && image_url.length() == 0) ? "("+attch.getProxyUrl()+")" : "\n("+attch.getProxyUrl()+")");
					}
					Messages collectedMessage = new Messages();
					collectedMessage.setUserID(e.getMember().getUser().getIdLong());
					collectedMessage.setUsername(e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator());
					collectedMessage.setGuildID(e.getGuild().getIdLong());
					collectedMessage.setChannelID(channel_id);
					collectedMessage.setChannelName(e.getChannel().getName());
					collectedMessage.setMessage(e.getMessage().getContentRaw()+image_url.toString()+"\n");
					collectedMessage.setMessageID(e.getMessageIdLong());
					collectedMessage.setTime(ZonedDateTime.now());
					collectedMessage.setIsEdit(true); // note: flag set to true for edited message
					
					if(log[0]) 	FileSetting.appendFile("./message_log/"+e.getChannel().getId()+".txt", "EDIT ["+collectedMessage.getTime().toString()+" - "+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+" ("+e.getMember().getUser().getId()+")]: "+collectedMessage.getMessage());
					if(log[1]) {
						if(messages != null) {
							messages.add(collectedMessage);
							Hashes.addMessagePool(e.getGuild().getIdLong(), e.getMessageIdLong(), messages);
						}
					}
				}
				
				//if true, print the message history on message edit
				if(printEditHistory) {
					var messageCounter = 0;
					if(messages != null) {
						//print initial and second message, else print only the last message
						if(messages.size() == 2) {
							for(final var message : messages) {
								final var printMessage = message.getMessage();
								boolean result = STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setAuthor(message.getUserName()+" ("+message.getUserID()+")").setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EDIT_TITLE_HISTORY)+(++messageCounter))
										, (printMessage.length() <= 2048 ? printMessage : printMessage.substring(0, 2040)+"...")
										, Channel.EDI.getType(), Channel.TRA.getType());
								if(!result)
									break;
							}
						}
						else {
							int num = messages.size();
							final var message = messages.get(num-1);
							final var printMessage = message.getMessage();
							STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setAuthor(message.getUserName()+" ("+message.getUserID()+")").setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EDIT_TITLE_HISTORY)+num)
									, (printMessage.length() <= 2048 ? printMessage : printMessage.substring(0, 2040)+"...")
									, Channel.EDI.getType(), Channel.TRA.getType());
						}
					}
				}
				
				//check if the current user is being watched and that the cache log is enabled
				var watchedMember = Azrael.SQLgetWatchlist(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong());
				var sentMessage = Hashes.getMessagePool(e.getGuild().getIdLong(), e.getMessageIdLong());
				//if the watched member level equals 2, then print all written messages from that user in a separate channel
				if(watchedMember != null && watchedMember.getLevel() == 2 && sentMessage != null) {
					TextChannel textChannel = e.getGuild().getTextChannelById(watchedMember.getWatchChannel());
					if(e.getGuild().getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS) || STATIC.setPermissions(e.getGuild(), textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS))) {
						var cachedMessage = sentMessage.get(0);
						textChannel.sendMessage(new EmbedBuilder()
							.setAuthor(cachedMessage.getUserName()+" ("+cachedMessage.getUserID()+")")
							.setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EDIT_TITLE_WATCH)).setColor(Color.WHITE)
							.setDescription("["+cachedMessage.getUserName()+"]: "+cachedMessage.getMessage()).build()).queue();
					}
					else {
						STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MESSAGE_WRITE.getName()+" and "+Permission.MESSAGE_EMBED_LINKS.getName())+e.getChannel().getAsMention(), Channel.LOG.getType());
						logger.error("MESSAGE_WRITE and MESSAGE_EMBED_LINKS permissions required to log messages of watched users on text channel {} in guild {}", e.getChannel().getId(), e.getGuild().getId());
					}
				}
				//print an error if the cache log is not enabled
				else if(watchedMember != null && watchedMember.getLevel() == 2 && sentMessage == null) {
					TextChannel textChannel = e.getGuild().getTextChannelById(watchedMember.getWatchChannel());
					if(e.getGuild().getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS) || STATIC.setPermissions(e.getGuild(), textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS))) {
						textChannel.sendMessage(new EmbedBuilder()
								.setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED)
								.setDescription(STATIC.getTranslation2(e.getGuild(), Translation.EDIT_WATCH_ERR).replace("{}", e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator())).build()).queue();
					}
					else {
						STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MESSAGE_WRITE.getName()+" and "+Permission.MESSAGE_EMBED_LINKS.getName())+e.getChannel().getAsMention(), Channel.LOG.getType());
						logger.error("MESSAGE_WRITE and MESSAGE_EMBED_LINKS permissions required to log messages of watched users on text channel {} in guild {}", e.getChannel().getId(), e.getGuild().getId());
					}
				}
				
				//Run google service, if enabled
				if(GuildIni.getGoogleFunctionalitiesEnabled(e.getGuild().getIdLong()) && GuildIni.getGoogleSpreadsheetsEnabled(e.getGuild().getIdLong()) && allChannels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong() && f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.VOT.getType())).findAny().orElse(null) != null) {
					final String [] sheet = Azrael.SQLgetGoogleFilesAndEvent(e.getGuild().getIdLong(), 2, GoogleEvent.VOTE.id, e.getChannel().getId());
					if(sheet != null && !sheet[0].equals("empty")) {
						final String file_id = sheet[0];
						final String row_start = sheet[1].replaceAll("![A-Z0-9]*", "");
						if((sheet[2] == null || sheet[2].length() == 0) || sheet[2].equals(e.getChannel().getId())) {
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
										//retrieve the saved mapping for the vote event
										final var columns = Azrael.SQLgetGoogleSpreadsheetMapping(file_id, GoogleEvent.VOTE.id, e.getGuild().getIdLong());
										if(columns != null && columns.size() > 0) {
											//find out where the up_vote and down_vote columns are and mark them
											int columnUpVote = 0;
											int columnDownVote = 0;
											int columnMessage = 0;
											for(final var column : columns) {
												if(column.getItem() == GoogleDD.UP_VOTE)
													columnUpVote = column.getColumn();
												else if(column.getItem() == GoogleDD.DOWN_VOTE)
													columnDownVote = column.getColumn();
												else if(column.getItem() == GoogleDD.MESSAGE)
													columnMessage = column.getColumn();
											}
											if(columnMessage != 0) {
												ArrayList<List<Object>> values = new ArrayList<List<Object>>();
												String thumbsup = EmojiManager.getForAlias(":thumbsup:").getUnicode();
												String thumbsdown = EmojiManager.getForAlias(":thumbsdown:").getUnicode();
												int countThumbsUp = 0;
												int countThumbsDown = 0;
												for(final var reaction : e.getMessage().getReactions()) {
													if(columnUpVote > 0 && reaction.getReactionEmote().getName().equals(thumbsup))
														countThumbsUp = reaction.getCount()-1;
													else if(columnDownVote > 0 && reaction.getReactionEmote().getName().equals(thumbsdown))
														countThumbsDown = reaction.getCount()-1;
												}
												//build update array
												int columnCount = 0;
												for(final var column : row) {
													columnCount ++;
													if(columnCount == columnUpVote)
														values.add(Arrays.asList(""+countThumbsUp));
													else if(columnCount == columnDownVote)
														values.add(Arrays.asList(""+countThumbsDown));
													else if(columnCount == columnMessage)
														values.add(Arrays.asList(e.getMessage().getContentRaw()));
													else
														values.add(Arrays.asList(column));
												}
												//execute Runnable
												STATIC.killThread("vote"+e.getMessageId());
												GoogleSheets.overwriteRowOnSpreadsheet(service, file_id, values, row_start+"!A"+currentRow);
											}
										}
										//interrupt the row search
										break;
									}
								}
							} catch (Exception e1) {
								STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED), STATIC.getTranslation2(e.getGuild(), Translation.GOOGLE_WEBSERVICE)+e1.getMessage(), Channel.LOG.getType());
								logger.error("Google Spreadsheet webservice error for event VOTE in guild {}", e.getGuild().getIdLong(), e1);
							}
						}
					}
				}
				
				//Run google service, if enabled
				if(!e.getMember().getUser().isBot() && GuildIni.getGoogleFunctionalitiesEnabled(e.getGuild().getIdLong()) && GuildIni.getGoogleSpreadsheetsEnabled(e.getGuild().getIdLong())) {
					final String [] array = Azrael.SQLgetGoogleFilesAndEvent(e.getGuild().getIdLong(), 2, GoogleEvent.COMMENT.id, e.getChannel().getId());
					if(array != null && !array[0].equals("empty")) {
						//log low priority messages to google spreadsheets
						if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY))) {
							e.getChannel().retrieveMessageById(e.getMessageId()).queueAfter(10, TimeUnit.SECONDS, m -> {
								StringBuilder urls = new StringBuilder();
								for(final var attachment : e.getMessage().getAttachments()) {
									urls.append(attachment.getProxyUrl()+"\n");
								}
								GoogleUtils.handleSpreadsheetRequest(array, e.getGuild(), e.getChannel().getId(), ""+e.getMember().getUser().getId(), new Timestamp(System.currentTimeMillis()), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), e.getMember().getEffectiveName(), null, null, null, null, null, "COMMENT", null, null, null, null, null, e.getMessageIdLong(), e.getMessage().getContentRaw(), urls.toString().trim(), 0, 0, GoogleEvent.COMMENT.id);
							}, err -> {
								//message was removed
							});
						}
					}
				}
			});
		}).start();
	}
}
