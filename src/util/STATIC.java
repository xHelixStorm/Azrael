package util;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
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
	
	private static final String VERSION = "6.7.330";
	private static String TOKEN = "";
	private static String SESSION_NAME = "";
	private static long ADMIN = 0;
	private static String ACTIONLOG = "";
	private static String DOUBLEEXPERIENCE = "";
	private static String COUNTMEMBERS = "";
	private static String GAMEMESSAGE = "";
	private static TwitterFactory twitterFactory = null;
	private static final CopyOnWriteArrayList<Thread> threads = new CopyOnWriteArrayList<Thread>();
	private static final CopyOnWriteArrayList<Timer> timers = new CopyOnWriteArrayList<Timer>();
	
	public static String getVersion() {
		return VERSION;
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
	
	public static void addThread(Thread thread, final String name) {
		if(threads.parallelStream().filter(f -> f.getName().equals(name)).findAny().orElse(null) != null)
			return;
		
		thread.setName(name);
		threads.add(thread);
	}
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
	public static void removeThread(final Thread thread) {
		threads.remove(thread);
	}
	
	public static void addTimer(Timer timer) {
		if(timers.parallelStream().filter(f -> f.equals(timer)).findAny().orElse(null) != null)
			return;
		
		timers.add(timer);
	}
	
	public static void killAllTimers() {
		for(Timer timer : timers) {
			timer.cancel();
		}
		timers.clear();
	}
	
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
	
	@SuppressWarnings("unused")
	public static void handleRemovedMessages(GuildMessageReceivedEvent e, GuildMessageUpdateEvent e2, String [] output) {
		Logger logger = LoggerFactory.getLogger(STATIC.class);
		logger.debug("Message removed from {} in guild {}", (e != null ? e.getMember().getUser().getId() : e2.getMember().getUser().getId()), e.getGuild().getName());
		var muteRole = DiscordRoles.SQLgetRoles((e != null ? e.getGuild().getIdLong() : e2.getGuild().getIdLong())).parallelStream().filter(f -> f.getCategory_ABV().equals("mut")).findAny().orElse(null);
		if(muteRole == null) {
			if(e != null)e.getChannel().sendMessage(e.getMember().getAsMention()+" "+output[0]).queue();
			else 		 e2.getChannel().sendMessage(e2.getMember().getAsMention()+" "+output[0]).queue();
		}
		else {
			var cache = Hashes.getTempCache("report_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
			if(cache == null || cache.getExpiration() - System.currentTimeMillis() <= 0) {
				if(e != null)e.getChannel().sendMessage(e.getMember().getAsMention()+" "+output[0]).queue();
				else 		 e2.getChannel().sendMessage(e2.getMember().getAsMention()+" "+output[0]).queue();
				Hashes.addTempCache("report_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId(), new Cache(300000, "1"));
			}
			else if(cache != null) {
				if(cache.getAdditionalInfo().equals("1")) {
					if(e != null)e.getChannel().sendMessage(e.getMember().getAsMention()+" "+output[1]).queue();
					else 		 e2.getChannel().sendMessage(e2.getMember().getAsMention()+" "+output[1]).queue();
					Hashes.addTempCache("report_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId(), new Cache(300000, "2"));
				}
				else if(cache.getAdditionalInfo().equals("2")) {
					if(e != null)e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(muteRole.getRole_ID())).queue();
					else		 e.getGuild().addRoleToMember(e2.getMember(), e2.getGuild().getRoleById(muteRole.getRole_ID())).queue();
					Hashes.clearTempCache("report_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId());
				}
			}
		}
	}
	
	public static void handleUnwatch(GuildBanEvent e, GuildMemberLeaveEvent e2, short type) {
		var user_id = (e != null ? e.getUser().getIdLong() : e2.getMember().getUser().getIdLong());
		var guild_id = (e != null ? e.getGuild().getIdLong() : e2.getGuild().getIdLong());
		var unwatchReason = (type == 1 ? "ban" : "kick");
		var watchedUser = Hashes.getWatchlist(guild_id+"-"+user_id);
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
		URLConnection con = url.openConnection();
		con.setConnectTimeout(5000);
		con.setReadTimeout(10000);
		return new BufferedReader(new InputStreamReader(con.getInputStream()));
	}
	
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
