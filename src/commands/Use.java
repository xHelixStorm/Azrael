package commands;

import java.awt.Color;
import java.sql.Timestamp;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Guilds;
import constructors.Ranking;
import core.Hashes;
import core.UserPrivs;
import enums.Channel;
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
		Ranking user_details = RankingSystem.SQLgetWholeRankView(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong());
		if(guild_settings.getRankingState()) {
			var bot_channels = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.BOT.getType())).collect(Collectors.toList());
			if(bot_channels.size() == 0 || bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null) != null) {
				if(args.length == 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.USE_HELP)).build()).queue();
				}
				else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DEFAULT_LEVEL))) {
					user_details.setRankingLevel(guild_settings.getLevelID());
					final var skinDescription = (guild_settings.getLevelID() > 0 ? guild_settings.getLevelDescription() : STATIC.getTranslation(e.getMember(), Translation.PARAM_NONE).toUpperCase());
					if(RankingSystem.SQLUpdateUserLevelSkin(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingLevel()) > 0) {
						Hashes.addRanking(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), user_details);
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.USE_LEVEL_RESET).replace("{}", skinDescription)).build()).queue();
					}
					else {
						//if rows didn't get updated, throw an error and write it into error log
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Current level skin couldn't be updated with the default level skin for user {} in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
						RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Level skin couldn't be resetted", "Resetting to the default skin has failed. Skin: "+skinDescription);
					}
				}
				else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DEFAULT_RANK))) {
					user_details.setRankingRank(guild_settings.getRankID());
					final var skinDescription = (guild_settings.getRankID() > 0 ? guild_settings.getRankDescription() : STATIC.getTranslation(e.getMember(), Translation.PARAM_NONE).toUpperCase());
					if(RankingSystem.SQLUpdateUserRankSkin(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingRank()) > 0) {
						Hashes.addRanking(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), user_details);
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.USE_RANK_RESET).replace("{}", skinDescription)).build()).queue();
					}
					else {
						//if rows didn't get updated, throw an error and write it into error log
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Current rank skin couldn't be updated with the default rank skin for user {} in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
						RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Rank skin couldn't be resetted", "Resetting to the default skin has failed. Skin: "+skinDescription);
					}
				}
				else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DEFAULT_PROFILE))) {
					user_details.setRankingProfile(guild_settings.getProfileID());
					final var skinDescription = (guild_settings.getProfileID() > 0 ? guild_settings.getProfileDescription() : STATIC.getTranslation(e.getMember(), Translation.PARAM_NONE).toUpperCase());
					if(RankingSystem.SQLUpdateUserProfileSkin(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingProfile()) > 0) {
						Hashes.addRanking(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), user_details);
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.USE_PROFILE_RESET).replace("{}", skinDescription)).build()).queue();
					}
					else {
						//if rows didn't get updated, throw an error and write it into error log
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Current profile skin couldn't be updated with the default profile skin for user {} in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
						RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Profile skin couldn't be resetted", "Resetting to the default skin has failed. Skin: "+skinDescription);
					}
				}
				else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DEFAULT_ICONS))) {
					user_details.setRankingIcon(guild_settings.getIconID());
					final var skinDescription = (guild_settings.getIconID() > 0 ? guild_settings.getIconDescription() : STATIC.getTranslation(e.getMember(), Translation.PARAM_NONE).toUpperCase());
					if(RankingSystem.SQLUpdateUserIconSkin(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingIcon()) > 0) {
						Hashes.addRanking(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), user_details);
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.USE_ICON_RESET).replace("{}", skinDescription)).build()).queue();
					}
					else {
						//if rows didn't get updated, throw and error and write it into error log
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Current icon skin couldn't be updated with the default icon skin for user {} in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
						RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Icons skin couldn't be resetted", "Resetting to the default skin has failed. Skin: "+skinDescription);
					}
				}
				else {
					StringBuilder out = new StringBuilder();
					for(int i = 0; i < args.length; i++) {
						out.append(args[i]+" ");
					}
					String input = out.toString().trim();
					constructors.Inventory inventory = RankingSystem.SQLgetItemIDAndSkinType(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), input);
					if(inventory != null && inventory.getItemID() != 0 && inventory.getStatus().equals("perm")) {
						if(inventory.getSkinType().equals("lev")) {
							final String filter = input;
							final var skin = RankingSystem.SQLgetRankingLevelList(e.getGuild().getIdLong()).parallelStream().filter(r -> r.getSkinDescription().equalsIgnoreCase(filter)).findAny().orElse(null);
							user_details.setRankingLevel(skin.getSkin());
							if(RankingSystem.SQLUpdateUserLevelSkin(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingLevel()) > 0) {
								Hashes.addRanking(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), user_details);
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.USE_SKIN).replace("{}", input)).build()).queue();
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
								logger.error("Selected level skin {} for user {} couldn't be updated in guild {}", skin.getSkinDescription(), e.getMember().getUser().getId(), e.getGuild().getId());
								RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Level skin couldn't be updated", "Level skin update has failed. Skin: "+skin.getSkinDescription());
							}
						}
						else if(inventory.getSkinType().equals("ran")) {
							final String filter = input;
							final var skin = RankingSystem.SQLgetRankingRankList(e.getGuild().getIdLong()).parallelStream().filter(r -> r.getSkinDescription().equalsIgnoreCase(filter)).findAny().orElse(null);
							user_details.setRankingRank(skin.getSkin());
							if(RankingSystem.SQLUpdateUserRankSkin(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingRank()) > 0) {
								Hashes.addRanking(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), user_details);
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.USE_SKIN).replace("{}", input)).build()).queue();
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
								logger.error("Selected rank skin {} for user {} couldn't be updated in guild {}", skin.getSkinDescription(), e.getMember().getUser().getId(), e.getGuild().getId());
								RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Rank skin couldn't be updated", "Rank skin update has failed. Skin: "+skin.getSkinDescription());
							}
						}
						else if(inventory.getSkinType().equals("pro")) {
							final String filter = input;
							final var skin = RankingSystem.SQLgetRankingProfileList(e.getGuild().getIdLong()).parallelStream().filter(r -> r.getSkinDescription().equalsIgnoreCase(filter)).findAny().orElse(null);
							user_details.setRankingProfile(skin.getSkin());
							if(RankingSystem.SQLUpdateUserProfileSkin(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingProfile()) > 0) {
								Hashes.addRanking(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), user_details);
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.USE_SKIN).replace("{}", input)).build()).queue();
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
								logger.error("Selected profile skin {} for user {} couldn't be updated in guild {}", skin.getSkinDescription(), e.getMember().getUser().getId(), e.getGuild().getId());
								RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Profile skin couldn't be updated", "Profile skin update has failed. Skin: "+skin.getSkinDescription());
							}
						}
						else if(inventory.getSkinType().equals("ico")) {
							final String filter = input;
							final var skin = RankingSystem.SQLgetRankingIconsList(e.getGuild().getIdLong()).parallelStream().filter(r -> r.getSkinDescription().equalsIgnoreCase(filter)).findAny().orElse(null);
							user_details.setRankingIcon(skin.getSkin());
							if(RankingSystem.SQLUpdateUserIconSkin(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingIcon()) > 0) {
								Hashes.addRanking(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), user_details);
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.USE_SKIN).replace("{}", input)).build()).queue();
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
								logger.error("Selected icon skin {} for user {} couldn't be updated in guild {}", skin.getSkinDescription(), e.getMember().getUser().getId(), e.getGuild().getId());
								RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Icon skin couldn't be updated", "Icon skin update has failed. Skin: "+skin.getSkinDescription());
							}
						}
						//TODO: throw different message if a used item is still available in inventory but changed from limited use to expiration
						else if(inventory.getSkinType().equals("ite")) {
							var inventoryNumber = RankingSystem.SQLgetInventoryNumber(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), input, "perm");
							var expiration = RankingSystem.SQLgetExpirationFromInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), inventory.getItemID());
							var numberLimit = RankingSystem.SQLgetNumberLimitFromInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), inventory.getItemID());
							long time = System.currentTimeMillis();
							Timestamp timestamp = new Timestamp(time);
							Timestamp timestamp2 = null;
							if(expiration != null)
								timestamp2 = new Timestamp(expiration.getTime()+1000*60*60*24);
							else
								timestamp2 = new Timestamp(time+1000*60*60*24);

							if(inventoryNumber == 1) {
								if(RankingSystem.SQLDeleteAndInsertInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), numberLimit+1, inventory.getItemID(), timestamp, timestamp2) == 0) {
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
									logger.error("Item id {} for the user {} couldn't be used or opened in guild {}", inventory.getItemID(), e.getMember().getUser().getId(), e.getGuild().getId());
									RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Item couldn't be used or activated", "Item use failed. item id: "+inventory.getItemID());
								}
							}
							else {
								if(RankingSystem.SQLUpdateAndInsertInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), inventoryNumber, numberLimit+1, inventory.getItemID(), timestamp, timestamp2) == 0) {
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
									logger.error("Item id {} for the user {} couldn't be used or opened in guild {}", inventory.getItemID(), e.getMember().getUser().getId(), e.getGuild().getId());
									RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Item couldn't be used or activated", "Item use failed. item id: "+inventory.getItemID());
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
		logger.trace("{} has used Use command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}
}
