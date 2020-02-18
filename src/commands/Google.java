package commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import core.Hashes;
import core.UserPrivs;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * Google service set up for specific events
 * @author xHelixStorm
 *
 */

public class Google implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Google.class);
	private final static EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE).setTitle("Google interface options!");

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		//check if the command is enabled and that the user has enough permissions
		if(GuildIni.getGoogleCommand(e.getGuild().getIdLong())) {
			var commandLevel = GuildIni.getGoogleLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || e.getMember().getUser().getIdLong() == GuildIni.getAdmin(e.getGuild().getIdLong()))
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		//print help message and all currently available APIs
		if(args.length == 0) {
			e.getChannel().sendMessage(message.setDescription("Make use of a google API and assign events upon to interact with any available platform of google. For example google docs / spreadsheets.\n"
				+ "Available APIs and parameters:\n\n"
				+ "**docs**: build an interface with google docs\n"
				+ "**spreadsheets** : build an interface with google spreadsheets").build()).queue();
		}
		else if(args.length == 1) {
			//Write in cache to display options related to google docs
			if(args[0].equalsIgnoreCase("docs")) {
				Hashes.addTempCache("google_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "docs"));
			}
			//Write in cache to display options related to google spreadsheets
			else if(args[0].equalsIgnoreCase("spreadsheets")) {
				Hashes.addTempCache("google_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "spreadsheets"));
			}
			else {
				e.getChannel().sendMessage(message.setDescription("API doesn't exist or was not written correctly. Please try again!").build()).queue();
			}
		}
		else
			e.getChannel().sendMessage(message.setDescription("More than one parameters are not supported with this command!").build()).queue();
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("{} has used Google command in guild {}", e.getMember().getUser().getIdLong(), e.getGuild().getId());
	}

}
