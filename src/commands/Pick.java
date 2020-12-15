package commands;

import java.awt.Color;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Member;
import enums.Translation;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Competitive;
import util.STATIC;

public class Pick implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Pick.class);

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
				//confirm that its the same channel where the queue has been filled and also that the room status is 2
				if(room.getStatus() == 2 && room.getType() == 2 && room.getChannelID() == e.getChannel().getIdLong()) {
					final var picker = Competitive.SQLRetrievePicker(e.getGuild().getIdLong(), room.getRoomID(), 2);
					if(picker != null) {
						//double check that the current user is the picker
						if(picker.getUserID() == e.getMember().getUser().getIdLong()) {
							if(args.length == 0) {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.PICK_HELP)).build()).queue();
							}
							else if(args.length == 1) {
								String firstParam = args[0].replaceAll("[<@!>]", "");
								Member member = null;
								if(firstParam.replaceAll("[0-9]*", "").length() == 0) {
									long user_id = Long.parseLong(firstParam);
									member = Competitive.SQLRetrieveMember(e.getGuild().getIdLong(), room.getRoomID(), 2, user_id);
								}
								else {
									String username = "";
									for(int i = 0; i < args.length; i++) {
										username = username+args[i]+" ";
									}
									username = username.trim();
									member = Competitive.SQLRetrieveMember(e.getGuild().getIdLong(), room.getRoomID(), 2, username);
								}
								if(member == null) {
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PICK_ERR_2)).build()).queue();
								}
								else {
									if(member.getTeam() == 0) {
										final var map = Competitive.SQLgetMap(room.getMapID());
										if(map != null) {
											//pick the user and switch the picker with the other leader
											if(Competitive.SQLPickMember(e.getGuild().getIdLong(), room.getRoomID(), member.getUserID(), picker.getUserID(), picker.getTeam()) > 0) {
												e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.PICK_SUCCESS).replaceFirst("\\{\\}", member.getUsername()).replace("{}", ""+picker.getTeam())).build()).queue();
												//if the Bot can read the history, delete the old room message and reprint, else just print the newest message
												if((e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.MESSAGE_HISTORY) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.MESSAGE_HISTORY)))) {
													e.getChannel().retrieveMessageById(room.getMessageID()).queue(m -> {
														m.delete().queue();
														Changemap.printMessage(e, room, map, false);
													}, err -> {
														Changemap.printMessage(e, room, map, false);
													});
												}
												else {
													Changemap.printMessage(e, room, map, false);
												}
											}
											else {
												e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
												logger.error("Player {} couldn't be picked for room {} in guild {}", member.getUserID(), room.getRoomID(), e.getGuild().getId());
											}
										}
										else {
											e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
											logger.error("Map {} couldn't be retrieved in guild {}", room.getMapID(), e.getGuild().getId());
										}
									}
									else {
										e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PICK_ERR_3).replace("{}", member.getUsername())).build()).queue();
									}
								}
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
							}
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PICK_ERR)).build()).queue();
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Picker of room {} couldn't be retrieved in guild {}", room.getRoomID(), e.getGuild().getId());
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
		logger.trace("{} has used Pick command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}

}
