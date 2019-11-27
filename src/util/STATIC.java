package util;

/**
 * The STATIC class contains the current version number of the bot,
 * global variables that get initialized from the config.ini file
 * or by passing parameters on bot launch.
 * 
 * It additionally contains methods to retrieve the mysql url String,
 * to collect running threads, to terminate specific collected threads,
 * to handle removed messages by the filter, and other things which are
 * used in multiple classes.  
 */

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import constructors.Channels;
import core.Hashes;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import sql.Azrael;
import sql.DiscordRoles;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class STATIC {
	private final static Logger logger = LoggerFactory.getLogger(STATIC.class);
	private static final String VERSION = "6.9.372";
	private static String TOKEN = "";
	private static String SESSION_NAME = "";
	private static long ADMIN = 0;
	private static String TIMEZONE = "";
	private static String ACTIONLOG = "";
	private static String DOUBLEEXPERIENCE = "";
	private static String DOUBLEEXPERIENCESTART = "";
	private static String DOUBLEEXPERIENCEEND = "";
	private static String COUNTMEMBERS = "";
	private static String GAMEMESSAGE = "";
	private static String FILELOGGER = "";
	private static String TEMP = "";
	private static TwitterFactory twitterFactory = null;
	private static final CopyOnWriteArrayList<Thread> threads = new CopyOnWriteArrayList<Thread>();
	private static final CopyOnWriteArrayList<Timer> timers = new CopyOnWriteArrayList<Timer>();
	
	public static String getVersion() {
		return VERSION;
	}
	
	//default mysql url String to access the database. As parameters, the database name and ip address to the mysql server are required
	public static String getDatabaseURL(final String _dbName, final String _ip) {
		return "jdbc:mysql://"+_ip+"/"+_dbName+"?autoReconnect=true&useSSL=true&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone="+IniFileReader.getTimezone();
	}
	public static String getDatabaseURL(final String _dbName, final String _ip, String _param) {
		return "jdbc:mysql://"+_ip+"/"+_dbName+"?autoReconnect=true&useSSL=true&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone="+IniFileReader.getTimezone()+_param;
	}
	
	public static void setToken(String _token) {
		TOKEN = _token;
	}
	public static String getToken() {
		return TOKEN;
	}
	
	public static void setSessionName(String _name) {
		SESSION_NAME = _name;
	}
	public static String getSessionName() {
		return SESSION_NAME;
	}
	
	public static void setAdmin(long _admin) {
		ADMIN = _admin;
	}
	public static long getAdmin() {
		return ADMIN;
	}
	
	public static void setTimezone(String _timezone) {
		TIMEZONE = _timezone;
	}
	public static String getTimezone() {
		return TIMEZONE;
	}
	
	public static void setActionLog(String _actionLog) {
		ACTIONLOG = _actionLog;
	}
	public static String getActionLog() {
		return ACTIONLOG;
	}
	
	public static void setDoubleExperience(String _doubleExperience) {
		DOUBLEEXPERIENCE = _doubleExperience;
	}
	public static String getDoubleExperience() {
		return DOUBLEEXPERIENCE;
	}
	
	public static void setDoubleExperienceStart(String _doubleExperienceStart) {
		DOUBLEEXPERIENCESTART = _doubleExperienceStart;
	}
	public static String getDoubleExperienceStart() {
		return DOUBLEEXPERIENCESTART;
	}
	
	public static void setDoubleExperienceEnd(String _doubleExperienceEnd) {
		DOUBLEEXPERIENCEEND = _doubleExperienceEnd;
	}
	public static String getDoubleExperienceEnd() {
		return DOUBLEEXPERIENCEEND;
	}
	
	public static void setCountMembers(String _countMembers) {
		COUNTMEMBERS = _countMembers;
	}
	public static String getCountMembers() {
		return COUNTMEMBERS;
	}
	
	public static void setGameMessage(String _gameMessage) {
		GAMEMESSAGE = _gameMessage;
	}
	public static String getGameMessage() {
		return GAMEMESSAGE;
	}
	
	public static void setFileLogger(String _fileLogger) {
		FILELOGGER = _fileLogger;
	}
	public static String getFileLogger() {
		return FILELOGGER;
	}
	
	public static void setTemp(String _temp) {
		TEMP = _temp;
	}
	public static String getTemp() {
		return TEMP;
	}
	
	//collect a running thread (for example a user mute due to the Thread.sleep) to concurrent array and assign a name to it
	public static void addThread(Thread thread, final String name) {
		if(threads.parallelStream().filter(f -> f.getName().equals(name)).findAny().orElse(null) != null)
			return;
		
		thread.setName(name);
		threads.add(thread);
	}
	//kill a specific thread basing by the name of the thread
	public static boolean killThread(final String name) {
		var thread = threads.parallelStream().filter(f -> f.getName().equals(name)).findAny().orElse(null);
		if(thread != null) {
			thread.interrupt();
			threads.remove(thread);
			return true;
		}
		else {
			return false;
		}
	}
	//remove a thread from the array. Either gets called from the method killThread or after the thread in question terminates on its own
	public static void removeThread(final Thread thread) {
		threads.remove(thread);
	}
	
	//collect a timer into the concurrent array
	public static void addTimer(Timer timer) {
		if(timers.parallelStream().filter(f -> f.equals(timer)).findAny().orElse(null) != null)
			return;
		
		timers.add(timer);
	}
	
	//terminate all currently running timers
	public static void killAllTimers() {
		for(Timer timer : timers) {
			timer.cancel();
		}
		timers.clear();
	}
	
	//retrieve a list of channels in String format. For example when a command can't be executed and then has to reference to one or more bot channel(s)
	public static String getChannels(List<Channels> channels) {
		StringBuilder out = new StringBuilder();
		var first = true;
		var last = channels.size()-1;
		for(Channels channel : channels) {
			if(first)
				out.append("<#"+channel.getChannel_ID()+">");
			else if(channels.get(last).getChannel_ID() == channel.getChannel_ID())
				out.append(" or <#"+channel.getChannel_ID()+">");
			else
				out.append(", <#"+channel.getChannel_ID()+">");
		}
		return out.toString();
	}
	
	//define the default privilege level for different role types. Used during role registrations. 0 is lowest and 100 is highest privilege
	@SuppressWarnings("preview")
	public static int getLevel(String category) {
		return switch(category) {
			case "adm" -> 100;
			case "mod" -> 20;
			case "bot" -> 10;
			case "com" -> 1;
			default    -> 0;
		};
	}
	
	//Called by LanguageFilter and LanguageEditFilter class. Keeps track of removed messages, warns the user accordingly and mutes when the limit has been reached
	public static void handleRemovedMessages(GuildMessageReceivedEvent e, GuildMessageUpdateEvent e2, String [] output) {
		Member member = (e != null ? e.getMember() : e2.getMember());
		Guild guild = (e != null ? e.getGuild() : e2.getGuild());
		TextChannel channel = (e != null ? e.getChannel() : e2.getChannel());
		logger.debug("Message removed from {} in guild {}", member.getUser().getId(), guild.getId());
		var muteRole = DiscordRoles.SQLgetRoles(guild.getIdLong()).parallelStream().filter(f -> f.getCategory_ABV().equals("mut")).findAny().orElse(null);
		if(muteRole == null) {
			channel.sendMessage(member.getAsMention()+" "+output[0]).queue();
		}
		else {
			var cache = Hashes.getTempCache("report_gu"+guild.getId()+"us"+member.getUser().getId());
			if(cache == null || cache.getExpiration() - System.currentTimeMillis() <= 0) {
				channel.sendMessage(member.getAsMention()+" "+output[0]).queue();
				Hashes.addTempCache("report_gu"+guild.getId()+"us"+member.getUser().getId(), new Cache(300000, "1"));
			}
			else if(cache != null) {
				if(cache.getAdditionalInfo().equals("1")) {
					channel.sendMessage(member.getAsMention()+" "+output[1]).queue();
					Hashes.addTempCache("report_gu"+guild.getId()+"us"+member.getUser().getId(), new Cache(300000, "2"));
				}
				else if(cache.getAdditionalInfo().equals("2")) {
					if(guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
						guild.addRoleToMember(member, guild.getRoleById(muteRole.getRole_ID())).queue();
					}
					else {
						final var log_channel = Azrael.SQLgetChannels(guild.getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("log")).findAny().orElse(null);
						if(log_channel != null) guild.getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Mute role couldn't be assigned after the third censoring warning because the MANAGE ROLES permission is missing!").build()).queue();
						logger.warn("MANAGE ROLES permission required to mute members for guild {}!", guild.getId());
					}
					Hashes.clearTempCache("report_gu"+guild.getId()+"us"+member.getUser().getId());
				}
			}
		}
	}
	
	//remove the watch state from a user that either got banned or kicked from a server
	public static void handleUnwatch(GuildBanEvent e, GuildMemberLeaveEvent e2, short type) {
		var user_id = (e != null ? e.getUser().getIdLong() : e2.getMember().getUser().getIdLong());
		var guild_id = (e != null ? e.getGuild().getIdLong() : e2.getGuild().getIdLong());
		var unwatchReason = (type == 1 ? "ban" : "kick");
		var watchedUser = Azrael.SQLgetWatchlist(user_id, guild_id);
		if(watchedUser != null) {
			if(Azrael.SQLDeleteWatchlist(user_id, guild_id) > 0) {
				e.getGuild().getTextChannelById(watchedUser.getWatchChannel()).sendMessage(new EmbedBuilder().setColor(Color.YELLOW).setTitle("Watch lifted due to "+unwatchReason+"!")
					.setDescription("The watch for the user "+e.getUser().getName()+"#"+e.getUser().getDiscriminator()+" has been removed due to a "+unwatchReason+"!").build()).queue();
				Hashes.removeWatchlist(guild_id+"-"+user_id);
				logger.debug("The user {} has been removed from the watchlist in guild {}", user_id, guild_id);
			}
			else {
				e.getGuild().getTextChannelById(watchedUser.getWatchChannel()).sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Error!")
					.setDescription("An internal error occurred. The user "+e.getUser().getName()+"#"+e.getUser().getDiscriminator()+" couldn't be removed from Azrael.watchlist!").build()).queue();
				logger.error("An internal error occurred. The user {} couldn't be removed from the Azrael.watchlist table for guild {}", user_id, guild_id);
			}
		}
	}
	
	//method to trust all certificates and to retrieve the html code from a webpage
	public static BufferedReader retrieveWebPageCode(String link) throws IOException {
 
        try {
        	// Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };
        	// Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			
			// Create all-trusting host name verifier
	        HostnameVerifier allHostsValid = new HostnameVerifier() {
	            public boolean verify(String hostname, SSLSession session) {
	                return true;
	            }
	        };
	        
	     // Install the all-trusting host verifier
	        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		} catch (KeyManagementException | NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
		URL url = new URL(link);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
		con.setConnectTimeout(10000);
		con.setReadTimeout(10000);
		return new BufferedReader(new InputStreamReader(con.getInputStream()));
	}
	
	//login into twitter. Only done if required and the bot has been set up to collect tweets with a specific hashtag
	public static void loginTwitter() {
		if(twitterFactory == null) {
			final var tokens = IniFileReader.getTwitterKeys();
			if(tokens[0] != null && tokens[1] != null && tokens[2] != null && tokens[3] != null) {
				ConfigurationBuilder cb = new ConfigurationBuilder()
						.setOAuthConsumerKey(tokens[0])
						.setOAuthConsumerSecret(tokens[1])
						.setOAuthAccessToken(tokens[2])
						.setOAuthAccessTokenSecret(tokens[3]);
				twitterFactory = new TwitterFactory(cb.build());
			}
		}
	}
	
	public static TwitterFactory getTwitterFactory() {
		return twitterFactory;
	}
	
	@SuppressWarnings("preview")
	public static int returnEmote(String reactionName) {
		return switch(reactionName) {
			case "one" 	 -> 0;
			case "two"   -> 1;
			case "three" -> 2;
			case "four"  -> 3;
			case "five"  -> 4;
			case "six" 	 -> 5;
			case "seven" -> 6;
			case "eight" -> 7;
			case "nine"  -> 8;
			default 	 -> 9;
		};
	}
}
