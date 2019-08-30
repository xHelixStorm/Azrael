package listeners;

import fileManagement.IniFileReader;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rankingSystem.DoubleExperienceOff;
import rankingSystem.DoubleExperienceStart;
import timerTask.ClearHashes;
import timerTask.VerifyMutedMembers;
import util.STATIC;

public class ReconnectedListener extends ListenerAdapter{
	
	@Override
	public void onReconnect(ReconnectedEvent e) {
		STATIC.killAllTimers();
		if(IniFileReader.getDoubleExpEnabled()) {
			DoubleExperienceStart.runTask(null, e, null, null);
			DoubleExperienceOff.runTask();
		}
		ClearHashes.runTask();
		VerifyMutedMembers.runTask(null, e, null);
	}
}
