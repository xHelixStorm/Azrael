package commands;

/**
 * The Commands commands prints all available and enabled commands
 * to the user.
 */

import java.awt.Color;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.UserPrivs;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import preparedMessages.CommandList;
import sql.Azrael;
import util.STATIC;

public class Commands implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Commands.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		//check if the command is enabled and that the user has enough permissions
		if(GuildIni.getCommandsCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getCommandsLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.MAGENTA).setThumbnail(e.getJDA().getSelfUser().getAvatarUrl()).setTitle("Here are all available commands!");
		long guild_id = e.getGuild().getIdLong();
		//retrieve both log and bot channels
		var allowed_channels = Azrael.SQLgetChannels(guild_id).parallelStream().filter(f -> f.getChannel_Type() != null && (f.getChannel_Type().equals("bot") || f.getChannel_Type().equals("log"))).collect(Collectors.toList());
		var bot_channels = allowed_channels.parallelStream().filter(f -> f.getChannel_Type().equals("bot")).collect(Collectors.toList());
		var this_channel = allowed_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null);
		
		//if any bot channels are registered, be sure that the commands gets written from within a bot channel
		//or log channel as exception. While throwing the error, mention only the bot channels.
		if(this_channel == null && bot_channels.size() > 0) {
			e.getChannel().sendMessage(e.getMember().getAsMention()+" I'm not allowed to execute commands in this channel, please write it again in "+STATIC.getChannels(bot_channels)).queue();
		}
		//print commands list
		else {
			e.getChannel().sendMessage(messageBuild.setDescription(CommandList.getHelp(guild_id, (UserPrivs.isUserAdmin(e.getMember()) || UserPrivs.isUserMod(e.getMember()) || e.getMember().getUser().getIdLong() == GuildIni.getAdmin(e.getGuild().getIdLong())))).build()).queue();		
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("{} has used Commands command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}

}
