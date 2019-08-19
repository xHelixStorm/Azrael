package commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.UserPrivs;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sql.RankingSystem;
import util.STATIC;
import sql.Azrael;

public class Top implements Command{
	
	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(GuildIni.getTopCommand(e.getGuild().getIdLong())){
			Logger logger = LoggerFactory.getLogger(Top.class);
			logger.debug("{} has used Top command", e.getMember().getUser().getId());
			final var commandLevel = GuildIni.getTopLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong()) {
				long member_id = e.getMember().getUser().getIdLong();
				int rank = 0;
				StringBuilder message = new StringBuilder();
				long guild_id = e.getGuild().getIdLong();
				String name;
				int level;
				long experience;
				long user_experience = 0;
				int user_level = 0;
				int i = 0;
				int page = 0;
				boolean runTopList = false;
				
				if(args.length == 0){
					page = 1;
					runTopList = true;
				}
				else if(args.length > 1 && args[0].equalsIgnoreCase("-page")) {
					try {
						page = Integer.parseInt(args[1]);
						if(page < 1){page = 1;}
					} catch(NumberFormatException nfe) {
						page = 1;
					}
					runTopList = true;
				}
				else if(args[0].equalsIgnoreCase("-help")){
					e.getTextChannel().sendMessage("```To use this command, type H!top to show the top 10 ranking in this server.\nTo display other pages use H!top -page x.\nNote that it can't display pages where 10 users aren't listed!```").queue();
				}
				else{
					e.getTextChannel().sendMessage("Please type **H!top -help** to display the command usage!").queue();
				}
							
				if(runTopList == true){
					var bot_channels = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("bot")).collect(Collectors.toList());
					if(bot_channels.size() == 0 || bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getTextChannel().getIdLong()).findAny().orElse(null) != null) {
						ArrayList<constructors.Rank> rankList = RankingSystem.SQLRanking(guild_id);
						constructors.Rank ranking1 = rankList.parallelStream().filter(r -> r.getUser_ID() == member_id).findAny().orElse(null);
						rank = ranking1.getRank();
						user_experience = ranking1.getExperience();
						user_level = ranking1.getLevel();
						
						try {
							//try to get the last entry of the page. if entry doesn't exist jump to catch clause
							rankList.get(((page-1)*10)+9);
							
							//display the top ten of the current page
							for(int iterate = (page-1)*10; iterate < page*10; iterate++) {
								constructors.Rank ranking = rankList.get(iterate);
								i = i + 1;
								try {
									name = e.getGuild().getMemberById(ranking.getUser_ID()).getUser().getName();
								} catch (NullPointerException | ConcurrentModificationException e1){
									name = "'user has left the guild'";
								}
								level = ranking.getLevel();
								experience = ranking.getExperience();				
								if(i == 10){
									message.append("["+ranking.getRank()+"]\t> #"+name+"\n\t\t\t Level: "+level+"\t Experience: "+experience+"\n");
									e.getTextChannel().sendMessage("```CMake\nRanking | User\n\n"+message.toString()+"\n"
											+ "-------------------------------------\n #Personal information\n"
											+ " Rank: "+rank+"\t Level: "+user_level+"\t Experience: "+user_experience+"\n\n \t\t\t<Page "+page+">```").queue();
								}
								else{
									message.append("["+ranking.getRank()+"] \t> #"+name+"\n\t\t\t Level: "+level+"\t Experience: "+experience+"\n");
								}
							}
						} catch(IndexOutOfBoundsException ioobe) {
							e.getTextChannel().sendMessage("There aren't at least 10 people on this page and hence it can not be displayed!").queue();
						}
					}
					else{
						e.getTextChannel().sendMessage("Apologies young padawan but I'm not allowed to execute this command in this channel. Please retry in "+STATIC.getChannels(bot_channels)).queue();
						logger.warn("Top command used in a not bot channel");
					}
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
