package commands;

import commandsContainer.ShopExecution;
import core.Guilds;
import core.Hashes;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingSystem;
import sql.Azrael;

public class Shop implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(IniFileReader.getShopCommand()){
			String input = e.getMessage().getContentRaw();
			if(Hashes.getStatus(e.getGuild().getIdLong()).getRankingState() == true){
				Azrael.SQLgetChannelID(e.getGuild().getIdLong(), "bot");
				if(Azrael.getChannelID() == 0 || e.getTextChannel().getIdLong() == Azrael.getChannelID()){
					//RankingDB.SQLgetDefaultSkins(e.getGuild().getIdLong());
					Guilds guild_settings = Hashes.getStatus(e.getGuild().getIdLong());
					if(input.toUpperCase().equals(IniFileReader.getCommandPrefix().toUpperCase()+"SHOP LEVEL UPS")){
						ShopExecution.displayPartOfShop(e, "lev", guild_settings.getLevelDescription());
					}
					else if(input.toUpperCase().equals(IniFileReader.getCommandPrefix().toUpperCase()+"SHOP RANKS")){
						ShopExecution.displayPartOfShop(e, "ran", guild_settings.getRankDescription());
					}
					else if(input.toUpperCase().equals(IniFileReader.getCommandPrefix().toUpperCase()+"SHOP PROFILES")){
						ShopExecution.displayPartOfShop(e, "pro", guild_settings.getProfileDescription());
					}
					else if(input.toUpperCase().equals(IniFileReader.getCommandPrefix().toUpperCase()+"SHOP ICONS")){
						ShopExecution.displayPartOfShop(e, "ico", guild_settings.getIconDescription());
					}
					else if(input.toUpperCase().equals(IniFileReader.getCommandPrefix().toUpperCase()+"SHOP ITEMS")){
						ShopExecution.displayPartOfShop(e, "ite", "");
					}
					else{
						ShopExecution.displayWholeShop(e, guild_settings.getLevelDescription(), guild_settings.getRankDescription(), guild_settings.getProfileDescription(), guild_settings.getIconDescription());
					}
				}
				else{
					e.getTextChannel().sendMessage(e.getMember().getAsMention()+" I'm not allowed to execute commands in this channel, please write it again in <#"+Azrael.getChannelID()+">").queue();
				}
			}
			else{
				e.getTextChannel().sendMessage("Ranking system isn't enabled! Please ask an administrator to enable it before executing!").queue();
			}
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		RankingSystem.clearAllVariables();
		RankingSystem.clearDescriptionVariables();
		Azrael.clearAllVariables();
	}

	@Override
	public String help() {
		return null;
	}
}
