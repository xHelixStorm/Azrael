package commands;

import java.awt.Color;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.UserPrivs;
import enums.Channel;
import enums.Translation;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import preparedMessages.CommandList;
import sql.Azrael;
import util.STATIC;

/**
 * The Commands command prints all available and enabled commands
 * to the user.
 * @author xHelixStorm
 *
 */

public class Help implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Help.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		//check if the command is enabled and that the user has enough permissions
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
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.MAGENTA).setThumbnail(e.getJDA().getSelfUser().getEffectiveAvatarUrl());
		long guild_id = e.getGuild().getIdLong();
		//retrieve both log and bot channels
		var bot_channels = Azrael.SQLgetChannels(guild_id).parallelStream().filter(f -> f.getChannel_Type() != null && (f.getChannel_Type().equals(Channel.BOT.getType()))).collect(Collectors.toList());
		var this_channel = bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null);
		
		//if any bot channels are registered, be sure that the commands gets written from within a bot channel
		//or was written by a staff member. If written by a staff member, ignore channel restrictions.
		final boolean admin = (UserPrivs.isUserAdmin(e.getMember()) || UserPrivs.isUserMod(e.getMember()) || e.getMember().getUser().getIdLong() == GuildIni.getAdmin(e.getGuild().getIdLong()));
		if(this_channel == null && bot_channels.size() > 0 && !admin) {
			e.getChannel().sendMessage(e.getMember().getAsMention()+STATIC.getTranslation(e.getMember(), Translation.NOT_BOT_CHANNEL)+STATIC.getChannels(bot_channels)).queue();
		}
		//print commands list
		else {
			String out = CommandList.getHelp(e.getMember(), admin, 1);
			if(out.length() > 0)
				e.getChannel().sendMessage(messageBuild.setTitle(STATIC.getTranslation(e.getMember(), Translation.COMMAND_HEADER_1)).setDescription(out).build()).queue();
			out = CommandList.getHelp(e.getMember(), admin, 2);
			if(out.length() > 0)
				e.getChannel().sendMessage(messageBuild.setTitle(STATIC.getTranslation(e.getMember(), Translation.COMMAND_HEADER_2)).setDescription(out).build()).queue();
			out = CommandList.getHelp(e.getMember(), admin, 3);
			if(out.length() > 0)
				e.getChannel().sendMessage(messageBuild.setTitle(STATIC.getTranslation(e.getMember(), Translation.COMMAND_HEADER_3)).setDescription(out).build()).queue();
			out = CommandList.getHelp(e.getMember(), admin, 4);
			if(out.length() > 0)
				e.getChannel().sendMessage(messageBuild.setTitle(STATIC.getTranslation(e.getMember(), Translation.COMMAND_HEADER_4)).setDescription(out).build()).queue();
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.trace("{} has used Commands command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}

}
