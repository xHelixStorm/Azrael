package de.azrael.commands;

import java.awt.Color;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Cache;
import de.azrael.core.Hashes;
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
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * The Mute command allows the one who utilizes it to mute
 * multiple users from a server at the same time. A reason
 * can be applied in the end as well.
 * @author xHelixStorm
 *
 */

public class Mute implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Mute.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		//check if the command is enabled and that the user has enough permissions
		if(GuildIni.getMuteCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getMuteLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else if(!GuildIni.getIgnoreMissingPermissions(e.getGuild().getIdLong()))
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		//confirm that the bot has the manage roles permission
		if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
			//print the help message for this command
			if(args.length == 0) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MUTE_HELP)).build()).queue();
			}
			//mute all users that have passed in the parameters
			else {
				final String nameNotExists = STATIC.getTranslation(e.getMember(), Translation.MUTE_NAME_NOT_EXISTS);
				final String idNotExists = STATIC.getTranslation(e.getMember(), Translation.MUTE_ID_NOT_EXISTS);
				args = e.getMessage().getContentRaw().substring(GuildIni.getCommandPrefix(e.getGuild().getIdLong()).length()+5).split(" ");
				boolean userFound = false;
				StringBuilder reason = new StringBuilder();
				ArrayList<Member> users = new ArrayList<Member>();
				//iterate through all parameters to find users to mute and eventually a reason
				for(final var argument : args) {
					//execute this block if the discord name has been passed
					if(argument.matches(".*?#[0-9]{4}")) {
						//search for the user to retrieve the id
						var name = argument.split("#");
						Member member = e.getGuild().getMemberByTag(name[0], name[1]);
						//confirm that this member is still present in the server
						if(member != null) {
							//already inserted users shouldn't be muted twice
							if(!users.contains(member))
								users.add(member);
							userFound = true;
						}
						else {
							e.getChannel().sendMessage(nameNotExists.replace("{}", argument)).queue();
						}
					}
					//execute this block if a user id has been passed
					else if(argument.matches("([0-9]{17,18}|<(@|@!)[0-9]{17,18}>)")) {
						Member member = e.getGuild().getMemberById(argument.replaceAll("[<@!>]*", ""));
						//confirm that this member is still present in the server
						if(member != null) {
							//already inserted users shouldn't be muted twice
							if(!users.contains(member))
								users.add(member);
							userFound = true;
						}
						else {
							e.getChannel().sendMessage(idNotExists.replace("{}", argument)).queue();
						}
					}
					else if(userFound) {
						//collect reason if available
						reason.append(argument+" ");
					}
					else {
						//interrupt command. Start parameter isn't correct
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.MUTE_ERR)).build()).queue();
						break;
					}
				}
				//enter this block if users to mute have been found and added to the array
				if(users.size() > 0) {
					//retrieve the mute role
					final var mute_role = DiscordRoles.SQLgetRoles(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getCategory_ABV().equals("mut")).findAny().orElse(null);
					//execute only if a mute role has been registered
					if(mute_role != null) {
						//iterate through the collected users
						for(final var member : users) {
							//verify that this user doesn't have higher privileges than the bot
							if(e.getGuild().getSelfMember().canInteract(member)) {
								//verify that this user isn't already muted
								if(!UserPrivs.isUserMuted(member)) {
									//mute this user
									var applyReason = (reason.toString().length() > 0 ? reason.toString() : STATIC.getTranslation2(e.getGuild(), Translation.DEFAULT_REASON));
									Azrael.SQLInsertHistory(member.getUser().getIdLong(), e.getGuild().getIdLong(), "mute", applyReason, 0, "");
									Hashes.addTempCache("mute_time_gu"+e.getGuild().getId()+"us"+member.getUser().getId(), new Cache(e.getMember().getId(), applyReason));
									e.getGuild().addRoleToMember(member, e.getGuild().getRoleById(mute_role.getRole_ID())).queue();
								}
								else {
									e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.ALREADY_MUTED).replace("{}", member.getUser().getName()+"#"+member.getUser().getDiscriminator())).queue();
								}
							}
							else {
								e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.LOW_PRIVILEGES).replace("{}", member.getUser().getName()+"#"+member.getUser().getDiscriminator())).queue();
							}
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.NO_MUTE_ROLE)).build()).queue();
					}
				}
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MANAGE_ROLES.getName()).build()).queue();
			logger.warn("MANAGE_ROLES permission required to mute users for guild {}", e.getGuild().getId());
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.trace("The Mute command has been used from user {} in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}

}
