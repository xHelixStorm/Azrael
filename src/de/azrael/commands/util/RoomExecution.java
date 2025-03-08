package de.azrael.commands.util;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Cache;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.sql.Azrael;
import de.azrael.sql.Competitive;
import de.azrael.util.Hashes;
import de.azrael.util.STATIC;
import de.azrael.util.UserPrivs;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class RoomExecution {
	private final static Logger logger = LoggerFactory.getLogger(RoomExecution.class);
	
	public static void runClose(MessageReceivedEvent e, int room_id) {
		if(UserPrivs.comparePrivilege(e.getMember(), STATIC.getCommandLevel(e.getGuild(), Command.ROOM_CLOSE))) {
			if(Competitive.SQLDeleteMatchmakingRoom(e.getGuild().getIdLong(), room_id) > 0) {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.ROOM_CLOSE).replace("{}", ""+room_id)).build()).queue();
				logger.info("User {} has removed the room {} in guild {}", e.getMember().getUser().getId(), room_id, e.getGuild().getId());
			}
			else {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Matchmaking room {} couldn't be removed in guild {}", room_id, e.getGuild().getId());
			}
			Hashes.clearTempCache("room_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.ROOM.getColumn(), e.getMessage().getContentRaw());
		}
	}
	
	public static void runWinnerHelp(MessageReceivedEvent e, Cache cache, BotConfigs botConfig) {
		if(UserPrivs.comparePrivilege(e.getMember(), STATIC.getCommandLevel(e.getGuild(), Command.ROOM_WINNER))) {
			final String teamName1 = botConfig.getCompetitiveTeam1Name();
			final String teamName2 = botConfig.getCompetitiveTeam2Name();
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.ROOM_WINNER_HELP).replaceFirst("\\{\\}", (teamName1.length() > 0 ? teamName1 : STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_TEAM_1).toLowerCase())).replace("{}", (teamName2.length() > 0 ? teamName2 : STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_TEAM_2).toLowerCase()))).build()).queue();
			Hashes.addTempCache("room_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.ROOM.getColumn(), e.getMessage().getContentRaw());
		}
	}
	
	public static void runWinner(MessageReceivedEvent e, String [] args, Cache cache, boolean clan, BotConfigs botConfig) {
		if(UserPrivs.comparePrivilege(e.getMember(), STATIC.getCommandLevel(e.getGuild(), Command.ROOM_WINNER))) {
			if(args.length == 2) {
				if(args[1].replaceAll("[0-9]*", "").length() == 0) {
					final int team = Integer.parseInt(args[1]);
					final int room_id = Integer.parseInt(cache.getAdditionalInfo2());
					if(Competitive.SQLsetWinner(e.getGuild().getIdLong(), room_id, team, clan) > 0) {
						final String teamName1 = botConfig.getCompetitiveTeam1Name();
						final String teamName2 = botConfig.getCompetitiveTeam2Name();
						String teamName = "";
						if(team == 1)
							teamName = (teamName1.length() > 0 ? teamName1 : STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_TEAM_1));
						else
							teamName = (teamName2.length() > 0 ? teamName2 : STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_TEAM_2));
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.ROOM_WINNER_DECIDED).replaceFirst("\\{\\}", teamName).replace("{}", ""+room_id)).build()).queue();
						logger.info("User {} has selected team {} as winners of room {} in guild {}", e.getMember().getUser().getId(), team, room_id, e.getGuild().getId());
						Hashes.clearTempCache("room_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					}
					else {
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Winner couldn't be set for room {} in guild {}", room_id, e.getGuild().getId());
						Hashes.clearTempCache("room_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					}
				}
				else {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.ROOM_WINNER_ERR)).build()).queue();
					Hashes.addTempCache("room_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
				}
			}
			else {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.ROOM_WINNER_ERR)).build()).queue();
				Hashes.addTempCache("room_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
			}
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.ROOM.getColumn(), e.getMessage().getContentRaw());
		}
	}
	
	public static void runReopen(MessageReceivedEvent e, int room_id, Cache cache, boolean clan) {
		if(UserPrivs.comparePrivilege(e.getMember(), STATIC.getCommandLevel(e.getGuild(), Command.ROOM_REOPEN))) {
			final var room = Competitive.SQLgetMatchmakingRoom(e.getGuild().getIdLong(), room_id);
			if(room != null && room.getRoomID() != 0) {
				if(Competitive.SQLrevertWinner(e.getGuild().getIdLong(), room_id, room.getWinner(), clan) > 0) {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.ROOM_REOPEN).replace("{}", ""+room_id)).build()).queue();
					logger.info("User {} has undone the previous winner selection for room {} in guild {}", e.getMember().getUser().getId(), room_id, e.getGuild().getId());
				}
				else {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Room winner couldn't be reverted for room {} in guild {}", room_id, e.getGuild().getId());
				}
			}
			else {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Room details couldn't be retrieved for room {} in guild {}", room_id, e.getGuild().getId());
			}
			Hashes.clearTempCache("room_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.ROOM.getColumn(), e.getMessage().getContentRaw());
		}
	}
}
