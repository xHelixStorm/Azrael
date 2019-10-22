package core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Timestamp;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commands.About;
import commands.Commands;
import commands.Daily;
import commands.Display;
import commands.DoubleExperience;
import commands.Equip;
import commands.Filter;
import commands.HeavyCensoring;
import commands.Help;
import commands.Inventory;
import commands.Meow;
import commands.Mute;
import commands.Patchnotes;
import commands.Profile;
import commands.Pug;
import commands.Quiz;
import commands.Randomshop;
import commands.Rank;
import commands.Reboot;
import commands.Register;
import commands.Remove;
import commands.RoleReaction;
import commands.Rss;
import commands.Set;
import commands.Shop;
import commands.ShutDown;
import commands.Top;
import commands.Use;
import commands.User;
import fileManagement.IniFileReader;
import listeners.AvatarUpdateListener;
import listeners.BanListener;
import listeners.GuildJoinListener;
import listeners.GuildLeaveListener;
import listeners.GuildListener;
import listeners.GuildMessageReactionAddListener;
import listeners.GuildMessageReactionRemoveListener;
import listeners.GuildMessageEditListener;
import listeners.GuildMessageListener;
import listeners.GuildMessageRemovedListener;
import listeners.NameListener;
import listeners.NicknameListener;
import listeners.PrivateMessageListener;
import listeners.ReadyListener;
import listeners.ReconnectedListener;
import listeners.ResumedListener;
import listeners.RoleCreateListener;
import listeners.RoleListener;
import listeners.RoleRemovedListener;
import listeners.ShutdownListener;
import listeners.StatusListener;
import listeners.TextChannelListener;
import listeners.UnbanListener;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import util.STATIC;

public class Main {
	static {System.setProperty("logback.configurationFile", "./logback.xml");}
	public static JDABuilder builder;
	
	public static void main(String [] args){
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
						+ "timezone:<String> (timezone location like 'Europe/Berlin' for mysql databases)"
						+ "actionlog:<BOOLEAN> (true/false parameter to log actions related to the ranking system and other updates)\n"
						+ "doubleexperience:<BOOLEAN> (true/false parameter to either enable or disable double experience events)\n"
						+ "doubleexperiencestart:<WEEKDAY> (Regular days from Monday to Sunday as parameter to define the start day of double experience events)\n"
						+ "doubleexperienceend:<WEEKDAY> (Regular days from Monday to Sunday as parameter to define the end day of double experience events)\n"
						+ "countmembers:<BOOLEAN> (true/false parameter to either enable or disable the count of all active members as playing status)\n"
						+ "filelogger:<BOOLEAN> (true/false parameter to print all messages to the console if off or to file if on)\n"
						+ "gamemessage:<STRING> (Message to display as playing status. Separate blank spaces with '-')\n"
						+ "temp:<STRING> (Path for the temp directory)");
				System.exit(0);
			}
			else {
				//initialize all static variables
				STATIC.setToken(args[0].trim());
				if(args.length > 1)
					STATIC.setSessionName(args[1].trim());
				if(args.length > 2) {
					for(final var argument : args) {
						final var currentArgument = argument.toLowerCase();
						if(currentArgument.startsWith("admin:"))
							STATIC.setAdmin(Long.parseLong(argument.split(":")[1].trim()));
						if(currentArgument.startsWith("timezone:"))
							STATIC.setTimezone(argument.split(":")[1].trim());
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
					}
				}
			}
		}
		
		if(IniFileReader.getFileLogger()) {
			PrintStream out;
			PrintStream err;
			try {
				out = new PrintStream(new FileOutputStream("log/"+STATIC.getSessionName()+"log"+new Timestamp(System.currentTimeMillis()).toString().replaceAll(":", "-")+".txt"));
				err = new PrintStream(new FileOutputStream("log/"+STATIC.getSessionName()+"err"+new Timestamp(System.currentTimeMillis()).toString().replaceAll(":", "-")+".txt"));
				System.setOut(out);
				System.setErr(err);
			} catch (FileNotFoundException e1) {
				logger.warn("eventlog.txt or errlog.txt couldn't be found on start up", e1);
			}
		}
		
		String token = (STATIC.getToken().length() > 0 ? STATIC.getToken() : IniFileReader.getToken());
		builder = new JDABuilder(AccountType.BOT);
		builder.setToken(token);
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
		CommandHandler.commandsPublic.put("help", new Help());
		CommandHandler.commandsPublic.put("about", new About());
		CommandHandler.commandsPublic.put("reboot", new Reboot());
		CommandHandler.commandsPublic.put("commands", new Commands());
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
		CommandHandler.commandsPublic.put("rss", new Rss());
		CommandHandler.commandsPublic.put("randomshop", new Randomshop());
		CommandHandler.commandsPublic.put("patchnotes", new Patchnotes());
		CommandHandler.commandsPublic.put("doubleexperience", new DoubleExperience());
		CommandHandler.commandsPublic.put("equip", new Equip());
		CommandHandler.commandsPublic.put("remove", new Remove());
		CommandHandler.commandsPublic.put("heavycensoring", new HeavyCensoring());
		CommandHandler.commandsPublic.put("mute", new Mute());
	}
	
	public static void addPrivateCommands() {
		CommandHandler.commandsPrivate.put("!equip", new Equip());
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
			new TextChannelListener()
		);
	}
}
