package commands;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingSystem;
import sql.Azrael;

public class Use implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(IniFileReader.getUseCommand()){
			Logger logger = LoggerFactory.getLogger(Use.class);
			logger.debug("{} has used Use command", e.getMember().getUser().getId());
			
			RankingSystem.SQLgetWholeRankView(e.getMember().getUser().getIdLong());
			rankingSystem.Rank user_details = Hashes.getRanking(e.getMember().getUser().getIdLong());
			if(Hashes.getStatus(e.getGuild().getIdLong()).getRankingState()){
				Azrael.SQLgetChannelID(e.getGuild().getIdLong(), "bot");
				if(e.getTextChannel().getIdLong() == Azrael.getChannelID() || Azrael.getChannelID() == 0){
					String input = e.getMessage().getContentRaw();
					if(input.equals(IniFileReader.getCommandPrefix()+"use")){
						e.getTextChannel().sendMessage("write the description of the item/skin together with this command to use it!\nTo reset your choice use either default-level, default-rank, default-profile or default-icons to reset your settings!").queue();
					}
					else if(input.equals(IniFileReader.getCommandPrefix()+"use default-level")){
						RankingSystem.SQLgetRankingLevel();
						rankingSystem.Rank rank = Hashes.getRankList("ranking-level").parallelStream().filter(r -> r.getLevelDescription().equalsIgnoreCase(Hashes.getStatus(e.getGuild().getIdLong()).getLevelDescription())).findAny().orElse(null);
						user_details.setRankingLevel(rank.getRankingLevel());
						user_details.setLevelDescription(rank.getLevelDescription());
						user_details.setColorRLevel(rank.getColorRLevel());
						user_details.setColorGLevel(rank.getColorGLevel());
						user_details.setColorBLevel(rank.getColorBLevel());
						user_details.setRankXLevel(rank.getRankXLevel());
						user_details.setRankYLevel(rank.getRankYLevel());
						user_details.setRankWidthLevel(rank.getRankWidthLevel());
						user_details.setRankHeightLevel(rank.getRankHeightLevel());
						var editedRows = RankingSystem.SQLUpdateUserLevelSkin(e.getMember().getUser().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingLevel());
						if(editedRows > 0) {
							Hashes.addRanking(e.getMember().getUser().getIdLong(), user_details);
							e.getTextChannel().sendMessage("Level skin has been resetted to the server default skin!").queue();
						}
						else {
							//if rows didn't get updated, throw and error and write it into error log
							e.getTextChannel().sendMessage("Level skin couldn't be resetted to the server default skin. Internal error, please contact an administrator!").queue();
							RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), "Level skin couldn't be resetted", "Resetting to the default skin has failed. Skin: "+user_details.getLevelDescription());
						}
					}
					else if(input.equals(IniFileReader.getCommandPrefix()+"use default-rank")){
						RankingSystem.SQLgetRankingRank();
						rankingSystem.Rank rank = Hashes.getRankList("ranking-rank").parallelStream().filter(r -> r.getRankDescription().equalsIgnoreCase(Hashes.getStatus(e.getGuild().getIdLong()).getRankDescription())).findAny().orElse(null);
						user_details.setRankingRank(rank.getRankingRank());
						user_details.setRankDescription(rank.getRankDescription());
						user_details.setBarColorRank(rank.getBarColorRank());
						user_details.setAdditionalTextRank(rank.getAdditionalTextRank());
						user_details.setColorRRank(rank.getColorRRank());
						user_details.setColorGRank(rank.getColorGRank());
						user_details.setColorBRank(rank.getColorBRank());
						user_details.setRankXRank(rank.getRankXRank());
						user_details.setRankYRank(rank.getRankYRank());
						user_details.setRankWidthRank(rank.getRankWidthRank());
						user_details.setRankHeightRank(rank.getRankHeightRank());
						var editedRows = RankingSystem.SQLUpdateUserRankSkin(e.getMember().getUser().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingRank());
						if(editedRows > 0) {
							Hashes.addRanking(e.getMember().getUser().getIdLong(), user_details);
							e.getTextChannel().sendMessage("Rank skin has been resetted to the server default skin!").queue();
						}
						else {
							//if rows didn't get updated, throw and error and write it into error log
							e.getTextChannel().sendMessage("Rank skin couldn't be resetted to the server default skin. Internal error, please contact an administrator!").queue();
							RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), "Rank skin couldn't be resetted", "Resetting to the default skin has failed. Skin: "+user_details.getRankDescription());
						}
					}
					else if(input.equals(IniFileReader.getCommandPrefix()+"use default-profile")){
						RankingSystem.SQLgetRankingProfile();
						rankingSystem.Rank rank = Hashes.getRankList("ranking-profile").parallelStream().filter(r -> r.getProfileDescription().equalsIgnoreCase(Hashes.getStatus(e.getGuild().getIdLong()).getProfileDescription())).findAny().orElse(null);
						user_details.setRankingProfile(rank.getRankingProfile());
						user_details.setProfileDescription(rank.getProfileDescription());
						user_details.setBarColorProfile(rank.getBarColorProfile());
						user_details.setAdditionalTextProfile(rank.getAdditionalTextProfile());
						user_details.setColorRProfile(rank.getColorRProfile());
						user_details.setColorGProfile(rank.getColorGProfile());
						user_details.setColorBProfile(rank.getColorBProfile());
						user_details.setRankXProfile(rank.getRankXProfile());
						user_details.setRankYProfile(rank.getRankYProfile());
						user_details.setRankWidthProfile(rank.getRankWidthProfile());
						user_details.setRankHeightProfile(rank.getRankHeightProfile());
						var editedRows = RankingSystem.SQLUpdateUserProfileSkin(e.getMember().getUser().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingProfile());
						if(editedRows > 0) {
							Hashes.addRanking(e.getMember().getUser().getIdLong(), user_details);
							e.getTextChannel().sendMessage("Profile skin has been resetted to the server default skin!").queue();
						}
						else {
							//if rows didn't get updated, throw and error and write it into error log
							e.getTextChannel().sendMessage("Profile skin couldn't be resetted to the server default skin. Internal error, please contact an administrator!").queue();
							RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), "Profile skin couldn't be resetted", "Resetting to the default skin has failed. Skin: "+user_details.getProfileDescription());
						}
					}
					else if(input.equals(IniFileReader.getCommandPrefix()+"use default-icons")){
						RankingSystem.SQLgetRankingIcons();
						rankingSystem.Rank rank = Hashes.getRankList("ranking-icons").parallelStream().filter(r -> r.getIconDescription().equalsIgnoreCase(Hashes.getStatus(e.getGuild().getIdLong()).getIconDescription())).findAny().orElse(null);
						user_details.setRankingIcon(rank.getRankingIcon());
						user_details.setIconDescription(rank.getIconDescription());
						var editedRows = RankingSystem.SQLUpdateUserIconSkin(e.getMember().getUser().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingIcon());
						if(editedRows > 0) {
							Hashes.addRanking(e.getMember().getUser().getIdLong(), user_details);
							e.getTextChannel().sendMessage("Icon skins has been resetted to the server default skin!").queue();
						}
						else {
							//if rows didn't get updated, throw and error and write it into error log
							e.getTextChannel().sendMessage("Icons skin couldn't be resetted to the server default skin. Internal error, please contact an administrator!").queue();
							RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), "Icons skin couldn't be resetted", "Resetting to the default skin has failed. Skin: "+user_details.getIconDescription());
						}
					}
					else if(input.contains(IniFileReader.getCommandPrefix()+"use ")){
						input = input.substring(IniFileReader.getCommandPrefix().length()+4);
						RankingSystem.SQLgetItemIDAndSkinType(e.getMember().getUser().getIdLong(), input);
						if(RankingSystem.getItemID() != 0 && RankingSystem.getStatus().equals("perm")){
							if(RankingSystem.getSkinType().equals("lev")){
								final String filter = input;
								RankingSystem.SQLgetRankingLevel();
								rankingSystem.Rank rank = Hashes.getRankList("ranking-level").parallelStream().filter(r -> r.getLevelDescription().equalsIgnoreCase(filter)).findAny().orElse(null);
								user_details.setRankingLevel(rank.getRankingLevel());
								user_details.setLevelDescription(rank.getLevelDescription());
								user_details.setColorRLevel(rank.getColorRLevel());
								user_details.setColorGLevel(rank.getColorGLevel());
								user_details.setColorBLevel(rank.getColorBLevel());
								user_details.setRankXLevel(rank.getRankXLevel());
								user_details.setRankYLevel(rank.getRankYLevel());
								user_details.setRankWidthLevel(rank.getRankWidthLevel());
								user_details.setRankHeightLevel(rank.getRankHeightLevel());
								var editedRows = RankingSystem.SQLUpdateUserLevelSkin(e.getMember().getUser().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingLevel());
								if(editedRows > 0) {
									Hashes.addRanking(e.getMember().getUser().getIdLong(), user_details);
									e.getTextChannel().sendMessage("**"+input+"** will be used from now on!").queue();
								}
								else {
									e.getTextChannel().sendMessage("Level skin couldn't be updated to the selected skin. Internal error, please contact an administrator!").queue();
									RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), "Level skin couldn't be updated", "Level skin update has failed. Skin: "+user_details.getLevelDescription());
								}
							}
							else if(RankingSystem.getSkinType().equals("ran")){
								final String filter = input;
								RankingSystem.SQLgetRankingRank();
								rankingSystem.Rank rank = Hashes.getRankList("ranking-rank").parallelStream().filter(r -> r.getRankDescription().equalsIgnoreCase(filter)).findAny().orElse(null);
								user_details.setRankingRank(rank.getRankingRank());
								user_details.setRankDescription(rank.getRankDescription());
								user_details.setBarColorRank(rank.getBarColorRank());
								user_details.setAdditionalTextRank(rank.getAdditionalTextRank());
								user_details.setColorRRank(rank.getColorRRank());
								user_details.setColorGRank(rank.getColorGRank());
								user_details.setColorBRank(rank.getColorBRank());
								user_details.setRankXRank(rank.getRankXRank());
								user_details.setRankYRank(rank.getRankYRank());
								user_details.setRankWidthRank(rank.getRankWidthRank());
								user_details.setRankHeightRank(rank.getRankHeightRank());
								var editedRows = RankingSystem.SQLUpdateUserRankSkin(e.getMember().getUser().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingRank());
								if(editedRows > 0) {
									Hashes.addRanking(e.getMember().getUser().getIdLong(), user_details);
									e.getTextChannel().sendMessage("**"+input+"** will be used from now on!").queue();
								}
								else {
									e.getTextChannel().sendMessage("Level skin couldn't be updated to the selected skin. Internal error, please contact an administrator!").queue();
									RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), "Rank skin couldn't be updated", "Rank skin update has failed. Skin: "+user_details.getRankDescription());
								}
							}
							else if(RankingSystem.getSkinType().equals("pro")){
								final String filter = input;
								RankingSystem.SQLgetRankingProfile();
								rankingSystem.Rank rank = Hashes.getRankList("ranking-profile").parallelStream().filter(r -> r.getProfileDescription().equalsIgnoreCase(filter)).findAny().orElse(null);
								user_details.setRankingProfile(rank.getRankingProfile());
								user_details.setProfileDescription(rank.getProfileDescription());
								user_details.setBarColorProfile(rank.getBarColorProfile());
								user_details.setAdditionalTextProfile(rank.getAdditionalTextProfile());
								user_details.setColorRProfile(rank.getColorRProfile());
								user_details.setColorGProfile(rank.getColorGProfile());
								user_details.setColorBProfile(rank.getColorBProfile());
								user_details.setRankXProfile(rank.getRankXProfile());
								user_details.setRankYProfile(rank.getRankYProfile());
								user_details.setRankWidthProfile(rank.getRankWidthProfile());
								user_details.setRankHeightProfile(rank.getRankHeightProfile());
								var editedRows = RankingSystem.SQLUpdateUserProfileSkin(e.getMember().getUser().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingProfile());
								if(editedRows > 0) {
									Hashes.addRanking(e.getMember().getUser().getIdLong(), user_details);
									e.getTextChannel().sendMessage("**"+input+"** will be used from now on!").queue();
								}
								else {
									e.getTextChannel().sendMessage("Profile skin couldn't be updated to the selected skin. Internal error, please contact an administrator!").queue();
									RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), "Profile skin couldn't be updated", "Profile skin update has failed. Skin: "+user_details.getProfileDescription());
								}
							}
							else if(RankingSystem.getSkinType().equals("ico")){
								final String filter = input;
								RankingSystem.SQLgetRankingIcons();
								rankingSystem.Rank rank = Hashes.getRankList("ranking-icons").parallelStream().filter(r -> r.getIconDescription().equalsIgnoreCase(filter)).findAny().orElse(null);
								user_details.setRankingIcon(rank.getRankingIcon());
								user_details.setIconDescription(rank.getIconDescription());
								var editedRows = RankingSystem.SQLUpdateUserIconSkin(e.getMember().getUser().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingIcon());
								if(editedRows > 0) {
									Hashes.addRanking(e.getMember().getUser().getIdLong(), user_details);
									e.getTextChannel().sendMessage("**"+input+"** will be used from now on!").queue();
								}
								else {
									e.getTextChannel().sendMessage("Icons skin couldn't be updated to the selected skin. Internal error, please contact an administrator!").queue();
									RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), "Icon skin couldn't be updated", "Icon skin update has failed. Skin: "+user_details.getIconDescription());
								}
							}
							else if(RankingSystem.getSkinType().equals("ite")){
								RankingSystem.SQLgetInventoryAndDescription(e.getMember().getUser().getIdLong(), input, "perm");
								RankingSystem.SQLgetExpirationFromInventory(e.getMember().getUser().getIdLong(), RankingSystem.getItemID());
								RankingSystem.SQLgetNumberLimitFromInventory(e.getMember().getUser().getIdLong(), RankingSystem.getItemID());
								long time = System.currentTimeMillis();
								Timestamp timestamp = new Timestamp(time);
								try {
									Timestamp timestamp2 = new Timestamp(RankingSystem.getExpiration().getTime()+1000*60*60*24);
									if(RankingSystem.getNumber() == 1){
										if(RankingSystem.SQLDeleteAndInsertInventory(e.getMember().getUser().getIdLong(), RankingSystem.getNumberLimit()+1, RankingSystem.getItemID(), timestamp, timestamp2) == 0) {
											e.getTextChannel().sendMessage("Item couldn't be used or activated. Internal error, please contact an administrator!").queue();
											RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), "Item couldn't be used or activated", "Item use failed. item id: "+RankingSystem.getItemID());
										}
									}
									else{
										if(RankingSystem.SQLUpdateAndInsertInventory(e.getMember().getUser().getIdLong(), RankingSystem.getNumber(), RankingSystem.getNumberLimit()+1, RankingSystem.getItemID(), timestamp, timestamp2) == 0) {
											e.getTextChannel().sendMessage("Item couldn't be used or activated. Internal error, please contact an administrator!").queue();
											RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), "Item couldn't be used or activated", "Item use failed. item id: "+RankingSystem.getItemID());
										}
									}
								} catch(NullPointerException npe){
									Timestamp timestamp2 = new Timestamp(time+1000*60*60*24);
									if(RankingSystem.getNumber() == 1){
										if(RankingSystem.SQLDeleteAndInsertInventory(e.getMember().getUser().getIdLong(), RankingSystem.getNumberLimit()+1, RankingSystem.getItemID(), timestamp, timestamp2) == 0) {
											e.getTextChannel().sendMessage("Item couldn't be used or activated. Internal error, please contact an administrator!").queue();
											RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), "Item couldn't be used or activated", "Item use failed. item id: "+RankingSystem.getItemID());
										}
									}
									else{
										if(RankingSystem.SQLUpdateAndInsertInventory(e.getMember().getUser().getIdLong(), RankingSystem.getNumber(), RankingSystem.getNumberLimit()+1, RankingSystem.getItemID(), timestamp, timestamp2) == 0) {
											e.getTextChannel().sendMessage("Item couldn't be used or activated. Internal error, please contact an administrator!").queue();
											RankingSystem.SQLInsertActionLog("high", e.getMember().getUser().getIdLong(), "Item couldn't be used or activated", "Item use failed. item id: "+RankingSystem.getItemID());
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
					e.getTextChannel().sendMessage(e.getMember().getAsMention()+" I'm not allowed to execute commands in this channel, please write it again in <#"+Azrael.getChannelID()+">").queue();
					logger.warn("Use command used in a not bot channel");
				}
			}
			else{
				e.getTextChannel().sendMessage(e.getMember().getAsMention()+" you can't use any item or skin while the ranking system is disabled!").queue();
			}
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		RankingSystem.clearAllVariables();
		Azrael.clearAllVariables();
	}

	@Override
	public String help() {
		return null;
	}

}
