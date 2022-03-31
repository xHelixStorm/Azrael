package de.azrael.util;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiManager;

import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Cache;
import de.azrael.constructors.Channels;
import de.azrael.constructors.SpamDetection;
import de.azrael.core.Hashes;
import de.azrael.core.UserPrivs;
import de.azrael.enums.Channel;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.fileManagement.FileSetting;
import de.azrael.fileManagement.GuildIni;
import de.azrael.listeners.ShutdownListener;
import de.azrael.sql.Azrael;
import de.azrael.sql.BotConfiguration;
import de.azrael.sql.DiscordRoles;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * The STATIC class contains the current version number of the bot,
 * global variables that get initialized from the config.ini file
 * or by passing parameters on bot launch.
 * 
 * It additionally contains methods to retrieve the mysql url String,
 * to collect running threads, to terminate specific collected threads,
 * to handle removed messages by the filter, and other things which are
 * used in multiple classes.  
 * @author xHelixStorm
 *
 */

public class STATIC {
	private final static Logger logger = LoggerFactory.getLogger(STATIC.class);
	
	private static final String VERSION = "8.01.591";
	
	private static final JSONObject eng_lang = new JSONObject(FileSetting.readFile("./files/Languages/eng_lang.json"));
	private static final JSONObject ger_lang = new JSONObject(FileSetting.readFile("./files/Languages/ger_lang.json"));
	private static final JSONObject spa_lang = new JSONObject(FileSetting.readFile("./files/Languages/spa_lang.json"));
	private static final JSONObject rus_lang = new JSONObject(FileSetting.readFile("./files/Languages/rus_lang.json"));
	private static final JSONObject por_lang = new JSONObject(FileSetting.readFile("./files/Languages/por_lang.json"));
	private static final JSONObject fre_lang = new JSONObject(FileSetting.readFile("./files/Languages/fre_lang.json"));
	
	private static final String TWITTER_CONSUMER_KEY = "TWITTER_CONSUMER_KEY";
	private static final String TWITTER_CONSUMER_KEY_SECRET = "TWITTER_CONSUMER_KEY_SECRET";
	private static final String TWITTER_ACCESS_TOKEN = "TWITTER_ACCESS_TOKEN";
	private static final String TWITTER_ACCESS_TOKEN_SECRET = "TWITTER_ACCESS_TOKEN_SECRET";
	
	private static OffsetDateTime bootTime = null;
	
	private static TwitterFactory twitterFactory = null;
	private static final CopyOnWriteArrayList<Thread> threads = new CopyOnWriteArrayList<Thread>();
	private static final CopyOnWriteArrayList<Timer> timers = new CopyOnWriteArrayList<Timer>();
	
	public static String getVersion() {
		return VERSION;
	}
	
	private static String getMessage(String lang, Translation event) {
		return switch(lang) {
			case "eng" -> {
				if(eng_lang.has(event.section()))
					yield (String)eng_lang.get(event.section());
				else
					yield "Message "+event.section()+" not found!";
			}
			case "ger" -> {
				if(ger_lang.has(event.section()))
					yield (String)ger_lang.get(event.section());
				else
					yield "Message "+event.section()+" not found!";
			}
			case "spa" -> {
				if(spa_lang.has(event.section()))
					yield (String)spa_lang.getString(event.section());
				else
					yield "Message "+event.section()+" not found!";
			}
			case "rus" -> {
				if(rus_lang.has(event.section()))
					yield (String)rus_lang.getString(event.section());
				else
					yield "Message "+event.section()+" not found!";
			}
			case "por" -> {
				if(por_lang.has(event.section()))
					yield (String)por_lang.getString(event.section());
				else
					yield "Message "+event.section()+" not found!";
			}
			case "fre" -> {
				if(fre_lang.has(event.section()))
					yield (String)fre_lang.getString(event.section());
				else
					yield "Message "+event.section()+" not found!";
			}
			default -> "Missing translation!";
		};
	}
	
	/**
	 * Retrieve a translation for a message 
	 * @param guild required server for looking up which language to use
	 * @param section message to look up in a json file
	 * @return
	 */
	
