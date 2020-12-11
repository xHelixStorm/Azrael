package commandsContainer;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import constructors.Ranking;
import core.Hashes;
import core.UserPrivs;
import enums.Translation;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import sql.RankingSystem;
import sql.RankingSystemItems;
import util.STATIC;

/**
 * Addition to the equip command
 * @author xHelixStorm
 *
 */

public class EquipExecution {
	private final static Logger logger = LoggerFactory.getLogger(EquipExecution.class);
	
	public static void findGuild(PrivateMessageReceivedEvent e, List<String> guilds, final String filter) {
		var foundGuilds = guilds.parallelStream().filter(f -> f.contains(filter)).collect(Collectors.toList());
		if(foundGuilds != null) {
			if(foundGuilds.size() == 1) {
				if(UserPrivs.comparePrivilege(e.getJDA().getGuildById(foundGuilds.get(0)).getMemberById(e.getAuthor().getId()), GuildIni.getEquipLevel(Long.parseLong(foundGuilds.get(0))))) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation3(e.getAuthor(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_HELP)).build()).queue();
					Hashes.addTempCache("equip_us"+e.getAuthor().getId(), new Cache(180000, foundGuilds.get(0)));
				}
				else {
					EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle(STATIC.getTranslation3(e.getAuthor(), Translation.EMBED_TITLE_DENIED));
					e.getChannel().sendMessage(denied.setDescription(e.getAuthor().getAsMention() + STATIC.getTranslation3(e.getAuthor(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(GuildIni.getEquipLevel(Long.parseLong(foundGuilds.get(0))), e.getJDA().getGuildById(foundGuilds.get(0)).getMemberById(e.getAuthor().getIdLong()))).build()).queue();
				}
			}
			else {
				StringBuilder out = new StringBuilder();
				var thisGuilds = "";
				var i = 1;
				for(final var guild_id : foundGuilds) {
					if(UserPrivs.comparePrivilege(e.getJDA().getGuildById(guild_id).getMemberById(e.getAuthor().getId()), GuildIni.getEquipLevel(Long.parseLong(guild_id)))) {
						out.append("**"+i+": "+e.getJDA().getGuildById(guild_id)+" ("+e.getJDA().getGuildById(guild_id).getId()+")**\n");
					}
					else {
						out.append("**"+i+": "+e.getJDA().getGuildById(guild_id)+" ("+e.getJDA().getGuildById(guild_id).getId()+") "+STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_DENIED)+"**\n");
					}
					if(i != foundGuilds.size())
						thisGuilds += guild_id+"-";
					else
						thisGuilds += guild_id;
					i++;
				}
				try {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_SERVER_SELECT)+out.toString()).build()).queue();
					Hashes.addTempCache("equip_us"+e.getAuthor().getId(), new Cache(180000, thisGuilds, "wait"));
				} catch(IllegalArgumentException iae) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_SERVER_SELECT_2)).build()).queue();
					Hashes.addTempCache("equip_us"+e.getAuthor().getId(), new Cache(180000, thisGuilds, "err"));
				}
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_NO_SERVER_FOUND)).build()).queue();
			var cache = Hashes.getTempCache("equip_us"+e.getAuthor().getId()).setExpiration(180000);
			Hashes.addTempCache("equip_us"+e.getAuthor().getId(), cache);
		}
	}
	
	public static void selectAvailableGuilds(PrivateMessageReceivedEvent e, final String guilds, final int selection) {
		var guild_ids = guilds.split("-");
		if(selection >= 0 && selection < guild_ids.length) {
			var guild_id = guild_ids[selection];
			if(UserPrivs.comparePrivilege(e.getJDA().getGuildById(guild_id).getMemberById(e.getAuthor().getId()), GuildIni.getEquipLevel(Long.parseLong(guild_id)))) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.blue).setTitle(STATIC.getTranslation3(e.getAuthor(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_HELP)).build()).queue();
				Hashes.addTempCache("equip_us"+e.getAuthor().getId(), new Cache(180000, guild_id));
			}
			else {
				EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle(STATIC.getTranslation3(e.getAuthor(), Translation.EMBED_TITLE_DENIED));
				e.getChannel().sendMessage(denied.setDescription(e.getAuthor().getAsMention() + STATIC.getTranslation3(e.getAuthor(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(GuildIni.getEquipLevel(Long.parseLong(guild_id)), e.getJDA().getGuildById(guild_id).getMember(e.getAuthor()))).build()).queue();
				var cache = Hashes.getTempCache("equip_us"+e.getAuthor().getId()).setExpiration(180000);
				Hashes.addTempCache("equip_us"+e.getAuthor().getId(), cache);
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_SELECT_ERR)).build()).queue();
			var cache = Hashes.getTempCache("equip_us"+e.getAuthor().getId()).setExpiration(180000);
			Hashes.addTempCache("equip_us"+e.getAuthor().getId(), cache);
		}
	}
	
	public static void equipmentItemScreen(PrivateMessageReceivedEvent e, final String guild_id, String action) {
		final long guild = guild_id.transform(v -> Long.valueOf(v));
		Ranking user_details = null;
		RankingSystem.SQLDeleteInventory();
		//retrieve weapon descriptions for already equipped items or find out, if they're expired from the inventory and then remove
		var weapon1 = RankingSystemItems.SQLgetEquippedWeaponDescription(e.getAuthor().getIdLong(), guild, 1);
		var weapon2 = RankingSystemItems.SQLgetEquippedWeaponDescription(e.getAuthor().getIdLong(), guild, 2);
		var weapon3 = RankingSystemItems.SQLgetEquippedWeaponDescription(e.getAuthor().getIdLong(), guild, 3);
		var skill = RankingSystemItems.SQLgetEquippedSkillDescription(e.getAuthor().getIdLong(), guild);
		
		if(weapon1.equals("expired")) {
			if(RankingSystemItems.SQLRemoveEquippedWeapon(e.getAuthor().getIdLong(), guild, 1) == 0) {
				logger.error("Weapon slot 1 couldn't be unequipped on expiration for user {} in guild {}", e.getAuthor().getId(), guild);
			}
			if(user_details == null) user_details = RankingSystem.SQLgetWholeRankView(e.getAuthor().getIdLong(), guild);
			if(user_details != null) {
				user_details.setWeapon1(0);
				Hashes.addRanking(guild, e.getAuthor().getIdLong(), user_details);
			}
			weapon1 = STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_EMPTY);
		}
		if(weapon2.equals("expired")) {
			if(RankingSystemItems.SQLRemoveEquippedWeapon(e.getAuthor().getIdLong(), guild, 2) == 0) {
				logger.error("Weapon slot 2 couldn't be unequipped on expiration for user {} in guild {}", e.getAuthor().getId(), guild);
			}
			if(user_details == null) user_details = RankingSystem.SQLgetWholeRankView(e.getAuthor().getIdLong(), guild);
			if(user_details != null) {
				user_details.setWeapon2(0);
				Hashes.addRanking(guild, e.getAuthor().getIdLong(), user_details);
			}
			weapon2 = STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_EMPTY);
		}
		if(weapon3.equals("expired")) {
			if(RankingSystemItems.SQLRemoveEquippedWeapon(e.getAuthor().getIdLong(), guild, 3) == 0) {
				logger.error("Weapon slot 3 couldn't be unequipped on expiration for user {} in guild {}", e.getAuthor().getId(), guild);
			}
			if(user_details == null) user_details = RankingSystem.SQLgetWholeRankView(e.getAuthor().getIdLong(), guild);
			if(user_details != null) {
				user_details.setWeapon3(0);
				Hashes.addRanking(guild, e.getAuthor().getIdLong(), user_details);
			}
			weapon3 = STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_EMPTY);
		}
		if(skill.equals("expired")) {
			if(RankingSystemItems.SQLRemoveEquippedSkill(e.getAuthor().getIdLong(), guild) == 0) {
				logger.error("Skill slot couldn't be unequipped on expiration for user {} in guild {}", e.getAuthor().getId(), guild);
			}
			if(user_details == null) user_details = RankingSystem.SQLgetWholeRankView(e.getAuthor().getIdLong(), guild);
			if(user_details != null) {
				user_details.setSkill(0);
				Hashes.addRanking(guild, e.getAuthor().getIdLong(), user_details);
			}
			skill = STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_EMPTY);
		}
		
		e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_EQUIPMENT)
				+ STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_SLOT_1).replace("{}", weapon1)
				+ STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_SLOT_2).replace("{}", weapon2)
				+ STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_SLOT_3).replace("{}", weapon3)
				+ STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_SLOT_4).replace("{}", skill))
				.build()).queue();
		Hashes.addTempCache("equip_us"+e.getAuthor().getId(), new Cache(180000, guild_id, action));
	}
	
	public static void removeWholeEquipment(PrivateMessageReceivedEvent e, final long guild_id) {
		var user_details = RankingSystem.SQLgetWholeRankView(e.getAuthor().getIdLong(), guild_id);
		if(user_details.getWeapon1() != 0 || user_details.getWeapon2() != 0 || user_details.getWeapon3() != 0 || user_details.getSkill() != 0) {
			if(RankingSystemItems.SQLUnequipWholeEquipment(e.getAuthor().getIdLong(), guild_id) > 0) {
				user_details.setWeapon1(0);
				user_details.setWeapon2(0);
				user_details.setWeapon3(0);
				user_details.setSkill(0);
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_UNEQUIP_ALL)).build()).queue();
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation3(e.getAuthor(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("All weapons and skill slots couldn't be unequipped for user {} in guild {}", e.getAuthor().getId(), guild_id);
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_UNEQUIP_ALL_EMPTY)).build()).queue();
		}
	}
	
	public static void slotSelection(PrivateMessageReceivedEvent e, final String guild_id, final int selection, String action) {
		if(selection > 0 && selection <= 4) {
			if(action.equals("set")) {
				switch(selection) {
					case 1, 2, 3 -> {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_TITLE_SLOT).replace("{}", ""+selection)).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_SELECT_WEAPON)).build()).queue();
					}
					case 4 -> {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_TITLE_SKILL)).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_SELECT_SKILL)).build()).queue();
					}
				}
				Hashes.addTempCache("equip_us"+e.getAuthor().getId(), new Cache(180000, guild_id, "set-"+selection));
			}
			else if(action.equals("remove")) {
				int itemID = 0;
				long guild = Long.parseLong(guild_id);
				var user_details = RankingSystem.SQLgetWholeRankView(e.getAuthor().getIdLong(), guild);
				switch(selection) {
					case 1 -> itemID = user_details.getWeapon1();
					case 2 -> itemID = user_details.getWeapon2();
					case 3 -> itemID = user_details.getWeapon3();
					case 4 -> itemID = user_details.getSkill();
				}
				if(itemID != 0) {
					if(RankingSystemItems.SQLUnequipWeapon(e.getAuthor().getIdLong(), guild, selection) > 0) {
						switch(selection) {
							case 1 -> {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_WEP_UNEQUIPPED)).build()).queue();
								user_details.setWeapon1(0);
							}
							case 2 -> {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_WEP_UNEQUIPPED)).build()).queue();
								user_details.setWeapon2(0);
							}
							case 3 -> {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_WEP_UNEQUIPPED)).build()).queue();
								user_details.setWeapon3(0);
							}
							case 4 -> {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_SKILL_UNEQUIPPED)).build()).queue();
								user_details.setSkill(0);
							}
						}
						equipmentItemScreen(e, guild_id, "remove");
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation3(e.getAuthor(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Slot {} couldn't be unequipped for user {} in guild {}", selection, e.getAuthor().getId(), guild_id);
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_SLOT_EMPTY)).build()).queue();
					equipmentItemScreen(e, guild_id, "remove");
				}
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_SELECT_DIGIT)).build()).queue();
			var cache = Hashes.getTempCache("equip_us"+e.getAuthor().getId()).setExpiration(180000);
			Hashes.addTempCache("equip_us"+e.getAuthor().getId(), cache);
		}
	}
	
	public static void searchInventory(PrivateMessageReceivedEvent e, final String guild_id, final int selection, final String item) {
		var guild = Long.parseLong(guild_id);
		if(selection >= 1 && selection <= 3) {
			//weapons
			var weapons = RankingSystemItems.SQLfilterInventoryWeapons(e.getAuthor().getIdLong(), guild, item);
			if(weapons == null) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation3(e.getAuthor(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Weapon/Skill {} couldn't be retrieved for user {} in guild {}", item, e.getAuthor().getId(), guild_id);
			}
			else if(weapons.size() == 1) {
				var user_details = RankingSystem.SQLgetWholeRankView(e.getAuthor().getIdLong(), guild);
				if(user_details != null) {
					var weapon = weapons.get(0);
					if(weapon.getItemId() != user_details.getWeapon1() && weapon.getItemId() != user_details.getWeapon2() && weapon.getItemId() != user_details.getWeapon3()) {
						var weapon1 = RankingSystemItems.SQLgetWholeWeaponShop(guild).parallelStream().filter(f -> f.getWeaponID() == user_details.getWeapon1()).findAny().orElse(null);
						var weapon2 = RankingSystemItems.SQLgetWholeWeaponShop(guild).parallelStream().filter(f -> f.getWeaponID() == user_details.getWeapon2()).findAny().orElse(null);
						var weapon3 = RankingSystemItems.SQLgetWholeWeaponShop(guild).parallelStream().filter(f -> f.getWeaponID() == user_details.getWeapon3()).findAny().orElse(null);
						if(!weapon.getAbbreviation().equals((weapon1 != null ? weapon1.getWeaponAbbv() : "")) && !weapon.getAbbreviation().equals((weapon2 != null ? weapon2.getWeaponAbbv() : "")) && !weapon.getAbbreviation().equals((weapon3 != null ? weapon3.getWeaponAbbv() : ""))) {
							if(RankingSystemItems.SQLEquipWeapon(e.getAuthor().getIdLong(), guild, weapon.getItemId(), selection) > 0) {
								switch(selection) {
									case 1 -> user_details.setWeapon1(weapon.getItemId());
									case 2 -> user_details.setWeapon2(weapon.getItemId());
									case 3 -> user_details.setWeapon3(weapon.getItemId());
								}
								Hashes.addRanking(guild, e.getAuthor().getIdLong(), user_details);
								EmbedBuilder embed = new EmbedBuilder().setColor(Color.BLUE);
								e.getChannel().sendMessage(embed.setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_EQUIPPED)).build()).queue();
								equipmentItemScreen(e, guild_id, "set");
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation3(e.getAuthor(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.GENERAL_ERROR)).build()).queue();
								logger.error("Weapon {} couldn't be equipped for user {} in guild {}", weapon.getItemId(), e.getAuthor().getId(), guild);
								Hashes.clearTempCache("equip_us"+e.getAuthor().getId());
							}
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_ALREADY_EQUIPPED)).build()).queue();
							var cache = Hashes.getTempCache("equip_us"+e.getAuthor().getId()).setExpiration(180000);
							Hashes.addTempCache("equip_us"+e.getAuthor().getId(), cache);
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_ALREADY_EQUIPPED_2)).build()).queue();
						var cache = Hashes.getTempCache("equip_us"+e.getAuthor().getId()).setExpiration(180000);
						Hashes.addTempCache("equip_us"+e.getAuthor().getId(), cache);
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation3(e.getAuthor(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Ranking details of user {} couldn't be retrieved in guild {}", e.getAuthor().getId(), guild_id);
				}
				
			}
			else if(weapons.size() > 1) {
				StringBuilder sb = new StringBuilder();
				var items = "";
				var index = 1;
				for(final var weapon : weapons) {
					if(index != weapons.size()) 
						items += weapon.getItemId()+"-";
					else
						items += weapon.getItemId();
					sb.append("**"+index+": "+weapon.getDescription()+" "+weapon.getStat()+"**\n");
					index++;
				}
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_SELECT_WEAPON_2)+sb.toString()).build()).queue();
				Hashes.addTempCache("equip_us"+e.getAuthor().getId(), new Cache(180000, guild_id, "set-"+selection+"_"+items));
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_SELECT_WEP_NOT_FOUND)).build()).queue();
				var cache = Hashes.getTempCache("equip_us"+e.getAuthor().getId()).setExpiration(180000);
				Hashes.addTempCache("equip_us"+e.getAuthor().getId(), cache);
			}
		}
		else if(selection == 4) {
			//skill
			var skills = RankingSystemItems.SQLfilterInventorySkills(e.getAuthor().getIdLong(), guild, item);
			if(skills.size() == 1) {
				var user_details = RankingSystem.SQLgetWholeRankView(e.getAuthor().getIdLong(), guild);
				var skill = skills.get(0);
				if(skill.getItemId() != user_details.getSkill()) {
					if(RankingSystemItems.SQLEquipSkill(e.getAuthor().getIdLong(), guild, skill.getItemId()) > 0) {
						user_details.setSkill(skill.getItemId());
						Hashes.addRanking(guild, e.getAuthor().getIdLong(), user_details);
						EmbedBuilder embed = new EmbedBuilder().setColor(Color.BLUE);
						e.getChannel().sendMessage(embed.setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_SKILL_EQUIPPED)).build()).queue();
						equipmentItemScreen(e, guild_id, "set");
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation3(e.getAuthor(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Skill {} couldn't be equipped for user {} in guild {}", skill.getItemId(), e.getAuthor().getId(), guild);
						Hashes.clearTempCache("equip_us"+e.getAuthor().getId());
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_SKILL_ALREADY_EQUIPPED)).build()).queue();
					var cache = Hashes.getTempCache("equip_us"+e.getAuthor().getId()).setExpiration(180000);
					Hashes.addTempCache("equip_us"+e.getAuthor().getId(), cache);
				}
			}
			else if(skills.size() > 1) {
				StringBuilder sb = new StringBuilder();
				var items = "";
				var index = 1;
				for(final var skill : skills) {
					if(index != skills.size()) 
						items += skill.getItemId()+"-";
					else
						items += skill.getItemId();
					sb.append("**"+index+": "+skill.getDescription()+"**\n");
					index++;
				}
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_SELECT_SKILL_2)+sb.toString()).build()).queue();
				Hashes.addTempCache("equip_us"+e.getAuthor().getId(), new Cache(180000, guild_id, "set-"+selection+"_"+items));
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_SELECT_SKILL_NOT_FOUND)).build()).queue();
				var cache = Hashes.getTempCache("equip_us"+e.getAuthor().getId()).setExpiration(180000);
				Hashes.addTempCache("equip_us"+e.getAuthor().getId(), cache);
			}
		}
	}
	
	public static void selectItem(PrivateMessageReceivedEvent e, final String guild_id, final int slot, final int selection, final String [] items) {
		if(selection >= 0 && selection < items.length) {
			var guild = Long.parseLong(guild_id);
			switch(slot) {
				case 1, 2, 3 -> {
					var weapon_id = Integer.parseInt(items[selection]);
					var user_details = RankingSystem.SQLgetWholeRankView(e.getAuthor().getIdLong(), guild);
					if(user_details != null) {
						if(weapon_id != user_details.getWeapon1() && weapon_id != user_details.getWeapon2() && weapon_id != user_details.getWeapon3()) {
							var weapon1 = RankingSystemItems.SQLgetWholeWeaponShop(guild).parallelStream().filter(f -> f.getWeaponID() == user_details.getWeapon1()).findAny().orElse(null);
							var weapon2 = RankingSystemItems.SQLgetWholeWeaponShop(guild).parallelStream().filter(f -> f.getWeaponID() == user_details.getWeapon2()).findAny().orElse(null);
							var weapon3 = RankingSystemItems.SQLgetWholeWeaponShop(guild).parallelStream().filter(f -> f.getWeaponID() == user_details.getWeapon3()).findAny().orElse(null);
							var selectedWeapon = RankingSystemItems.SQLgetWholeWeaponShop(guild).parallelStream().filter(f -> f.getWeaponID() == weapon_id).findAny().orElse(null);
							if(!selectedWeapon.getWeaponAbbv().equals((weapon1 != null ? weapon1.getWeaponAbbv() : "")) && !selectedWeapon.getWeaponAbbv().equals((weapon2 != null ? weapon2.getWeaponAbbv() : "")) && !selectedWeapon.getWeaponAbbv().equals((weapon3 != null ? weapon3.getWeaponAbbv() : ""))) {
								if(RankingSystemItems.SQLEquipWeapon(e.getAuthor().getIdLong(), guild, weapon_id, slot) > 0) {
									switch(selection) {
										case 0 -> user_details.setWeapon1(weapon_id);
										case 1 -> user_details.setWeapon2(weapon_id);
										case 2 -> user_details.setWeapon3(weapon_id);
									}
									Hashes.addRanking(guild, e.getAuthor().getIdLong(), user_details);
									EmbedBuilder embed = new EmbedBuilder().setColor(Color.BLUE);
									e.getChannel().sendMessage(embed.setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_EQUIPPED)).build()).queue();
									equipmentItemScreen(e, guild_id, "set");
								}
								else {
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation3(e.getAuthor(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.GENERAL_ERROR)).build()).queue();
									logger.error("Weapon {} couldn't be equipped for user {} in guild {}", weapon_id, e.getAuthor().getId(), guild);
									Hashes.clearTempCache("equip_us"+e.getAuthor().getId());
								}
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_ALREADY_EQUIPPED)).build()).queue();
								var cache = Hashes.getTempCache("equip_us"+e.getAuthor().getId()).setExpiration(180000);
								Hashes.addTempCache("equip_us"+e.getAuthor().getId(), cache);
							}
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_ALREADY_EQUIPPED_2)).build()).queue();
							var cache = Hashes.getTempCache("equip_us"+e.getAuthor().getId()).setExpiration(180000);
							Hashes.addTempCache("equip_us"+e.getAuthor().getId(), cache);
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation3(e.getAuthor(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Ranking details of user {} couldn't be retrieved in guild {}", e.getAuthor().getId(), guild_id);
					}
				}
				case 4 -> {
					var skill_id = Integer.parseInt(items[selection]);
					var user_details = RankingSystem.SQLgetWholeRankView(e.getAuthor().getIdLong(), guild);
					if(user_details != null) {
						if(user_details.getSkill() != skill_id) {
							if(RankingSystemItems.SQLEquipSkill(e.getAuthor().getIdLong(), guild, skill_id) > 0) {
								user_details.setSkill(skill_id);
								Hashes.addRanking(guild, e.getAuthor().getIdLong(), user_details);
								EmbedBuilder embed = new EmbedBuilder().setColor(Color.BLUE);
								e.getChannel().sendMessage(embed.setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_SKILL_EQUIPPED)).build()).queue();
								equipmentItemScreen(e, guild_id, "set");
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation3(e.getAuthor(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.GENERAL_ERROR)).build()).queue();
								logger.error("Skill {} couldn't be equipped for user {} in guild {}", skill_id, e.getAuthor().getId(), guild);
								Hashes.clearTempCache("equip_us"+e.getAuthor().getId());
							}
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_SKILL_ALREADY_EQUIPPED)).build()).queue();
							var cache = Hashes.getTempCache("equip_us"+e.getAuthor().getId()).setExpiration(180000);
							Hashes.addTempCache("equip_us"+e.getAuthor().getId(), cache);
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation3(e.getAuthor(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Ranking details of user {} couldn't be retrieved in guild {}", e.getAuthor().getId(), guild_id);
					}
				}
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_SELECT_DIGIT)).build()).queue();
			var cache = Hashes.getTempCache("equip_us"+e.getAuthor().getId()).setExpiration(180000);
			Hashes.addTempCache("equip_us"+e.getAuthor().getId(), cache);
		}
	}
}
