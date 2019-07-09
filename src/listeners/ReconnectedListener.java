package listeners;

import fileManagement.IniFileReader;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rankingSystem.DoubleExperienceOff;
import rankingSystem.DoubleExperienceStart;
import util.STATIC;

public class ReconnectedListener extends ListenerAdapter{
	
	@Override
	public void onReconnect(ReconnectedEvent e) {
		if(IniFileReader.getDoubleExpEnabled()) {
			STATIC.killAllTimers();
			DoubleExperienceStart.runTask(null, e, null, null);
			DoubleExperienceOff.runTask();
		}
	}
}
