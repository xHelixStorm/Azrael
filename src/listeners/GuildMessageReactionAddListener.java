package listeners;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiParser;

import commandsContainer.RandomshopExecution;
import constructors.Cache;
import core.Hashes;
import core.UserPrivs;
import enums.Translation;
import fileManagement.GuildIni;
import inventory.InventoryBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.DiscordRoles;
import sql.RankingSystem;
import sql.RankingSystemItems;
import util.STATIC;
import sql.Azrael;

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
			if(!UserPrivs.isUserBot(e.getGuild().getMember(e.getUser()))) {
				//any action below won't apply for muted users
				if(!UserPrivs.isUserMuted(e.getGuild().getMember(e.getUser()))) {
					//verify that the custom server reactions is enabled
					if(Azrael.SQLgetCommandExecutionReaction(e.getGuild().getIdLong())) {
						//check for any registered reaction channel and execute logic only if it happened in that channel
						var rea_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("rea")).findAny().orElse(null);
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
													e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(reactionRoles.get(i).getRole_ID())).queue();
													logger.debug("{} received a role upon reacting in guild {}", e.getUser().getId(), e.getGuild().getId());
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
													e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(reactionRoles.get(emote).getRole_ID())).queue();
													logger.debug("{} received a role upon reacting in guild {}", e.getUser().getId(), e.getGuild().getId());
												}
											}
											else
												printPermissionError(e);
										}
									}
									else {
										int emote = STATIC.returnEmote(reactionName);
										//check if the bot has the manage roles permission
										if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES))
											//A tenth emote possibility doesn't exist
											if(emote != 9) {
												e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(reactionRoles.get(emote).getRole_ID())).queue();
												logger.debug("{} received a role upon reacting in guild {}", e.getUser().getId(), e.getGuild().getId());
											}
										else
											printPermissionError(e);
									}
								}
							}
							else
								logger.error("Reaction roles couldn't be retrieved from DiscordRoles.roles in guild {}", e.getGuild().getId());
						}
						
						//check if a role has to be assigned
						long role_id = 0;
						boolean customEmoji = false;
						if(e.getReactionEmote().getName().replaceAll("[a-zA-Z0-9]*", "").length() != 0)
							role_id = DiscordRoles.SQLgetReactionRole(e.getMessageIdLong(), e.getReactionEmote().getAsCodepoints());
						else {
							role_id = DiscordRoles.SQLgetReactionRole(e.getMessageIdLong(), e.getReactionEmote().getName());
							customEmoji = true;
						}
						if(role_id != 0) {
							if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
								e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(role_id)).queue();
							}
							else {
								printPermissionError(e);
							}
						}
						
						//check if this reaction has to be added into a different message of the bot
						var cache = Hashes.getTempCache("write_edit_reaction_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getUserId()+"me"+e.getMessageId());
						if(cache != null && cache.getExpiration() - System.currentTimeMillis() > 0) {
							String channel_id = cache.getAdditionalInfo();
							String message_id = cache.getAdditionalInfo2();
							final boolean isEmojiCustom = customEmoji;
							//check that the permissions are correct
							if(e.getGuild().getSelfMember().hasPermission(e.getGuild().getTextChannelById(channel_id), Permission.MESSAGE_HISTORY)) {
								if(e.getGuild().getSelfMember().hasPermission(e.getGuild().getTextChannelById(channel_id), Permission.MESSAGE_ADD_REACTION)) {
									e.getGuild().getTextChannelById(channel_id).retrieveMessageById(message_id).queue(message -> {
										String reaction = null;
										//unicode emojis
										if(!isEmojiCustom) {
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
									logger.error("MESSAGE_ADD_REACTION permission missing for channel {} in guild {} to add a reaction", channel_id, e.getGuild().getId());
									Hashes.clearTempCache("write_edit_reaction_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getUserId()+"me"+e.getMessageId());
								}
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MESSAGE_HISTORY.getName()).build()).queue();
								logger.error("MESSAGE_HISTORY permission missing for channel {} in guild {} to read the history", channel_id, e.getGuild().getId());
								Hashes.clearTempCache("write_edit_reaction_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getUserId()+"me"+e.getMessageId());
							}
						}
					}
				}
				
				//check if either the arrow left or arrow right reaction has been used
				if(EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":arrow_left:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":arrow_right:")) {
					//inventory reactions
					var inventory = Hashes.getTempCache("inventory_gu"+e.getGuild().getId()+"me"+e.getMessageId()+"us"+e.getMember().getUser().getId());
					//randomshop reactions
					var randomshop = Hashes.getTempCache("randomshop_gu"+e.getGuild().getId()+"me"+e.getMessageId()+"us"+e.getMember().getUser().getId());
					
					//execute if it's an inventory reaction
					if(inventory != null) {
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
						//delete previously drawn inventory so that the new inventory image can be drawn
						e.getChannel().retrieveMessageById(e.getMessageId()).complete().delete().queue();
						Hashes.addTempCache("inventory_bot_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(60000, e.getMember().getUser().getId()+"_"+current_page+"_"+last_page+"_"+inventory_tab+"_"+sub_tab));
						//retrieve current theme and max items in the current inventory
						final var guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
						final var theme = guild_settings.getThemeID();
						final int maxItems = guild_settings.getInventoryMaxItems();
						//draw inventory depending on the category that was chosen
						if(inventory_tab.equalsIgnoreCase("weapons")) {
							if(!sub_tab.equalsIgnoreCase("total"))
								InventoryBuilder.DrawInventory(null, e, inventory_tab, sub_tab, RankingSystem.SQLgetInventoryAndDescriptionsWeapons(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), ((current_page-1)*maxItems), maxItems, sub_tab, theme), current_page, last_page, guild_settings);
							else
								InventoryBuilder.DrawInventory(null, e, inventory_tab, sub_tab, RankingSystem.SQLgetInventoryAndDescriptionsWeapons(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), ((current_page-1)*maxItems), maxItems, theme), current_page, last_page, guild_settings);
						}
						else if(inventory_tab.equalsIgnoreCase("skins"))
							InventoryBuilder.DrawInventory(null, e, inventory_tab, sub_tab, RankingSystem.SQLgetInventoryAndDescriptionsSkins(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), ((current_page-1)*maxItems), maxItems, theme), current_page, last_page, guild_settings);
						else
							InventoryBuilder.DrawInventory(null, e, inventory_tab, sub_tab, RankingSystem.SQLgetInventoryAndDescriptions(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), ((current_page-1)*maxItems), maxItems, theme), current_page, last_page, guild_settings);
					}
					//execute if it's a randomshop reaction
					else if(randomshop != null) {
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
						//delete previous randomshop image and redraw the new one
						e.getChannel().retrieveMessageById(e.getMessageId()).complete().delete().queue();
						final var theme = RankingSystem.SQLgetGuild(e.getGuild().getIdLong()).getThemeID();
						RandomshopExecution.inspectItems(null, e, RankingSystemItems.SQLgetWeaponAbbvs(e.getGuild().getIdLong(), theme), RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong(), theme, false), input, current_page);
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
		final var log_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
		if(log_channel != null) e.getGuild().getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS).replaceFirst("\\{\\}", e.getUser().getName()+"#"+e.getUser().getDiscriminator()).replace("{}", e.getUserId())+Permission.MANAGE_ROLES.getName()).build()).queue();
		logger.warn("MANAGE ROLES permission missing to apply reaction roles in guild {}!", e.getGuild().getId());
	}
}
