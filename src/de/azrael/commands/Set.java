package de.azrael.commands;

import java.awt.Color;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.commandsContainer.SetChannelFilter;
import de.azrael.commandsContainer.SetCompServer;
import de.azrael.commandsContainer.SetDailyItem;
import de.azrael.commandsContainer.SetGiveawayItems;
import de.azrael.commandsContainer.SetIconDefaultSkin;
import de.azrael.commandsContainer.SetLanguage;
import de.azrael.commandsContainer.SetLevelDefaultSkin;
import de.azrael.commandsContainer.SetMaps;
import de.azrael.commandsContainer.SetMatchmakingMembers;
import de.azrael.commandsContainer.SetMaxClanMembers;
import de.azrael.commandsContainer.SetMaxExperience;
import de.azrael.commandsContainer.SetPrivilegeLevel;
import de.azrael.commandsContainer.SetProfileDefaultSkin;
import de.azrael.commandsContainer.SetRankDefaultSkin;
import de.azrael.commandsContainer.SetRankingSystem;
import de.azrael.commandsContainer.SetWarning;
import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Dailies;
import de.azrael.core.UserPrivs;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.fileManagement.IniFileReader;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.sql.BotConfiguration;
import de.azrael.sql.RankingSystem;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * Set up the Bot with various functionalities
 */

