package de.azrael.commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Cache;
import de.azrael.core.Hashes;
import de.azrael.core.UserPrivs;
import de.azrael.enums.Translation;
import de.azrael.fileManagement.GuildIni;
import de.azrael.interfaces.CommandPublic;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Schedule implements CommandPublic {
	private static Logger logger = LoggerFactory.getLogger(Schedule.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		//check if the command is enabled and that the user has enough permissions
		if(GuildIni.getScheduleCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getScheduleLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.SCHEDULE_HELP)+"\n"
			+ "**"+STATIC.getTranslation(e.getMember(), Translation.PARAM_DISPLAY)+"**\n"
			+ "**"+STATIC.getTranslation(e.getMember(), Translation.PARAM_CREATE)+"**\n"
			+ "**"+STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE)+"**").build()).queue();
		Hashes.addTempCache("schedule_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000));
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.trace("User {} has used Schedule command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}

}
