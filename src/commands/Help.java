package commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.UserPrivs;
import fileManagement.FileSetting;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Help implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Help.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		if(GuildIni.getHelpCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getHelpLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		var fileInput = FileSetting.readFile("files/Guilds/"+e.getGuild().getId()+"/helpmessage.txt");
		e.getChannel().sendMessage(new EmbedBuilder().setTitle("Coming to the rescue!").setColor(Color.BLUE).setDescription((fileInput != null && fileInput.length() > 0 ? fileInput : "The help command message has not been configured!")).build()).queue();
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("{} has used Help command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}
}
