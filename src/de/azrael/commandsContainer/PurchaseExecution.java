package de.azrael.commandsContainer;

import java.awt.Color;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Guilds;
import de.azrael.constructors.InventoryContent;
import de.azrael.constructors.Weapons;
import de.azrael.core.Hashes;
import de.azrael.enums.Translation;
import de.azrael.sql.RankingSystem;
import de.azrael.sql.RankingSystemItems;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class PurchaseExecution {
	private final static Logger logger = LoggerFactory.getLogger(PurchaseExecution.class);
	
	public static void purchase(GuildMessageReceivedEvent e, final String type, final String item_number, Guilds guild_settings) {
		final var item_id = Integer.parseInt(item_number);
		var user_details = RankingSystem.SQLgetWholeRankView(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong());
		if(!type.equals("wep") && !type.equals("ski")) {
			var skin = RankingSystem.SQLgetSkinshopContentAndType(e.getGuild().getIdLong(), true).parallelStream().filter(s -> s.getItemID() == item_id).findAny().orElse(null);
			if(user_details.getCurrency() >= skin.getPrice()) {
				long new_currency = user_details.getCurrency() - skin.getPrice();
				var editedRows = 0;
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				InventoryContent inventory = RankingSystem.SQLgetNumberAndExpirationFromInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), skin.getShopDescription(), "perm");
				if(inventory != null)
					editedRows = RankingSystem.SQLUpdateCurrencyAndInsertInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), new_currency, skin.getItemID(), timestamp, inventory.getNumber()+1);
				else
					editedRows = RankingSystem.SQLUpdateCurrencyAndInsertInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), new_currency, skin.getItemID(), timestamp, 1);
				if(editedRows > 0) {
					user_details.setCurrency(new_currency);
					Hashes.addRanking(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), user_details);
					logger.info("User {} has purchased {} with value {} in guild {}", e.getMember().getUser().getId(), skin.getShopDescription(), skin.getPrice(), e.getGuild().getId());
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SHOP_PURCHASED).replace("{}", skin.getShopDescription())).build()).queue();
					returnSkinMenu(e, guild_settings, type);
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Item or skin {} couldn't be purchased by user {} in guild {}", skin.getShopDescription(), e.getMember().getUser().getId(), e.getGuild().getId());
					RankingSystem.SQLInsertActionLog("critical", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "purchase interrupted", "An error occurred while purchasing "+skin.getShopDescription());
					Hashes.clearTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SHOP_NOT_ENOUGH_CUR)).build()).queue();
				returnSkinMenu(e, guild_settings, type);
			}
		}
		else if(type.equals("wep")) {
			Weapons weapon = RankingSystemItems.SQLgetWholeWeaponShop(e.getGuild().getIdLong()).parallelStream().filter(w -> w.getWeaponID() == item_id).findAny().orElse(null);
			if(user_details.getCurrency() >= weapon.getPrice()) {
				long new_currency = user_details.getCurrency() - weapon.getPrice();
				var editedRows = 0;
				long timestamp = System.currentTimeMillis();
				InventoryContent inventory = RankingSystemItems.SQLgetNumberAndExpirationFromInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), weapon.getWeaponID(), "limit", true);
				if(inventory != null)
					editedRows = RankingSystemItems.SQLUpdateCurrencyAndInsertTimedInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), new_currency, weapon.getWeaponID(), timestamp, inventory.getExpiration().getTime(), 1, true, 0);
				else
					editedRows = RankingSystemItems.SQLUpdateCurrencyAndInsertTimedInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), new_currency, weapon.getWeaponID(), timestamp, timestamp, 1, true, 0);
				if(editedRows > 0) {
					user_details.setCurrency(new_currency);
					Hashes.addRanking(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), user_details);
					logger.info("User {} has purchased {} with value {} in guild {}", e.getMember().getUser().getId(), weapon.getDescription()+(weapon.getStatDescription() != null ? " "+weapon.getStatDescription() : ""), weapon.getPrice(), e.getGuild().getId());
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SHOP_PURCHASED).replace("{}", weapon.getDescription()+(weapon.getStatDescription() != null ? " "+weapon.getStatDescription() : ""))).build()).queue();
					ShopExecution.displayShopWeapons(e, weapon.getCategoryDescription());
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Weapon {} couldn't be purchased by user {} in guild {}", weapon.getDescription()+(weapon.getStatDescription() != null ? " "+weapon.getStatDescription() : ""), e.getMember().getUser().getId(), e.getGuild().getId());
					RankingSystem.SQLInsertActionLog("critical", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "purchase interrupted", "An error occurred while purchasing "+weapon.getDescription()+(weapon.getStatDescription() != null ? " "+weapon.getStatDescription() : ""));
					Hashes.clearTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SHOP_NOT_ENOUGH_CUR)).build()).queue();
				ShopExecution.displayShopWeapons(e, weapon.getCategoryDescription());
			}
		}
		else if(type.equals("ski")) {
			var skill = RankingSystemItems.SQLgetSkills(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getSkillId() == item_id).findAny().orElse(null);
			if(user_details.getCurrency() >= skill.getPrice()) {
				long new_currency = user_details.getCurrency() - skill.getPrice();
				var editedRows = 0;
				long timestamp = System.currentTimeMillis();
				InventoryContent inventory = RankingSystemItems.SQLgetNumberAndExpirationFromInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), skill.getSkillId(), "limit", false);
				if(inventory != null)
					editedRows = RankingSystemItems.SQLUpdateCurrencyAndInsertTimedInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), new_currency, skill.getSkillId(), timestamp, inventory.getExpiration().getTime(), 1, false, 0);
				else
					editedRows = RankingSystemItems.SQLUpdateCurrencyAndInsertTimedInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), new_currency, skill.getSkillId(), timestamp, timestamp, 1, false, 0);
				if(editedRows > 0) {
					user_details.setCurrency(new_currency);
					Hashes.addRanking(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), user_details);
					logger.info("User {} has purchased {} with value {} in guild {}", e.getMember().getUser().getId(), skill.getDescription(), skill.getPrice(), e.getGuild().getId());
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SHOP_PURCHASED).replace("{}", skill.getDescription())).build()).queue();
					ShopExecution.displaySkills(e, guild_settings);
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Skill {} couldn't be purchased by user {} in guild {}", skill.getDescription(), e.getMember().getUser().getId(), e.getGuild().getId());
					RankingSystem.SQLInsertActionLog("critical", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "purchase interrupted", "An error occurred while purchasing "+skill.getDescription());
					Hashes.clearTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SHOP_NOT_ENOUGH_CUR)).build()).queue();
				ShopExecution.displaySkills(e, guild_settings);
			}
		}
	}
	
	public static void sell(GuildMessageReceivedEvent e, final String type, final String item_number, Guilds guild_settings) {
		final var item_id = Integer.parseInt(item_number);
		var user_details = RankingSystem.SQLgetWholeRankView(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong());
		if(!type.equals("wep") && !type.equals("ski")) {
			var skin = RankingSystem.SQLgetSkinshopContentAndType(e.getGuild().getIdLong(), true).parallelStream().filter(s -> s.getItemID() == item_id).findAny().orElse(null);
			var newCurrency = user_details.getCurrency()+(skin.getPrice()/10);
			user_details.setLastUpdate(new Timestamp(System.currentTimeMillis()));
			if(RankingSystem.SQLUpdateCurrencyAndRemoveInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), newCurrency, skin.getItemID(), user_details.getLastUpdate()) > 0) {
				logger.info("User {} has sold {} for {} in guild {}", e.getMember().getUser().getId(), skin.getShopDescription(), (skin.getPrice()/10), e.getGuild().getId());
				user_details.setCurrency(newCurrency);
				var curLevelSkin = RankingSystem.SQLgetRankingLevel(user_details.getLevel(), e.getGuild().getIdLong());
				if(curLevelSkin.getSkinDescription().equals(skin.getShopDescription())) {
					user_details.setRankingLevel(guild_settings.getLevelID());
					if(RankingSystem.SQLUpdateUserLevelSkin(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), guild_settings.getLevelID()) == 0) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("The default level up skin for user {} couldn't be updated upon selling in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
					}
				}
				var curRankSkin = RankingSystem.SQLgetRankingRank(user_details.getLevel(), e.getGuild().getIdLong());
				if(curRankSkin.getSkinDescription().equals(skin.getShopDescription())) {
					user_details.setRankingRank(guild_settings.getRankID());
					if(RankingSystem.SQLUpdateUserRankSkin(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), guild_settings.getRankID()) == 0) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("The default rank skin for user {} couldn't be updated upon selling in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
					}
				}
				var curProfileSkin = RankingSystem.SQLgetRankingProfile(user_details.getLevel(), e.getGuild().getIdLong());
				if(curProfileSkin.getSkinDescription().equals(skin.getShopDescription())) {
					user_details.setRankingProfile(guild_settings.getProfileID());
					if(RankingSystem.SQLUpdateUserProfileSkin(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), guild_settings.getProfileID()) == 0) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("The default profile skin for user {} couldn't be updated upon selling in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
					}
				}
				var curIconSkin = RankingSystem.SQLgetRankingIcons(user_details.getLevel(), e.getGuild().getIdLong());
				if(curIconSkin.getSkinDescription().equals(skin.getShopDescription())) {
					user_details.setRankingIcon(guild_settings.getIconID());
					if(RankingSystem.SQLUpdateUserIconSkin(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), guild_settings.getIconID()) == 0) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("The default icon skin for user {} couldn't be updated upon selling in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
					}
				}
				Hashes.addRanking(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), user_details);
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SHOP_SOLD)).build()).queue();
				returnSkinMenu(e, guild_settings, type);
			}
			else {
				returnSkinMenu(e, guild_settings, type);
			}
		}
	}
	
	private static void returnSkinMenu(GuildMessageReceivedEvent e, Guilds guild_settings, final String type) {
		switch(type) {
			case "lev" -> ShopExecution.displayShop(e, "lev", guild_settings.getLevelDescription());
			case "ran" -> ShopExecution.displayShop(e, "ran", guild_settings.getRankDescription());
			case "pro" -> ShopExecution.displayShop(e, "pro", guild_settings.getProfileDescription());
			case "ico" -> ShopExecution.displayShop(e, "ico", guild_settings.getIconDescription());
			case "ite" -> ShopExecution.displayShop(e, "ite", "");
	}
	}
}
