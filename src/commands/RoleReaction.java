package commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import core.UserPrivs;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import preparedMessages.ReactionMessage;
import sql.Azrael;
import sql.DiscordRoles;

public class RoleReaction implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(RoleReaction.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		if(GuildIni.getRoleReactionCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getRoleReactionLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		//after a channel has been registered for self role assignment, it can be disabled and enabled with this command
		if(args.length > 0 && args[0].equalsIgnoreCase("enable")) {
			if(Azrael.SQLgetCommandExecutionReaction(e.getGuild().getIdLong()) == true) {
				e.getChannel().sendMessage(e.getMember().getUser().getAsMention()+" Role reactions are already enabled!").queue();
			}
			else {
				var rea_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("rea")).findAny().orElse(null);
				if(rea_channel != null) {							
					if(Azrael.SQLUpdateReaction(e.getGuild().getIdLong(), true) > 0) {
						e.getChannel().sendMessage("Role Reactions have been enabled!").queue();
						ReactionMessage.print(e, rea_channel.getChannel_ID());
					}
					else {
						e.getChannel().sendMessage("An internal error occurred. Role reactions couldn't be enabled in Azrael.commands").queue();
						logger.error("Role reactions couldn't be enabled in table Azrael.commands for guild {}", e.getGuild().getName());
					}
				}
				else {
					e.getChannel().sendMessage(e.getMember().getUser().getAsMention()+" Please set a reaction channel before continuing!!").queue();
				}
			}
		}
		else if(args.length > 0 && args[0].equalsIgnoreCase("disable")) {
			if(Azrael.SQLgetCommandExecutionReaction(e.getGuild().getIdLong()) == false) {
				e.getChannel().sendMessage(e.getMember().getUser().getAsMention()+" Role reactions are already disabled!").queue();
			}
			else {
				if(Azrael.SQLUpdateReaction(e.getGuild().getIdLong(), false) > 0) {
					var rea_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("rea")).findAny().orElse(null);
					if(rea_channel != null && Hashes.getReactionMessage(e.getGuild().getIdLong()) != null) {
						e.getGuild().getTextChannelById(rea_channel.getChannel_ID()).deleteMessageById(Hashes.getReactionMessage(e.getGuild().getIdLong())).queue();
					}
					var reactionRoles = DiscordRoles.SQLgetReactionRoles(e.getGuild().getIdLong());
					if(reactionRoles != null && reactionRoles.size() > 0) {
						for(int i = 0; i < reactionRoles.size(); i++) {
							long role_id = reactionRoles.get(i).getRole_ID();
							for(Member m : e.getGuild().getMembersWithRoles(e.getGuild().getRoleById(role_id))) {
								e.getGuild().removeRoleFromMember(m, e.getGuild().getRoleById(role_id)).queue();
							}
							if(i == 8) break;
						}
					}
					e.getChannel().sendMessage("Role reactions have been disabled and the assigned roles have been removed!").queue();
				}
				else {
					e.getChannel().sendMessage("An internal error occurred. Role reactions couldn't be disabled in Azrael.commands").queue();
					logger.error("Role reactions couldn't be disabled in table Azrael.commands for guild {}", e.getGuild().getName());
				}
			}
		}
		else {
			e.getChannel().sendMessage("Write enable or disable together with the the command to make the reaction message appear or to delete it and remove all self assigned roles!").queue();
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("{} has used RoleReaction command in guild {}!", e.getMember().getUser().getId(), e.getGuild().getId());
	}
}
