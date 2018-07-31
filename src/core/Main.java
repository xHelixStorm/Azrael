package core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Timestamp;

import javax.security.auth.login.LoginException;

import commands.About;
import commands.Commands;
import commands.Daily;
import commands.Display;
import commands.Filter;
import commands.Help;
import commands.Inventory;
import commands.Meow;
import commands.Profile;
import commands.Pug;
import commands.Purchase;
import commands.Rank;
import commands.Reboot;
import commands.Register;
import commands.Set;
import commands.Shop;
import commands.ShutDown;
import commands.Top;
import commands.Use;
import commands.User;
import fileManagement.IniFileReader;
import listeners.BanListener;
import listeners.CommandListener;
import listeners.GuildJoinListener;
import listeners.GuildLeaveListener;
import listeners.GuildListener;
import listeners.MessageEditListener;
import listeners.MessageListener;
import listeners.MessageRemovedListener;
import listeners.NameListener;
import listeners.NicknameListener;
import listeners.ReadyListener;
import listeners.RoleListener;
import listeners.RoleRemovedListener;
import listeners.ShutdownListener;
import listeners.UnbanListener;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;

public class Main {

	public static JDABuilder builder;
	
	public static void main(String [] args){
		boolean [] dir = new boolean[2];
		dir[0] = (new File("./log")).mkdirs();
		dir[1] = (new File("./message_log")).mkdirs();
		
		long time = System.currentTimeMillis();
		Timestamp timestamp = new Timestamp(time);
		
		PrintStream out;
		try {
			out = new PrintStream(new FileOutputStream("log/log"+timestamp.toString().replaceAll(":", "-")+".txt"));
			System.setOut(out);
			System.setErr(out);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		String token = IniFileReader.getToken();
		builder = new JDABuilder(AccountType.BOT);
		builder.setToken(token);
		builder.setAutoReconnect(true);
		builder.setStatus(OnlineStatus.ONLINE);	
	
		addCommands();
		addListeners();
		
		builder.setGame(new Game(null){

			@Override
			public String getName() {
				return IniFileReader.getGameMessage();
			}

			@Override
			public GameType getType() {
				return GameType.DEFAULT;
			}

			@Override
			public String getUrl() {
				return null;
			}
			
		});
		
		try {
			@SuppressWarnings("unused")
			JDA jda = builder.buildBlocking();
		} catch (LoginException | IllegalArgumentException | InterruptedException e) {
			e.printStackTrace();
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
	}
	
	public static void addListeners(){
		
		builder.addEventListener(new ReadyListener());
		builder.addEventListener(new GuildListener());
		builder.addEventListener(new CommandListener());
		builder.addEventListener(new RoleListener());
		builder.addEventListener(new BanListener());
		builder.addEventListener(new UnbanListener());
		builder.addEventListener(new MessageListener());
		builder.addEventListener(new GuildLeaveListener());
		builder.addEventListener(new MessageEditListener());
		builder.addEventListener(new NameListener());
		builder.addEventListener(new GuildJoinListener());
		builder.addEventListener(new ShutdownListener());
		builder.addEventListener(new RoleRemovedListener());
		builder.addEventListener(new NicknameListener());
		builder.addEventListener(new MessageRemovedListener());
	}
}
