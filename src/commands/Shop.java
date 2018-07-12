package commands;

import commandsContainer.ShopExecution;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingDB;
import sql.SqlConnect;

public class Shop implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(IniFileReader.getShopCommand().equals("true")){
			String input = e.getMessage().getContentRaw();
			RankingDB.SQLgetGuild(e.getGuild().getIdLong());
			if(RankingDB.getRankingState()){
				SqlConnect.SQLgetChannelID(e.getGuild().getIdLong(), "bot");
				if(SqlConnect.getChannelID() != 0 && e.getTextChannel().getIdLong() != SqlConnect.getChannelID()){
					e.getTextChannel().sendMessage(e.getMember().getAsMention()+" I'm not allowed to execute commands in this channel, please write it again in <#"+SqlConnect.getChannelID()+">").queue();
				}
				else{
					RankingDB.SQLgetDefaultSkins(e.getGuild().getIdLong());
					
					if(input.toUpperCase().equals(IniFileReader.getCommandPrefix().toUpperCase()+"SHOP LEVEL UPS")){
						ShopExecution.displayPartOfShop(e, "lev", RankingDB.getLevelDescription());
					}
					else if(input.toUpperCase().equals(IniFileReader.getCommandPrefix().toUpperCase()+"SHOP RANKS")){
						ShopExecution.displayPartOfShop(e, "ran", RankingDB.getRankDescription());
					}
					else if(input.toUpperCase().equals(IniFileReader.getCommandPrefix().toUpperCase()+"SHOP PROFILES")){
						ShopExecution.displayPartOfShop(e, "pro", RankingDB.getProfileDescription());
					}
					else if(input.toUpperCase().equals(IniFileReader.getCommandPrefix().toUpperCase()+"SHOP ICONS")){
						ShopExecution.displayPartOfShop(e, "ico", RankingDB.getIconDescription());
					}
					else if(input.toUpperCase().equals(IniFileReader.getCommandPrefix().toUpperCase()+"SHOP ITEMS")){
						ShopExecution.displayPartOfShop(e, "ite", RankingDB.getIconDescription());
					}
					else{
						ShopExecution.displayWholeShop(e, RankingDB.getLevelDescription(), RankingDB.getRankDescription(), RankingDB.getProfileDescription(), RankingDB.getIconDescription());
					}
				}
			}
			else{
				e.getTextChannel().sendMessage("Ranking system isn't enabled! Please ask an administrator to enable it before executing!").queue();
			}
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		RankingDB.clearAllVariables();
		RankingDB.clearDescriptionVariables();
		SqlConnect.clearAllVariables();
	}

	@Override
	public String help() {
		return null;
	}
}
