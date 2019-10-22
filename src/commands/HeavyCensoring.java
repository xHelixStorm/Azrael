package commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import core.UserPrivs;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import threads.LowerHeavyCensoring;

public class HeavyCensoring implements CommandPublic {
	Logger logger = LoggerFactory.getLogger(HeavyCensoring.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		if(GuildIni.getHeavyCensoringCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getHeavyCensoringLevel(e.getGuild().getIdLong());
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
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.WHITE).setDescription("Either enable or disable this feature by writing enable or disable together with the command. If enabled, every channel where a filter language is enabled,"
					+ " will be automatically censored, if the message contains only one letter, same subsequent messages or if the message doesn't contain any normal letter or digit. "
					+ "Additionaly if the threshold surpases the 30 deleted messages, it will start muting everyone who will get a removed message by the heavy censoring!").build()).queue();
		}
		else if(args.length == 1) {
			if(args[0].equalsIgnoreCase("enable") || args[0].equalsIgnoreCase("disable")) {
				var heavyCensoring = Hashes.getHeavyCensoring(e.getGuild().getIdLong());
				if(args[0].equalsIgnoreCase("enable")) {
					if(heavyCensoring == null || !heavyCensoring) {
						Hashes.addHeavyCensoring(e.getGuild().getIdLong(), true);
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.WHITE).setDescription("Heavy censoring has been enabled!").build()).queue();
						new Thread(new LowerHeavyCensoring(e)).start();
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Heavy censoring is already enabled!").build()).queue();
					}
				}
				else {
					if(heavyCensoring != null && heavyCensoring) {
						Hashes.addHeavyCensoring(e.getGuild().getIdLong(), false);
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.WHITE).setDescription("Heavy censoring has been disabled!").build()).queue();
						Hashes.removeCensoreMessage(e.getGuild().getIdLong());
						Hashes.removeFilterThreshold(e.getGuild().getIdLong());
						Hashes.getHeavyCensoringThread(e.getGuild().getIdLong()).interrupt();
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Heavy censoring is already disabled!").build()).queue();
					}
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Please use either enable or disable together with the command").build()).queue();
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Please use either enable or disable together with the command").build()).queue();
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("The HeavyCensoring command has been used from {}", e.getMember().getUser().getId());
	}

}
