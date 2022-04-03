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
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Restrict implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Restrict.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.RESTRICT);
	}

	@Override
	public boolean action(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		final int room_id = Competitive.SQLisUserInRoom(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong());
		if(room_id > 0) {
			final var room = Competitive.SQLgetMatchmakingRoom(e.getGuild().getIdLong(), room_id);
			if(room != null && room.getRoomID() != 0) {
				if(room.getChannelID() == e.getChannel().getIdLong() && room.getStatus() == 1 && (room.getLastJoined().getTime()+1800000)-System.currentTimeMillis() > 0) {
					final var fromMember = Competitive.SQLRetrieveMember(e.getGuild().getIdLong(), room.getRoomID(), 1, e.getMember().getUser().getIdLong());
					if(fromMember != null) {
						if(fromMember.isMaster()) {
							if(args.length == 0) {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.RESTRICT_HELP)
										.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_MAX))).build()).queue();
							}
							else if(args.length == 1) {
								int memberLimit = 0;
								boolean allowed = false;
								if(args[0].replaceAll("[0-9]*", "").length() == 0) {
									memberLimit = Integer.parseInt(args[0]);
									final int maxMemberLimit = Competitive.SQLgetMatchmakingMembers(e.getGuild().getIdLong());
									if(memberLimit <= maxMemberLimit && memberLimit >= 2) {
										if(memberLimit % 2 == 0) {
											if(room.getMembers() <= memberLimit) {
												allowed = true;
											}
											else {
												e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.RESTRICT_ERR2)).build()).queue();
											}
										}
										else if (maxMemberLimit > room.getMembers() && memberLimit == maxMemberLimit) {
											allowed = true;
										}
										else {
											e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.RESTRICT_ERR)
													.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_MAX))).build()).queue();
										}
									}
								}
								else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_MAX))) {
									memberLimit = Competitive.SQLgetMatchmakingMembers(e.getGuild().getIdLong());
									if(memberLimit > 0) {
										allowed = true;
									}
									else {
										e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
										logger.error("The general member limit for every room couldn't be retrieved in guild {}", room.getRoomID(), e.getGuild().getId());
									}
								}
								else {
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
								}
								if(allowed) {
									//update member limit here
									if(Competitive.SQLUpdateRoomMemberLimit(e.getGuild().getIdLong(), room.getRoomID(), memberLimit) >= 0) {
										e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.RESTRICT_SUCCESS).replaceFirst("\\{\\}", ""+room.getRoomID()).replace("{}", ""+memberLimit)).build()).queue();
										logger.info("User {} limited the room {} to {} members in guild {}", e.getMember().getUser().getId(), room.getRoomID(), memberLimit, e.getGuild().getId());
									}
									else {
										e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
										logger.error("Member restriction for room {} couldn't be updated in guild {}", room.getRoomID(), e.getGuild().getId());
									}
								}
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
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
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Restrict command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.RESTRICT.getColumn(), out.toString().trim());
		}
	}

}
