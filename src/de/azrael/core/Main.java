package de.azrael.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.EnumSet;

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
import de.azrael.commands.Reddit;
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
import de.azrael.commands.Twitch;
import de.azrael.commands.Use;
import de.azrael.commands.User;
import de.azrael.commands.Warn;
import de.azrael.commands.Web;
import de.azrael.commands.Write;
import de.azrael.fileManagement.IniFileReader;
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
		
		//collect parameters, if provided. Token and SessionName have to be the first 2 parameters
		if(args.length > 0) {
			//display all available parmeters if program receives 'list' as parameter and terminate with exit 0
			if(args[0].equals("list")) {
				System.out.println("These are all available parameters. If nothing has been provided, the values from config.ini will be taken:\n\n"
						+ "admin:<NUMERIC> (17/18 digit long user id that defines the admin for shutdown and reboot)\n"
						+ "timezone:<String> (timezone location like 'Europe/Berlin' for mysql databases)\n"
						+ "sessionname: <String> (if the bot should be started multiple times, assign a name)"
						+ "actionlog:<BOOLEAN> (true/false parameter to log actions related to the ranking system and other updates)\n"
						+ "doubleexperience:<BOOLEAN> (true/false parameter to either enable or disable double experience events)\n"
						+ "doubleexperiencestart:<WEEKDAY> (Regular days from Monday to Sunday as parameter to define the start day of double experience events)\n"
						+ "doubleexperienceend:<WEEKDAY> (Regular days from Monday to Sunday as parameter to define the end day of double experience events)\n"
						+ "countmembers:<BOOLEAN> (true/false parameter to either enable or disable the count of all active members as playing status)\n"
						+ "filelogger:<BOOLEAN> (true/false parameter to print all messages to the console if off or to file if on)\n"
						+ "gamemessage:<STRING> (Message to display as playing status. Separate blank spaces with '-')\n"
						+ "temp:<STRING> (Path for the temp directory)\n"
						+ "port:<NUMERIC> (port number for the webservice)");
				System.exit(0);
			}
			else {
				//initialize all static variables
				STATIC.setToken(args[0].trim());
				if(args.length > 1) {
					for(final var argument : args) {
						final var currentArgument = argument.toLowerCase();
						if(currentArgument.startsWith("admin:"))
							STATIC.setAdmin(Long.parseLong(argument.split(":")[1].trim()));
						if(currentArgument.startsWith("timezone:"))
							STATIC.setTimezone(argument.split(":")[1].trim());
						if(currentArgument.startsWith("sessionname:"))
							STATIC.setSessionName(argument.split(":")[1].trim());
						if(currentArgument.startsWith("actionlog:"))
							STATIC.setActionLog(argument.split(":")[1].trim());
						if(currentArgument.startsWith("doubleexperience:"))
							STATIC.setDoubleExperience(argument.split(":")[1].trim());
						if(currentArgument.startsWith("doubleexperiencestart:"))
							STATIC.setDoubleExperienceStart(argument.split(":")[1].trim());
						if(currentArgument.startsWith("doubleexperienceend:"))
							STATIC.setDoubleExperienceEnd(argument.split(":")[1].trim());
						if(currentArgument.startsWith("countmembers:"))
							STATIC.setCountMembers(argument.split(":")[1].trim());
						if(currentArgument.startsWith("filelogger:"))
							STATIC.setFileLogger(argument.split(":")[1].trim());
						if(currentArgument.startsWith("gamemessage:")) {
							var splitMessage = argument.split(":")[1].split("-");
							StringBuilder message = new StringBuilder();
							for(final var split : splitMessage)
								message.append(split+" ");
							STATIC.setGameMessage(message.toString().trim());
						}
						if(currentArgument.startsWith("temp:"))
							STATIC.setTemp(argument.split(":")[1].trim());
						if(currentArgument.startsWith("port:"))
							STATIC.setPort(Integer.parseInt(argument.split(":")[1].trim()));
					}
				}
			}
		}
		if(STATIC.getToken().length() == 0)
			STATIC.setToken(IniFileReader.getToken());
		
		if(IniFileReader.getFileLogger()) {
			PrintStream out;
			try {
				final String fileName = (STATIC.getSessionName().length() > 0 ? STATIC.getSessionName() : "Azrael");
				out = new PrintStream(new FileOutputStream("log/"+fileName+".log", true));
				System.setOut(out);
				System.setErr(out);
			} catch (FileNotFoundException e1) {
				logger.warn("Log file couldn't be found on start up", e1);
			}
		}
		
		String token = STATIC.getToken();
		builder = JDABuilder.createDefault(token)
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
		CommandHandler.commandsPublic.put("reddit", new Reddit());
		CommandHandler.commandsPublic.put("invites", new Invites());
		CommandHandler.commandsPublic.put("twitch", new Twitch());
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
