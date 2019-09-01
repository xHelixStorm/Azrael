package commandsContainer;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;

import constructors.Cache;
import constructors.Rank;
import core.Hashes;
import core.UserPrivs;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import sql.RankingSystem;
import sql.RankingSystemItems;

public class EquipExecution {
	public static void findGuild(PrivateMessageReceivedEvent e, List<String> guilds, final String filter) {
		var foundGuilds = guilds.parallelStream().filter(f -> f.contains(filter)).collect(Collectors.toList());
		if(foundGuilds != null) {
			if(foundGuilds.size() == 1) {
				if(UserPrivs.comparePrivilege(e.getJDA().getGuildById(foundGuilds.get(0)).getMemberById(e.getAuthor().getId()), GuildIni.getEquipLevel(Long.parseLong(foundGuilds.get(0))))) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.blue).setTitle("Equip command").setDescription("Write one of the following available options to equip a weapon and/or skill. You have 3 minutes to select an option or use 'exit' to terminate!\n\n"
							+ "**show**\n"
							+ "**set**\n"
							+ "**remove**\n"
							+ "**remove-all**").build()).queue();
					Hashes.addTempCache("equip_us"+e.getAuthor().getId(), new Cache(180000, foundGuilds.get(0)));
				}
				else {
					EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Access Denied!");
					e.getChannel().sendMessage(denied.setDescription(e.getAuthor().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(GuildIni.getEquipLevel(Long.parseLong(foundGuilds.get(0))), e.getJDA().getGuildById(foundGuilds.get(0)))).build()).queue();
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
						out.append("**"+i+": "+e.getJDA().getGuildById(guild_id)+" ("+e.getJDA().getGuildById(guild_id).getId()+") PERMISSION DENIED**\n");
					}
					if(i != foundGuilds.size())
						thisGuilds += guild_id+"-";
					else
						thisGuilds += guild_id;
					i++;
				}
				try {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription("Select the server by sending a digit of the server you wish to edit your equipment! You have 3 minutes to select a server or write 'exit' to terminate!\n\n"+out.toString()).build()).queue();
					Hashes.addTempCache("equip_us"+e.getAuthor().getId(), new Cache(180000, thisGuilds, "wait"));
				} catch(IllegalArgumentException iae) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription("I'm active in too many servers to display the server selection page. Please either write the server name or the server id to directly select the server!").build()).queue();
					Hashes.addTempCache("equip_us"+e.getAuthor().getId(), new Cache(180000, thisGuilds, "err"));
				}
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Server(s) couldn't be found. Please retry").build()).queue();
			var cache = Hashes.getTempCache("equip_us"+e.getAuthor().getId()).setExpiration(180000);
			Hashes.addTempCache("equip_us"+e.getAuthor().getId(), cache);
		}
	}
	
	public static void selectAvailableGuilds(PrivateMessageReceivedEvent e, final String guilds, final int selection) {
		var guild_ids = guilds.split("-");
		if(selection >= 0 && selection < guild_ids.length) {
			var guild_id = guild_ids[selection];
			if(UserPrivs.comparePrivilege(e.getJDA().getGuildById(guild_id).getMemberById(e.getAuthor().getId()), GuildIni.getEquipLevel(Long.parseLong(guild_id)))) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.blue).setTitle("Equip command").setDescription("Write one of the following available options to equip a weapon and/or skill. You have 3 minutes to select an option or use 'exit' to terminate!\n\n"
						+ "**show**\n"
						+ "**set**\n"
						+ "**remove**\n"
						+ "**remove-all**").build()).queue();
				Hashes.addTempCache("equip_us"+e.getAuthor().getId(), new Cache(180000, guild_id));
			}
			else {
				EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Access Denied!");
				e.getChannel().sendMessage(denied.setDescription(e.getAuthor().getAsMention() + " **My apologies young padawan. You don't have enough privileges to run this command on this server! Please select another\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(GuildIni.getEquipLevel(Long.parseLong(guild_id)), e.getJDA().getGuildById(guild_id))).build()).queue();
				var cache = Hashes.getTempCache("equip_us"+e.getAuthor().getId()).setExpiration(180000);
				Hashes.addTempCache("equip_us"+e.getAuthor().getId(), cache);
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Please select a channel with a digit between 1 and "+guild_ids.length).build()).queue();
			var cache = Hashes.getTempCache("equip_us"+e.getAuthor().getId()).setExpiration(180000);
			Hashes.addTempCache("equip_us"+e.getAuthor().getId(), cache);
		}
	}
	
	public static void equipmentItemScreen(PrivateMessageReceivedEvent e, final String guild_id, String action) {
		final long guild = guild_id.transform(v -> Long.valueOf(v));
		var guild_settings = RankingSystem.SQLgetGuild(guild);
		var theme_id = (guild_settings != null ? guild_settings.getThemeID() : 0);
		Rank user_details = null;
		RankingSystem.SQLDeleteInventory();
		//retrieve weapon descriptions for already equipped items or find out, if they're expired from the inventory and then remove
		var weapon1 = RankingSystemItems.SQLgetEquippedWeaponDescription(e.getAuthor().getIdLong(), guild, 1);
		var weapon2 = RankingSystemItems.SQLgetEquippedWeaponDescription(e.getAuthor().getIdLong(), guild, 2);
		var weapon3 = RankingSystemItems.SQLgetEquippedWeaponDescription(e.getAuthor().getIdLong(), guild, 3);
		var skill = RankingSystemItems.SQLgetEquippedSkillDescription(e.getAuthor().getIdLong(), guild);
		
		if(weapon1.equals("expired")) {
			if(RankingSystemItems.SQLRemoveEquippedWeapon(e.getAuthor().getIdLong(), guild, 1) == 0) {
				//To do: log error
			}
			if(user_details == null) user_details = RankingSystem.SQLgetWholeRankView(e.getAuthor().getIdLong(), guild, theme_id);
			if(user_details != null) {
				user_details.setWeapon1(0);
				Hashes.addRanking(guild+"_"+e.getAuthor().getId(), user_details);
			}
			weapon1 = "empty";
		}
		if(weapon2.equals("expired")) {
			if(RankingSystemItems.SQLRemoveEquippedWeapon(e.getAuthor().getIdLong(), guild, 2) == 0) {
				//To do: log error
			}
			if(user_details == null) user_details = RankingSystem.SQLgetWholeRankView(e.getAuthor().getIdLong(), guild, theme_id);
			if(user_details != null) {
				user_details.setWeapon2(0);
				Hashes.addRanking(guild+"_"+e.getAuthor().getId(), user_details);
			}
			weapon2 = "empty";
		}
		if(weapon3.equals("expired")) {
			if(RankingSystemItems.SQLRemoveEquippedWeapon(e.getAuthor().getIdLong(), guild, 3) == 0) {
				//To do: log error
			}
			if(user_details == null) user_details = RankingSystem.SQLgetWholeRankView(e.getAuthor().getIdLong(), guild, theme_id);
			if(user_details != null) {
				user_details.setWeapon3(0);
				Hashes.addRanking(guild+"_"+e.getAuthor().getId(), user_details);
			}
			weapon3 = "empty";
		}
		if(skill.equals("expired")) {
			if(RankingSystemItems.SQLRemoveEquippedSkill(e.getAuthor().getIdLong(), guild) == 0) {
				//To do: log error
			}
			if(user_details == null) user_details = RankingSystem.SQLgetWholeRankView(e.getAuthor().getIdLong(), guild, theme_id);
			if(user_details != null) {
				user_details.setSkill(0);
				Hashes.addRanking(guild+"_"+e.getAuthor().getId(), user_details);
			}
			skill = "empty";
		}
		
		e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription("Type a digit from 1 to 4 to select a slot and then equip a weapon or skill. Once completed, use 'exit' to close the item equipment. For every action, you have 3 minutes until it'll get terminated automatically.\nCurrent equipment:\n\n"
				+ "slot 1 : **"+weapon1+"**\n"
				+ "slot 2: **"+weapon2+"**\n"
				+ "slot 3: **"+weapon3+"**\n"
				+ "skill   : **"+skill+"**").build()).queue();
		Hashes.addTempCache("equip_us"+e.getAuthor().getId(), new Cache(180000, guild_id, action));
	}
	
	public static void removeWholeEquipment(PrivateMessageReceivedEvent e, final long guild_id) {
		var user_details = RankingSystem.SQLgetWholeRankView(e.getAuthor().getIdLong(), guild_id, RankingSystem.SQLgetGuild(guild_id).getThemeID());
		if(user_details.getWeapon1() != 0 || user_details.getWeapon2() != 0 || user_details.getWeapon3() != 0 || user_details.getSkill() != 0) {
			if(RankingSystemItems.SQLUnequipWholeEquipment(e.getAuthor().getIdLong(), guild_id) > 0) {
				user_details.setWeapon1(0);
				user_details.setWeapon2(0);
				user_details.setWeapon3(0);
				user_details.setSkill(0);
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle("Unequipment successfull").setDescription("Weapons and skill have been successfully unequipped!").build()).queue();
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Unequip error!").setDescription("An internal error occurred. Items couldn't be unequipped!").build()).queue();
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle("Equipment empty!").setDescription("There is nothing to unequip!").build()).queue();
		}
	}
	
	@SuppressWarnings("preview")
	public static void slotSelection(PrivateMessageReceivedEvent e, final String guild_id, final int selection, String action) {
		if(selection > 0 && selection <= 4) {
			if(action.equals("set")) {
				switch(selection) {
					case 1, 2, 3 -> {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle("Weapon slot "+selection+" selected!").setDescription("Now type the name of the weapon! If more options appear, select one with a digit!").build()).queue();
					}
					case 4 -> {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle("Skill slot selected!").setDescription("Now type the name of the skill! If more options appear, select one with a digit!").build()).queue();
					}
				}
				Hashes.addTempCache("equip_us"+e.getAuthor().getId(), new Cache(180000, guild_id, "set-"+selection));
			}
			else if(action.equals("remove")) {
				int itemID = 0;
				long guild = Long.parseLong(guild_id);
				var user_details = RankingSystem.SQLgetWholeRankView(e.getAuthor().getIdLong(), guild, RankingSystem.SQLgetGuild(guild).getThemeID());
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
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle("Unequip sucessfull!").setDescription("Weapon has been unequipped!").build()).queue();
								user_details.setWeapon1(0);
							}
							case 2 -> {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle("Unequip sucessfull!").setDescription("Weapon has been unequipped!").build()).queue();
								user_details.setWeapon2(0);
							}
							case 3 -> {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle("Unequip sucessfull!").setDescription("Weapon has been unequipped!").build()).queue();
								user_details.setWeapon3(0);
							}
							case 4 -> {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle("Unequip sucessfull!").setDescription("Skill has been unequipped!").build()).queue();
								user_details.setSkill(0);
							}
						}
						equipmentItemScreen(e, guild_id, "remove");
					}
					else
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Update failed!").setDescription("An internal error occurred and the item couldn't be unequipped!").build()).queue();
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle("Empty slot!").setDescription("You don't have anything equipped on this slot already!").build()).queue();
					equipmentItemScreen(e, guild_id, "remove");
				}
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Please select a slot with a digit between 1 and 4").build()).queue();
			var cache = Hashes.getTempCache("equip_us"+e.getAuthor().getId()).setExpiration(180000);
			Hashes.addTempCache("equip_us"+e.getAuthor().getId(), cache);
		}
	}
	
	@SuppressWarnings("preview")
	public static void searchInventory(PrivateMessageReceivedEvent e, final String guild_id, final int selection, final String item) {
		var guild = Long.parseLong(guild_id);
		if(selection >= 1 && selection <= 3) {
			//weapons
			var weapons = RankingSystemItems.SQLfilterInventoryWeapons(e.getAuthor().getIdLong(), guild, item);
			if(weapons.size() == 1) {
				var user_details = RankingSystem.SQLgetWholeRankView(e.getAuthor().getIdLong(), guild, RankingSystem.SQLgetGuild(guild).getThemeID());
				var weapon = weapons.get(0);
				if(weapon.getItemId() != user_details.getWeapon1() && weapon.getItemId() != user_details.getWeapon2() && weapon.getItemId() != user_details.getWeapon3()) {
					var guild_settings = RankingSystem.SQLgetGuild(guild);
					var weapon1 = RankingSystemItems.SQLgetWholeWeaponShop(guild, guild_settings.getThemeID()).parallelStream().filter(f -> f.getWeaponID() == user_details.getWeapon1()).findAny().orElse(null);
					var weapon2 = RankingSystemItems.SQLgetWholeWeaponShop(guild, guild_settings.getThemeID()).parallelStream().filter(f -> f.getWeaponID() == user_details.getWeapon2()).findAny().orElse(null);
					var weapon3 = RankingSystemItems.SQLgetWholeWeaponShop(guild, guild_settings.getThemeID()).parallelStream().filter(f -> f.getWeaponID() == user_details.getWeapon3()).findAny().orElse(null);
					if(!weapon.getAbbreviation().equals((weapon1 != null ? weapon1.getWeaponAbbv() : "")) && !weapon.getAbbreviation().equals((weapon2 != null ? weapon2.getWeaponAbbv() : "")) && !weapon.getAbbreviation().equals((weapon3 != null ? weapon3.getWeaponAbbv() : ""))) {
						if(RankingSystemItems.SQLEquipWeapon(e.getAuthor().getIdLong(), guild, weapon.getItemId(), selection) > 0) {
							switch(selection) {
								case 1 -> user_details.setWeapon1(weapon.getItemId());
								case 2 -> user_details.setWeapon2(weapon.getItemId());
								case 3 -> user_details.setWeapon3(weapon.getItemId());
							}
							Hashes.addRanking(guild_id+"_"+e.getAuthor().getId(), user_details);
							EmbedBuilder embed = new EmbedBuilder().setColor(Color.BLUE);
							e.getChannel().sendMessage(embed.setDescription("Weapon equipped!").build()).complete();
							equipmentItemScreen(e, guild_id, "set");
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Weapon couldn't be equipped. Please retry later again!").build()).queue();
							Hashes.clearTempCache("equip_us"+e.getAuthor().getId());
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("You have already equipped this weapon type! Please choose a different weapon or write 'return' to return to the slot selection!").build()).queue();
						var cache = Hashes.getTempCache("equip_us"+e.getAuthor().getId()).setExpiration(180000);
						Hashes.addTempCache("equip_us"+e.getAuthor().getId(), cache);
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("You have already equipped this weapon! Please choose a different weapon or write 'return' to return to the slot selection!").build()).queue();
					var cache = Hashes.getTempCache("equip_us"+e.getAuthor().getId()).setExpiration(180000);
					Hashes.addTempCache("equip_us"+e.getAuthor().getId(), cache);
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
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription("Now select a digit to select the weapon you mean to equip!\n\n"+sb.toString()).build()).queue();
				Hashes.addTempCache("equip_us"+e.getAuthor().getId(), new Cache(180000, guild_id, "set-"+selection+"_"+items));
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Nothing has been found. Please double check if you have this weapon in your inventory and try again or write 'return' to return to the slot selection!").build()).queue();
				var cache = Hashes.getTempCache("equip_us"+e.getAuthor().getId()).setExpiration(180000);
				Hashes.addTempCache("equip_us"+e.getAuthor().getId(), cache);
			}
		}
		else if(selection == 4) {
			//skill
			var skills = RankingSystemItems.SQLfilterInventorySkills(e.getAuthor().getIdLong(), guild, item);
			if(skills.size() == 1) {
				var user_details = RankingSystem.SQLgetWholeRankView(e.getAuthor().getIdLong(), guild, RankingSystem.SQLgetGuild(guild).getThemeID());
				var skill = skills.get(0);
				if(skill.getItemId() != user_details.getSkill()) {
					if(RankingSystemItems.SQLEquipSkill(e.getAuthor().getIdLong(), guild, skill.getItemId()) > 0) {
						user_details.setSkill(skill.getItemId());
						Hashes.addRanking(guild_id+"_"+e.getAuthor().getId(), user_details);
						EmbedBuilder embed = new EmbedBuilder().setColor(Color.BLUE);
						e.getChannel().sendMessage(embed.setDescription("Skill equipped!").build()).complete();
						equipmentItemScreen(e, guild_id, "set");
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Weapon couldn't be equipped. Please retry later again!").build()).queue();
						Hashes.clearTempCache("equip_us"+e.getAuthor().getId());
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("You have already equipped this skill! Please choose a different skill or write 'return' to return to the slot selection!").build()).queue();
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
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription("Now select a digit to select the skill you mean to equip!\n\n"+sb.toString()).build()).queue();
				Hashes.addTempCache("equip_us"+e.getAuthor().getId(), new Cache(180000, guild_id, "set-"+selection+"_"+items));
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Nothing has been found. Please double check if you have this skill in your inventory and try again or write 'return' to return to the slot selection!").build()).queue();
				var cache = Hashes.getTempCache("equip_us"+e.getAuthor().getId()).setExpiration(180000);
				Hashes.addTempCache("equip_us"+e.getAuthor().getId(), cache);
			}
		}
	}
	
	@SuppressWarnings("preview")
	public static void selectItem(PrivateMessageReceivedEvent e, final String guild_id, final int slot, final int selection, final String [] items) {
		if(selection >= 0 && selection < items.length) {
			var guild = Long.parseLong(guild_id);
			switch(slot) {
				case 1, 2, 3 -> {
					var weapon_id = Integer.parseInt(items[selection]);
					var user_details = RankingSystem.SQLgetWholeRankView(e.getAuthor().getIdLong(), guild, RankingSystem.SQLgetGuild(guild).getThemeID());
					if(weapon_id != user_details.getWeapon1() && weapon_id != user_details.getWeapon2() && weapon_id != user_details.getWeapon3()) {
						var guild_settings = RankingSystem.SQLgetGuild(guild);
						var weapon1 = RankingSystemItems.SQLgetWholeWeaponShop(guild, guild_settings.getThemeID()).parallelStream().filter(f -> f.getWeaponID() == user_details.getWeapon1()).findAny().orElse(null);
						var weapon2 = RankingSystemItems.SQLgetWholeWeaponShop(guild, guild_settings.getThemeID()).parallelStream().filter(f -> f.getWeaponID() == user_details.getWeapon2()).findAny().orElse(null);
						var weapon3 = RankingSystemItems.SQLgetWholeWeaponShop(guild, guild_settings.getThemeID()).parallelStream().filter(f -> f.getWeaponID() == user_details.getWeapon3()).findAny().orElse(null);
						var selectedWeapon = RankingSystemItems.SQLgetWholeWeaponShop(guild, guild_settings.getThemeID()).parallelStream().filter(f -> f.getWeaponID() == weapon_id).findAny().orElse(null);
						if(!selectedWeapon.getWeaponAbbv().equals((weapon1 != null ? weapon1.getWeaponAbbv() : "")) && !selectedWeapon.getWeaponAbbv().equals((weapon2 != null ? weapon2.getWeaponAbbv() : "")) && !selectedWeapon.getWeaponAbbv().equals((weapon3 != null ? weapon3.getWeaponAbbv() : ""))) {
							if(RankingSystemItems.SQLEquipWeapon(e.getAuthor().getIdLong(), guild, weapon_id, selection+1) > 0) {
								switch(selection) {
									case 0 -> user_details.setWeapon1(weapon_id);
									case 1 -> user_details.setWeapon2(weapon_id);
									case 2 -> user_details.setWeapon3(weapon_id);
								}
								Hashes.addRanking(guild_id+"_"+e.getAuthor().getId(), user_details);
								EmbedBuilder embed = new EmbedBuilder().setColor(Color.BLUE);
								e.getChannel().sendMessage(embed.setDescription("Weapon equipped!").build()).complete();
								equipmentItemScreen(e, guild_id, "set");
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Weapon couldn't be equipped. Please retry later again!").build()).queue();
								Hashes.clearTempCache("equip_us"+e.getAuthor().getId());
							}
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("You have already equipped this weapon type! Please choose a different weapon or write 'return' to return to the slot selection!").build()).queue();
							var cache = Hashes.getTempCache("equip_us"+e.getAuthor().getId()).setExpiration(180000);
							Hashes.addTempCache("equip_us"+e.getAuthor().getId(), cache);
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("You have already equipped this weapon! Please choose a different weapon or write 'return' to return to the slot selection!").build()).queue();
						var cache = Hashes.getTempCache("equip_us"+e.getAuthor().getId()).setExpiration(180000);
						Hashes.addTempCache("equip_us"+e.getAuthor().getId(), cache);
					}
				}
				case 4 -> {
					var skill_id = Integer.parseInt(items[selection]);
					var user_details = RankingSystem.SQLgetWholeRankView(e.getAuthor().getIdLong(), guild, RankingSystem.SQLgetGuild(guild).getThemeID());
					if(user_details.getSkill() != skill_id) {
						if(RankingSystemItems.SQLEquipSkill(e.getAuthor().getIdLong(), guild, skill_id) > 0) {
							user_details.setSkill(skill_id);
							Hashes.addRanking(guild_id+"_"+e.getAuthor().getId(), user_details);
							EmbedBuilder embed = new EmbedBuilder().setColor(Color.BLUE);
							e.getChannel().sendMessage(embed.setDescription("Skill equipped!").build()).complete();
							equipmentItemScreen(e, guild_id, "set");
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Skill couldn't be equipped. Please retry later again!").build()).queue();
							Hashes.clearTempCache("equip_us"+e.getAuthor().getId());
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("You have already equipped this skill! Please choose a different weapon or write 'return' to return to the slot selection!").build()).queue();
						var cache = Hashes.getTempCache("equip_us"+e.getAuthor().getId()).setExpiration(180000);
						Hashes.addTempCache("equip_us"+e.getAuthor().getId(), cache);
					}
				}
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Please select a channel with a digit between 1 and "+items.length).build()).queue();
			var cache = Hashes.getTempCache("equip_us"+e.getAuthor().getId()).setExpiration(180000);
			Hashes.addTempCache("equip_us"+e.getAuthor().getId(), cache);
		}
	}
}
