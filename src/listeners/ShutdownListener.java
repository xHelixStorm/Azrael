package listeners;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fileManagement.FileSetting;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.Azrael;

public class ShutdownListener extends ListenerAdapter{
	
	@Override
	public void onShutdown(ShutdownEvent e){
		final Logger logger = LoggerFactory.getLogger(ShutdownListener.class);
		String filecontent = FileSetting.readFile(IniFileReader.getTempDirectory()+"running.azr");
		
		if(SystemUtils.IS_OS_LINUX) {
			if(filecontent.contains("1")) {
				deleteTemp(logger);
				try {
					Process proc;
					proc = Runtime.getRuntime().exec("./scripts/restart.sh");
					proc.waitFor();
				} catch (IOException | InterruptedException e1) {
					logger.error("restart.sh script couldn't be started");
				}
			}
		}
		else if(SystemUtils.IS_OS_WINDOWS) {			
			if(filecontent.contains("1")) {
				deleteTemp(logger);
				try {
					Process proc;
					proc = Runtime.getRuntime().exec("./scripts/restart.bat");
					proc.waitFor();
				} catch (IOException | InterruptedException e1) {
					logger.error("restart.bat script couldn't be started");
				}
			}
		}
		if(filecontent.contains("2")) {
			FileSetting.createFile(IniFileReader.getTempDirectory()+"running.azr", "1");
			logger.warn("Duplicate running session shut down!");
			Azrael.SQLInsertActionLog("DUPLICATE_SESSION", e.getJDA().getSelfUser().getIdLong(), 0, "Shutdown");
		}
		else {
			logger.debug("Bot has shut down or reboot has been commenced");
			Azrael.SQLInsertActionLog("BOT_SHUTDOWN", e.getJDA().getSelfUser().getIdLong(), 0, "Shutdown");
		}
		System.exit(0);
	}
	
	private static void deleteTemp(Logger logger) {
		try {
			FileUtils.forceDelete(new File(IniFileReader.getTempDirectory()));
		} catch (IOException e2) {
			logger.error("Temp directory couldn't be deleted", e2);
		}
	}
}
