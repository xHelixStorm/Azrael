package de.azrael.commandsContainer;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.enums.Translation;
import de.azrael.sql.DiscordRoles;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

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
							logger.info("User {} has updated the permission level of role {} to level {} in guild {}", role_id, level, e.getGuild().getId());
							DiscordRoles.SQLgetRoles(e.getGuild().getIdLong());
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("Permission level of role {} couldn't be updated to level {} in guild {}", role_id, level, e.getGuild().getId());
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
