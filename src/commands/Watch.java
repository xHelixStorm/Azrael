package commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Watchlist;
import core.Hashes;
import core.UserPrivs;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;

public class Watch implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Watch.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		if(GuildIni.getWatchCommand(e.getGuild().getIdLong())) {
			var commandLevel = GuildIni.getWatchLevel(e.getGuild().getIdLong());
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
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription("To use this command, tag one or multiple users together with the command to use a level 1 watch on every tagged user! User IDs can be utilized as well!").build()).queue();
		}
		else {
			var highPrivileges = false;
			if(UserPrivs.comparePrivilege(e.getMember(), GuildIni.getUserUseWatchChannelLevel(e.getGuild().getIdLong())))
				highPrivileges = true;
			var allChannels = Azrael.SQLgetChannels(e.getGuild().getIdLong());
			long channel_id = 0;
			if(highPrivileges) {
				var trash_channel = allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("tra")).findAny().orElse(null);
				var watch_channel = allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("wat")).findAny().orElse(null);
				channel_id = (watch_channel != null ? watch_channel.getChannel_ID() : (trash_channel != null ? trash_channel.getChannel_ID() : 0));
			}
			else {
				var trash_channel = allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("tra")).findAny().orElse(null);
				channel_id = (trash_channel != null ? trash_channel.getChannel_ID() : 0);
			}
			if(channel_id != 0) {
				//execute action to add users to watchlist
				for(final var user : args) {
					var user_id = user.replaceAll("[^0-9]*", "");
					if(user_id.length() == 17 || user_id.length() == 18) {
						if(Azrael.SQLInsertWatchlist(Long.parseLong(user_id), e.getGuild().getIdLong(), 1, channel_id, highPrivileges) > 0) {
							Hashes.addWatchlist(e.getGuild().getId()+"-"+e.getMember().getUser().getId(), new Watchlist(1, channel_id, highPrivileges));
							logger.debug("User {} has been added to the watchlist for guild {}", user_id, e.getGuild().getId());
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Error!").setDescription("An internal error occurred. **"+user+"** couldn't be inserted into Azrael.watchlist table!").build()).queue();
							logger.error("{} couldn't be inserted into Azrael.watchlist for guild {}", user_id, e.getGuild().getId());
						}
					}
				}
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription("Task completed!").build()).queue();
			}
			else {
				//throw error for missing trash channel
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Trash channel not found!").setDescription("Before watching a discord user, please register a trash channel!").build()).queue();
			}
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("The user {} has used the Watch command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}

}
