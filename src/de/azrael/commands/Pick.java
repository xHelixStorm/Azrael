package de.azrael.commands;

import java.awt.Color;
import java.util.EnumSet;

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
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Pick implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Pick.class);

	@Override
	public boolean called(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.PICK);
	}

	@Override
	public boolean action(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		final int room_id = Competitive.SQLisUserInRoom(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong());
		if(room_id > 0) {
			final var room = Competitive.SQLgetMatchmakingRoom(e.getGuild().getIdLong(), room_id);
			if(room != null && room.getRoomID() != 0) {
				//confirm that its the same channel where the queue has been filled and also that the room status is 2
				if(room.getStatus() == 2 && room.getType() == 2 && room.getChannelID() == e.getChannel().getIdLong() && (room.getLastJoined().getTime()+1800000)-System.currentTimeMillis() > 0) {
					final var picker = Competitive.SQLRetrievePicker(e.getGuild().getIdLong(), room.getRoomID(), 2);
					if(picker != null) {
						//double check that the current user is the picker
						if(picker.getUserID() == e.getMember().getUser().getIdLong()) {
							if(args.length == 0) {
								e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.PICK_HELP)).build()).queue();
							}
							else {
								String firstParam = args[0].replaceAll("[<@!>]", "");
								Member member = null;
								if(args.length == 1 && firstParam.replaceAll("[0-9]*", "").length() == 0  && e.getGuild().getMemberById(args[0]) != null) {
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
									e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PICK_ERR_2)).build()).queue();
								}
								else {
									if(member.getTeam() == 0) {
										final var map = Competitive.SQLgetMap(room.getMapID());
										if(map != null) {
											//pick the user and switch the picker with the other leader
											if(Competitive.SQLPickMember(e.getGuild().getIdLong(), room.getRoomID(), member.getUserID(), picker.getUserID(), picker.getTeam()) > 0) {
												e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.PICK_SUCCESS).replaceFirst("\\{\\}", member.getUsername()).replace("{}", ""+picker.getTeam())).build()).queue();
												//if the Bot can read the history, delete the old room message and reprint, else just print the newest message
												if((e.getGuild().getSelfMember().hasPermission(e.getGuildChannel(), Permission.MESSAGE_HISTORY) || STATIC.setPermissions(e.getGuild(), e.getChannel().asTextChannel(), EnumSet.of(Permission.MESSAGE_HISTORY)))) {
													e.getChannel().retrieveMessageById(room.getMessageID()).queue(m -> {
														m.delete().queue();
														Changemap.printMessage(e, room, map, false, botConfig);
													}, err -> {
														Changemap.printMessage(e, room, map, false, botConfig);
													});
												}
												else {
													Changemap.printMessage(e, room, map, false, botConfig);
												}
											}
											else {
												e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
												logger.error("Player {} couldn't be picked for room {} in guild {}", member.getUserID(), room.getRoomID(), e.getGuild().getId());
											}
										}
										else {
											e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
											logger.error("Map {} couldn't be retrieved in guild {}", room.getMapID(), e.getGuild().getId());
										}
									}
									else {
										e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PICK_ERR_3).replace("{}", member.getUsername())).build()).queue();
									}
								}
							}
						}
						else {
							e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PICK_ERR)).build()).queue();
						}
					}
					else {
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Picker of room {} couldn't be retrieved in guild {}", room.getRoomID(), e.getGuild().getId());
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
			logger.trace("{} has used Pick command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.PICK.getColumn(), out.toString().trim());
		}
	}

}
