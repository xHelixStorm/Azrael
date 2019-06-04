package commands;

import java.awt.Color;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commandsContainer.ShopExecution;
import core.Guilds;
import core.UserPrivs;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.Azrael;
import sql.RankingSystem;
import sql.RankingSystemItems;
import util.STATIC;

public class Shop implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(GuildIni.getShopCommand(e.getGuild().getIdLong())) {
			Logger logger = LoggerFactory.getLogger(Shop.class);
			logger.debug("{} has used Shop command", e.getMember().getUser().getId());
			if(UserPrivs.comparePrivilege(e.getMember(), GuildIni.getShopLevel(e.getGuild().getIdLong())) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong()) {
				Guilds guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
				if(guild_settings.getRankingState() == true) {
					var bot_channels = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type().equals("bot")).collect(Collectors.toList());
					if(bot_channels.size() == 0 || bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getTextChannel().getIdLong()).findAny().orElse(null) != null) {
						final String prefix = GuildIni.getCommandPrefix(e.getGuild().getIdLong());
						if(args.length > 1 && args[0].equalsIgnoreCase("level") && args[1].equalsIgnoreCase("ups")) {
							ShopExecution.displayPartOfShop(e, "lev", guild_settings.getLevelDescription());
						}
						else if(args.length > 0 && args[0].equalsIgnoreCase("ranks")) {
							ShopExecution.displayPartOfShop(e, "ran", guild_settings.getRankDescription());
						}
						else if(args.length > 0 && args[0].equalsIgnoreCase("profiles")) {
							ShopExecution.displayPartOfShop(e, "pro", guild_settings.getProfileDescription());
						}
						else if(args.length > 0 && args[0].equalsIgnoreCase("icons")) {
							ShopExecution.displayPartOfShop(e, "ico", guild_settings.getIconDescription());
						}
						else if(args.length > 0 && args[0].equalsIgnoreCase("items")) {
							ShopExecution.displayPartOfShop(e, "ite", "");
						}
						else if(args.length == 1 && args[0].equalsIgnoreCase("weapons")) {
							StringBuilder builder = new StringBuilder();
							for(String category : RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong(), guild_settings.getThemeID())) {
								builder.append(category+", ");
							}
							e.getTextChannel().sendMessage("Use these weapon sections to filter the weapons you wish to purchase together with the command:\n**"+builder.toString()+"**").queue();
						}
						else if(args.length > 1 && args[0].equalsIgnoreCase("weapons")) {
							ShopExecution.displayPartOfShopWeapons(e, args[1]);
						}
						else {
							e.getTextChannel().sendMessage("Write the shop command together with the category of the shop you want to visit. For example "+prefix+"shop **level ups** / **ranks** / **profiles** / **icons** / **items** / **weapons**").queue();
						}
					}
					else{
						e.getTextChannel().sendMessage(e.getMember().getAsMention()+" I'm not allowed to execute commands in this channel, please write it again in "+STATIC.getChannels(bot_channels)).queue();
						logger.warn("Shop command used in a not bot channel");
					}
				}
				else {
					e.getTextChannel().sendMessage("Ranking system isn't enabled! Please ask an administrator to enable it before executing!").queue();
				}
			}
			else {
				EmbedBuilder message = new EmbedBuilder();
				e.getTextChannel().sendMessage(message.setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:").build()).queue();
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
