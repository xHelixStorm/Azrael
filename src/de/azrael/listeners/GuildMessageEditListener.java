package de.azrael.listeners;

import java.awt.Color;
import java.net.SocketTimeoutException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.sheets.v4.model.ValueRange;

import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Channels;
import de.azrael.constructors.Messages;
import de.azrael.core.Hashes;
import de.azrael.enums.Channel;
import de.azrael.enums.GoogleDD;
import de.azrael.enums.GoogleEvent;
import de.azrael.enums.Translation;
import de.azrael.fileManagement.FileSetting;
import de.azrael.fileManagement.GuildIni;
import de.azrael.filter.LanguageFilter;
import de.azrael.filter.URLFilter;
import de.azrael.google.GoogleSheets;
import de.azrael.google.GoogleUtils;
import de.azrael.sql.Azrael;
import de.azrael.sql.BotConfiguration;
import de.azrael.threads.DelayedGoogleUpdate;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

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
							new Thread(new LanguageFilter(e.getMessage(), filter_lang, allChannels)).start();
							//if url censoring is allowed, execute that filter as well
							if(allChannels.parallelStream().filter(f -> f.getChannel_ID() == channel_id && f.getURLCensoring()).findAny().orElse(null) != null)
								new Thread(new URLFilter(e.getMessage(), e.getMessage().getMember(), filter_lang, allChannels)).start();
						}
						else {
							STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MESSAGE_WRITE.getName()+" and "+Permission.MESSAGE_MANAGE.getName())+e.getChannel().getName(), Channel.LOG.getType());
							logger.error("MESSAGE_WRITE and MESSAGE_MANAGE permissions required to censor messages inside text channel {} in guild {}", e.getChannel().getId(), e.getGuild().getId());
						}
					}
					//if the url censoring is enabled but no languages to filter have been set, start the url censoring anyway
					else if(allChannels.parallelStream().filter(f -> f.getChannel_ID() == channel_id && f.getURLCensoring()).findAny().orElse(null) != null) {
						if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_MANAGE) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_MANAGE))) {
							new Thread(new URLFilter(e.getMessage(), e.getMessage().getMember(), filter_lang, allChannels)).start();
						}
						else {
							STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MESSAGE_WRITE.getName()+" and "+Permission.MESSAGE_MANAGE.getName())+e.getChannel().getName(), Channel.LOG.getType());
							logger.error("MESSAGE_WRITE and MESSAGE_MANAGE permissions required to censor messages inside text channel {} in guild {}", e.getChannel().getId(), e.getGuild().getId());
						}
					}
				});
			}
			
			executor.execute(() -> {
				BotConfigs botConfig = BotConfiguration.SQLgetBotConfigs(e.getGuild().getIdLong());
				boolean printEditHistory = false;
				//check if edited messages should be collected and printed in a channel
				if(botConfig.getEditedMessages()) {
					//verify if the message history has to be printed and not just the edited message
					if(botConfig.getEditedMessagesHistory()) {
						printEditHistory = true;
					}
					//print the edited message either in an edit or trash channel
					else {
						STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setAuthor(e.getMessage().getMember().getUser().getName()+"#"+e.getMessage().getMember().getUser().getDiscriminator()+" ("+e.getMessage().getMember().getUser().getId()+")").setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EDIT_TITLE))
							, (e.getMessage().getContentRaw().length() <= 2048 ? e.getMessage().getContentRaw() : e.getMessage().getContentRaw().substring(0, 2040)+"...")
							, Channel.EDI.getType(), Channel.TRA.getType());
					}
				}
				
				//check if the channel log and cache log is enabled and if one of the two or bot is/are enabled then write message to file or/and log to system cache
				if((botConfig.getChannelLog() || botConfig.getCacheLog()) && e.getMessage().getMember() != null && !e.getMessage().getMember().getUser().isBot()) {
					StringBuilder image_url = new StringBuilder();
					for(Attachment attch : e.getMessage().getAttachments()){
						image_url.append((e.getMessage().getContentRaw().length() == 0 && image_url.length() == 0) ? "("+attch.getProxyUrl()+")" : "\n("+attch.getProxyUrl()+")");
					}
					Messages collectedMessage = new Messages();
					collectedMessage.setUserID(e.getMessage().getMember().getUser().getIdLong());
					collectedMessage.setUsername(e.getMessage().getMember().getUser().getName()+"#"+e.getMessage().getMember().getUser().getDiscriminator());
					collectedMessage.setGuildID(e.getGuild().getIdLong());
					collectedMessage.setChannelID(channel_id);
					collectedMessage.setChannelName(e.getChannel().getName());
					collectedMessage.setMessage(e.getMessage().getContentRaw()+image_url.toString()+"\n");
					collectedMessage.setMessageID(e.getMessageIdLong());
					collectedMessage.setTime(ZonedDateTime.now());
					collectedMessage.setIsEdit(true); // note: flag set to true for edited message
					collectedMessage.setIsUserBot(e.getMessage().getMember().getUser().isBot());
					
					if(botConfig.getChannelLog())
						FileSetting.appendFile("./message_log/"+e.getChannel().getId()+".txt", "EDIT ["+collectedMessage.getTime().toString()+" - "+e.getMessage().getMember().getUser().getName()+"#"+e.getMessage().getMember().getUser().getDiscriminator()+" ("+e.getMessage().getMember().getUser().getId()+")]: "+collectedMessage.getMessage());
					if(botConfig.getCacheLog()) {
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
				if(e.getMember() != null) {
					var watchedMember = Azrael.SQLgetWatchlist(e.getMessage().getMember().getUser().getIdLong(), e.getGuild().getIdLong());
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
									.setDescription(STATIC.getTranslation2(e.getGuild(), Translation.EDIT_WATCH_ERR).replace("{}", e.getMessage().getMember().getUser().getName()+"#"+e.getMessage().getMember().getUser().getDiscriminator())).build()).queue();
						}
						else {
							STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MESSAGE_WRITE.getName()+" and "+Permission.MESSAGE_EMBED_LINKS.getName())+e.getChannel().getAsMention(), Channel.LOG.getType());
							logger.error("MESSAGE_WRITE and MESSAGE_EMBED_LINKS permissions required to log messages of watched users on text channel {} in guild {}", e.getChannel().getId(), e.getGuild().getId());
						}
					}
				}
				
				if(botConfig.getGoogleFunctionalities()) {
					//Run google service, if enabled
					runVoteSpreadsheetService(e, allChannels);
					
					//Run google service, if enabled
					runCommentSpreadsheetService(e);
				}
			});
		}).start();
	}
	
	private static void runVoteSpreadsheetService(GuildMessageUpdateEvent e, ArrayList<Channels> allChannels) {
		if(allChannels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong() && f.getChannel_Type() != null && (f.getChannel_Type().equals(Channel.VOT.getType()) || f.getChannel_Type().equals(Channel.VO2.getType()))).findAny().orElse(null) != null) {
			final String [] sheet = Azrael.SQLgetGoogleFilesAndEvent(e.getGuild().getIdLong(), 2, GoogleEvent.VOTE.id, e.getChannel().getId());
			if(sheet != null && !sheet[0].equals("empty")) {
				final String file_id = sheet[0];
				final String row_start = sheet[1].replaceAll("![A-Z0-9]*", "");
				if((sheet[2] == null || sheet[2].length() == 0) || sheet[2].equals(e.getChannel().getId())) {
					if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY))) {
						try {
							ValueRange response = DelayedGoogleUpdate.getCachedValueRange("VOTE"+e.getGuild().getId()+e.getChannel().getId());
							if(response == null) {
								final var service = GoogleSheets.getSheetsClientService();
								response = GoogleSheets.readWholeSpreadsheet(service, file_id, row_start);
								DelayedGoogleUpdate.cacheRetrievedSheetValueRange("VOTE"+e.getGuild().getId()+e.getChannel().getId(), response);
							}
							if(response.getValues() != null && response.getValues().parallelStream().filter(f -> f.parallelStream().filter(f2 -> ((String)f2).equals(e.getMessageId())).findAny().orElse(null) != null).findAny().orElse(null) != null) {
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
											int columnShrugVote = 0;
											int columnMessage = 0;
											for(final var column : columns) {
												if(column.getItem() == GoogleDD.UP_VOTE)
													columnUpVote = column.getColumn();
												else if(column.getItem() == GoogleDD.DOWN_VOTE)
													columnDownVote = column.getColumn();
												else if(column.getItem() == GoogleDD.SHRUG_VOTE)
													columnShrugVote = column.getColumn();
												else if(column.getItem() == GoogleDD.MESSAGE)
													columnMessage = column.getColumn();
											}
											if(columnMessage != 0) {
												ArrayList<List<Object>> values = new ArrayList<List<Object>>();
												final String [] reactions = GuildIni.getVoteReactions(e.getGuild());
												Object thumbsup = STATIC.retrieveEmoji(e.getGuild(), reactions[0], ":thumbsup:");
												Object thumbsdown = STATIC.retrieveEmoji(e.getGuild(), reactions[1], ":thumbsdown:");
												Object shrug = STATIC.retrieveEmoji(e.getGuild(), reactions[2], ":shrug:");
												int countThumbsUp = 0;
												int countThumbsDown = 0;
												int countShrug = 0;
												for(final var reaction : e.getChannel().retrieveMessageById(e.getMessageIdLong()).complete().getReactions()) {
													if(columnUpVote > 0 && ((reaction.getReactionEmote().isEmoji() && thumbsup instanceof String && reaction.getReactionEmote().getName().equals((String)thumbsup)) || (reaction.getReactionEmote().isEmote() && thumbsup instanceof Emote && reaction.getReactionEmote().getEmote().getIdLong() == ((Emote)thumbsup).getIdLong())))
														countThumbsUp = reaction.getCount()-1;
													if(columnDownVote > 0 && ((reaction.getReactionEmote().isEmoji() && thumbsdown instanceof String && reaction.getReactionEmote().getName().equals((String)thumbsdown)) || (reaction.getReactionEmote().isEmote() && thumbsdown instanceof Emote && reaction.getReactionEmote().getEmote().getIdLong() == ((Emote)thumbsdown).getIdLong())))
														countThumbsDown = reaction.getCount()-1;
													if(columnShrugVote > 0 && ((reaction.getReactionEmote().isEmoji() && shrug instanceof String && reaction.getReactionEmote().getName().equals((String)shrug)) || (reaction.getReactionEmote().isEmote() && shrug instanceof Emote && reaction.getReactionEmote().getEmote().getIdLong() == ((Emote)shrug).getIdLong())))
														countShrug = reaction.getCount()-1;
												}
												//build update array
												int columnCount = 0;
												for(final var column : row) {
													columnCount ++;
													if(columnCount == columnUpVote)
														values.add(Arrays.asList(""+countThumbsUp));
													else if(columnCount == columnDownVote)
														values.add(Arrays.asList(""+countThumbsDown));
													else if(columnCount == columnShrugVote)
														values.add(Arrays.asList(""+countShrug));
													else if(columnCount == columnMessage)
														values.add(Arrays.asList(e.getMessage().getContentRaw()));
													else
														values.add(Arrays.asList(column));
												}
												ValueRange valueRange = new ValueRange().setRange(row_start+"!A"+currentRow).setMajorDimension("COLUMNS").setValues(values);
												//execute Runnable
												if(!STATIC.threadExists("VOTE"+e.getGuild().getId()+e.getChannel().getId())) {
													new Thread(new DelayedGoogleUpdate(e.getGuild(), valueRange, e.getMessageIdLong(), file_id, e.getChannel().getId(), "update", GoogleEvent.VOTE)).start();
												}
												else {
													DelayedGoogleUpdate.handleAdditionalRequest(e.getGuild(), e.getChannel().getId(), valueRange, e.getMessageIdLong(), "update");
												}
											}
										}
										//interrupt the row search
										break;
									}
								}
							}
							else if(DelayedGoogleUpdate.containsMessage(e.getGuild().getId()+"_"+e.getChannel().getId()+"_"+e.getMessageId(), "add")) {
								final String [] reactions = GuildIni.getVoteReactions(e.getGuild());
								Object thumbsup = STATIC.retrieveEmoji(e.getGuild(), reactions[0], ":thumbsup:");
								Object thumbsdown = STATIC.retrieveEmoji(e.getGuild(), reactions[1], ":thumbsdown:");
								Object shrug = STATIC.retrieveEmoji(e.getGuild(), reactions[2], ":shrug:");
								int upVote = 0;
								int downVote = 0;
								int shrugVote = 0;
								for(final var reaction : e.getChannel().retrieveMessageById(e.getMessageId()).complete().getReactions()) {
									if(reaction.getReactionEmote().isEmoji() && thumbsup instanceof String && reaction.getReactionEmote().getName().equals((String)thumbsup))
										upVote = reaction.getCount()-1;
									else if(reaction.getReactionEmote().isEmote() && thumbsup instanceof Emote && reaction.getReactionEmote().getIdLong() == ((Emote)thumbsup).getIdLong())
										upVote = reaction.getCount()-1;
									else if(reaction.getReactionEmote().isEmoji() && thumbsdown instanceof String && reaction.getReactionEmote().getName().equals((String)thumbsdown))
										downVote = reaction.getCount()-1;
									else if(reaction.getReactionEmote().isEmote() && thumbsdown instanceof Emote && reaction.getReactionEmote().getIdLong() == ((Emote)thumbsdown).getIdLong())
										downVote = reaction.getCount()-1;
									else if(reaction.getReactionEmote().isEmoji() && shrug instanceof String && reaction.getReactionEmote().getName().equals((String)shrug))
										shrugVote = reaction.getCount()-1;
									else if(reaction.getReactionEmote().isEmote() && shrug instanceof Emote && reaction.getReactionEmote().getIdLong() == ((Emote)shrug).getIdLong())
										shrugVote = reaction.getCount()-1;
								}
								final var values = GoogleSheets.spreadsheetVoteRequest(sheet, e.getGuild(), e.getChannel().getId(), e.getMember().getUser().getId(), new Timestamp(System.currentTimeMillis()), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), e.getMember().getEffectiveName(), e.getMessageIdLong(), e.getMessage().getContentRaw(), upVote, downVote, shrugVote);
								if(values != null) {
									ValueRange valueRange = new ValueRange().setValues(values);
									if(!STATIC.threadExists("VOTE"+e.getGuild().getId()+e.getChannel().getId())) {
										new Thread(new DelayedGoogleUpdate(e.getGuild(), valueRange, e.getMessageIdLong(), sheet[0], e.getChannel().getId(), "update", GoogleEvent.VOTE)).start();
									}
									else {
										DelayedGoogleUpdate.handleAdditionalRequest(e.getGuild(), e.getChannel().getId(), valueRange, e.getMessageIdLong(), "update");
									}
								}
							}
						} catch(SocketTimeoutException e1) {
							if(GoogleUtils.timeoutHandler(e.getGuild(), file_id, GoogleEvent.VOTE.name(), e1)) {
								runVoteSpreadsheetService(e, allChannels);
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
	
	private static void runCommentSpreadsheetService(GuildMessageUpdateEvent e) {
		if(!e.getMessage().getMember().getUser().isBot()) {
			final String [] array = Azrael.SQLgetGoogleFilesAndEvent(e.getGuild().getIdLong(), 2, GoogleEvent.COMMENT.id, e.getChannel().getId());
			if(array != null && !array[0].equals("empty")) {
				final String file_id = array[0];
				final String row_start = array[1].replaceAll("![A-Z0-9]*", "");
				if(array[2] != null && array[2].equals(e.getChannel().getId())) {
					//log low priority messages to google spreadsheets
					if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY))) {
						try {
							ValueRange response = DelayedGoogleUpdate.getCachedValueRange("COMMENT"+e.getGuild().getId()+e.getChannel().getId());
							if(response == null) {
								final var service = GoogleSheets.getSheetsClientService();
								response = GoogleSheets.readWholeSpreadsheet(service, file_id, row_start);
								DelayedGoogleUpdate.cacheRetrievedSheetValueRange("COMMENT"+e.getGuild().getId()+e.getChannel().getId(), response);
							}
							if(response.getValues() != null && response.getValues().parallelStream().filter(f -> f.parallelStream().filter(f2 -> ((String)f2).equals(e.getMessageId())).findAny().orElse(null) != null).findAny().orElse(null) != null) {
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
										//retrieve the saved mapping for the comment event
										final var columns = Azrael.SQLgetGoogleSpreadsheetMapping(file_id, GoogleEvent.COMMENT.id, e.getGuild().getIdLong());
										if(columns != null && columns.size() > 0) {
											int columnMessage = 0;
											int columnAttachment = 0;
											for(final var column : columns) {
												if(column.getItem() == GoogleDD.MESSAGE)
													columnMessage = column.getColumn();
												else  if(column.getItem() == GoogleDD.SCREEN_URL)
													columnAttachment = column.getColumn();
											}
											if(columnMessage != 0) {
												ArrayList<List<Object>> values = new ArrayList<List<Object>>();
												//build update array
												int columnCount = 0;
												for(final var column : row) {
													columnCount ++;
													if(columnCount == columnMessage)
														values.add(Arrays.asList(e.getMessage().getContentRaw()));
													else if(columnCount == columnAttachment) {
														StringBuilder urls = new StringBuilder();
														for(final var attachment : e.getMessage().getAttachments()) {
															urls.append(attachment.getProxyUrl()+"\n");
														}
														values.add(Arrays.asList(urls.toString()));
													}
													else
														values.add(Arrays.asList(column));
												}
												ValueRange valueRange = new ValueRange().setRange(row_start+"!A"+currentRow).setMajorDimension("COLUMNS").setValues(values);
												//Execute Runnable
												if(!STATIC.threadExists("COMMENT"+e.getGuild().getId()+e.getChannel().getId())) {
													new Thread(new DelayedGoogleUpdate(e.getGuild(), valueRange, e.getMessageIdLong(), array[0], e.getChannel().getId(), "update", GoogleEvent.COMMENT)).start();
												}
												else {
													DelayedGoogleUpdate.handleAdditionalRequest(e.getGuild(), e.getChannel().getId(), valueRange, e.getMessageIdLong(), "update");
												}
											}
										}
										//interrupt the row search
										break;
									}
								}
							}
							else {
								StringBuilder urls = new StringBuilder();
								for(final var attachment : e.getMessage().getAttachments()) {
									urls.append(attachment.getProxyUrl()+"\n");
								}
								final var values = GoogleSheets.spreadsheetCommentRequest(array, e.getGuild(), e.getChannel().getId(), e.getMember().getUser().getId(), new Timestamp(System.currentTimeMillis()), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), e.getMember().getEffectiveName(), e.getMessageIdLong(), e.getMessage().getContentRaw(), urls.toString().trim());
								if(values != null) {
									ValueRange valueRange = new ValueRange().setValues(values);
									if(!STATIC.threadExists("COMMENT"+e.getGuild().getId()+e.getChannel().getId())) {
										new Thread(new DelayedGoogleUpdate(e.getGuild(), valueRange, e.getMessageIdLong(), array[0], e.getChannel().getId(), "update", GoogleEvent.COMMENT)).start();
									}
									else {
										DelayedGoogleUpdate.handleAdditionalRequest(e.getGuild(), e.getChannel().getId(), valueRange, e.getMessageIdLong(), "update");
									}
								}
							}
						} catch (SocketTimeoutException e1) {
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
}
