package commands;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Ranking;
import core.UserPrivs;
import enums.Channel;
import enums.Translation;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.RankingSystem;
import util.STATIC;
import sql.Azrael;

/**
 * Display the top 10 ranking players
 * @author xHelixStorm
 *
 */

public class Top implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Top.class);
	
	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		if(GuildIni.getTopCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getTopLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
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
		
		if(args.length == 0) {
			page = 1;
		}
		else if(args.length >= 1) {
			if(args[0].replaceAll("[0-9]*", "").length() == 0)
				page = Integer.parseInt(args[0]);
			else
				page = 1;
		}
					
		var bot_channels = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.BOT.getType())).collect(Collectors.toList());
		if(bot_channels.size() == 0 || bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null) != null) {
			//TODO: error handling when Ranking query ran into error or is empty
			ArrayList<Ranking> rankList = RankingSystem.SQLRanking(guild_id);
			Ranking ranking1 = rankList.parallelStream().filter(r -> r.getUser_ID() == member_id).findAny().orElse(null);
			rank = ranking1.getRank();
			user_experience = ranking1.getExperience();
			user_level = ranking1.getLevel();
			
			//always display the last page if an unreasonable page has been provided
			int index = (page-1)*10;
			while(index >= rankList.size()) {
				index -= 10;
				page --;
			}
			
			//display the top ten of the current page
			for(int iterate = index; iterate < page*10; iterate++) {
				if(iterate < rankList.size()) {
					Ranking ranking = rankList.get(iterate);
					i = i + 1;
					Member member = e.getGuild().getMemberById(ranking.getUser_ID());
					if(member != null)
						name = member.getUser().getName();
					else
						name = STATIC.getTranslation(e.getMember(), Translation.TOP_USER_LEFT);
					level = ranking.getLevel();
					experience = ranking.getExperience();				
					if(i == 10 || i == rankList.size()) {
						message.append("["+(ranking.getRank() < 10 ? "0"+ranking.getRank() : ranking.getRank())+"] \t> #"+name+"\n\t\t\t Level: "+level+"\t "+STATIC.getTranslation(e.getMember(), Translation.TOP_EXPERIENCE)+experience+"\n");
						e.getChannel().sendMessage("```CMake\n"+STATIC.getTranslation(e.getMember(), Translation.TOP_TITLE)+"\n\n"+message.toString()+"\n"
							+ "-------------------------------------\n #"+STATIC.getTranslation(e.getMember(), Translation.TOP_PERSONAL_INFO)+"\n"
							+ " "+STATIC.getTranslation(e.getMember(), Translation.TOP_RANK)+rank+"\t "+STATIC.getTranslation(e.getMember(), Translation.TOP_LEVEL)+user_level+"\t "+STATIC.getTranslation(e.getMember(), Translation.TOP_EXPERIENCE)+user_experience+"\n\n"+STATIC.getTranslation(e.getMember(), Translation.TOP_PAGE)+page+"```").queue();
					}
					else {
						message.append("["+(ranking.getRank() < 10 ? "0"+ranking.getRank() : ranking.getRank())+"] \t> #"+name+"\n\t\t\t "+STATIC.getTranslation(e.getMember(), Translation.TOP_LEVEL)+level+"\t "+STATIC.getTranslation(e.getMember(), Translation.TOP_EXPERIENCE)+experience+"\n");
					}
				}
				else {
					break;
				}
			}
		}
		else{
			e.getChannel().sendMessage(e.getMember().getAsMention()+STATIC.getTranslation(e.getMember(), Translation.NOT_BOT_CHANNEL)+STATIC.getChannels(bot_channels)).queue();
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.trace("{} has used Top command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}
}
