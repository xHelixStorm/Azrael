package commandsContainer;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import enums.Translation;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.DiscordRoles;
import util.STATIC;

/**
 * Extension of the set command
 * @author xHelixStorm
 *
 */

public class SetPrivilegeLevel {
	private final static Logger logger = LoggerFactory.getLogger(SetPrivilegeLevel.class);
	
	public static void runTask(GuildMessageReceivedEvent e, String [] args) {
		if(args.length == 3) {
			var role = args[1];
			if(e.getGuild().getRoleById(role) != null) {
				var role_id = Long.parseLong(role);
				if(args[2].replaceAll("[0-9]*", "").length() == 0) {
					var level = Integer.parseInt(args[2]);
					if(level <= 100 && level >= 0) {
						if(DiscordRoles.SQLUpdateLevel(e.getGuild().getIdLong(), role_id, level) > 0) {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_PERMISSION_ADDED)).build()).queue();
							logger.debug("role id {} has been updated with privilege level {} in guild {}", role_id, level, e.getGuild().getId());
							DiscordRoles.SQLgetRoles(e.getGuild().getIdLong());
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("privilege level of role if {} couldn't be updated in table DiscordRoles.roles for guild {}", role_id, e.getGuild().getId());
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_PERMISSION_NO_LEVEL)).build()).queue();
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_PERMISSION_NO_LEVEL)).build()).queue();
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.NO_ROLE_ID)).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
		}
	}
}
