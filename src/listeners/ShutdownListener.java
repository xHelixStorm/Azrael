package listeners;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fileManagement.FileSetting;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.Azrael;

public class ShutdownListener extends ListenerAdapter{
	
	@Override
	public void onShutdown(ShutdownEvent e){
		final Logger logger = LoggerFactory.getLogger(ShutdownListener.class);
		String filecontent = FileSetting.readFile("./files/reboot.azr");
		
		try {
			FileUtils.forceDelete(new File(IniFileReader.getTempDirectory()));
		} catch (IOException e2) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e2.printStackTrace();
		}
		
		if(SystemUtils.IS_OS_LINUX) {
			if(filecontent.contains("1")){
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
			if(filecontent.contains("1")){
				try {
					Process proc;
					proc = Runtime.getRuntime().exec("./scripts/restart.bat");
					proc.waitFor();
				} catch (IOException | InterruptedException e1) {
					logger.error("restart.bat script couldn't be started");
				}
			}
		}
		logger.debug("Bot has shut down or reboot has been commenced");
		Azrael.SQLInsertActionLog("BOT_SHUTDOWN", e.getJDA().getSelfUser().getIdLong(), 0, "Shutdown");
	}
}
