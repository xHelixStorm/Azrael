package commandsContainer;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import core.Hashes;
import core.UserPrivs;
import enums.Translation;
import fileManagement.GuildIni;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Competitive;
import util.STATIC;

public class RoomExecution {
	private final static Logger logger = LoggerFactory.getLogger(RoomExecution.class);
	
	public static void runClose(GuildMessageReceivedEvent e, int room_id) {
		if(UserPrivs.comparePrivilege(e.getMember(), GuildIni.getRoomCloseLevel(e.getGuild().getIdLong())) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong()) {
			if(Competitive.SQLDeleteMatchmakingRoom(e.getGuild().getIdLong(), room_id) > 0) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.ROOM_CLOSE).replace("{}", ""+room_id)).build()).queue();
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Matchmaking room {} couldn't be deleted in guild {}", room_id, e.getGuild().getId());
			}
			Hashes.clearTempCache("room_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
		}
	}
	
	public static void runWinnerHelp(GuildMessageReceivedEvent e, Cache cache) {
		if(UserPrivs.comparePrivilege(e.getMember(), GuildIni.getRoomWinnerLevel(e.getGuild().getIdLong())) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong()) {
			final String iniTeamName1 = GuildIni.getCompetitiveTeam1(e.getGuild().getIdLong());
			final String iniTeamName2 = GuildIni.getCompetitiveTeam2(e.getGuild().getIdLong());
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.ROOM_WINNER_HELP).replaceFirst("\\{\\}", (iniTeamName1.length() > 0 ? iniTeamName1 : STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_TEAM_1).toLowerCase())).replace("{}", (iniTeamName2.length() > 0 ? iniTeamName2 : STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_TEAM_2).toLowerCase()))).build()).queue();
			Hashes.addTempCache("room_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
		}
	}
	
	public static void runWinner(GuildMessageReceivedEvent e, String [] args, Cache cache, boolean clan) {
		if(UserPrivs.comparePrivilege(e.getMember(), GuildIni.getRoomWinnerLevel(e.getGuild().getIdLong())) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong()) {
			if(args.length == 2) {
				if(args[1].replaceAll("[0-9]*", "").length() == 0) {
					final int team = Integer.parseInt(args[1]);
					final int room_id = Integer.parseInt(cache.getAdditionalInfo2());
					if(Competitive.SQLsetWinner(e.getGuild().getIdLong(), room_id, team, clan) > 0) {
						final String iniTeamName1 = GuildIni.getCompetitiveTeam1(e.getGuild().getIdLong());
						final String iniTeamName2 = GuildIni.getCompetitiveTeam2(e.getGuild().getIdLong());
						String teamName = "";
						if(team == 1)
							teamName = (iniTeamName1.length() > 0 ? iniTeamName1 : STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_TEAM_1));
						else
							teamName = (iniTeamName2.length() > 0 ? iniTeamName2 : STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_TEAM_2));
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.ROOM_WINNER_DECIDED).replaceFirst("\\{\\}", teamName).replace("{}", ""+room_id)).build()).queue();
						Hashes.clearTempCache("room_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Winner couldn't be set in room {} and guild {}", room_id, e.getGuild().getId());
						Hashes.clearTempCache("room_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.ROOM_WINNER_ERR)).build()).queue();
					Hashes.addTempCache("room_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.ROOM_WINNER_ERR)).build()).queue();
				Hashes.addTempCache("room_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
			}
		}
	}
	
	public static void runReopen(GuildMessageReceivedEvent e, int room_id, Cache cache, boolean clan) {
		if(UserPrivs.comparePrivilege(e.getMember(), GuildIni.getRoomReopenLevel(e.getGuild().getIdLong())) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong()) {
			final var room = Competitive.SQLgetMatchmakingRoom(e.getGuild().getIdLong(), room_id);
			if(room != null && room.getRoomID() != 0) {
				if(Competitive.SQLrevertWinner(e.getGuild().getIdLong(), room_id, room.getWinner(), clan) > 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.ROOM_REOPEN).replace("{}", ""+room_id)).build()).queue();
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Room details couldn't be retrieved from Azrael.matchmaking_rooms for room {} and guild {}", room_id, e.getGuild().getId());
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Room details couldn't be retrieved from Azrael.matchmaking_rooms for room {} and guild {}", room_id, e.getGuild().getId());
			}
			Hashes.clearTempCache("room_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
		}
	}
}
