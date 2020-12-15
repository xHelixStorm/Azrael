package commands;

import java.awt.Color;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.UserPrivs;
import enums.Channel;
import enums.Translation;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;
import sql.Competitive;
import util.STATIC;

public class Leaderboard implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Leaderboard.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		//check if the command is enabled and that the user has enough permissions
		if(GuildIni.getLeaderboardCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getLeaderboardLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		var bot_channels = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.BOT.getType())).collect(Collectors.toList());
		if(bot_channels.size() == 0 || bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null) != null) {
			final var ranking = Competitive.SQLgetRankingTop10(e.getGuild().getIdLong());
			StringBuilder out = new StringBuilder();
			final String eloName = STATIC.getTranslation(e.getMember(), Translation.LEADERBOARD_ELO);
			if(ranking != null && ranking.size() > 0) {
				int count = 1;
				for(final var member : ranking) {
					final String [] array = member.split("-");
					final String username = array[0];
					final String elo = array[1];
					String position = StringUtils.leftPad(""+count, 2, "0");
					
					out.append("["+position+"] \t > #"+username+"\n\t\t\t "+eloName+elo+"\n");
					count++;
				}
				final var rank = Competitive.SQLgetRanking(e.getGuild().getIdLong());
				String personalInfo = "";
				if(rank != null) {
					final String personalRank = rank.parallelStream().filter(f -> f.split("-")[0].equals(e.getMember().getUser().getId())).findAny().orElse(null);
					if(personalRank != null) {
						String [] array = personalRank.split("-");
						personalInfo = " "+STATIC.getTranslation(e.getMember(), Translation.LEADERBOARD_RANK)+Math.round(Double.parseDouble(array[1]))+"\t "+eloName+array[2];
					}
					else {
						personalInfo = STATIC.getTranslation(e.getMember(), Translation.LEADERBOARD_ERR_2);
					}
				}
				else {
					personalInfo = STATIC.getTranslation(e.getMember(), Translation.LEADERBOARD_ERR_2);
				}
				e.getChannel().sendMessage("```CMAKE\n"+STATIC.getTranslation(e.getMember(), Translation.LEADERBOARD_TITLE)+"\n\n"+out.toString()+"\n"
						+ "-------------------------------------\n #"+STATIC.getTranslation(e.getMember(), Translation.TOP_PERSONAL_INFO)+"\n"
						+ personalInfo+"\n```").queue();
			}
			else if(ranking != null) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.LEADERBOARD_ERR)).build()).queue();
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Top 10 ELO ranking couldn't be retrieved in guild {}", e.getGuild().getId());
			}
		}
		else {
			e.getChannel().sendMessage(e.getMember().getAsMention()+STATIC.getTranslation(e.getMember(), Translation.NOT_BOT_CHANNEL)+STATIC.getChannels(bot_channels)).queue();
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.trace("{} has used Leaderboard command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}

}
