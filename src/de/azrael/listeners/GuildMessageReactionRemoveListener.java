package de.azrael.listeners;

import java.awt.Color;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.sheets.v4.model.ValueRange;
import com.vdurmont.emoji.EmojiParser;

import de.azrael.constructors.BotConfigs;
import de.azrael.enums.Channel;
import de.azrael.enums.GoogleDD;
import de.azrael.enums.GoogleEvent;
import de.azrael.enums.Translation;
import de.azrael.google.GoogleSheets;
import de.azrael.google.GoogleUtils;
import de.azrael.sql.Azrael;
import de.azrael.sql.BotConfiguration;
import de.azrael.sql.DiscordRoles;
import de.azrael.threads.DelayedGoogleUpdate;
import de.azrael.util.STATIC;
import de.azrael.util.UserPrivs;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * This class gets executed when a reaction has been removed
 * from a message. 
 * 
 * It is either used to let users removed roles by themselves
 * through reactions.
 * @author xHelixStorm
 * 
 */

public class GuildMessageReactionRemoveListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(GuildMessageReactionRemoveListener.class);
	
	@Override
	public void onMessageReactionRemove(MessageReactionRemoveEvent e) {
		if(e.isFromGuild() && e.getChannelType().equals(ChannelType.TEXT)) {
			new Thread(() -> {
				//no action should be taken, if a bot has added a reaction
				if(e.getUser() != null && !e.getUser().isBot()) {
					//any action below won't apply for muted users
					if(!UserPrivs.isUserMuted(e.getGuild().getMember(e.getUser()))) {
						//verify that the custom server reactions is enabled
						if(Azrael.SQLgetCommandExecutionReaction(e.getGuild().getIdLong())) {
							//check for any registered reaction channel and execute logic only if it happened in that channel
							var rea_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.REA.getType())).findAny().orElse(null);
							if(rea_channel != null && rea_channel.getChannel_ID() == e.getChannel().getIdLong()) {
								//retrieve all reaction roles which will be assigned to a user when reacted
								var reactionRoles = DiscordRoles.SQLgetReactionRoles(e.getGuild().getIdLong());
								if(reactionRoles != null && reactionRoles.size() > 0) {
									String reactionName = "";
									//check if the basic reactions one to nine have been used
									if((EmojiParser.parseToAliases(e.getEmoji().getName()).equals(":one:") || EmojiParser.parseToAliases(e.getEmoji().getName()).equals(":two:") || EmojiParser.parseToAliases(e.getEmoji().getName()).equals(":three:") || EmojiParser.parseToAliases(e.getEmoji().getName()).equals(":four:") || EmojiParser.parseToAliases(e.getEmoji().getName()).equals(":five:") || EmojiParser.parseToAliases(e.getEmoji().getName()).equals(":six:") || EmojiParser.parseToAliases(e.getEmoji().getName()).equals(":seven:") || EmojiParser.parseToAliases(e.getEmoji().getName()).equals(":eight:") || EmojiParser.parseToAliases(e.getEmoji().getName()).equals(":nine:")) && e.getChannel().getIdLong() == rea_channel.getChannel_ID()) {
										reactionName = EmojiParser.parseToAliases(e.getEmoji().getName()).replaceAll(":", "");
									}
									//else retrieve the name of the emote which isn't the default emote
									else {
										reactionName = e.getEmoji().getName();
									}
									
									//continue if a reaction name has been found
									if(reactionName.length() > 0) {
										boolean emoteFound = false;
										BotConfigs botConfig = BotConfiguration.SQLgetBotConfigs(e.getGuild().getIdLong());
										//check if the custom emote mode is enabled, else assign roles to members basing on the default emote
										if(botConfig.isReactionsEnabled()) {
											//retrieve all reaction names
											String [] reactions = botConfig.getReactionEmojis();
											//iterate through all reaction roles in order
											for(int i = 0; i < reactionRoles.size(); i++) {
												//check if the reacted reaction is the same which is saved in the ini file, if yes assign role basing that reaction
												if(reactions[i] != null && reactions[i].length() > 0 && (reactionName.equals(reactions[i]) || EmojiParser.parseToAliases(reactionName).replaceAll(":", "").equals(reactions[i]))) {
													//check if the bot has the manage roles permission
													if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
														final Role role = e.getGuild().getRoleById(reactionRoles.get(i).getRole_ID());
														if(role != null) {
															e.getGuild().removeRoleFromMember(e.getMember(), role).queue();
															logger.info("User {} removed the role {} from itself by reacting in guild {}", e.getUser().getId(), role.getId(), e.getGuild().getId());
														}
													}
													else
														printPermissionError(e);
													emoteFound = true;
													break;
												}
												//interrupt loop if more than 9 roles are registered
												if(i == 8) break;
											}
											if(emoteFound == false) {
												int emote = STATIC.returnEmote(reactionName);
												//check if the bot has the manage roles permission
												if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
													//A tenth emote possibility doesn't exist
													if(emote != 9) {
														final Role role = e.getGuild().getRoleById(reactionRoles.get(emote).getRole_ID());
														if(role != null) {
															e.getGuild().removeRoleFromMember(e.getMember(), role).queue();
															logger.info("User {} removed the role {} from itself by reacting in guild {}", e.getUser().getId(), role.getId(), e.getGuild().getId());
														}
													}
												}
												else
													printPermissionError(e);
											}
										}
										else {
											int emote = STATIC.returnEmote(reactionName);
											//check if the bot has the manage roles permission
											if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
												//A tenth emote possibility doesn't exist
												if(emote != 9) {
													final Role role = e.getGuild().getRoleById(reactionRoles.get(emote).getRole_ID());
													if(role != null) {
														e.getGuild().removeRoleFromMember(e.getMember(), role).queue();
														logger.info("User {} removed the role {} from itself by reacting in guild {}", e.getUser().getId(), role.getId(), e.getGuild().getId());
													}
												}
											}
												
											else
												printPermissionError(e);
										}
									}
								}
								else
									logger.error("Reaction roles couldn't be retrieved in guild {}", e.getGuild().getId());
							}
							//check if a role has to be removed
							long role_id = DiscordRoles.SQLgetReactionRole(e.getMessageIdLong(), e.getEmoji().getName());
							if(role_id != 0) {
								if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
									e.getGuild().removeRoleFromMember(e.getMember(), e.getGuild().getRoleById(role_id)).queue();
								}
								else {
									printPermissionError(e);
								}
							}
						}
					}
					
					//Run google service, if enabled
					runVoteSpreadsheetService(e);
				}
			}).start();
		}
	}
	
	/**
	 * Print error that the manage roles permission is missing!
	 * @param e
	 */
	private static void printPermissionError(MessageReactionRemoveEvent e) {
		STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION)+Permission.MANAGE_ROLES.getName(), Channel.LOG.getType());
		logger.warn("MANAGE ROLES permission required to remove reaction roles in guild {}", e.getGuild().getId());
	}
	
	private static void runVoteSpreadsheetService(MessageReactionRemoveEvent e) {
		if(e.isFromGuild() && e.getChannelType().equals(ChannelType.TEXT)) {
			if(BotConfiguration.SQLgetBotConfigs(e.getGuild().getIdLong()).getGoogleFunctionalities()) {
				//check if it's a vote channel
				final var channels = Azrael.SQLgetChannels(e.getGuild().getIdLong());
				final var thisChannel = channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong() && f.getChannel_Type() != null && (f.getChannel_Type().equals(Channel.VOT.getType()) || f.getChannel_Type().equals(Channel.VO2.getType()))).findAny().orElse(null);
				if(thisChannel != null) {
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
											for(final var column : columns) {
												if(column.getItem() == GoogleDD.UP_VOTE)
													columnUpVote = column.getColumn();
												else if(column.getItem() == GoogleDD.DOWN_VOTE)
													columnDownVote = column.getColumn();
												else if(column.getItem() == GoogleDD.SHRUG_VOTE)
													columnShrugVote = column.getColumn();
											}
											if(columnUpVote != 0 || columnDownVote != 0 || columnShrugVote != 0) {
												//build update array
												ArrayList<List<Object>> values = new ArrayList<List<Object>>();
												BotConfigs botConfig = BotConfiguration.SQLgetBotConfigs(e.getGuild().getIdLong());
												final String [] reactions = botConfig.getVoteReactions();
												Object thumbsup = STATIC.retrieveEmoji(e.getGuild(), reactions[0], ":thumbsup:");
												Object thumbsdown = STATIC.retrieveEmoji(e.getGuild(), reactions[1], ":thumbsdown:");
												Object shrug = STATIC.retrieveEmoji(e.getGuild(), reactions[2], ":shrug:");
												int thumbsUpCount = 0;
												int thumbsDownCount = 0;
												int shrugCount = 0;
												for(final var reaction : e.getChannel().retrieveMessageById(e.getMessageId()).complete().getReactions()) {
													if(columnUpVote > 0 && ((reaction.getEmoji().getType().equals(Emoji.Type.UNICODE) && thumbsup instanceof String && reaction.getEmoji().getName().equals((String)thumbsup)) || (reaction.getEmoji().getType().equals(Emoji.Type.CUSTOM) && thumbsup instanceof RichCustomEmoji && reaction.getEmoji().asCustom().getIdLong() == ((RichCustomEmoji)thumbsup).getIdLong())))
														thumbsUpCount = reaction.getCount()-1;
													if(columnDownVote > 0 && ((reaction.getEmoji().getType().equals(Emoji.Type.UNICODE) && thumbsdown instanceof String && reaction.getEmoji().getName().equals((String)thumbsdown)) || (reaction.getEmoji().getType().equals(Emoji.Type.CUSTOM) && thumbsdown instanceof RichCustomEmoji && reaction.getEmoji().asCustom().getIdLong() == ((RichCustomEmoji)thumbsdown).getIdLong())))
														thumbsDownCount = reaction.getCount()-1;
													if(columnShrugVote > 0 && ((reaction.getEmoji().getType().equals(Emoji.Type.UNICODE) && shrug instanceof String && reaction.getEmoji().getName().equals((String)shrug)) || (reaction.getEmoji().getType().equals(Emoji.Type.CUSTOM) && shrug instanceof RichCustomEmoji && reaction.getEmoji().asCustom().getIdLong() == ((RichCustomEmoji)shrug).getIdLong())))
														shrugCount = reaction.getCount()-1;
												}
												int columnCount = 0;
												for(final var column : row) {
													columnCount ++;
													if(columnCount == columnUpVote)
														values.add(Arrays.asList(""+thumbsUpCount));
													else if(columnCount == columnDownVote)
														values.add(Arrays.asList(""+thumbsDownCount));
													else if(columnCount == columnShrugVote)
														values.add(Arrays.asList(""+shrugCount));
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
	}
}
