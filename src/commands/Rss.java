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
			e.getChannel().sendMessage(message.setDescription("Use this command to set up RSS pages that will be displayed in a dedicated channel:\n\n"
					+ "**-register**: register an rss url for this server\n"
					+ "**-format**: change the format of how rss feeds should be displayed\n"
					+ "**-remove**: remove an rss url for this server\n"
					+ "**-test**: picks the first rss feeed to test the settings\n"
					+ "**-display**: display the current registered feeds for this server").build()).queue();
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase("-register")) {
			message.setColor(Color.BLUE);
			e.getChannel().sendMessage(message.setDescription("Please insert an URL to register the rss page").build()).queue();
		}
		else if(args.length > 1 && args[0].equalsIgnoreCase("-register")) {
			//register a link
			String input = args[1];
			if(Azrael.SQLInsertRSS(input, e.getGuild().getIdLong()) > 0) {
				message.setColor(Color.BLUE);
				e.getChannel().sendMessage(message.setDescription("RSS has been registered").build()).queue();
				logger.debug("{} RSS link has been registered for guild {}", input, e.getGuild().getId());
			}
			else {
				message.setColor(Color.RED);
				e.getChannel().sendMessage(message.setDescription("RSS link couldn't be registered. Either the link has been already registered or an internal error occurred").build()).queue();
				logger.error("{} RSS link couldn't be registered for guild {}", input, e.getGuild().getId());
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
			e.getChannel().sendMessage(message.setDescription("Please select a digit for the RSS feed to be removed:\n\n"+(out.length() > 0 ? out.toString(): "<no rss feeds have been registered>")).build()).queue();
			logger.debug("{} chose to remove a feed", e.getMember().getUser().getId());
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
			e.getChannel().sendMessage(message.setDescription("Please select a digit for the RSS feed to be personalized:\n\n"+(out.length() > 0 ? out.toString(): "<no rss feeds have been registered>")).build()).queue();
			logger.debug("{} chose to change the format of a feed", e.getMember().getUser().getId());
			if(out.length() > 0)
				Hashes.addTempCache("rss_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(180000, "format"));
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
			e.getChannel().sendMessage(message.setDescription("Please select a digit for the RSS that needs to be tested:\n\n"+(out.length() > 0 ? out.toString(): "<no rss feeds have been registered>")).build()).queue();
			logger.debug("{} chose to change the format of a feed", e.getMember().getUser().getId());
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
			e.getChannel().sendMessage(message.setDescription("These are the registered rss feeds:\n\n"+(out.length() > 0 ? out.toString(): "<no rss feeds have been registered>")).build()).queue();
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
