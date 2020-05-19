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
 * The Edit command allows a user to edit any Bot written message
 * and also to apply or remove reactions
 * @author xHelixStorm
 *
 */

public class Edit implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Edit.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		//check if the command is enabled and that the user has enough permissions
		if(GuildIni.getEditCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getEditLevel(e.getGuild().getIdLong());
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
			e.getChannel().sendMessage(new EmbedBuilder().setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_HELP)).build()).queue();
		}
		else if (args.length == 2) {
			String channel_id = args[0].replaceAll("[<>#]", "");
			String message_id = args[1];
			if(e.getGuild().getTextChannelById(channel_id) != null) {
				Hashes.addTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "E", channel_id, message_id));
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_NO_TEXT_CHANNEL)).build()).queue();
			}
		}
		else if(args.length == 3) {
			if(args[2].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_REACTION)) || args[2].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_CLEAR_REACTIONS))) {
				String channel_id = args[0].replaceAll("[<>#]", "");
				String message_id = args[1];
				String parameter = args[2].toLowerCase();
				if(e.getGuild().getTextChannelById(channel_id) != null) {
					Hashes.addTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, (parameter.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_REACTION)) ? "RA" : "RC"), channel_id, message_id));
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_NO_TEXT_CHANNEL)).build()).queue();
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_NOT_ENOUGH_PARAMS)).build()).queue();
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("{} has used Edit command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}

}
