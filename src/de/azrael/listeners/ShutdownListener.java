package de.azrael.listeners;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.enums.Directory;
import de.azrael.enums.Translation;
import de.azrael.sql.Azrael;
import de.azrael.util.FileHandler;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * This class gets executed when the Bot has shutdown
 * 
 * Verify if that what was shutdown was a duplicate session
 * or a normal session and if it should be restarted.
 * @author xHelixStorm
 * 
 */

public class ShutdownListener extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(ShutdownListener.class);
	
	private static int shutdownCountdown = 0;
	private static TextChannel shutdownChannel = null;
	private static JDA shutdownJda = null;
	
	@Override
	public void onShutdown(ShutdownEvent e) {
		final String sessionName = System.getProperty("SESSION_NAME");
		final String fileName = sessionName+"running.azr";
		final String pid = ""+ProcessHandle.current().pid();
		
		//retrieve the file with the bot pid value
		String fileContent = FileHandler.readFile(Directory.TEMP, fileName);
		
		//execute if the retrieved pid is the same as of current pid
		if(!fileContent.isBlank() && fileContent.contains(""+pid)) {
			FileHandler.deleteFile(Directory.TEMP, fileName);
			try {
				Process proc;
				//execute command to restart the bot
				proc = Runtime.getRuntime().exec("screen -dm -S "+sessionName+" java -Dtwitter4j.loggerFactory=twitter4j.NullLoggerFactory -jar Azrael.jar "+compileParameters());
				proc.waitFor();
			} catch (IOException | InterruptedException e1) {
				logger.error("Bot couldn't be restarted!", e1);
			}
		}
		
		//check if a duplicate session has been started and terminate the current session, if it occurred
		if(!fileContent.isBlank() && !fileContent.contains(""+pid)) {
			logger.warn("Duplicate running session shut down!");
			Azrael.SQLInsertActionLog("DUPLICATE_SESSION", e.getJDA().getSelfUser().getIdLong(), 0, "Shutdown");
		}
		//do a regular shutdown
		else {
			logger.info("Bot has shut down or reboot has been commenced!");
			Azrael.SQLInsertActionLog("BOT_SHUTDOWN", e.getJDA().getSelfUser().getIdLong(), 0, "Shutdown");
		}
		System.exit(0);
	}
	
	private static String compileParameters() {
		StringBuilder params = new StringBuilder();
		params.append(System.getProperty("TOKEN"));
		params.append(" sessionname:"+System.getProperty("SESSION_NAME"));
		params.append(" encryption:"+System.getProperty("AES_SECRET"));
		params.append(" actionlog:"+System.getProperty("ACTION_LOG"));
		params.append(" countguilds:"+System.getProperty("COUNT_GUILDS"));
		params.append(" statusmessage:"+System.getProperty("STATUS_MESSAGE"));
		params.append(" homepage:"+System.getProperty("HOMEPAGE"));
		params.append(" port:"+System.getProperty("WEBSERVER_PORT"));
		params.append(" temp:"+Directory.TEMP.getPath());
		params.append(" spreadsheetdelay:"+System.getProperty("SPREADSHEET_UPDATE_DELAY"));
		
		return params.toString();
	}
	
	public static synchronized void incrementShutdownCountDown() {
		shutdownCountdown++;
	}
	
	public static void setShutdownChannel(TextChannel channel) {
		shutdownChannel = channel;
	}
	
	public static void setShutdownJDA(JDA jda) {
		shutdownJda = jda;
	}
	
	public static synchronized void decreaseShutdownCountdown() {
		shutdownCountdown--;
		if(shutdownCountdown == 0) {
			if(shutdownChannel != null) {
				shutdownChannel.sendMessage(STATIC.getTranslation2(shutdownChannel.getGuild(), Translation.SHUTDOWN)).queue();
				shutdownChannel.getJDA().shutdownNow();
			}
			else if(shutdownJda != null) {
				shutdownJda.shutdownNow();
			}
		}
	}
}
