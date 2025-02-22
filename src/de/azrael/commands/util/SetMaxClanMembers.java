package de.azrael.commands.util;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.enums.Translation;
import de.azrael.sql.Competitive;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SetMaxClanMembers {
	private final static Logger logger = LoggerFactory.getLogger(SetMaxClanMembers.class);
	
	public static void runHelp(MessageReceivedEvent e) {
		e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_MAX_CLAN_MEMBERS_HELP)).build()).queue();
	}
	
	public static void runTask(MessageReceivedEvent e, String [] args) {
		if(args[1].matches("[0-9]*")) {
			final var members = Long.parseLong(args[1]);
			if(members >= 0 && members <= 1000) {
				if(Competitive.SQLUpdateMaxClanMembers(e.getGuild().getIdLong(), (int)members) > 0) {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_MAX_CLAN_MEMBERS_UPDATED)).build()).queue();
					logger.info("User {} has updated the max clan members limit to {} in guild {}", e.getMember().getUser().getId(), members, e.getGuild().getId());
				}
				else {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("The max allowed clan members limit of {} members couldn't be updated in guild {}", members, e.getGuild().getId());
				}
			}
			else{
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_MAX_CLAN_MEMBERS_ERR)).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.SET_MAX_CLAN_MEMBERS_ERR)).build()).queue();
		}
	}
}