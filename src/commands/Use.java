package commands;

import java.awt.Color;
import java.sql.Timestamp;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Guilds;
import core.Hashes;
import core.UserPrivs;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingSystem;
import util.STATIC;
import sql.Azrael;

public class Use implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(GuildIni.getUseCommand(e.getGuild().getIdLong())){
			Logger logger = LoggerFactory.getLogger(Use.class);
			logger.debug("{} has used Use command", e.getMember().getUser().getId());
			final var commandLevel = GuildIni.getUseLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong()) {
				Guilds guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
				constructors.Rank user_details = RankingSystem.SQLgetWholeRankView(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), guild_settings.getThemeID());
				if(guild_settings.getRankingState()){
					var bot_channels = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type().equals("bot")).collect(Collectors.toList());
					if(bot_channels.size() == 0 || bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getTextChannel().getIdLong()).findAny().orElse(null) != null) {
						String input = e.getMessage().getContentRaw();
						final String prefix = GuildIni.getCommandPrefix(e.getGuild().getIdLong());
						if(args.length == 0){
							e.getTextChannel().sendMessage("write the description of the item/skin together with this command to use it!\nTo reset your choice use either default-level, default-rank, default-profile or default-icons to reset your settings!").queue();
						}
						else if(args[0].equalsIgnoreCase("default-level")){
							constructors.Rank rank = RankingSystem.SQLgetRankingLevel().parallelStream().filter(r -> r.getLevelDescription().equalsIgnoreCase(guild_settings.getLevelDescription()) && r.getThemeID() == guild_settings.getThemeID()).findAny().orElse(null);
							user_details.setRankingLevel(rank.getRankingLevel());
							user_details.setLevelDescription(rank.getLevelDescription());
							user_details.setColorRLevel(rank.getColorRLevel());
							user_details.setColorGLevel(rank.getColorGLevel());
							user_details.setColorBLevel(rank.getColorBLevel());
							user_details.setRankXLevel(rank.getRankXLevel());
							user_details.setRankYLevel(rank.getRankYLevel());
							user_details.setRankWidthLevel(rank.getRankWidthLevel());
							user_details.setRankHeightLevel(rank.getRankHeightLevel());
							if(user_details.getRankingLevel() != 0) {
								if(RankingSystem.SQLUpdateUserLevelSkin(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingLevel()) > 0) {
									Hashes.addRanking(e.getGuild().getId()+"_"+e.getMember().getUser().getIdLong(), user_details);
									e.getTextChannel().sendMessage("Level skin has been resetted to **"+user_details.getLevelDescription()+"**!").queue();
								}
								else {
									//if rows didn't get updated, throw and error and write it into error log
									e.getTextChannel().sendMessage("Level skin couldn't be resetted to the server default skin. Internal error, please contact an administrator!").queue();
									logger.error("RankingSystem.users table couldn't be updated with the default level skin for {}", e.getMember().getUser().getId());
									RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Level skin couldn't be resetted", "Resetting to the default skin has failed. Skin: "+user_details.getLevelDescription());
								}
							}
							else {
								EmbedBuilder error = new EmbedBuilder().setColor(Color.RED).setTitle("An error occured!");
								e.getTextChannel().sendMessage(error.setDescription("Default skins aren't defined. Please contact an administrator!").build()).queue();
								logger.error("Default skins in RankingSystem.guilds are not defined for guild {}", e.getGuild().getName());
							}
						}
						else if(args[0].equalsIgnoreCase("default-rank")){
							constructors.Rank rank = RankingSystem.SQLgetRankingRank().parallelStream().filter(r -> r.getRankDescription().equalsIgnoreCase(guild_settings.getRankDescription()) && r.getThemeID() == guild_settings.getThemeID()).findAny().orElse(null);
							user_details.setRankingRank(rank.getRankingRank());
							user_details.setRankDescription(rank.getRankDescription());
							user_details.setBarColorRank(rank.getBarColorRank());
							user_details.setAdditionalExpTextRank(rank.getAdditionalExpTextRank());
							user_details.setAdditionalPercentTextRank(rank.getAdditionalPercentTextRank());
							user_details.setColorRRank(rank.getColorRRank());
							user_details.setColorGRank(rank.getColorGRank());
							user_details.setColorBRank(rank.getColorBRank());
							user_details.setRankXRank(rank.getRankXRank());
							user_details.setRankYRank(rank.getRankYRank());
							user_details.setRankWidthRank(rank.getRankWidthRank());
							user_details.setRankHeightRank(rank.getRankHeightRank());
							if(user_details.getRankingRank() != 0) {
								if(RankingSystem.SQLUpdateUserRankSkin(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingRank()) > 0) {
									Hashes.addRanking(e.getGuild().getId()+"_"+e.getMember().getUser().getIdLong(), user_details);
									e.getTextChannel().sendMessage("Rank skin has been resetted to **"+user_details.getRankDescription()+"**!").queue();
								}
								else {
									//if rows didn't get updated, throw and error and write it into error log
									e.getTextChannel().sendMessage("Rank skin couldn't be resetted to the server default skin. Internal error, please contact an administrator!").queue();
									logger.error("RankingSystem.users table couldn't be updated with the default rank skin for {}", e.getMember().getUser().getId());
									RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Rank skin couldn't be resetted", "Resetting to the default skin has failed. Skin: "+user_details.getRankDescription());
								}
							}
							else {
								EmbedBuilder error = new EmbedBuilder().setColor(Color.RED).setTitle("An error occured!");
								e.getTextChannel().sendMessage(error.setDescription("Default skins aren't defined. Please contact an administrator!").build()).queue();
								logger.error("Default skins in RankingSystem.guilds are not defined for guild {}", e.getGuild().getName());
							}
						}
						else if(args[0].equalsIgnoreCase("default-profile")){
							constructors.Rank rank = RankingSystem.SQLgetRankingProfile().parallelStream().filter(r -> r.getProfileDescription().equalsIgnoreCase(guild_settings.getProfileDescription()) && r.getThemeID() == guild_settings.getThemeID()).findAny().orElse(null);
							user_details.setRankingProfile(rank.getRankingProfile());
							user_details.setProfileDescription(rank.getProfileDescription());
							user_details.setBarColorProfile(rank.getBarColorProfile());
							user_details.setAdditionalExpTextProfile(rank.getAdditionalExpTextProfile());
							user_details.setAdditionalPercentTextProfile(rank.getAdditionalPercentTextProfile());
							user_details.setColorRProfile(rank.getColorRProfile());
							user_details.setColorGProfile(rank.getColorGProfile());
							user_details.setColorBProfile(rank.getColorBProfile());
							user_details.setRankXProfile(rank.getRankXProfile());
							user_details.setRankYProfile(rank.getRankYProfile());
							user_details.setRankWidthProfile(rank.getRankWidthProfile());
							user_details.setRankHeightProfile(rank.getRankHeightProfile());
							if(user_details.getRankingProfile() != 0) {
								if(RankingSystem.SQLUpdateUserProfileSkin(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingProfile()) > 0) {
									Hashes.addRanking(e.getGuild().getId()+"_"+e.getMember().getUser().getIdLong(), user_details);
									e.getTextChannel().sendMessage("Profile skin has been resetted to **"+user_details.getProfileDescription()+"**!").queue();
								}
								else {
									//if rows didn't get updated, throw and error and write it into error log
									e.getTextChannel().sendMessage("Profile skin couldn't be resetted to the server default skin. Internal error, please contact an administrator!").queue();
									logger.error("RankingSystem.users table couldn't be updated with the default profile skin for {}", e.getMember().getUser().getId());
									RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Profile skin couldn't be resetted", "Resetting to the default skin has failed. Skin: "+user_details.getProfileDescription());
								}
							}
							else {
								EmbedBuilder error = new EmbedBuilder().setColor(Color.RED).setTitle("An error occured!");
								e.getTextChannel().sendMessage(error.setDescription("Default skins aren't defined. Please contact an administrator!").build()).queue();
								logger.error("Default skins in RankingSystem.guilds are not defined for guild {}", e.getGuild().getName());
							}
						}
						else if(args[0].equalsIgnoreCase("default-icons")){
							constructors.Rank rank = RankingSystem.SQLgetRankingIcons().parallelStream().filter(r -> r.getIconDescription().equalsIgnoreCase(guild_settings.getIconDescription()) && r.getThemeID() == guild_settings.getThemeID()).findAny().orElse(null);
							user_details.setRankingIcon(rank.getRankingIcon());
							user_details.setIconDescription(rank.getIconDescription());
							if(user_details.getRankingIcon() != 0) {
								if(RankingSystem.SQLUpdateUserIconSkin(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingIcon()) > 0) {
									Hashes.addRanking(e.getGuild().getId()+"_"+e.getMember().getUser().getIdLong(), user_details);
									e.getTextChannel().sendMessage("Icon skins has been resetted to **"+user_details.getIconDescription()+"**!").queue();
								}
								else {
									//if rows didn't get updated, throw and error and write it into error log
									e.getTextChannel().sendMessage("Icons skin couldn't be resetted to the server default skin. Internal error, please contact an administrator!").queue();
									logger.error("RankingSystem.users table couldn't be updated with the default icon skin for {}", e.getMember().getUser().getId());
									RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Icons skin couldn't be resetted", "Resetting to the default skin has failed. Skin: "+user_details.getIconDescription());
								}
							}
							else {
								EmbedBuilder error = new EmbedBuilder().setColor(Color.RED).setTitle("An error occured!");
								e.getTextChannel().sendMessage(error.setDescription("Default skins aren't defined. Please contact an administrator!").build()).queue();
								logger.error("Default skins in RankingSystem.guilds are not defined for guild {}", e.getGuild().getName());
							}
						}
						else {
							input = input.substring(prefix.length()+4);
							constructors.Inventory inventory = RankingSystem.SQLgetItemIDAndSkinType(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), input, guild_settings.getThemeID());
							if(inventory != null && inventory.getItemID() != 0 && inventory.getStatus().equals("perm")){
								if(inventory.getSkinType().equals("lev")){
									final String filter = input;
									constructors.Rank rank = RankingSystem.SQLgetRankingLevel().parallelStream().filter(r -> r.getLevelDescription().equalsIgnoreCase(filter)).findAny().orElse(null);
									user_details.setRankingLevel(rank.getRankingLevel());
									user_details.setLevelDescription(rank.getLevelDescription());
									user_details.setColorRLevel(rank.getColorRLevel());
									user_details.setColorGLevel(rank.getColorGLevel());
									user_details.setColorBLevel(rank.getColorBLevel());
									user_details.setRankXLevel(rank.getRankXLevel());
									user_details.setRankYLevel(rank.getRankYLevel());
									user_details.setRankWidthLevel(rank.getRankWidthLevel());
									user_details.setRankHeightLevel(rank.getRankHeightLevel());
									if(RankingSystem.SQLUpdateUserLevelSkin(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingLevel()) > 0) {
										Hashes.addRanking(e.getGuild().getId()+"_"+e.getMember().getUser().getIdLong(), user_details);
										e.getTextChannel().sendMessage("**"+input+"** will be used from now on!").queue();
									}
									else {
										e.getTextChannel().sendMessage("Level skin couldn't be updated to the selected skin. Internal error, please contact an administrator!").queue();
										logger.error("RankingSystem.users table couldn't be updated with the selected level skin {} for {}", user_details.getLevelDescription(), e.getMember().getUser().getId());
										RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Level skin couldn't be updated", "Level skin update has failed. Skin: "+user_details.getLevelDescription());
									}
								}
								else if(inventory.getSkinType().equals("ran")){
									final String filter = input;
									constructors.Rank rank = RankingSystem.SQLgetRankingRank().parallelStream().filter(r -> r.getRankDescription().equalsIgnoreCase(filter)).findAny().orElse(null);
									user_details.setRankingRank(rank.getRankingRank());
									user_details.setRankDescription(rank.getRankDescription());
									user_details.setBarColorRank(rank.getBarColorRank());
									user_details.setAdditionalExpTextRank(rank.getAdditionalExpTextRank());
									user_details.setAdditionalPercentTextRank(rank.getAdditionalPercentTextRank());
									user_details.setColorRRank(rank.getColorRRank());
									user_details.setColorGRank(rank.getColorGRank());
									user_details.setColorBRank(rank.getColorBRank());
									user_details.setRankXRank(rank.getRankXRank());
									user_details.setRankYRank(rank.getRankYRank());
									user_details.setRankWidthRank(rank.getRankWidthRank());
									user_details.setRankHeightRank(rank.getRankHeightRank());
									if(RankingSystem.SQLUpdateUserRankSkin(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingRank()) > 0) {
										Hashes.addRanking(e.getGuild().getId()+"_"+e.getMember().getUser().getIdLong(), user_details);
										e.getTextChannel().sendMessage("**"+input+"** will be used from now on!").queue();
									}
									else {
										e.getTextChannel().sendMessage("Level skin couldn't be updated to the selected skin. Internal error, please contact an administrator!").queue();
										logger.error("RankingSystem.users table couldn't be updated with the selected rank skin {} for {}", user_details.getRankDescription(), e.getMember().getUser().getId());
										RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Rank skin couldn't be updated", "Rank skin update has failed. Skin: "+user_details.getRankDescription());
									}
								}
								else if(inventory.getSkinType().equals("pro")){
									final String filter = input;
									constructors.Rank rank = RankingSystem.SQLgetRankingProfile().parallelStream().filter(r -> r.getProfileDescription().equalsIgnoreCase(filter)).findAny().orElse(null);
									user_details.setRankingProfile(rank.getRankingProfile());
									user_details.setProfileDescription(rank.getProfileDescription());
									user_details.setBarColorProfile(rank.getBarColorProfile());
									user_details.setAdditionalExpTextProfile(rank.getAdditionalExpTextProfile());
									user_details.setAdditionalPercentTextProfile(rank.getAdditionalPercentTextProfile());
									user_details.setColorRProfile(rank.getColorRProfile());
									user_details.setColorGProfile(rank.getColorGProfile());
									user_details.setColorBProfile(rank.getColorBProfile());
									user_details.setRankXProfile(rank.getRankXProfile());
									user_details.setRankYProfile(rank.getRankYProfile());
									user_details.setRankWidthProfile(rank.getRankWidthProfile());
									user_details.setRankHeightProfile(rank.getRankHeightProfile());
									if(RankingSystem.SQLUpdateUserProfileSkin(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingProfile()) > 0) {
										Hashes.addRanking(e.getGuild().getId()+"_"+e.getMember().getUser().getIdLong(), user_details);
										e.getTextChannel().sendMessage("**"+input+"** will be used from now on!").queue();
									}
									else {
										e.getTextChannel().sendMessage("Profile skin couldn't be updated to the selected skin. Internal error, please contact an administrator!").queue();
										logger.error("RankingSystem.users table couldn't be updated with the selected profile skin {} for {}", user_details.getProfileDescription(), e.getMember().getUser().getId());
										RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Profile skin couldn't be updated", "Profile skin update has failed. Skin: "+user_details.getProfileDescription());
									}
								}
								else if(inventory.getSkinType().equals("ico")){
									final String filter = input;
									constructors.Rank rank = RankingSystem.SQLgetRankingIcons().parallelStream().filter(r -> r.getIconDescription().equalsIgnoreCase(filter)).findAny().orElse(null);
									user_details.setRankingIcon(rank.getRankingIcon());
									user_details.setIconDescription(rank.getIconDescription());
									if(RankingSystem.SQLUpdateUserIconSkin(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingIcon()) > 0) {
										Hashes.addRanking(e.getGuild().getId()+"_"+e.getMember().getUser().getIdLong(), user_details);
										e.getTextChannel().sendMessage("**"+input+"** will be used from now on!").queue();
									}
									else {
										e.getTextChannel().sendMessage("Icons skin couldn't be updated to the selected skin. Internal error, please contact an administrator!").queue();
										logger.error("RankingSystem.users table couldn't be updated with the selected icons skin {} for {}", user_details.getIconDescription(), e.getMember().getUser().getId());
										RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Icon skin couldn't be updated", "Icon skin update has failed. Skin: "+user_details.getIconDescription());
									}
								}
								else if(inventory.getSkinType().equals("ite")){
									var inventoryNumber = RankingSystem.SQLgetInventoryNumber(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), input, "perm", guild_settings.getThemeID());
									var expiration = RankingSystem.SQLgetExpirationFromInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), inventory.getItemID(), guild_settings.getThemeID());
									var numberLimit = RankingSystem.SQLgetNumberLimitFromInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), inventory.getItemID(), guild_settings.getThemeID());
									long time = System.currentTimeMillis();
									Timestamp timestamp = new Timestamp(time);
									try {
										Timestamp timestamp2 = new Timestamp(expiration.getTime()+1000*60*60*24);
										if(inventoryNumber == 1){
											if(RankingSystem.SQLDeleteAndInsertInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), numberLimit+1, inventory.getItemID(), timestamp, timestamp2, guild_settings.getThemeID()) == 0) {
												e.getTextChannel().sendMessage("Item couldn't be used or activated. Internal error, please contact an administrator!").queue();
												logger.error("Item id {} for the user {} couldn't be used or opened", inventory.getItemID(), e.getMember().getUser().getId());
												RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Item couldn't be used or activated", "Item use failed. item id: "+inventory.getItemID());
											}
										}
										else{
											if(RankingSystem.SQLUpdateAndInsertInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), inventoryNumber, numberLimit+1, inventory.getItemID(), timestamp, timestamp2, guild_settings.getThemeID()) == 0) {
												e.getTextChannel().sendMessage("Item couldn't be used or activated. Internal error, please contact an administrator!").queue();
												logger.error("Item id {} for the user {} couldn't be used or opened", inventory.getItemID(), e.getMember().getUser().getId());
												RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Item couldn't be used or activated", "Item use failed. item id: "+inventory.getItemID());
											}
										}
									} catch(NullPointerException npe){
										Timestamp timestamp2 = new Timestamp(time+1000*60*60*24);
										if(inventoryNumber == 1){
											if(RankingSystem.SQLDeleteAndInsertInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), numberLimit+1, inventory.getItemID(), timestamp, timestamp2, guild_settings.getThemeID()) == 0) {
												e.getTextChannel().sendMessage("Item couldn't be used or activated. Internal error, please contact an administrator!").queue();
												logger.error("Item id {} for the user {} couldn't be used or opened", inventory.getItemID(), e.getMember().getUser().getId());
												RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Item couldn't be used or activated", "Item use failed. item id: "+inventory.getItemID());
											}
										}
										else{
											if(RankingSystem.SQLUpdateAndInsertInventory(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), inventoryNumber, numberLimit+1, inventory.getItemID(), timestamp, timestamp2, guild_settings.getThemeID()) == 0) {
												e.getTextChannel().sendMessage("Item couldn't be used or activated. Internal error, please contact an administrator!").queue();
												logger.error("Item id {} for the user {} couldn't be used or opened", inventory.getItemID(), e.getMember().getUser().getId());
												RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), "Item couldn't be used or activated", "Item use failed. item id: "+inventory.getItemID());
											}
										}
									}
									e.getTextChannel().sendMessage("**"+input+"** has been opened!").queue();
								}
							}
							else{
								e.getTextChannel().sendMessage(e.getMember().getAsMention()+" you don't have an item that goes by that name in your inventory... To use default skins look up the usage of this command for the default skins!").queue();
							}
						}
					}
					else{
						e.getTextChannel().sendMessage(e.getMember().getAsMention()+" I'm not allowed to execute commands in this channel, please write it again in "+STATIC.getChannels(bot_channels)).queue();
						logger.warn("Use command used in a not bot channel");
					}
				}
				else{
					e.getTextChannel().sendMessage(e.getMember().getAsMention()+" you can't use any item or skin while the ranking system is disabled!").queue();
				}
			}
			else {
				EmbedBuilder message = new EmbedBuilder();
				e.getTextChannel().sendMessage(message.setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(commandLevel, e.getGuild())).build()).queue();
			}
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
