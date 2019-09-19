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
import commands.Help;
import commands.Inventory;
import commands.Meow;
import commands.Patchnotes;
import commands.Profile;
import commands.Pug;
import commands.Quiz;
import commands.Randomshop;
import commands.Rank;
import commands.Reboot;
import commands.Register;
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
import listeners.BoostCountListener;
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

public class Main {
	static {System.setProperty("logback.configurationFile", "./logback.xml");}
	public static JDABuilder builder;
	
	public static void main(String [] args){
		Logger logger = LoggerFactory.getLogger(Main.class);
		boolean [] dir = new boolean[3];
		dir[0] = (new File("./log")).mkdirs();
		dir[1] = (new File("./message_log")).mkdirs();
		dir[2] = (new File("./ini")).mkdirs();
		
		if(IniFileReader.getFileLogger()) {
			
			PrintStream out;
			PrintStream err;
			try {
				out = new PrintStream(new FileOutputStream("log/log"+new Timestamp(System.currentTimeMillis()).toString().replaceAll(":", "-")+".txt"));
				err = new PrintStream(new FileOutputStream("log/err"+new Timestamp(System.currentTimeMillis()).toString().replaceAll(":", "-")+".txt"));
				System.setOut(out);
				System.setErr(err);
			} catch (FileNotFoundException e1) {
				logger.warn("eventlog.txt or errlog.txt couldn't be found on start up", e1);
			}
		}
		
		String token = IniFileReader.getToken();
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
			new BoostCountListener(),
			new TextChannelListener()
		);
	}
}
