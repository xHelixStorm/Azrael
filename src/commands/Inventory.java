package commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Guilds;
import fileManagement.FileSetting;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import inventory.InventoryBuilder;
import inventory.InventoryContent;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingSystem;
import sql.RankingSystemItems;
import sql.Azrael;

public class Inventory implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(GuildIni.getHelpCommand(e.getGuild().getIdLong())){
			Logger logger = LoggerFactory.getLogger(Inventory.class);
			logger.debug("{} has used Inventory command", e.getMember().getUser().getId());
			
			Guilds guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
			if(guild_settings.getRankingState()){
				var bot_channel = Azrael.SQLgetChannelID(e.getGuild().getIdLong(), "bot");
				if(bot_channel == e.getTextChannel().getIdLong() || bot_channel == 0){
					final String prefix = GuildIni.getCommandPrefix(e.getGuild().getIdLong());
					if(e.getMessage().getContentRaw().equals(prefix+"inventory -help")) {
						EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
						e.getTextChannel().sendMessage(message.setDescription("- Type **-list** after the command to display the whole inventory as a list\n"
								+ "- Type **-page** and then the page together with the command to directly select the page you wish to view\n"
								+ "- Type the tab name to filter your inventory item by type. Available types are **items** and **weapons**\n"
								+ "- Type the sub tab after the tab name together with the command to further filter your inventory selection").build()).queue();
					}
					else if(e.getMessage().getContentRaw().equals(prefix+"inventory -list")){
						String out = "";
						for(InventoryContent inventory : RankingSystem.SQLgetInventoryAndDescriptionWithoutLimit(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), guild_settings.getThemeID())){
							out+= (inventory.getDescription() != null ? inventory.getDescription() : inventory.getWeaponDescription()+" "+inventory.getStat())+"\n";
						}
						e.getTextChannel().sendMessage("```"+out+"```").queue();
					}
					else{
						int limit = 0;
						int itemNumber;
						String lastWord = e.getMessage().getContentRaw().substring(e.getMessage().getContentRaw().lastIndexOf(" ")+1).toLowerCase();
						String sub_cat = RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong(), guild_settings.getThemeID()).parallelStream().filter(c -> c.equalsIgnoreCase(lastWord)).findAny().orElse(null);
						final var maxItems = GuildIni.getInventoryMaxItems(e.getGuild().getIdLong());
						if(e.getMessage().getContentRaw().toLowerCase().contains("items"))
							itemNumber = RankingSystem.SQLgetTotalItemNumber(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "ite", maxItems, guild_settings.getThemeID());
						else if(e.getMessage().getContentRaw().toLowerCase().contains("weapons")) {
							if(!lastWord.equals("weapons") && sub_cat != null)
								itemNumber = RankingSystem.SQLgetTotalItemNumber(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), maxItems, true, lastWord, guild_settings.getThemeID());
							else
								itemNumber = RankingSystem.SQLgetTotalItemNumber(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), maxItems, false, guild_settings.getThemeID());
						}
						else if(e.getMessage().getContentRaw().toLowerCase().contains("skins")) {
							itemNumber = RankingSystem.SQLgetTotalItemNumber(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), maxItems, "ite", true, guild_settings.getThemeID());
						}
						else
							itemNumber = RankingSystem.SQLgetTotalItemNumber(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), maxItems, guild_settings.getThemeID());
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
						
						String drawTab = "";
						if(e.getMessage().getContentRaw().toLowerCase().contains("items"))
							InventoryBuilder.DrawInventory(e, null, "items", "total", RankingSystem.SQLgetInventoryAndDescriptionsItems(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), limit, maxItems, guild_settings.getThemeID()), limit/maxItems+1, itemNumber+1);
						else if(e.getMessage().getContentRaw().toLowerCase().contains("weapons")) {
							if(!lastWord.equals("weapons") && sub_cat != null) {
								drawTab = "weapons_"+lastWord;
								InventoryBuilder.DrawInventory(e, null, "weapons", lastWord, RankingSystem.SQLgetInventoryAndDescriptionsWeapons(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), limit, maxItems, lastWord, guild_settings.getThemeID()), limit/maxItems+1, itemNumber+1);
							}
							else {
								drawTab = "weapons_total";
								InventoryBuilder.DrawInventory(e, null, "weapons", "total", RankingSystem.SQLgetInventoryAndDescriptionsWeapons(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), limit, maxItems, guild_settings.getThemeID()), limit/maxItems+1, itemNumber+1);
							}
						}
						else if(e.getMessage().getContentRaw().toLowerCase().contains("skins")) {
							drawTab = "skins_total";
							InventoryBuilder.DrawInventory(e, null, "skins", "total", RankingSystem.SQLgetInventoryAndDescriptionsSkins(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), limit, maxItems, guild_settings.getThemeID()), limit/maxItems+1, itemNumber+1);
						}
						else {
							drawTab = "total_total";
							InventoryBuilder.DrawInventory(e, null, "total", "total", RankingSystem.SQLgetInventoryAndDescriptions(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), limit, maxItems, guild_settings.getThemeID()), limit/maxItems+1, itemNumber+1);
						}
						
						FileSetting.createFile(IniFileReader.getTempDirectory()+"AutoDelFiles/inventory_bot_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+".azr", e.getMember().getUser().getId()+"_"+(limit/maxItems+1)+"_"+(itemNumber+1)+"_"+drawTab);
					}
				}
				else{
					e.getTextChannel().sendMessage(e.getMember().getAsMention()+" I'm not allowed to execute commands in this channel, please write it again in <#"+bot_channel+">").queue();
					logger.warn("Inventory command used in a not bot channel");
				}
			}
			else{
				e.getTextChannel().sendMessage(e.getMember().getAsMention()+" you can't use any item or skin while the ranking system is disabled!").queue();
			}
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		
	}

	@Override
	public String help() {
		return null;
	}

}
