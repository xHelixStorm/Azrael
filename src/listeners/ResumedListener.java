package listeners;

import fileManagement.IniFileReader;
import net.dv8tion.jda.core.events.ResumedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import rankingSystem.DoubleExperienceOff;
import rankingSystem.DoubleExperienceStart;
import util.STATIC;

public class ResumedListener extends ListenerAdapter{
	
	@Override
	public void onResume(ResumedEvent e) {
		if(IniFileReader.getDoubleExpEnabled()) {
			STATIC.killAllTimers();
			DoubleExperienceStart.runTask(null, null, e, null);
			DoubleExperienceOff.runTask();
		}
	}
}
