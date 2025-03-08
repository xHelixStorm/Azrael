package de.azrael.commands;

import java.awt.Color;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Member;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.sql.Competitive;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Leave implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Leave.class);

	@Override
	public boolean called(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.LEAVE);
	}

	@Override
	public boolean action(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		//check if this user is in a queue
		final int room_id = Competitive.SQLisUserInRoom(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong());
		if(room_id > 0) {
			//retrieve room information and username from user
			final var room = Competitive.SQLgetMatchmakingRoom(e.getGuild().getIdLong(), room_id);
			if(room != null && room.getRoomID() > 0) {
				if(room.getStatus() == 1) {
					if((room.getLastJoined().getTime()+1800000)-System.currentTimeMillis() > 0) {
						final var user = Competitive.SQLRetrieveMember(e.getGuild().getIdLong(), room.getRoomID(), room.getStatus(), e.getMember().getUser().getIdLong());
						if(user != null) {
							if((room.getMembers()-1) > 0) {
								//leave the room
								if(Competitive.SQLLeaveRoom(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), room_id) > 0) {
									e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.LEAVE_SUCCESS).replaceFirst("\\{\\}", user.getUsername()).replaceFirst("\\{\\}", ""+(room.getMembers()-1)).replace("{}", ""+room.getMemberLimit())).build()).queue();
									//give away room master to next member with highest elo
									if(user.isMaster() && room.getMembers()-1 > 0) {
										final var roomMembers = Competitive.SQLgetMatchmakingMembers(e.getGuild().getIdLong(), room.getRoomID());
										if(roomMembers != null && roomMembers.size() > 0) {
											final var toMember = roomMembers.parallelStream().max(Comparator.comparingInt(Member::getElo)).get();
											if(Competitive.SQLUpdateRoomMaster(e.getGuild().getIdLong(), toMember.getUserID(), room.getRoomID()) > 0) {
												e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation2(e.getGuild(), Translation.LEAVE_MASTER).replaceFirst("\\{\\}", toMember.getUsername()).replace("{}", ""+room.getRoomID())).build()).queue();
											}
											else {
												e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.LEAVE_ERR_3)).build()).queue();
												logger.error("User {} couldn't become the room master of room {} in guild {}", toMember.getUserID(), room.getRoomID(), e.getGuild().getId());
											}
										}
										else {
											e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.LEAVE_ERR_3)).build()).queue();
											logger.error("Matchmaking room members couldn't be retrieved in guild {}", e.getGuild().getId());
										}
									}
								}
								else {
									e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
									logger.error("User {} couldn't leave the room in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
								}
							}
							else {
								//delete the matchmaking room, if no one is queueing
								if(Competitive.SQLDeleteMatchmakingRoom(e.getGuild().getIdLong(), room_id) > 0) {
									e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.LEAVE_SUCCESS).replaceFirst("\\{\\}", user.getUsername()).replaceFirst("\\{\\}", ""+(room.getMembers()-1)).replace("{}", ""+room.getMemberLimit())).build()).queue();
									e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.LEAVE_CLOSE_ROOM)).build()).queue();
								}
							}
						}
						else {
							e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("Leaving user {} couldn't be retrieved in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
						}
					}
					else {
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.LEAVE_ERR)).build()).queue();
					}
				}
				else {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.LEAVE_ERR_2)).build()).queue();
				}
			}
			else {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Room information couldn't be retrieved for room {} in guild {}", room_id, e.getGuild().getId());
			}
		}
		else if(room_id == 0) {
			//user is not in a queue
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.LEAVE_ERR)).build()).queue();
		}
		else {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("It couldn't be verified if user {} has already joined a room in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
		}
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, MessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Leave command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.LEAVE.getColumn(), out.toString().trim());
		}
	}

}
