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
import commands.Filter;
import commands.Help;
import commands.Inventory;
import commands.Meow;
import commands.Patchnotes;
import commands.Profile;
import commands.Pug;
import commands.Purchase;
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
import listeners.GuildJoinListener;
import listeners.GuildLeaveListener;
import listeners.GuildListener;
import listeners.GuildMessageReactionAddListener;
import listeners.GuildMessageReactionRemoveListener;
import listeners.MessageEditListener;
import listeners.MessageListener;
import listeners.MessageRemovedListener;
import listeners.NameListener;
import listeners.NicknameListener;
import listeners.ReadyListener;
import listeners.ReconnectedListener;
import listeners.ResumedListener;
import listeners.RoleCreateListener;
import listeners.RoleListener;
import listeners.RoleRemovedListener;
import listeners.ShutdownListener;
import listeners.StatusListener;
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
	
		addCommands();
		addListeners();
		
		try {
			@SuppressWarnings("unused")
			JDA jda = builder.build();
		} catch (LoginException | IllegalArgumentException e) {
			logger.error("Login or Token error", e);
		}
	}
	
	public static void addCommands(){
		
		CommandHandler.commands.put("shutdown", new ShutDown());
		CommandHandler.commands.put("help", new Help());
		CommandHandler.commands.put("about", new About());
		CommandHandler.commands.put("reboot", new Reboot());
		CommandHandler.commands.put("commands", new Commands());
		CommandHandler.commands.put("pug", new Pug());
		CommandHandler.commands.put("meow", new Meow());
		CommandHandler.commands.put("rank", new Rank());
		CommandHandler.commands.put("profile", new Profile());
		CommandHandler.commands.put("display", new Display());
		CommandHandler.commands.put("top", new Top());
		CommandHandler.commands.put("register", new Register());
		CommandHandler.commands.put("set", new Set());
		CommandHandler.commands.put("shop", new Shop());
		CommandHandler.commands.put("purchase", new Purchase());
		CommandHandler.commands.put("use", new Use());
		CommandHandler.commands.put("inventory", new Inventory());
		CommandHandler.commands.put("daily", new Daily());
		CommandHandler.commands.put("user", new User());
		CommandHandler.commands.put("filter", new Filter());
		CommandHandler.commands.put("quiz", new Quiz());
		CommandHandler.commands.put("rolereaction", new RoleReaction());
		CommandHandler.commands.put("rss", new Rss());
		CommandHandler.commands.put("randomshop", new Randomshop());
		CommandHandler.commands.put("patchnotes", new Patchnotes());
		CommandHandler.commands.put("doubleexperience", new DoubleExperience());
	}
	
	public static void addListeners() {
		builder.addEventListeners(
				new ReadyListener(),
				new GuildListener(),
				new RoleListener(),
				new BanListener(),
				new UnbanListener(),
				new MessageListener(),
				new GuildLeaveListener(),
				new MessageEditListener(),
				new NameListener(),
				new GuildJoinListener(),
				new ShutdownListener(),
				new RoleRemovedListener(),
				new NicknameListener(),
				new MessageRemovedListener(),
				new AvatarUpdateListener(),
				new GuildMessageReactionAddListener(),
				new GuildMessageReactionRemoveListener(),
				new StatusListener(),
				new ReconnectedListener(),
				new ResumedListener(),
				new RoleCreateListener()
		);
	}
}
