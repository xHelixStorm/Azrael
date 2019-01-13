package commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.RSS;
import core.UserPrivs;
import fileManagement.FileSetting;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.Azrael;

public class Rss implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(IniFileReader.getRssCommand()) {
			EmbedBuilder message = new EmbedBuilder();
			if(UserPrivs.isUserAdmin(e.getMember().getUser(), e.getGuild().getIdLong()) || UserPrivs.isUserMod(e.getMember().getUser(), e.getGuild().getIdLong()) || IniFileReader.getAdmin() == e.getMember().getUser().getIdLong()) {
				Logger logger = LoggerFactory.getLogger(Rss.class);
				if(e.getMessage().getContentRaw().equals(IniFileReader.getCommandPrefix()+"rss")) {
					//throw default message with instructions
					message.setColor(Color.BLUE);
					e.getTextChannel().sendMessage(message.setDescription("Use this command to set up RSS pages that will be displayed in a dedicated channel:\n\n"
							+ "**-register**: register an rss url for this server\n"
							+ "**-format**: change the format of how rss feeds should be displayed\n"
							+ "**-remove**: remove an rss url for this server\n"
							+ "**-test**: picks the first rss feeed to test the settings\n"
							+ "**-display**: display the current registered feeds for this server").build()).queue();
				}
				else if(e.getMessage().getContentRaw().equals(IniFileReader.getCommandPrefix()+"rss -register")) {
					message.setColor(Color.BLUE);
					e.getTextChannel().sendMessage(message.setDescription("Please insert an URL to register the rss page").build()).queue();
				}
				else if(e.getMessage().getContentRaw().contains(IniFileReader.getCommandPrefix()+"rss -register ")) {
					//register a link
					String input = e.getMessage().getContentRaw().substring(IniFileReader.getCommandPrefix().length()+14);
					if(Azrael.SQLInsertRSS(input, e.getGuild().getIdLong()) > 0) {
						message.setColor(Color.BLUE);
						e.getTextChannel().sendMessage(message.setDescription("RSS has been registered").build()).queue();
						logger.debug("{} RSS link has been registered for guild {}", input, e.getGuild().getId());
					}
					else {
						message.setColor(Color.RED);
						e.getTextChannel().sendMessage(message.setDescription("RSS link couldn't be registered. Either the link has been already registered or an internal error occurred").build()).queue();
						logger.error("{} RSS link couldn't be registered for guild {}", input, e.getGuild().getId());
					}
				}
				else if(e.getMessage().getContentRaw().contains(IniFileReader.getCommandPrefix()+"rss -remove")) {
					int counter = 1;
					StringBuilder out = new StringBuilder();
					for(RSS feed : Azrael.SQLgetRSSFeeds(e.getGuild().getIdLong())) {
						out.append("**Link "+counter+":** "+feed.getURL()+"\n");
						counter++;
					}
					message.setColor(Color.BLUE);
					e.getTextChannel().sendMessage(message.setDescription("Please select a digit for the RSS feed to be removed:\n\n"+(out.length() > 0 ? out.toString(): "<no rss feeds have been registered>")).build()).queue();
					logger.debug("{} chose to remove a feed", e.getMember().getUser().getId());
					if(out.length() > 0)
						FileSetting.createFile(IniFileReader.getTempDirectory()+"AutoDelFiles/rss_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+".azr", "remove");
				}
				else if(e.getMessage().getContentRaw().equals(IniFileReader.getCommandPrefix()+"rss -format")) {
					int counter = 1;
					StringBuilder out = new StringBuilder();
					for(RSS feed : Azrael.SQLgetRSSFeeds(e.getGuild().getIdLong())) {
						out.append("**Link "+counter+":** "+feed.getURL()+"\n");
						counter++;
					}
					message.setColor(Color.BLUE);
					e.getTextChannel().sendMessage(message.setDescription("Please select a digit for the RSS feed to be personalized:\n\n"+(out.length() > 0 ? out.toString(): "<no rss feeds have been registered>")).build()).queue();
					logger.debug("{} chose to change the format of a feed", e.getMember().getUser().getId());
					if(out.length() > 0)
						FileSetting.createFile(IniFileReader.getTempDirectory()+"AutoDelFiles/rss_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+".azr", "format");
				}
				else if(e.getMessage().getContentRaw().equals(IniFileReader.getCommandPrefix()+"rss -test")) {
					//test a feed
					int counter = 1;
					StringBuilder out = new StringBuilder();
					for(RSS feed : Azrael.SQLgetRSSFeeds(e.getGuild().getIdLong())) {
						out.append("**Link "+counter+":** "+feed.getURL()+"\n");
						counter++;
					}
					message.setColor(Color.BLUE);
					e.getTextChannel().sendMessage(message.setDescription("Please select a digit for the RSS that needs to be tested:\n\n"+(out.length() > 0 ? out.toString(): "<no rss feeds have been registered>")).build()).queue();
					logger.debug("{} chose to change the format of a feed", e.getMember().getUser().getId());
					if(out.length() > 0)
						FileSetting.createFile(IniFileReader.getTempDirectory()+"AutoDelFiles/rss_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId()+".azr", "test");
				}
				else if(e.getMessage().getContentRaw().equals(IniFileReader.getCommandPrefix()+"rss -display")) {
					//display the registered feeds
					int counter = 1;
					StringBuilder out = new StringBuilder();
					for(RSS feed : Azrael.SQLgetRSSFeeds(e.getGuild().getIdLong())) {
						out.append("**Link "+counter+":** "+feed.getURL()+"\n");
						counter++;
					}
					message.setColor(Color.BLUE);
					e.getTextChannel().sendMessage(message.setDescription("These are the registered rss feeds:\n\n"+(out.length() > 0 ? out.toString(): "<no rss feeds have been registered>")).build()).queue();
				}
				else {
					e.getTextChannel().sendMessage(message.setColor(Color.RED).setDescription("Parameter not accepted. Please review the available parameters for this command").build()).queue();
				}
			}
			else {
				e.getTextChannel().sendMessage(message.setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setDescription(e.getMember().getAsMention() + " **My apologies young padawan. This command can be used only from an Administrator or an Moderator. Here a cookie** :cookie:").build()).queue();
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
