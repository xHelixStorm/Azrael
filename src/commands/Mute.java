package commands;

import java.awt.Color;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import core.Hashes;
import core.UserPrivs;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;
import sql.DiscordRoles;

public class Mute implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Mute.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		if(GuildIni.getMuteCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getMuteLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
			if(args.length == 0) {
				e.getChannel().sendMessage("Write this command together with multiple user IDs or user names with discriminators to mute them at the same time. As last parameter, a reason can be applied as well!").queue();
			}
			else {
				args = e.getMessage().getContentRaw().substring(GuildIni.getCommandPrefix(e.getGuild().getIdLong()).length()+5).split(" ");
				boolean userFound = false;
				StringBuilder reason = new StringBuilder();
				ArrayList<Member> users = new ArrayList<Member>();
				//iterate through all parameters to find users to mute and eventually a reason
				for(final var argument : args) {
					if(argument.matches(".*?#[0-9]{4}")) {
						//search for the user to retrieve the id
						var name = argument.split("#");
						Member member = e.getGuild().getMemberByTag(name[0], name[1]);
						if(member != null) {
							if(!users.contains(member))
								users.add(member);
							userFound = true;
						}
						else {
							e.getChannel().sendMessage("The user **"+argument+"** doesn't exist on this server! Please try again!").queue();
							users.clear();
							break;
						}
					}
					else if(argument.matches("([0-9]{17,18}|<@[0-9]{17,18}>)")) {
						//users to mute
						Member member = e.getGuild().getMemberById(argument.replaceAll("[<@>]*", ""));
						if(member != null) {
							if(!users.contains(member))
								users.add(member);
							userFound = true;
						}
						else {
							e.getChannel().sendMessage("The user with the id number **"+argument+"** doesn't exist on this server! Please try again!").queue();
							users.clear();
							break;
						}
					}
					else if(userFound) {
						//collect reason if available
						reason.append(argument+" ");
					}
					else {
						//interrupt command. Start parameter isn't correct
						e.getChannel().sendMessage("Please provide one or more existing usernames or user IDs and then a reason if desired!").queue();
						break;
					}
				}
				if(users.size() > 0) {
					final var mute_role = DiscordRoles.SQLgetRoles(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getCategory_ABV().equals("mut")).findAny().orElse(null);
					if(mute_role != null) {
						for(final var member : users) {
							if(e.getGuild().getSelfMember().canInteract(member)) {
								if(!UserPrivs.isUserMuted(member.getUser(), e.getGuild().getIdLong())) {
									var applyReason = (reason.toString().length() > 0 ? reason.toString() : "No reason has been provided!");
									Azrael.SQLInsertHistory(member.getUser().getIdLong(), e.getGuild().getIdLong(), "mute", applyReason, 0);
									Hashes.addTempCache("mute_time_gu"+e.getGuild().getId()+"us"+member.getUser().getId(), new Cache(e.getMember().getAsMention(), applyReason));
									e.getGuild().addRoleToMember(member, e.getGuild().getRoleById(mute_role.getRole_ID())).queue();
								}
								else {
									e.getChannel().sendMessage("The user "+member.getUser().getName()+"#"+member.getUser().getDiscriminator()+" is already muted!").queue();
								}
							}
							else {
								e.getChannel().sendMessage("The user "+member.getUser().getName()+"#"+member.getUser().getDiscriminator()+" has higher privileges than myself and hence couldn't be muted!").queue();
							}
						}
					}
					else {
						e.getChannel().sendMessage("No mute role is registered! Please register a mute role before using this command!").queue();
					}
				}
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("This command can't be used without the MANAGE_ROLES permission! Please enable and then try again!").build()).queue();
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("The Mute command has been used from user {} in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}

}
