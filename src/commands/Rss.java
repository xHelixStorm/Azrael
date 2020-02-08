package commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import constructors.RSS;
import core.Hashes;
import core.UserPrivs;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;
import util.STATIC;

public class Rss implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Rss.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		if(GuildIni.getRssCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getRssLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		EmbedBuilder message = new EmbedBuilder();
		if(args.length == 0) {
			//throw default message with instructions
			message.setColor(Color.BLUE);
			e.getChannel().sendMessage(message.setDescription("Use this command to set up RSS pages or Twitter hashtags that will be displayed in a dedicated channel:\n\n"
					+ "**-register**: register an rss url or twitter hashtag for this server\n"
					+ "**-format**: change the format of how rss feeds and hashtags should be displayed\n"
					+ "**-options**: change the options for your tweets to display only specific tweets\n"
					+ "**-remove**: remove an rss or hashtag from this server\n"
					+ "**-test**: picks the first rss feed or hashtag to test the settings\n"
					+ "**-display**: display the current registered feeds and hashtags for this server").build()).queue();
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase("-register")) {
			message.setColor(Color.BLUE);
			e.getChannel().sendMessage(message.setDescription("Do you wish to add a basic RSS url or add a Twitter hashtag?\n"
					+ "Write either 1 for the Basic RSS or 2 for the Twitter hashtag together with the full command to select an option!").build()).queue();
		}
		else if(args.length > 1 && args[0].equalsIgnoreCase("-register") && !args[1].matches("[^\\d]")) {
			//select a rss model
			var type = Integer.parseInt(args[1]);
			if(type >= 1 && type <= 2) {
				if(type == 1) {
					message.setColor(Color.BLUE);
					e.getChannel().sendMessage(message.setDescription("Now please apply a regular RSS url!").build()).queue();
					Hashes.addTempCache("rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(180000, "register", ""+type));
				}
				else if(type == 2) {
					STATIC.loginTwitter();
					if(STATIC.getTwitterFactory() != null) {
						message.setColor(Color.BLUE);
						e.getChannel().sendMessage(message.setDescription("Now please apply a Twitter hashtag!").build()).queue();
						Hashes.addTempCache("rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(180000, "register", ""+type));
					}
					else {
						message.setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription("Please set up the config.ini file after creating a Twitter Bot on https://apps.twitter.com before using this command!").build()).queue();
					}
				}
			}
			else {
				message.setColor(Color.RED);
				e.getChannel().sendMessage(message.setDescription("Please type 1 to register an rss url or 2 to register a Twitter hashtag!").build()).queue();
			}
		}
		else if(args[0].equalsIgnoreCase("-remove")) {
			int counter = 1;
			StringBuilder out = new StringBuilder();
			for(RSS feed : Azrael.SQLgetRSSFeeds(e.getGuild().getIdLong())) {
				out.append("**Link "+counter+":** "+feed.getURL()+"\n");
				counter++;
			}
			message.setColor(Color.BLUE);
			e.getChannel().sendMessage(message.setDescription("Please select a digit for the RSS feed or hashtag to be removed:\n\n"+(out.length() > 0 ? out.toString(): "<no rss feeds have been registered>")).build()).queue();
			logger.debug("{} chose to remove a feed in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			if(out.length() > 0)
				Hashes.addTempCache("rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(180000, "remove"));
		}
		else if(args[0].equalsIgnoreCase("-format")) {
			int counter = 1;
			StringBuilder out = new StringBuilder();
			for(RSS feed : Azrael.SQLgetRSSFeeds(e.getGuild().getIdLong())) {
				out.append("**Link "+counter+":** "+feed.getURL()+"\n");
				counter++;
			}
			message.setColor(Color.BLUE);
			e.getChannel().sendMessage(message.setDescription("Please select a digit for the RSS feed or hashtag to be personalized:\n\n"+(out.length() > 0 ? out.toString(): "<no rss feeds have been registered>")).build()).queue();
			logger.debug("{} chose to change the format of a feed in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			if(out.length() > 0)
				Hashes.addTempCache("rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(180000, "format"));
		}
		else if(args[0].equalsIgnoreCase("-options")) {
			int counter = 1;
			StringBuilder out = new StringBuilder();
			for(RSS feed: Azrael.SQLgetRSSFeeds(e.getGuild().getIdLong(), 2)) {
				out.append("**Hashtag "+counter+":** "+feed.getURL()+"\n");
				counter++;
			}
			message.setColor(Color.BLUE);
			if(out.length() > 0) {
				e.getChannel().sendMessage(message.setDescription("Please select a digit to change the options of a hashtag:\n\n"+out.toString()).build()).queue();
				logger.debug("{} choose to change the options of a tweet in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
				Hashes.addTempCache("rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(180000, "options"));
			}
			else {
				e.getChannel().sendMessage(message.setDescription("No hashtags for Twitter have been registered. Register at least one tweet before being able to set the options.").build()).queue();
			}
		}
		else if(args[0].equalsIgnoreCase("-test")) {
			//test a feed
			int counter = 1;
			StringBuilder out = new StringBuilder();
			for(RSS feed : Azrael.SQLgetRSSFeeds(e.getGuild().getIdLong())) {
				out.append("**Link "+counter+":** "+feed.getURL()+"\n");
				counter++;
			}
			message.setColor(Color.BLUE);
			e.getChannel().sendMessage(message.setDescription("Please select a digit for the RSS or hashtag that needs to be tested:\n\n"+(out.length() > 0 ? out.toString(): "<no rss feeds have been registered>")).build()).queue();
			logger.debug("{} chose to change the format of a feed in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			if(out.length() > 0)
				Hashes.addTempCache("rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(180000, "test"));
		}
		else if(args[0].equalsIgnoreCase("-display")) {
			//display the registered feeds
			int counter = 1;
			StringBuilder out = new StringBuilder();
			for(RSS feed : Azrael.SQLgetRSSFeeds(e.getGuild().getIdLong())) {
				out.append("**Link "+counter+":** "+feed.getURL()+"\n");
				counter++;
			}
			message.setColor(Color.BLUE);
			e.getChannel().sendMessage(message.setDescription("These are the registered rss feeds and hashtags:\n\n"+(out.length() > 0 ? out.toString(): "<no rss feeds have been registered>")).build()).queue();
		}
		else {
			e.getChannel().sendMessage(message.setColor(Color.RED).setDescription("Parameter not accepted. Please review the available parameters for this command").build()).queue();
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("The user {} has used the Rss command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}
}
