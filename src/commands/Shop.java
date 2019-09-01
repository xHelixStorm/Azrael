package commands;

import java.awt.Color;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import constructors.Guilds;
import core.Hashes;
import core.UserPrivs;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;
import sql.RankingSystem;
import util.STATIC;

public class Shop implements CommandPublic{

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		if(GuildIni.getShopCommand(e.getGuild().getIdLong())) {
			Logger logger = LoggerFactory.getLogger(Shop.class);
			logger.debug("{} has used Shop command", e.getMember().getUser().getId());
			final var commandLevel = GuildIni.getShopLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong()) {
				Guilds guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
				if(guild_settings.getRankingState() == true) {
					var bot_channels = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("bot")).collect(Collectors.toList());
					if(bot_channels.size() == 0 || bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null) != null) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle("Welcome, step in!").setThumbnail(IniFileReader.getShopThumbnail())
								.setDescription("Welcome to my shop! Have a look around! Write out the section that you wish to take a closer look into and type 'exit' when you wish to leave!\n\n"
										+ "**level ups\n"
										+ "ranks\n"
										+ "profiles\n"
										+ "icons\n"
										+ "items\n"
										+ "weapons\n"
										+ "skills**").build()).queue();
						Hashes.addTempCache("shop_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000));
					}
					else{
						e.getChannel().sendMessage(e.getMember().getAsMention()+" I'm not allowed to execute commands in this channel, please write it again in "+STATIC.getChannels(bot_channels)).queue();
						logger.warn("Shop command used in a not bot channel");
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Ranking system isn't enabled! Please ask an administrator to enable it before executing!").build()).queue();
				}
			}
			else {
				EmbedBuilder message = new EmbedBuilder();
				e.getChannel().sendMessage(message.setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(commandLevel, e.getGuild())).build()).queue();
			}
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		
	}

	@Override
	public String help() {
		return null;
	}
}
