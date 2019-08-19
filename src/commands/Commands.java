package commands;

import java.awt.Color;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.UserPrivs;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import preparedMessages.CommandList;
import sql.Azrael;
import util.STATIC;

public class Commands implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(GuildIni.getCommandsCommand(e.getGuild().getIdLong())) {
			Logger logger = LoggerFactory.getLogger(Commands.class);
			logger.debug("{} has used Commands command", e.getMember().getUser().getId());
			var admin = GuildIni.getAdmin(e.getGuild().getIdLong());
			final var commandLevel = GuildIni.getCommandsLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || admin == e.getMember().getUser().getIdLong()) {
				EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.MAGENTA).setThumbnail(e.getJDA().getSelfUser().getAvatarUrl()).setTitle("Here are all available commands!");
				long guild_id = e.getGuild().getIdLong();
				var allowed_channels = Azrael.SQLgetChannels(guild_id).parallelStream().filter(f -> f.getChannel_Type() != null && (f.getChannel_Type().equals("bot") || f.getChannel_Type().equals("log"))).collect(Collectors.toList());
				var bot_channels = allowed_channels.parallelStream().filter(f -> f.getChannel_Type().equals("bot")).collect(Collectors.toList());
				var this_channel = allowed_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getTextChannel().getIdLong()).findAny().orElse(null);
				
				if(this_channel == null && bot_channels.size() > 0) {
					e.getTextChannel().sendMessage(e.getMember().getAsMention()+" I'm not allowed to execute commands in this channel, please write it again in "+STATIC.getChannels(bot_channels)).queue();
					logger.warn("Commands command was used in a not bot channel");
				}
				else {
					e.getTextChannel().sendMessage(messageBuild.setDescription(CommandList.getHelp(guild_id, (UserPrivs.isUserAdmin(e.getMember().getUser(), e.getGuild().getIdLong()) || UserPrivs.isUserMod(e.getMember().getUser(), e.getGuild().getIdLong()) || e.getMember().getUser().getIdLong() == admin))).build()).queue();		
				}
			}
			else {
				EmbedBuilder message = new EmbedBuilder();
				e.getTextChannel().sendMessage(message.setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(commandLevel, e.getGuild())).build()).queue();
			}
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		
	}

	@Override
	public String help() {
		return null;
	}

}
