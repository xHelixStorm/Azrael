package listeners;

import java.io.IOException;

import fileManagement.FileSetting;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.SqlConnect;

public class ShutdownListener extends ListenerAdapter{
	
	@Override
	public void onShutdown(ShutdownEvent e){
		String filecontent = FileSetting.readFile("./files/reboot");
		
		try {
			Process proc;
			proc = Runtime.getRuntime().exec("rm -rf "+IniFileReader.getTempDirectory());
			proc.waitFor();
		} catch (InterruptedException | IOException e1) {
			e1.printStackTrace();
		}
		
		if(filecontent.contains("1")){
			try {
				Process proc;
				proc = Runtime.getRuntime().exec("./scripts/restart.sh");
				proc.waitFor();
			} catch (IOException | InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		SqlConnect.SQLInsertActionLog("BOT_SHUTDOWN", e.getJDA().getSelfUser().getIdLong(), 0, "Shutdown");
	}
}
