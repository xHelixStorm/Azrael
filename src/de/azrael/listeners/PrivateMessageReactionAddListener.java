package de.azrael.listeners;

import java.awt.Color;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiManager;

import de.azrael.enums.Translation;
import de.azrael.sql.Competitive;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class PrivateMessageReactionAddListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(PrivateMessageReactionAddListener.class);
	
	private final String accept = EmojiManager.getForAlias(":white_check_mark:").getUnicode();
	private final String deny = EmojiManager.getForAlias(":x:").getUnicode();
	
	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent e) {
		if(!e.isFromGuild() && e.getChannelType().equals(ChannelType.PRIVATE)) {
			if(!e.getUser().isBot()) {
				//handle clan invites and applications
				if(e.getEmoji().getName().equals(accept) || e.getEmoji().getName().equals(deny)) {
					e.getChannel().retrieveMessageById(e.getMessageId()).queue(m -> {
						if(m.getAuthor().getIdLong() == e.getJDA().getSelfUser().getIdLong() && m.getEmbeds().size() > 0) {
							final String footer = m.getEmbeds().get(0).getFooter().getText();
							if(footer != null && footer.length() > 0) {
								final String [] args = footer.split("-");
								if(args.length == 3) {
									if(args[0].replaceAll("[0-9]*", "").length() != 0)
										return;
									final long guild_id = Long.parseLong(args[0]);
									if(args[1].replaceAll("[0-9]*", "").length() != 0)
										return;
									final long user_id = Long.parseLong(args[1]);
									if(args[2].replaceAll("[0-9]*", "").length() != 0)
										return;
									final int clan_id = Integer.parseInt(args[2]);
									
									final var reservation = Competitive.SQLgetClanReservation(guild_id, user_id, clan_id);
									if(reservation != null && reservation.getClanID() != 0) {
										if(reservation.getType() == 1) {
											switch(reservation.getAction()) {
												case 0 -> {
													//insert user into the clan as new member
													if(e.getEmoji().getName().equals(accept)) {
														final int maxMembers = Competitive.SQLgetMaxClanMembers(guild_id);
														if(Competitive.SQLgetClanMemberNumber(guild_id, clan_id)+1 <= maxMembers) {
															if(Competitive.SQLInsertClanMember(guild_id, user_id, clan_id) > 0) {
																m.delete().queue();
																e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation3(e.getUser(), Translation.CLAN_ACCEPTED_2)).build()).queue();
																Competitive.SQLUpdateClanReservationAction(guild_id, user_id, clan_id, reservation.getType(), 1);
																Member member = e.getJDA().getGuildById(guild_id).getMemberById(user_id);
																if(member != null) {
																	member.getUser().openPrivateChannel().queue(channel -> {
																		channel.sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation3(e.getUser(), Translation.CLAN_DM_ACCEPTED)).build()).queue(m2 -> {
																			//message got sent
																		}, err -> {
																			//dms are locked
																			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation3(e.getUser(), Translation.CLAN_DM_LOCKED)).build()).queue();
																		});
																	});
																}
																else {
																	e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation3(e.getUser(), Translation.CLAN_NOT_IN_GUILD)).build()).queue();
																}
															}
															else {
																e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation3(e.getUser(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation3(e.getUser(), Translation.GENERAL_ERROR)).build()).queue();
																logger.error("User {} couldn't join clan {} in guild {}", user_id, clan_id, guild_id);
															}
														}
														else {
															e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation3(e.getUser(), Translation.CLAN_LIMIT)).build()).queue();
														}
													}
													//dont' allow the user to join the clan
													else if(e.getEmoji().getName().equals(deny)) {
														m.delete().queue();
														e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation3(e.getUser(), Translation.CLAN_DENIED_2)).build()).queue();
														Competitive.SQLUpdateClanReservationAction(guild_id, user_id, clan_id, reservation.getType(), 2);
														Member member = e.getJDA().getGuildById(guild_id).getMemberById(user_id);
														if(member != null) {
															member.getUser().openPrivateChannel().queue(channel -> {
																channel.sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation3(e.getUser(), Translation.CLAN_DM_DENIED)).build()).queue(m2 -> {
																	//message got sent
																}, err -> {
																	//dms are locked
																	e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation3(e.getUser(), Translation.CLAN_DM_LOCKED)).build()).queue();
																});
															});
														}
														else {
															e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation3(e.getUser(), Translation.CLAN_NOT_IN_GUILD)).build()).queue();
														}
													}
												}
												case 1 -> {
													//user has been already accepted
													m.delete().queue();
													e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation3(e.getUser(), Translation.CLAN_ACCEPTED)).build()).queue();
												}
												case 2 -> {
													//user has been already denied
													m.delete().queue();
													e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation3(e.getUser(), Translation.CLAN_DENIED)).build()).queue();
												}
											}
										}
										else if(reservation.getType() == 2) {
											switch(reservation.getAction()) {
												case 0 -> {
													//insert user into the clan as new member
													if(e.getEmoji().getName().equals(accept)) {
														final int maxMembers = Competitive.SQLgetMaxClanMembers(guild_id);
														if(Competitive.SQLgetClanMemberNumber(guild_id, clan_id)+1 <= maxMembers) {
															if(Competitive.SQLInsertClanMember(guild_id, user_id, clan_id) > 0) {
																m.delete().queue();
																e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation3(e.getUser(), Translation.CLAN_ACCEPTED_3)).build()).queue();
																Competitive.SQLUpdateClanReservationAction(guild_id, user_id, clan_id, reservation.getType(), 1);
															}
															else {
																e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation3(e.getUser(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation3(e.getUser(), Translation.GENERAL_ERROR)).build()).queue();
																logger.error("User {} couldn't join clan {} in guild {}", user_id, clan_id, guild_id);
															}
														}
														else {
															e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation3(e.getUser(), Translation.CLAN_LIMIT)).build()).queue();
														}
													}
													//deny the invitation
													else if(e.getEmoji().getName().equals(deny)) {
														m.delete().queue();
														e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation3(e.getUser(), Translation.CLAN_DENIED_3)).build()).queue();
														Competitive.SQLUpdateClanReservationAction(guild_id, user_id, clan_id, reservation.getType(), 2);
													}
												}
											}
										}
										else if(reservation.getType() == 3) {
											switch(reservation.getAction()) {
												case 0 -> {
													if((reservation.getTimestamp().getTime()+600000)-System.currentTimeMillis() > 0) {
														if(e.getEmoji().getName().equals(accept)) {
															//accept challenge
															final var map = Competitive.SQLgetRandomMap(guild_id);
															int map_id = 0;
															if(map != null && map.getMapID() != 0) {
																map_id = map.getMapID();
															}
															else if(map == null) {
																e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation3(e.getUser(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation3(e.getUser(), Translation.GENERAL_ERROR)).build()).queue();
																logger.error("Competitive map couldn't be retrieved in guild {}", guild_id);
																return;
															}
															final String challengerClanName = Competitive.SQLgetClanName(guild_id, clan_id);
															if(challengerClanName != null) {
																final var challengeClan = Competitive.SQLgetClanDetails(e.getUser().getIdLong(), guild_id);
																if(challengeClan != null && challengeClan.getClanID() != 0) {
																	final var members = Competitive.SQLgetMatchmakingMembers(guild_id);
																	if(members > 0) {
																		final int room_id = Competitive.SQLCreateClanMatchmakingRoom(guild_id, 3, map_id, clan_id, challengeClan.getClanID(), members); 
																		if(room_id > 0) {
																			m.delete().queue();
																			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation3(e.getUser(), Translation.CW_WAR_ACCEPTED).replace("{}", challengerClanName)).build()).queue();
																			Guild guild = e.getJDA().getGuildById(guild_id);
																			TextChannel textChannel = guild.getTextChannelById(reservation.getChannelID());
																			if(guild.getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS) || STATIC.setPermissions(guild, textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS)))
																				textChannel.sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation2(e.getJDA().getGuildById(guild_id), Translation.CW_WAR_ACCEPTED_2).replaceFirst("\\{\\}", challengeClan.getClanName()).replaceFirst("\\{\\}", challengerClanName).replace("{}", ""+room_id)).build()).queue();
																			else
																				logger.warn("MESSAGE_WRITE and MESSAGE_EMBED_LINKS permission required to display the clan war reply on channel {} in guild {}", textChannel.getId(), guild.getId());
																			Competitive.SQLUpdateClanReservationAction(guild_id, user_id, clan_id, reservation.getType(), 1);
																		}
																		else {
																			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation3(e.getUser(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation3(e.getUser(), Translation.GENERAL_ERROR)).build()).queue();
																			logger.error("A new clan matchmaking room couldn't be created in guild {}", guild_id);
																		}
																	}
																	else {
																		e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation3(e.getUser(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation3(e.getUser(), Translation.GENERAL_ERROR)).build()).queue();
																		logger.error("Member limit for a matchmaking room couldn't be retrieved in guild {}", guild_id);
																	}
																}
																else {
																	e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation3(e.getUser(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation3(e.getUser(), Translation.GENERAL_ERROR)).build()).queue();
																	logger.error("Clan details couldn't be retrieved for user {} in guild {}", e.getUser().getIdLong(), guild_id);
																}
															}
															else {
																e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation3(e.getUser(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation3(e.getUser(), Translation.GENERAL_ERROR)).build()).queue();
																logger.error("Clan name couldn't be retrieved for clan {} in guild {}", clan_id, guild_id);
															}
														}
														else if(e.getEmoji().getName().equals(deny)) {
															//decline challenge
															final String challengerClanName = Competitive.SQLgetClanName(guild_id, clan_id);
															if(challengerClanName != null) {
																final var challengeClan = Competitive.SQLgetClanDetails(e.getUser().getIdLong(), guild_id);
																if(challengeClan != null && challengeClan.getClanID() != 0) {
																	m.delete().queue();
																	Member member = e.getJDA().getGuildById(guild_id).getMemberById(user_id);
																	if(member != null) {
																		member.getUser().openPrivateChannel().queue(channel -> {
																			channel.sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(member, Translation.CW_WAR_DECLINED_2).replace("{}", challengeClan.getClanName())).build()).queue(m2 -> {
																				//message got sent
																			}, err -> {
																				//dms are locked
																				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation3(e.getUser(), Translation.CLAN_DM_LOCKED)).build()).queue();
																			});
																		});
																	}
																	else {
																		e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation3(e.getUser(), Translation.CLAN_NOT_IN_GUILD)).build()).queue();
																	}
																	e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation3(e.getUser(), Translation.CW_WAR_DECLINED).replace("{}", challengerClanName)).build()).queue();
																	Competitive.SQLUpdateClanReservationAction(guild_id, user_id, clan_id, reservation.getType(), 2);
																}
																else {
																	e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation3(e.getUser(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation3(e.getUser(), Translation.GENERAL_ERROR)).build()).queue();
																	logger.error("Clan details couldn't be retrieved for user {} in guild {}", e.getUser().getIdLong(), guild_id);
																}
															}
															else {
																e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation3(e.getUser(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation3(e.getUser(), Translation.GENERAL_ERROR)).build()).queue();
																logger.error("Clan name couldn't be retrieved for clan {} in guild {}", clan_id, guild_id);
															}
														}
													}
													else {
														m.delete().queue();
														e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation3(e.getUser(), Translation.CW_EXPIRED)).build()).queue();
													}
												}
												case 1 -> {
													//challenge has been already accepted
													m.delete().queue();
													e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation3(e.getUser(), Translation.CW_WAR_ACCEPTED_3)).build()).queue();
												}
												case 2 -> {
													//challenge has been already declined
													m.delete().queue();
													e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation3(e.getUser(), Translation.CW_WAR_DECLINED_3)).build()).queue();
												}
											}
										}
									}
									else if(reservation == null) {
										e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation3(e.getUser(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation3(e.getUser(), Translation.GENERAL_ERROR)).build()).queue();
										logger.error("It couldn't be verified if there was an open application, invitation or clan war challenge for user {} and clan {} in guild {}", user_id, clan_id, guild_id);
									}
								}
							}
						}
					});
				}
			}
		}
	}
}
