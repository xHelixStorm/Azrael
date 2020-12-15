package commandsContainer;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import constructors.Guilds;
import constructors.Skins;
import constructors.Weapons;
import core.Hashes;
import enums.Translation;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.RankingSystem;
import sql.RankingSystemItems;
import util.STATIC;

/**
 * Extension of the shop command
 * @author xHelixStorm
 *
 */

public class ShopExecution {
	private final static Logger logger = LoggerFactory.getLogger(ShopExecution.class);
	
	public static void displayShop(GuildMessageReceivedEvent e, String _type, String _description) {
		Guilds guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
		final var content = RankingSystem.SQLgetSkinshopContentAndType(e.getGuild().getIdLong(), true);
		if(content != null) {
			List<Skins> filteredContent = content.parallelStream().filter(f -> f.getSkinType().equals(_type)).collect(Collectors.toList());
			StringBuilder builder = new StringBuilder();
			StringBuilder priceBuilder = new StringBuilder();
			var items = "";
			var index = 1;
			
			for(Skins skin_info : filteredContent) {
				items += (items.length() == 0 ? "" : "-")+skin_info.getItemID();
				String price;
				if(skin_info.getShopDescription().equals(_description)){price = STATIC.getTranslation(e.getMember(), Translation.SHOP_DEFAULT);}
				else{price = skin_info.getPrice()+" "+guild_settings.getCurrency();}
				if(RankingSystem.SQLgetItemID(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), skin_info.getItemID()) != 0 && !skin_info.getSkinType().equals("ite")){price = STATIC.getTranslation(e.getMember(), Translation.SHOP_BOUGHT);}
				builder.append(index+": *_"+skin_info.getShopDescription()+"_*\n");
				priceBuilder.append("*_"+price+"_*\n");
				index++;
			}
			EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(IniFileReader.getShopThumbnail());
			message.addField(filteredContent.get(0).getSkinDescription(), STATIC.getTranslation(e.getMember(), Translation.SHOP_SHOW), false);
			message.addField(STATIC.getTranslation(e.getMember(), Translation.SHOP_DESCRIPTION), builder.toString(), true);
			message.addField(STATIC.getTranslation(e.getMember(), Translation.SHOP_PRICE), priceBuilder.toString(), true);
			
			e.getChannel().sendMessage(message.build()).queue();
			Hashes.addTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, _type, items));
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Shop skins couldn't be retrieved in guild {}", e.getGuild().getId());
			Hashes.clearTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
		}
	}
	
	public static void displaySingleItem(GuildMessageReceivedEvent e, String _type, String [] _items, Guilds guild_settings, final int selection) {
		if(selection >= 0 && selection < _items.length) {
			final var shop = RankingSystem.SQLgetSkinshopContentAndType(e.getGuild().getIdLong(), true);
			if(shop != null) {
				final var item = Integer.parseInt(_items[selection]);
				var shopItem = shop.parallelStream().filter(f -> f.getItemID() == item).findAny().orElse(null);
				var defaultSkin = false;
				var alreadyPurchased = false;
				var terminator = "%";
				if((guild_settings.getLevelID() > 0 && guild_settings.getLevelDescription().equals(shopItem.getShopDescription())) || (guild_settings.getRankID() > 0 && guild_settings.getRankDescription().equals(shopItem.getShopDescription())) || (guild_settings.getProfileID() > 0 && guild_settings.getProfileDescription().equals(shopItem.getShopDescription())) || (guild_settings.getIconID() > 0 && guild_settings.getIconDescription().equals(shopItem.getShopDescription()))) {
					defaultSkin = true;
				}
				if(RankingSystem.SQLgetItemIDAndSkinType(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), shopItem.getShopDescription()) != null && !shopItem.getSkinType().equals("ite")) {
					alreadyPurchased = true;
				}
				EmbedBuilder embed = new EmbedBuilder();
				if(shopItem.getThumbnail() != null && shopItem.getThumbnail().contains("http"))
					embed.setThumbnail(shopItem.getThumbnail());
				embed.setDescription("**"+shopItem.getShopDescription()+"**\n"+(shopItem.getSkinFullDescription() != null && shopItem.getSkinFullDescription().length() > 0 ? shopItem.getSkinFullDescription() : ""));
				if(defaultSkin) {
					terminator = "$";
					embed.setColor(Color.YELLOW);
					embed.addField(STATIC.getTranslation(e.getMember(), Translation.SHOP_RETURN), STATIC.getTranslation(e.getMember(), Translation.SHOP_RETURN_MESSAGE), false);
				}
				else if(alreadyPurchased) {
					terminator = "#";
					embed.setColor(Color.YELLOW);
					embed.addField(STATIC.getTranslation(e.getMember(), Translation.SHOP_SELL), (shopItem.getPrice()/10)+" "+guild_settings.getCurrency(), true);
					embed.addField(STATIC.getTranslation(e.getMember(), Translation.SHOP_RETURN), STATIC.getTranslation(e.getMember(), Translation.SHOP_RETURN_MESSAGE), true);
				}
				else {
					embed.setColor(Color.BLUE);
					embed.addField(STATIC.getTranslation(e.getMember(), Translation.SHOP_PURCHASE), shopItem.getPrice()+" "+guild_settings.getCurrency(), true);
					embed.addField(STATIC.getTranslation(e.getMember(), Translation.SHOP_RETURN), STATIC.getTranslation(e.getMember(), Translation.SHOP_RETURN_MESSAGE), true);
				}
				e.getChannel().sendMessage(embed.build()).queue();
				Hashes.addTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, _type, shopItem.getItemID()+terminator));
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Shop content couldn't be retrieved in guild {}", e.getGuild().getId());
				Hashes.clearTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SHOP_INSPECT_ERR)).build()).queue();
			var cache = Hashes.getTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId()).setExpiration(180000);
			Hashes.addTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache);
		}
	}
	
	public static void displayWeaponCategories(GuildMessageReceivedEvent e) {
		final var categoriesList = RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong(), false);
		if(categoriesList != null) {
			StringBuilder builder = new StringBuilder();
			var categories = "";
			boolean first = true;
			for(String category : categoriesList) {
				categories += (categories.length() == 0 ? "" : "-")+category;
				if(first) {
					builder.append("**"+category+"**");
					first = false;
				}
				else {
					builder.append(", **"+category+"**");
				}
			}
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setThumbnail(IniFileReader.getShopThumbnail()).setDescription(STATIC.getTranslation(e.getMember(), Translation.SHOP_DISPLAY_WEP)+builder.toString()).build()).queue();
			Hashes.addTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "wea", categories));
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Weapon categories couldn't be retrieved in guild {}", e.getGuild().getId());
			Hashes.clearTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
		}
	}
	
	public static void displayShopWeapons(GuildMessageReceivedEvent e, String _type) {
		Guilds guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
		final var content = RankingSystemItems.SQLgetWholeWeaponShop(e.getGuild().getIdLong());
		if(content != null) {
			List<Weapons> filteredContent = content.parallelStream().filter(f -> f.getCategoryDescription().equalsIgnoreCase(_type) && f.getEnabled()).collect(Collectors.toList());
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
				message.addField(STATIC.getTranslation(e.getMember(), Translation.SHOP_WEAPONS)+filteredContent.get(0).getCategoryDescription(), STATIC.getTranslation(e.getMember(), Translation.SHOP_SHOW), false);
				message.addField(STATIC.getTranslation(e.getMember(), Translation.SHOP_DESCRIPTION), builder.toString(), true);
				message.addField(STATIC.getTranslation(e.getMember(), Translation.SHOP_PRICE), priceBuilder.toString(), true);
				
				e.getChannel().sendMessage(message.build()).queue();
				Hashes.addTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "wea-"+_type, weapons));
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Weapons couldn't be retrieved in guild {}", e.getGuild().getId());
			Hashes.clearTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
		}
	}
	
	public static void displaySingleWeapon(GuildMessageReceivedEvent e, String _type, String [] weapons, Guilds guild_settings, final int selection) {
		if(selection >= 0 && selection < weapons.length) {
			var shop = RankingSystemItems.SQLgetWholeWeaponShop(e.getGuild().getIdLong());
			if(shop != null) {
				final var weapon_id = Integer.parseInt(weapons[selection]);
				var shopItem = shop.parallelStream().filter(f -> f.getWeaponID() == weapon_id && f.getEnabled()).findAny().orElse(null);
				EmbedBuilder embed = new EmbedBuilder();
				if(shopItem.getThumbnail().contains("http"))
					embed.setThumbnail(shopItem.getThumbnail());
				embed.setDescription("**"+shopItem.getDescription()+" "+shopItem.getStatDescription()+"**\n"+(shopItem.getFullDescription() != null && shopItem.getFullDescription().length() > 0 ? shopItem.getFullDescription() : ""));
				embed.setColor(Color.BLUE);
				embed.addField(STATIC.getTranslation(e.getMember(), Translation.SHOP_PURCHASE), shopItem.getPrice()+" "+guild_settings.getCurrency(), true);
				embed.addField(STATIC.getTranslation(e.getMember(), Translation.SHOP_RETURN), STATIC.getTranslation(e.getMember(), Translation.SHOP_RETURN_MESSAGE), true);
				e.getChannel().sendMessage(embed.build()).queue();
				Hashes.addTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, _type, shopItem.getWeaponID()+"%"));
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Weapons couldn't be retrieved in guild {}", e.getGuild().getId());
				Hashes.clearTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SHOP_INSPECT_ERR)).build()).queue();
			var cache = Hashes.getTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId()).setExpiration(180000);
			Hashes.addTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache);
		}
	}
	
	public static void displaySkills(GuildMessageReceivedEvent e, Guilds guild_settings) {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(IniFileReader.getShopThumbnail());
		var skills = RankingSystemItems.SQLgetSkills(e.getGuild().getIdLong());
		if(skills != null) {
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
			message.addField(STATIC.getTranslation(e.getMember(), Translation.SHOP_SKILLS), STATIC.getTranslation(e.getMember(), Translation.SHOP_SHOW), false);
			message.addField(STATIC.getTranslation(e.getMember(), Translation.SHOP_DESCRIPTION), builder.toString(), true);
			message.addField(STATIC.getTranslation(e.getMember(), Translation.SHOP_PRICE), priceBuilder.toString(), true);
			
			e.getChannel().sendMessage(message.build()).queue();
			Hashes.addTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "ski", skill));
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Skills couldn't be retrieved in guild {}", e.getGuild().getId());
			Hashes.clearTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
		}
	}
	
	public static void displaySingleSkill(GuildMessageReceivedEvent e, Guilds guild_settings, String [] skills, final int selection) {
		if(selection >= 0 && selection < skills.length) {
			final var shop = RankingSystemItems.SQLgetSkills(e.getGuild().getIdLong());
			if(shop != null) {
				final var skill_id = Integer.parseInt(skills[selection]);
				var shopItem = shop.parallelStream().filter(f -> f.getSkillId() == skill_id && f.getEnabled()).findAny().orElse(null);
				EmbedBuilder embed = new EmbedBuilder();
				if(shopItem.getThumbnail().contains("http"))
					embed.setThumbnail(shopItem.getThumbnail());
				embed.setDescription("**"+shopItem.getDescription()+"**\n"+(shopItem.getFullDescription() != null && shopItem.getFullDescription().length() > 0 ? shopItem.getFullDescription() : ""));
				embed.setColor(Color.BLUE);
				embed.addField(STATIC.getTranslation(e.getMember(), Translation.SHOP_PURCHASE), shopItem.getPrice()+" "+guild_settings.getCurrency(), true);
				embed.addField(STATIC.getTranslation(e.getMember(), Translation.SHOP_RETURN), STATIC.getTranslation(e.getMember(), Translation.SHOP_RETURN_MESSAGE), true);
				e.getChannel().sendMessage(embed.build()).queue();
				Hashes.addTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "ski", shopItem.getSkillId()+"%"));
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Skills couldn't be retrieved in guild {}", e.getGuild().getId());
				Hashes.clearTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SHOP_INSPECT_ERR)).build()).queue();
			var cache = Hashes.getTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId()).setExpiration(180000);
			Hashes.addTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache);
		}
	}
}
