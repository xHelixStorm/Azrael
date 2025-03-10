package de.azrael.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Ranking;
import de.azrael.enums.Channel;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.sql.RankingSystem;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Display the top 10 ranking players
 * @author xHelixStorm
 *
 */

public class Top implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Top.class);
	
	@Override
	public boolean called(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.TOP);
	}

	@Override
	public boolean action(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
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
			ArrayList<Ranking> rankList = RankingSystem.SQLRanking(guild_id);
			Ranking ranking1 = rankList.parallelStream().filter(r -> r.getUser_ID() == member_id).findAny().orElse(null);
			if(rankList.size() > 0 && ranking1 != null) {
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
								+ "-------------------------------------\n#"+STATIC.getTranslation(e.getMember(), Translation.TOP_PERSONAL_INFO)+"\n"
								+ STATIC.getTranslation(e.getMember(), Translation.TOP_RANK)+rank+"\t "+STATIC.getTranslation(e.getMember(), Translation.TOP_LEVEL)+user_level+"\t "+STATIC.getTranslation(e.getMember(), Translation.TOP_EXPERIENCE)+user_experience+"\n\n"+STATIC.getTranslation(e.getMember(), Translation.TOP_PAGE)+page+"```").queue();
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
			else {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Ranking list couldn't be retrieved or user {} couldn't be found in the ranking in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			}
		}
		else{
			e.getChannel().sendMessage(e.getMember().getAsMention()+STATIC.getTranslation(e.getMember(), Translation.NOT_BOT_CHANNEL)+STATIC.getChannels(bot_channels)).queue();
		}
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, MessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Top command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.TOP.getColumn(), out.toString().trim());
		}
	}
}
