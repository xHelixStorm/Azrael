package de.azrael.commandsContainer;

import java.awt.Color;
import java.net.MalformedURLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

import org.jpastebin.pastebin.exceptions.LoginException;
import org.jpastebin.pastebin.exceptions.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.enums.Translation;
import de.azrael.sql.Azrael;
import de.azrael.sql.RankingSystem;
import de.azrael.util.Pastebin;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * Extension of the set command
 * @author xHelixStorm
 *
 */

public class SetGiveawayItems {
	private final static Logger logger = LoggerFactory.getLogger(SetGiveawayItems.class);
	
	public static void runTask(GuildMessageReceivedEvent e, String [] args) {
		if(args.length == 3) {
			switch(args[1]) {
				case "1" -> {
					final var guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
					if(guild_settings != null && guild_settings.getRankingState()) {
						//verify if a parameter has been passed
						if(args[2].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_EXTEND))) {
							//calculate the next beginning of the month
							Timestamp timestamp = calculateMonth();
							final int result = RankingSystem.SQLUpdateRewardExpiration(e.getGuild().getIdLong(), timestamp);
							if(result > 0) {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_GIVEAWAY_EXTENDED)).build()).queue();
								logger.info("User {} has extended the expiration of the currently registered giveaway rewards until the end of this month in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
							}
							else if(result == 0) {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_GIVEAWAY_EXTEND_ERR)).build()).queue();
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
								logger.error("Giveaway rewards expiration couldn't be extended until the end of the current month in guild {}", e.getGuild().getId());
							}
						}
						//verify pastebin link and save the content into array
						else if(args[2].matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && args[2].startsWith("http")) {
							try {
								String [] rewards = Pastebin.readPasteLink(args[2]).split("[\\r\\n]+");
								//calculate the next beginning of the month
								Timestamp timestamp = calculateMonth();
								
								//insert rewards into table and return error with true or false
								boolean err = RankingSystem.SQLBulkInsertGiveawayRewards(rewards, timestamp, e.getGuild().getIdLong());
								
								if(err == false) {
									logger.info("User {} has used the pastebin url {} to save giveaway rewards in guild {}", e.getMember().getUser().getId(), args[2], e.getGuild().getId());
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_GIVEAWAY_ADDED)).build()).queue();
								}
								else {
									EmbedBuilder error = new EmbedBuilder().setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED);
									e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
									logger.error("Giveaway rewards couldn't be saved with the pastebin url {} in guild {}", args[2], e.getGuild().getId());
								}
							} catch (MalformedURLException | RuntimeException | LoginException | ParseException e1) {
								EmbedBuilder error = new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PASTE_READ_ERR));
								e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR_2)).build()).queue();
								logger.error("Reading pastebin url {} failed in guild {}", args[2], e.getGuild().getId(), e1);
							}
						}
						else {
							EmbedBuilder error = new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_NOT_PASTE));
							e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.LEVEL_SYSTEM_NOT_ENABLED)).build()).queue();
					}
				}
				case "2" -> {
					if(args[2].matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && args[2].startsWith("http")) {
						try {
							String [] rewards = Pastebin.readPasteLink(args[2]).split("[\\r\\n]+");
							boolean success = Azrael.SQLInsertGiveawayRewards(e.getGuild().getIdLong(), rewards);
							if(success) {
								logger.info("User {} has used the pastebin url {} to save giveaway rewards in guild {}", e.getMember().getUser().getId(), args[2], e.getGuild().getId());
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_GIVEAWAY_ADDED)).build()).queue();
							}
							else {
								EmbedBuilder error = new EmbedBuilder().setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED);
								e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
								logger.error("Giveaway rewards couldn't be saved with the pastebin url {} in guild {}", args[2], e.getGuild().getId());
							}
						} catch (MalformedURLException | RuntimeException | LoginException | ParseException e1) {
							EmbedBuilder error = new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PASTE_READ_ERR));
							e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR_2)).build()).queue();
							logger.error("Reading pastebin url {} failed in guild {}", args[2], e.getGuild().getId(), e1);
						}
					}
					else {
						EmbedBuilder error = new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_NOT_PASTE));
						e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
					}
				}
				default -> {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
				}
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
		}
	}
	
	private static Timestamp calculateMonth() {
		//calculate the next beginning of the month
		LocalTime midnight = LocalTime.MIDNIGHT;
		LocalDate today = LocalDate.now();
		LocalDate beginningOfMonth = today.with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);
		LocalDateTime beginningOfMonthMidnight = LocalDateTime.of(beginningOfMonth, midnight);
		return Timestamp.valueOf(beginningOfMonthMidnight);
	}
}