	public static String getTranslation(Member member, Translation event) {
		//retrieve language. In case none was found, search on the db and if it fails use default language
		String lang = Azrael.SQLgetUserLang(member.getUser().getIdLong());
		if(lang == null) {
			lang = Azrael.SQLgetLanguage(member.getGuild().getIdLong());
			if(lang == null) {
				lang = "eng";
				Hashes.setLanguage(member.getUser().getIdLong(), lang);
				Hashes.setLanguage(member.getGuild().getIdLong(), lang);
			}
			else
				Hashes.setLanguage(member.getUser().getIdLong(), lang);
		}
		
		return getMessage(lang, event);
	}
	
	public static String getTranslation2(Guild guild, Translation event) {
		//retrieve language. In case non was found, search on the db and if it fails use default language
		String lang = Hashes.getLanguage(guild.getIdLong());
		if(lang == null) {
			lang = Azrael.SQLgetLanguage(guild.getIdLong());
			if(lang == null) {
				lang = "eng";
				Hashes.setLanguage(guild.getIdLong(), lang);
			}
			else
				Hashes.setLanguage(guild.getIdLong(), lang);
		}
		
		return getMessage(lang, event);
	}
	
	public static String getTranslation3(User user, Translation event) {
		//retrieve language. In case non was found, search on the db and if it fails use default language
		String lang = Azrael.SQLgetUserLang(user.getIdLong());
		if(lang == null) {
			lang = "eng";
			Hashes.setLanguage(user.getIdLong(), lang);
		}
		
		return getMessage(lang, event);
	}
	
	public static String getLanguage(Member member) {
		String lang = Azrael.SQLgetUserLang(member.getUser().getIdLong());
		if(lang == null) {
			lang = "eng";
			Hashes.setLanguage(member.getUser().getIdLong(), lang);
		}
		return lang;
	}
	
	public static String getLanguage2(Guild guild) {
		String lang = Hashes.getLanguage(guild.getIdLong());
		if(lang == null) {
			lang = Azrael.SQLgetLanguage(guild.getIdLong());
			if(lang == null) {
				lang = "eng";
				Hashes.setLanguage(guild.getIdLong(), lang);
			}
			else {
				Hashes.setLanguage(guild.getIdLong(), lang);
			}
		}
		return lang;
	}
	
	//MYSQL
	public static Connection getDatabaseURL(final int setup) throws SQLException {
		final String host = System.getProperty("DB_"+setup+"_HOST");
		final String ip = System.getProperty("DB_"+setup+"_IP");
		final String port = System.getProperty("DB_"+setup+"_PORT");
		final String database = System.getProperty("DB_"+setup+"_DB_NAME");
		final String timezone = System.getProperty("DB_"+setup+"_TIMEZONE");
		final String user = System.getProperty("DB_"+setup+"_USER");
		final String pass = System.getProperty("DB_"+setup+"_PASS");
		return DriverManager.getConnection("jdbc:mysql://"+(!host.isEmpty() ? host : ip)+":"+port+"/"+database+"?autoReconnect=true&useSSL=true&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone="+timezone
			, user, pass);
	}
	public static Connection getDatabaseURL(final int setup, String param) throws SQLException {
		final String host = System.getProperty("DB_"+setup+"_HOST");
		final String ip = System.getProperty("DB_"+setup+"_IP");
		final String port = System.getProperty("DB_"+setup+"_PORT");
		final String database = System.getProperty("DB_"+setup+"_DB_NAME");
		final String timezone = System.getProperty("DB_"+setup+"_TIMEZONE");
		final String user = System.getProperty("DB_"+setup+"_USER");
		final String pass = System.getProperty("DB_"+setup+"_PASS");
		return DriverManager.getConnection("jdbc:mysql://"+(!host.isEmpty() ? host : ip)+":"+port+"/"+database+"?autoReconnect=true&useSSL=true&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone="+timezone+param
			, user, pass);
	}
	
	public static void initializeBootTime() {
		bootTime = OffsetDateTime.now();
	}
	public static OffsetDateTime getBootTime() {
		return bootTime;
	}
	
