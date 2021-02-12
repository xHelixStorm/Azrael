package commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Member;
import enums.Translation;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Competitive;
import util.STATIC;

public class Master implements CommandPublic {
	final static private Logger logger = LoggerFactory.getLogger(Master.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		return true;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		final int room_id = Competitive.SQLisUserInRoom(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong());
		if(room_id > 0) {
			final var room = Competitive.SQLgetMatchmakingRoom(e.getGuild().getIdLong(), room_id);
			if(room != null && room.getRoomID() != 0) {
				if(room.getChannelID() == e.getChannel().getIdLong() && room.getStatus() == 1 && (room.getLastJoined().getTime()+1800000)-System.currentTimeMillis() > 0) {
					final var fromMember = Competitive.SQLRetrieveMember(e.getGuild().getIdLong(), room.getRoomID(), 1, e.getMember().getUser().getIdLong());
					if(fromMember != null) {
						//proceed only if member is the master of the room
						if(fromMember.isMaster()) {
							if(args.length == 0) { 
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MASTER_HELP)).build()).queue();
							}
							else {
								String firstParam = args[0].replaceAll("[<@!>]", "");
								Member member = null;
								if(args.length == 1 && firstParam.replaceAll("[0-9]*", "").length() == 0 && e.getGuild().getMemberById(args[0]) != null) {
									long user_id = Long.parseLong(firstParam);
									member = Competitive.SQLRetrieveMember(e.getGuild().getIdLong(), room.getRoomID(), 1, user_id);
								}
								else {
									String username = "";
									for(int i = 0; i < args.length; i++) {
										username = username+args[i]+" ";
									}
									username = username.trim();
									member = Competitive.SQLRetrieveMember(e.getGuild().getIdLong(), room.getRoomID(), 1, username);
								}
								if(member == null) {
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PICK_ERR_2)).build()).queue();
								}
								else {
									//update the room master
									if(Competitive.SQLUpdateRoomMaster(e.getGuild().getIdLong(), fromMember.getUserID(), member.getUserID(), room.getRoomID()) > 0) {
										e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.MASTER_PASSED).replaceFirst("\\{\\}", member.getUsername()).replace("{}", ""+room.getRoomID())).build()).queue();
										logger.info("Member {} has passed on the room master of room {} to member {} in guild {}", fromMember.getUserID(), room.getRoomID(), member.getUserID(), e.getGuild().getId());
									}
									else {
										e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
										logger.error("Room master couldn't be passed on from member {} to member {} for room {} in guild {}", fromMember.getUserID(), member.getUserID(), room.getRoomID(), e.getGuild().getId());
									}
								}
							}
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.MASTER_ERR).replace("{}", ""+room.getRoomID())).build()).queue();
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Member {} of room {} couldn't be retrieved in guild {}", e.getMember().getUser().getId(), room.getRoomID(), e.getGuild().getId());
					}
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("It couldn't be verified if user {} has already joined a room in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			}
		}
		else if(room_id == -1) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("It couldn't be verified if user {} has already joined a room in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.trace("{} has used Master command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}

}
