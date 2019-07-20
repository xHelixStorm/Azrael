package commandsContainer;

import java.awt.Color;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Guilds;
import constructors.InventoryContent;
import constructors.Weapons;
import core.Hashes;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sql.RankingSystem;
import sql.RankingSystemItems;

public class PurchaseExecution {
	public static void purchase(MessageReceivedEvent e, final String type, final String item_number, Guilds guild_settings) {
		Logger logger = LoggerFactory.getLogger(PurchaseExecution.class);
		final var item_id = Integer.parseInt(item_number);
		var user_details = RankingSystem.SQLgetWholeRankView(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), guild_settings.getThemeID());
		if(!type.equals("wep") && !type.equals("ski")) {
			var skin = RankingSystem.SQLgetSkinshopContentAndType(e.getGuild().getIdLong(), guild_settings.getThemeID()).parallelStream().filter(s -> s.getItemID() == item_id).findAny().orElse(null);
			if(user_details.getCurrency() >= skin.getPrice()) {
				long new_currency = user_details.getCurrency() - skin.getPrice();
				var editedRows = 0;
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				InventoryContent inventory = RankingSystem.SQLgetNumberAndExpirationFromInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), skin.getShopDescription(), "perm", guild_settings.getThemeID());
				if(inventory != null)
					editedRows = RankingSystem.SQLUpdateCurrencyAndInsertInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), new_currency, skin.getItemID(), timestamp, inventory.getNumber()+1, guild_settings.getThemeID());
				else
					editedRows = RankingSystem.SQLUpdateCurrencyAndInsertInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), new_currency, skin.getItemID(), timestamp, 1, guild_settings.getThemeID());
				if(editedRows > 0) {
					user_details.setCurrency(new_currency);
					Hashes.addRanking(e.getGuild().getId()+"_"+e.getMember().getUser().getIdLong(), user_details);
					logger.debug("{} has purchased {}", e.getMember().getUser().getId(), skin.getShopDescription());
					e.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription("You have successfully purchased **"+skin.getShopDescription()+"**").build()).queue();
					returnSkinMenu(e, guild_settings, type);
				}
				else {
					e.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("An internal error occurred and purchase has been interrupted. Please contact an administrator!").build()).queue();
					RankingSystem.SQLInsertActionLog("critical", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "purchase interrupted", "An error occurred while purchasing "+skin.getShopDescription());
					Hashes.clearTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+"us"+e.getMember().getUser().getId());
				}
			}
			else {
				e.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(e.getMember().getAsMention()+" you currently don't have enough money to purchase this item/skin!").build()).queue();
				returnSkinMenu(e, guild_settings, type);
			}
		}
		else if(type.equals("wep")) {
			Weapons weapon = RankingSystemItems.SQLgetWholeWeaponShop(e.getGuild().getIdLong(), guild_settings.getThemeID()).parallelStream().filter(w -> w.getWeaponID() == item_id).findAny().orElse(null);
			if(user_details.getCurrency() >= weapon.getPrice()) {
				long new_currency = user_details.getCurrency() - weapon.getPrice();
				var editedRows = 0;
				long timestamp = System.currentTimeMillis();
				InventoryContent inventory = RankingSystemItems.SQLgetNumberAndExpirationFromInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), weapon.getWeaponID(), "limit", guild_settings.getThemeID(), true);
				if(inventory != null)
					editedRows = RankingSystemItems.SQLUpdateCurrencyAndInsertTimedInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), new_currency, weapon.getWeaponID(), timestamp, inventory.getExpiration().getTime(), 1, guild_settings.getThemeID(), true);
				else
					editedRows = RankingSystemItems.SQLUpdateCurrencyAndInsertTimedInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), new_currency, weapon.getWeaponID(), timestamp, timestamp, 1, guild_settings.getThemeID(), true);
				if(editedRows > 0) {
					user_details.setCurrency(new_currency);
					Hashes.addRanking(e.getGuild().getId()+"_"+e.getMember().getUser().getIdLong(), user_details);
					logger.debug("{} has purchased {}", e.getMember().getUser().getId(), weapon.getDescription()+" "+weapon.getStatDescription());
					e.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription("You have successfully purchased **"+weapon.getDescription()+" "+weapon.getStatDescription()+"**").build()).queue();
					ShopExecution.displayShopWeapons(e, weapon.getCategoryDescription());
				}
				else {
					e.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("An internal error occurred and purchase has been interrupted. Please contact an administrator!").build()).queue();
					RankingSystem.SQLInsertActionLog("critical", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "purchase interrupted", "An error occurred while purchasing "+weapon.getDescription()+" "+weapon.getStatDescription());
					Hashes.clearTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+"us"+e.getMember().getUser().getId());
				}
			}
			else {
				e.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(e.getMember().getAsMention()+" you don't have enough money to purchase this weapon!").build()).queue();
				ShopExecution.displayShopWeapons(e, weapon.getCategoryDescription());
			}
		}
		else if(type.equals("ski")) {
			var skill = RankingSystemItems.SQLgetSkills(e.getGuild().getIdLong(), guild_settings.getThemeID()).parallelStream().filter(f -> f.getSkillId() == item_id).findAny().orElse(null);
			if(user_details.getCurrency() >= skill.getPrice()) {
				long new_currency = user_details.getCurrency() - skill.getPrice();
				var editedRows = 0;
				long timestamp = System.currentTimeMillis();
				InventoryContent inventory = RankingSystemItems.SQLgetNumberAndExpirationFromInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), skill.getSkillId(), "limit", guild_settings.getThemeID(), false);
				if(inventory != null)
					editedRows = RankingSystemItems.SQLUpdateCurrencyAndInsertTimedInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), new_currency, skill.getSkillId(), timestamp, inventory.getExpiration().getTime(), 1, guild_settings.getThemeID(), false);
				else
					editedRows = RankingSystemItems.SQLUpdateCurrencyAndInsertTimedInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), new_currency, skill.getSkillId(), timestamp, timestamp, 1, guild_settings.getThemeID(), false);
				if(editedRows > 0) {
					user_details.setCurrency(new_currency);
					Hashes.addRanking(e.getGuild().getId()+"_"+e.getMember().getUser().getIdLong(), user_details);
					logger.debug("{} has purchased {}", e.getMember().getUser().getId(), skill.getDescription());
					e.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription("You have successfully purchased **"+skill.getDescription()+"**").build()).queue();
					ShopExecution.displaySkills(e, guild_settings);
				}
				else {
					e.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("An internal error occurred and purchase has been interrupted. Please contact an administrator!").build()).queue();
					RankingSystem.SQLInsertActionLog("critical", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "purchase interrupted", "An error occurred while purchasing "+skill.getDescription());
					Hashes.clearTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+"us"+e.getMember().getUser().getId());
				}
			}
			else {
				e.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(e.getMember().getAsMention()+" you don't have enough money to purchase this skill!").build()).queue();
				ShopExecution.displaySkills(e, guild_settings);
			}
		}
	}
	
	@SuppressWarnings("preview")
	private static void returnSkinMenu(MessageReceivedEvent e, Guilds guild_settings, final String type) {
		switch(type) {
			case "lev" -> ShopExecution.displayShop(e, "lev", guild_settings.getLevelDescription());
			case "ran" -> ShopExecution.displayShop(e, "ran", guild_settings.getRankDescription());
			case "pro" -> ShopExecution.displayShop(e, "pro", guild_settings.getProfileDescription());
			case "ico" -> ShopExecution.displayShop(e, "ico", guild_settings.getIconDescription());
			case "ite" -> ShopExecution.displayShop(e, "ite", "");
	}
	}
}
