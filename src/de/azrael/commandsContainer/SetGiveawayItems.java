package de.azrael.commandsContainer;

import java.awt.Color;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.enums.Directory;
import de.azrael.enums.Translation;
import de.azrael.sql.Azrael;
import de.azrael.sql.RankingSystem;
import de.azrael.util.FileHandler;
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
		if((args.length == 2 || args.length == 3)) {
			final var attachments = e.getMessage().getAttachments();
			boolean attachmentFound = false;
			String fileName = "";
			//check if an attachment has been added
			if(attachments.size() == 1 && (attachments.get(0).getFileExtension() == null || attachments.get(0).getFileExtension().contains("txt"))) {
				fileName = e.getGuild().getId()+attachments.get(0).getFileName();
				attachments.get(0).downloadToFile(Directory.TEMP.getPath()+fileName);
				attachmentFound = true;
			}
			switch(args[1]) {
				case "1" -> {
					final var guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
					if(guild_settings != null && guild_settings.getRankingState()) {
						//verify if a parameter has been passed
						if(args.length == 3 && args[2].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_EXTEND))) {
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
						//verify if giveaway rewards should be cleared
						else if(args.length == 3 && args[2].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_CLEAR))) {
							final int result = RankingSystem.SQLDeleteGiveawayRewards(e.getGuild().getIdLong());
							if(result >= 0) {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_GIVEAWAY_CLEARED)).build()).queue();
							}
							else {
								EmbedBuilder error = new EmbedBuilder().setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED);
								e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
								logger.error("Giveaway rewards couldn't be cleared in guild {}", e.getGuild().getId());
							}
						}
						//verify file and save the content into array
						else if(attachmentFound) {
							String [] rewards = FileHandler.readFile(Directory.TEMP, fileName).split("[\\r\\n]");
							//calculate the next beginning of the month
							Timestamp timestamp = calculateMonth();
							
							//validate giveaway rewards to not exceed table column size
							for(final String reward : rewards) {
								if(reward.length() > 50 || reward.isBlank()) {
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TXT_FILE_INVALID)).build()).queue();
									return;
								}
							}
							
							//insert rewards into table and return error with true or false
							boolean err = RankingSystem.SQLBulkInsertGiveawayRewards(rewards, timestamp, e.getGuild().getIdLong());
							
							if(err == false) {
								logger.info("User {} has registered giveaway rewards from text file in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_GIVEAWAY_ADDED)).build()).queue();
							}
							else {
								EmbedBuilder error = new EmbedBuilder().setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED);
								e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
								logger.error("Giveaway rewards couldn't be registered with the text file in guild {}", e.getGuild().getId());
							}
						}
						else if(args.length > 3) {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TXT_FILE_NOT_FOUND)).build()).queue();
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.LEVEL_SYSTEM_NOT_ENABLED)).build()).queue();
					}
				}
				case "2" -> {
					//verify if giveaway rewards should be cleared
					if(args.length == 3 && args[2].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_CLEAR))) {
						final int result = Azrael.SQLDeleteGiveawayRewards(e.getGuild().getIdLong());
						if(result >= 0) {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_GIVEAWAY_CLEARED)).build()).queue();
						}
						else {
							EmbedBuilder error = new EmbedBuilder().setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED);
							e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("Giveaway rewards couldn't be cleared in guild {}", e.getGuild().getId());
						}
					}
					//verify if an attachment has been submitted
					else if(attachmentFound) {
						String [] rewards = FileHandler.readFile(Directory.TEMP, fileName).split("[\\r\\n]");
						
						//validate giveaway rewards to not exceed table column size
						for(final String reward : rewards) {
							if(reward.length() > 50 || reward.isBlank()) {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TXT_FILE_INVALID)).build()).queue();
								return;
							}
						}
						
						boolean success = Azrael.SQLInsertGiveawayRewards(e.getGuild().getIdLong(), rewards);
						if(success) {
							logger.info("User {} has registered giveaway rewards from text file in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_GIVEAWAY_ADDED)).build()).queue();
						}
						else {
							EmbedBuilder error = new EmbedBuilder().setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setColor(Color.RED);
							e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("Giveaway rewards couldn't be registered with a text file in guild {}", e.getGuild().getId());
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TXT_FILE_NOT_FOUND)).build()).queue();
					}
				}
				default -> {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
				}
			}
			FileHandler.deleteFile(Directory.TEMP, fileName);
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
