package commands;

import java.awt.Color;
import java.sql.Timestamp;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Guilds;
import core.Hashes;
import core.UserPrivs;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import inventory.InventoryContent;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import rankingSystem.Skins;
import rankingSystem.Weapons;
import sql.RankingSystem;
import sql.RankingSystemItems;
import util.STATIC;
import sql.Azrael;

public class Purchase implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(GuildIni.getPurchaseCommand(e.getGuild().getIdLong())){
			Logger logger = LoggerFactory.getLogger(Purchase.class);
			logger.debug("{} has used Purchase command", e.getMember().getUser().getId());
			if(UserPrivs.comparePrivilege(e.getMember(), GuildIni.getPurchaseLevel(e.getGuild().getIdLong())) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong()) {
				Guilds setting = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
				if(setting.getRankingState()){
					var bot_channels = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type().equals("bot")).collect(Collectors.toList());
					if(bot_channels.size() == 0 || bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getTextChannel().getIdLong()).findAny().orElse(null) != null){
						String input = e.getMessage().getContentRaw();
						final String prefix = GuildIni.getCommandPrefix(e.getGuild().getIdLong());
						if(args.length == 0){
							e.getTextChannel().sendMessage("To purchase an item or skin, use the **"+prefix+"purchase** command together with the description name of the item you want to purchase. Items to purchase can be found with the **"+prefix+"shop** command").queue();
						}
						else if(args.length > 0){
							input = input.substring(prefix.length()+9);
							final String filter = input;
							Skins skin = RankingSystem.SQLgetSkinshopContentAndType(e.getGuild().getIdLong(), setting.getThemeID()).parallelStream().filter(s -> s.getShopDescription().equalsIgnoreCase(filter)).findAny().orElse(null);
							Weapons weapon = RankingSystemItems.SQLgetWholeWeaponShop(e.getGuild().getIdLong(), setting.getThemeID()).parallelStream().filter(w -> filter.equalsIgnoreCase(w.getDescription()+" "+w.getStatDescription())).findAny().orElse(null);
							if(skin != null || weapon != null) {
								if(skin != null) {
									int item_id = skin.getItemID();
									if(skin.getShopDescription().length() > 0){
										if(!input.equalsIgnoreCase(setting.getLevelDescription()) && !input.equalsIgnoreCase(setting.getRankDescription()) && !input.equalsIgnoreCase(setting.getProfileDescription()) && !input.equalsIgnoreCase(setting.getIconDescription())){
											if(RankingSystem.SQLgetItemIDAndSkinType(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), input, setting.getThemeID()) == null || skin.getSkinType().equals("ite")){
												rankingSystem.Rank user_details = RankingSystem.SQLgetWholeRankView(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), setting.getThemeID());
												if(user_details.getCurrency() >= skin.getPrice()){
													long new_currency = user_details.getCurrency() - skin.getPrice();
													var editedRows = 0;
													Timestamp timestamp = new Timestamp(System.currentTimeMillis());
													InventoryContent inventory = RankingSystem.SQLgetNumberAndExpirationFromInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), skin.getShopDescription(), "perm", setting.getThemeID());
													if(inventory != null)
														editedRows = RankingSystem.SQLUpdateCurrencyAndInsertInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), new_currency, item_id, timestamp, inventory.getNumber()+1, setting.getThemeID());
													else
														editedRows = RankingSystem.SQLUpdateCurrencyAndInsertInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), new_currency, item_id, timestamp, 1, setting.getThemeID());
													if(editedRows > 0) {
														user_details.setCurrency(new_currency);
														Hashes.addRanking(e.getGuild().getId()+"_"+e.getMember().getUser().getIdLong(), user_details);
														logger.debug("{} has purchased {}", e.getMember().getUser().getId(), input.toUpperCase());
														e.getTextChannel().sendMessage("You have successfully purchased **"+input+"**").queue();
													}
													else {
														e.getTextChannel().sendMessage("An internal error occurred and purchase has been interrupted. Please contact an administrator!").queue();
														RankingSystem.SQLInsertActionLog("critical", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "purchase interrupted", "An error occurred while purchasing "+input);
													}
												}
												else{
													e.getTextChannel().sendMessage(e.getMember().getAsMention()+" you don't have enough money to purchase this item/skin!").queue();
												}
											}
											else{
												e.getTextChannel().sendMessage(e.getMember().getAsMention()+" you have already purchased this item! Please retry with another!").queue();
											}
										}
										else{
											e.getTextChannel().sendMessage(e.getMember().getAsMention()+" you tried to purchase a skin that is being used as the default server skin. Please choose another skin to purchase!").queue();
										}
									}
									else{
										e.getTextChannel().sendMessage(e.getMember().getAsMention()+" the requested item doesn't exist. Please use "+prefix+"shop and type the given item description to purchase an item!").queue();
									}
								}
								else if(weapon != null) {
									if(weapon.getDescription().length() > 0) {
										rankingSystem.Rank user_details = RankingSystem.SQLgetWholeRankView(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), setting.getThemeID());
										if(user_details.getCurrency() >= weapon.getPrice()) {
											long new_currency = user_details.getCurrency() - weapon.getPrice();
											var editedRows = 0;
											long timestamp = System.currentTimeMillis();
											InventoryContent inventory = RankingSystemItems.SQLgetNumberAndExpirationFromInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), weapon.getWeaponID(), "limit", setting.getThemeID());
											if(inventory != null)
												editedRows = RankingSystemItems.SQLUpdateCurrencyAndInsertTimedInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), new_currency, weapon.getWeaponID(), timestamp, inventory.getExpiration().getTime(), 1, setting.getThemeID());
											else
												editedRows = RankingSystemItems.SQLUpdateCurrencyAndInsertTimedInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), new_currency, weapon.getWeaponID(), timestamp, timestamp, 1, setting.getThemeID());
											if(editedRows > 0) {
												user_details.setCurrency(new_currency);
												Hashes.addRanking(e.getGuild().getId()+"_"+e.getMember().getUser().getIdLong(), user_details);
												logger.debug("{} has purchased {}", e.getMember().getUser().getId(), input.toUpperCase());
												e.getTextChannel().sendMessage("You have successfully purchased **"+input+"**").queue();
											}
											else {
												e.getTextChannel().sendMessage("An internal error occurred and purchase has been interrupted. Please contact an administrator!").queue();
												RankingSystem.SQLInsertActionLog("critical", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "purchase interrupted", "An error occurred while purchasing "+input);
											}
										}
										else {
											e.getTextChannel().sendMessage(e.getMember().getAsMention()+" you don't have enough money to purchase this weapon/skill!").queue();
										}
									}
									else {
										e.getTextChannel().sendMessage(e.getMember().getAsMention()+" the requested item doesn't exist. Please use "+prefix+"shop and type the given item description to purchase an item!").queue();
									}
								}
							}
							else {
								e.getTextChannel().sendMessage("The item/skin/weapon you tried to purchase doesn't exist or has been written wrong. Please check the shop for the descriptions").queue();
							}
						}
					}
					else{
						e.getTextChannel().sendMessage(e.getMember().getAsMention()+" I'm not allowed to execute commands in this channel, please write it again in "+STATIC.getChannels(bot_channels)).queue();
						logger.warn("Purchase command used in a not bot channel");
					}
				}
				else{
					e.getTextChannel().sendMessage(e.getMember().getAsMention()+" You can't purchase anything from the shop while the ranking system is disabled!").queue();
				}
			}
			else {
				EmbedBuilder message = new EmbedBuilder();
				e.getTextChannel().sendMessage(message.setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:").build()).queue();
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
