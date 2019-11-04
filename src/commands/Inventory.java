package commands;

/**
 * The Inventory command allows the user to inspect all
 * purchased or acquired items/skins/weapons/skills
 */

import java.awt.Color;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import constructors.Guilds;
import constructors.InventoryContent;
import core.Hashes;
import core.UserPrivs;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import inventory.InventoryBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.RankingSystem;
import sql.RankingSystemItems;
import util.STATIC;
import sql.Azrael;

public class Inventory implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Inventory.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		//check if the command is enabled and that the user has enough permissions
		if(GuildIni.getInventoryCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getInventoryLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		//verify that the ranking system is enabled for the current server
		Guilds guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
		if(guild_settings.getRankingState()) {
			//retrieve all registered bot channels and print the inventory only in these channels.
			//if no bot channel has been registered, print in the current channel
			var bot_channels = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("bot")).collect(Collectors.toList());
			if(bot_channels.size() == 0 || bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null) != null) {
				final String prefix = GuildIni.getCommandPrefix(e.getGuild().getIdLong());
				//print the help message if the -help parameter has been used
				if(args.length > 0 && args[0].equalsIgnoreCase("-help")) {
					EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
					e.getChannel().sendMessage(message.setDescription("- Type **-list** after the command to display the whole inventory as a list\n"
							+ "- Type **-page** and then the page together with the command to directly select the page you wish to view\n"
							+ "- Type the tab name to filter your inventory item by type. Available types are **items**, **weapons** and **skins**\n"
							+ "- Type the sub tab after the tab name together with the command to further filter your inventory selection").build()).queue();
				}
				//print the inventory in text format
				else if(args.length > 0 && args[0].equalsIgnoreCase("-list")) {
					StringBuilder out = new StringBuilder();
					for(InventoryContent inventory : RankingSystem.SQLgetInventoryAndDescriptionWithoutLimit(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), guild_settings.getThemeID())){
						out.append((inventory.getDescription() != null ? inventory.getDescription() : inventory.getWeaponDescription()+" "+inventory.getStat())+"\n");
					}
					if(out.length() == 0)
						out.append("Inventory is empty!");
					e.getChannel().sendMessage("```"+out+"```").queue();
				}
				//handle preparation to draw the inventory
				else {
					int limit = 0;
					int itemNumber;
					//check if an additional parameter has been added for the category and search for it
					String sub_cat = RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong(), guild_settings.getThemeID(), true).parallelStream().filter(c -> c.equalsIgnoreCase((args.length > 1 ? args[1] : ""))).findAny().orElse(null);
					//retrieve the number of all available items in the inventory
					final var maxItems = guild_settings.getInventoryMaxItems();
					//retrieve the number of items to display basing on the category
					if(e.getMessage().getContentRaw().toLowerCase().contains("items"))
						itemNumber = RankingSystem.SQLgetTotalItemNumber(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "ite", maxItems, guild_settings.getThemeID());
					else if(e.getMessage().getContentRaw().toLowerCase().contains("weapons")) {
						if(args.length > 1 && sub_cat != null)
							itemNumber = RankingSystem.SQLgetTotalItemNumber(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), maxItems, true, args[1], guild_settings.getThemeID());
						else
							itemNumber = RankingSystem.SQLgetTotalItemNumber(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), maxItems, false, guild_settings.getThemeID());
					}
					else if(e.getMessage().getContentRaw().toLowerCase().contains("skins")) {
						itemNumber = RankingSystem.SQLgetTotalItemNumber(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), maxItems, "ite", true, guild_settings.getThemeID());
					}
					else
						itemNumber = RankingSystem.SQLgetTotalItemNumber(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), maxItems, guild_settings.getThemeID());
					//check if the user wishes to jump to a specific page directly
					if(e.getMessage().getContentRaw().contains(prefix+"inventory -page ")){
						try {
							limit = Integer.parseInt(e.getMessage().getContentRaw().replaceAll("[^0-9]", ""))-1;
							if(limit <= itemNumber){
								limit*=maxItems;
							}
						} catch(NumberFormatException nfe){
							limit = 0;
						}
					}
					
					//draw the inventory and assign a fitting tab image
					//write to cache so that reactions can be added, if there are multiple pages
					String drawTab = "";
					if(args.length > 0 && args[0].equalsIgnoreCase("items")) {
						drawTab = "items_total";
						Hashes.addTempCache("inventory_bot_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(60000, e.getMember().getUser().getId()+"_"+(limit/maxItems+1)+"_"+(itemNumber+1)+"_"+drawTab));
						InventoryBuilder.DrawInventory(e, null, "items", "total", RankingSystem.SQLgetInventoryAndDescriptionsItems(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), limit, maxItems, guild_settings.getThemeID()), limit/maxItems+1, itemNumber+1, guild_settings);
					}
					else if(args.length > 0 && args[0].equalsIgnoreCase("weapons")) {
						if(args.length > 1 && sub_cat != null) {
							drawTab = "weapons_"+args[1];
							Hashes.addTempCache("inventory_bot_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(60000, e.getMember().getUser().getId()+"_"+(limit/maxItems+1)+"_"+(itemNumber+1)+"_"+drawTab));
							InventoryBuilder.DrawInventory(e, null, "weapons", args[1], RankingSystem.SQLgetInventoryAndDescriptionsWeapons(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), limit, maxItems, args[1], guild_settings.getThemeID()), limit/maxItems+1, itemNumber+1, guild_settings);
						}
						else {
							drawTab = "weapons_total";
							Hashes.addTempCache("inventory_bot_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(60000, e.getMember().getUser().getId()+"_"+(limit/maxItems+1)+"_"+(itemNumber+1)+"_"+drawTab));
							InventoryBuilder.DrawInventory(e, null, "weapons", "total", RankingSystem.SQLgetInventoryAndDescriptionsWeapons(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), limit, maxItems, guild_settings.getThemeID()), limit/maxItems+1, itemNumber+1, guild_settings);
						}
					}
					else if(args.length > 0 && args[0].equalsIgnoreCase("skins")) {
						drawTab = "skins_total";
						Hashes.addTempCache("inventory_bot_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(60000, e.getMember().getUser().getId()+"_"+(limit/maxItems+1)+"_"+(itemNumber+1)+"_"+drawTab));
						InventoryBuilder.DrawInventory(e, null, "skins", "total", RankingSystem.SQLgetInventoryAndDescriptionsSkins(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), limit, maxItems, guild_settings.getThemeID()), limit/maxItems+1, itemNumber+1, guild_settings);
					}
					else {
						drawTab = "total_total";
						Hashes.addTempCache("inventory_bot_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(60000, e.getMember().getUser().getId()+"_"+(limit/maxItems+1)+"_"+(itemNumber+1)+"_"+drawTab));
						InventoryBuilder.DrawInventory(e, null, "total", "total", RankingSystem.SQLgetInventoryAndDescriptions(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), limit, maxItems, guild_settings.getThemeID()), limit/maxItems+1, itemNumber+1, guild_settings);
					}
				}
			}
			else{
				e.getChannel().sendMessage(e.getMember().getAsMention()+" I'm not allowed to execute commands in this channel, please write it again in "+STATIC.getChannels(bot_channels)).queue();
			}
		}
		else{
			e.getChannel().sendMessage(e.getMember().getAsMention()+" you can't use any item or skin while the ranking system is disabled!").queue();
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("{} has used Inventory command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}
}
