package de.azrael.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.EnumSet;
import java.util.Properties;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.util.ContextInitializer;
import de.azrael.commands.About;
import de.azrael.commands.Accept;
import de.azrael.commands.Changemap;
import de.azrael.commands.Clan;
import de.azrael.commands.Invites;
import de.azrael.commands.Cw;
import de.azrael.commands.Daily;
import de.azrael.commands.Deny;
import de.azrael.commands.Display;
import de.azrael.commands.DoubleExperience;
import de.azrael.commands.Edit;
import de.azrael.commands.Equip;
import de.azrael.commands.Filter;
import de.azrael.commands.Google;
import de.azrael.commands.HeavyCensoring;
import de.azrael.commands.Help;
import de.azrael.commands.Inventory;
import de.azrael.commands.Join;
import de.azrael.commands.Language;
import de.azrael.commands.Leaderboard;
import de.azrael.commands.Leave;
import de.azrael.commands.Master;
import de.azrael.commands.Matchmaking;
import de.azrael.commands.Meow;
import de.azrael.commands.Mute;
import de.azrael.commands.Patchnotes;
import de.azrael.commands.Pick;
import de.azrael.commands.Profile;
import de.azrael.commands.Prune;
import de.azrael.commands.Pug;
import de.azrael.commands.Queue;
import de.azrael.commands.Quiz;
import de.azrael.commands.Randomshop;
import de.azrael.commands.Rank;
import de.azrael.commands.Reboot;
import de.azrael.commands.Register;
import de.azrael.commands.Remove;
import de.azrael.commands.Restrict;
import de.azrael.commands.RoleReaction;
import de.azrael.commands.Room;
import de.azrael.commands.Schedule;
import de.azrael.commands.Set;
import de.azrael.commands.Shop;
import de.azrael.commands.ShutDown;
import de.azrael.commands.Start;
import de.azrael.commands.Stats;
import de.azrael.commands.Subscribe;
import de.azrael.commands.Top;
import de.azrael.commands.Use;
import de.azrael.commands.User;
import de.azrael.commands.Warn;
import de.azrael.commands.Web;
import de.azrael.commands.Write;
import de.azrael.listeners.AvatarUpdateListener;
import de.azrael.listeners.BanListener;
import de.azrael.listeners.CategoryListener;
import de.azrael.listeners.GuildJoinListener;
import de.azrael.listeners.GuildLeaveListener;
import de.azrael.listeners.GuildListener;
import de.azrael.listeners.GuildMessageEditListener;
import de.azrael.listeners.GuildMessageListener;
import de.azrael.listeners.GuildMessageReactionAddListener;
import de.azrael.listeners.GuildMessageReactionRemoveListener;
import de.azrael.listeners.GuildMessageRemovedListener;
import de.azrael.listeners.NameListener;
import de.azrael.listeners.NicknameListener;
import de.azrael.listeners.PrivateMessageListener;
import de.azrael.listeners.PrivateMessageReactionAddListener;
import de.azrael.listeners.ReadyListener;
import de.azrael.listeners.ReconnectedListener;
import de.azrael.listeners.ResumedListener;
import de.azrael.listeners.RoleCreateListener;
import de.azrael.listeners.RoleListener;
import de.azrael.listeners.RoleNameUpdateListener;
import de.azrael.listeners.RoleRemovedListener;
import de.azrael.listeners.ShutdownListener;
import de.azrael.listeners.StatusListener;
import de.azrael.listeners.TextChannelListener;
import de.azrael.listeners.UnbanListener;
import de.azrael.listeners.VoiceChannelListener;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class Main {
	static {System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "./logback.xml");}
	private final static Logger logger = LoggerFactory.getLogger(Main.class);
	private final static String SECRET = "./.secret_data";
	private static JDABuilder builder;
	
	public static void main(String [] args) {
		//set default uncaught exception handler
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				logger.error("Uncaught exception on thread {}: ", t, e);
			}
		});
		
		Logger logger = LoggerFactory.getLogger(Main.class);
		boolean [] dir = new boolean[3];
		dir[0] = (new File("./log")).mkdirs();
		dir[1] = (new File("./message_log")).mkdirs();
		dir[2] = (new File("./ini")).mkdirs();
		
		//Verify the passed parameters
		if(args.length == 0) {
			//display all available parameters when no parameter has been passed
			System.out.println("These are all available parameters. A valid token and an encryption key is mandatory to start the application:\n\n"
					+ "encryption:<String> (AES key to decrypt sql requests and confidential information)"
					+ "sessionname:<String> (if the bot should be started more than once, assign a name)\n"
					+ "actionlog:<BOOLEAN> (true/false parameter to log actions related to the ranking system and other updates)\n"
					+ "countguilds:<BOOLEAN> (true/false parameter to either enable or disable the count of all guilds as status message)\n"
					+ "filelog:<BOOLEAN> (true/false parameter to print all messages into a text file when enabled)\n"
					+ "statusmessage:<STRING> (Message to display as status message. Separate blank spaces with '-')\n"
					+ "temp:<STRING> (Path for the temp directory)\n"
					+ "homepage:<String> (url for the Bot homepage)\n"
					+ "port:<NUMERIC> (port number for the webservice)\n"
					+ "spreadsheetdelay:<NUMERIC> (numeric delay time to execute batch updates on spreadsheets)");
			
		} 
		else {
			//initialize all static variables
			System.setProperty("TOKEN", args[0].trim());
			for(final var argument : args) {
				final var currentArgument = argument.toLowerCase();
				if(currentArgument.startsWith("encryption:"))
					System.setProperty("AES_SECRET", argument.split(":")[1].trim());
				if(currentArgument.startsWith("sessionname:"))
					System.setProperty("SESSION_NAME", argument.split(":")[1].trim());
				if(currentArgument.startsWith("actionlog:"))
					System.setProperty("ACTION_LOG", argument.split(":")[1].trim());
				if(currentArgument.startsWith("countguilds:"))
					System.setProperty("COUNT_GUILDS", argument.split(":")[1].trim());
				if(currentArgument.startsWith("filelog:"))
					System.setProperty("FILE_LOG", argument.split(":")[1].trim());
				if(currentArgument.startsWith("statusmessage:")) {
					var splitMessage = argument.split(":")[1].split("-");
					StringBuilder message = new StringBuilder();
					for(final var split : splitMessage)
						message.append(split+" ");
					System.setProperty("STATUS_MESSAGE", message.toString());
				}
				if(currentArgument.startsWith("temp:"))
					System.setProperty("TEMP_DIRECTORY", argument.split(":")[1].trim());
				if(currentArgument.startsWith("homepage:"))
					System.setProperty("HOMEPAGE", argument.split(":")[1].trim());
				if(currentArgument.startsWith("port:"))
					System.setProperty("WEBSERVER_PORT", argument.split(":")[1].trim());
				if(currentArgument.startsWith("spreadsheetdelay:"))
					System.setProperty("SPREADSHEET_UPDATE_DELAY", argument.split(":")[1].trim());
			}
			
			if(System.getProperty("TOKEN") == null) {
				logger.error("No token has been provided!");
				return;
			}
			if(System.getProperty("AES_SECRET") == null) {
				logger.error("No encryption key received!");
				return;
			}
			
			//Load DB configuration from file
			Properties prop = new Properties();
			try {
				FileInputStream secret = new FileInputStream(SECRET);
				prop.load(secret);
				
				//Database options
				final String dbNumber = prop.getProperty("DATABASE_NUMBER");
				if(dbNumber != null && dbNumber.trim().matches("^[0-9]*$")) {
					for(int i = 1; i <= Integer.parseInt(dbNumber.trim()); i++) {
						final String sourceCodeName = prop.getProperty("DB_"+i+"_SOURCECODE_NAME");
						final String dbName = prop.getProperty("DB_"+i+"_DB_NAME");
						final String ip = prop.getProperty("DB_"+i+"_IP", "127.0.0.1");
						final String port = prop.getProperty("DB_"+i+"_PORT", "3306");
						final String host = prop.getProperty("DB_"+i+"_HOST", "http://localhost");
						final String timezone = prop.getProperty("DB_"+i+"_TIMEZONE", "UTC");
						final String user = prop.getProperty("DB_"+i+"_USER");
						final String pass = prop.getProperty("DB_"+i+"_PASS");
						
						if(sourceCodeName == null || dbName == null || user == null || pass == null) {
							if(sourceCodeName == null)
								logger.error("Parameter DB_{}_SOURCECODE_NAME not found!", i);
							if(dbName == null)
								logger.error("Parameter DB_{}_DB_NAME not found!", i);
							if(user == null)
								logger.error("Parameter DB_{}_USER not found!", i);
							if(pass == null)
								logger.error("Parameter DB_{}_PASS not found!", i);
							logger.error("Database configuration couldn't be loaded. Application shutdown!");
							return;
						}
						
						System.setProperty("DB_"+i+"_SOURCECODE_NAME", sourceCodeName.trim());
						System.setProperty("DB_"+i+"_DB_NAME", dbName.trim());
						System.setProperty("DB_"+i+"_IP", ip.trim());
						System.setProperty("DB_"+i+"_PORT", port.trim());
						System.setProperty("DB_"+i+"_HOST", host.trim());
						System.setProperty("DB_"+i+"_TIMEZONE", timezone);
						System.setProperty("DB_"+i+"_USER", STATIC.decrypt(user.trim()));
						System.setProperty("DB_"+i+"_PASS", STATIC.decrypt(pass.trim()));
					}
				}
				else if(dbNumber == null) {
					logger.error("DATABASE_NUMBER parameter not found!");
					return;
				}
				else {
					logger.error("Invalid number of database connections!");
					return;
				}
				
				//Pastebin options
				System.setProperty("PASTEBIN_API_KEY", prop.getProperty("PASTEBIN_API_KEY", "").trim());
				final String pastebinUser = prop.getProperty("PASTEBIN_USER", "");
				final String pastebinPass = prop.getProperty("PASTEBIN_PASS", "");
				System.setProperty("PASTEBIN_USER", (pastebinUser.isBlank() ? pastebinUser.trim() : STATIC.decrypt(pastebinUser.trim())));
				System.setProperty("PASTEBIN_PASS", (pastebinPass.isBlank() ? pastebinPass.trim() : STATIC.decrypt(pastebinPass.trim())));
				
				//Imgur options
				System.setProperty("IMGUR_API_KEY", prop.getProperty("IMGUR_API_KEY", "").trim());
				
				//Twitter options
				System.setProperty("TWITTER_CONSUMER_KEY", prop.getProperty("TWITTER_CONSUMER_KEY", "").trim());
				System.setProperty("TWITTER_CONSUMER_KEY_SECRET", prop.getProperty("TWITTER_CONSUMER_KEY_SECRET", "").trim());
				System.setProperty("TWITTER_ACCESS_TOKEN", prop.getProperty("TWITTER_ACCESS_TOKEN", "").trim());
				System.setProperty("TWITTER_ACCESS_TOKEN_SECRET", prop.getProperty("TWITTER_ACCESS_TOKEN_SECRET", "").trim());
				
				//Reddit options
				System.setProperty("REDDIT_CLIENT_ID", prop.getProperty("REDDIT_CLIENT_ID", "").trim());
				System.setProperty("REDDIT_CLIENT_SECRET", prop.getProperty("REDDIT_CLIENT_SECRET", "").trim());
				final String redditUser = prop.getProperty("REDDIT_USER", "");
				final String redditPass = prop.getProperty("REDDIT_PASS", "");
				System.setProperty("REDDIT_USER", (redditUser.isBlank() ? redditUser.trim() : STATIC.decrypt(redditUser.trim())));
				System.setProperty("REDDIT_PASS", (redditPass.isBlank() ? redditPass.trim() : STATIC.decrypt(redditPass.trim())));
				
				//Twitch options
				System.setProperty("TWITCH_CLIENT_ID", prop.getProperty("TWITCH_CLIENT_ID", "").trim());
				System.setProperty("TWITCH_CLIENT_SECRET", prop.getProperty("TWITCH_CLIENT_SECRET", "").trim());
			} catch (Exception e1) {
				logger.error("Database configurations couldn't be loaded. Application shutdown!", e1);
				return;
			}
			
			//set default values, if not set
			if(System.getProperty("SESSION_NAME") == null)
				System.setProperty("SESSION_NAME", "Azrael");
			if(System.getProperty("COUNT_GUILDS") == null || (!System.getProperty("COUNT_GUILDS").equals("true") && !System.getProperty("COUNT_GUILDS").equals("false")))
				System.setProperty("COUNT_GUILDS", "false");
			if(System.getProperty("ACTION_LOG") == null || (!System.getProperty("ACTION_LOG").equals("true") && !System.getProperty("ACTION_LOG").equals("false")))
				System.setProperty("ACTION_LOG", prop.getProperty("ACTION_LOG", "false"));
			if(System.getProperty("FILE_LOG") == null || (!System.getProperty("FILE_LOG").equals("true") && !System.getProperty("FILE_LOG").equals("false")))
				System.setProperty("FILE_LOG", prop.getProperty("FILE_LOG", "false"));
			if(System.getProperty("WEBSERVER_PORT") == null || !System.getProperty("WEBSERVER_PORT").matches("[0-9]{1,}"))
				System.setProperty("WEBSERVER_PORT", prop.getProperty("WEBSERVER_PORT", "0"));
			if(System.getProperty("TEMP_DIRECTORY") == null)
				System.setProperty("TEMP_DIRECTORY", "./tmp");
			if(System.getProperty("SPREADSHEET_UPDATE_DELAY") == null || (!System.getProperty("SPREADSHEET_UPDATE_DELAY").matches("[0-9]*") && Long.parseLong(System.getProperty("SPREADSHEET_UPDATE_DELAY")) > 60  && Long.parseLong(System.getProperty("SPREADSHEET_UPDATE_DELAY")) < 0))
				System.setProperty("SPREADSHEET_UPDATE_DELAY", "0");
			
			if(System.getProperty("FILE_LOG").equals("true")) {
				PrintStream out;
				try {
					final String fileName = System.getProperty("SESSION_NAME");
					out = new PrintStream(new FileOutputStream("log/"+fileName+".log", true));
					System.setOut(out);
					System.setErr(out);
				} catch (FileNotFoundException e1) {
					logger.warn("Log file couldn't be found on start up", e1);
				}
			}
			
			builder = JDABuilder.createDefault(System.getProperty("TOKEN"))
					.enableIntents(EnumSet.of(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES))
					.setMemberCachePolicy(MemberCachePolicy.ALL);
			builder.setAutoReconnect(true);
			builder.setStatus(OnlineStatus.ONLINE);	
		
			addPublicCommands();
			addPrivateCommands();
			addListeners();
			
			try {
				@SuppressWarnings("unused")
				JDA jda = builder.build();
			} catch (LoginException | IllegalArgumentException e) {
				logger.error("Login or Token error", e);
			}
		}
	}
	
	public static void addPublicCommands() {
		
		CommandHandler.commandsPublic.put("shutdown", new ShutDown());
		CommandHandler.commandsPublic.put("about", new About());
		CommandHandler.commandsPublic.put("reboot", new Reboot());
		CommandHandler.commandsPublic.put("help", new Help());
		CommandHandler.commandsPublic.put("pug", new Pug());
		CommandHandler.commandsPublic.put("meow", new Meow());
		CommandHandler.commandsPublic.put("rank", new Rank());
		CommandHandler.commandsPublic.put("profile", new Profile());
		CommandHandler.commandsPublic.put("display", new Display());
		CommandHandler.commandsPublic.put("top", new Top());
		CommandHandler.commandsPublic.put("register", new Register());
		CommandHandler.commandsPublic.put("set", new Set());
		CommandHandler.commandsPublic.put("shop", new Shop());
		CommandHandler.commandsPublic.put("use", new Use());
		CommandHandler.commandsPublic.put("inventory", new Inventory());
		CommandHandler.commandsPublic.put("daily", new Daily());
		CommandHandler.commandsPublic.put("user", new User());
		CommandHandler.commandsPublic.put("filter", new Filter());
		CommandHandler.commandsPublic.put("quiz", new Quiz());
		CommandHandler.commandsPublic.put("rolereaction", new RoleReaction());
		CommandHandler.commandsPublic.put("subscribe", new Subscribe());
		CommandHandler.commandsPublic.put("randomshop", new Randomshop());
		CommandHandler.commandsPublic.put("patchnotes", new Patchnotes());
		CommandHandler.commandsPublic.put("doubleexperience", new DoubleExperience());
		CommandHandler.commandsPublic.put("equip", new Equip());
		CommandHandler.commandsPublic.put("remove", new Remove());
		CommandHandler.commandsPublic.put("heavycensoring", new HeavyCensoring());
		CommandHandler.commandsPublic.put("mute", new Mute());
		CommandHandler.commandsPublic.put("google", new Google());
		CommandHandler.commandsPublic.put("write", new Write());
		CommandHandler.commandsPublic.put("edit", new Edit());
		CommandHandler.commandsPublic.put("matchmaking", new Matchmaking());
		CommandHandler.commandsPublic.put("join", new Join());
		CommandHandler.commandsPublic.put("clan", new Clan());
		CommandHandler.commandsPublic.put("leave", new Leave());
		CommandHandler.commandsPublic.put("queue", new Queue());
		CommandHandler.commandsPublic.put("changemap", new Changemap());
		CommandHandler.commandsPublic.put("pick", new Pick());
		CommandHandler.commandsPublic.put("cw", new Cw());
		CommandHandler.commandsPublic.put("room", new Room());
		CommandHandler.commandsPublic.put("stats", new Stats());
		CommandHandler.commandsPublic.put("leaderboard", new Leaderboard());
		CommandHandler.commandsPublic.put("web", new Web());
		CommandHandler.commandsPublic.put("accept", new Accept());
		CommandHandler.commandsPublic.put("deny", new Deny());
		CommandHandler.commandsPublic.put("language", new Language());
		CommandHandler.commandsPublic.put("schedule", new Schedule());
		CommandHandler.commandsPublic.put("master", new Master());
		CommandHandler.commandsPublic.put("restrict", new Restrict());
		CommandHandler.commandsPublic.put("start", new Start());
		CommandHandler.commandsPublic.put("prune", new Prune());
		CommandHandler.commandsPublic.put("warn", new Warn());
		CommandHandler.commandsPublic.put("invites", new Invites());
	}
	
	public static void addPrivateCommands() {
		CommandHandler.commandsPrivate.put("!equip", new Equip());
		CommandHandler.commandsPrivate.put("!web", new Web());
	}
	
	public static void addListeners() {
		builder.addEventListeners(
			new ReadyListener(),
			new GuildListener(),
			new RoleListener(),
			new BanListener(),
			new UnbanListener(),
			new GuildMessageListener(),
			new GuildLeaveListener(),
			new GuildMessageEditListener(),
			new PrivateMessageListener(),
			new NameListener(),
			new GuildJoinListener(),
			new ShutdownListener(),
			new RoleRemovedListener(),
			new NicknameListener(),
			new GuildMessageRemovedListener(),
			new AvatarUpdateListener(),
			new GuildMessageReactionAddListener(),
			new GuildMessageReactionRemoveListener(),
			new StatusListener(),
			new ReconnectedListener(),
			new ResumedListener(),
			new RoleCreateListener(),
			new TextChannelListener(),
			new RoleNameUpdateListener(),
			new PrivateMessageReactionAddListener(),
			new CategoryListener(),
			new VoiceChannelListener()
		);
	}
}
