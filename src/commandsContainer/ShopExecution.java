package commandsContainer;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;

import constructors.Cache;
import constructors.Guilds;
import constructors.Skins;
import constructors.Weapons;
import core.Hashes;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.RankingSystem;
import sql.RankingSystemItems;

public class ShopExecution {
	
	public static void displayShop(GuildMessageReceivedEvent _e, String _type, String _description){
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(IniFileReader.getShopThumbnail());
		Guilds guild_settings = RankingSystem.SQLgetGuild(_e.getGuild().getIdLong());
		List<Skins> filteredContent = RankingSystem.SQLgetSkinshopContentAndType(_e.getGuild().getIdLong(), guild_settings.getThemeID()).parallelStream().filter(e -> e.getSkinType().equals(_type)).collect(Collectors.toList());
		StringBuilder builder = new StringBuilder();
		StringBuilder priceBuilder = new StringBuilder();
		var items = "";
		var index = 1;
		
		for(Skins skin_info : filteredContent) {
			items += (items.length() == 0 ? "" : "-")+skin_info.getItemID();
			String price;
			if(skin_info.getShopDescription().equals(_description)){price = "DEFAULT";}
			else{price = skin_info.getPrice()+" "+guild_settings.getCurrency();}
			if(RankingSystem.SQLgetItemID(_e.getMember().getUser().getIdLong(), _e.getGuild().getIdLong(), skin_info.getItemID(), guild_settings.getThemeID()) != 0 && !skin_info.getSkinType().equals("ite")){price = "PURCHASED";}
			builder.append(index+": *_"+skin_info.getShopDescription()+"_*\n");
			priceBuilder.append("*_"+price+"_*\n");
			index++;
		}
		message.addField(filteredContent.get(0).getSkinDescription(), "***Here the requested stock! Select an item with a digit to inspect!***", false);
		message.addField("Description", builder.toString(), true);
		message.addField("Price", priceBuilder.toString(), true);
		
		_e.getChannel().sendMessage(message.build()).queue();
		Hashes.addTempCache("shop_gu"+_e.getGuild().getId()+"ch"+_e.getChannel().getId()+"us"+_e.getMember().getUser().getId(), new Cache(180000, _type, items));
	}
	
	public static void displaySingleItem(GuildMessageReceivedEvent _e, String _type, String [] _items, Guilds guild_settings, final int selection) {
		if(selection >= 0 && selection < _items.length) {
			final var item = Integer.parseInt(_items[selection]);
			var shopItem = RankingSystem.SQLgetSkinshopContentAndType(_e.getGuild().getIdLong(), guild_settings.getThemeID()).parallelStream().filter(f -> f.getItemID() == item).findAny().orElse(null);
			var defaultSkin = false;
			var alreadyPurchased = false;
			var terminator = "%";
			if(guild_settings.getLevelDescription().equals(shopItem.getShopDescription()) || guild_settings.getRankDescription().equals(shopItem.getShopDescription()) || guild_settings.getProfileDescription().equals(shopItem.getShopDescription()) || guild_settings.getIconDescription().equals(shopItem.getShopDescription())) {
				defaultSkin = true;
			}
			if(RankingSystem.SQLgetItemIDAndSkinType(_e.getMember().getUser().getIdLong(), _e.getGuild().getIdLong(), shopItem.getShopDescription(), guild_settings.getThemeID()) != null && !shopItem.getSkinType().equals("ite")) {
				alreadyPurchased = true;
			}
			EmbedBuilder embed = new EmbedBuilder();
			if(shopItem.getThumbnail() != null && shopItem.getThumbnail().contains("http"))
				embed.setThumbnail(shopItem.getThumbnail());
			embed.setDescription("**"+shopItem.getShopDescription()+"**\n"+(shopItem.getSkinFullDescription() != null && shopItem.getSkinFullDescription().length() > 0 ? shopItem.getSkinFullDescription() : ""));
			if(defaultSkin) {
				terminator = "$";
				embed.setColor(Color.YELLOW);
				embed.addField("Return", "Skin selection page", false);
			}
			else if(alreadyPurchased) {
				terminator = "#";
				embed.setColor(Color.YELLOW);
				embed.addField("Sell", (shopItem.getPrice()/10)+" "+guild_settings.getCurrency(), true);
				embed.addField("Return", "Skin selection page", true);
			}
			else {
				embed.setColor(Color.BLUE);
				embed.addField("Purchase", shopItem.getPrice()+" "+guild_settings.getCurrency(), true);
				embed.addField("Return", "Skin selection page", true);
			}
			_e.getChannel().sendMessage(embed.build()).queue();
			Hashes.addTempCache("shop_gu"+_e.getGuild().getId()+"ch"+_e.getChannel().getId()+"us"+_e.getMember().getUser().getId(), new Cache(180000, _type, shopItem.getItemID()+terminator));
		}
		else {
			_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Please select a digit between 1 and "+_items.length+" to closely inspect an item and then purchase!").build()).queue();
			var cache = Hashes.getTempCache("shop_gu"+_e.getGuild().getId()+"ch"+_e.getChannel().getId()+"us"+_e.getMember().getUser().getId()).setExpiration(180000);
			Hashes.addTempCache("shop_gu"+_e.getGuild().getId()+"ch"+_e.getChannel().getId()+"us"+_e.getMember().getUser().getId(), cache);
		}
	}
	
