package commands;

import java.sql.Timestamp;

import core.Hashes;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingDB;
import sql.SqlConnect;

public class Use implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(IniFileReader.getUseCommand().equals("true")){
			RankingDB.SQLgetWholeRankView(e.getMember().getUser().getIdLong());
			rankingSystem.Rank user_details = Hashes.getRanking(e.getMember().getUser().getIdLong());
			if(Hashes.getStatus(e.getGuild().getIdLong()).getRankingState()){
				SqlConnect.SQLgetChannelID(e.getGuild().getIdLong(), "bot");
				if(e.getTextChannel().getIdLong() == SqlConnect.getChannelID() || SqlConnect.getChannelID() == 0){
					String input = e.getMessage().getContentRaw();
					if(input.equals(IniFileReader.getCommandPrefix()+"use")){
						e.getTextChannel().sendMessage("write the description of the item/skin together with this command to use it!\nTo reset your choice use either default-level, default-rank, default-profile or default-icons to reset your settings!").queue();
					}
					else if(input.equals(IniFileReader.getCommandPrefix()+"use default-level")){
						RankingDB.SQLgetRankingLevelByDesc(Hashes.getStatus(e.getGuild().getIdLong()).getLevelDescription());
						user_details.setRankingLevel(RankingDB.getLevelSkin());
						user_details.setLevelDescription(RankingDB.getLevelDescription());
						user_details.setColorRLevel(RankingDB.getTextColorRLevel());
						user_details.setColorGLevel(RankingDB.getTextColorGLevel());
						user_details.setColorBLevel(RankingDB.getTextColorBLevel());
						user_details.setRankXLevel(RankingDB.getRankXLevel());
						user_details.setRankYLevel(RankingDB.getRankYLevel());
						user_details.setRankWidthLevel(RankingDB.getRankWidthLevel());
						user_details.setRankHeightLevel(RankingDB.getRankHeightLevel());
						RankingDB.SQLUpdateUserLevelSkin(e.getMember().getUser().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingLevel());
						Hashes.addRanking(e.getMember().getUser().getIdLong(), user_details);
						e.getTextChannel().sendMessage("Level skin has been resetted to the server default skin!").queue();
					}
					else if(input.equals(IniFileReader.getCommandPrefix()+"use default-rank")){
						RankingDB.SQLgetRankingRankByDesc(Hashes.getStatus(e.getGuild().getIdLong()).getRankDescription());
						user_details.setRankingRank(RankingDB.getRankSkin());
						user_details.setRankDescription(RankingDB.getRankDescription());
						user_details.setBarColorRank(RankingDB.getColorRank());
						user_details.setAdditionalTextRank(RankingDB.getExpAndPercentAllowedRank());
						user_details.setColorRRank(RankingDB.getTextColorRRank());
						user_details.setColorGRank(RankingDB.getTextColorGRank());
						user_details.setColorBRank(RankingDB.getTextColorBRank());
						user_details.setRankXRank(RankingDB.getRankXRank());
						user_details.setRankYRank(RankingDB.getRankYRank());
						user_details.setRankWidthRank(RankingDB.getRankWidthRank());
						user_details.setRankHeightRank(RankingDB.getRankHeightRank());
						RankingDB.SQLUpdateUserRankSkin(e.getMember().getUser().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingRank());
						Hashes.addRanking(e.getMember().getUser().getIdLong(), user_details);
						e.getTextChannel().sendMessage("Rank skin has been resetted to the server default skin!").queue();
					}
					else if(input.equals(IniFileReader.getCommandPrefix()+"use default-profile")){
						RankingDB.SQLgetRankingProfileByDesc(Hashes.getStatus(e.getGuild().getIdLong()).getProfileDescription());
						user_details.setRankingProfile(RankingDB.getProfileSkin());
						user_details.setProfileDescription(RankingDB.getProfileDescription());
						user_details.setBarColorProfile(RankingDB.getColorProfile());
						user_details.setAdditionalTextProfile(RankingDB.getExpAndPercentAllowedProfile());
						user_details.setColorRProfile(RankingDB.getTextColorRProfile());
						user_details.setColorGProfile(RankingDB.getTextColorGProfile());
						user_details.setColorBProfile(RankingDB.getTextColorBProfile());
						user_details.setRankXProfile(RankingDB.getRankXProfile());
						user_details.setRankYProfile(RankingDB.getRankYProfile());
						user_details.setRankWidthProfile(RankingDB.getRankWidthProfile());
						user_details.setRankHeightProfile(RankingDB.getRankHeightProfile());
						RankingDB.SQLUpdateUserProfileSkin(e.getMember().getUser().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingProfile());
						Hashes.addRanking(e.getMember().getUser().getIdLong(), user_details);
						e.getTextChannel().sendMessage("Profile skin has been resetted to the server default skin!").queue();
					}
					else if(input.equals(IniFileReader.getCommandPrefix()+"use default-icons")){
						RankingDB.SQLgetRankingIconsByDesc(Hashes.getStatus(e.getGuild().getIdLong()).getIconDescription());
						user_details.setRankingIcon(RankingDB.getIconSkin());
						user_details.setIconDescription(RankingDB.getIconDescription());
						RankingDB.SQLUpdateUserIconSkin(e.getMember().getUser().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), user_details.getRankingIcon());
						Hashes.addRanking(e.getMember().getUser().getIdLong(), user_details);
						e.getTextChannel().sendMessage("Icon skins has been resetted to the server default skin!").queue();
					}
					else if(input.contains(IniFileReader.getCommandPrefix()+"use ")){
						input = input.substring(IniFileReader.getCommandPrefix().length()+4);
						RankingDB.SQLgetItemIDAndSkinType(e.getMember().getUser().getIdLong(), input);
						if(RankingDB.getItemID() != 0 && RankingDB.getStatus().equals("perm")){
							if(RankingDB.getSkinType().equals("lev")){
								RankingDB.SQLgetRankingLevelByDesc(input);
								RankingDB.SQLUpdateUserLevelSkin(e.getMember().getUser().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), RankingDB.getLevelSkin());
								user_details.setRankingLevel(RankingDB.getLevelSkin());
								user_details.setLevelDescription(RankingDB.getLevelDescription());
								user_details.setColorRLevel(RankingDB.getTextColorRLevel());
								user_details.setColorGLevel(RankingDB.getTextColorGLevel());
								user_details.setColorBLevel(RankingDB.getTextColorBLevel());
								user_details.setRankXLevel(RankingDB.getRankXLevel());
								user_details.setRankYLevel(RankingDB.getRankYLevel());
								user_details.setRankWidthLevel(RankingDB.getRankWidthLevel());
								user_details.setRankHeightLevel(RankingDB.getRankHeightLevel());
								Hashes.addRanking(e.getMember().getUser().getIdLong(), user_details);
								e.getTextChannel().sendMessage("**"+input+"** will be used from now on!").queue();
							}
							else if(RankingDB.getSkinType().equals("ran")){
								RankingDB.SQLgetRankingRankByDesc(input);
								RankingDB.SQLUpdateUserRankSkin(e.getMember().getUser().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), RankingDB.getRankSkin());
								user_details.setRankingRank(RankingDB.getRankSkin());
								user_details.setRankDescription(RankingDB.getRankDescription());
								user_details.setBarColorRank(RankingDB.getColorRank());
								user_details.setAdditionalTextRank(RankingDB.getExpAndPercentAllowedRank());
								user_details.setColorRRank(RankingDB.getTextColorRRank());
								user_details.setColorGRank(RankingDB.getTextColorGRank());
								user_details.setColorBRank(RankingDB.getTextColorBRank());
								user_details.setRankXRank(RankingDB.getRankXRank());
								user_details.setRankYRank(RankingDB.getRankYRank());
								user_details.setRankWidthRank(RankingDB.getRankWidthRank());
								user_details.setRankHeightRank(RankingDB.getRankHeightRank());
								Hashes.addRanking(e.getMember().getUser().getIdLong(), user_details);
								e.getTextChannel().sendMessage("**"+input+"** will be used from now on!").queue();
							}
							else if(RankingDB.getSkinType().equals("pro")){
								RankingDB.SQLgetRankingProfileByDesc(input);
								RankingDB.SQLUpdateUserProfileSkin(e.getMember().getUser().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), RankingDB.getProfileSkin());
								user_details.setRankingProfile(RankingDB.getProfileSkin());
								user_details.setProfileDescription(RankingDB.getProfileDescription());
								user_details.setBarColorProfile(RankingDB.getColorProfile());
								user_details.setAdditionalTextProfile(RankingDB.getExpAndPercentAllowedProfile());
								user_details.setColorRProfile(RankingDB.getTextColorRProfile());
								user_details.setColorGProfile(RankingDB.getTextColorGProfile());
								user_details.setColorBProfile(RankingDB.getTextColorBProfile());
								user_details.setRankXProfile(RankingDB.getRankXProfile());
								user_details.setRankYProfile(RankingDB.getRankYProfile());
								user_details.setRankWidthProfile(RankingDB.getRankWidthProfile());
								user_details.setRankHeightProfile(RankingDB.getRankHeightProfile());
								Hashes.addRanking(e.getMember().getUser().getIdLong(), user_details);
								e.getTextChannel().sendMessage("**"+input+"** will be used from now on!").queue();
							}
							else if(RankingDB.getSkinType().equals("ico")){
								RankingDB.SQLgetRankingIconsByDesc(input);
								RankingDB.SQLUpdateUserIconSkin(e.getMember().getUser().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), RankingDB.getIconSkin());
								user_details.setRankingIcon(RankingDB.getIconSkin());
								user_details.setIconDescription(RankingDB.getIconDescription());
								Hashes.addRanking(e.getMember().getUser().getIdLong(), user_details);
								e.getTextChannel().sendMessage("**"+input+"** will be used from now on!").queue();
							}
							else if(RankingDB.getSkinType().equals("ite")){
								RankingDB.SQLgetInventoryAndDescription(e.getMember().getUser().getIdLong(), input, "perm");
								RankingDB.SQLgetExpirationFromInventory(e.getMember().getUser().getIdLong(), RankingDB.getItemID());
								RankingDB.SQLgetNumberLimitFromInventory(e.getMember().getUser().getIdLong(), RankingDB.getItemID());
								long time = System.currentTimeMillis();
								Timestamp timestamp = new Timestamp(time);
								try {
									Timestamp timestamp2 = new Timestamp(RankingDB.getExpiration().getTime()+1000*60*60*24);
									if(RankingDB.getNumber() == 1){
										RankingDB.SQLDeleteAndInsertInventory(e.getMember().getUser().getIdLong(), RankingDB.getNumberLimit()+1, RankingDB.getItemID(), timestamp, timestamp2);
									}
									else{
										RankingDB.SQLUpdateAndInsertInventory(e.getMember().getUser().getIdLong(), RankingDB.getNumber(), RankingDB.getNumberLimit()+1, RankingDB.getItemID(), timestamp, timestamp2);
									}
								} catch(NullPointerException npe){
									Timestamp timestamp2 = new Timestamp(time+1000*60*60*24);
									if(RankingDB.getNumber() == 1){
										RankingDB.SQLDeleteAndInsertInventory(e.getMember().getUser().getIdLong(), RankingDB.getNumberLimit()+1, RankingDB.getItemID(), timestamp, timestamp2);
									}
									else{
										RankingDB.SQLUpdateAndInsertInventory(e.getMember().getUser().getIdLong(), RankingDB.getNumber(), RankingDB.getNumberLimit()+1, RankingDB.getItemID(), timestamp, timestamp2);
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
					e.getTextChannel().sendMessage(e.getMember().getAsMention()+" I'm not allowed to execute commands in this channel, please write it again in <#"+SqlConnect.getChannelID()+">").queue();
				}
			}
			else{
				e.getTextChannel().sendMessage(e.getMember().getAsMention()+" you can't use any item or skin while the ranking system is disabled!").queue();
			}
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		RankingDB.clearAllVariables();
		SqlConnect.clearAllVariables();
	}

	@Override
	public String help() {
		return null;
	}

}
