package filter;

/**
 * This class gets executed when a message has been sent into a channel
 * that has been registered to filter urls. It gets called from 
 * GuildMessageListener and GuildMessageEditListener.
 * 
 * Depending if the full url deletion mode has been enabled by the
 * guild ini file, this class will either remove messages, which contain
 * a url, basing a blacklist or ignore urls basing a whitelist. 
 * Depending on the guild mode, only the blacklist or whitelist can be
 * used.
 */

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLHandshakeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Channels;
import core.Hashes;
import core.UserPrivs;
import fileManagement.GuildIni;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import sql.Azrael;
import util.STATIC;

public class URLFilter implements Runnable{
	private final static Logger logger = LoggerFactory.getLogger(URLFilter.class);

	GuildMessageReceivedEvent e;
	GuildMessageUpdateEvent e2;
	List<String> lang;
	List<Channels> allChannels;
	
	public URLFilter(GuildMessageReceivedEvent _e, GuildMessageUpdateEvent _e2, List<String> _lang, List<Channels> _allChannels) {
		this.e = _e;
		this.e2 = _e2;
		this.lang = _lang;
		this.allChannels = _allChannels;
	}
	
	@Override
	public void run() {
		var guild_id = (e != null ? e.getGuild().getIdLong() : e2.getGuild().getIdLong());
		Member member = (e != null ? e.getMember() : e2.getMember());
		//filtered has to be everything that wasn't posted by a bot, moderator or administrator
		if(!UserPrivs.isUserBot(member) && !UserPrivs.isUserMod(member) && !UserPrivs.isUserAdmin(member)) {
			//get the whole message
			String incomingURL = (e != null ? e.getMessage().getContentRaw() : e2.getMessage().getContentRaw());
			//Regex pattern to retrieve a full url (e.g github.com/xHelixStorm/Azrael) which the retrieved message could contain
			Pattern urlPattern = Pattern.compile("[\\w]{1,1}[a-zA-Z0-9@:%._+~#=-]{0,256}\\.[a-zA-Z]{1,1}[a-zA-Z0-9()]{1,5}[\\/a-zA-z0-9\\-?=&%.,+]*\\b");
			Matcher matcher = urlPattern.matcher(incomingURL);
			//proceed if an url have been found. Repeat search in case of false detections
			while(matcher.find()) {
				//save the found value to variable 
				var foundURL = matcher.group();
				//allow urls only if they are larger than 4 letters and if multiple dots aren't written together (e.g youtube..com)
				if(foundURL.length() >= 4 && !Pattern.compile("[.]{2,}").matcher(foundURL).find()) {
					//get the fully qualified domain name with the second regex
					var shortURL = "";
					Matcher matcher2 = Pattern.compile("[\\w]{1,1}[a-zA-Z0-9@:%._+~#=-]{0,256}\\.[a-zA-Z]{1,1}[a-zA-Z0-9()]{1,5}\\b").matcher(foundURL);
					if(matcher2.find())
						shortURL = matcher2.group();
					final var fqdn = shortURL;
					//check if the bot is in full url delete mode for this server, if not, remove urls basing a blacklist table 
					var fullBlacklist = GuildIni.getURLBlacklist(guild_id);
					if(fullBlacklist) {
						//remove url if that fqdn had been used before
						if(Hashes.findGlobalURLBlacklist(fqdn)) {
							//Whitelist check, if this url should be ignored
							var whitelist = Azrael.SQLgetURLWhitelist(guild_id);
							if(whitelist == null || whitelist.parallelStream().filter(f -> fqdn.contains(f)).findAny().orElse(null) == null) {
								printMessage(e, e2, fqdn, fullBlacklist, buildReplyMessageLang(lang), allChannels);
								break;
							}
						}
						else {
							//Do a ping check and confirm that the url is valid or not and then insert into global blacklist
							try {
								if(pingHost(foundURL)) {
									Hashes.addGlobalURLBlacklist(fqdn);
									//check the url with the whitelist and don't delete message if found
									var whitelist = Azrael.SQLgetURLWhitelist(guild_id);
									if(whitelist == null || whitelist.parallelStream().filter(f -> fqdn.contains(f)).findAny().orElse(null) == null) {
										printMessage(e, e2, fqdn, fullBlacklist, buildReplyMessageLang(lang), allChannels);
										break;
									}
								}
							} catch(UnknownHostException e1) {
								logger.warn("Invalid URL {} for guild {}", foundURL, guild_id);
							} catch (MalformedURLException e1) {
								logger.error("URL malformed error for url {} and guild {}", foundURL, guild_id, e1);
							} catch (IOException e1) {
								logger.warn("Invalid URL {} for guild {}", foundURL, guild_id, e1);
							}
						}
					}
					else {
						//confront link with the blacklist table and delete if found
						var blacklist = Azrael.SQLgetURLBlacklist(guild_id);
						if(blacklist != null && blacklist.size() > 0 && blacklist.parallelStream().filter(f -> foundURL.contains(f)).findAny().orElse(null) != null) {
							printMessage(e, e2, foundURL, fullBlacklist, buildReplyMessageLang(lang), allChannels);
							break;
						}
					}
				}
			}
		}
	}
	
