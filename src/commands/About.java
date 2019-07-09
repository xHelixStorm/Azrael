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
import sql.Azrael;
import util.STATIC;

public class About implements Command {

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(GuildIni.getAboutCommand(e.getGuild().getIdLong())) {
			Logger logger = LoggerFactory.getLogger(About.class);
			logger.debug("{} has used About command", e.getMember().getUser().getId());
			final var commandLevel = GuildIni.getAboutLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong()) {
				long guild_id = e.getGuild().getIdLong();
				var bot_channels = Azrael.SQLgetChannels(guild_id).parallelStream().filter(f -> f.getChannel_Type().equals("bot")).collect(Collectors.toList());
				var this_channel = bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getTextChannel().getIdLong()).findAny().orElse(null);
				
				if(this_channel == null && bot_channels.size() > 0) {
					e.getTextChannel().sendMessage(e.getMember().getAsMention()+" I'm not allowed to execute commands in this channel, please write it again in "+STATIC.getChannels(bot_channels)).queue();
					logger.warn("About command used in a not bot channel");
				}
				else {
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
					e.getTextChannel().sendMessage(messageBuilder.build()).queue();
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
