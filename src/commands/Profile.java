package commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Cache;
import core.Guilds;
import core.Hashes;
import fileManagement.GuildIni;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import rankingSystem.RankingMethods;
import sql.RankingSystem;

public class Profile implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(GuildIni.getProfileCommand(e.getGuild().getIdLong())){
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.execute(() -> {
				Logger logger = LoggerFactory.getLogger(Profile.class);
				logger.debug("{} has used Profile command", e.getMember().getUser().getId());
				
				long user_id = 0;
				if(args.length > 0) {
					String id = args[0];
					try {
						id = id.replaceAll("[^0-9]", "");
						user_id = id.length() > 0 ? Long.parseLong(id) : 0;
						e.getGuild().getMemberById(user_id).getUser();
					} catch(Exception exc) {
						user_id = e.getMember().getUser().getIdLong();
					}
				}
				else {
					user_id = e.getMember().getUser().getIdLong();
				}
				
				long guild_id = e.getGuild().getIdLong();
				int rank = 0;
				
				Guilds guild_settings = RankingSystem.SQLgetGuild(guild_id);
				rankingSystem.Rank user_details = RankingSystem.SQLgetWholeRankView(user_id, guild_id, guild_settings.getThemeID());
				
				if(guild_settings.getRankingState()){				
					var cache = Hashes.getTempCache("profileDelay_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
					if(cache == null || cache.getExpiration() - System.currentTimeMillis() <= 0) {
						Hashes.addTempCache("profileDelay_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId(), new Cache(30000));
						
						float experienceCounter;
						int convertedExperience;
						
						String name = e.getGuild().getMemberById(user_id).getEffectiveName();
						String avatar = e.getGuild().getMemberById(user_id).getUser().getEffectiveAvatarUrl();
						final int level = user_details.getLevel();
						float currentExperience = user_details.getCurrentExperience();
						float rankUpExperience = user_details.getRankUpExperience();
						final long experience = user_details.getExperience();
						final long currency = user_details.getCurrency();
						final int max_level = guild_settings.getMaxLevel();
						final int profile_skin = user_details.getRankingProfile();
						final int icon_skin = user_details.getRankingIcon();
						final int bar_color = user_details.getBarColorProfile();
						final boolean additional_exp_text = user_details.getAdditionalExpTextProfile();
						final boolean additional_percent_text = user_details.getAdditionalPercentTextProfile();
						final int color_r = user_details.getColorRProfile();
						final int color_g = user_details.getColorGProfile();
						final int color_b = user_details.getColorBProfile();
						final int rankx = user_details.getRankXProfile();
						final int ranky = user_details.getRankYProfile();
						final int rank_width = user_details.getRankWidthProfile();
						final int rank_height = user_details.getRankHeightProfile();
						
						if(profile_skin != 0 && icon_skin != 0) {
							if(level == max_level){currentExperience = 999999; rankUpExperience = 999999;}
							
							experienceCounter = (currentExperience / rankUpExperience)*100;
							convertedExperience = (int) experienceCounter;
							if(convertedExperience > 100) {
								convertedExperience = 100;
							}
							
							ArrayList<rankingSystem.Rank> rankList = RankingSystem.SQLRanking(guild_id);
							if(rankList.size() > 0) {
								search: for(rankingSystem.Rank ranking : rankList){
									if(user_id == ranking.getUser_ID()){
										rank = ranking.getRank();
										break search;
									}
								}
							}
							if(currentExperience >= 0) {
								RankingMethods.getProfile(e, name, avatar, convertedExperience, level, currentExperience, rankUpExperience, experience, currency, rank, profile_skin, icon_skin, bar_color, additional_exp_text, additional_percent_text, color_r, color_g, color_b, rankx, ranky, rank_width, rank_height);
							}
							else {
								EmbedBuilder error = new EmbedBuilder().setColor(Color.RED).setTitle("An error occured!");
								e.getTextChannel().sendMessage(error.setDescription("An error occured on use. Please contact an administrator or moderator!").build()).queue();
								RankingSystem.SQLInsertActionLog("critical", user_id, guild_id, "negative experience value", "The user has less experience points in proportion to his level: "+currentExperience);
								logger.error("Negative experience valur for {} in guild {}", user_id, e.getGuild().getName());
							}
						}
						else {
							EmbedBuilder error = new EmbedBuilder().setColor(Color.RED).setTitle("An error occured!");
							e.getTextChannel().sendMessage(error.setDescription("Default skins aren't defined. Please contact an administrator!").build()).queue();
							logger.error("Default skins in RankingSystem.guilds are not defined for guild {}", e.getGuild().getName());
						}
					}
					else{
						e.getTextChannel().sendMessage("This command is currently having a cooldown, please try again later").queue();
					}
				}
				else {
					e.getTextChannel().sendMessage("**The ranking system is disabled. Please contact an administrator to enable the feature!**").queue();
				}
			});
			executor.shutdown();
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
	
	}

	@Override
	public String help() {
		return null;
	}

}
