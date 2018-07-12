package core;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Timestamp;

import javax.security.auth.login.LoginException;

import commands.About;
import commands.Commands;
import commands.Daily;
import commands.Display;
//import commands.GameStatus;
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
import fileManagement.IniFileReader;
import listeners.BanListener;
import listeners.CommandListener;
import listeners.GuildJoinListener;
import listeners.GuildLeaveListener;
import listeners.GuildListener;
import listeners.MessageEditListener;
import listeners.MessageListener;
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
		long time = System.currentTimeMillis();
		Timestamp timestamp = new Timestamp(time);
		
		try {
			PrintStream out = new PrintStream(new FileOutputStream("log/log"+timestamp.toString()+".txt"));
			System.setOut(out);
			System.setErr(out);
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
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
				return "H!about for informations";
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
	}
}
