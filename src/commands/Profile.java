package commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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
import sql.Azrael;
import sql.RankingSystem;
import util.STATIC;

public class Profile implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Profile.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		if(GuildIni.getProfileCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getProfileLevel(e.getGuild().getIdLong());
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
			var bot_channels = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("bot")).collect(Collectors.toList());
			var this_channel = bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null);
			
			if(this_channel == null && bot_channels.size() > 0) {
				e.getChannel().sendMessage(e.getMember().getAsMention()+" I'm not allowed to execute commands in this channel, please write it again in "+STATIC.getChannels(bot_channels)).queue();
			}
			else {
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
				constructors.Rank user_details = RankingSystem.SQLgetWholeRankView(user_id, guild_id);
				
				if(guild_settings.getRankingState()){				
					var cache = Hashes.getTempCache("profileDelay_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
					if(cache == null || cache.getExpiration() - System.currentTimeMillis() <= 0) {
						Hashes.addTempCache("profileDelay_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId(), new Cache(30000));
						
						float experienceCounter;
						int convertedExperience;
						
						String name = e.getGuild().getMemberById(user_id).getEffectiveName();
						String avatar = e.getGuild().getMemberById(user_id).getUser().getEffectiveAvatarUrl();
						float currentExperience = user_details.getCurrentExperience();
						float rankUpExperience = user_details.getRankUpExperience();
						
						if(user_details.getRankingProfile() != 0 && user_details.getRankingIcon() != 0) {
							if(user_details.getLevel() == guild_settings.getMaxLevel()) {
								currentExperience = 999999; rankUpExperience = 999999;
							}
							
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
								RankingMethods.getProfile(e, name, avatar, convertedExperience, rank, (int)currentExperience, (int)rankUpExperience, guild_settings.getThemeID(), user_details);
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
				else {
					e.getChannel().sendMessage("**The ranking system is disabled. Please contact an administrator to enable the feature!**").queue();
				}
			}
		});
		executor.shutdown();
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("{} has used Profile command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}
}
