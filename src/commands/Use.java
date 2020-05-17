package commands;

import java.awt.Color;
import java.sql.Timestamp;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Guilds;
import core.Hashes;
import core.UserPrivs;
import enums.Translation;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.RankingSystem;
import util.STATIC;
import sql.Azrael;

/**
 * The use command will allow users to open an item from their
 * inventories or to use purchased skins 
 * @author xHelixStorm
 *
 */

public class Use implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Use.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		if(GuildIni.getUseCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getUseLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		Guilds guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
		constructors.Rank user_details = RankingSystem.SQLgetWholeRankView(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong());
		if(guild_settings.getRankingState()) {
			var bot_channels = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("bot")).collect(Collectors.toList());
			if(bot_channels.size() == 0 || bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null) != null) {
				if(args.length == 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.USE_HELP)).build()).queue();
				}
				else if(args[0].equalsIgnoreCase("default-level")) {
					constructors.Rank rank = RankingSystem.SQLgetRankingLevel(e.getGuild().getIdLong()).parallelStream().filter(r -> r.getLevelDescription().equalsIgnoreCase(guild_settings.getLevelDescription()) && r.getThemeID() == guild_settings.getThemeID()).findAny().orElse(null);
					user_details.setRankingLevel(rank.getRankingLevel());
					user_details.setLevelDescription(rank.getLevelDescription());
					user_details.setFileTypeLevel(rank.getFileTypeLevel());
					user_details.setColorRLevel(rank.getColorRLevel());
					user_details.setColorGLevel(rank.getColorGLevel());
					user_details.setColorBLevel(rank.getColorBLevel());
					user_details.setRankXLevel(rank.getRankXLevel());
					user_details.setRankYLevel(rank.getRankYLevel());
					user_details.setRankWidthLevel(rank.getRankWidthLevel());
					user_details.setRankHeightLevel(rank.getRankHeightLevel());
					user_details.setLevelXLevel(rank.getLevelXLevel());
					user_details.setLevelYLevel(rank.getLevelYLevel());
					user_details.setNameXLevel(rank.getNameXLevel());
					user_details.setNameYLevel(rank.getNameYLevel());
					user_details.setNameLengthLimit_Level(rank.getNameLengthLimit_Level());
					user_details.setTextFontSize_Level(rank.getTextFontSize_Level());
					user_details.setNameFontSize_Level(rank.getNameFontSize_Level());
					if(user_details.getRankingLevel() != 0) {
						if(RankingSystem.SQLUpdateUserLevelSkin(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingLevel()) > 0) {
							Hashes.addRanking(e.getGuild().getId()+"_"+e.getMember().getUser().getIdLong(), user_details);
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.USE_LEVEL_RESET).replace("{}", user_details.getLevelDescription())).build()).queue();
						}
						else {
							//if rows didn't get updated, throw an error and write it into error log
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("RankingSystem.users table couldn't be updated with the default level skin for {}", e.getMember().getUser().getId());
							RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Level skin couldn't be resetted", "Resetting to the default skin has failed. Skin: "+user_details.getLevelDescription());
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Default skins in RankingSystem.guilds are not defined for guild {}", e.getGuild().getName());
					}
				}
				else if(args[0].equalsIgnoreCase("default-rank")) {
					constructors.Rank rank = RankingSystem.SQLgetRankingRank(e.getGuild().getIdLong()).parallelStream().filter(r -> r.getRankDescription().equalsIgnoreCase(guild_settings.getRankDescription()) && r.getThemeID() == guild_settings.getThemeID()).findAny().orElse(null);
					user_details.setRankingRank(rank.getRankingRank());
					user_details.setRankDescription(rank.getRankDescription());
					user_details.setFileTypeRank(rank.getFileTypeRank());
					user_details.setBarColorRank(rank.getBarColorRank());
					user_details.setColorRRank(rank.getColorRRank());
					user_details.setColorGRank(rank.getColorGRank());
					user_details.setColorBRank(rank.getColorBRank());
					user_details.setRankXRank(rank.getRankXRank());
					user_details.setRankYRank(rank.getRankYRank());
					user_details.setRankWidthRank(rank.getRankWidthRank());
					user_details.setRankHeightRank(rank.getRankHeightRank());
					user_details.setNameXRank(rank.getNameXRank());
					user_details.setNameYRank(rank.getNameYRank());
					user_details.setBarXRank(rank.getBarXRank());
					user_details.setBarYRank(rank.getBarYRank());
					user_details.setAvatarXRank(rank.getAvatarXRank());
					user_details.setAvatarYRank(rank.getAvatarYRank());
					user_details.setAvatarWidthRank(rank.getAvatarWidthRank());
					user_details.setAvatarHeightRank(rank.getAvatarHeightRank());
					user_details.setExpTextXRank(rank.getExpTextXRank());
					user_details.setExpTextYRank(rank.getExpTextYRank());
					user_details.setPercentTextXRank(rank.getPercentTextXRank());
					user_details.setPercentTextYRank(rank.getPercentTextYRank());
					user_details.setPlacementXRank(rank.getPlacementXRank());
					user_details.setPlacementYRank(rank.getPlacementYRank());
					user_details.setNameLengthLimit_Rank(rank.getNameLengthLimit_Rank());
					user_details.setTextFontSize_Rank(rank.getTextFontSize_Rank());
					user_details.setNameFontSize_Rank(rank.getNameFontSize_Rank());
					
					if(user_details.getRankingRank() != 0) {
						if(RankingSystem.SQLUpdateUserRankSkin(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingRank()) > 0) {
							Hashes.addRanking(e.getGuild().getId()+"_"+e.getMember().getUser().getIdLong(), user_details);
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.USE_RANK_RESET).replace("{}", user_details.getRankDescription())).build()).queue();
						}
						else {
							//if rows didn't get updated, throw an error and write it into error log
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("RankingSystem.users table couldn't be updated with the default rank skin for {}", e.getMember().getUser().getId());
							RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Rank skin couldn't be resetted", "Resetting to the default skin has failed. Skin: "+user_details.getRankDescription());
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Default skins in RankingSystem.guilds are not defined for guild {}", e.getGuild().getName());
					}
				}
				else if(args[0].equalsIgnoreCase("default-profile")) {
					constructors.Rank rank = RankingSystem.SQLgetRankingProfile(e.getGuild().getIdLong()).parallelStream().filter(r -> r.getProfileDescription().equalsIgnoreCase(guild_settings.getProfileDescription()) && r.getThemeID() == guild_settings.getThemeID()).findAny().orElse(null);
					user_details.setRankingProfile(rank.getRankingProfile());
					user_details.setProfileDescription(rank.getProfileDescription());
					user_details.setFileTypeProfile(rank.getFileTypeProfile());
					user_details.setBarColorProfile(rank.getBarColorProfile());
					user_details.setColorRProfile(rank.getColorRProfile());
					user_details.setColorGProfile(rank.getColorGProfile());
					user_details.setColorBProfile(rank.getColorBProfile());
					user_details.setRankXProfile(rank.getRankXProfile());
					user_details.setRankYProfile(rank.getRankYProfile());
					user_details.setRankWidthProfile(rank.getRankWidthProfile());
					user_details.setRankHeightProfile(rank.getRankHeightProfile());
					user_details.setLevelXProfile(rank.getLevelXProfile());
					user_details.setLevelYProfile(rank.getLevelYProfile());
					user_details.setNameXProfile(rank.getNameXProfile());
					user_details.setNameYProfile(rank.getNameYProfile());
					user_details.setBarXProfile(rank.getBarXProfile());
					user_details.setBarYProfile(rank.getBarYProfile());
					user_details.setAvatarXProfile(rank.getAvatarXProfile());
					user_details.setAvatarYProfile(rank.getAvatarYProfile());
					user_details.setAvatarWidthProfile(rank.getAvatarWidthProfile());
					user_details.setAvatarHeightProfile(rank.getAvatarHeightProfile());
					user_details.setExpTextXProfile(rank.getExpTextXProfile());
					user_details.setExpTextYProfile(rank.getExpTextYProfile());
					user_details.setPercentTextXProfile(rank.getPercentTextXProfile());
					user_details.setPercentTextYProfile(rank.getPercentTextYProfile());
					user_details.setPlacementXProfile(rank.getPlacementXProfile());
					user_details.setPlacementYProfile(rank.getPlacementYProfile());
					user_details.setExperienceXProfile(rank.getExperienceXProfile());
					user_details.setExperienceYProfile(rank.getExperienceYProfile());
					user_details.setCurrencyXProfile(rank.getCurrencyXProfile());
					user_details.setCurrencyYProfile(rank.getCurrencyYProfile());
					user_details.setNameLengthLimit_Profile(rank.getNameLengthLimit_Profile());
					user_details.setTextFontSize_Profile(rank.getTextFontSize_Profile());
					user_details.setNameFontSize_Profile(rank.getNameFontSize_Profile());
					user_details.setDescriptionMode_Profile(rank.getDescriptionMode_Profile());
					if(user_details.getRankingProfile() != 0) {
						if(RankingSystem.SQLUpdateUserProfileSkin(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingProfile()) > 0) {
							Hashes.addRanking(e.getGuild().getId()+"_"+e.getMember().getUser().getIdLong(), user_details);
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.USE_PROFILE_RESET).replace("{}", user_details.getProfileDescription())).build()).queue();
						}
						else {
							//if rows didn't get updated, throw an error and write it into error log
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("RankingSystem.users table couldn't be updated with the default profile skin for {}", e.getMember().getUser().getId());
							RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Profile skin couldn't be resetted", "Resetting to the default skin has failed. Skin: "+user_details.getProfileDescription());
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Default skins in RankingSystem.guilds are not defined for guild {}", e.getGuild().getName());
					}
				}
				else if(args[0].equalsIgnoreCase("default-icons")) {
					constructors.Rank rank = RankingSystem.SQLgetRankingIcons(e.getGuild().getIdLong()).parallelStream().filter(r -> r.getIconDescription().equalsIgnoreCase(guild_settings.getIconDescription()) && r.getThemeID() == guild_settings.getThemeID()).findAny().orElse(null);
					user_details.setRankingIcon(rank.getRankingIcon());
					user_details.setIconDescription(rank.getIconDescription());
					user_details.setFileTypeIcon(rank.getFileTypeIcon());
					if(user_details.getRankingIcon() != 0) {
						if(RankingSystem.SQLUpdateUserIconSkin(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingIcon()) > 0) {
							Hashes.addRanking(e.getGuild().getId()+"_"+e.getMember().getUser().getIdLong(), user_details);
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.USE_ICON_RESET).replace("{}", user_details.getIconDescription())).build()).queue();
						}
						else {
							//if rows didn't get updated, throw and error and write it into error log
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("RankingSystem.users table couldn't be updated with the default icon skin for {}", e.getMember().getUser().getId());
							RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Icons skin couldn't be resetted", "Resetting to the default skin has failed. Skin: "+user_details.getIconDescription());
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Default skins in RankingSystem.guilds are not defined for guild {}", e.getGuild().getName());
					}
				}
				else {
					String input = args[0];
					constructors.Inventory inventory = RankingSystem.SQLgetItemIDAndSkinType(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), input, guild_settings.getThemeID());
					if(inventory != null && inventory.getItemID() != 0 && inventory.getStatus().equals("perm")) {
						if(inventory.getSkinType().equals("lev")) {
							final String filter = input;
							constructors.Rank rank = RankingSystem.SQLgetRankingLevel(e.getGuild().getIdLong()).parallelStream().filter(r -> r.getLevelDescription().equalsIgnoreCase(filter)).findAny().orElse(null);
							user_details.setRankingLevel(rank.getRankingLevel());
							user_details.setLevelDescription(rank.getLevelDescription());
							user_details.setFileTypeLevel(rank.getFileTypeLevel());
							user_details.setColorRLevel(rank.getColorRLevel());
							user_details.setColorGLevel(rank.getColorGLevel());
							user_details.setColorBLevel(rank.getColorBLevel());
							user_details.setRankXLevel(rank.getRankXLevel());
							user_details.setRankYLevel(rank.getRankYLevel());
							user_details.setRankWidthLevel(rank.getRankWidthLevel());
							user_details.setRankHeightLevel(rank.getRankHeightLevel());
							user_details.setLevelXLevel(rank.getLevelXLevel());
							user_details.setLevelYLevel(rank.getLevelYLevel());
							user_details.setNameXLevel(rank.getNameXLevel());
							user_details.setNameYLevel(rank.getNameYLevel());
							user_details.setNameLengthLimit_Level(rank.getNameLengthLimit_Level());
							user_details.setTextFontSize_Level(rank.getTextFontSize_Level());
							user_details.setNameFontSize_Level(rank.getNameFontSize_Level());
							if(RankingSystem.SQLUpdateUserLevelSkin(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingLevel()) > 0) {
								Hashes.addRanking(e.getGuild().getId()+"_"+e.getMember().getUser().getIdLong(), user_details);
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.USE_SKIN).replace("{}", input)).build()).queue();
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
								logger.error("RankingSystem.users table couldn't be updated with the selected level skin {} for {}", user_details.getLevelDescription(), e.getMember().getUser().getId());
								RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Level skin couldn't be updated", "Level skin update has failed. Skin: "+user_details.getLevelDescription());
							}
						}
						else if(inventory.getSkinType().equals("ran")) {
							final String filter = input;
							constructors.Rank rank = RankingSystem.SQLgetRankingRank(e.getGuild().getIdLong()).parallelStream().filter(r -> r.getRankDescription().equalsIgnoreCase(filter)).findAny().orElse(null);
							user_details.setRankingRank(rank.getRankingRank());
							user_details.setRankDescription(rank.getRankDescription());
							user_details.setFileTypeRank(rank.getFileTypeRank());
							user_details.setBarColorRank(rank.getBarColorRank());
							user_details.setColorRRank(rank.getColorRRank());
							user_details.setColorGRank(rank.getColorGRank());
							user_details.setColorBRank(rank.getColorBRank());
							user_details.setRankXRank(rank.getRankXRank());
							user_details.setRankYRank(rank.getRankYRank());
							user_details.setRankWidthRank(rank.getRankWidthRank());
							user_details.setRankHeightRank(rank.getRankHeightRank());
							user_details.setNameXRank(rank.getNameXRank());
							user_details.setNameYRank(rank.getNameYRank());
							user_details.setBarXRank(rank.getBarXRank());
							user_details.setBarYRank(rank.getBarYRank());
							user_details.setAvatarXRank(rank.getAvatarXRank());
							user_details.setAvatarYRank(rank.getAvatarYRank());
							user_details.setAvatarWidthRank(rank.getAvatarWidthRank());
							user_details.setAvatarHeightRank(rank.getAvatarHeightRank());
							user_details.setExpTextXRank(rank.getExpTextXRank());
							user_details.setExpTextYRank(rank.getExpTextYRank());
							user_details.setPercentTextXRank(rank.getPercentTextXRank());
							user_details.setPercentTextYRank(rank.getPercentTextYRank());
							user_details.setPlacementXRank(rank.getPlacementXRank());
							user_details.setPlacementYRank(rank.getPlacementYRank());
							user_details.setNameLengthLimit_Rank(rank.getNameLengthLimit_Rank());
							user_details.setTextFontSize_Rank(rank.getTextFontSize_Rank());
							user_details.setNameFontSize_Rank(rank.getNameFontSize_Rank());
							if(RankingSystem.SQLUpdateUserRankSkin(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingRank()) > 0) {
								Hashes.addRanking(e.getGuild().getId()+"_"+e.getMember().getUser().getIdLong(), user_details);
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.USE_SKIN).replace("{}", input)).build()).queue();
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
								logger.error("RankingSystem.users table couldn't be updated with the selected rank skin {} for {}", user_details.getRankDescription(), e.getMember().getUser().getId());
								RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Rank skin couldn't be updated", "Rank skin update has failed. Skin: "+user_details.getRankDescription());
							}
						}
						else if(inventory.getSkinType().equals("pro")) {
							final String filter = input;
							constructors.Rank rank = RankingSystem.SQLgetRankingProfile(e.getGuild().getIdLong()).parallelStream().filter(r -> r.getProfileDescription().equalsIgnoreCase(filter)).findAny().orElse(null);
							user_details.setRankingProfile(rank.getRankingProfile());
							user_details.setProfileDescription(rank.getProfileDescription());
							user_details.setFileTypeProfile(rank.getFileTypeProfile());
							user_details.setBarColorProfile(rank.getBarColorProfile());
							user_details.setColorRProfile(rank.getColorRProfile());
							user_details.setColorGProfile(rank.getColorGProfile());
							user_details.setColorBProfile(rank.getColorBProfile());
							user_details.setRankXProfile(rank.getRankXProfile());
							user_details.setRankYProfile(rank.getRankYProfile());
							user_details.setRankWidthProfile(rank.getRankWidthProfile());
							user_details.setRankHeightProfile(rank.getRankHeightProfile());
							user_details.setLevelXProfile(rank.getLevelXProfile());
							user_details.setLevelYProfile(rank.getLevelYProfile());
							user_details.setNameXProfile(rank.getNameXProfile());
							user_details.setNameYProfile(rank.getNameYProfile());
							user_details.setBarXProfile(rank.getBarXProfile());
							user_details.setBarYProfile(rank.getBarYProfile());
							user_details.setAvatarXProfile(rank.getAvatarXProfile());
							user_details.setAvatarYProfile(rank.getAvatarYProfile());
							user_details.setAvatarWidthProfile(rank.getAvatarWidthProfile());
							user_details.setAvatarHeightProfile(rank.getAvatarHeightProfile());
							user_details.setExpTextXProfile(rank.getExpTextXProfile());
							user_details.setExpTextYProfile(rank.getExpTextYProfile());
							user_details.setPercentTextXProfile(rank.getPercentTextXProfile());
							user_details.setPercentTextYProfile(rank.getPercentTextYProfile());
							user_details.setPlacementXProfile(rank.getPlacementXProfile());
							user_details.setPlacementYProfile(rank.getPlacementYProfile());
							user_details.setExperienceXProfile(rank.getExperienceXProfile());
							user_details.setExperienceYProfile(rank.getExperienceYProfile());
							user_details.setCurrencyXProfile(rank.getCurrencyXProfile());
							user_details.setCurrencyYProfile(rank.getCurrencyYProfile());
							user_details.setExpReachXProfile(rank.getExpReachXProfile());
							user_details.setExpReachYProfile(rank.getExpReachYProfile());
							user_details.setNameLengthLimit_Profile(rank.getNameLengthLimit_Profile());
							user_details.setTextFontSize_Profile(rank.getTextFontSize_Profile());
							user_details.setNameFontSize_Profile(rank.getNameFontSize_Profile());
							user_details.setDescriptionMode_Profile(rank.getDescriptionMode_Profile());
							if(RankingSystem.SQLUpdateUserProfileSkin(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingProfile()) > 0) {
								Hashes.addRanking(e.getGuild().getId()+"_"+e.getMember().getUser().getIdLong(), user_details);
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.USE_SKIN).replace("{}", input)).build()).queue();
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
								logger.error("RankingSystem.users table couldn't be updated with the selected profile skin {} for {}", user_details.getProfileDescription(), e.getMember().getUser().getId());
								RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Profile skin couldn't be updated", "Profile skin update has failed. Skin: "+user_details.getProfileDescription());
							}
						}
						else if(inventory.getSkinType().equals("ico")) {
							final String filter = input;
							constructors.Rank rank = RankingSystem.SQLgetRankingIcons(e.getGuild().getIdLong()).parallelStream().filter(r -> r.getIconDescription().equalsIgnoreCase(filter)).findAny().orElse(null);
							user_details.setRankingIcon(rank.getRankingIcon());
							user_details.setIconDescription(rank.getIconDescription());
							user_details.setFileTypeIcon(rank.getFileTypeIcon());
							if(RankingSystem.SQLUpdateUserIconSkin(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingIcon()) > 0) {
								Hashes.addRanking(e.getGuild().getId()+"_"+e.getMember().getUser().getIdLong(), user_details);
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.USE_SKIN).replace("{}", input)).build()).queue();
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
								logger.error("RankingSystem.users table couldn't be updated with the selected icons skin {} for {}", user_details.getIconDescription(), e.getMember().getUser().getId());
								RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Icon skin couldn't be updated", "Icon skin update has failed. Skin: "+user_details.getIconDescription());
							}
						}
						else if(inventory.getSkinType().equals("ite")) {
							var inventoryNumber = RankingSystem.SQLgetInventoryNumber(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), input, "perm", guild_settings.getThemeID());
							var expiration = RankingSystem.SQLgetExpirationFromInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), inventory.getItemID(), guild_settings.getThemeID());
							var numberLimit = RankingSystem.SQLgetNumberLimitFromInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), inventory.getItemID(), guild_settings.getThemeID());
							long time = System.currentTimeMillis();
							Timestamp timestamp = new Timestamp(time);
							try {
								Timestamp timestamp2 = new Timestamp(expiration.getTime()+1000*60*60*24);
								if(inventoryNumber == 1) {
									if(RankingSystem.SQLDeleteAndInsertInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), numberLimit+1, inventory.getItemID(), timestamp, timestamp2, guild_settings.getThemeID()) == 0) {
										e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
										logger.error("Item id {} for the user {} couldn't be used or opened in guild {}", inventory.getItemID(), e.getMember().getUser().getId(), e.getGuild().getId());
										RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Item couldn't be used or activated", "Item use failed. item id: "+inventory.getItemID());
									}
								}
								else {
									if(RankingSystem.SQLUpdateAndInsertInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), inventoryNumber, numberLimit+1, inventory.getItemID(), timestamp, timestamp2, guild_settings.getThemeID()) == 0) {
										e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
										logger.error("Item id {} for the user {} couldn't be used or opened in guild {}", inventory.getItemID(), e.getMember().getUser().getId(), e.getGuild().getId());
										RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Item couldn't be used or activated", "Item use failed. item id: "+inventory.getItemID());
									}
								}
							} catch(NullPointerException npe) {
								Timestamp timestamp2 = new Timestamp(time+1000*60*60*24);
								if(inventoryNumber == 1) {
									if(RankingSystem.SQLDeleteAndInsertInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), numberLimit+1, inventory.getItemID(), timestamp, timestamp2, guild_settings.getThemeID()) == 0) {
										e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
										logger.error("Item id {} for the user {} couldn't be used or opened in guild {}", inventory.getItemID(), e.getMember().getUser().getId(), e.getGuild().getId());
										RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Item couldn't be used or activated", "Item use failed. item id: "+inventory.getItemID());
									}
								}
								else {
									if(RankingSystem.SQLUpdateAndInsertInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), inventoryNumber, numberLimit+1, inventory.getItemID(), timestamp, timestamp2, guild_settings.getThemeID()) == 0) {
										e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
										logger.error("Item id {} for the user {} couldn't be used or opened in guild {}", inventory.getItemID(), e.getMember().getUser().getId(), e.getGuild().getId());
										RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Item couldn't be used or activated", "Item use failed. item id: "+inventory.getItemID());
									}
								}
							}
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.USE_ITEM).replace("{}", input)).build()).queue();
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.USE_NOT_EXISTS)).build()).queue();
					}
				}
			}
			else {
				e.getChannel().sendMessage(e.getMember().getAsMention()+STATIC.getTranslation(e.getMember(), Translation.NOT_BOT_CHANNEL)+STATIC.getChannels(bot_channels)).queue();
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.LEVEL_SYSTEM_NOT_ENABLED)).build()).queue();
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("{} has used Use command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}
}
