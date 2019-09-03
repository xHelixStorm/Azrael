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
import sql.Azrael;

public class Unwatch implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Unwatch.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		if(GuildIni.getUnwatchCommand(e.getGuild().getIdLong())) {
			var commandLevel = GuildIni.getUnwatchLevel(e.getGuild().getIdLong());
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
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription("To use this command, tag one or multiple users together with the command to unwatch every tagged user! User IDs can be utilized as well!").build()).queue();
		}
		else {
			var highPrivileges = false;
			if(UserPrivs.comparePrivilege(e.getMember(), GuildIni.getUserUseWatchChannelLevel(e.getGuild().getIdLong())))
				highPrivileges = true;
			//execute action to add users to watchlist
			for(final var user : args) {
				var user_id = user.replaceAll("[^0-9]*", "");
				if(user_id.length() == 17 || user_id.length() == 18) {
					var watchedUser = Hashes.getWatchlist(e.getGuild().getId()+"-"+user_id);
					if(watchedUser != null && Boolean.compare(watchedUser.hasHigherPrivileges(), highPrivileges) == 0) {
						if(Azrael.SQLDeleteWatchlist(Long.parseLong(user_id), e.getGuild().getIdLong()) > 0) {
							Hashes.removeWatchlist(e.getGuild().getId()+"-"+user_id);
							logger.debug("User {} has been removed from the watchlist for guild {}", user_id, e.getGuild().getId());
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Error!").setDescription("An internal error occurred. **"+user+"** couldn't be inserted into Azrael.watchlist table!").build()).queue();
							logger.error("{} couldn't be removed from Azrael.watchlist for guild {}", user_id, e.getGuild().getId());
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("**"+user+"** is not being watched!").build()).queue();
					}
				}
			}
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription("Task completed!").build()).queue();
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("The user {} has used the Unwatch command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}

}
