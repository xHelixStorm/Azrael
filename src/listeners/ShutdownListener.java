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
import util.STATIC;

public class ShutdownListener extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(ShutdownListener.class);
	
	@Override
	public void onShutdown(ShutdownEvent e) {
		String filecontent = FileSetting.readFile(IniFileReader.getTempDirectory()+STATIC.getSessionName()+"running.azr");
		
		if(SystemUtils.IS_OS_LINUX) {
			if(filecontent.contains("1")) {
				deleteTemp();
				try {
					Process proc;
					proc = Runtime.getRuntime().exec("screen "+(STATIC.getSessionName().length() > 0 ? "-S "+STATIC.getSessionName()+" " : "")+"java -jar --enable-preview Azrael.jar "+STATIC.getToken()+" "+STATIC.getSessionName()+" "+(STATIC.getAdmin() != 0 ? STATIC.getAdmin() : ""));
					proc.waitFor();
				} catch (IOException | InterruptedException e1) {
					logger.error("Bot couldn't be restarted!");
				}
			}
		}
		if(filecontent.contains("2")) {
			FileSetting.createFile(IniFileReader.getTempDirectory()+STATIC.getSessionName()+"running.azr", "1");
			logger.warn("Duplicate running session shut down!");
			Azrael.SQLInsertActionLog("DUPLICATE_SESSION", e.getJDA().getSelfUser().getIdLong(), 0, "Shutdown");
		}
		else {
			logger.debug("Bot has shut down or reboot has been commenced");
			Azrael.SQLInsertActionLog("BOT_SHUTDOWN", e.getJDA().getSelfUser().getIdLong(), 0, "Shutdown");
		}
		System.exit(0);
	}
	
	private static void deleteTemp() {
		try {
			FileUtils.forceDelete(new File(IniFileReader.getTempDirectory()));
		} catch (IOException e2) {
			logger.error("Temp directory couldn't be deleted", e2);
		}
	}
}
