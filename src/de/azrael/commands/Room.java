package de.azrael.commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Cache;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.sql.BotConfiguration;
import de.azrael.sql.Competitive;
import de.azrael.util.Hashes;
import de.azrael.util.STATIC;
import de.azrael.util.UserPrivs;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Room implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Room.class);

	@Override
	public boolean called(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.ROOM);
	}

	@Override
	public boolean action(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		if(args.length == 0) {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.ROOM_HELP)
					.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_ONGOING))).build()).queue();
		}
		else if(args.length == 1) {
			if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ONGOING))) {
				//display all ongoing matchmaking rooms
				final var rooms = Competitive.SQLgetOngoingMatchmakingRooms(e.getGuild().getIdLong());
				if(rooms != null && rooms.size() > 0) {
					StringBuilder out = new StringBuilder();
					final String room = STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_ROOM);
					final String status = STATIC.getTranslation(e.getMember(), Translation.ROOM_STATUS);
					for(final var curRoom : rooms) {
						out.append(room+"#**"+curRoom.getRoomID()+"**\t"+status+": "+(curRoom.getStatus() == 2 ? STATIC.getTranslation(e.getMember(), Translation.ROOM_ONGOING) : STATIC.getTranslation(e.getMember(), Translation.ROOM_REVIEW))+"\t"+STATIC.getTranslation(e.getMember(), Translation.ROOM_TYPE)+": "+(curRoom.getType() == 1 ? STATIC.getTranslation(e.getMember(), Translation.ROOM_REGULAR) : (curRoom.getType() == 2 ? STATIC.getTranslation(e.getMember(), Translation.ROOM_PICKING) : STATIC.getTranslation(e.getMember(), Translation.ROOM_CLAN_WAR)))+"\n");
					}
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(out.toString()).build()).queue();
				}
				else if(rooms != null) {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.ROOM_ERR_2)).build()).queue();
				}
				else {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Ongoing matchmaking rooms couldn't be retrieved in guild {}", e.getGuild().getId());
				}
			}
			else if(args[0].replaceAll("[0-9]*", "").length() == 0) {
				//display specific rooms
				final int room_id = Integer.parseInt(args[0]);
				final var room = Competitive.SQLgetMatchmakingRoom(e.getGuild().getIdLong(), room_id);
				if(room != null && room.getRoomID() != 0) {
					final int members = Competitive.SQLgetMatchmakingMembers(e.getGuild().getIdLong());
					if(members >= 0) {
						final var map = Competitive.SQLgetMap(room.getMapID());
						if(map != null) {
							EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
							if(room.getType() != 3)
								message.setTitle(STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_ROOM)+"#"+room.getRoomID());
							else
								message.setTitle(STATIC.getTranslation(e.getMember(), Translation.MATCHMAKING_CLAN_WAR)+"#"+room.getRoomID());
							message.addField(STATIC.getTranslation(e.getMember(), Translation.ROOM_MEMBERS), "("+room.getMembers()+"/"+members+")", true);
							message.addField(STATIC.getTranslation(e.getMember(), Translation.ROOM_TYPE), (room.getType() == 1 ? STATIC.getTranslation(e.getMember(), Translation.ROOM_REGULAR) : (room.getType() == 2 ? STATIC.getTranslation(e.getMember(), Translation.ROOM_PICKING) : STATIC.getTranslation(e.getMember(), Translation.ROOM_CLAN_WAR))), true);
							message.addField(STATIC.getTranslation(e.getMember(), Translation.ROOM_MAP), (map.getMapID() != 0 ? map.getName() : STATIC.getTranslation(e.getMember(), Translation.NOT_AVAILABLE)), true);
							if(room.getType() != 3) {
								final String teamName1 = botConfig.getCompetitiveTeam1Name();
								final String teamName2 = botConfig.getCompetitiveTeam2Name();
								message.addField(STATIC.getTranslation(e.getMember(), Translation.ROOM_WINNER), (room.getWinner() == 1 ? (teamName1.length() > 0 ? teamName1 : STATIC.getTranslation(e.getMember(), Translation.ROOM_TEAM_1)) : (room.getWinner() == 2 ? (teamName2.length() > 0 ? teamName2 : STATIC.getTranslation(e.getMember(), Translation.ROOM_TEAM_2)) : STATIC.getTranslation(e.getMember(), Translation.NOT_AVAILABLE))), true);
							}
							else {
								final String clanName1 = Competitive.SQLgetClanName(e.getGuild().getIdLong(), room.getClanID1());
								final String clanName2 = Competitive.SQLgetClanName(e.getGuild().getIdLong(), room.getClanID2());
								message.addField(STATIC.getTranslation(e.getMember(), Translation.ROOM_WINNER), (room.getWinner() == 1 ? clanName1 : (room.getWinner() == 2 ? clanName2 : STATIC.getTranslation(e.getMember(), Translation.NOT_AVAILABLE))), true);
								message.addField(STATIC.getTranslation(e.getMember(), Translation.ROOM_CLANS), clanName1+" "+STATIC.getTranslation(e.getMember(), Translation.ROOM_VS)+" "+clanName2, false);
							}
							message.addField(STATIC.getTranslation(e.getMember(), Translation.ROOM_STATUS), (room.getStatus() == 1 ? STATIC.getTranslation(e.getMember(), Translation.ROOM_OPENED) : (room.getStatus() == 2 ? STATIC.getTranslation(e.getMember(), Translation.ROOM_ONGOING) : (room.getStatus() == 3 ? STATIC.getTranslation(e.getMember(), Translation.ROOM_CLOSED) : STATIC.getTranslation(e.getMember(), Translation.ROOM_REVIEW)))), false);
							message.addField(STATIC.getTranslation(e.getMember(), Translation.ROOM_CREATED), room.getCreated().toLocalDateTime().toLocalDate().toString()+" "+room.getCreated().toLocalDateTime().toLocalTime().toString(), false);
							final var commandLevels = BotConfiguration.SQLgetCommand(e.getGuild().getIdLong(), 2, Command.ROOM_CLOSE, Command.ROOM_WINNER, Command.ROOM_REOPEN);
							if(room.getStatus() == 1 && (UserPrivs.comparePrivilege(e.getMember(), (Integer)commandLevels.get(0)))) {
								message.addField(STATIC.getTranslation(e.getMember(), Translation.ROOM_PARAMETERS), STATIC.getTranslation(e.getMember(), Translation.ROOM_PARAM_1), false);
								Hashes.addTempCache("room_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "1", ""+room.getRoomID(), (room.getType() == 3 ? "1" : "0")));
							}
							else if(room.getStatus() == 2 || room.getStatus() == 4) {
								boolean paramAvailable = false;
								String parameters = null;
								if((UserPrivs.comparePrivilege(e.getMember(), (Integer)commandLevels.get(0))) && STATIC.getCommandEnabled(e.getGuild(), Command.ROOM_CLOSE)) {
									parameters = STATIC.getTranslation(e.getMember(), Translation.ROOM_PARAM_1);
									paramAvailable = true;
								}
								if((UserPrivs.comparePrivilege(e.getMember(), (Integer)commandLevels.get(1))) && STATIC.getCommandEnabled(e.getGuild(), Command.ROOM_WINNER)) {
									if(paramAvailable)
										parameters += "\n";
									else
										paramAvailable = true;
									parameters += STATIC.getTranslation(e.getMember(), Translation.ROOM_PARAM_2);
								}
								if(paramAvailable) {
									message.addField(STATIC.getTranslation(e.getMember(), Translation.ROOM_PARAMETERS), parameters, false);
									Hashes.addTempCache("room_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "2", ""+room.getRoomID(), (room.getType() == 3 ? "1" : "0")));
								}
							}
							else if(room.getStatus() == 3 && (UserPrivs.comparePrivilege(e.getMember(), (Integer)commandLevels.get(2))) && STATIC.getCommandEnabled(e.getGuild(), Command.ROOM_REOPEN)) {
								message.addField(STATIC.getTranslation(e.getMember(), Translation.ROOM_PARAMETERS), STATIC.getTranslation(e.getMember(), Translation.ROOM_PARAM_3), false);
								Hashes.addTempCache("room_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "3", ""+room.getRoomID(), (room.getType() == 3 ? "1" : "0")));
							}
							e.getChannel().sendMessageEmbeds(message.build()).queue();
						}
						else {
							e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("Map {} couldn't be retrieved in guild {}", room.getMapID(), e.getGuild().getId());
						}
					}
					else if(members == -1) {
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Matchmaking room member limit couldn't be retrieved in guild {}", e.getGuild().getId());
					}
				}
				else if(room != null) {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.ROOM_ERR)).build()).queue();
				}
				else {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Matchmaking room details couldn't be retrieved for clan {} in guild {}", room_id, e.getGuild().getId());
				}
			}
			else {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
		}
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, MessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Room command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.ROOM.getColumn(), out.toString().trim());
		}
	}

}
