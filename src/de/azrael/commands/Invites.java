package de.azrael.commands;

import java.awt.Color;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.InviteManagement;
import de.azrael.enums.Command;
import de.azrael.enums.GoogleEvent;
import de.azrael.enums.Translation;
import de.azrael.google.GoogleSheets;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Invites implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Invites.class);
	private static boolean shutdownMode = false;
	
	public final static ConcurrentHashMap<Long, InviteManagement> inviteStatus = new ConcurrentHashMap<Long, InviteManagement>();

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.INVITES);
	}

	@Override
	public boolean action(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(args.length == 0) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS))
				.setDescription(STATIC.getTranslation(e.getMember(), Translation.INVITES_HELP)
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_CREATE))
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))
						.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_STATUS))).build()).queue();
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_CREATE))) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.INVITES_CREATE)).build()).queue();
		}
		else if(args.length == 2 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_CREATE)) && args[1].matches("[0-9]*")) {
			if(!STATIC.threadExists("invites_gu"+e.getGuild().getId())) {
				if(e.getGuild().getSelfMember().hasPermission(Permission.CREATE_INSTANT_INVITE)) {
					if(!shutdownMode) {
						final long total = Long.parseLong(args[1]);
						if(total > 0 && total <= 1000) {
							e.getGuild().retrieveInvites().queue(createdInvites -> {
								if(createdInvites.size() + total <= 1000) {
									try {
										STATIC.addThread(Thread.currentThread(), "invites_gu"+e.getGuild().getId());
										
										inviteStatus.put(e.getGuild().getIdLong(), new InviteManagement(total));
										final TextChannel textChannel = e.getGuild().getTextChannels().stream().filter(f -> e.getGuild().getSelfMember().hasPermission(f, Permission.MESSAGE_READ)).findAny().orElse(null);
										if(textChannel != null) {
											e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.INVITES_CREATE_START)).build()).queue();
											final ArrayList<String> invites = new ArrayList<String>();
											for(int i = 0; i < total; i++) {
												try {
													Invite invite = textChannel.createInvite().setMaxAge(0).setTemporary(false).setUnique(true).submit().get();
													logger.info("Invite {} created in guild {}", invite.getUrl(), e.getGuild().getId());
													invites.add(invite.getUrl());
													inviteStatus.put(e.getGuild().getIdLong(), inviteStatus.get(e.getGuild().getIdLong()).incrementInviteCount());
												} catch(Exception e1) {
													logger.error("An invite couldn't be created in guild {}", e.getGuild().getId());
												}
											}
											if(Azrael.SQLBatchInsertInvites(e.getGuild().getIdLong(), invites)) {
												e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.INVITES_CREATE_COMPLETE).replace("{}", invites.size()+"")).build()).queue();
												logger.info("User {} has created {} invites in guild {}", e.getMember().getUser().getId(), invites.size(), e.getGuild().getId());
												
												//execute spreadsheet google request
												if(botConfig.getGoogleFunctionalities()) {
													final var array = Azrael.SQLgetGoogleFilesAndEvent(e.getGuild().getIdLong(), 2, GoogleEvent.INVITES.id, "");
													final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
													final var values = GoogleSheets.spreadsheetInvitesRequest(array, e.getGuild(), "", "", timestamp, "", "", invites, timestamp);
													if(values != null) {
														GoogleSheets.appendRawDataToSpreadsheet(GoogleSheets.getSheetsClientService(), array[0], values, array[1], "ROWS");
													}
												}
											}
											else {
												e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.INVITES_CREATE_ERR)).build()).queue();
												logger.error("Generated invites couldn't be saved in guild {}", e.getGuild().getId());
											}
										}
										else {
											e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
											logger.error("A text channel with normal message read permission couldn't be retrieved in guild {}", e.getGuild().getId());
										}
									} catch(Exception e1) {
										e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
										logger.error("An unknown error occurred while creating one time use invites in guild {}", e.getGuild().getId());
									} finally {
										STATIC.removeThread(Thread.currentThread());
										inviteStatus.remove(e.getGuild().getIdLong());
									}
								}
								else {
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.INVITES_CREATE_ERR_3).replace("{}", ""+createdInvites.size())).build()).queue();
								}
							});
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.INVITES_CREATE_ERR_2)).build()).queue();
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.INVITES_SHUTDOWN_MODE)).build()).queue();
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.CREATE_INSTANT_INVITE.getName()).build()).queue();
					logger.error("Permission CREATE_INSTANT_INVITE required to create invites in guild {}", e.getGuild().getId());
				}
			}
			else {
				final var invite = inviteStatus.get(e.getGuild().getIdLong());
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.INVITES_ALREADY_RUNNING).replaceFirst("\\{\\}", invite.getInviteCount()+"").replace("{}", invite.getTotalInvites()+"")).build()).queue();
			}
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.INVITES_REMOVE_HELP)
					.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_ALL))).build()).queue();
		}
		else if(args.length == 2 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE)) && args[1].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ALL))) {
			if(!STATIC.threadExists("invites_gu"+e.getGuild().getId())) {
				if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_SERVER)) {
					if(!shutdownMode) {
						final ArrayList<String> invites = Azrael.SQLgetUnusedInvites(e.getGuild().getIdLong());
						if(invites != null && invites.size() > 0) {
							STATIC.addThread(Thread.currentThread(), "invites_gu"+e.getGuild().getId());
							final List<Invite> retrievedInvites = e.getGuild().retrieveInvites().complete();
							if(retrievedInvites.size() > 0) {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.INVITES_REMOVE_START)).build()).queue();
								try {
									inviteStatus.put(e.getGuild().getIdLong(), new InviteManagement(invites.size()));
									ArrayList<String> deleteInvites = new ArrayList<String>();
									for(final String inviteUrl : invites) {
										final Invite invite = retrievedInvites.parallelStream().filter(f -> f.getUrl().equals(inviteUrl)).findAny().orElse(null);
										if(invite != null) {
											try {
												invite.delete().submit().get();
												logger.info("Invite {} removed in guild {}", inviteUrl, e.getGuild().getId());
												deleteInvites.add(inviteUrl);
												inviteStatus.put(e.getGuild().getIdLong(), inviteStatus.get(e.getGuild().getIdLong()).incrementInviteCount());
											} catch(Exception e1) {
												logger.error("Invite {} couldn't be removed in guild {}", inviteUrl, e.getGuild().getId());
											}
										}
										else {
											logger.warn("Invite {} couldn't be removed because it might not exist in guild {}", inviteUrl, e.getGuild().getId());
										}
									}
									if(Azrael.SQLBatchDeleteInvites(e.getGuild().getIdLong(), deleteInvites)) {
										e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.INVITES_REMOVE_COMPLETE).replace("{}", deleteInvites.size()+"")).build()).queue();
									}
									else {
										e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.INVITES_REMOVE_ERR)).build()).queue();
										logger.error("Generated invites couldn't be saved in guild {}", e.getGuild().getId());
									}
								} catch(Exception e1) {
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
									logger.error("An unknown error occurred while removing one time use invites in guild {}", e.getGuild().getId());
								} finally {
									STATIC.removeThread(Thread.currentThread());
									inviteStatus.remove(e.getGuild().getIdLong());
								}
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.INVITES_REMOVE_ERR_2)).build()).queue();
							}
						}
						else if(invites != null) {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.INVITES_REMOVE_ERR_2)).build()).queue();
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.INVITES_SHUTDOWN_MODE)).build()).queue();
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MANAGE_SERVER.getName()).build()).queue();
					logger.error("Permission MANAGE_SERVER required to remove invites in guild {}", e.getGuild().getId());
				}
			}
			else {
				final var invite = inviteStatus.get(e.getGuild().getIdLong());
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.INVITES_ALREADY_RUNNING).replaceFirst("\\{\\}", invite.getInviteCount()+"").replace("{}", invite.getTotalInvites()+"")).build()).queue();
			}
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_STATUS))) {
			if(STATIC.threadExists("invites_gu"+e.getGuild().getId())) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.INVITES_STATUS).replaceFirst("\\{\\}", inviteStatus.get(e.getGuild().getIdLong()).getInviteCount()+"").replace("{}", inviteStatus.get(e.getGuild().getIdLong()).getTotalInvites()+"")).build()).queue();
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.INVITES_STATUS_ERR)).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
		}
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Invites command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.INVITES.getColumn(), out.toString().trim());
		}
	}

	
	public static void enableShutdownMode() {
		shutdownMode = true;
	}
}
