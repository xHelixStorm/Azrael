package commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import constructors.Guilds;
import core.Hashes;
import core.UserPrivs;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import rankingSystem.RankingMethods;
import sql.RankingSystem;

public class Rank implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Rank.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		if(GuildIni.getRankCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getRankLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(() -> {
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
			var rank = 0;
			
			Guilds guild_settings = RankingSystem.SQLgetGuild(guild_id);
			constructors.Rank user_details = RankingSystem.SQLgetWholeRankView(user_id, guild_id, guild_settings.getThemeID());
			
			if(guild_settings.getRankingState()) {
				var cache = Hashes.getTempCache("rankDelay_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
				if(cache == null || cache.getExpiration() - System.currentTimeMillis() <= 0) {
					Hashes.addTempCache("rankDelay_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId(), new Cache(30000));
					
					float experienceCounter;
					int convertedExperience;
					
					final String name = e.getGuild().getMemberById(user_id).getEffectiveName();
					final String avatar = e.getGuild().getMemberById(user_id).getUser().getEffectiveAvatarUrl();
					final int level = user_details.getLevel();
					float currentExperience = user_details.getCurrentExperience();
					float rankUpExperience = user_details.getRankUpExperience();
					final int max_level = guild_settings.getMaxLevel();
					final int rank_skin = user_details.getRankingRank();
					final int icon_skin = user_details.getRankingIcon();
					final int bar_color = user_details.getBarColorRank();
					final boolean additional_exp_text = user_details.getAdditionalExpTextRank();
					final boolean additional_percent_text = user_details.getAdditionalPercentTextRank();
					final int color_r = user_details.getColorRRank();
					final int color_g = user_details.getColorGRank();
					final int color_b = user_details.getColorBRank();
					final int rankx = user_details.getRankXRank();
					final int ranky = user_details.getRankYRank();
					final int rank_width = user_details.getRankWidthRank();
					final int rank_height = user_details.getRankHeightRank();
					
					if(rank_skin != 0 && icon_skin != 0) {
						if(level == max_level){currentExperience = 999999; rankUpExperience = 999999;}
						
						experienceCounter = (currentExperience / rankUpExperience)*100;
						convertedExperience = (int) experienceCounter;
						if(convertedExperience > 100) {
							convertedExperience = 100;
						}
						
						ArrayList<constructors.Rank> rankList = RankingSystem.SQLRanking(guild_id);
						if(rankList.size() > 0) {
							search: for(constructors.Rank ranking : rankList){
								if(user_id == ranking.getUser_ID()){
									rank = ranking.getRank();
									break search;
								}
							}
						}
						
						if(currentExperience >= 0) {
							RankingMethods.getRank(e, name, avatar, convertedExperience, level, rank, rank_skin, icon_skin, bar_color, additional_exp_text, additional_percent_text, color_r, color_g, color_b, rankx, ranky, rank_width, rank_height, guild_settings.getThemeID());
						}
						else {
							EmbedBuilder error = new EmbedBuilder().setColor(Color.RED).setTitle("An error occured!");
							e.getChannel().sendMessage(error.setDescription("An error occured on use. Please contact an administrator or moderator!").build()).queue();
							RankingSystem.SQLInsertActionLog("critical", user_id, guild_id, "negative experience value", "The user has less experience points in proportion to his level: "+currentExperience);
							logger.error("Negative experience valur for {} in guild {}", user_id, e.getGuild().getName());
						}
					}
					else {
						EmbedBuilder error = new EmbedBuilder().setColor(Color.RED).setTitle("An error occured!");
						e.getChannel().sendMessage(error.setDescription("Default skins aren't defined. Please contact an administrator!").build()).queue();
						logger.error("Default skins in RankingSystem.guilds are not defined for guild {}", e.getGuild().getName());
					}
				}
				else{
					e.getChannel().sendMessage("This command is currently having a cooldown, please try again later").queue();
				}
			}
			else{
				e.getChannel().sendMessage("**The ranking system is disabled. Please contact an administrator to enable the feature!**").queue();
			}
		});
		executor.shutdown();
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("{} has used Rank command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}
}