	private static boolean pingHost(String foundURL) throws MalformedURLException, IOException {
		try {
			//http ping
			URL url = new URL("http://"+foundURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
			con.setConnectTimeout(5000);
			con.setReadTimeout(5000);
			con.connect();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			if(in.readLine() != null) {
				return true;
			}
			
			//https ping
			url = new URL("https://"+foundURL);
			con = (HttpURLConnection) url.openConnection();
			con.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
			con.setConnectTimeout(5000);
			con.setReadTimeout(5000);
			con.connect();
			in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			if(in.readLine() != null) {
				return true;
			}
			return false;
		} catch(SSLHandshakeException e) {
			//still return true if there are any certificate issues but the website exists
			return true;
		}
	}
	
	@SuppressWarnings("preview")
	private static String [] buildReplyMessageLang(List<String> lang) {
		String [] output = new String[3];
		if(lang.size() == 1) {
			//defined messages for german, french and the rest in english
			switch(lang.get(0)) {
				case "ger" -> {
					output[0] = " Die Nachricht wurde wegen eines nicht erlaubten URL entfernt!";
					output[1] = " Dies ist deine zweite Warnung. Eine weitere entfernte Nachricht und du wirst auf diesem Server **stumm geschaltet**!";
					output[2] = " Die Eingabe von URLs ist auf diesem Kanal nicht gestattet! URL entfernt!";
				}
				case "fre" -> {
					output[0] = " Le message a été supprimé pour l'affichage d'une url non autorisée !";
					output[1] = " C'est votre deuxième avertissement. Encore une fois et vous serez **mis sous silence** sur le serveur !";
					output[2] = " La saisie d'URLs n'est pas autorisée sur ce chenal ! URL supprimée !";
				}
				default -> {
					output[0] = " Message has been removed for posting a not allowed url!";
					output[1] = " This has been the second warning. One more and you'll be **muted** from the server!";
					output[2] = " The input of URLs is not allowed in this channel! URL removed!";
				}
			}
		}
		//always use english if multiple filter languages have been set for a channel or if no filter language has been set
		else {
			output[0] = " Message has been removed for posting a not allowed url!";
			output[1] = " This has been the second warning. One more and you'll be **muted** from the server!";
			output[2] = " The input of URLs is not allowed in this channel! URL removed!";
		}
		return output;
	}
	
	private static void printMessage(GuildMessageReceivedEvent e, GuildMessageUpdateEvent e2, String foundURL, boolean defaultBlacklist, String [] output, List<Channels> allChannels) {
		//for sent messages
		if(e != null) {
			//delete message which contains the url
			e.getMessage().delete().reason("Not allowed URL found!").queue(success -> {
				//if the overall blacklist is enabled, print the message that urls are not allowed, else keep count of the removed messages and warn the user
				if(defaultBlacklist) {e.getChannel().sendMessage(e.getMember().getAsMention()+output[2]).queue();}
				else {STATIC.handleRemovedMessages(e, e2, output);}
				//retrieve the trash channel to print the removed message. If not available, don't print any message
				final Channels tra_channel = allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("tra")).findAny().orElse(null);
				if(tra_channel != null) {
					e.getGuild().getTextChannelById(tra_channel.getChannel_ID()).sendMessage(new EmbedBuilder()
						.setDescription(e.getMessage().getContentRaw())
						.setColor(Color.ORANGE)
						.setTitle("Message removed!")
						.addField("NAME", e.getMember().getAsMention(), true)
						.addField("USER ID", e.getMember().getUser().getId(), true)
						.addField("CHANNEL", e.getChannel().getAsMention(), true)
						.addField("TYPE", "URL", true)
						.addField("FQDN", foundURL, true)
						.setThumbnail(e.getMember().getUser().getEffectiveAvatarUrl())
						.build()
					).queue();
				}
			}, error -> {
				//when the message already has been removed, usually by another bot
				logger.warn("Message containing a url has been already deleted! URL: {}", foundURL);
			});
		}
		//for edited messages
		else {
			//delete message which contains the url
			e2.getMessage().delete().reason("Not allowed URL found!").queue(success -> {
				//if the overall blacklist is enabled, print the message that urls are not allowed, else keep count of the removed messages and warn the user
				if(defaultBlacklist) {e2.getChannel().sendMessage(e2.getMember().getAsMention()+output[2]).queue();}
				else {STATIC.handleRemovedMessages(e, e2, output);}
				//retrieve the trash channel to print the removed message. If not available, don't print any message
				final Channels tra_channel = allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("tra")).findAny().orElse(null);
				if(tra_channel != null) {
					e2.getGuild().getTextChannelById(tra_channel.getChannel_ID()).sendMessage(new EmbedBuilder()
						.setDescription(e2.getMessage().getContentRaw())
						.setColor(Color.ORANGE)
						.setTitle("Message removed!")
						.addField("NAME", e2.getMember().getAsMention(), true)
						.addField("USER ID", e2.getMember().getUser().getId(), true)
						.addField("CHANNEL", e2.getChannel().getAsMention(), true)
						.addField("TYPE", "URL", true)
						.addField("FQDN", foundURL, true)
						.setThumbnail(e2.getMember().getUser().getEffectiveAvatarUrl())
						.build()
					).queue();
				}
			}, error -> {
				//when the message already has been removed, usually by another bot
				logger.warn("Message containing a url has been already deleted! URL: {}", foundURL);
			});
		}
	}
}
