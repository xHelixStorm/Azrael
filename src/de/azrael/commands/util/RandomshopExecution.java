package de.azrael.commands.util;

import java.awt.Color;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Cache;
import de.azrael.constructors.Guilds;
import de.azrael.constructors.Ranking;
import de.azrael.constructors.WeaponAbbvs;
import de.azrael.constructors.WeaponStats;
import de.azrael.constructors.Weapons;
import de.azrael.enums.Translation;
import de.azrael.randomshop.RandomshopItemDrawer;
import de.azrael.randomshop.RandomshopRewardDrawer;
import de.azrael.sql.RankingSystem;
import de.azrael.sql.RankingSystemItems;
import de.azrael.util.Hashes;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class RandomshopExecution {
	private static final Logger logger = LoggerFactory.getLogger(RandomshopExecution.class);
	
	public static void runHelp(MessageReceivedEvent e, List<WeaponAbbvs> abbreviations, List<String> categories) {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
		if(abbreviations.size() == 0 && categories.size() == 0) {
			e.getChannel().sendMessageEmbeds(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.RANDOMSHOP_NOT_AVAILABLE)).build()).queue();
		}
		else {
			message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.RANDOMSHOP_HELP)
					.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_PLAY))
					.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REPLAY)));
			
			if(abbreviations.size() > 0) {
				//display all weapons
				StringBuilder out1 = new StringBuilder();
				StringBuilder out2 = new StringBuilder();
				boolean switcher = false;
				for(WeaponAbbvs abbv : abbreviations) {
					if(switcher == false) {
						switcher = true;
						out1.append(abbv.getDescription()+"\n");
					}
					else {
						switcher = false;
						out2.append(abbv.getDescription()+"\n");
					}
				}
				message.addField(STATIC.getTranslation(e.getMember(), Translation.RANDOMSHOP_WEP_TYPES), "_"+out1.toString()+"_", true);
				message.addField("", "_"+out2.toString()+"_", true);
				message.addBlankField(false);
			}
			if(categories.size() > 0) {
				//display all categories
				StringBuilder out1 = new StringBuilder();
				StringBuilder out2 = new StringBuilder();
				boolean switcher = false;
				for(String category : categories) {
					if(switcher == false) {
						switcher = true;
						out1.append(category+"\n");
					}
					else {
						switcher = false;
						out2.append(category+"\n");
					}
				}
				message.addField(STATIC.getTranslation(e.getMember(), Translation.RANDOMSHOP_WEP_CATEGORIES), "_"+out1.toString()+"_", true);
				message.addField("", "_"+out2.toString()+"_", true);
			}
			
			//print message
			e.getChannel().sendMessageEmbeds(message.build()).queue();
		}
	}
	
	public static void runRound(MessageReceivedEvent e, List<WeaponAbbvs> abbreviations, List<String> categories, String input) {
		var cache = Hashes.getTempCache("randomshop_playDelay_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
		if(cache == null || cache.getExpiration() - System.currentTimeMillis() <= 0) {
			Hashes.addTempCache("randomshop_playDelay_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId(), new Cache(3000));
			//first, search for results from the available lists
			final String abbv;
			final String category;
			WeaponAbbvs weapon_abbv = abbreviations.parallelStream().filter(a -> a.getDescription().equalsIgnoreCase(input)).findAny().orElse(null);
			if(weapon_abbv != null) {
				abbv = weapon_abbv.getAbbv();
			}
			else {
				abbv = null;
			}
			if(abbv == null) {
				category = categories.parallelStream().filter(c -> c.equalsIgnoreCase(input)).findAny().orElse(null);
			}
			else {
				category = null;
			}
			
			//second, check if anything has been found, else interrupt the process
			if(abbv != null || category != null) {
				Guilds guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
				Ranking user_details = RankingSystem.SQLgetWholeRankView(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong());
				final long price = guild_settings.getRandomshopPrice();
				if(user_details.getCurrency() >= price) {
					List<WeaponStats> stats = RankingSystemItems.SQLgetWeaponStats();
					if(stats.size() > 0) {
						final int rand = ThreadLocalRandom.current().nextInt(0, stats.size());
						user_details.setCurrency(user_details.getCurrency()-price);
						long extend = 0;
						var weapon_id = 0;
						var editedRows = 0;
						//get a random weapon id basing of either abbreviation or category and the random stat
						var number = 0;
						if(abbv != null) {
							weapon_id = RankingSystemItems.SQLgetRandomWeaponIDByAbbv(abbv, stats.get(rand).getID(), e.getGuild().getIdLong());
							number = RankingSystemItems.SQLgetNumberOfWeaponID(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), weapon_id);
						}
						else {
							weapon_id = RankingSystemItems.SQLgetRandomWeaponIDByCategory(e.getGuild().getIdLong(), category, stats.get(rand).getID());
							number = RankingSystemItems.SQLgetNumberOfWeaponID(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), weapon_id);
						}
						if(number == -1) {
							e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("It couldn't be verified if weapon {} already exists in inventory of user {} in guild {}", weapon_id, e.getMember().getUser().getId(), e.getGuild().getId());
							return;
						}
						if(weapon_id > 0) {
							final var weapons = RankingSystemItems.SQLgetWholeWeaponShop(e.getGuild().getIdLong());
							if(weapons != null) {
								final var selWeapon = weapon_id;
								//since the first query to retrieve the weapon id worked, we will assume that the weapon has been found
								final var weapon = weapons.parallelStream().filter(w -> w.getWeaponID() == selWeapon).findAny().orElse(null);
								//calculate if weapon will be timed or permanent
								switch(weapon.getOverlayLevel()) {
									case 1, 2 -> {
										extend = randomizeWeaponDuration(false);
									}
									case 3 -> {
										extend = randomizeWeaponDuration(true);
									}
								}
								if(extend == 0) {
									editedRows = RankingSystemItems.SQLUpdateCurrencyAndInsertWeaponRandomshop(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), user_details.getCurrency(), weapon_id, new Timestamp(System.currentTimeMillis()), (number+1));
								}
								else {
									final var inventoryItem = RankingSystemItems.SQLgetNumberAndExpirationFromInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), weapon_id, "limit", true);
									if(inventoryItem != null) {
										editedRows = RankingSystemItems.SQLUpdateCurrencyAndInsertTimedInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), user_details.getCurrency(), weapon_id, System.currentTimeMillis(), inventoryItem.getExpiration().getTime(), (inventoryItem.getNumber()+1), true, extend);
									}
									else {
										final long time = System.currentTimeMillis();
										editedRows = RankingSystemItems.SQLUpdateCurrencyAndInsertTimedInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), user_details.getCurrency(), weapon_id, time, time, 1, true, extend);
									}
								}
							}
							else {
								e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							}
						}
						else if (weapon_id == 0) {
							e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.RANDOMSHOP_WEP_TYPE_NA)).build()).queue();
							logger.error("Available weapons couldn't be retrieved in guild {}", e.getGuild().getId());
							Hashes.clearTempCache("randomshop_play_"+e.getMember().getUser().getId());
						}
						else {
							e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							if(abbv != null)
								logger.error("Random weapon couldn't be retrieved by abbreviation abbreviation {} in guild {}", abbv, e.getGuild().getId());
							else
								logger.error("Random weapon couldn't be retrieved by category {} in guild {}", category, e.getGuild().getId());
							Hashes.clearTempCache("randomshop_play_"+e.getMember().getUser().getId());
						}
						
						if(editedRows > 0) {
							//draw won item from the Randomshop
							final int weapon = weapon_id;
							RandomshopRewardDrawer.drawReward(e, RankingSystemItems.SQLgetWholeWeaponShop(e.getGuild().getIdLong()).parallelStream().filter(w -> w.getWeaponID() == weapon).findAny().orElse(null), user_details.getCurrency(), guild_settings, extend);
							Hashes.addTempCache("randomshop_play_"+e.getMember().getUser().getId(), new Cache(600000, input));
						}
						else if(weapon_id > 0) {
							EmbedBuilder message = new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
							e.getChannel().sendMessageEmbeds(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("The user {} couldn't receive the weapon with the weapon_id {} in guild {}", e.getMember().getUser().getId(), weapon_id, e.getGuild().getId());
						}
					}
					else {
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Weapon stats couldn't be retrieved in guild {}", e.getGuild().getId());
					}
				}
				else {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.RANDOMSHOP_BALANCE_ERR)+user_details.getCurrency()).build()).queue();
				}
			}
			else {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
			}
		}
	}
	
	public static void inspectItems(Member member, TextChannel channel, List<WeaponAbbvs> abbreviations, List<String> categories, String input, int page, boolean overrideDelay) {
		var cache = Hashes.getTempCache("randomshop_playDelay_gu"+member.getGuild().getId()+"us"+member.getUser().getId());
		if(cache == null || cache.getExpiration() - System.currentTimeMillis() <= 0 || overrideDelay) {
			Hashes.addTempCache("randomshop_playDelay_gu"+member.getGuild().getId()+"us"+member.getUser().getId(), new Cache(3000));
			final String abbv;
			final String category;
			WeaponAbbvs weapon_abbv = abbreviations.parallelStream().filter(a -> a.getDescription().equalsIgnoreCase(input)).findAny().orElse(null);
			if(weapon_abbv != null) {
				abbv = weapon_abbv.getAbbv();
			}
			else {
				abbv = null;
			}
			if(abbv == null) {
				category = categories.parallelStream().filter(c -> c.equalsIgnoreCase(input)).findAny().orElse(null);
			}
			else {
				category = null;
			}
			
			if(abbv != null || category != null) {
				final var guild_settings = RankingSystem.SQLgetGuild(member.getGuild().getIdLong());
				if(guild_settings != null) {
					List<Weapons> weapons;
					if(abbv != null) {
						weapons = RankingSystemItems.SQLgetWholeWeaponShop(member.getGuild().getIdLong());
						if(weapons == null) {
							channel.sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(member, Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(member, Translation.GENERAL_ERROR)).build()).queue();
							logger.error("Weapons couldn't be retrieved in guild {}", member.getGuild().getId());
							Hashes.clearTempCache("randomshop_bot_gu"+member.getGuild().getId()+"ch"+channel.getId());
							return;
						}
						weapons = weapons.parallelStream().filter(w -> w.getWeaponAbbv().equalsIgnoreCase(abbv) && w.getStat() == 1).collect(Collectors.toList());
					}
					else {
						weapons = RankingSystemItems.SQLgetWholeWeaponShop(member.getGuild().getIdLong());
						if(weapons == null) {
							channel.sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(member, Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(member, Translation.GENERAL_ERROR)).build()).queue();
							logger.error("Weapons couldn't be retrieved in guild {}", member.getGuild().getId());
							Hashes.clearTempCache("randomshop_bot_gu"+member.getGuild().getId()+"ch"+channel.getId());
							return;
						}
						weapons = weapons.parallelStream().filter(w -> w.getCategoryDescription().equalsIgnoreCase(category) && w.getStat() == 1).collect(Collectors.toList());
					}
					
					var maxItems = guild_settings.getRandomshopMaxItems();
					ArrayList<Weapons> filteredWeapons = new ArrayList<Weapons>();
					final var lastPage = (page*maxItems)-1;
					for(var i = (page-1)*maxItems; i <= lastPage; i++) {
						if(i < weapons.size() && weapons.get(i) != null)
							filteredWeapons.add(weapons.get(i));
						else
							break;
					}
					
					if(filteredWeapons != null && filteredWeapons.size() > 0) {
						final int last_page = ((weapons.size()-1)/maxItems)+1; //using modulo for the page size
						//draw page
						Hashes.addTempCache("randomshop_bot_gu"+member.getGuild().getId()+"ch"+channel.getId(), new Cache(180000, member.getUser().getId()+"_"+page+"_"+input+"_"+last_page));
						RandomshopItemDrawer.drawItems(member, channel, filteredWeapons, page, last_page, guild_settings);
					}
					else {
						//no items to display
						channel.sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(member, Translation.RANDOMSHOP_NO_ITEMS)).build()).queue();
						logger.warn("Randomshop content couldn't be displayed in guild {}", member.getGuild().getId());
					}
				}
				else {
					channel.sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(member, Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(member, Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Guild ranking information couldn't be retrieved in guild {}", member.getGuild().getId());
				}
			}
			else {
				channel.sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(member, Translation.PARAM_NOT_FOUND)).build()).queue();
			}
		}
	}
	
	private static long randomizeWeaponDuration(final boolean highRank) {
		final int rand = ThreadLocalRandom.current().nextInt(0, 100);
		if(!highRank) {
			if(rand <= 28) {
				return TimeUnit.DAYS.toMillis(1);
			}
			else if(rand > 28 && rand <= 54) {
				return TimeUnit.DAYS.toMillis(7);
			}
			else if(rand > 54 && rand <= 80) {
				return TimeUnit.DAYS.toMillis(30);
			}
			else {
				return 0L;
			}
		}
		else {
			if(rand <= 50) {
				return TimeUnit.DAYS.toMillis(30);
			}
			else {
				return 0L;
			}
		}
	}
}
