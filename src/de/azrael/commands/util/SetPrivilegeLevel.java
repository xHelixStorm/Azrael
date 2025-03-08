package de.azrael.commands.util;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.enums.Translation;
import de.azrael.sql.DiscordRoles;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Extension of the set command
 * @author xHelixStorm
 *
 */

public class SetPrivilegeLevel {
	private final static Logger logger = LoggerFactory.getLogger(SetPrivilegeLevel.class);
	
	public static void runTask(MessageReceivedEvent e, String [] args) {
		if(args.length == 3) {
			if(args[2].matches("[0-9]*")) {
				final var role = e.getGuild().getRoleById(args[2]);
				if(role != null) {
					if(args[1].matches("[0-9]*")) {
						var level = Integer.parseInt(args[1]);
						if(level <= 100 && level >= 0) {
							final int result = DiscordRoles.SQLUpdateLevel(e.getGuild().getIdLong(), role.getIdLong(), level);
							if(result > 0) {
								e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_PERMISSION_ADDED)).build()).queue();
								logger.info("User {} has updated the permission level of role {} to level {} in guild {}", role.getId(), level, e.getGuild().getId());
								DiscordRoles.SQLgetRoles(e.getGuild().getIdLong());
							}
							else if(result == 0) {
								e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_PERMISSION_NOT_FOUND)).build()).queue();
							}
							else {
								e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
								logger.error("Permission level of role {} couldn't be updated to level {} in guild {}", role.getId(), level, e.getGuild().getId());
							}
						}
						else {
							e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_PERMISSION_NO_LEVEL)).build()).queue();
						}
					}
					else {
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_PERMISSION_NO_LEVEL)).build()).queue();
					}
				}
				else {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.ROLE_NOT_EXISTS)).build()).queue();
				}
			}
			else {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.NO_ROLE_ID)).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
		}
	}
}
