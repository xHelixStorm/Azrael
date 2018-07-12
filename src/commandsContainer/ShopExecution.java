package commandsContainer;

import java.awt.Color;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import rankingSystem.Skins;
import sql.RankingDB;

public class ShopExecution {
	private static EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
	
	public static void displayWholeShop(MessageReceivedEvent _e, String _level_description, String _rank_description, String _profile_description, String _icon_description){
		int i = 1;
		RankingDB.SQLgetSkinshopContentAndType("lev");
		StringBuilder builder = new StringBuilder();
		builder.append("To group the results in the shop, write the header right after the command.\nThese items/skins are available to purchase:\n\n**"+RankingDB.getSkins().get(0).getSkinDescription()+"**\n");
		for(Skins skin_info : RankingDB.getSkins()){
			String price;
			if(skin_info.getShopDescription().equals(_level_description)){price = "DEFAULT";}
			else{price = skin_info.getPrice()+" PEN";}
			RankingDB.SQLgetItemID(_e.getMember().getUser().getIdLong(), skin_info.getItemID());
			if(RankingDB.getItemID() != 0){price = "PURCHASED";}
			builder.append(i+" : *_"+skin_info.getShopDescription()+"_*\nPrice: *_"+price+"_*\n");
			RankingDB.setItemID(0);
			i++;
		}
		RankingDB.clearSkinArray();
		
		RankingDB.SQLgetSkinshopContentAndType("ran");
		builder.append("\n**"+RankingDB.getSkins().get(0).getSkinDescription()+"**\n");
		for(Skins skin_info : RankingDB.getSkins()){
			String price;
			if(skin_info.getShopDescription().equals(_rank_description)){price = "DEFAULT";}
			else{price = skin_info.getPrice()+" PEN";}
			RankingDB.SQLgetItemID(_e.getMember().getUser().getIdLong(), skin_info.getItemID());
			if(RankingDB.getItemID() != 0){price = "PURCHASED";}
			builder.append(i+" : *_"+skin_info.getShopDescription()+"_*\nPrice: *_"+price+"_*\n");
			RankingDB.setItemID(0);
			i++;
		}
		RankingDB.clearSkinArray();
		
		RankingDB.SQLgetSkinshopContentAndType("pro");
		builder.append("\n**"+RankingDB.getSkins().get(0).getSkinDescription()+"**\n");
		for(Skins skin_info : RankingDB.getSkins()){
			String price;
			if(skin_info.getShopDescription().equals(_profile_description)){price = "DEFAULT";}
			else{price = skin_info.getPrice()+" PEN";}
			RankingDB.SQLgetItemID(_e.getMember().getUser().getIdLong(), skin_info.getItemID());
			if(RankingDB.getItemID() != 0){price = "PURCHASED";}
			builder.append(i+" : *_"+skin_info.getShopDescription()+"_*\nPrice: *_"+price+"_*\n");
			RankingDB.setItemID(0);
			i++;
		}
		RankingDB.clearSkinArray();
		
		RankingDB.SQLgetSkinshopContentAndType("ico");
		builder.append("\n**"+RankingDB.getSkins().get(0).getSkinDescription()+"**\n");
		for(Skins skin_info : RankingDB.getSkins()){
			String price;
			if(skin_info.getShopDescription().equals(_icon_description)){price = "DEFAULT";}
			else{price = skin_info.getPrice()+" PEN";}
			RankingDB.SQLgetItemID(_e.getMember().getUser().getIdLong(), skin_info.getItemID());
			if(RankingDB.getItemID() != 0){price = "PURCHASED";}
			builder.append(i+" : *_"+skin_info.getShopDescription()+"_*\nPrice: *_"+price+"_*\n");
			RankingDB.setItemID(0);
			i++;
		}
		RankingDB.clearSkinArray();
		
		RankingDB.SQLgetSkinshopContentAndType("ite");
		builder.append("\n**"+RankingDB.getSkins().get(0).getSkinDescription()+"**\n");
		for(Skins skin_info : RankingDB.getSkins()){
			String price = skin_info.getPrice()+" PEN";
			RankingDB.SQLgetItemID(_e.getMember().getUser().getIdLong(), skin_info.getItemID());
			builder.append(i+" : *_"+skin_info.getShopDescription()+"_*\nPrice: *_"+price+"_*\n");
			RankingDB.setItemID(0);
			i++;
		}
		RankingDB.clearSkinArray();
		
		_e.getTextChannel().sendMessage(message.setDescription(builder).build()).queue();
		RankingDB.clearAllVariables();
	}
	
	public static void displayPartOfShop(MessageReceivedEvent _e, String _type, String _description){
		int i = 1;
		RankingDB.SQLgetSkinshopContentAndType(_type);
		StringBuilder builder = new StringBuilder();
		builder.append("**"+RankingDB.getSkins().get(0).getSkinDescription()+"**\n");
		for(Skins skin_info : RankingDB.getSkins()){
			String price;
			if(skin_info.getShopDescription().equals(_description)){price = "DEFAULT";}
			else{price = skin_info.getPrice()+" PEN";}
			RankingDB.SQLgetItemID(_e.getMember().getUser().getIdLong(), skin_info.getItemID());
			if(RankingDB.getItemID() != 0){price = "PURCHASED";}
			builder.append(i+" : *_"+skin_info.getShopDescription()+"_*\nPrice: *_"+price+"_*\n");
			RankingDB.setItemID(0);
			i++;
		}
		RankingDB.clearSkinArray();
		
		_e.getTextChannel().sendMessage(message.setDescription(builder).build()).queue();
		RankingDB.clearAllVariables();
	}
}
