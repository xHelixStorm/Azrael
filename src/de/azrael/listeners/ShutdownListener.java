package de.azrael.listeners;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.enums.Translation;
import de.azrael.fileManagement.FileSetting;
import de.azrael.fileManagement.IniFileReader;
import de.azrael.sql.Azrael;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ShutdownEvent;
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
		//retrieve the file with the bot state (e.g. running / not running)
		String filecontent = FileSetting.readFile(IniFileReader.getTempDirectory()+STATIC.getSessionName()+"running.azr");
		
		//execute if the bot is labeled as running
		if(filecontent.contains("1")) {
			FileSetting.createFile(IniFileReader.getTempDirectory()+STATIC.getSessionName()+"running.azr", "0");
			try {
				Process proc;
				//execute command to restart the bot
				proc = Runtime.getRuntime().exec((IniFileReader.getLinuxScreen() ? "screen -dm "+(STATIC.getSessionName().length() > 0 ? "-S "+STATIC.getSessionName()+" " : "") : "")+"java -jar --enable-preview Azrael.jar "+compileParameters());
				proc.waitFor();
			} catch (IOException | InterruptedException e1) {
				logger.error("Bot couldn't be restarted!");
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
			logger.info("Bot has shut down or reboot has been commenced!");
			Azrael.SQLInsertActionLog("BOT_SHUTDOWN", e.getJDA().getSelfUser().getIdLong(), 0, "Shutdown");
		}
		System.exit(0);
	}
	
	private static String compileParameters() {
		StringBuilder params = new StringBuilder();
		params.append(STATIC.getToken());
		if(STATIC.getSessionName().length() > 0)
			params.append(" sessionname:"+STATIC.getSessionName());
		if(STATIC.getTimezone().length() > 0)
			params.append(" timezone:"+STATIC.getTimezone());
		if(STATIC.getActionLog().length() > 0)
			params.append(" actionlog:"+STATIC.getActionLog());
		if(STATIC.getDoubleExperience().length() > 0)
			params.append(" doubleexperience:"+STATIC.getDoubleExperience());
		if(STATIC.getDoubleExperienceStart().length() > 0)
			params.append(" doubleexperiencestart:"+STATIC.getDoubleExperienceStart());
		if(STATIC.getDoubleExperienceEnd().length() > 0)
			params.append(" doubleexperienceend:"+STATIC.getDoubleExperienceEnd());
		if(STATIC.getCountMembers().length() > 0)
			params.append(" countmembers:"+STATIC.getCountMembers());
		if(STATIC.getFileLogger().length() > 0)
			params.append(" filelogger:"+STATIC.getFileLogger());
		if(STATIC.getGameMessage().length() > 0)
			params.append(" gamemessage:"+STATIC.getGameMessage());
		if(STATIC.getTemp().length() > 0)
			params.append(" temp:"+STATIC.getTemp());
		
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
