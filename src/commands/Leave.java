package commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.UserPrivs;
import enums.Translation;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Competitive;
import util.STATIC;

public class Leave implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Leave.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		//check if the command is enabled and that the user has enough permissions
		if(GuildIni.getLeaveCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getLeaveLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		//check if this user is in a queue
		final int room_id = Competitive.SQLisUserInRoom(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong());
		if(room_id > 0) {
			//retrieve room information and username from user
			final var room = Competitive.SQLgetMatchmakingRoom(e.getGuild().getIdLong(), room_id);
			if(room != null && room.getRoomID() > 0) {
				if(room.getStatus() == 1) {
					if((room.getLastJoined().getTime()+1800000)-System.currentTimeMillis() > 0) {
						final String username = Competitive.SQLgetUsernameFromUserStats(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong());
						if(username != null) {
							final int members = Competitive.SQLgetMatchmakingMembers(e.getGuild().getIdLong());
							if((room.getMembers()-1) > 0) {
								//leave the room
								if(Competitive.SQLLeaveRoom(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), room_id) > 0) {
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.LEAVE_SUCCESS).replaceFirst("\\{\\}", username).replaceFirst("\\{\\}", ""+(room.getMembers()-1)).replace("{}", ""+members)).build()).queue();
								}
								else {
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
									logger.error("User {} couldn't leave the room in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
								}
							}
							else {
								//delete the matchmaking room, if no one is queueing
								if(Competitive.SQLDeleteMatchmakingRoom(e.getGuild().getIdLong(), room_id) > 0) {
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.LEAVE_SUCCESS).replaceFirst("\\{\\}", username).replaceFirst("\\{\\}", ""+(room.getMembers()-1)).replace("{}", ""+members)).build()).queue();
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.LEAVE_CLOSE_ROOM)).build()).queue();
								}
							}
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("Username couldn't be retrieved for user {} in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.LEAVE_ERR)).build()).queue();
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.LEAVE_ERR_2)).build()).queue();
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Room information couldn't be retrieved for room {} in guild {}", room_id, e.getGuild().getId());
			}
		}
		else if(room_id == 0) {
			//user is not in a queue
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.LEAVE_ERR)).build()).queue();
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("It couldn't be verified if user {} has already joined a room in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.trace("{} has used Leave command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}

}
