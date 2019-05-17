package commandsContainer;

import java.awt.Color;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Cache;
import core.Guilds;
import core.Hashes;
import fileManagement.GuildIni;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import randomshop.RandomshopItemDrawer;
import randomshop.RandomshopRewardDrawer;
import rankingSystem.Rank;
import rankingSystem.WeaponAbbvs;
import rankingSystem.WeaponStats;
import rankingSystem.Weapons;
import sql.RankingSystem;
import sql.RankingSystemItems;

public class RandomshopExecution {
	private static final Logger logger = LoggerFactory.getLogger(RandomshopExecution.class);
	
	public static void runHelp(MessageReceivedEvent e, List<WeaponAbbvs> abbreviations, List<String> categories) {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE).setTitle("Randomshop exclusives");
		if(abbreviations.size() == 0 && categories.size() == 0) {
			e.getTextChannel().sendMessage(message.setDescription("The randomshop is currently not available. Please retry later!").build()).queue();
		}
		else {
			message.setDescription("Write either one weapon type or weapon category together with the command to start the random shop. For example **"+GuildIni.getCommandPrefix(e.getGuild().getIdLong())+"randomshop -play <weapon type/weapon category>**."
					+ " Also make use of the -replay parameter to replay with the same weapon type or category. If you write a weapon category or weapon type without any parameters, you can view the selected content");
			
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
				message.addField("Weapon Types", "_"+out1.toString()+"_", true);
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
				message.addField("Weapon Categories", "_"+out1.toString()+"_", true);
				message.addField("", "_"+out2.toString()+"_", true);
			}
			
