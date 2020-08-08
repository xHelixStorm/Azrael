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

public class Queue implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Queue.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		//check if the command is enabled and that the user has enough permissions
		if(GuildIni.getQueueCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getQueueLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		final int room_id = Competitive.SQLisUserInRoom(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong());
		if(room_id > 0) {
			final var room = Competitive.SQLgetMatchmakingRoom(e.getGuild().getIdLong(), room_id);
			if(room != null && room.getRoomID() != 0) {
				final var queue = Competitive.SQLgetMatchmakingMembers(e.getGuild().getIdLong(), room_id);
				if(queue != null && queue.size() > 0) {
					StringBuilder out = new StringBuilder();
					for(final var user : queue) {
						out.append("["+user.getElo()+"] "+user.getUsername()+"\n");
					}
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.QUEUE_SUCCESS)+out.toString()).build()).queue();
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Users in the room {} couldn't be retrieved from Azrael.matchmaking_view for guild {}", room_id, e.getGuild().getId());
				}
			}
			else if(room != null) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.QUEUE_ERR)).build()).queue();
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Room details couldn't be retrieved from Azrael.matchmaking_rooms for room {} and guild {}", room_id, e.getGuild().getId());
			}
		}
		else if(room_id == 0) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.QUEUE_ERR)).build()).queue();
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("It couldn't be verified if user {} has already joined a room in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.trace("{} has used Queue command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}

}
