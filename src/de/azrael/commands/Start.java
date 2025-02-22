package de.azrael.commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.sql.Competitive;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Start implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Start.class);

	@Override
	public boolean called(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.START);
	}

	@Override
	public boolean action(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		final int room_id = Competitive.SQLisUserInRoom(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong());
		if(room_id > 0) {
			final var room = Competitive.SQLgetMatchmakingRoom(e.getGuild().getIdLong(), room_id);
			if(room != null && room.getRoomID() != 0) {
				if(room.getChannelID() == e.getChannel().getIdLong() && room.getStatus() == 1) {
					final var fromMember = Competitive.SQLRetrieveMember(e.getGuild().getIdLong(), room.getRoomID(), 1, e.getMember().getUser().getIdLong());
					if(fromMember != null) {
						if(fromMember.isMaster()) {
							if((room.getType() == 1 && room.getType() == 2 && room.getMembers() > 1) || (room.getType() == 3 && room.getMembers() > 3)) {
								Join.queueFull(e, room, room.getMemberLimit(), botConfig);
							}
							else {
								e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.START_ERR).replaceFirst("\\{\\}", ""+room.getRoomID())
									.replace("{}", (room.getType() == 3 ? "4" : "2"))).build()).queue();
							}
						}
						else {
							e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.MASTER_ERR).replace("{}", ""+room.getRoomID())).build()).queue();
						}
					}
					else {
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Member {} of room {} couldn't be retrieved in guild {}", e.getMember().getUser().getId(), room.getRoomID(), e.getGuild().getId());
					}
				}
			}
			else {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("It couldn't be verified if user {} has already joined a room in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			}
		}
		else if(room_id == -1) {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("It couldn't be verified if user {} has already joined a room in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
		}
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, MessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Start command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.START.getColumn(), out.toString().trim());
		}
	}

}
