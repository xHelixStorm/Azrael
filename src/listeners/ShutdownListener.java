package listeners;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import fileManagement.FileSetting;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.SqlConnect;

public class ShutdownListener extends ListenerAdapter{
	
	@Override
	public void onShutdown(ShutdownEvent e){
		String filecontent = FileSetting.readFile("./files/reboot.azr");
		
		try {
			FileUtils.forceDelete(new File(IniFileReader.getTempDirectory()));
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
		if(SystemUtils.IS_OS_LINUX) {
			if(filecontent.contains("1")){
				try {
					Process proc;
					proc = Runtime.getRuntime().exec("./scripts/restart.sh");
					proc.waitFor();
				} catch (IOException | InterruptedException e1) {
					e1.printStackTrace();
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
					e1.printStackTrace();
				}
			}
		}
		SqlConnect.SQLInsertActionLog("BOT_SHUTDOWN", e.getJDA().getSelfUser().getIdLong(), 0, "Shutdown");
	}
}
