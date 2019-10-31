package commands;

/**
 * The About command prints all current bot details
 */

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.UserPrivs;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;
import util.STATIC;

public class About implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(About.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		//check if the command is enabled and that the user has enough permissions
		if(GuildIni.getAboutCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getAboutLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		long guild_id = e.getGuild().getIdLong();
		//retrieve all registered bot channels and check if the current channel is registered
		var bot_channels = Azrael.SQLgetChannels(guild_id).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("bot")).collect(Collectors.toList());
		var this_channel = bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null);
		
		//if any bot channels are registered and if the current channel isn't a bot channel, then throw a message that this command can't be executed
		if(this_channel == null && bot_channels.size() > 0) {
			e.getChannel().sendMessage(e.getMember().getAsMention()+" I'm not allowed to execute commands in this channel, please write it again in "+STATIC.getChannels(bot_channels)).queue();
		}
		else {
			//Build message and print it to the user
			EmbedBuilder messageBuilder = new EmbedBuilder().setColor(0x00AE86).setThumbnail(e.getJDA().getSelfUser().getEffectiveAvatarUrl()).setTitle("About Page!");
			messageBuilder.setAuthor("Azrael", e.getJDA().getSelfUser().getEffectiveAvatarUrl());
			messageBuilder.setDescription("Here are all details about myself!");
			messageBuilder.addField("BOT VERSION", STATIC.getVersion(), true);
			messageBuilder.addField("DEVELOPER", "Java developer xHelixStorm", true);
			messageBuilder.addBlankField(false);
			messageBuilder.addField("Functionalities", "- Self designed ranking system.\n"
					+ "- Various bot settings for numerous moderation tools such as an automated mute system, bad-word filter, name filter and much more.\n"
					+ "- Entertainment commands to display your current level, ranking gain dailies, purchase from an integrated shop, take a look at your inventory or simply bring up cute pictures of pugs or cats.\n\n"
					+ "You can see all enabled commands under "+GuildIni.getCommandPrefix(guild_id)+"commands", false);
			messageBuilder.addBlankField(false);
			messageBuilder.addField("SOURCE CODE", "[Check the latest updates of Azrael on GitHub!](https://github.com/xHelixStorm/Azrael)", false);
			e.getChannel().sendMessage(messageBuilder.build()).queue();
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("{} has used About command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}
	
	

}
