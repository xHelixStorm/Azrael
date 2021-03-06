package de.azrael.listeners;

import java.awt.Color;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.sheets.v4.model.ValueRange;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;

import de.azrael.commandsContainer.RandomshopExecution;
import de.azrael.constructors.Cache;
import de.azrael.constructors.Clan;
import de.azrael.core.Hashes;
import de.azrael.core.UserPrivs;
import de.azrael.enums.Channel;
import de.azrael.enums.GoogleDD;
import de.azrael.enums.GoogleEvent;
import de.azrael.enums.Translation;
import de.azrael.fileManagement.GuildIni;
import de.azrael.google.GoogleSheets;
import de.azrael.google.GoogleUtils;
import de.azrael.inventory.InventoryBuilder;
import de.azrael.sql.Azrael;
import de.azrael.sql.DiscordRoles;
import de.azrael.sql.RankingSystem;
import de.azrael.sql.RankingSystemItems;
import de.azrael.threads.DelayedGoogleUpdate;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * This class gets executed when a reaction has been added
 * to a message. 
 * 
 * It is either used to let users assign roles to themselves
 * through reactions or to switch pages of the inventory
 * or randomshop.
 * @author xHelixStorm
 * 
 */

public class GuildMessageReactionAddListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(GuildMessageReactionAddListener.class);
	
	@Override
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent e) {
		new Thread(() ->  {
			//no action should be taken, if a bot has added a reaction
			if(!e.getUser().isBot() && !e.getGuild().getSelfMember().equals(e.getMember())) {
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
								if((EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":one:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":two:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":three:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":four:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":five:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":six:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":seven:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":eight:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":nine:")) && e.getChannel().getIdLong() == rea_channel.getChannel_ID()) {
									reactionName = EmojiParser.parseToAliases(e.getReactionEmote().getName()).replaceAll(":", "");
								}
								//else retrieve the name of the emote which isn't the default emote
								else {
									reactionName = e.getReactionEmote().getName();
								}
								
								//continue if a reaction name has been found
								if(reactionName.length() > 0) {
									//retrieve all names of the reactions from the guild ini file
									String [] reactions = GuildIni.getReactions(e.getGuild().getIdLong());
									boolean emoteFound = false;
									//check if the custom emote mode is enabled, else assign roles to members basing on the default emote
									if(GuildIni.getReactionEnabled(e.getGuild().getIdLong())) {
										//iterate through all reaction roles in order
										for(int i = 0; i < reactionRoles.size(); i++) {
											//check if the reacted reaction is the same which is saved in the ini file, if yes assign role basing that reaction
											if(reactions[i].length() > 0 && (reactionName.equals(reactions[i]) || EmojiParser.parseToAliases(reactionName).replaceAll(":", "").equals(reactions[i]))) {
												//check if the bot has the manage roles permission
												if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
													final Role role = e.getGuild().getRoleById(reactionRoles.get(i).getRole_ID());
													if(role != null) {
														e.getGuild().addRoleToMember(e.getMember(), role).queue();
														logger.info("User {} received the role {} by reacting in guild {}", e.getUser().getId(), role.getId(), e.getGuild().getId());
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
										//return to default emotes, if the name of the reaction wasn't added in the ini file
										if(emoteFound == false) {
											int emote = STATIC.returnEmote(reactionName);
											//check if the bot has the manage roles permission
											if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
												//A tenth emote possibility doesn't exist
												if(emote != 9) {
													final Role role = e.getGuild().getRoleById(reactionRoles.get(emote).getRole_ID());
													if(role != null) {
														e.getGuild().addRoleToMember(e.getMember(), role).queue();
														logger.info("User {} received the role {} by reacting in guild {}", e.getUser().getId(), role.getId(), e.getGuild().getId());
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
													e.getGuild().addRoleToMember(e.getMember(), role).queue();
													logger.info("User {} received the role {} by reacting in guild {}", e.getUser().getId(), role.getId(), e.getGuild().getId());
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
						
						//check if a role has to be assigned
						long role_id = 0;
						if(e.getReactionEmote().isEmoji())
							role_id = DiscordRoles.SQLgetReactionRole(e.getMessageIdLong(), e.getReactionEmote().getAsCodepoints());
						else {
							role_id = DiscordRoles.SQLgetReactionRole(e.getMessageIdLong(), e.getReactionEmote().getName());
						}
						if(role_id != 0) {
							if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
								e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(role_id)).queue();
							}
							else {
								printPermissionError(e);
							}
						}
					}
				}
				
				//check if this reaction has to be added into a different message of the bot
				var cache = Hashes.getTempCache("write_edit_reaction_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getUserId()+"me"+e.getMessageId());
				if(cache != null && cache.getExpiration() - System.currentTimeMillis() > 0) {
					String channel_id = cache.getAdditionalInfo();
					String message_id = cache.getAdditionalInfo2();
					boolean isEmojiCustom = false;
					if(!e.getReactionEmote().isEmoji())
						isEmojiCustom = true;
					final boolean customEmoji = isEmojiCustom;
					//check that the permissions are correct
					TextChannel textChannel = e.getGuild().getTextChannelById(channel_id);
					if(e.getGuild().getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_HISTORY) || STATIC.setPermissions(e.getGuild(), textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_HISTORY))) {
						if(e.getGuild().getSelfMember().hasPermission(textChannel, Permission.MESSAGE_ADD_REACTION) || STATIC.setPermissions(e.getGuild(), textChannel, EnumSet.of(Permission.MESSAGE_ADD_REACTION))) {
							textChannel.retrieveMessageById(message_id).queue(message -> {
								String reaction = null;
								//unicode emojis
								if(!customEmoji) {
									reaction = e.getReactionEmote().getAsCodepoints();
									message.addReaction(reaction).queue();
								}
								//user emojis
								else {
									reaction = e.getReactionEmote().getName();
									message.addReaction(e.getReactionEmote().getEmote()).queue();
								}
								//ask the user if it should be combined with a role
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REACTION_ADDED)).addField(STATIC.getTranslation(e.getMember(), Translation.REACTION_REPLY_YES), "", true).addField(STATIC.getTranslation(e.getMember(), Translation.REACTION_REPLY_NO), "", true).build()).queue();
								Hashes.addTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "RA1", message_id, reaction));
							});
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MESSAGE_ADD_REACTION.getName()).build()).queue();
							logger.error("MESSAGE_ADD_REACTION permission required to add a reaction on text channel {} in guild {}", channel_id, e.getGuild().getId());
							Hashes.clearTempCache("write_edit_reaction_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getUserId()+"me"+e.getMessageId());
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MESSAGE_HISTORY.getName()).build()).queue();
						logger.error("MESSAGE_HISTORY permission required to read the history of text channel {} in guild {}", channel_id, e.getGuild().getId());
						Hashes.clearTempCache("write_edit_reaction_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getUserId()+"me"+e.getMessageId());
					}
				}
				
				//check if either the arrow left or arrow right reaction has been used
				if(EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":arrow_left:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":arrow_right:")) {
					//inventory reactions
					final var inventory = Hashes.getTempCache("inventory_gu"+e.getGuild().getId()+"me"+e.getMessageId()+"us"+e.getMember().getUser().getId());
					//randomshop reactions
					final var randomshop = Hashes.getTempCache("randomshop_gu"+e.getGuild().getId()+"me"+e.getMessageId()+"us"+e.getMember().getUser().getId());
					//clan reactions
					final var clan = Hashes.getTempCache("clan_gu"+e.getGuild().getId()+"me"+e.getMessageId()+"us"+e.getMember().getUser().getId());
					
					//execute if it's an inventory reaction
					if(inventory != null) {
						if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES))) {
							//retrieve all inventory details
							String cache_content = inventory.getAdditionalInfo();
							String [] array = cache_content.split("_");
							int current_page = Integer.parseInt(array[0]);
							final int last_page = Integer.parseInt(array[1]);
							final String inventory_tab = array[2];
							final String sub_tab = array[3];
							//either return to the previous page or go to the next page and redraw the inventory
							if(EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":arrow_left:") && current_page != 1)
								current_page--;
							else if(EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":arrow_right:") && current_page != last_page)
								current_page++;
							if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.MESSAGE_MANAGE) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.MESSAGE_MANAGE))) {
								//delete previously drawn inventory so that the new inventory image can be drawn
								e.getChannel().retrieveMessageById(e.getMessageId()).complete().delete().queue();
							}
							else {
								logger.warn("MESSAGE_MANAGE permission required to delete messages in text channel {} in guild {}", e.getChannel().getId(), e.getGuild().getId());
							}
							Hashes.addTempCache("inventory_bot_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(60000, e.getMember().getUser().getId()+"_"+current_page+"_"+last_page+"_"+inventory_tab+"_"+sub_tab));
							//retrieve and max items in the current inventory
							final var guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
							final int maxItems = guild_settings.getInventoryMaxItems();
							//draw inventory depending on the category that was chosen
							if(inventory_tab.equalsIgnoreCase("weapons")) {
								if(!sub_tab.equalsIgnoreCase("total"))
									InventoryBuilder.DrawInventory(e.getGuild(), e.getMember(), e.getChannel(), inventory_tab, sub_tab, RankingSystem.SQLgetInventoryAndDescriptionsWeapons(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), ((current_page-1)*maxItems), maxItems, sub_tab), current_page, last_page, guild_settings);
								else
									InventoryBuilder.DrawInventory(e.getGuild(), e.getMember(), e.getChannel(), inventory_tab, sub_tab, RankingSystem.SQLgetInventoryAndDescriptionsWeapons(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), ((current_page-1)*maxItems), maxItems), current_page, last_page, guild_settings);
							}
							else if(inventory_tab.equalsIgnoreCase("skins"))
								InventoryBuilder.DrawInventory(e.getGuild(), e.getMember(), e.getChannel(), inventory_tab, sub_tab, RankingSystem.SQLgetInventoryAndDescriptionsSkins(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), ((current_page-1)*maxItems), maxItems), current_page, last_page, guild_settings);
							else
								InventoryBuilder.DrawInventory(e.getGuild(), e.getMember(), e.getChannel(), inventory_tab, sub_tab, RankingSystem.SQLgetInventoryAndDescriptions(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), ((current_page-1)*maxItems), maxItems), current_page, last_page, guild_settings);
						}
						else {
							STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MESSAGE_WRITE.getName()+" and "+Permission.MESSAGE_ATTACH_FILES.getName())+"<#"+e.getChannel().getId()+">", Channel.LOG.getType());
							logger.error("MESSAGE_WRITE and MESSAGE_ATTACH_FILES permission required to reupload a new image in channel {} in guild {}", e.getChannel().getId(), e.getGuild().getId());
						}
					}
					//execute if it's a randomshop reaction
					else if(randomshop != null) {
						if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES))) {
							//retrieve all randomshop details
							String cache_content = randomshop.getAdditionalInfo();
							String [] array = cache_content.split("_");
							int current_page = Integer.parseInt(array[0]);
							final int last_page = Integer.parseInt(array[1]);
							final String input = array[2];
							//either return to the previous page or go to the next page and redraw the randomshop
							if(EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":arrow_left:") && current_page != 1)
								current_page--;
							else if(EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":arrow_right:") && current_page != last_page)
								current_page++;
							if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.MESSAGE_MANAGE) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.MESSAGE_MANAGE))) {
								//delete previous randomshop image and redraw the new one
								e.getChannel().retrieveMessageById(e.getMessageId()).complete().delete().queue();
							}
							else {
								logger.warn("MESSAGE_MANAGE permission required to delete messages on text channel {} in guild {}", e.getChannel().getId(), e.getGuild().getId());
							}
							RandomshopExecution.inspectItems(e.getMember(), e.getChannel(), RankingSystemItems.SQLgetWeaponAbbvs(e.getGuild().getIdLong()), RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong(), false), input, current_page, true);
						}
						else {
							STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MESSAGE_WRITE.getName()+" and "+Permission.MESSAGE_ATTACH_FILES.getName())+"<#"+e.getChannel().getId()+">", Channel.LOG.getType());
							logger.error("MESSAGE_WRITE and MESSAGE_ATTACH_FILES permission required to reupload a new image on text channel {} in guild {}", e.getChannel().getId(), e.getGuild().getId());
						}
					}
					//execute if it's a clan reaction
					else if(clan != null) {
						if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_HISTORY, Permission.MESSAGE_ADD_REACTION) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_HISTORY, Permission.MESSAGE_ADD_REACTION))) {
							@SuppressWarnings("unchecked")
							final var clans = (ArrayList<Clan>) clan.getObject();
							int currentPage = Integer.parseInt(clan.getAdditionalInfo());
							int totPages = clans.size()/10;
							if(clans.size() % 10 > 0)
								totPages++;
							//either return to the previous page or go to the next page and reprint the available clans
							if(EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":arrow_left:") && currentPage != 1)
								currentPage--;
							else if(EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":arrow_right:") && currentPage != totPages)
								currentPage++;
							final int page = currentPage;
							final int lastPage = totPages;
							e.getChannel().retrieveMessageById(e.getMessageId()).queue(m -> {
								if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.MESSAGE_MANAGE) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.MESSAGE_MANAGE)))
									m.delete().queue();
								else
									logger.warn("MESSAGE_MANAGE permission required to delete messages on text channel {} in guild {}", e.getChannel().getId(), e.getGuild().getId());
								int count = 0;
								StringBuilder out = new StringBuilder();
								for(int i = (page-1)*10; i < clans.size(); i++) {
									final var curClan = clans.get(i);
									out.append("**"+curClan.getName()+"** ("+curClan.getMembers()+")\n");
									if(count == 9) break;
									count++;
								}
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setAuthor(STATIC.getTranslation(e.getMember(), Translation.TOP_PAGE)+page+"/"+lastPage).setTitle(STATIC.getTranslation(e.getMember(), Translation.CLAN_TITLE)).setDescription(out.toString()).build()).queue(m2 -> {
									m2.addReaction(EmojiManager.getForAlias(":arrow_left:").getUnicode()).queue();
									m2.addReaction(EmojiManager.getForAlias(":arrow_right:").getUnicode()).queue();
									Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"me"+m2.getId()+"us"+e.getMember().getUser().getId(), clan.updateDescription(""+page).setExpiration(180000));
									Hashes.addTempCache("clan_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getId(), new Cache(180000));
								});
							});
						}
						else {
							STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MESSAGE_WRITE.getName()+", "+Permission.MESSAGE_HISTORY.getName()+" and "+Permission.MESSAGE_ADD_REACTION.getName())+"<#"+e.getChannel().getId()+">", Channel.LOG.getType());
							logger.error("MESSAGE_WRITE, MESSAGE_HISTORY and MESSAGE_REACTION_ADD permissions required to display a new clan page with reactions on text channel {} in guild {}", e.getChannel().getId(), e.getGuild().getId());
						}
					}
				}
				
				//check if it's a vote channel and allow only one reaction
				final var channels = Azrael.SQLgetChannels(e.getGuild().getIdLong());
				final var thisChannel = channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null);
				if(thisChannel != null && thisChannel.getChannel_Type() != null && (thisChannel.getChannel_Type().equals(Channel.VOT.getType()) || thisChannel.getChannel_Type().equals(Channel.VO2.getType()))) {
					if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY))) {
						final String [] reactions = GuildIni.getVoteReactions(e.getGuild().getIdLong());
						Object thumbsup = STATIC.retrieveEmoji(e.getGuild(), reactions[0], ":thumbsup:");
						Object thumbsdown = STATIC.retrieveEmoji(e.getGuild(), reactions[1], ":thumbsdown:");
						Object shrug = STATIC.retrieveEmoji(e.getGuild(), reactions[2], ":shrug:");
						
						boolean runSpreadsheet = false;
						if((e.getReactionEmote().isEmoji() && thumbsup instanceof String && ((String)thumbsup).equals(e.getReactionEmote().getName())) || (e.getReactionEmote().isEmote() && thumbsup instanceof Emote && ((Emote)thumbsup).getIdLong() == e.getReactionEmote().getIdLong())) {
							if(thumbsdown instanceof String)
								e.getChannel().removeReactionById(e.getMessageIdLong(), (String)thumbsdown, e.getUser()).queue();
							else
								e.getChannel().removeReactionById(e.getMessageIdLong(), (Emote)thumbsdown, e.getUser()).queue();
							if(thisChannel.getChannel_Type().equals(Channel.VO2.getType())) {
								if(shrug instanceof String)
									e.getChannel().removeReactionById(e.getMessageIdLong(), (String)shrug, e.getUser()).queue();
								else
									e.getChannel().removeReactionById(e.getMessageIdLong(), (Emote)shrug, e.getUser()).queue();
							}
							runSpreadsheet = true;
						}
						else if((e.getReactionEmote().isEmoji() && thumbsdown instanceof String && ((String)thumbsdown).equals(e.getReactionEmote().getName())) || (e.getReactionEmote().isEmote() && thumbsdown instanceof Emote && ((Emote)thumbsdown).getIdLong() == e.getReactionEmote().getIdLong())) {
							if(thumbsup instanceof String)
								e.getChannel().removeReactionById(e.getMessageIdLong(), (String)thumbsup, e.getUser()).queue();
							else
								e.getChannel().removeReactionById(e.getMessageIdLong(), (Emote)thumbsup, e.getUser()).queue();
							if(thisChannel.getChannel_Type().equals(Channel.VO2.getType())) {
								if(shrug instanceof String)
									e.getChannel().removeReactionById(e.getMessageIdLong(), (String)shrug, e.getUser()).queue();
								else
									e.getChannel().removeReactionById(e.getMessageIdLong(), (Emote)shrug, e.getUser()).queue();
							}
							runSpreadsheet = true;
						}
						else if((e.getReactionEmote().isEmoji() && shrug instanceof String && ((String)shrug).equals(e.getReactionEmote().getName())) || (e.getReactionEmote().isEmote() && shrug instanceof Emote && ((Emote)shrug).getIdLong() == e.getReactionEmote().getIdLong())) {
							if(thumbsdown instanceof String)
								e.getChannel().removeReactionById(e.getMessageIdLong(), (String)thumbsdown, e.getUser()).queue();
							else
								e.getChannel().removeReactionById(e.getMessageIdLong(), (Emote)thumbsdown, e.getUser()).queue();
							if(thumbsup instanceof String)
								e.getChannel().removeReactionById(e.getMessageIdLong(), (String)thumbsup, e.getUser()).queue();
							else
								e.getChannel().removeReactionById(e.getMessageIdLong(), (Emote)thumbsup, e.getUser()).queue();
							runSpreadsheet = true;
						}
						
						//Run google service, if enabled
						runVoteSpreadsheetService(runSpreadsheet, e);
					}
					else {
						STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MESSAGE_MANAGE.getName())+"<#"+e.getChannel().getId()+">", Channel.LOG.getType());
						logger.error("MESSAGE_MANAGE permission required to remove a reaction from a user and MESSAGE_HISTORY to retrieve the reaction count on channel {} in guild {}", e.getChannel().getId(), e.getGuild().getId());
					}
				}
			}
		}).start();
	}
	
	/**
	 * Print error that the manage roles permission is missing!
	 * @param e
	 */
	private static void printPermissionError(GuildMessageReactionAddEvent e) {
		STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION)+Permission.MANAGE_ROLES.getName(), Channel.LOG.getType());
		logger.warn("MANAGE ROLES permission required to apply reaction roles in guild {}", e.getGuild().getId());
	}
	
	private static void runVoteSpreadsheetService(boolean runSpreadsheet, GuildMessageReactionAddEvent e) {
		if(runSpreadsheet && GuildIni.getGoogleFunctionalitiesEnabled(e.getGuild().getIdLong()) && GuildIni.getGoogleSpreadsheetsEnabled(e.getGuild().getIdLong())) {
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
										final String [] reactions = GuildIni.getVoteReactions(e.getGuild().getIdLong());
										Object thumbsup = STATIC.retrieveEmoji(e.getGuild(), reactions[0], ":thumbsup:");
										Object thumbsdown = STATIC.retrieveEmoji(e.getGuild(), reactions[1], ":thumbsdown:");
										Object shrug = STATIC.retrieveEmoji(e.getGuild(), reactions[2], ":shrug:");
										int thumbsUpCount = 0;
										int thumbsDownCount = 0;
										int shrugCount = 0;
										for(final var reaction : e.getChannel().retrieveMessageById(e.getMessageId()).complete().getReactions()) {
											if(columnUpVote > 0 && ((reaction.getReactionEmote().isEmoji() && thumbsup instanceof String && reaction.getReactionEmote().getName().equals((String)thumbsup)) || (reaction.getReactionEmote().isEmote() && thumbsup instanceof Emote && reaction.getReactionEmote().getEmote().getIdLong() == ((Emote)thumbsup).getIdLong())))
												thumbsUpCount = reaction.getCount()-1;
											if(columnDownVote > 0 && ((reaction.getReactionEmote().isEmoji() && thumbsdown instanceof String && reaction.getReactionEmote().getName().equals((String)thumbsdown)) || (reaction.getReactionEmote().isEmote() && thumbsdown instanceof Emote && reaction.getReactionEmote().getEmote().getIdLong() == ((Emote)thumbsdown).getIdLong())))
												thumbsDownCount = reaction.getCount()-1;
											if(columnShrugVote > 0 && ((reaction.getReactionEmote().isEmoji() && shrug instanceof String && reaction.getReactionEmote().getName().equals((String)shrug)) || (reaction.getReactionEmote().isEmote() && shrug instanceof Emote && reaction.getReactionEmote().getEmote().getIdLong() == ((Emote)shrug).getIdLong())))
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
							runVoteSpreadsheetService(runSpreadsheet, e);
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
