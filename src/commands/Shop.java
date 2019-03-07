package commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commandsContainer.ShopExecution;
import core.Guilds;
import core.Hashes;
import fileManagement.GuildIni;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.Azrael;
import sql.RankingSystemItems;

public class Shop implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(GuildIni.getShopCommand(e.getGuild().getIdLong())){
			Logger logger = LoggerFactory.getLogger(Shop.class);
			logger.debug("{} has used Shop command", e.getMember().getUser().getId());
			
			String input = e.getMessage().getContentRaw();
			if(Hashes.getStatus(e.getGuild().getIdLong()).getRankingState() == true){
				var bot_channel = Azrael.SQLgetChannelID(e.getGuild().getIdLong(), "bot");
				if(bot_channel == 0 || e.getTextChannel().getIdLong() == bot_channel){
					Guilds guild_settings = Hashes.getStatus(e.getGuild().getIdLong());
					final String prefix = GuildIni.getCommandPrefix(e.getGuild().getIdLong());
					if(input.toUpperCase().equals(prefix.toUpperCase()+"SHOP LEVEL UPS")) {
						ShopExecution.displayPartOfShop(e, "lev", guild_settings.getLevelDescription());
					}
					else if(input.toUpperCase().equals(prefix.toUpperCase()+"SHOP RANKS")) {
						ShopExecution.displayPartOfShop(e, "ran", guild_settings.getRankDescription());
					}
					else if(input.toUpperCase().equals(prefix.toUpperCase()+"SHOP PROFILES")) {
						ShopExecution.displayPartOfShop(e, "pro", guild_settings.getProfileDescription());
					}
					else if(input.toUpperCase().equals(prefix.toUpperCase()+"SHOP ICONS")) {
						ShopExecution.displayPartOfShop(e, "ico", guild_settings.getIconDescription());
					}
					else if(input.toUpperCase().equals(prefix.toUpperCase()+"SHOP ITEMS")) {
						ShopExecution.displayPartOfShop(e, "ite", "");
					}
					else if(input.toUpperCase().equals(prefix.toUpperCase()+"SHOP WEAPONS")) {
						StringBuilder builder = new StringBuilder();
						for(String category : RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong())) {
							builder.append(category+", ");
						}
						e.getTextChannel().sendMessage("Use these weapon sections to filter the weapons you wish to purchase together with the command:\n**"+builder.toString()+"**").queue();
					}
					else if(input.toUpperCase().contains(prefix.toUpperCase()+"SHOP WEAPONS ")) {
						String type = input.substring(prefix.length()+13);
						ShopExecution.displayPartOfShopWeapons(e, type);
					}
					else{
						e.getTextChannel().sendMessage("Write the shop command together with the category of the shop you want to visit. For example "+prefix+"shop **level ups** / **ranks** / **profiles** / **icons** / **items** / **weapons**").queue();
					}
				}
				else{
					e.getTextChannel().sendMessage(e.getMember().getAsMention()+" I'm not allowed to execute commands in this channel, please write it again in <#"+bot_channel+">").queue();
					logger.warn("Shop command used in a not bot channel");
					
				}
			}
			else{
				e.getTextChannel().sendMessage("Ranking system isn't enabled! Please ask an administrator to enable it before executing!").queue();
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
