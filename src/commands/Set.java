package commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commandsContainer.SetChannelFilter;
import commandsContainer.SetDailyItem;
import commandsContainer.SetGiveawayItems;
import commandsContainer.SetIconDefaultSkin;
import commandsContainer.SetLevelDefaultSkin;
import commandsContainer.SetMaxExperience;
import commandsContainer.SetPrivilegeLevel;
import commandsContainer.SetProfileDefaultSkin;
import commandsContainer.SetRankDefaultSkin;
import commandsContainer.SetRankingSystem;
import commandsContainer.SetWarning;
import constructors.Dailies;
import core.UserPrivs;
import enums.Translation;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.RankingSystem;
import util.STATIC;

/**
 * Set up the Bot with various functionalities
 * @author xHelixStorm
 *
 */

public class Set implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(RoleReaction.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		if(GuildIni.getSetCommand(e.getGuild().getIdLong())) {
			return true;
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		var adminPermission = (GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong());
		var commandLevel = GuildIni.getSetLevel(e.getGuild().getIdLong());
		if(UserPrivs.comparePrivilege(e.getMember(), GuildIni.getSetLevel(e.getGuild().getIdLong())) || adminPermission) {
			EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(IniFileReader.getSettingsThumbnail());
			if(args.length == 0) {
				e.getChannel().sendMessage(messageBuild.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_HELP)).build()).queue();
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase("permission-level")) {
				commandLevel = GuildIni.getSetPrivilegeLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_PERMISSION)).build()).queue();
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase("permission-level")) {
				commandLevel = GuildIni.getSetPrivilegeLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					SetPrivilegeLevel.runTask(e, args);
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase("channel-censor")) {
				commandLevel = GuildIni.getSetChannelFilterLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_CENSOR)).build()).queue();
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase("channel-censor")) {
				commandLevel = GuildIni.getSetChannelFilterLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					SetChannelFilter.runTask(e, args);
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase("warnings")) {
				commandLevel = GuildIni.getSetWarningsLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					SetWarning.runHelp(e);
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase("warnings")) {
				commandLevel = GuildIni.getSetWarningsLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					SetWarning.runTask(e, args[1]);
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase("ranking")) {
				commandLevel = GuildIni.getSetRankingLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_RANKING)).build()).queue();
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase("ranking")) {
				commandLevel = GuildIni.getSetRankingLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					var guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
					if(guild_settings.getThemeID() == 0) {
						e.getChannel().sendMessage(messageBuild.setDescription("Before enabling the ranking system, please set a theme ID!").build()).queue();
						return;
					}
					if(guild_settings.getLevelID() == 0) {
						e.getChannel().sendMessage(messageBuild.setDescription("Before enabling the ranking system, please set a default level skin!").build()).queue();
						return;
					}
					if(guild_settings.getRankID() == 0) {
						e.getChannel().sendMessage(messageBuild.setDescription("Before enabling the ranking system, please set a default rank skin!").build()).queue();
						return;
					}
					if(guild_settings.getProfileID() == 0) {
						e.getChannel().sendMessage(messageBuild.setDescription("Before enabling the ranking system, please set a default profile skin!").build()).queue();
						return;
					}
					if(guild_settings.getIconID() == 0) {
						e.getChannel().sendMessage(messageBuild.setDescription("Before enabling the ranking system, please set a default icon skin!").build()).queue();
						return;
					}
					SetRankingSystem.runTask(e, args[1]);
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase("max-experience")) {
				commandLevel = GuildIni.getSetMaxExperienceLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_MAX_EXPERIENCE)).build()).queue();
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase("max-experience")) {
				commandLevel = GuildIni.getSetMaxExperienceLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					SetMaxExperience.runTask(e, args[1], RankingSystem.SQLgetGuild(e.getGuild().getIdLong()));
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase("default-level-skin")) {
				commandLevel = GuildIni.getSetDefaultLevelSkinLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					StringBuilder out = new StringBuilder();
					final var skins = RankingSystem.SQLgetRankingLevel(e.getGuild().getIdLong());
					if(skins != null) {
						final var theme_id = RankingSystem.SQLgetGuild(e.getGuild().getIdLong()).getThemeID();
						for(constructors.Rank rankingSystem : skins.parallelStream().filter(t -> t.getThemeID() == theme_id).collect(Collectors.toList())) {
							out.append(rankingSystem.getLevelLine()+":\t"+rankingSystem.getLevelDescription()+"\n");
						}
						if(out.length() > 0)
							e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_LEVEL_HELP)+out.toString()).build()).queue();
						else
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_LEVEL_SKIN_ERR)).build()).queue();
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Level skins couldn't be retrieved from RankingSystem.ranking_level in guild {}", e.getGuild().getId());
					}
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase("default-level-skin")) {
				commandLevel = GuildIni.getSetDefaultLevelSkinLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					final var skins = RankingSystem.SQLgetRankingLevel(e.getGuild().getIdLong());
					if(skins != null) {
						final var theme_id = RankingSystem.SQLgetGuild(e.getGuild().getIdLong()).getThemeID();
						var themedSkins = skins.parallelStream().filter(t -> t.getThemeID() == theme_id).collect(Collectors.toList());
						if(themedSkins.size() > 0) {
							if(args[1].replaceAll("[0-9]*", "").length() == 0)
								SetLevelDefaultSkin.runTask(e, Integer.parseInt(args[1]), themedSkins.size(), themedSkins);
							else
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_INVALID_NUMBER)).build()).queue();
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_LEVEL_SKIN_ERR)).build()).queue();
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Level skins couldn't be retrieved from RankingSystem.ranking_level in guild {}", e.getGuild().getId());
					}
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase("default-rank-skin")) {
				commandLevel = GuildIni.getSetDefaultRankSkinLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					StringBuilder out = new StringBuilder();
					final var skins = RankingSystem.SQLgetRankingRank(e.getGuild().getIdLong());
					if(skins != null) {
						final var theme_id = RankingSystem.SQLgetGuild(e.getGuild().getIdLong()).getThemeID();
						for(constructors.Rank rankingSystem : skins.parallelStream().filter(t -> t.getThemeID() == theme_id).collect(Collectors.toList())) {
							out.append(rankingSystem.getRankLine()+":\t"+rankingSystem.getRankDescription()+"\n");
						}
						if(out.length() > 0)
							e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_RANK_HELP)+out.toString()).build()).queue();
						else
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_RANK_SKIN_ERR)).build()).queue();
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Rank skins couldn't be retrieved from RankingSystem.ranking_rank in guild {}", e.getGuild().getId());
					}
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase("default-rank-skin")) {
				commandLevel = GuildIni.getSetDefaultRankSkinLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					final var skins = RankingSystem.SQLgetRankingRank(e.getGuild().getIdLong());
					if(skins != null) {
						final var theme_id = RankingSystem.SQLgetGuild(e.getGuild().getIdLong()).getThemeID();
						var themedSkins = skins.parallelStream().filter(t -> t.getThemeID() == theme_id).collect(Collectors.toList());
						if(themedSkins.size() > 0) {
							if(args[1].replaceAll("[0-9]", "").length() == 0)
								SetRankDefaultSkin.runTask(e, Integer.parseInt(args[1]), themedSkins.size(), themedSkins);
							else
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_INVALID_NUMBER)).build()).queue();
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_RANK_SKIN_ERR)).build()).queue();
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Rank skins couldn't be retrieved from RankingSystem.ranking_rank in guild {}", e.getGuild().getId());
					}
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase("default-profile-skin")) {
				commandLevel = GuildIni.getSetDefaultProfileSkinLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					StringBuilder out = new StringBuilder();
					final var skins = RankingSystem.SQLgetRankingProfile(e.getGuild().getIdLong());
					if(skins != null) {
						final var theme_id = RankingSystem.SQLgetGuild(e.getGuild().getIdLong()).getThemeID();
						for(constructors.Rank rankingSystem : skins.parallelStream().filter(t -> t.getThemeID() == theme_id).collect(Collectors.toList())) {
							out.append(rankingSystem.getProfileLine()+":\t"+rankingSystem.getProfileDescription()+"\n");
						}
						if(out.length() > 0)
							e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_PROFILE_HELP)+out.toString()).build()).queue();
						else
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_PROFILE_SKIN_ERR)).build()).queue();
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Profile skins couldn't be retrieved from RankingSystem.ranking_profile in guild {}", e.getGuild().getId());
					}
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase("default-profile-skin")) {
				commandLevel = GuildIni.getSetDefaultProfileSkinLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					final var theme_id = RankingSystem.SQLgetGuild(e.getGuild().getIdLong()).getThemeID();
					final var skins = RankingSystem.SQLgetRankingProfile(e.getGuild().getIdLong());
					if(skins != null) {
						var themedSkins = skins.parallelStream().filter(t -> t.getThemeID() == theme_id).collect(Collectors.toList());
						if(themedSkins.size() > 0) {
							if(args[1].replaceAll("[0-9]", "").length() == 0)
								SetProfileDefaultSkin.runTask(e, Integer.parseInt(args[1]), themedSkins.size(), themedSkins);
							else
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_INVALID_NUMBER)).build()).queue();
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_PROFILE_SKIN_ERR)).build()).queue();
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Profile skins couldn't be retrieved from RankingSystem.ranking_profile in guild {}", e.getGuild().getId());
					}
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase("default-icon-skin")) {
				commandLevel = GuildIni.getSetDefaultIconSkinLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					StringBuilder out = new StringBuilder();
					final var skins = RankingSystem.SQLgetRankingIcons(e.getGuild().getIdLong());
					if(skins != null) {
						final var theme_id = RankingSystem.SQLgetGuild(e.getGuild().getIdLong()).getThemeID();
						for(constructors.Rank rankingSystem : skins.parallelStream().filter(t -> t.getThemeID() == theme_id).collect(Collectors.toList())) {
							out.append(rankingSystem.getIconLine()+":\t"+rankingSystem.getIconDescription()+"\n");
						}
						if(out.length() > 0)
							e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_ICON_HELP)+out.toString()).build()).queue();
						else
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_ICON_SKIN_ERR)).build()).queue();
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Icon skins couldn't be retrieved from RankingSystem.ranking_icons in guild {}", e.getGuild().getId());
					}
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase("default-icon-skin")) {
				commandLevel = GuildIni.getSetDefaultIconSkinLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					final var skins = RankingSystem.SQLgetRankingIcons(e.getGuild().getIdLong());
					if(skins != null) {
						final var theme_id = RankingSystem.SQLgetGuild(e.getGuild().getIdLong()).getThemeID();
						var themedSkins = skins.parallelStream().filter(t -> t.getThemeID() == theme_id).collect(Collectors.toList());
						if(themedSkins.size() > 0) {
							if(args[1].replaceAll("[0-9]", "").length() == 0)
								SetIconDefaultSkin.runTask(e, Integer.parseInt(args[1]), themedSkins.size(), themedSkins);
							else
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_INVALID_NUMBER)).build()).queue();
						}
						else
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_ICON_SKIN_ERR)).build()).queue();
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Icon skins couldn't be retrieved from RankingSystem.ranking_icons in guild {}", e.getGuild().getId());
					}
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase("daily-item")) {
				commandLevel = GuildIni.getSetDailyItemLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_DAILY_ITEM_HELP)).build()).queue();
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase("daily-item")) {
				commandLevel = GuildIni.getSetDailyItemLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					ArrayList<Dailies> daily_items = RankingSystem.SQLgetDailiesAndType(e.getGuild().getIdLong(), RankingSystem.SQLgetGuild(e.getGuild().getIdLong()).getThemeID());
					var tot_weight = daily_items.parallelStream().mapToInt(i -> i.getWeight()).sum();
					SetDailyItem.runTask(e, args, daily_items, tot_weight);
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase("giveaway-items")) {
				commandLevel = GuildIni.getSetGiveawayItemsLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_GIVEAWAY)).build()).queue();
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase("giveaway-items")) {
				commandLevel = GuildIni.getSetGiveawayItemsLevel(e.getGuild().getIdLong());
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					//TODO: rework the command to replace the current saved list
					SetGiveawayItems.runTask(e, args[1]);
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
			}
		}
		else {
			UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("{} has used Set command for guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}
}