			//print message
			e.getTextChannel().sendMessage(message.build()).queue();
		}
	}
	
	public static void runRound(MessageReceivedEvent e, List<WeaponAbbvs> abbreviations, List<String> categories, String input) {
		var cache = Hashes.getTempCache("randomshop_playDelay_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
		if(cache == null || cache.getExpiration() - System.currentTimeMillis() <= 0) {
			Hashes.addTempCache("randomshop_playDelay_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId(), new Cache(3000));
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.execute(() -> {
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
					Rank user_details = RankingSystem.SQLgetWholeRankView(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), guild_settings.getThemeID());
					final long price = guild_settings.getRandomshopPrice();
					if(user_details.getCurrency() >= price) {
						List<WeaponStats> stats = RankingSystemItems.SQLgetWeaponStats(e.getGuild().getIdLong(), guild_settings.getThemeID());
						if(stats.size() > 0) {
							final int rand = ThreadLocalRandom.current().nextInt(0, (stats.size()-1));
							user_details.setCurrency(user_details.getCurrency()-price);
							var weapon_id = 0;
							var editedRows = 0;
							//get a random weapon id basing of either abbreviation or category and the random stat
							if(abbv != null) {
								weapon_id = RankingSystemItems.SQLgetRandomWeaponIDByAbbv(e.getGuild().getIdLong(), abbv, stats.get(rand).getID(), guild_settings.getThemeID());
								final var number = RankingSystemItems.SQLgetNumberOfWeaponID(e.getGuild().getIdLong(), weapon_id, guild_settings.getThemeID());
								if(weapon_id > 0) {
									editedRows = RankingSystemItems.SQLUpdateCurrencyAndInsertWeaponRandomshop(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), user_details.getCurrency(), weapon_id, new Timestamp(System.currentTimeMillis()), (number+1), guild_settings.getThemeID());
								}
								else {
									e.getTextChannel().sendMessage("Weapons for this weapon type have not been configured. Please contact an administrator!").queue();
									logger.warn("Table weapon_shop_content is not configured for the weapon abbreviation {} in guild {}", abbv, e.getGuild().getName());
								}
							}
							else {
								weapon_id = RankingSystemItems.SQLgetRandomWeaponIDByCategory(e.getGuild().getIdLong(), category, stats.get(rand).getID(), guild_settings.getThemeID());
								final var number = RankingSystemItems.SQLgetNumberOfWeaponID(e.getGuild().getIdLong(), weapon_id, guild_settings.getThemeID());
								if(weapon_id > 0) {
									editedRows = RankingSystemItems.SQLUpdateCurrencyAndInsertWeaponRandomshop(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), user_details.getCurrency(), weapon_id, new Timestamp(System.currentTimeMillis()), (number+1), guild_settings.getThemeID());
								}
								else {
									e.getTextChannel().sendMessage("Weapons for this weapon category have not been configured. Please contact an administrator!").queue();
									logger.warn("Table weapon_shop_content is not configured for the weapon category {} in guild {}", category, e.getGuild().getName());
								}
							}
							
							if(editedRows > 0) {
								//draw won item from the Randomshop
								final int weapon = weapon_id;
								RandomshopRewardDrawer.drawReward(e, RankingSystemItems.SQLgetWholeWeaponShop(e.getGuild().getIdLong(), guild_settings.getThemeID()).parallelStream().filter(w -> w.getWeaponID() == weapon).findAny().orElse(null), user_details.getCurrency());
								Hashes.addTempCache("randomshop_play_"+e.getMember().getUser().getId(), new Cache(180000, input));
							}
							else if(weapon_id > 0){
								EmbedBuilder message = new EmbedBuilder().setColor(Color.RED).setTitle("Randomshop failed");
								e.getTextChannel().sendMessage(message.setDescription("An internal error occurred while receiving a weapon from the Randomshop. Please contact an administrator").build()).queue();
								logger.error("The user {} couldn't receive the weapon with the weapon_id {} in guild {}", e.getMember().getUser().getId(), weapon_id, e.getGuild().getName());
							}
						}
						else {
							e.getTextChannel().sendMessage("No weapon stats available. Please contact an administrator to configure them!").queue();
							logger.warn("Table weapon_stats is not configured for guild {}", e.getGuild().getName());
						}
					}
					else {
						e.getTextChannel().sendMessage("I'm sorry. you don't have enough currency to play another round. Your current currency amounts to: "+user_details.getCurrency()).queue();
					}
				}
				else {
					e.getTextChannel().sendMessage("No valid input has been passed. Randomshop interrupted!").queue();
				}
			});
		}
	}
	
	public static void inspectItems(MessageReceivedEvent e, GuildMessageReactionAddEvent e2, List<WeaponAbbvs> abbreviations, List<String> categories, String input, int page) {
		var cache = Hashes.getTempCache("randomshop_playDelay_gu"+(e != null ? e.getGuild().getId() : e2.getGuild().getId())+"us"+(e != null ? e.getMember().getUser().getId() : e2.getMember().getUser().getId()));
		if(cache == null || cache.getExpiration() - System.currentTimeMillis() <= 0 || e2 != null) {
			Hashes.addTempCache("randomshop_playDelay_gu"+(e != null ? e.getGuild().getId() : e2.getGuild().getId())+"us"+(e != null ? e.getMember().getUser().getId() : e2.getMember().getUser().getId()), new Cache(3000));
			
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.execute(() -> {
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
					List<Weapons> weapons;
					if(abbv != null) {
						weapons = RankingSystemItems.SQLgetWholeWeaponShop((e != null ? e.getGuild().getIdLong() : e2.getGuild().getIdLong()), RankingSystem.SQLgetGuild((e != null ? e.getGuild().getIdLong() : e2.getGuild().getIdLong())).getThemeID()).parallelStream().filter(w -> w.getWeaponAbbv().equalsIgnoreCase(abbv) && w.getStat() == 1).collect(Collectors.toList());
					}
					else {
						weapons = RankingSystemItems.SQLgetWholeWeaponShop((e != null ? e.getGuild().getIdLong() : e2.getGuild().getIdLong()), RankingSystem.SQLgetGuild((e != null ? e.getGuild().getIdLong() : e2.getGuild().getIdLong())).getThemeID()).parallelStream().filter(w -> w.getCategoryDescription().equalsIgnoreCase(category) && w.getStat() == 1).collect(Collectors.toList());
					}
					
					var maxItems = GuildIni.getRandomshopItemsMaxItems((e != null ? e.getGuild().getIdLong() : e2.getGuild().getIdLong()));
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
						if(e != null) {
							Hashes.addTempCache("randomshop_bot_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId(), new Cache(180000, e.getMember().getUser().getId()+"_"+page+"_"+input+"_"+last_page));
							RandomshopItemDrawer.drawItems(e, null, filteredWeapons, page, last_page);
						}
						else {
							Hashes.addTempCache("randomshop_bot_gu"+e2.getGuild().getId()+"ch"+e2.getChannel().getId(), new Cache(180000, e2.getMember().getUser().getId()+"_"+page+"_"+input+"_"+last_page));
							RandomshopItemDrawer.drawItems(null, e2, filteredWeapons, page, last_page);
						}
					}
					else {
						//no items to display
						if(e != null)
							e.getTextChannel().sendMessage("No items to display could be retrieved!").queue();
						else
							e2.getChannel().sendMessage("No items to display could be retrieved!").queue();
						logger.warn("Randomshop content couldn't be displayed in guild {}", e.getGuild().getName());
					}
				}
				else {
					e.getTextChannel().sendMessage("No valid input has been passed. Randomshop interrupted!").queue();
				}
			});
		}
	}
}
