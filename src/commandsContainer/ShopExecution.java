package commandsContainer;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;

import core.Guilds;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import rankingSystem.Skins;
import rankingSystem.Weapons;
import sql.RankingSystem;
import sql.RankingSystemItems;

public class ShopExecution {
	
	public static void displayPartOfShop(MessageReceivedEvent _e, String _type, String _description){
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(IniFileReader.getShopThumbnail()).setTitle("Welcome to my shop!");
		Guilds guild_settings = RankingSystem.SQLgetGuild(_e.getGuild().getIdLong());
		List<Skins> filteredContent = RankingSystem.SQLgetSkinshopContentAndType(_e.getGuild().getIdLong(), guild_settings.getThemeID()).parallelStream().filter(e -> e.getSkinType().equals(_type)).collect(Collectors.toList());
		StringBuilder builder = new StringBuilder();
		StringBuilder priceBuilder = new StringBuilder();
		
		for(Skins skin_info : filteredContent){
			String price;
			if(skin_info.getShopDescription().equals(_description)){price = "DEFAULT";}
			else{price = skin_info.getPrice()+" "+guild_settings.getCurrency();}
			if(RankingSystem.SQLgetItemID(_e.getMember().getUser().getIdLong(), _e.getGuild().getIdLong(), skin_info.getItemID(), guild_settings.getThemeID()) != 0){price = "PURCHASED";}
			builder.append("*_"+skin_info.getShopDescription()+"_*\n");
			priceBuilder.append("*_"+price+"_*\n");
		}
		message.addField(filteredContent.get(0).getSkinDescription(), "***Here the requested stock!***", false);
		message.addField("Description", builder.toString(), true);
		message.addField("Price", priceBuilder.toString(), true);
		
		_e.getTextChannel().sendMessage(message.build()).queue();
	}
	
	public static void displayPartOfShopWeapons(MessageReceivedEvent _e, String _type) {
		Guilds guild_settings = RankingSystem.SQLgetGuild(_e.getGuild().getIdLong());
		List<Weapons> filteredContent = RankingSystemItems.SQLgetWholeWeaponShop(_e.getGuild().getIdLong(), guild_settings.getThemeID()).parallelStream().filter(f -> f.getCategoryDescription().equalsIgnoreCase(_type) && f.getEnabled()).collect(Collectors.toList());
		if(filteredContent.size() > 0) {
			StringBuilder builder = new StringBuilder();
			StringBuilder priceBuilder = new StringBuilder();
			
			for(Weapons weapon : filteredContent) {
				builder.append("*_"+weapon.getDescription()+" "+weapon.getStatDescription()+"_*\n");
				priceBuilder.append("*_"+weapon.getPrice()+" "+guild_settings.getCurrency()+"_*\n");
			}
			EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(IniFileReader.getShopThumbnail()).setTitle("Welcome to my shop!");
			message.addField("Weapons: "+filteredContent.get(0).getCategoryDescription(), "***Here the requested stock!***", false);
			message.addField("Description", builder.toString(), true);
			message.addField("Price", priceBuilder.toString(), true);
			
			_e.getTextChannel().sendMessage(message.build()).queue();
		}
		else {
			_e.getTextChannel().sendMessage("This weapon shop section doesn't exist or there are no items available. Please run **"+GuildIni.getCommandPrefix(_e.getGuild().getIdLong())+"shop weapons** to view all available sections to purchase weapons").queue();
		}
	}
}