	public static void displayWeaponCategories(GuildMessageReceivedEvent _e, final int theme_id) {
		StringBuilder builder = new StringBuilder();
		var categories = "";
		for(String category : RankingSystemItems.SQLgetWeaponCategories(_e.getGuild().getIdLong(), theme_id, false)) {
			categories += (categories.length() == 0 ? "" : "-")+category;
			builder.append(category+", ");
		}
		_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setThumbnail(IniFileReader.getShopThumbnail())
				.setDescription("My weapons interest you? Alright! Which category do you wish to take a look into?\n\n**"+builder.toString()+"**").build()).queue();
		Hashes.addTempCache("shop_gu"+_e.getGuild().getId()+"ch"+_e.getChannel().getId()+"us"+_e.getMember().getUser().getId(), new Cache(180000, "wea", categories));
	}
	
	public static void displayShopWeapons(GuildMessageReceivedEvent _e, String _type) {
		Guilds guild_settings = RankingSystem.SQLgetGuild(_e.getGuild().getIdLong());
		List<Weapons> filteredContent = RankingSystemItems.SQLgetWholeWeaponShop(_e.getGuild().getIdLong(), guild_settings.getThemeID()).parallelStream().filter(f -> f.getCategoryDescription().equalsIgnoreCase(_type) && f.getEnabled()).collect(Collectors.toList());
		if(filteredContent.size() > 0) {
			StringBuilder builder = new StringBuilder();
			StringBuilder priceBuilder = new StringBuilder();
			
			var weapons = "";
			var index = 1;
			for(Weapons weapon : filteredContent) {
				weapons += (weapons.length() == 0 ? "" : "-")+weapon.getWeaponID();
				builder.append(index+": *_"+weapon.getDescription()+" "+weapon.getStatDescription()+"_*\n");
				priceBuilder.append("*_"+weapon.getPrice()+" "+guild_settings.getCurrency()+"_*\n");
				index++;
			}
			EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(IniFileReader.getShopThumbnail());
			message.addField("Weapons: "+filteredContent.get(0).getCategoryDescription(), "***Here the requested stock! Send a digit to inspect a weapon!***", false);
			message.addField("Description", builder.toString(), true);
			message.addField("Price", priceBuilder.toString(), true);
			
			_e.getChannel().sendMessage(message.build()).queue();
			Hashes.addTempCache("shop_gu"+_e.getGuild().getId()+"ch"+_e.getChannel().getId()+"us"+_e.getMember().getUser().getId(), new Cache(180000, "wea-"+_type, weapons));
		}
	}
	
	public static void displaySingleWeapon(GuildMessageReceivedEvent _e, String _type, String [] weapons, Guilds guild_settings, final int selection) {
		if(selection >= 0 && selection < weapons.length) {
			final var weapon_id = Integer.parseInt(weapons[selection]);
			var shopItem = RankingSystemItems.SQLgetWholeWeaponShop(_e.getGuild().getIdLong(), guild_settings.getThemeID()).parallelStream().filter(f -> f.getWeaponID() == weapon_id && f.getEnabled()).findAny().orElse(null);
			EmbedBuilder embed = new EmbedBuilder();
			if(shopItem.getThumbnail().contains("http"))
				embed.setThumbnail(shopItem.getThumbnail());
			embed.setDescription("**"+shopItem.getDescription()+" "+shopItem.getStatDescription()+"**\n"+(shopItem.getFullDescription() != null && shopItem.getFullDescription().length() > 0 ? shopItem.getFullDescription() : ""));
			embed.setColor(Color.BLUE);
			embed.addField("Purchase", shopItem.getPrice()+" "+guild_settings.getCurrency(), true);
			embed.addField("Return", "Return to weapon selection", true);
			_e.getChannel().sendMessage(embed.build()).queue();
			Hashes.addTempCache("shop_gu"+_e.getGuild().getId()+"ch"+_e.getChannel().getId()+"us"+_e.getMember().getUser().getId(), new Cache(180000, _type, shopItem.getWeaponID()+"%"));
		}
		else {
			_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Please select a digit between 1 and "+weapons.length+" to closely inspect a weapon and then purchase!").build()).queue();
			var cache = Hashes.getTempCache("shop_gu"+_e.getGuild().getId()+"ch"+_e.getChannel().getId()+"us"+_e.getMember().getUser().getId()).setExpiration(180000);
			Hashes.addTempCache("shop_gu"+_e.getGuild().getId()+"ch"+_e.getChannel().getId()+"us"+_e.getMember().getUser().getId(), cache);
		}
	}
	
	public static void displaySkills(GuildMessageReceivedEvent _e, Guilds guild_settings) {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(IniFileReader.getShopThumbnail());
		var skills = RankingSystemItems.SQLgetSkills(_e.getGuild().getIdLong(), guild_settings.getThemeID());
		StringBuilder builder = new StringBuilder();
		StringBuilder priceBuilder = new StringBuilder();
		var skill = "";
		var index = 1;
		for(final var key : skills) {
			skill += (skill.length() == 0 ? "" : "-")+key.getSkillId();
			builder.append(index+": *_"+key.getDescription()+"_*\n");
			priceBuilder.append("*_"+key.getPrice()+" "+guild_settings.getCurrency()+"_*\n");
			index++;
		}
		message.addField("Skills", "***Here the requested stock! Send a digit to inspect a skill!***", false);
		message.addField("Description", builder.toString(), true);
		message.addField("Price", priceBuilder.toString(), true);
		
		_e.getChannel().sendMessage(message.build()).queue();
		Hashes.addTempCache("shop_gu"+_e.getGuild().getId()+"ch"+_e.getChannel().getId()+"us"+_e.getMember().getUser().getId(), new Cache(180000, "ski", skill));
	}
	
	public static void displaySingleSkill(GuildMessageReceivedEvent _e, Guilds guild_settings, String [] skills, final int selection) {
		if(selection >= 0 && selection < skills.length) {
			final var skill_id = Integer.parseInt(skills[selection]);
			var shopItem = RankingSystemItems.SQLgetSkills(_e.getGuild().getIdLong(), guild_settings.getThemeID()).parallelStream().filter(f -> f.getSkillId() == skill_id && f.getEnabled()).findAny().orElse(null);
			EmbedBuilder embed = new EmbedBuilder();
			if(shopItem.getThumbnail().contains("http"))
				embed.setThumbnail(shopItem.getThumbnail());
			embed.setDescription("**"+shopItem.getDescription()+"**\n"+(shopItem.getFullDescription() != null && shopItem.getFullDescription().length() > 0 ? shopItem.getFullDescription() : ""));
			embed.setColor(Color.BLUE);
			embed.addField("Purchase", shopItem.getPrice()+" "+guild_settings.getCurrency(), true);
			embed.addField("Return", "", true);
			_e.getChannel().sendMessage(embed.build()).queue();
			Hashes.addTempCache("shop_gu"+_e.getGuild().getId()+"ch"+_e.getChannel().getId()+"us"+_e.getMember().getUser().getId(), new Cache(180000, "ski", shopItem.getSkillId()+"%"));
		}
		else {
			_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Please select a digit between 1 and "+skills.length+" to closely inspect an item and then purchase!").build()).queue();
			var cache = Hashes.getTempCache("shop_gu"+_e.getGuild().getId()+"ch"+_e.getChannel().getId()+"us"+_e.getMember().getUser().getId()).setExpiration(180000);
			Hashes.addTempCache("shop_gu"+_e.getGuild().getId()+"ch"+_e.getChannel().getId()+"us"+_e.getMember().getUser().getId(), cache);
		}
	}
}
