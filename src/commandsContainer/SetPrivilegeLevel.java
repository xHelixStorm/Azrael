package commandsContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sql.DiscordRoles;

public class SetPrivilegeLevel {
	public static void runTask(MessageReceivedEvent e, String [] args) {
		if(args.length > 2) {
			var role = args[1];
			if(role.length() == 18) {
				try {
					var role_id = Long.parseLong(role);
					var level = Integer.parseInt(args[2]);
					if(level <= 100 && level >= 0) {
						Logger logger = LoggerFactory.getLogger(SetPrivilegeLevel.class);
						if(DiscordRoles.SQLUpdateLevel(e.getGuild().getIdLong(), role_id, level) > 0) {
							logger.debug("role id {} has been updated with privilege level {}", role_id, level);
							e.getTextChannel().sendMessage("**The role privilege has been updated successfully!**").queue();
							DiscordRoles.SQLgetRoles(e.getGuild().getIdLong());
						}
						else {
							logger.error("privilege level of role if {} couldn't be updated in table DiscordRoles.roles", role_id);
						}
					}
					else {
						e.getTextChannel().sendMessage(e.getMember().getAsMention()+" Please type a privilege level between 0 and 100!").queue();
					}
				} catch(NumberFormatException nfe) {
					e.getTextChannel().sendMessage(e.getMember().getAsMention()+" Please type a valid role id and privilege level!").queue();
				}
			}
			else {
				e.getTextChannel().sendMessage(e.getMember().getAsMention()+" The role id has to be 18 digits long. Execution interrupted!").queue();
			}
		}
		else {
			e.getTextChannel().sendMessage(e.getMember().getAsMention()+" Something went wrong. Please recheck the syntax!").queue();
		}
	}
}
