package filter;

import java.awt.Color;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import fileManagement.GuildIni;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sql.Azrael;

class URLFilter {
	private final static Logger logger = LoggerFactory.getLogger(URLFilter.class);

	public static void searchURL(MessageReceivedEvent e) {
		Pattern urlPattern = Pattern.compile("[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b");
		Matcher matcher = urlPattern.matcher(e.getMessage().getContentRaw());
		if(matcher.find()) {
			var foundURL = matcher.group(1);
			if(GuildIni.getURLBlacklist(e.getGuild().getIdLong())) {
				Azrael.SQLgetGlobalURLBlacklist();
				if(Hashes.findGlobalURLBlacklist(foundURL)) {
					//Whitelist check, if this url should be ignored
					var whitelist = Azrael.SQLgetURLWhitelist(e.getGuild().getIdLong());
					if(whitelist == null || whitelist.parallelStream().filter(f -> f.equals(foundURL)).findAny().orElse(null) == null)
						printMessage(e, foundURL);
				}
				else {
					//Do a web check and confirm that the url is valid or not and then insert into global blacklist
					try {
						pingHost(foundURL);
						if(Azrael.SQLInsertGlobalURLBlacklist(foundURL) > 0)
							Hashes.addGlobalURLBlacklist(foundURL);
						//check the url with the whitelist and don't delete message if found
						var whitelist = Azrael.SQLgetURLWhitelist(e.getGuild().getIdLong());
						if(whitelist == null || whitelist.parallelStream().filter(f -> f.equals(foundURL)).findAny().orElse(null) == null)
							printMessage(e, foundURL);
					} catch (MalformedURLException e1) {
						logger.error("URL malformed error", e1);
					} catch (IOException e1) {
						logger.warn("Invalid URL");
					}
				}
			}
			else {
				//confront link with the blacklist table and delete if found
				var blacklist = Azrael.SQLgetURLBlacklist(e.getGuild().getIdLong());
				if(blacklist != null && blacklist.parallelStream().filter(f -> f.equalsIgnoreCase(foundURL)).findAny().orElse(null) != null)
					printMessage(e, foundURL);
			}
		}
	}
	
	private static void pingHost(String foundURL) throws MalformedURLException, IOException {
		URL url = new URL(foundURL);
		URLConnection con = url.openConnection();
		con.setConnectTimeout(5000);
		con.setReadTimeout(5000);
		con.connect();
	}
	
	private static void printMessage(MessageReceivedEvent e, String foundURL) {
		e.getMessage().delete().reason("Not allowed URL found!").complete();
		var tra_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type().equals("tra")).findAny().orElse(null);
		if(tra_channel != null) {
			e.getGuild().getTextChannelById(tra_channel.getChannel_ID()).sendMessage(new EmbedBuilder()
				.setDescription(e.getMessage().getContentRaw())
				.setColor(Color.ORANGE)
				.setTitle("Message removed!")
				.addField("NAME", e.getMember().getUser().getAsMention(), true)
				.addField("USER ID", e.getMember().getUser().getId(), true)
				.addField("CHANNEL", e.getTextChannel().getAsMention(), true)
				.addField("TYPE", "URL", true)
				.addField("FQDN", foundURL, true)
				.setThumbnail(e.getMember().getUser().getEffectiveAvatarUrl())
				.build()
			).queue();
		}
	}

}
