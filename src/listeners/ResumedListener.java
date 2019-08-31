package listeners;

import fileManagement.IniFileReader;
import net.dv8tion.jda.api.events.ResumedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rankingSystem.DoubleExperienceOff;
import rankingSystem.DoubleExperienceStart;
import timerTask.ClearHashes;
import timerTask.VerifyMutedMembers;
import util.STATIC;

public class ResumedListener extends ListenerAdapter{
	
	@Override
	public void onResume(ResumedEvent e) {
		STATIC.killAllTimers();
		if(IniFileReader.getDoubleExpEnabled()) {
			DoubleExperienceStart.runTask(null, null, e, null);
			DoubleExperienceOff.runTask();
		}
		ClearHashes.runTask();
		VerifyMutedMembers.runTask(null, null, e, false);
	}
}