public class Set implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(RoleReaction.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(STATIC.getCommandEnabled(e.getGuild(), Command.SET)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean action(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		var adminPermission = BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong());
		var commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET);
		if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
			EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(IniFileReader.getSettingsThumbnail());
			if(args.length == 0) {
				e.getChannel().sendMessage(messageBuild.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_HELP)).build()).queue();
				return true;
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_PERMISSION_LEVEL))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_PRIVILEGE);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_PERMISSION)).build()).queue();
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_PERMISSION_LEVEL))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_PRIVILEGE);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					SetPrivilegeLevel.runTask(e, args);
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_CHANNEL_CENSOR))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_CHANNEL_FILTER);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					final var langs = Azrael.SQLgetLanguages(STATIC.getLanguage(e.getMember()));
					if(langs != null && langs.size() > 0) {
						StringBuilder out = new StringBuilder();
						StringBuilder out2 = new StringBuilder();
						for(final var lang : langs) {
							final String [] split = lang.split("-");
							out.append("**"+split[0]+"**\n");
							out2.append("*"+split[1]+"*\n");
						}
						e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_CENSOR)).addField("", out.toString(), true).addField("", out2.toString(), true).build()).queue();
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Languages couldn't be retrieved in guild {}", e.getGuild().getId());
					}
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_CHANNEL_CENSOR))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_CHANNEL_FILTER);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					SetChannelFilter.runTask(e, args);
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_WARNINGS))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_WARNINGS);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					SetWarning.runHelp(e);
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_WARNINGS))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_WARNINGS);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					SetWarning.runTask(e, args[1]);
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_RANKING))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_RANKING);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_RANKING)).build()).queue();
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_RANKING))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_RANKING);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					SetRankingSystem.runTask(e, args[1]);
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_MAX_EXPERIENCE))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_MAX_EXPERIENCE);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_MAX_EXPERIENCE)).build()).queue();
					return true;
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_MAX_EXPERIENCE))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_MAX_EXPERIENCE);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					SetMaxExperience.runTask(e, args[1], RankingSystem.SQLgetGuild(e.getGuild().getIdLong()));
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DEFAULT_LEVEL_SKIN))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_DEFAULT_LEVEL_SKIN);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					StringBuilder out = new StringBuilder();
					StringBuilder out2 = new StringBuilder();
					final var skins = RankingSystem.SQLgetRankingLevelList(e.getGuild().getIdLong());
					if(skins != null) {
						out.append("0\n");
						out2.append(STATIC.getTranslation(e.getMember(), Translation.PARAM_NONE).toUpperCase()+"\n");
						for(final var skin : skins) {
							out.append(skin.getLine()+"\n");
							out2.append(skin.getSkinDescription()+"\n");
						}
						if(skins.size() > 0)
							e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_LEVEL_HELP)).addField("", out.toString(), true).addField("", out2.toString(), true).build()).queue();
						else
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_LEVEL_SKIN_ERR)).build()).queue();
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Level skins couldn't be retrieved in guild {}", e.getGuild().getId());
					}
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DEFAULT_LEVEL_SKIN))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_DEFAULT_LEVEL_SKIN);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					final var skins = RankingSystem.SQLgetRankingLevelList(e.getGuild().getIdLong());
					if(skins != null) {
						if(skins.size() > 0) {
							if(args[1].replaceAll("[0-9]*", "").length() == 0)
								SetLevelDefaultSkin.runTask(e, Integer.parseInt(args[1]), skins.size(), skins);
							else
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_INVALID_NUMBER)).build()).queue();
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_LEVEL_SKIN_ERR)).build()).queue();
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Level skins couldn't be retrieved in guild {}", e.getGuild().getId());
					}
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DEFAULT_RANK_SKIN))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_DEFAULT_RANK_SKIN);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					StringBuilder out = new StringBuilder();
					StringBuilder out2 = new StringBuilder();
					final var skins = RankingSystem.SQLgetRankingRankList(e.getGuild().getIdLong());
					if(skins != null) {
						out.append("0\n");
						out2.append(STATIC.getTranslation(e.getMember(), Translation.PARAM_NONE).toUpperCase()+"\n");
						for(final var skin : skins) {
							out.append(skin.getLine()+"\n");
							out2.append(skin.getSkinDescription()+"\n");
						}
						if(skins.size() > 0)
							e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_RANK_HELP)).addField("", out.toString(), true).addField("", out2.toString(), true).build()).queue();
						else
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_RANK_SKIN_ERR)).build()).queue();
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Rank skins couldn't be retrieved in guild {}", e.getGuild().getId());
					}
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DEFAULT_RANK_SKIN))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_DEFAULT_RANK_SKIN);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					final var skins = RankingSystem.SQLgetRankingRankList(e.getGuild().getIdLong());
					if(skins != null) {
						if(skins.size() > 0) {
							if(args[1].replaceAll("[0-9]", "").length() == 0)
								SetRankDefaultSkin.runTask(e, Integer.parseInt(args[1]), skins.size(), skins);
							else
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_INVALID_NUMBER)).build()).queue();
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_RANK_SKIN_ERR)).build()).queue();
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Rank skins couldn't be retrieved in guild {}", e.getGuild().getId());
					}
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DEFAULT_PROFILE_SKIN))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_DEFAULT_PROFILE_SKIN);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					StringBuilder out = new StringBuilder();
					StringBuilder out2 = new StringBuilder();
					final var skins = RankingSystem.SQLgetRankingProfileList(e.getGuild().getIdLong());
					if(skins != null) {
						out.append("0\n");
						out2.append(STATIC.getTranslation(e.getMember(), Translation.PARAM_NONE).toUpperCase()+"\n");
						for(final var skin : skins) {
							out.append(skin.getLine()+"\n");
							out2.append(skin.getSkinDescription()+"\n");
						}
						if(skins.size() > 0)
							e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_PROFILE_HELP)).addField("", out.toString(), true).addField("", out2.toString(), true).build()).queue();
						else
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_PROFILE_SKIN_ERR)).build()).queue();
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Profile skins couldn't be retrieved in guild {}", e.getGuild().getId());
					}
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DEFAULT_PROFILE_SKIN))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_DEFAULT_PROFILE_SKIN);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					final var skins = RankingSystem.SQLgetRankingProfileList(e.getGuild().getIdLong());
					if(skins != null) {
						if(skins.size() > 0) {
							if(args[1].replaceAll("[0-9]", "").length() == 0)
								SetProfileDefaultSkin.runTask(e, Integer.parseInt(args[1]), skins.size(), skins);
							else
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_INVALID_NUMBER)).build()).queue();
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_PROFILE_SKIN_ERR)).build()).queue();
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Profile skins couldn't be retrieved in guild {}", e.getGuild().getId());
					}
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DEFAULT_ICON_SKIN))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_DEFAULT_ICON_SKIN);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					StringBuilder out = new StringBuilder();
					StringBuilder out2 = new StringBuilder();
					final var skins = RankingSystem.SQLgetRankingIconsList(e.getGuild().getIdLong());
					if(skins != null) {
						out.append("0\n");
						out2.append(STATIC.getTranslation(e.getMember(), Translation.PARAM_NONE).toUpperCase()+"\n");
						for(final var skin : skins) {
							out.append(skin.getLine()+"\n");
							out2.append(skin.getSkinDescription()+"\n");
						}
						if(skins.size() > 0)
							e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_ICON_HELP)).addField("", out.toString(), true).addField("", out2.toString(), true).build()).queue();
						else
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_ICON_SKIN_ERR)).build()).queue();
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Icon skins couldn't be retrieved in guild {}", e.getGuild().getId());
					}
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DEFAULT_ICON_SKIN))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_DEFAULT_ICON_SKIN);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					final var skins = RankingSystem.SQLgetRankingIconsList(e.getGuild().getIdLong());
					if(skins != null) {
						if(skins.size() > 0) {
							if(args[1].replaceAll("[0-9]", "").length() == 0)
								SetIconDefaultSkin.runTask(e, Integer.parseInt(args[1]), skins.size(), skins);
							else
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_INVALID_NUMBER)).build()).queue();
						}
						else
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_ICON_SKIN_ERR)).build()).queue();
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Icon skins couldn't be retrieved in guild {}", e.getGuild().getId());
					}
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DAILY_ITEM))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_DAILY_ITEM);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_DAILY_ITEM_HELP)).build()).queue();
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DAILY_ITEM))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_DAILY_ITEM);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					ArrayList<Dailies> daily_items = RankingSystem.SQLgetDailiesAndType(e.getGuild().getIdLong());
					var tot_weight = daily_items.parallelStream().mapToInt(i -> i.getWeight()).sum();
					SetDailyItem.runTask(e, args, daily_items, tot_weight);
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_GIVEAWAY_ITEMS))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_GIVEAWAY_ITEMS);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_GIVEAWAY)).build()).queue();
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_GIVEAWAY_ITEMS))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_GIVEAWAY_ITEMS);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					SetGiveawayItems.runTask(e, args);
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_COMP_SERVER))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_COMP_SERVER);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					SetCompServer.runHelp(e);
					return true;
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_COMP_SERVER))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_COMP_SERVER);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					SetCompServer.runTask(e, args);
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_MAX_CLAN_MEMBERS))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_MAX_CLAN_MEMBERS);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					SetMaxClanMembers.runHelp(e);
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_MAX_CLAN_MEMBERS))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_MAX_CLAN_MEMBERS);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					SetMaxClanMembers.runTask(e, args);
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_MATCHMAKING_MEMBERS))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_MATCHMAKING_MEMBERS);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					SetMatchmakingMembers.runHelp(e);
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_MATCHMAKING_MEMBERS))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_MATCHMAKING_MEMBERS);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					SetMatchmakingMembers.runTask(e, args);
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_MAPS))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_MAPS);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					SetMaps.runHelp(e);
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_MAPS))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_MAPS);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					SetMaps.runTask(e, args);
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_LANGUAGE))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_LANGUAGE);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					SetLanguage.runHelp(e);
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_LANGUAGE))) {
				commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.SET_LANGUAGE);
				if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
					SetLanguage.runTask(e, args);
					return true;
				}
				else {
					UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
			}
		}
		else if(!botConfig.getIgnoreMissingPermissions()) {
			UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void executed(String[] args, boolean success, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Set command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.SET.getColumn(), out.toString().trim());
		}
	}
}
