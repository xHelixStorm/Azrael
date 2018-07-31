package commands;

import java.util.ConcurrentModificationException;

import fileManagement.IniFileReader;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingDB;
import sql.SqlConnect;

public class Top implements Command{
	
	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(IniFileReader.getTopCommand().equals("true")){
			String command = e.getMessage().getContentRaw();
			long member_id = e.getMember().getUser().getIdLong();
			int rank = 0;
			StringBuilder message = new StringBuilder();
			long guild_id = e.getGuild().getIdLong();
			long channel = e.getTextChannel().getIdLong();
			long channel_id;
			String name;
			int level;
			long experience;
			long user_experience = 0;
			int user_level = 0;
			int i = 0;
			int page = 0;
			boolean runTopList = false;
			
			if(command.equals(IniFileReader.getCommandPrefix()+"top")){
				page = 1;
				runTopList = true;
			}
			else if(command.contains("H!top -page ")){
				page = Integer.parseInt(command.substring(12));
				if(page < 1){page = 1;}
				runTopList = true;
			}
			else if(command.equals("H!top -help")){
				e.getTextChannel().sendMessage("```To use this command, type H!top to show the top 10 ranking in this server.\nTo display other pages use H!top -page x.\nNote that it can't display pages where 10 users aren't listed!```").queue();
			}
			else{
				e.getTextChannel().sendMessage("Please type **H!top -help** to display the command usage!").queue();
			}
			
			SqlConnect.SQLgetChannelID(guild_id, "bot");
			channel_id = SqlConnect.getChannelID();
			
			if(runTopList == true){
				if(channel_id == channel){
					RankingDB.SQLRanking();
					search: for(rankingSystem.Rank ranking1 : RankingDB.getRankList()){
						if(member_id == ranking1.getUser_id()){
							rank = ranking1.getRank();
							user_experience = ranking1.getExperience();
							user_level = ranking1.getLevel();
							break search;
						}
					}
					
					RankingDB.clearArrayList();
					RankingDB.SQLTopRanking(page);
					if(RankingDB.getRankList().size() < 10){
						e.getTextChannel().sendMessage("There aren't at least 10 people on this page and hence it can not be displayed!").queue();
					}
					else{
						for(rankingSystem.Rank ranking : RankingDB.getRankList()){
							i = i + 1;
							try {
								name = e.getGuild().getMemberById(ranking.getUser_id()).getUser().getName();
							} catch (NullPointerException | ConcurrentModificationException e1){
								name = "'user has left the guild'";
							}
							level = ranking.getLevel();
							experience = ranking.getExperience();				
							if(i == 10){
								message.append("["+(ranking.getRank()+(page-1)*10)+"]\t> #"+name+"\n\t\t\t Level: "+level+"\t Experience: "+experience+"\n");
								e.getTextChannel().sendMessage("```CMake\nRanking | User\n\n"+message.toString()+"\n"
										+ "-------------------------------------\n #Personal information\n"
										+ " Rank: "+rank+"\t Level: "+user_level+"\t Experience: "+user_experience+"\n\n \t\t\t<Page "+page+">```").queue();
							}
							else{
								message.append("["+(ranking.getRank()+(page-1)*10)+"] \t> #"+name+"\n\t\t\t Level: "+level+"\t Experience: "+experience+"\n");
							}
						}
					}
					RankingDB.clearArrayList();
				}
				else{
					e.getTextChannel().sendMessage("Apologies young padawan but I'm not allowed to execute this command in this channel. Please retry in <#"+channel_id+">").queue();
				}
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
