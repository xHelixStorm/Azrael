package de.azrael.commands;

import java.awt.Color;
import java.util.EnumSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiManager;

import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.ClanMember;
import de.azrael.enums.Channel;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.sql.Competitive;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Cw implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Cw.class);

	@Override
	public boolean called(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.CW);
	}

	@Override
	public boolean action(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		final var all_channels = Azrael.SQLgetChannels(e.getGuild().getIdLong());
		var bot_channels = all_channels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.BOT.getType())).collect(Collectors.toList());
		var com_channels = all_channels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.CO3.getType())).collect(Collectors.toList());
		var this_bot_channel = bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null);
		var this_com_channel = com_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null);
		
		//if any bot channels are registered and if the current channel isn't a bot channel and no competitive channel exists, then throw a message that this command can't be executed
		if(com_channels.size() == 0 && this_bot_channel == null && bot_channels.size() > 0) {
			e.getChannel().sendMessage(e.getMember().getAsMention()+STATIC.getTranslation(e.getMember(), Translation.NOT_BOT_CHANNEL)+STATIC.getChannels(bot_channels)).queue();
			return true;
		}
		//do the same for competitive channels
		else if(this_com_channel == null && com_channels.size() > 0) {
			e.getChannel().sendMessage(e.getMember().getAsMention()+STATIC.getTranslation(e.getMember(), Translation.WRONG_CHANNEL)+STATIC.getChannels(com_channels)).queue();
			return true;
		}
		
		final var member = Competitive.SQLgetClanDetails(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong());
		if(member != null && member.getClanID() != 0) {
			if(args.length == 0) {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.CW_HELP)
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_JOIN))
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_LEAVE))
						.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_START))).build()).queue();
			}
			else {
				if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_JOIN))) {
					//join the clan war room
					final var room = Competitive.SQLgetMatchmakingRoom(e.getGuild().getIdLong(), member.getClanID(), 3, 1);
					if(room != null && room.getRoomID() != 0) {
						if((room.getLastJoined().getTime()+1800000)-System.currentTimeMillis() > 0) {
							final var result = Competitive.SQLisUserInRoom(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong());
							if(result == 0) {
								int team = 0;
								if(room.getClanID1() == member.getClanID())
									team = 1;
								else if(room.getClanID2() == member.getClanID())
									team = 2;
								if(room.getMemberLimit() > 0) {
									if(room.getMembers()+1 <= room.getMemberLimit()) {
										final String username = Competitive.SQLgetUsernameFromUserStats(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong());
										if(username != null) {
											final var map = Competitive.SQLgetMap(room.getMapID());
											if(map != null) {
												if(Competitive.SQLJoinRoom(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), room.getRoomID(), team) > 0) {
													e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.JOIN_QUEUE).replaceFirst("\\{\\}", username).replaceFirst("\\{\\}", ""+room.getRoomID()).replaceFirst("\\{\\}", ""+(room.getMembers()+1)).replace("{}", ""+room.getMemberLimit())).build()).queue();
													//print room details
													if(e.getGuild().getSelfMember().hasPermission(e.getGuildChannel(), Permission.MESSAGE_HISTORY) || STATIC.setPermissions(e.getGuild(), e.getChannel().asTextChannel(), EnumSet.of(Permission.MESSAGE_HISTORY))) {
														if(room.getMessageID() != 0) {
															e.getGuild().getTextChannelById(room.getChannelID()).retrieveMessageById(room.getMessageID()).queue(m -> {
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
														Changemap.printMessage(e, room, map, false, botConfig);
													}
												}
												else {
													e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
													logger.error("User {} couldn't join room {} in guild {}", e.getMember().getUser().getId(), room.getRoomID(), e.getGuild().getId());
												}
											}
											else {
												e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
												logger.error("Map {} couldn't be retrieved in guild {}", room.getMapID(), e.getGuild().getId(), e.getGuild().getId());
											}
										}
										else {
											e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
											logger.error("Username for user {} couldn't be retrieved in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
										}
									}
									else {
										e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.JOIN_QUEUE_FULL)).build()).queue();
									}
								}
								else {
									e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.JOIN_ERR_12)).build()).queue();
								}
							}
							else if(result > 0) {
								e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.JOIN_ERR_11)).build()).queue();
							}
							else {
								e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
								logger.error("It couldn't be verified if user {} has already joined a room in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
							}
						}
						else {
							e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.JOIN_ERR_10)).build()).queue();
						}
					}
					else if(room != null) {
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CW_WAR_NO_ROOM)).build()).queue();
					}
					else {
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Open matchmaking room details couldn't be retrieved for clan {} in guild {}", member.getClanID(), e.getGuild().getId());
					}
				}
				else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_LEAVE))) {
					//leave the clan war room
					final var room_id = Competitive.SQLisUserInRoom(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong());
					if(room_id > 0) {
						final var room = Competitive.SQLgetMatchmakingRoom(e.getGuild().getIdLong(), room_id);
						if(room != null && room.getRoomID() != 0) {
							if(room.getStatus() == 1) {
								if((room.getLastJoined().getTime()+1800000)-System.currentTimeMillis() > 0) {
									final String username = Competitive.SQLgetUsernameFromUserStats(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong());
									if(username != null) {
										final var map = Competitive.SQLgetMap(room.getMapID());
										if(map != null) {
											if((room.getMembers()-1) > 0) {
												if(Competitive.SQLLeaveRoom(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), room_id) > 0) {
													e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.LEAVE_SUCCESS).replaceFirst("\\{\\}", username).replaceFirst("\\{\\}", ""+(room.getMembers()-1)).replace("{}", ""+room.getMemberLimit())).build()).queue();
													//print room details
													if(e.getGuild().getSelfMember().hasPermission(e.getGuildChannel(), Permission.MESSAGE_HISTORY) || STATIC.setPermissions(e.getGuild(), e.getChannel().asTextChannel(), EnumSet.of(Permission.MESSAGE_HISTORY))) {
														if(room.getMessageID() != 0) {
															e.getGuild().getTextChannelById(room.getChannelID()).retrieveMessageById(room.getMessageID()).queue(m -> {
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
														Changemap.printMessage(e, room, map, false, botConfig);
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
													e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.LEAVE_SUCCESS).replaceFirst("\\{\\}", username).replaceFirst("\\{\\}", ""+(room.getMembers()-1)).replace("{}", ""+room.getMemberLimit())).build()).queue();
													e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.LEAVE_CLOSE_ROOM)).build()).queue();
												}
											}
										}
										else {
											e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
											logger.error("Map {} couldn't be retrieved in guild {}", room.getMapID(), e.getGuild().getId());
										}
									}
									else {
										e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
										logger.error("Username couldn't be retrieved for user {} in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
									}
								}
								else {
									e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.LEAVE_ERR)).build()).queue();
								}
							}
							else {
								e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CW_LEAVE_ERR)).build()).queue();
							}
						}
						else {
							e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("Open matchmaking room details couldn't be retrieved for room {} in guild {}", room_id, e.getGuild().getId());
						}
					}
					else if(room_id == 0) {
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.LEAVE_ERR)).build()).queue();
					}
					else {
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("It couldn't be verified if user {} has already joined a room in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
					}
				}
				else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_START))) {
					//start the matchmaking room
					if(member.getMemberLevel() > 1) {
						final var room_id = Competitive.SQLisUserInRoom(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong());
						if(room_id > 0) {
							final var room = Competitive.SQLgetMatchmakingRoom(e.getGuild().getIdLong(), room_id);
							if(room != null && room.getRoomID() != 0) {
								if(room.getStatus() == 1) {
									if((room.getLastJoined().getTime()+1800000)-System.currentTimeMillis() > 0) {
										final var map = Competitive.SQLgetMap(room.getMapID());
										if(map != null) {
											if(Competitive.SQLUpdateMatchmakingRoomStatus(e.getGuild().getIdLong(), room.getRoomID(), 2) > 0) {
												e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.CW_START_SUCCESS).replace("{}", ""+room.getRoomID())).build()).queue();
												//print room details
												room.setStatus(2);
												if(e.getGuild().getSelfMember().hasPermission(e.getGuildChannel(), Permission.MESSAGE_HISTORY) || STATIC.setPermissions(e.getGuild(), e.getChannel().asTextChannel(), EnumSet.of(Permission.MESSAGE_HISTORY))) {
													if(room.getMessageID() != 0) {
														e.getGuild().getTextChannelById(room.getChannelID()).retrieveMessageById(room.getMessageID()).queue(m -> {
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
													Changemap.printMessage(e, room, map, false, botConfig);
												}
											}
											else {
												e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
												logger.error("Matchmaking room status of room {} couldn't be updated in guild {}", room.getRoomID(), e.getGuild().getId());
											}
										}
										else {
											e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
											logger.error("Map {} couldn't be retrieved in guild {}", room.getMapID(), e.getGuild().getId());
										}
									}
									else {
										e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CW_START_ERR)).build()).queue();
									}
								}
								else {
									e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CW_START_ERR_2)).build()).queue();
								}
							}
							else {
								e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
								logger.error("Open matchmaking room details couldn't be retrieved for room {} in guild {}", room_id, e.getGuild().getId());
							}
						}
						else if(room_id == 0) {
							e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CW_START_ERR)).build()).queue();
						}
						else {
							e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("It couldn't be verified if user {} has already joined a room in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
						}
					}
					else {
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CW_START_ERR)).build()).queue();
					}
				}
				else {
					//challenge a clan
					if(member.getMemberLevel() > 1) {
						final int result = Competitive.SQLisClanMatchmakingRoomOngoing(e.getGuild().getIdLong(), 3, member.getClanID());
						if(result == 0) {
							final var reservation = Competitive.SQLgetClanReservation(e.getGuild().getIdLong(), member.getClanID(), 3, false);
							if(reservation != null && reservation.getClanID() == 0) {
								challengeClan(e, args, member);
							}
							else if(reservation != null) {
								if((reservation.getTimestamp().getTime()+600000)-System.currentTimeMillis() < 0) {
									challengeClan(e, args, member);
								}
								else {
									e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CW_ERR_4)).build()).queue();
								}
							}
							else {
								e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
								logger.error("Clan reservation of clan {} couldn't be retrieved in guild {}", member.getClanID(), e.getGuild().getId());
							}
						}
						else if(result == 1) {
							e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CW_ERR_5)).build()).queue();
						}
						else {
							e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("It couldn't be verified if the clan {} is engaged in a clan war in guild {}", member.getClanID(), e.getGuild().getId());
						}
					}
					else {
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CW_ERR_2)).build()).queue();
					}
				}
			}
		}
		else if(member != null) {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CW_ERR)).build()).queue();
		}
		else {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Clan details of user {} couldn't be retrieved in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
		}
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, MessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Cw command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.CW.getColumn(), out.toString().trim());
		}
	}

	private static void challengeClan(MessageReceivedEvent e, String [] args, ClanMember member) {
		String clanName = "";
		for(int i = 0; i < args.length; i++) {
			clanName += args[i]+" ";
		}
		final String challengedClan = clanName.trim();
		final int clan_id = Competitive.SQLgetClanID(e.getGuild().getIdLong(), challengedClan);
		if(clan_id > 0) {
			final int result = Competitive.SQLisClanMatchmakingRoomOngoing(e.getGuild().getIdLong(), 3, clan_id);
			if(result == 0) {
				final var challengedClanMembers = Competitive.SQLgetClanMembersStaff(e.getGuild().getIdLong(), clan_id);
				if(challengedClanMembers != null && challengedClanMembers.size() > 0) {
					if(Competitive.SQLInsertClanReservation(e.getGuild().getIdLong(), e.getMember().getUser().getIdLong(), member.getClanID(), 3, e.getChannel().getIdLong()) > 0) {
						for(final var clanMember : challengedClanMembers) {
							Member guildMember = e.getGuild().getMemberById(clanMember.getUserID());
							if(guildMember != null) {
								guildMember.getUser().openPrivateChannel().queue(channel -> {
									channel.sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setFooter(e.getGuild().getId()+"-"+member.getUserID()+"-"+member.getClanID()).setDescription(STATIC.getTranslation2(e.getGuild(), Translation.CW_CHALLENGE).replace("{}", member.getClanName())).build()).queue(m -> {
										m.addReaction(Emoji.fromUnicode(EmojiManager.getForAlias(":white_check_mark:").getUnicode())).queue();
										m.addReaction(Emoji.fromUnicode(EmojiManager.getForAlias(":x:").getUnicode())).queue();
									});
								});
							}
						}
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.CW_CHALLENGE_SENT)).build()).queue();
					}
					else {
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("CW reservation couldn't be set for user {} and clan {} in guild {}", e.getMember().getUser().getId(), member.getClanID(), e.getGuild().getId());
					}
				}
				else {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Deputies of clan {} couldn't be retrieved in guild {}", clan_id, e.getGuild().getId());
				}
			}
			else if(result == 1) {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CW_ERR_6)).build()).queue();
			}
			else {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("It couldn't be verified if the clan {} is engaged in a clan war in guild {}", clan_id, e.getGuild().getId());
			}
		}
		else if(clan_id == 0) {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CW_ERR_3)).build()).queue();
		}
		else {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Clan id couldn't be retrieved in guild {}", e.getGuild().getId());
		}
	}
}
