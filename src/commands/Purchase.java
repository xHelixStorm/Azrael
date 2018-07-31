package commands;

import java.sql.Timestamp;

import fileManagement.IniFileReader;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingDB;
import sql.SqlConnect;

public class Purchase implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(IniFileReader.getPurchaseCommand().equals("true")){
			RankingDB.SQLgetUserUserDetailsRanking(e.getMember().getUser().getIdLong());
			RankingDB.SQLgetGuild(e.getGuild().getIdLong());
			if(RankingDB.getRankingState() == true){
				SqlConnect.SQLgetChannelID(e.getGuild().getIdLong(), "bot");
				if(e.getTextChannel().getIdLong() == SqlConnect.getChannelID()){
					String input = e.getMessage().getContentRaw();
					if(input.equals(IniFileReader.getCommandPrefix()+"purchase")){
						e.getTextChannel().sendMessage("To purchase an item or skin, use the **"+IniFileReader.getCommandPrefix()+"purchase** command together with the description name of the item you want to purchase. Items to purchase can be found with the **"+IniFileReader.getCommandPrefix()+"shop** command").queue();
					}
					else if(input.contains(IniFileReader.getCommandPrefix()+"purchase ")){
						input = input.substring(IniFileReader.getCommandPrefix().length()+9);
						RankingDB.SQLgetShopContent(input);
						int item_id = RankingDB.getItemID();
						if(!RankingDB.getDescription().equals("")){
							if(!input.toUpperCase().equals(RankingDB.getLevelDescription().toUpperCase()) && !input.toUpperCase().equals(RankingDB.getRankDescription().toUpperCase()) && !input.toUpperCase().equals(RankingDB.getProfileDescription().toUpperCase()) && !input.toUpperCase().equals(RankingDB.getIconDescription().toUpperCase())){
								RankingDB.setItemID(0);
								RankingDB.SQLgetItemIDAndSkinType(e.getMember().getUser().getIdLong(), input);
								if(RankingDB.getItemID() == 0 || RankingDB.getSkinType().equals("ite")){
									if(RankingDB.getCurrency() >= RankingDB.getPrice()){
										RankingDB.SQLgetNumberAndExpirationFromInventory(e.getMember().getUser().getIdLong(), RankingDB.getDescription(), "perm");
										Timestamp timestamp = new Timestamp(System.currentTimeMillis());
										RankingDB.SQLUpdateCurrencyAndInsertInventory(e.getMember().getUser().getIdLong(), RankingDB.getCurrency()-RankingDB.getPrice(), item_id, timestamp, RankingDB.getNumber()+1);
										e.getTextChannel().sendMessage("You have successfully purchased **"+input+"**").queue();
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
					e.getTextChannel().sendMessage(e.getMember().getAsMention()+" I'm not allowed to execute commands in this channel, please write it again in <#"+SqlConnect.getChannelID()+">").queue();
				}
			}
			else{
				e.getTextChannel().sendMessage(e.getMember().getAsMention()+" You can't purchase anything from the shop while the ranking system is disabled!").queue();
			}
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		RankingDB.clearAllVariables();
		RankingDB.clearDescriptionVariables();
	}

	@Override
	public String help() { 
		return null;
	}

}
