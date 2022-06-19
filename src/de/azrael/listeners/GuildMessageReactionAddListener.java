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
import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Cache;
import de.azrael.constructors.CategoryConf;
import de.azrael.constructors.Channels;
import de.azrael.constructors.Clan;
import de.azrael.constructors.Dailies;
import de.azrael.constructors.InventoryContent;
import de.azrael.constructors.Roles;
import de.azrael.core.Hashes;
import de.azrael.core.UserPrivs;
import de.azrael.enums.Channel;
import de.azrael.enums.GoogleDD;
import de.azrael.enums.GoogleEvent;
import de.azrael.enums.Translation;
import de.azrael.google.GoogleSheets;
import de.azrael.google.GoogleUtils;
import de.azrael.inventory.InventoryBuilder;
import de.azrael.sql.Azrael;
import de.azrael.sql.BotConfiguration;
import de.azrael.sql.DiscordRoles;
import de.azrael.sql.RankingSystem;
import de.azrael.sql.RankingSystemItems;
import de.azrael.threads.DelayedGoogleUpdate;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
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
					//pagination reactions
					final var pagination = Hashes.getTempCache("pagination_gu"+e.getGuild().getId()+"me"+e.getMessageId()+"us"+e.getMember().getUser().getId());
					
					//execute if it's an inventory reaction
					if(inventory != null && inventory.getExpiration() >= System.currentTimeMillis()) {
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
							STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MESSAGE_WRITE.getName()+", "+Permission.MESSAGE_ATTACH_FILES.getName())+"<#"+e.getChannel().getId()+">", Channel.LOG.getType());
							logger.error("MESSAGE_WRITE and MESSAGE_ATTACH_FILES permission required to reupload a new image in channel {} in guild {}", e.getChannel().getId(), e.getGuild().getId());
						}
					}
					//execute if it's a randomshop reaction
					else if(randomshop != null && randomshop.getExpiration() >= System.currentTimeMillis()) {
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
							STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MESSAGE_WRITE.getName()+", "+Permission.MESSAGE_ATTACH_FILES.getName())+"<#"+e.getChannel().getId()+">", Channel.LOG.getType());
							logger.error("MESSAGE_WRITE and MESSAGE_ATTACH_FILES permission required to reupload a new image on text channel {} in guild {}", e.getChannel().getId(), e.getGuild().getId());
						}
					}
					//execute if it's a clan reaction
					else if(clan != null && clan.getExpiration() >= System.currentTimeMillis()) {
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
							STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MESSAGE_WRITE.getName()+", "+Permission.MESSAGE_HISTORY.getName()+", "+Permission.MESSAGE_ADD_REACTION.getName())+"<#"+e.getChannel().getId()+">", Channel.LOG.getType());
							logger.error("MESSAGE_WRITE, MESSAGE_HISTORY and MESSAGE_REACTION_ADD permissions required to display a new clan page with reactions on text channel {} in guild {}", e.getChannel().getId(), e.getGuild().getId());
						}
					}
					//execute if it's a pagination reaction
					else if(pagination != null && pagination.getExpiration() >= System.currentTimeMillis()) {
						if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_HISTORY, Permission.MESSAGE_MANAGE) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_HISTORY, Permission.MESSAGE_MANAGE))) {
							final int curPage = Integer.parseInt(pagination.getAdditionalInfo());
							if(EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":arrow_left:")) {
								if(curPage == 1) {
									e.getReaction().removeReaction(e.getMember().getUser()).queue();
									Hashes.addTempCache("pagination_gu"+e.getGuild().getId()+"me"+e.getMessageId()+"us"+e.getMember().getUser().getId(), pagination.setExpiration(180000));
								}
								else {
									final Object object = pagination.getObject();
									final int pageCount = Integer.parseInt(pagination.getAdditionalInfo3());
									final int maxPage = (((List<?>)object).size()/pageCount)+(((List<?>)object).size()%pageCount > 0 ? 1 : 0);
									e.getChannel().retrieveMessageById(e.getMessageId()).queue(m -> {
										StringBuilder out = new StringBuilder();
										Object prevItem = null;
										int count = 0;
										for(final Object item : (List<?>)object) {
											if(count == ((curPage-1)*pageCount)) break;
											else if(count >= ((curPage-2)*pageCount)) {
												out.append(buildPaginationMessage(e.getMember(), item, prevItem, pagination.getAdditionalInfo2()));
												if(itemObjectValidation(item, prevItem))
													count++;
											}
											else
												count++;
											prevItem = item;
										}
										//Rebuild message embed with new description
										m.editMessage(rebuildEmbed(m.getEmbeds().get(0), out.toString(), curPage-1, maxPage).build()).queue();
										e.getReaction().removeReaction(e.getMember().getUser()).queue();
									});
									Hashes.addTempCache("pagination_gu"+e.getGuild().getId()+"me"+e.getMessageId()+"us"+e.getMember().getUser().getId(), pagination.setExpiration(180000).updateDescription(""+(curPage-1)));
								}
							}
							else {
								final Object object = pagination.getObject();
								final int pageCount = Integer.parseInt(pagination.getAdditionalInfo3());
								final int maxPage = (((List<?>)object).size()/pageCount)+(((List<?>)object).size()%pageCount > 0 ? 1 : 0);
								if(curPage == maxPage) {
									e.getReaction().removeReaction(e.getMember().getUser()).queue();
									Hashes.addTempCache("pagination_gu"+e.getGuild().getId()+"me"+e.getMessageId()+"us"+e.getMember().getUser().getId(), pagination.setExpiration(180000));
								}
								else {
									e.getChannel().retrieveMessageById(e.getMessageId()).queue(m -> {
										StringBuilder out = new StringBuilder();
										Object prevItem = null;
										int count = 0;
										for(final Object item : (List<?>)object) {
											if(count == (curPage+1)*pageCount) break;
											else if(count >= (curPage*pageCount)) {
												out.append(buildPaginationMessage(e.getMember(), item, prevItem, pagination.getAdditionalInfo2()));
												if(itemObjectValidation(item, prevItem))
													count++;
											}
											else
												count++;
											prevItem = item;
										}
										//Rebuild message embed with new description
										m.editMessage(rebuildEmbed(m.getEmbeds().get(0), out.toString(), curPage+1, maxPage).build()).queue();
										e.getReaction().removeReaction(e.getMember().getUser()).queue();
									});
									Hashes.addTempCache("pagination_gu"+e.getGuild().getId()+"me"+e.getMessageId()+"us"+e.getMember().getUser().getId(), pagination.setExpiration(180000).updateDescription(""+(curPage+1)));
								}
							}
						}
						else {
							STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MESSAGE_WRITE.getName()+", "+Permission.MESSAGE_HISTORY.getName()+", "+Permission.MESSAGE_MANAGE.getName())+"<#"+e.getChannel().getId()+">", Channel.LOG.getType());
							logger.error("MESSAGE_WRITE, MESSAGE_HISTORY and MESSAGE_MANAGE permissions required to update the embed pagination on text channel {} in guild {}", e.getChannel().getId(), e.getGuild().getId());
						}
					}
				}
				
				//check if it's a vote channel and allow only one reaction
				final var channels = Azrael.SQLgetChannels(e.getGuild().getIdLong());
				final var thisChannel = channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null);
				if(thisChannel != null && thisChannel.getChannel_Type() != null && (thisChannel.getChannel_Type().equals(Channel.VOT.getType()) || thisChannel.getChannel_Type().equals(Channel.VO2.getType()))) {
					if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY))) {
						BotConfigs botConfig = BotConfiguration.SQLgetBotConfigs(e.getGuild().getIdLong());
						final String [] reactions = botConfig.getVoteReactions();
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
						runVoteSpreadsheetService(runSpreadsheet, e, botConfig);
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
	
	private static void runVoteSpreadsheetService(boolean runSpreadsheet, GuildMessageReactionAddEvent e, BotConfigs botConfig) {
		if(runSpreadsheet && BotConfiguration.SQLgetBotConfigs(e.getGuild().getIdLong()).getGoogleFunctionalities()) {
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
										final String [] reactions = botConfig.getVoteReactions();
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
							runVoteSpreadsheetService(runSpreadsheet, e, botConfig);
						}
					} catch (Exception e1) {
						STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED), STATIC.getTranslation2(e.getGuild(), Translation.GOOGLE_WEBSERVICE)+e1.getMessage(), Channel.LOG.getType());
						logger.error("Google Spreadsheet webservice error for event VOTE in guild {}", e.getGuild().getIdLong(), e1);
					}
				}
			}
		}
	}
	
	private static String buildPaginationMessage(Member member, Object item, Object prevItem, String method) {
		//Derived from H!display roles
		if(item instanceof Role) 
			return "**"+((Role)item).getName()+"** ("+((Role)item).getId()+")\n";
		//Derived from H!display registered-roles
		else if(item instanceof Roles && method.equals("1")) { 
			final var role = (Roles)item;
			return (!role.getCategory_ABV().equals("def") ?
					"**"+role.getRole_Name()+ "**("+role.getRole_ID()+")\n"
					+ STATIC.getTranslation(member, Translation.DISPLAY_ROLE_TYPE)+role.getCategory_Name()+"\n"
					+ STATIC.getTranslation(member, Translation.DISPLAY_PERMISSION_LEVEL)+role.getLevel()+"\n"
					+ STATIC.getTranslation(member, Translation.DISPLAY_PERSISTANT)+(role.isPersistent() ? STATIC.getTranslation(member, Translation.DISPLAY_IS_PERSISTANT) : STATIC.getTranslation(member, Translation.DISPLAY_IS_NOT_PERSISTANT))+"\n\n"
					: "");
		}
		//Derived from H!display ranking-roles
		else if(item instanceof Roles && method.equals("2"))
			return "**"+((Roles)item).getRole_Name()+"** ("+ ((Roles)item).getRole_ID() +")\n"+STATIC.getTranslation(member, Translation.DISPLAY_UNLOCK_LEVEL)+((Roles)item).getLevel()+"\n";
		//Derived from H!display categories
		else if(item instanceof Category)
			return "**"+((Category)item).getName()+"** ("+((Category)item).getId()+")\n";
		//Derived from H!display registered-categories
		else if(item instanceof CategoryConf) {
			Category category = member.getGuild().getCategoryById(((CategoryConf)item).getCategoryID());
			return (category != null ? "**"+category.getName()+"** ("+category.getId()+")\n"+STATIC.getTranslation(member, Translation.DISPLAY_CATEGORY_TYPE)+((CategoryConf)item).getType()+"\n\n" : "");
		}
		//Derived from H!display text-channels
		else if(item instanceof TextChannel)
			return "**"+((TextChannel)item).getName()+"** ("+((TextChannel)item).getId()+")\n";
		//Derived from H!display voice-channels
		else if(item instanceof VoiceChannel)
			return "**"+((VoiceChannel)item).getName()+"** ("+((VoiceChannel)item).getId()+")\n";
		//Derived from H!display registered-channels
		else if(item instanceof Channels) {
			Channels channel = (Channels)item;
			if(itemObjectValidation(item, prevItem))
				return (prevItem != null ? "\n\n" : "")+"**"+channel.getChannel_Name()+"** ("+channel.getChannel_ID()+")\n"
						+ STATIC.getTranslation(member, Translation.DISPLAY_CHANNEL_TYPE)+(channel.getChannel_Type_Name() != null ? channel.getChannel_Type_Name() : STATIC.getTranslation(member, Translation.NOT_AVAILABLE))+"\n"
						+ STATIC.getTranslation(member, Translation.DISPLAY_URL_CENSORING)+(channel.getURLCensoring() ? STATIC.getTranslation(member, Translation.DISPLAY_IS_ENABLED) : STATIC.getTranslation(member, Translation.DISPLAY_IS_NOT_ENABLED))+"\n"
						+ STATIC.getTranslation(member, Translation.DISPLAY_TEXT_CENSORING)+(channel.getTxtRemoval() ? STATIC.getTranslation(member, Translation.DISPLAY_IS_ENABLED) : STATIC.getTranslation(member, Translation.DISPLAY_IS_NOT_ENABLED))+"\n"
						+ STATIC.getTranslation(member, Translation.DISPLAY_LANG_CENSORING)+(channel.getLang_Filter() != null ? channel.getLang_Filter() : STATIC.getTranslation(member, Translation.NOT_AVAILABLE));
			else
				return ", "+channel.getLang_Filter();
		}
		//Derived from H!display dailies
		else if(item instanceof Dailies)
			return "**"+((Dailies)item).getDescription()+"**\n"+STATIC.getTranslation(member, Translation.DISPLAY_PROBABILITY)+((Dailies)item).getWeight()+"%\n\n";
		//Derived from H!display watchedUsers
		else if(item instanceof String && method.equals("1"))
			return "**"+(String)item+"**\n";
		//Derived from H!user information
		else if(item instanceof String && method.equals("2"))
			return "[`"+(String)item+"`] ";
		else if(item instanceof String && method.equals("3"))
			return (String)item+"\n";
		//Derived from H!inventory
		else if(item instanceof InventoryContent)
			return (((InventoryContent)item).getDescription() != null ? ((InventoryContent)item).getDescription() : ((InventoryContent)item).getWeaponDescription()+" "+((InventoryContent)item).getStat())+"\n";
		return "";
	}
	
	private static boolean itemObjectValidation(Object item, Object prevItem) {
		if(prevItem == null)
			return true;
		if(prevItem instanceof Channels && ((Channels)prevItem).getChannel_ID() == ((Channels)item).getChannel_ID())
			return false;
		return true;
	}
	
	private static EmbedBuilder rebuildEmbed(MessageEmbed embed, String out, int newPage, int maxPage) {
		EmbedBuilder embedBuilder = new EmbedBuilder().setColor(embed.getColor()).setTitle(embed.getTitle());
		for(final var field : embed.getFields()) {
			embedBuilder.addField(field.getName(), field.getValue(), field.isInline());
		}
		if(embed.getThumbnail() != null)
			embedBuilder.setThumbnail(embed.getThumbnail().getUrl());
		embedBuilder.setFooter(newPage+"/"+maxPage);
		embedBuilder.setDescription(out);
		return embedBuilder;
	}
}
