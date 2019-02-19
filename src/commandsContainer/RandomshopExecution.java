package commandsContainer;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import rankingSystem.Rank;
import rankingSystem.WeaponAbbvs;
import rankingSystem.WeaponStats;
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
			message.setDescription("Write either one weapon type or weapon category together with the command to start the random shop. For example **"+IniFileReader.getCommandPrefix()+"randomshop -play <weapon type/weapon category>**."
					+ " Also make use of the -replay parameter to replay with the same weapon type or category");
			
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
		//first, search for results from the available lists
		WeaponAbbvs weapon_abbv = abbreviations.parallelStream().filter(a -> a.getDescription().equalsIgnoreCase(input)).findAny().orElse(null);
		final String abbv;
		final String category;
		if(weapon_abbv != null) {
			abbv = weapon_abbv.getDescription();
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
			Rank user_details = RankingSystem.SQLgetWholeRankView(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong());
			if(user_details.getCurrency() >= 10000) {
				List<WeaponStats> stats = RankingSystemItems.SQLgetWeaponStats(e.getGuild().getIdLong());
				if(stats.size() > 0) {
					int rand = ThreadLocalRandom.current().nextInt(0, (stats.size()-1));
					user_details.setCurrency(user_details.getCurrency()-20000);
					var editedRows = 0;
					//get a random weapon id basing of either abbreviation or category and the random stat
					if(abbv != null) {
						var weapon_id = RankingSystemItems.SQLgetRandomWeaponIDByAbbv(e.getGuild().getIdLong(), abbv, stats.get(rand).getID());
						if(weapon_id > 0) {
							editedRows = RankingSystemItems.SQLUpdateCurrencyAndInsertWeaponRandomshop(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), user_details.getCurrency(), weapon_id);
						}
						else {
							e.getTextChannel().sendMessage("Weapons for this weapon type have not been configured. Please contact an administrator!").queue();
							logger.warn("Table weapon_shop_content is not configured for the weapon abbreviation {} in guild {}", abbv, e.getGuild().getName());
						}
					}
					else {
						var weapon_id = RankingSystemItems.SQLgetRandomWeaponIDByCategory(e.getGuild().getIdLong(), category, stats.get(rand).getID());
						if(weapon_id > 0) {
							editedRows = RankingSystemItems.SQLUpdateCurrencyAndInsertWeaponRandomshop(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), user_details.getCurrency(), weapon_id);
						}
						else {
							e.getTextChannel().sendMessage("Weapons for this weapon category have not been configured. Please contact an administrator!").queue();
							logger.warn("Table weapon_shop_content is not configured for the weapon category {} in guild {}", category, e.getGuild().getName());
						}
					}
					
					if(editedRows > 0) {
						//draw won item from the Randomshop
						
					}
					else {
						EmbedBuilder message = new EmbedBuilder().setColor(Color.RED).setTitle("Randomshop failed");
						e.getTextChannel().sendMessage(message.setDescription("An internal error occurred while receiving a weapon from the Randomshop. Please contact an administrator").build()).queue();
					}
				}
				else {
					e.getTextChannel().sendMessage("No weapon stats available. Please contact an administrator to configure them!").queue();
					logger.warn("Table weapon_stats is not configured for guild {}", e.getGuild().getName());
				}
			}
			else {
				e.getTextChannel().sendMessage("I'm sorry. you don't have enough currency to play another round. Your current currency amounts to: "+user_details.getCurrency());
			}
		}
		else {
			e.getTextChannel().sendMessage("No valid input has been passed. Randomshop interrupted!").queue();
		}
	}
}
