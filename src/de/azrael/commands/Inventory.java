package de.azrael.commands;

import java.awt.Color;
import java.io.File;
import java.util.EnumSet;
import java.util.stream.Collectors;

import org.jpastebin.exceptions.PasteException;
import org.jpastebin.pastebin.exceptions.LoginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Cache;
import de.azrael.constructors.Guilds;
import de.azrael.constructors.InventoryContent;
import de.azrael.core.Hashes;
import de.azrael.enums.Channel;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.inventory.InventoryBuilder;
import de.azrael.sql.Azrael;
import de.azrael.sql.RankingSystem;
import de.azrael.sql.RankingSystemItems;
import de.azrael.util.Pastebin;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * The Inventory command allows the user to inspect all
 * purchased or acquired items/skins/weapons/skills
 * @author xHelixStorm
 *
 */

public class Inventory implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Inventory.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.INVENTORY);
	}

	@Override
	public boolean action(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		//verify that the ranking system is enabled for the current server
		Guilds guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
		if(guild_settings.getRankingState()) {
			//retrieve all registered bot channels and print the inventory only in these channels.
			//if no bot channel has been registered, print in the current channel
			var bot_channels = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.BOT.getType())).collect(Collectors.toList());
			if(bot_channels.size() == 0 || bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null) != null) {
				//print the inventory in text format, if the inventory image is not available
				if(!new File("./files/RankingSystem/Inventory/inventory_blank.png").exists()) {
					StringBuilder out = new StringBuilder();
					for(InventoryContent inventory : RankingSystem.SQLgetInventoryAndDescriptionWithoutLimit(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())){
						out.append((inventory.getDescription() != null ? inventory.getDescription() : inventory.getWeaponDescription()+" "+inventory.getStat())+"\n");
					}
					if(out.length() == 0)
						out.append(STATIC.getTranslation(e.getMember(), Translation.INVENTORY_EMPTY));
					if(out.length() <= 2000)
						e.getChannel().sendMessage("```"+out.toString()+"```").queue();
					else {
						try {
							Pastebin.GuestPaste(STATIC.getTranslation(e.getMember(), Translation.INVENTORY_NAME), out.toString());
						} catch (IllegalStateException | LoginException | PasteException e1) {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PASTE)).setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_PASTE_ERR_2)).build()).queue();
							logger.error("Inventory of user {} couldn't be uploaded on pastebin in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
						}
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
					
					if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.MESSAGE_ATTACH_FILES) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.MESSAGE_ATTACH_FILES))) {
						//draw the inventory and assign a fitting tab image
						//write to cache so that reactions can be added, if there are multiple pages
						String drawTab = "";
						if(args.length > 0 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ITEMS))) {
							drawTab = "items_total";
							Hashes.addTempCache("inventory_bot_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(60000, e.getMember().getUser().getId()+"_"+(limit/maxItems+1)+"_"+(itemNumber+1)+"_"+drawTab));
							InventoryBuilder.DrawInventory(e.getGuild(), e.getMember(), e.getChannel(), "items", "total", RankingSystem.SQLgetInventoryAndDescriptionsItems(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), limit, maxItems), limit/maxItems+1, itemNumber+1, guild_settings);
						}
						else if(args.length > 0 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_WEAPONS))) {
							if(args.length > 1 && sub_cat != null) {
								drawTab = "weapons_"+args[1];
								Hashes.addTempCache("inventory_bot_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(60000, e.getMember().getUser().getId()+"_"+(limit/maxItems+1)+"_"+(itemNumber+1)+"_"+drawTab));
								InventoryBuilder.DrawInventory(e.getGuild(), e.getMember(), e.getChannel(), "weapons", args[1], RankingSystem.SQLgetInventoryAndDescriptionsWeapons(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), limit, maxItems, args[1]), limit/maxItems+1, itemNumber+1, guild_settings);
							}
							else {
								drawTab = "weapons_total";
								Hashes.addTempCache("inventory_bot_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(60000, e.getMember().getUser().getId()+"_"+(limit/maxItems+1)+"_"+(itemNumber+1)+"_"+drawTab));
								InventoryBuilder.DrawInventory(e.getGuild(), e.getMember(), e.getChannel(), "weapons", "total", RankingSystem.SQLgetInventoryAndDescriptionsWeapons(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), limit, maxItems), limit/maxItems+1, itemNumber+1, guild_settings);
							}
						}
						else if(args.length > 0 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_SKINS))) {
							drawTab = "skins_total";
							Hashes.addTempCache("inventory_bot_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(60000, e.getMember().getUser().getId()+"_"+(limit/maxItems+1)+"_"+(itemNumber+1)+"_"+drawTab));
							InventoryBuilder.DrawInventory(e.getGuild(), e.getMember(), e.getChannel(), "skins", "total", RankingSystem.SQLgetInventoryAndDescriptionsSkins(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), limit, maxItems), limit/maxItems+1, itemNumber+1, guild_settings);
						}
						else {
							drawTab = "total_total";
							Hashes.addTempCache("inventory_bot_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(60000, e.getMember().getUser().getId()+"_"+(limit/maxItems+1)+"_"+(itemNumber+1)+"_"+drawTab));
							InventoryBuilder.DrawInventory(e.getGuild(), e.getMember(), e.getChannel(), "total", "total", RankingSystem.SQLgetInventoryAndDescriptions(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), limit, maxItems), limit/maxItems+1, itemNumber+1, guild_settings);
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MESSAGE_ATTACH_FILES.getName()).build()).queue();
						logger.error("Permission MESSAGE_ATTACH_FILES required to display the inventory in channel {} for guild {}", e.getChannel().getId(), e.getGuild().getId());
					}
				}
			}
			else{
				e.getChannel().sendMessage(e.getMember().getAsMention()+STATIC.getTranslation(e.getMember(), Translation.NOT_BOT_CHANNEL)+STATIC.getChannels(bot_channels)).queue();
			}
		}
		else{
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.LEVEL_SYSTEM_NOT_ENABLED)).build()).queue();
		}
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Inventory command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.INVENTORY.getColumn(), out.toString().trim());
		}
	}
}