	//check if the command is enabled and that the user has enough permissions
	public static boolean commandValidation(GuildMessageReceivedEvent e, BotConfigs botConfig, Command commandName) {
		boolean enabled = false;
		int commandLevel = 0;
		@SuppressWarnings("unchecked")
		final var command = (ArrayList<Object>)BotConfiguration.SQLgetCommand(e.getGuild().getIdLong(), 1, commandName);
		for(Object val : command) {
			if(val instanceof Boolean)
				enabled = (Boolean)val;
			else if(val instanceof Integer)
				commandLevel = (Integer)val;
		}
		if(enabled) {
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong()))
				return true;
			else if(!botConfig.getIgnoreMissingPermissions())
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}
	
	//check if command is enabled
	public static boolean getCommandEnabled(Guild guild, Command command) {
		return (Boolean)((ArrayList<?>)BotConfiguration.SQLgetCommand(guild.getIdLong(), 1, command)).get(0);
	}
	
	//return command level 
	public static int getCommandLevel(Guild guild, Command command) {
		return (Integer)BotConfiguration.SQLgetCommand(guild.getIdLong(), 2, command);
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
	
	//check if the thread exists
	public static boolean threadExists(final String name) {
		if(threads.parallelStream().filter(f -> f.getName().equals(name)).findAny().orElse(null) != null)
			return true;
		else
			return false;
	}
	
	//count the number of threads which start with VOTE and COMMENT and pass them to the shutdown listener. After that, terminate the running threads
	public static void killGoogleThreads() {
		List<Thread> curThreads = new ArrayList<Thread>();
		threads.forEach(t -> {
			if(t.getName().startsWith("COMMENT") || t.getName().startsWith("VOTE")) {
				ShutdownListener.incrementShutdownCountDown();
				curThreads.add(t);
			}
		});
		curThreads.parallelStream().forEach(t -> killThread(t.getName()));
		
	}
	
	//count number of threads which are running in sleep mode for the google request
	public static int getGoogleThreadCount() {
		return threads.parallelStream().filter(f -> f.getName().startsWith("COMMENT") || f.getName().startsWith("VOTE")).collect(Collectors.toList()).size();
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
		for(final Channels channel : channels) {
			if(first) {
				out.append("<#"+channel.getChannel_ID()+">");
			}
			else if(channels.get(last).getChannel_ID() == channel.getChannel_ID()) {
				out.append(" or <#"+channel.getChannel_ID()+">");
			}
			else {
				out.append(", <#"+channel.getChannel_ID()+">");
			}
			first = false;
		}
		return out.toString();
	}
	
	public static String getRestrictedChannels(List<String> channels) {
		StringBuilder out = new StringBuilder();
		var first = true;
		var last = channels.size()-1;
		for(final String channel : channels) {
			if(first) {
				out.append("<#"+channel+">");
			}
			else if(channels.get(last).equals(channel)) {
				out.append(" or <#"+channel+">");
			}
			else {
				out.append(", <#"+channel+">");
			}
			first = false;
		}
		return out.toString();
	}
	
	//define the default privilege level for different role types. Used during role registrations. 0 is lowest and 100 is highest privilege
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
	public static void handleRemovedMessages(Member member, TextChannel channel, String [] output) {
		logger.debug("Message removed from user {} in guild {}", member.getUser().getId(), member.getGuild().getId());
		var muteRole = DiscordRoles.SQLgetRoles(member.getGuild().getIdLong()).parallelStream().filter(f -> f.getCategory_ABV().equals("mut")).findAny().orElse(null);
		if(muteRole == null) {
			if(member.getGuild().getSelfMember().hasPermission(channel, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE) || STATIC.setPermissions(member.getGuild(), channel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE)))
				channel.sendMessage(member.getAsMention()+" "+output[0]).queue();
		}
		else {
			var cache = Hashes.getTempCache("report_gu"+member.getGuild().getId()+"us"+member.getUser().getId());
			if(cache == null || cache.getExpiration() - System.currentTimeMillis() <= 0) {
				if(member.getGuild().getSelfMember().hasPermission(channel, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE) || STATIC.setPermissions(member.getGuild(), channel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE)))
					channel.sendMessage(member.getAsMention()+" "+output[0]).queue();
				Hashes.addTempCache("report_gu"+member.getGuild().getId()+"us"+member.getUser().getId(), new Cache(300000, "1"));
			}
			else if(cache != null) {
				if(cache.getAdditionalInfo().equals("1")) {
					if(member.getGuild().getSelfMember().hasPermission(channel, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE) || STATIC.setPermissions(member.getGuild(), channel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE)))
						channel.sendMessage(member.getAsMention()+" "+output[1]).queue();
					Hashes.addTempCache("report_gu"+member.getGuild().getId()+"us"+member.getUser().getId(), new Cache(300000, "2"));
				}
				else if(cache.getAdditionalInfo().equals("2")) {
					if(member.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
						Azrael.SQLInsertHistory(member.getUser().getIdLong(), member.getGuild().getIdLong(), "mute", STATIC.getTranslation2(member.getGuild(), Translation.CENSOR_MUTE_REASON), 0, "");
						member.getGuild().addRoleToMember(member, member.getGuild().getRoleById(muteRole.getRole_ID())).reason("User muted after censoring 3 messages").queue();
					}
					else {
						writeToRemoteChannel(member.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(member.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(member.getGuild(), Translation.CENSOR_ROLE_ADD_ERR)+Permission.MANAGE_ROLES, Channel.LOG.getType());
						logger.warn("MANAGE ROLES permission required to mute members in guild {}", member.getGuild().getId());
					}
					Hashes.clearTempCache("report_gu"+member.getGuild().getId()+"us"+member.getUser().getId());
				}
			}
		}
	}
	
	//remove the watch state from a user that either got banned or kicked from a server
	public static void handleUnwatch(GuildBanEvent e, GuildMemberRemoveEvent e2, short type) {
		var user_id = (e != null ? e.getUser().getIdLong() : e2.getUser().getIdLong());
		var guild_id = (e != null ? e.getGuild().getIdLong() : e2.getGuild().getIdLong());
		var unwatchReason = (type == 1 ? "ban" : "kick");
		var watchedUser = Azrael.SQLgetWatchlist(user_id, guild_id);
		if(watchedUser != null) {
			if(Azrael.SQLDeleteWatchlist(user_id, guild_id) > 0) {
				TextChannel textChannel = e.getGuild().getTextChannelById(watchedUser.getWatchChannel());
				if(e.getGuild().getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS) || STATIC.setPermissions(e.getGuild(), textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS))) {
					textChannel.sendMessage(new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_WATCH_LIFTED))
						.setDescription(STATIC.getTranslation2(e.getGuild(), Translation.WATCHING_LIFTED).replace("{}", e.getUser().getName()+"#"+e.getUser().getDiscriminator())+unwatchReason+"!").build()).queue();
				}
				else {
					STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MESSAGE_WRITE.getName()+" and "+Permission.MESSAGE_EMBED_LINKS.getName())+textChannel.getAsMention(), Channel.LOG.getType());
					logger.error("MESSAGE_WRITE and MESSAGE_EMBED_LINKS permissions required on text channel {} to log notifications regarding the removal of the watch status in guild {}", textChannel.getId(), e.getGuild().getId());
				}
				Hashes.removeWatchlist(guild_id+"-"+user_id);
				logger.info("User {} has been removed from the watchlist in guild {}", user_id, guild_id);
			}
			else {
				TextChannel textChannel = e.getGuild().getTextChannelById(watchedUser.getWatchChannel());
				if(e.getGuild().getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS) || STATIC.setPermissions(e.getGuild(), textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS))) {
					textChannel.sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_ERROR))
						.setDescription(STATIC.getTranslation2(e.getGuild(), Translation.WATCHING_LIFTED_ERR).replace("{}", e.getUser().getName()+"#"+e.getUser().getDiscriminator())).build()).queue();
				}
				else {
					STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MESSAGE_WRITE.getName()+" and "+Permission.MESSAGE_EMBED_LINKS.getName())+textChannel.getAsMention(), Channel.LOG.getType());
					logger.error("MESSAGE_WRITE and MESSAGE_EMBED_LINKS permissions required on text channel {} to log notifications regarding the removal of the watch status in guild {}", textChannel.getId(), e.getGuild().getId());
				}
				logger.error("User {} couldn't be removed from the watch list in guild {}", user_id, guild_id);
			}
		}
	}
	
	//method to trust all certificates and to retrieve the html code from a webpage
	public static BufferedReader retrieveWebPageCode(String link) throws SocketTimeoutException, IOException {
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
			final String twitterConsumerKey = System.getProperty(TWITTER_CONSUMER_KEY);
			final String twitterConsumerKeySecret = System.getProperty(TWITTER_CONSUMER_KEY_SECRET);
			final String twitterAccessToken = System.getProperty(TWITTER_ACCESS_TOKEN);
			final String twitterAccessTokenSecret = System.getProperty(TWITTER_ACCESS_TOKEN_SECRET);
			if(!twitterConsumerKey.isEmpty() && !twitterConsumerKeySecret.isEmpty() && !twitterAccessToken.isEmpty() && !twitterAccessTokenSecret.isEmpty()) {
				ConfigurationBuilder cb = new ConfigurationBuilder()
						.setDebugEnabled(false)
						.setOAuthConsumerKey(twitterConsumerKey)
						.setOAuthConsumerSecret(twitterConsumerKeySecret)
						.setOAuthAccessToken(twitterAccessToken)
						.setOAuthAccessTokenSecret(twitterAccessTokenSecret);
				twitterFactory = new TwitterFactory(cb.build());
			}
		}
	}
	
	public static TwitterFactory getTwitterFactory() {
		return twitterFactory;
	}
	
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
	
	public static boolean spamDetected(GuildMessageReceivedEvent e) {
		if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
			final long user_id = e.getMember().getUser().getIdLong();
			final long guild_id = e.getGuild().getIdLong();
			final long channel_id = e.getChannel().getIdLong();
			final String message = e.getMessage().getContentRaw();
			
			//verify if the current user is spamming
			if(GuildIni.getSpamDetection(e.getGuild())) {
				//User doesn't have to be an admin, moderator or bot user and they are only allowed to spam in a bot channel
				if(!e.getMember().getUser().isBot() && !UserPrivs.isUserMod(e.getMember()) && !UserPrivs.isUserAdmin(e.getMember()) && Azrael.SQLgetChannels(guild_id).parallelStream().filter(f -> f.getChannel_ID() == channel_id && f.getChannel_Type() != null && (f.getChannel_Type().equals(Channel.BOT.getType()) || f.getChannel_Type().equals(Channel.QUI.getType()))).findAny().orElse(null) == null) {
					final int messagesLimit = GuildIni.getMessagesLimit(e.getGuild());
					final int messagesOverChannelsLimit = GuildIni.getMessageOverChannelsLimit(e.getGuild());
					final var cache = Hashes.getTempCache("spamDetection_gu"+guild_id+"us"+user_id);
					if(cache != null && cache.getExpiration() - System.currentTimeMillis() > 0) {
						if(cache.getAdditionalInfo().equalsIgnoreCase(message)) {
							if(e.getGuild().getSelfMember().canInteract(e.getMember())) {
								if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
									final var mute = DiscordRoles.SQLgetRoles(guild_id).parallelStream().filter(f -> f.getCategory_ABV().equals("mut")).findAny().orElse(null);
									if(mute != null) {
										e.getGuild().addRoleToMember(user_id, e.getGuild().getRoleById(mute.getRole_ID())).reason(STATIC.getTranslation2(e.getGuild(), Translation.CENSOR_MUTE_REASON_2)).queue();
										final int warning = Azrael.SQLgetWarning(user_id, guild_id);
										final long penalty = (long) Azrael.SQLgetWarning(guild_id, warning+1).getTimer();
										Azrael.SQLInsertHistory(user_id, guild_id, "mute", STATIC.getTranslation2(e.getGuild(), Translation.CENSOR_MUTE_REASON_2), penalty, "");
										Hashes.clearTempCache("spamDetection_gu"+guild_id+"us"+user_id);
										deleteSpamMessages(e, cache);
										return true;
									}
								}
								else {
									STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION)+Permission.MANAGE_ROLES, Channel.LOG.getType());
									logger.error("MANAGE_ROLES permission required to mute a user for spam in guild {}", e.getGuild().getId());
								}
							}
							else {
								Hashes.clearTempCache("spamDetection_gu"+guild_id+"us"+user_id);
							}
						}
					}
					else if(cache != null && cache.getExpiration() - System.currentTimeMillis() <= 0)
						Hashes.clearTempCache("spamDetection_gu"+guild_id+"us"+user_id);
					
					//collect messages
					final String lcMessage = message.toLowerCase();
					var spamMessages = Hashes.getSpamDetection(user_id+"_"+guild_id);
					if(spamMessages == null) {
						spamMessages = new SpamDetection(GuildIni.getMessageExpires(e.getGuild()));
						spamMessages.put(lcMessage, e.getMessageIdLong(), channel_id);
					}
					else if(!spamMessages.isExpired()) {
						if(spamMessages.getMessages().get(0).getMessage().equalsIgnoreCase(lcMessage))
							spamMessages.put(lcMessage, e.getMessageIdLong(), channel_id);
						else {
							spamMessages.clear();
							spamMessages.put(lcMessage, e.getMessageIdLong(), channel_id);
						}
					}
					else {
						spamMessages.clear();
						spamMessages.put(lcMessage, e.getMessageIdLong(), channel_id);
					}
					
					//warn the user if the messages limit has been hit or directly mute if the user starts to spam a different message
					if(messagesLimit != 0 && spamMessages.size() == messagesLimit) {
						Hashes.removeSpamDetection(user_id+"_"+guild_id);
						if(cache == null) {
							if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE)))
								e.getChannel().sendMessage(e.getMember().getAsMention()+STATIC.getTranslation2(e.getGuild(), Translation.CENSOR_SPAM)).queue();
							Hashes.addTempCache("spamDetection_gu"+guild_id+"us"+user_id, new Cache(GuildIni.getMessageExpires(e.getGuild()), lcMessage).setObject(spamMessages));
						}
						else {
							if(e.getGuild().getSelfMember().canInteract(e.getMember())) {
								if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
									final var mute = DiscordRoles.SQLgetRoles(guild_id).parallelStream().filter(f -> f.getCategory_ABV().equals("mut")).findAny().orElse(null);
									if(mute != null) {
										e.getGuild().addRoleToMember(user_id, e.getGuild().getRoleById(mute.getRole_ID())).reason(STATIC.getTranslation2(e.getGuild(), Translation.CENSOR_MUTE_REASON_2)).queue();
										final int warning = Azrael.SQLgetWarning(user_id, guild_id);
										final long penalty = (long) Azrael.SQLgetWarning(guild_id, warning+1).getTimer();
										Azrael.SQLInsertHistory(user_id, guild_id, "mute", STATIC.getTranslation2(e.getGuild(), Translation.CENSOR_MUTE_REASON_2), penalty, "");
										Hashes.clearTempCache("spamDetection_gu"+guild_id+"us"+user_id);
										deleteSpamMessages(e, cache);
										return true;
									}
								}
								else {
									STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION)+Permission.MANAGE_ROLES, Channel.LOG.getType());
									logger.error("MANAGE_ROLES permission required to mute a user for spam in guild {}", e.getGuild().getId());
								}
							}
							else {
								Hashes.clearTempCache("spamDetection_gu"+guild_id+"us"+user_id);
							}
						}
					}
					else {
						Set<Long> channels = new HashSet<Long>();
						for(final var spamMessage : spamMessages.getMessages()) {
							if(!channels.contains(spamMessage.getChannelID()))
								channels.add(spamMessage.getChannelID());
						}
						if(messagesOverChannelsLimit != 0 && channels.size() == messagesOverChannelsLimit) {
							Hashes.removeSpamDetection(user_id+"_"+guild_id);
							if(cache == null) {
								if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE)))
									e.getChannel().sendMessage(e.getMember().getAsMention()+STATIC.getTranslation2(e.getGuild(), Translation.CENSOR_SPAM)).queue();
								Hashes.addTempCache("spamDetection_gu"+guild_id+"us"+user_id, new Cache(GuildIni.getMessageExpires(e.getGuild()), lcMessage).setObject(spamMessages));
							}
							else {
								if(e.getGuild().getSelfMember().canInteract(e.getMember())) {
									if(e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
										final var mute = DiscordRoles.SQLgetRoles(guild_id).parallelStream().filter(f -> f.getCategory_ABV().equals("mut")).findAny().orElse(null);
										if(mute != null) {
											e.getGuild().addRoleToMember(user_id, e.getGuild().getRoleById(mute.getRole_ID())).reason(STATIC.getTranslation2(e.getGuild(), Translation.CENSOR_MUTE_REASON_2)).queue();
											final int warning = Azrael.SQLgetWarning(user_id, guild_id);
											final long penalty = (long) Azrael.SQLgetWarning(guild_id, warning+1).getTimer();
											Azrael.SQLInsertHistory(user_id, guild_id, "mute", STATIC.getTranslation2(e.getGuild(), Translation.CENSOR_MUTE_REASON_2), penalty, "");
											Hashes.clearTempCache("spamDetection_gu"+guild_id+"us"+user_id);
											deleteSpamMessages(e, cache);
											return true;
										}
									}
									else {
										STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION)+Permission.MANAGE_ROLES, Channel.LOG.getType());
										logger.error("MANAGE_ROLES permission required to mute a user for spam in guild {}", e.getGuild().getId());
									}
								}
								else {
									Hashes.clearTempCache("spamDetection_gu"+guild_id+"us"+user_id);
								}
							}
						}
						else if(messagesLimit != 0 || messagesOverChannelsLimit != 0)
							Hashes.addSpamMessage(user_id+"_"+guild_id, spamMessages);
					}
				}
			}
		}
		return false;
	}
	
	private static void deleteSpamMessages(GuildMessageReceivedEvent e, Cache cache) {
		//delete all spammed messages
		if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.MESSAGE_MANAGE) || setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.MESSAGE_MANAGE))) {
			e.getMessage().delete().queue(m -> {
				//inform what messages are being deleted
				EmbedBuilder out = new EmbedBuilder().setTimestamp(ZonedDateTime.now()).setTitle(e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+" ("+e.getMember().getUser().getId()+")");
				final var printMessage = STATIC.getTranslation2(e.getGuild(), Translation.DELETE_SPAM)+"\n"+e.getMessage().getContentRaw();
				writeToRemoteChannel(e.getGuild(), out, (printMessage.length() <= 2048 ? printMessage : printMessage.substring(0, 2040)+"..."), Channel.TRA.getType());
				final SpamDetection messages = (SpamDetection) cache.getObject();
				for(final var curMessage : messages.getMessages()) {
					if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY) || setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY))) {
						e.getGuild().getTextChannelById(curMessage.getChannelID()).retrieveMessageById(curMessage.getMessageID()).queue(m2 -> {
							m2.delete().queue();
						}, err -> {
							logger.warn("Spammed message from user {} in channel {} has been already deleted in guild {}", e.getMember().getUser().getId(), curMessage.getChannelID(), e.getGuild().getId());
						});
					}
					else {
						logger.warn("Spammed messages from user {} in channel {} couldn't be deleted because MESSAGE_MANAGE or MESSAGE_HISTORY permission are required in guild {}", e.getMember().getUser().getId(), curMessage.getChannelID(), e.getGuild().getId());
					}
				}
			});
		}
		else {
			logger.warn("Spammed messages from user {} in channel {} couldn't be deleted because MESSAGE_MANAGE permission is required in guild {}", e.getMember().getUser().getId(), e.getChannel().getId(), e.getGuild().getId());
		}
	}
	
	public static boolean writeToRemoteChannel(final Guild guild, final EmbedBuilder embed, final String message, final String channelType) {
		final var channel = Azrael.SQLgetChannels(guild.getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(channelType)).findAny().orElse(null);
		if(channel != null) {
			final TextChannel textChannel = guild.getTextChannelById(channel.getChannel_ID());
			if(textChannel != null) {
				if(embed != null && (guild.getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS) || setPermissions(guild, textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS)))) {
					textChannel.sendMessage(embed.setDescription(message).build()).queue();
					return true;
				}
				else if(embed == null && (guild.getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE) || setPermissions(guild, textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE)))) {
					textChannel.sendMessage(message).queue();
				}
				else {
					logger.warn("MESSAGE_WRITE or MESSAGE_EMBED_LINKS permission required to send a message in channel {}", textChannel.getId());
				}
			}
		}
		return false;
	}
	
	public static boolean writeToRemoteChannel(final Guild guild, final EmbedBuilder embed, final String message, final String channelType, final String channelType2) {
		final var channels = Azrael.SQLgetChannels(guild.getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && (f.getChannel_Type().equals(channelType) || f.getChannel_Type().equals(channelType2))).collect(Collectors.toList());
		var channel = channels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(channelType)).findAny().orElse(null);
		if(channel == null)
			channel = channels.parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(channelType2)).findAny().orElse(null);
		if(channel != null) {
			final TextChannel textChannel = guild.getTextChannelById(channel.getChannel_ID());
			if(textChannel != null) {
				if(embed != null && (guild.getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS) || setPermissions(guild, textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS)))) {
					textChannel.sendMessage(embed.setDescription(message).build()).queue();
					return true;
				}
				else if(embed == null && (guild.getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE) || setPermissions(guild, textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE)))) {
					textChannel.sendMessage(message).queue();
				}
				else {
					logger.warn("MESSAGE_WRITE or MESSAGE_EMBED_LINKS permission required to send a message in channel {}", textChannel.getId());
				}
			}
		}
		return false;
	}
	
	public static boolean setPermissions(Guild guild, TextChannel textChannel, Collection<Permission> permissions) {
		if(guild.getSelfMember().hasPermission(textChannel, Permission.MANAGE_CHANNEL, Permission.MANAGE_PERMISSIONS)) {
			textChannel.getManager().putPermissionOverride(guild.getSelfMember(), permissions, null).complete();
			logger.info("Permissions overriden of text channel {} in guild {}", textChannel.getId(), guild.getId());
			return true;
		}
		return false;
	}
	
	public static boolean setPermissions(Guild guild, Category category, Collection<Permission> permissions) {
		if(guild.getSelfMember().hasPermission(category, Permission.MANAGE_CHANNEL, Permission.MANAGE_PERMISSIONS)) {
			category.getManager().putPermissionOverride(guild.getSelfMember(), permissions, null).complete();
			logger.info("Permissions overriden of category {} in guild {}", category.getId(), guild.getId());
			return true;
		}
		return false;
	}
	
	public static String encrypt(final String rawMessage) {
		MessageDigest sha = null;
		try {
			byte [] key = System.getProperty("AES_SECRET").getBytes("UTF-8");
			sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16);
			final SecretKeySpec secret = new SecretKeySpec(key, "AES");
			try {
				Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
				cipher.init(Cipher.ENCRYPT_MODE, secret);
				return new String(cipher.doFinal(Base64.getEncoder().encode(rawMessage.getBytes())));
			} catch (NoSuchPaddingException e) {
				logger.error("Encryption padding not available", e);
			} catch (InvalidKeyException e) {
				logger.error("Encryption key invalid", e);
			} catch (Exception e) {
				logger.error("Message couldn't be encrypted", e);
			}
		} catch (UnsupportedEncodingException e) {
			logger.error("AES secret not available", e);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Algorithm is not supported", e);
		}
		return null;
	}
	
	public static String decrypt(final String encryptedMessage) {
		MessageDigest sha = null;
		try {
			byte [] key = System.getProperty("AES_SECRET").getBytes("UTF-8");
			sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16);
			final SecretKeySpec secret = new SecretKeySpec(key, "AES");
			try {
				Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
				cipher.init(Cipher.DECRYPT_MODE, secret);
				return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedMessage)));
			} catch (NoSuchPaddingException e) {
				logger.error("Decryption padding not available", e);
			} catch (InvalidKeyException e) {
				logger.error("Decryption key invalid", e);
			} catch (Exception e) {
				logger.error("Message couldn't be decrypted", e);
			}
		} catch (UnsupportedEncodingException e) {
			logger.error("AES secret not available", e);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Algorithm is not supported", e);
		}
		return null;
	}
	
	public static Object retrieveEmoji(final Guild guild, final String targetEmoji, final String defaultEmoji) {
		Object emoji = null;
		if(targetEmoji != null && targetEmoji.length() > 0) {
			final var emotes = guild.getEmotesByName(targetEmoji, false);
			if(emotes.size() > 0)
				emoji = emotes.get(0);
			else
				emoji = EmojiManager.getForAlias(":"+targetEmoji+":").getUnicode();
		}
		else {
			emoji = EmojiManager.getForAlias(defaultEmoji).getUnicode();
		}
		return emoji;
	}
}
