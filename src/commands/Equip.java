package commands;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import core.Hashes;
import core.UserPrivs;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import interfaces.CommandPrivate;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import sql.RankingSystem;

public class Equip implements CommandPublic, CommandPrivate {
	private final static Logger logger = LoggerFactory.getLogger(Equip.class);
	
	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		if(GuildIni.getEquipCommand(e.getGuild().getIdLong()))
			return true;
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(e.getMember().getAsMention()+" My apologies young padawan, please try to use this command in a private message to me by just writing equip without prefix!").build()).queue();
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("{} has used Equip command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}

	@Override
	public boolean called(String[] args, PrivateMessageReceivedEvent e) {
		return true;
	}

	@Override
	public void action(String[] args, PrivateMessageReceivedEvent e) {
		//save mutual guilds into an array where the ranking state is enabled
		List<Guild> mutualGuilds = e.getAuthor().getMutualGuilds().parallelStream().filter(f -> RankingSystem.SQLgetGuild(f.getIdLong()).getRankingState() && GuildIni.getEquipCommand(f.getIdLong())).collect(Collectors.toList());
		//If there's at least 1 guild with an enabled ranking state, proceed!
		if(mutualGuilds != null && mutualGuilds.size() > 0) {
			if(mutualGuilds.size() == 1) {
				if(UserPrivs.comparePrivilege(e.getJDA().getGuildById(mutualGuilds.get(0).getId()).getMemberById(e.getAuthor().getId()), GuildIni.getEquipLevel(mutualGuilds.get(0).getIdLong()))) {
					//directly make the selection screen appear (e.g. equip, unequip, etc)
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle("Equip command").setDescription("Write one of the following available options to equip a weapon and/or skill. You have 3 minutes to select an option or use 'exit' to terminate!\n\n"
							+ "**show**\n"
							+ "**set**\n"
							+ "**remove**\n"
							+ "**remove-all**").build()).queue();
					Hashes.addTempCache("equip_us"+e.getAuthor().getId(), new Cache(180000, mutualGuilds.get(0).getId()));
				}
				else {
					EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Access Denied!");
					e.getChannel().sendMessage(denied.setDescription(e.getAuthor().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(GuildIni.getEquipLevel(mutualGuilds.get(0).getIdLong()), mutualGuilds.get(0))).build()).queue();
				}
			}
			else {
				//show the guild selection screen and then go over to the action selection screen
				StringBuilder out = new StringBuilder();
				var guilds = "";
				var i = 1;
				for(final var guild : mutualGuilds) {
					if(UserPrivs.comparePrivilege(guild.getMemberById(e.getAuthor().getId()), GuildIni.getEquipLevel(guild.getIdLong()))) {
						out.append("**"+i+": "+guild.getName()+" ("+guild.getId()+")**\n");
					}
					else {
						out.append("**"+i+": "+guild.getName()+" ("+guild.getId()+") PERMISSION DENIED**\n");
					}
					if(i != mutualGuilds.size())
						guilds += guild.getId()+"-";
					else
						guilds += guild.getId();
					i++;
				}
				try {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription("Select the server by sending a digit of the server you wish to edit your equipment! You have 3 minutes to select a server or write 'exit' to terminate!\n\n"+out.toString()).build()).queue();
					Hashes.addTempCache("equip_us"+e.getAuthor().getId(), new Cache(180000, guilds, "wait"));
				} catch(IllegalArgumentException iae) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription("I'm active in too many servers to display the server selection page. Please either write the server name or the server id to directly select the server!").build()).queue();
					Hashes.addTempCache("equip_us"+e.getAuthor().getId(), new Cache(180000, guilds, "err"));
				}
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(e.getAuthor().getAsMention()+" The ranking system is disabled on all servers. Please contact an administrator to enable the feature!").build()).queue();
		}
	}

	@Override
	public void executed(boolean success, PrivateMessageReceivedEvent e) {
		logger.debug("{} has used Equip command in private message", e.getAuthor().getId());
	}

}
