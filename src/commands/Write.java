package commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import core.Hashes;
import core.UserPrivs;
import enums.Translation;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import util.STATIC;

/**
 * This command allows a user to submit a message as the Bot.
 * To use by mentioning the text channel together with the command.
 * @author xHelixStorm
 *
 */

public class Write implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Write.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		//check if the command is enabled and that the user has enough permissions
		if(GuildIni.getWriteCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getWriteLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		if(args.length == 0) {
			e.getChannel().sendMessage(new EmbedBuilder().setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.WRITE_HELP)).build()).queue();
		}
		else if (args.length == 1) {
			String channel_id = args[0].replaceAll("[<>#]", "");
			if(e.getGuild().getTextChannelById(channel_id) != null) {
				Hashes.addTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "W", channel_id));
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TEXT_CHANNEL_NOT_EXISTS)).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.trace("{} has used Write command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}
	
}
