package de.azrael.commands;

import java.awt.Color;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.core.UserPrivs;
import de.azrael.enums.Translation;
import de.azrael.fileManagement.GuildIni;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.sql.DiscordRoles;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Accept implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Accept.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		//check if the command is enabled and that the user has enough permissions
		if(GuildIni.getAcceptCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getAcceptLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else if(!GuildIni.getIgnoreMissingPermissions(e.getGuild().getIdLong()))
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		//start by looking if a Verification / Waiting category has been set up
		final var categories = Azrael.SQLgetCategories(e.getGuild().getIdLong());
		if(categories != null) {
			final var verification = categories.parallelStream().filter(f -> f.getType().equals("ver")).findAny().orElse(null);
			if(verification != null) {
				final var category = e.getGuild().getCategoryById(verification.getCategoryID());
				if(category != null) {
					final var ver_role = DiscordRoles.SQLgetRoles(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getCategory_ABV().equals("ver")).findAny().orElse(null);
					if(ver_role != null) {
						if(args.length == 0) {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.ACCEPT_HELP)).build()).queue();
						}
						else if(args.length == 1) {
							final var user = args[0].replaceAll("[^0-9]*", "");
							if(user.length() > 0) {
								final Member member = e.getGuild().getMemberById(user);
								if(member != null) {
									final TextChannel textChannel = e.getGuild().getTextChannels().parallelStream().filter(f -> f.getName().equals(member.getUser().getId())).findAny().orElse(null);
									if(member.getRoles().size() == 0 && textChannel != null) {
										if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
											final Role role = e.getGuild().getRoleById(ver_role.getRole_ID());
											if(role != null) {
												e.getGuild().addRoleToMember(member, role).queue(m -> {
													Azrael.SQLInsertHistory(member.getUser().getIdLong(), e.getGuild().getIdLong(), "accepted", STATIC.getTranslation2(e.getGuild(), Translation.ACCEPT_REASON), 0, e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator());
													e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.ACCEPT_DONE).replaceFirst("\\{\\}", member.getUser().getName()+"#"+member.getUser().getDiscriminator()).replace("{}", member.getUser().getId())).build()).queue();
													if(e.getGuild().getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MANAGE_CHANNEL) || STATIC.setPermissions(e.getGuild(), textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MANAGE_CHANNEL))) {
														textChannel.delete().queue();
													}
													else {
														e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MANAGE_CHANNEL.getName())+textChannel.getAsMention()).build()).queue();
														logger.error("MANAGE_ROLES permission required to to assign roles in guild {}", e.getGuild().getId());
													}
												});
											}
											else {
												e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
												logger.error("Role {} doesn't exist anymore in guild {}", ver_role.getRole_ID(), e.getGuild().getId());
											}
										}
										else {
											e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MANAGE_ROLES.getName()).build()).queue();
											logger.error("MANAGE_ROLES permission required to to assign roles in guild {}", e.getGuild().getId());
										}
									}
									else {
										e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.ACCEPT_ERR)).build()).queue();
									}
								}
								else {
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_NOT_FOUND)).build()).queue();
								}
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.USER_NOT_FOUND)).build()).queue();
							}
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.ACCEPT_NO_WAIT)).build()).queue();
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Category {} doesn't exist anymore in guild {}", verification.getCategoryID(), e.getGuild().getId());
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.ACCEPT_NO_WAIT)).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Categories couldn't be retrieved for guild {}", e.getGuild().getId());
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.trace("{} has used Accept command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}

}
