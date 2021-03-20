package de.azrael.commandsContainer;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Guilds;
import de.azrael.core.Hashes;
import de.azrael.enums.Translation;
import de.azrael.sql.RankingSystem;
import de.azrael.threads.CollectUsers;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class SetRankingSystem {
	private final static Logger logger = LoggerFactory.getLogger(SetRankingSystem.class);
	
	public static void runTask(GuildMessageReceivedEvent e, String _input){
		boolean ranking_state = false;
		boolean wrongInput = false;
		String message;
		
		if(_input.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE))) {
			ranking_state = true;
			message = STATIC.getTranslation(e.getMember(), Translation.SET_RANKING_ENABLE);
		}
		else if(_input.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISABLE))) {
			ranking_state = false;
			message = STATIC.getTranslation(e.getMember(), Translation.SET_RANKING_DISABLE);
		}
		else {
			wrongInput = true;
			message = STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND);
		}
		
		if(wrongInput == false) {
			if(RankingSystem.SQLUpdateRankingSystem(e.getGuild().getIdLong(), e.getGuild().getName(), ranking_state) > 0) {
				Guilds guild = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
				guild.setRankingState(ranking_state);
				Hashes.addStatus(e.getGuild().getIdLong(), guild);
				logger.info("User {} has updated the ranking system status to {} in guild {}", e.getMember().getUser().getId(), _input, e.getGuild().getId());
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(message).build()).queue();
				
				if(ranking_state) {
					Hashes.initializeGuildRanking(e.getGuild().getIdLong());
					Hashes.clearRankingLevels();
					if(RankingSystem.SQLgetRoles(e.getGuild().getIdLong()) == null) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Registered ranking roles couldn't be called and cached in guild {}", e.getGuild().getId());
					}
					Hashes.clearRankingLevels();
					if(RankingSystem.SQLgetLevels(e.getGuild().getIdLong()).size() == 0) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Ranking levels couldn't be called and cached in guild {}", e.getGuild().getId());
					}
					new Thread(new CollectUsers(e, true)).start();
				}
				else {
					Hashes.removeGuildRanking(e.getGuild().getIdLong());
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Ranking system status couldn't be updated in guild {}", e.getGuild().getId());
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(message).build()).queue();
		}
	}
}
