package de.azrael.commands;

import java.awt.Color;
import java.util.EnumSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Cache;
import de.azrael.constructors.Guilds;
import de.azrael.constructors.InventoryContent;
import de.azrael.enums.Channel;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.inventory.InventoryBuilder;
import de.azrael.sql.Azrael;
import de.azrael.sql.RankingSystem;
import de.azrael.sql.RankingSystemItems;
import de.azrael.util.Hashes;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * The Inventory command allows the user to inspect all
 * purchased or acquired items/skins/weapons/skills
 * @author xHelixStorm
 *
 */

public class Inventory implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Inventory.class);

	@Override
	public boolean called(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.INVENTORY);
	}

	@Override
	public boolean action(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		//verify that the ranking system is enabled for the current server
		Guilds guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
		if(guild_settings.getRankingState()) {
			//retrieve all registered bot channels and print the inventory only in these channels.
			//if no bot channel has been registered, print in the current channel
			var bot_channels = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.BOT.getType())).collect(Collectors.toList());
			if(bot_channels.size() == 0 || bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null) != null) {
				//print the inventory in text format, if the inventory image is not available
				if(guild_settings.getInventoryId() == 0) {
					StringBuilder out = new StringBuilder();
					final var items = RankingSystem.SQLgetInventoryAndDescriptionWithoutLimit(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong());
					int count = 0;
					for(InventoryContent inventory : items) {
						if(count == 10) break;
						out.append((inventory.getDescription() != null ? inventory.getDescription() : inventory.getWeaponDescription()+" "+inventory.getStat())+"\n");
						count++;
					}
					if(out.length() == 0)
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.INVENTORY_EMPTY)).build()).queue();
					else {
						final int maxPage = (items.size()/10)+(items.size()%10 > 0 ? 1 : 0);
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setFooter("1/"+maxPage).setDescription(out.toString()).build()).queue(m -> {
							STATIC.addPaginationReactions(e, m, maxPage, "1", "10", items);
						});
					}
				}
				//handle preparation to draw the inventory
				else {
					int limit = 0;
					int itemNumber;
					//check if an additional parameter has been added for the category and search for it
					String sub_cat = RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong(), true).parallelStream().filter(c -> c.equalsIgnoreCase((args.length > 1 ? args[1] : ""))).findAny().orElse(null);
					//retrieve the number of all available items in the inventory
					final var maxItems = guild_settings.getInventoryMaxItems();
					//retrieve the number of items to display basing on the category
					if(args.length > 0) {
					if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ITEMS)))
							itemNumber = RankingSystem.SQLgetTotalItemNumber(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "ite", maxItems);
						else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_WEAPONS))) {
							if(args.length > 1 && sub_cat != null)
								itemNumber = RankingSystem.SQLgetTotalItemNumber(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), maxItems, true, args[1]);
							else
								itemNumber = RankingSystem.SQLgetTotalItemNumber(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), maxItems, false);
						}
						else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_SKINS))) {
							itemNumber = RankingSystem.SQLgetTotalItemNumber(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), maxItems, "ite", true);
						}
						else
							itemNumber = RankingSystem.SQLgetTotalItemNumber(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), maxItems);
					}
					else
						itemNumber = RankingSystem.SQLgetTotalItemNumber(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), maxItems);
					
					if(e.getGuild().getSelfMember().hasPermission(e.getGuildChannel(), Permission.MESSAGE_ATTACH_FILES) || STATIC.setPermissions(e.getGuild(), e.getChannel().asTextChannel(), EnumSet.of(Permission.MESSAGE_ATTACH_FILES))) {
						//draw the inventory and assign a fitting tab image
						//write to cache so that reactions can be added, if there are multiple pages
						String drawTab = "";
						if(args.length > 0 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ITEMS))) {
							drawTab = "items_total";
							Hashes.addTempCache("inventory_bot_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(60000, e.getMember().getUser().getId()+"_"+(limit/maxItems+1)+"_"+(itemNumber+1)+"_"+drawTab));
							InventoryBuilder.drawInventory(e.getGuild(), e.getMember(), e.getChannel().asTextChannel(), "items", "total", RankingSystem.SQLgetInventoryAndDescriptionsItems(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), limit, maxItems), limit/maxItems+1, itemNumber+1, guild_settings);
						}
						else if(args.length > 0 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_WEAPONS))) {
							if(args.length > 1 && sub_cat != null) {
								drawTab = "weapons_"+args[1];
								Hashes.addTempCache("inventory_bot_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(60000, e.getMember().getUser().getId()+"_"+(limit/maxItems+1)+"_"+(itemNumber+1)+"_"+drawTab));
								InventoryBuilder.drawInventory(e.getGuild(), e.getMember(), e.getChannel().asTextChannel(), "weapons", args[1], RankingSystem.SQLgetInventoryAndDescriptionsWeapons(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), limit, maxItems, args[1]), limit/maxItems+1, itemNumber+1, guild_settings);
							}
							else {
								drawTab = "weapons_total";
								Hashes.addTempCache("inventory_bot_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(60000, e.getMember().getUser().getId()+"_"+(limit/maxItems+1)+"_"+(itemNumber+1)+"_"+drawTab));
								InventoryBuilder.drawInventory(e.getGuild(), e.getMember(), e.getChannel().asTextChannel(), "weapons", "total", RankingSystem.SQLgetInventoryAndDescriptionsWeapons(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), limit, maxItems), limit/maxItems+1, itemNumber+1, guild_settings);
							}
						}
						else if(args.length > 0 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_SKINS))) {
							drawTab = "skins_total";
							Hashes.addTempCache("inventory_bot_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(60000, e.getMember().getUser().getId()+"_"+(limit/maxItems+1)+"_"+(itemNumber+1)+"_"+drawTab));
							InventoryBuilder.drawInventory(e.getGuild(), e.getMember(), e.getChannel().asTextChannel(), "skins", "total", RankingSystem.SQLgetInventoryAndDescriptionsSkins(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), limit, maxItems), limit/maxItems+1, itemNumber+1, guild_settings);
						}
						else {
							drawTab = "total_total";
							Hashes.addTempCache("inventory_bot_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(60000, e.getMember().getUser().getId()+"_"+(limit/maxItems+1)+"_"+(itemNumber+1)+"_"+drawTab));
							InventoryBuilder.drawInventory(e.getGuild(), e.getMember(), e.getChannel().asTextChannel(), "total", "total", RankingSystem.SQLgetInventoryAndDescriptions(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), limit, maxItems), limit/maxItems+1, itemNumber+1, guild_settings);
						}
					}
					else {
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MESSAGE_ATTACH_FILES.getName()).build()).queue();
						logger.error("Permission MESSAGE_ATTACH_FILES required to display the inventory in channel {} for guild {}", e.getChannel().getId(), e.getGuild().getId());
					}
				}
			}
			else{
				e.getChannel().sendMessage(e.getMember().getAsMention()+STATIC.getTranslation(e.getMember(), Translation.NOT_BOT_CHANNEL)+STATIC.getChannels(bot_channels)).queue();
			}
		}
		else{
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.LEVEL_SYSTEM_NOT_ENABLED)).build()).queue();
		}
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, MessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Inventory command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.INVENTORY.getColumn(), out.toString().trim());
		}
	}
}
