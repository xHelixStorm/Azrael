package commands;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Guilds;
import core.Hashes;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import rankingSystem.Skins;
import sql.RankingSystem;
import sql.Azrael;

public class Purchase implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(IniFileReader.getPurchaseCommand()){
			Logger logger = LoggerFactory.getLogger(Purchase.class);
			logger.info("{} has used Purchase command", e.getMember().getUser().getId());
			
			RankingSystem.SQLgetWholeRankView(e.getMember().getUser().getIdLong());
			Guilds setting = Hashes.getStatus(e.getGuild().getIdLong());
			if(Hashes.getStatus(e.getGuild().getIdLong()).getRankingState()){
				Azrael.SQLgetChannelID(e.getGuild().getIdLong(), "bot");
				if(e.getTextChannel().getIdLong() == Azrael.getChannelID() || Azrael.getChannelID() == 0){
					String input = e.getMessage().getContentRaw();
					if(input.equals(IniFileReader.getCommandPrefix()+"purchase")){
						e.getTextChannel().sendMessage("To purchase an item or skin, use the **"+IniFileReader.getCommandPrefix()+"purchase** command together with the description name of the item you want to purchase. Items to purchase can be found with the **"+IniFileReader.getCommandPrefix()+"shop** command").queue();
					}
					else if(input.contains(IniFileReader.getCommandPrefix()+"purchase ")){
						input = input.substring(IniFileReader.getCommandPrefix().length()+9);
						final String filter = input;
						RankingSystem.SQLgetSkinshopContentAndType();
						Skins skin = Hashes.getShopContent("shop").parallelStream().filter(s -> s.getShopDescription().equalsIgnoreCase(filter)).findAny().orElse(null);
						//RankingDB.SQLgetShopContent(input);
						int item_id = skin.getItemID();
						if(skin.getShopDescription().length() > 0){
							if(!input.equalsIgnoreCase(setting.getLevelDescription()) && !input.equalsIgnoreCase(setting.getRankDescription()) && !input.equalsIgnoreCase(setting.getProfileDescription()) && !input.equalsIgnoreCase(setting.getIconDescription())){
								if(!RankingSystem.SQLgetItemIDAndSkinType(e.getMember().getUser().getIdLong(), input) || skin.getSkinType().equals("ite")){
									rankingSystem.Rank user_details = Hashes.getRanking(e.getMember().getUser().getIdLong());
									if(user_details.getCurrency() >= skin.getPrice()){
										long new_currency = user_details.getCurrency() - skin.getPrice();
										var editedRows = 0;
										Timestamp timestamp = new Timestamp(System.currentTimeMillis());
										if(RankingSystem.SQLgetNumberAndExpirationFromInventory(e.getMember().getUser().getIdLong(), skin.getShopDescription(), "perm"))
											editedRows = RankingSystem.SQLUpdateCurrencyAndInsertInventory(e.getMember().getUser().getIdLong(), new_currency, item_id, timestamp, RankingSystem.getNumber()+1);
										else
											editedRows = RankingSystem.SQLUpdateCurrencyAndInsertInventory(e.getMember().getUser().getIdLong(), new_currency, item_id, timestamp, 1);
										if(editedRows > 0) {
											user_details.setCurrency(new_currency);
											Hashes.addRanking(e.getMember().getUser().getIdLong(), user_details);
											logger.info("{} has purchased {}", e.getMember().getUser().getId(), input.toUpperCase());
											e.getTextChannel().sendMessage("You have successfully purchased **"+input+"**").queue();
										}
										else {
											e.getTextChannel().sendMessage("An internal error occurred and purchase has been interrupted. Please contact an administrator!").queue();
											RankingSystem.SQLInsertActionLog("critical", e.getMember().getUser().getIdLong(), "purchase interrupted", "An error occurred while purchasing "+input);
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
							e.getTextChannel().sendMessage(e.getMember().getAsMention()+" the requested item doesn't exist. Please use "+IniFileReader.getCommandPrefix()+"shop and type the given item description to purchase an item!").queue();
						}
					}
				}
				else{
					e.getTextChannel().sendMessage(e.getMember().getAsMention()+" I'm not allowed to execute commands in this channel, please write it again in <#"+Azrael.getChannelID()+">").queue();
				}
			}
			else{
				e.getTextChannel().sendMessage(e.getMember().getAsMention()+" You can't purchase anything from the shop while the ranking system is disabled!").queue();
			}
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		RankingSystem.clearAllVariables();
		RankingSystem.clearDescriptionVariables();
	}

	@Override
	public String help() { 
		return null;
	}

}
