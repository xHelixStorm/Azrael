package commands;

import java.sql.Timestamp;

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
			RankingDB.SQLgetUserUserDetailsRanking(e.getMember().getUser().getIdLong());
			RankingDB.SQLgetGuild(e.getGuild().getIdLong());
			if(RankingDB.getRankingState() == true){
				SqlConnect.SQLgetChannelID(e.getGuild().getIdLong(), "bot");
				if(e.getTextChannel().getIdLong() == SqlConnect.getChannelID()){
					RankingDB.SQLgetGuild(e.getGuild().getIdLong());
					String input = e.getMessage().getContentRaw();
					if(input.equals(IniFileReader.getCommandPrefix()+"use")){
						e.getTextChannel().sendMessage("write the description of the item/skin together with this command to use it!\nTo reset your choice use either default-level, default-rank, default-profile or default-icons to reset your settings!").queue();
					}
					else if(input.equals(IniFileReader.getCommandPrefix()+"use default-level")){
						//to reset the level skin from guild!
						RankingDB.SQLUpdateUserLevelSkin(e.getMember().getUser().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), RankingDB.getRankingLevel());
						e.getTextChannel().sendMessage("Level skin has been resetted to the server default skin!").queue();
					}
					else if(input.equals(IniFileReader.getCommandPrefix()+"use default-rank")){
						//to reset the rank skin from guild!
						RankingDB.SQLUpdateUserRankSkin(e.getMember().getUser().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), RankingDB.getRankingRank());
						e.getTextChannel().sendMessage("Rank skin has been resetted to the server default skin!").queue();
					}
					else if(input.equals(IniFileReader.getCommandPrefix()+"use default-profile")){
						RankingDB.SQLUpdateUserProfileSkin(e.getMember().getUser().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), RankingDB.getRankingProfile());
						e.getTextChannel().sendMessage("Profile skin has been resetted to the server default skin!").queue();
					}
					else if(input.equals(IniFileReader.getCommandPrefix()+"use default-icons")){
						RankingDB.SQLUpdateUserIconSkin(e.getMember().getUser().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), RankingDB.getRankingIcon());
						e.getTextChannel().sendMessage("Icon skins has been resetted to the server default skin!").queue();
					}
					else if(input.contains(IniFileReader.getCommandPrefix()+"use ")){
						input = input.substring(IniFileReader.getCommandPrefix().length()+4);
						RankingDB.SQLgetItemIDAndSkinType(e.getMember().getUser().getIdLong(), input);
						if(RankingDB.getItemID() != 0 && RankingDB.getStatus().equals("perm")){
							if(RankingDB.getSkinType().equals("lev")){
								RankingDB.SQLgetRankingLevelID(input);
								RankingDB.SQLUpdateUserLevelSkin(e.getMember().getUser().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), RankingDB.getLevelSkin());
								e.getTextChannel().sendMessage("**"+input+"** will be used from now on!").queue();
							}
							else if(RankingDB.getSkinType().equals("ran")){
								RankingDB.SQLgetRankingRankID(input);
								RankingDB.SQLUpdateUserRankSkin(e.getMember().getUser().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), RankingDB.getRankSkin());
								e.getTextChannel().sendMessage("**"+input+"** will be used from now on!").queue();
							}
							else if(RankingDB.getSkinType().equals("pro")){
								RankingDB.SQLgetRankingProfileID(input);
								RankingDB.SQLUpdateUserProfileSkin(e.getMember().getUser().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), RankingDB.getProfileSkin());
								e.getTextChannel().sendMessage("**"+input+"** will be used from now on!").queue();
							}
							else if(RankingDB.getSkinType().equals("ico")){
								RankingDB.SQLgetRankingIconsID(input);
								RankingDB.SQLUpdateUserIconSkin(e.getMember().getUser().getIdLong(), e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator(), RankingDB.getIconSkin());
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
