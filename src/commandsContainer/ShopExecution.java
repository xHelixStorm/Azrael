package commandsContainer;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;

import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import rankingSystem.Skins;
import sql.RankingSystem;

public class ShopExecution {	
	public static void displayWholeShop(MessageReceivedEvent _e, String _level_description, String _rank_description, String _profile_description, String _icon_description){
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(IniFileReader.getShopThumbnail()).setTitle("Welcome to my shop!");
		List<Skins> shopContent = RankingSystem.SQLgetSkinshopContentAndType(_e.getGuild().getIdLong());
		List<Skins> levelContent = shopContent.parallelStream().filter(e -> e.getSkinType().equals("lev")).collect(Collectors.toList());
		List<Skins> rankContent = shopContent.parallelStream().filter(e -> e.getSkinType().equals("ran")).collect(Collectors.toList());
		List<Skins> profileContent = shopContent.parallelStream().filter(e -> e.getSkinType().equals("pro")).collect(Collectors.toList());
		List<Skins> iconsContent = shopContent.parallelStream().filter(e -> e.getSkinType().equals("ico")).collect(Collectors.toList());
		List<Skins> itemContent = shopContent.parallelStream().filter(e -> e.getSkinType().equals("ite")).collect(Collectors.toList());
		StringBuilder builder = new StringBuilder();
		StringBuilder priceBuilder = new StringBuilder();

		message.setDescription("To group the results in the shop, write the header right after the command. We got these items/skins currently in our stock. Have a look around!");
		for(Skins skin_info : levelContent){
			String price;
			if(skin_info.getShopDescription().equals(_level_description)){price = "DEFAULT";}
			else{price = skin_info.getPrice()+" PEN";}
			if(RankingSystem.SQLgetItemID(_e.getMember().getUser().getIdLong(), _e.getGuild().getIdLong(), skin_info.getItemID()) != 0){price = "PURCHASED";}
			builder.append("*_"+skin_info.getShopDescription()+"_*\n");
			priceBuilder.append("*_"+price+"_*\n");
		}
		message.addField(levelContent.get(0).getSkinDescription(), "***Look here for all Level Up skins that appear when you level up!***", false);
		message.addField("Description", builder.toString(), true);
		message.addField("Price", priceBuilder.toString()+"\n", true);
		builder.setLength(0);
		priceBuilder.setLength(0);
		
		for(Skins skin_info : rankContent){
			String price;
			if(skin_info.getShopDescription().equals(_rank_description)){price = "DEFAULT";}
			else{price = skin_info.getPrice()+" PEN";}
			if(RankingSystem.SQLgetItemID(_e.getMember().getUser().getIdLong(), _e.getGuild().getIdLong(), skin_info.getItemID()) != 0){price = "PURCHASED";}
			builder.append("*_"+skin_info.getShopDescription()+"_*\n");
			priceBuilder.append("*_"+price+"_*\n");
		}
		message.addField(rankContent.get(0).getSkinDescription(), "***Look here for all Rank skins for the command "+IniFileReader.getCommandPrefix()+"rank!***", false);
		message.addField("Description", builder.toString(), true);
		message.addField("Price", priceBuilder.toString(), true);
		builder.setLength(0);
		priceBuilder.setLength(0);
		
		for(Skins skin_info : profileContent){
			String price;
			if(skin_info.getShopDescription().equals(_profile_description)){price = "DEFAULT";}
			else{price = skin_info.getPrice()+" PEN";}
			if(RankingSystem.SQLgetItemID(_e.getMember().getUser().getIdLong(), _e.getGuild().getIdLong(), skin_info.getItemID()) != 0){price = "PURCHASED";}
			builder.append("*_"+skin_info.getShopDescription()+"_*\n");
			priceBuilder.append("*_"+price+"_*\n");
		}
		message.addField(profileContent.get(0).getSkinDescription(), "***Look here for all Profile skins for the command "+IniFileReader.getCommandPrefix()+"profile!***", false);
		message.addField("Description", builder.toString(), true);
		message.addField("Price", priceBuilder.toString(), true);
		builder.setLength(0);
		priceBuilder.setLength(0);
		
		for(Skins skin_info : iconsContent){
			String price;
			if(skin_info.getShopDescription().equals(_icon_description)){price = "DEFAULT";}
			else{price = skin_info.getPrice()+" PEN";}
			if(RankingSystem.SQLgetItemID(_e.getMember().getUser().getIdLong(), _e.getGuild().getIdLong(), skin_info.getItemID()) != 0){price = "PURCHASED";}
			builder.append("*_"+skin_info.getShopDescription()+"_*\n");
			priceBuilder.append("*_"+price+"_*\n");
		}
		message.addField(iconsContent.get(0).getSkinDescription(), "***Look here for all special icons that will be displayed on the rank and profile command!***", false);
		message.addField("Description", builder.toString(), true);
		message.addField("Price", priceBuilder.toString(), true);
		builder.setLength(0);
		priceBuilder.setLength(0);
		
		for(Skins skin_info : itemContent){
			String price = skin_info.getPrice()+" PEN";
			builder.append("*_"+skin_info.getShopDescription()+"_*\n");
			priceBuilder.append("*_"+price+"_*\n");
		}
		message.addField(itemContent.get(0).getSkinDescription(), "***Look here to have a look at our available items!***", false);
		message.addField("Description", builder.toString(), true);
		message.addField("Price", priceBuilder.toString(), true);
		builder.setLength(0);
		priceBuilder.setLength(0);
		
		_e.getTextChannel().sendMessage(message.build()).queue();
	}
	
	public static void displayPartOfShop(MessageReceivedEvent _e, String _type, String _description){
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(IniFileReader.getShopThumbnail()).setTitle("Welcome to my shop!");
		List<Skins> shopContent = RankingSystem.SQLgetSkinshopContentAndType(_e.getGuild().getIdLong());
		List<Skins> filteredContent = shopContent.parallelStream().filter(e -> e.getSkinType().equals(_type)).collect(Collectors.toList());
		StringBuilder builder = new StringBuilder();
		StringBuilder priceBuilder = new StringBuilder();
		
		for(Skins skin_info : filteredContent){
			String price;
			if(skin_info.getShopDescription().equals(_description)){price = "DEFAULT";}
			else{price = skin_info.getPrice()+" PEN";}
			if(RankingSystem.SQLgetItemID(_e.getMember().getUser().getIdLong(), _e.getGuild().getIdLong(), skin_info.getItemID()) != 0){price = "PURCHASED";}
			builder.append("*_"+skin_info.getShopDescription()+"_*\n");
			priceBuilder.append("*_"+price+"_*\n");
		}
		message.addField(filteredContent.get(0).getSkinDescription(), "***Here the requested stock!***", false);
		message.addField("Description", builder.toString(), true);
		message.addField("Price", priceBuilder.toString(), true);
		
		_e.getTextChannel().sendMessage(message.build()).queue();
	}
}
