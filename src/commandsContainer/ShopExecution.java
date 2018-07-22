package commandsContainer;

import java.awt.Color;

import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import rankingSystem.Skins;
import sql.RankingDB;

public class ShopExecution {	
	public static void displayWholeShop(MessageReceivedEvent _e, String _level_description, String _rank_description, String _profile_description, String _icon_description){
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(IniFileReader.getShopThumbnail()).setTitle("Welcome to my shop!");
		RankingDB.SQLgetSkinshopContentAndType("lev");
		StringBuilder builder = new StringBuilder();
		StringBuilder priceBuilder = new StringBuilder();

		message.setDescription("To group the results in the shop, write the header right after the command. We got these items/skins currently in our stock. Have a look around!");
		for(Skins skin_info : RankingDB.getSkins()){
			String price;
			if(skin_info.getShopDescription().equals(_level_description)){price = "DEFAULT";}
			else{price = skin_info.getPrice()+" PEN";}
			RankingDB.SQLgetItemID(_e.getMember().getUser().getIdLong(), skin_info.getItemID());
			if(RankingDB.getItemID() != 0){price = "PURCHASED";}
			builder.append("*_"+skin_info.getShopDescription()+"_*\n");
			priceBuilder.append("*_"+price+"_*\n");
			RankingDB.setItemID(0);
		}
		message.addField(RankingDB.getSkins().get(0).getSkinDescription(), "***Look here for all Level Up skins that appear when you level up!***", false);
		message.addField("Description", builder.toString(), true);
		message.addField("Price", priceBuilder.toString()+"\n", true);
		RankingDB.clearSkinArray();
		builder.setLength(0);
		priceBuilder.setLength(0);
		
		RankingDB.SQLgetSkinshopContentAndType("ran");
		for(Skins skin_info : RankingDB.getSkins()){
			String price;
			if(skin_info.getShopDescription().equals(_rank_description)){price = "DEFAULT";}
			else{price = skin_info.getPrice()+" PEN";}
			RankingDB.SQLgetItemID(_e.getMember().getUser().getIdLong(), skin_info.getItemID());
			if(RankingDB.getItemID() != 0){price = "PURCHASED";}
			builder.append("*_"+skin_info.getShopDescription()+"_*\n");
			priceBuilder.append("*_"+price+"_*\n");
			RankingDB.setItemID(0);
		}
		message.addField(RankingDB.getSkins().get(0).getSkinDescription(), "***Look here for all Rank skins for the command "+IniFileReader.getCommandPrefix()+"rank!***", false);
		message.addField("Description", builder.toString(), true);
		message.addField("Price", priceBuilder.toString(), true);
		RankingDB.clearSkinArray();
		builder.setLength(0);
		priceBuilder.setLength(0);
		
		RankingDB.SQLgetSkinshopContentAndType("pro");
		for(Skins skin_info : RankingDB.getSkins()){
			String price;
			if(skin_info.getShopDescription().equals(_profile_description)){price = "DEFAULT";}
			else{price = skin_info.getPrice()+" PEN";}
			RankingDB.SQLgetItemID(_e.getMember().getUser().getIdLong(), skin_info.getItemID());
			if(RankingDB.getItemID() != 0){price = "PURCHASED";}
			builder.append("*_"+skin_info.getShopDescription()+"_*\n");
			priceBuilder.append("*_"+price+"_*\n");
			RankingDB.setItemID(0);
		}
		message.addField(RankingDB.getSkins().get(0).getSkinDescription(), "***Look here for all Profile skins for the command "+IniFileReader.getCommandPrefix()+"profile!***", false);
		message.addField("Description", builder.toString(), true);
		message.addField("Price", priceBuilder.toString(), true);
		RankingDB.clearSkinArray();
		builder.setLength(0);
		priceBuilder.setLength(0);
		
		RankingDB.SQLgetSkinshopContentAndType("ico");
		for(Skins skin_info : RankingDB.getSkins()){
			String price;
			if(skin_info.getShopDescription().equals(_icon_description)){price = "DEFAULT";}
			else{price = skin_info.getPrice()+" PEN";}
			RankingDB.SQLgetItemID(_e.getMember().getUser().getIdLong(), skin_info.getItemID());
			if(RankingDB.getItemID() != 0){price = "PURCHASED";}
			builder.append("*_"+skin_info.getShopDescription()+"_*\n");
			priceBuilder.append("*_"+price+"_*\n");
			RankingDB.setItemID(0);
		}
		message.addField(RankingDB.getSkins().get(0).getSkinDescription(), "***Look here for all special icons that will be displayed on the rank and profile command!***", false);
		message.addField("Description", builder.toString(), true);
		message.addField("Price", priceBuilder.toString(), true);
		RankingDB.clearSkinArray();
		builder.setLength(0);
		priceBuilder.setLength(0);
		
		RankingDB.SQLgetSkinshopContentAndType("ite");
		for(Skins skin_info : RankingDB.getSkins()){
			String price = skin_info.getPrice()+" PEN";
			RankingDB.SQLgetItemID(_e.getMember().getUser().getIdLong(), skin_info.getItemID());
			builder.append("*_"+skin_info.getShopDescription()+"_*\n");
			priceBuilder.append("*_"+price+"_*\n");
			RankingDB.setItemID(0);
		}
		message.addField(RankingDB.getSkins().get(0).getSkinDescription(), "***Look here to have a look at our available items!***", false);
		message.addField("Description", builder.toString(), true);
		message.addField("Price", priceBuilder.toString(), true);
		RankingDB.clearSkinArray();
		builder.setLength(0);
		priceBuilder.setLength(0);
		
		_e.getTextChannel().sendMessage(message.build()).queue();
		RankingDB.clearAllVariables();
	}
	
	public static void displayPartOfShop(MessageReceivedEvent _e, String _type, String _description){
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(IniFileReader.getShopThumbnail()).setTitle("Welcome to my shop!");
		RankingDB.SQLgetSkinshopContentAndType(_type);
		StringBuilder builder = new StringBuilder();
		StringBuilder priceBuilder = new StringBuilder();
		
		for(Skins skin_info : RankingDB.getSkins()){
			String price;
			if(skin_info.getShopDescription().equals(_description)){price = "DEFAULT";}
			else{price = skin_info.getPrice()+" PEN";}
			RankingDB.SQLgetItemID(_e.getMember().getUser().getIdLong(), skin_info.getItemID());
			if(RankingDB.getItemID() != 0){price = "PURCHASED";}
			builder.append("*_"+skin_info.getShopDescription()+"_*\n");
			priceBuilder.append("*_"+price+"_*\n");
			RankingDB.setItemID(0);
		}
		message.addField(RankingDB.getSkins().get(0).getSkinDescription(), "***Here the requested stock!***", false);
		message.addField("Description", builder.toString(), true);
		message.addField("Price", priceBuilder.toString(), true);
		RankingDB.clearSkinArray();
		
		_e.getTextChannel().sendMessage(message.build()).queue();
		RankingDB.clearAllVariables();
	}
}
