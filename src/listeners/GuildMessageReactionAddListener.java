package listeners;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiParser;

import commandsContainer.RandomshopExecution;
import core.Hashes;
import core.UserPrivs;
import fileManagement.FileSetting;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import inventory.InventoryBuilder;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.DiscordRoles;
import sql.RankingSystem;
import sql.RankingSystemItems;
import sql.Azrael;

public class GuildMessageReactionAddListener extends ListenerAdapter{
	
	@Override
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent e) {
		if(!UserPrivs.isUserBot(e.getUser(), e.getGuild().getIdLong())) {
			if(!UserPrivs.isUserMuted(e.getUser(), e.getGuild().getIdLong())) {
				if(Azrael.SQLgetCommandExecutionReaction(e.getGuild().getIdLong())) {
					Logger logger = LoggerFactory.getLogger(GuildMessageReactionAddListener.class);
					if(DiscordRoles.SQLgetRolesByCategory(e.getGuild().getIdLong(), "rea")) {
						var rea_channel = Azrael.SQLgetChannelID(e.getGuild().getIdLong(), "rea");
						String reactionName = "";
						if((EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":one:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":two:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":three:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":four:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":five:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":six:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":seven:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":eight:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":nine:")) && e.getChannel().getIdLong() == rea_channel) {
							reactionName = EmojiParser.parseToAliases(e.getReactionEmote().getName()).replaceAll(":", "");
						}
						else if(e.getChannel().getIdLong() == rea_channel) {
							reactionName = e.getReactionEmote().getName();
						}
						
						if(reactionName.length() > 0) {
							String [] reactions = GuildIni.getReactions(e.getGuild().getIdLong());
							boolean emoteFound = false;
							if(reactions[0].equals("true")) {
								for(int i = 1; i < 10; i++) {
									if(reactions[i].length() > 0 && (reactionName.equals(reactions[i]) || EmojiParser.parseToAliases(reactionName).replaceAll(":", "").equals(reactions[i]))) {
										e.getGuild().getController().addSingleRoleToMember(e.getMember(), e.getGuild().getRoleById(Hashes.getRoles(i+"_"+e.getGuild().getId()).getRole_ID())).queue();
										emoteFound = true;
										break;
									}
								}
								if(emoteFound == false) {
									int emote = returnEmote(reactionName);
									e.getGuild().getController().addSingleRoleToMember(e.getMember(), e.getGuild().getRoleById(Hashes.getRoles(emote+"_"+e.getGuild().getId()).getRole_ID())).queue();
								}
							}
							else {
								int emote = returnEmote(reactionName);
								e.getGuild().getController().addSingleRoleToMember(e.getMember(), e.getGuild().getRoleById(Hashes.getRoles(emote+"_"+e.getGuild().getId()).getRole_ID())).queue();
							}
							logger.debug("{} received a role upon reacting in guild {}", e.getUser().getId(), e.getGuild().getName());
						}
					}
					else
						logger.error("Reaction roles couldn't be retrieved from DiscordRoles.roles in guild {}", e.getGuild().getName());
				}
			}
			
			if(EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":arrow_left:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":arrow_right:")) {
				//inventory reactions
				File inventory = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/inventory_gu"+e.getGuild().getId()+"me"+e.getMessageId()+"us"+e.getMember().getUser().getId()+".azr");
				//randomshop reactions
				File randomshop = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/randomshop_gu"+e.getGuild().getId()+"me"+e.getMessageId()+"us"+e.getMember().getUser().getId()+".azr");
				
				if(inventory.exists()) {
					ExecutorService executor = Executors.newSingleThreadExecutor();
					executor.execute(() -> {
						String file_content = FileSetting.readFile(inventory.getAbsolutePath());
						String [] array = file_content.split("_");
						int current_page = Integer.parseInt(array[0]);
						final int last_page = Integer.parseInt(array[1]);
						final String inventory_tab = array[2];
						final String sub_tab = array[3];
						if(EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":arrow_left:") && current_page != 1)
							current_page--;
						else if(EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":arrow_right:") && current_page != last_page)
							current_page++;
						e.getChannel().getMessageById(e.getMessageId()).complete().delete().queue();
						inventory.delete();
						FileSetting.createFile(IniFileReader.getTempDirectory()+"AutoDelFiles/inventory_bot_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+".azr", e.getMember().getUser().getId()+"_"+current_page+"_"+last_page+"_"+inventory_tab+"_"+sub_tab);
						final int maxItems = GuildIni.getInventoryMaxItems(e.getGuild().getIdLong());
						if(inventory_tab.equalsIgnoreCase("weapons")) {
							if(!sub_tab.equalsIgnoreCase("total"))
								InventoryBuilder.DrawInventory(null, e, inventory_tab, sub_tab, RankingSystem.SQLgetInventoryAndDescriptionsWeapons(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), ((current_page-1)*maxItems), maxItems, sub_tab), current_page, last_page);
							else
								InventoryBuilder.DrawInventory(null, e, inventory_tab, sub_tab, RankingSystem.SQLgetInventoryAndDescriptionsWeapons(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), ((current_page-1)*maxItems), maxItems), current_page, last_page);
						}
						else if(inventory_tab.equalsIgnoreCase("skins"))
							InventoryBuilder.DrawInventory(null, e, inventory_tab, sub_tab, RankingSystem.SQLgetInventoryAndDescriptionsSkins(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), ((current_page-1)*maxItems), maxItems), current_page, last_page);
						else
							InventoryBuilder.DrawInventory(null, e, inventory_tab, sub_tab, RankingSystem.SQLgetInventoryAndDescriptions(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), ((current_page-1)*maxItems), maxItems), current_page, last_page);
					});
				}
				else if(randomshop.exists()) {
					String file_content = FileSetting.readFile(randomshop.getAbsolutePath());
					String [] array = file_content.split("_");
					int current_page = Integer.parseInt(array[0]);
					final int last_page = Integer.parseInt(array[1]);
					final String input = array[2];
					if(EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":arrow_left:") && current_page != 1)
						current_page--;
					else if(EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":arrow_right:") && current_page != last_page)
						current_page++;
					e.getChannel().getMessageById(e.getMessageId()).complete().delete().queue();
					randomshop.delete();
					RandomshopExecution.inspectItems(null, e, RankingSystemItems.SQLgetWeaponAbbvs(e.getGuild().getIdLong()), RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong()), input, current_page);
				}
			}
		}
	}
	
	private int returnEmote(String reactionName) {
		switch(reactionName) {
			case "one":
				return 1;
			case "two":
				return 2;
			case "three":
				return 3;
			case "four":
				return 4;
			case "five":
				return 5;
			case "six":
				return 6;
			case "seven":
				return 7;
			case "eight":
				return 8;
			case "nine":
				return 9;
			default:
				return 0;
		}
	}
}
