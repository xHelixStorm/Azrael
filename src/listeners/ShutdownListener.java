package listeners;

/**
 * This class gets executed when the Bot has shutdown
 * 
 * Verify if that what was shutdown was a duplicate session
 * or a normal session and if it should be restarted.
 */

import java.io.IOException;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fileManagement.FileSetting;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.Azrael;
import util.STATIC;

public class ShutdownListener extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(ShutdownListener.class);
	
	@Override
	public void onShutdown(ShutdownEvent e) {
		//retrieve the file with the bot state (e.g. running / not running)
		String filecontent = FileSetting.readFile(IniFileReader.getTempDirectory()+STATIC.getSessionName()+"running.azr");
		
		//support only linux for a restart operation
		if(SystemUtils.IS_OS_LINUX) {
			//execute if the bot is labeled as running
			if(filecontent.contains("1")) {
				try {
					//execute screen command to restart the bot
					Process proc;
					proc = Runtime.getRuntime().exec("screen -dm "+(STATIC.getSessionName().length() > 0 ? "-S "+STATIC.getSessionName()+" " : "")+"java -jar --enable-preview Azrael.jar "+STATIC.getToken()+(STATIC.getSessionName().length() > 0 ? " "+STATIC.getSessionName() : "")+(STATIC.getAdmin() != 0 ? " "+STATIC.getAdmin() : ""));
					proc.waitFor();
				} catch (IOException | InterruptedException e1) {
					logger.error("Bot couldn't be restarted!");
				}
			}
		}
		//check if a duplicate session has been started and terminate the current session, if it occurred
		if(filecontent.contains("2")) {
			FileSetting.createFile(IniFileReader.getTempDirectory()+STATIC.getSessionName()+"running.azr", "1");
			logger.warn("Duplicate running session shut down!");
			Azrael.SQLInsertActionLog("DUPLICATE_SESSION", e.getJDA().getSelfUser().getIdLong(), 0, "Shutdown");
		}
		//do a regular shutdown
		else {
			logger.debug("Bot has shut down or reboot has been commenced");
			Azrael.SQLInsertActionLog("BOT_SHUTDOWN", e.getJDA().getSelfUser().getIdLong(), 0, "Shutdown");
		}
		System.exit(0);
	}
}
