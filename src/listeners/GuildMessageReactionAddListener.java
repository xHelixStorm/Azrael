package listeners;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiParser;

import commandsContainer.RandomshopExecution;
import constructors.Cache;
import core.Hashes;
import core.UserPrivs;
import fileManagement.GuildIni;
import inventory.InventoryBuilder;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.DiscordRoles;
import sql.RankingSystem;
import sql.RankingSystemItems;
import sql.Azrael;

public class GuildMessageReactionAddListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(GuildMessageReactionAddListener.class);
	
	@Override
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent e) {
		if(!UserPrivs.isUserBot(e.getUser(), e.getGuild().getIdLong())) {
			if(!UserPrivs.isUserMuted(e.getUser(), e.getGuild().getIdLong())) {
				if(Azrael.SQLgetCommandExecutionReaction(e.getGuild().getIdLong())) {
					if(DiscordRoles.SQLgetRolesByCategory(e.getGuild().getIdLong(), "rea")) {
						var rea_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("rea")).findAny().orElse(null);
						String reactionName = "";
						if((EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":one:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":two:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":three:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":four:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":five:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":six:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":seven:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":eight:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":nine:")) && e.getChannel().getIdLong() == rea_channel.getChannel_ID()) {
							reactionName = EmojiParser.parseToAliases(e.getReactionEmote().getName()).replaceAll(":", "");
						}
						else if(rea_channel != null && e.getChannel().getIdLong() == rea_channel.getChannel_ID()) {
							reactionName = e.getReactionEmote().getName();
						}
						
						if(reactionName.length() > 0) {
							String [] reactions = GuildIni.getReactions(e.getGuild().getIdLong());
							boolean emoteFound = false;
							if(reactions[0].equals("true")) {
								for(int i = 1; i < 10; i++) {
									if(reactions[i].length() > 0 && (reactionName.equals(reactions[i]) || EmojiParser.parseToAliases(reactionName).replaceAll(":", "").equals(reactions[i]))) {
										e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(Hashes.getRoles(i+"_"+e.getGuild().getId()).getRole_ID())).queue();
										emoteFound = true;
										break;
									}
								}
								if(emoteFound == false) {
									int emote = returnEmote(reactionName);
									e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(Hashes.getRoles(emote+"_"+e.getGuild().getId()).getRole_ID())).queue();
								}
							}
							else {
								int emote = returnEmote(reactionName);
								e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(Hashes.getRoles(emote+"_"+e.getGuild().getId()).getRole_ID())).queue();
							}
							logger.debug("{} received a role upon reacting in guild {}", e.getUser().getId(), e.getGuild().getId());
						}
					}
					else
						logger.error("Reaction roles couldn't be retrieved from DiscordRoles.roles in guild {}", e.getGuild().getId());
				}
			}
			
			if(EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":arrow_left:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":arrow_right:")) {
				//inventory reactions
				var inventory = Hashes.getTempCache("inventory_gu"+e.getGuild().getId()+"me"+e.getMessageId()+"us"+e.getMember().getUser().getId());
				//randomshop reactions
				var randomshop = Hashes.getTempCache("randomshop_gu"+e.getGuild().getId()+"me"+e.getMessageId()+"us"+e.getMember().getUser().getId());
				
				if(inventory != null) {
					ExecutorService executor = Executors.newSingleThreadExecutor();
					executor.execute(() -> {
						String cache_content = inventory.getAdditionalInfo();
						String [] array = cache_content.split("_");
						int current_page = Integer.parseInt(array[0]);
						final int last_page = Integer.parseInt(array[1]);
						final String inventory_tab = array[2];
						final String sub_tab = array[3];
						if(EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":arrow_left:") && current_page != 1)
							current_page--;
						else if(EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":arrow_right:") && current_page != last_page)
							current_page++;
						e.getChannel().retrieveMessageById(e.getMessageId()).complete().delete().queue();
						Hashes.addTempCache("inventory_bot_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(60000, e.getMember().getUser().getId()+"_"+current_page+"_"+last_page+"_"+inventory_tab+"_"+sub_tab));
						final int maxItems = GuildIni.getInventoryMaxItems(e.getGuild().getIdLong());
						final var theme = RankingSystem.SQLgetGuild(e.getGuild().getIdLong()).getThemeID();
						var guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
						if(inventory_tab.equalsIgnoreCase("weapons")) {
							if(!sub_tab.equalsIgnoreCase("total"))
								InventoryBuilder.DrawInventory(null, e, inventory_tab, sub_tab, RankingSystem.SQLgetInventoryAndDescriptionsWeapons(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), ((current_page-1)*maxItems), maxItems, sub_tab, theme), current_page, last_page, guild_settings.getThemeID());
							else
								InventoryBuilder.DrawInventory(null, e, inventory_tab, sub_tab, RankingSystem.SQLgetInventoryAndDescriptionsWeapons(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), ((current_page-1)*maxItems), maxItems, theme), current_page, last_page, guild_settings.getThemeID());
						}
						else if(inventory_tab.equalsIgnoreCase("skins"))
							InventoryBuilder.DrawInventory(null, e, inventory_tab, sub_tab, RankingSystem.SQLgetInventoryAndDescriptionsSkins(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), ((current_page-1)*maxItems), maxItems, theme), current_page, last_page, guild_settings.getThemeID());
						else
							InventoryBuilder.DrawInventory(null, e, inventory_tab, sub_tab, RankingSystem.SQLgetInventoryAndDescriptions(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), ((current_page-1)*maxItems), maxItems, theme), current_page, last_page, guild_settings.getThemeID());
					});
				}
				else if(randomshop != null) {
					String cache_content = randomshop.getAdditionalInfo();
					String [] array = cache_content.split("_");
					int current_page = Integer.parseInt(array[0]);
					final int last_page = Integer.parseInt(array[1]);
					final String input = array[2];
					if(EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":arrow_left:") && current_page != 1)
						current_page--;
					else if(EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":arrow_right:") && current_page != last_page)
						current_page++;
					e.getChannel().retrieveMessageById(e.getMessageId()).complete().delete().queue();
					final var theme = RankingSystem.SQLgetGuild(e.getGuild().getIdLong()).getThemeID();
					RandomshopExecution.inspectItems(null, e, RankingSystemItems.SQLgetWeaponAbbvs(e.getGuild().getIdLong(), theme), RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong(), theme, false), input, current_page);
				}
			}
		}
	}
	
	@SuppressWarnings("preview")
	private int returnEmote(String reactionName) {
		return switch(reactionName) {
			case "one" 	 -> 1;
			case "two"   -> 2;
			case "three" -> 3;
			case "four"  -> 4;
			case "five"  -> 5;
			case "six" 	 -> 6;
			case "seven" -> 7;
			case "eight" -> 8;
			case "nine"  -> 9;
			default 	 -> 0;
		};
	}
}
