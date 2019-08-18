package filter;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Channels;
import core.Hashes;
import fileManagement.GuildIni;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import sql.Azrael;
import util.STATIC;

public class URLFilter implements Runnable{
	private final static Logger logger = LoggerFactory.getLogger(URLFilter.class);

	MessageReceivedEvent e;
	MessageUpdateEvent e2;
	List<String> lang;
	
	public URLFilter(MessageReceivedEvent _e, MessageUpdateEvent _e2, List<String> _lang) {
		this.e = _e;
		this.e2 = _e2;
		this.lang = _lang;
	}
	
	@Override
	public void run() {
		var guild_id = (e != null ? e.getGuild().getIdLong() : e2.getGuild().getIdLong());
		if(verifyChannel((e != null ? e.getTextChannel().getIdLong() : e2.getTextChannel().getIdLong()), guild_id)) {
			Pattern urlPattern = Pattern.compile("[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b");
			Matcher matcher = urlPattern.matcher((e != null ? e.getMessage().getContentRaw() : e2.getMessage().getContentRaw()));
			if(matcher.find()) {
				var foundURL = matcher.group(0);
				var fullBlacklist = GuildIni.getURLBlacklist(guild_id);
				if(fullBlacklist) {
					if(Hashes.findGlobalURLBlacklist(foundURL)) {
						//Whitelist check, if this url should be ignored
						var whitelist = Azrael.SQLgetURLWhitelist(guild_id);
						if(whitelist == null || whitelist.parallelStream().filter(f -> foundURL.contains(f)).findAny().orElse(null) == null)
							printMessage(e, e2, foundURL, fullBlacklist, buildReplyMessageLang(lang));
					}
					else {
						//Do a web check and confirm that the url is valid or not and then insert into global blacklist
						try {
							if(pingHost(foundURL)) {
								Hashes.addGlobalURLBlacklist(foundURL);
								//check the url with the whitelist and don't delete message if found
								var whitelist = Azrael.SQLgetURLWhitelist(guild_id);
								if(whitelist == null || whitelist.parallelStream().filter(f -> foundURL.contains(foundURL)).findAny().orElse(null) == null)
									printMessage(e, e2, foundURL, fullBlacklist, buildReplyMessageLang(lang));
							}
						} catch (MalformedURLException e1) {
							logger.error("URL malformed error", e1);
						} catch (IOException e1) {
							logger.warn("Invalid URL");
						}
					}
				}
				else {
					//confront link with the blacklist table and delete if found
					var blacklist = Azrael.SQLgetURLBlacklist(guild_id);
					if(blacklist != null && blacklist.size() > 0 && blacklist.parallelStream().filter(f -> foundURL.contains(f)).findAny().orElse(null) != null)
						printMessage(e, e2, foundURL, fullBlacklist, buildReplyMessageLang(lang));
				}
			}
		}
	}
	
	private static boolean verifyChannel(long channel_id, long guild_id) {
		var channels = Azrael.SQLgetChannels(guild_id);
		var channel = channels.parallelStream().filter(f -> f.getURLCensoring() && f.getChannel_ID() == channel_id).findAny().orElse(null);
		if(channel != null)
			return true;
		else
			return false;
	}
	
	private static boolean pingHost(String foundURL) throws MalformedURLException, IOException {
		//http ping
		URL url = new URL("http://"+foundURL);
		URLConnection con = url.openConnection();
		con.setConnectTimeout(5000);
		con.setReadTimeout(5000);
		con.connect();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		if(in.readLine() != null) {
			return true;
		}
		
		//https ping
		url = new URL("https://"+foundURL);
		con = url.openConnection();
		con.setConnectTimeout(5000);
		con.setReadTimeout(5000);
		con.connect();
		in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		if(in.readLine() != null) {
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("preview")
	private static String [] buildReplyMessageLang(List<String> lang) {
		String [] output = new String[3];
		if(lang.size() == 1) {
			switch(lang.get(0)) {
				case "ger" -> {
					output[0] = " Die Nachricht wurde wegen eines nicht erlaubten URL entfernt!";
					output[1] = " Dies ist deine zweite Warnung. Eine weitere entfernte Nachricht und du wirst auf diesem Server **stumm geschaltet**!";
					output[2] = " Die Eingabe von URLs ist auf diesem Server nicht gestattet! URL entfernt!";
				}
				case "fre" -> {
					output[0] = " Le message a été supprimé pour l'affichage d'une url non autorisée !";
					output[1] = " C'est votre deuxième avertissement. Encore une fois et vous serez **mis sous silence** sur le serveur !";
					output[2] = " La saisie d'URLs n'est pas autorisée sur ce serveur ! URL supprimée !";
				}
				default -> {
					output[0] = " Message has been removed for posting a not allowed url!";
					output[1] = " This has been the second warning. One more and you'll be **muted** from the server!";
					output[2] = " The input of URLs is not allowed on this server! URL removed!";
				}
			}
		}
		else {
			output[0] = " Message has been removed for posting a not allowed url!";
			output[1] = " This has been the second warning. One more and you'll be **muted** from the server!";
			output[2] = " The input of URLs is not allowed on this server! URL removed!";
		}
		return output;
	}
	
	private static void printMessage(MessageReceivedEvent e, MessageUpdateEvent e2, String foundURL, boolean defaultBlacklist, String [] output) {
		Channels tra_channel;
		if(e != null) {
			e.getMessage().delete().reason("Not allowed URL found!").complete();
			if(defaultBlacklist) {e.getTextChannel().sendMessage(e.getMember().getAsMention()+output[2]).queue();}
			else {STATIC.handleRemovedMessages(e, e2, output);}
			tra_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type().equals("tra")).findAny().orElse(null);
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
		else {
			e2.getMessage().delete().reason("Not allowed URL found!").complete();
			if(defaultBlacklist) {e2.getTextChannel().sendMessage(e2.getMember().getAsMention()+output[2]).queue();}
			else {STATIC.handleRemovedMessages(e, e2, output);}
			tra_channel = Azrael.SQLgetChannels(e2.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type().equals("tra")).findAny().orElse(null);
			if(tra_channel != null) {
				e2.getGuild().getTextChannelById(tra_channel.getChannel_ID()).sendMessage(new EmbedBuilder()
					.setDescription(e2.getMessage().getContentRaw())
					.setColor(Color.ORANGE)
					.setTitle("Message removed!")
					.addField("NAME", e2.getMember().getUser().getAsMention(), true)
					.addField("USER ID", e2.getMember().getUser().getId(), true)
					.addField("CHANNEL", e2.getTextChannel().getAsMention(), true)
					.addField("TYPE", "URL", true)
					.addField("FQDN", foundURL, true)
					.setThumbnail(e2.getMember().getUser().getEffectiveAvatarUrl())
					.build()
				).queue();
			}
		}
	}
}
