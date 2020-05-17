package listeners;

import fileManagement.IniFileReader;
import net.dv8tion.jda.api.events.ResumedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rankingSystem.DoubleExperienceOff;
import rankingSystem.DoubleExperienceStart;
import timerTask.ClearHashes;
import timerTask.VerifyMutedMembers;
import util.STATIC;

/**
 * This class gets executed when the Bot was able to 
 * resume to Discord. 
 * 
 * Main task is the restart of timers.
 * @author xHelixStorm
 * 
 */

public class ResumedListener extends ListenerAdapter{
	
	@Override
	public void onResume(ResumedEvent e) {
		//clear all timers
		STATIC.killAllTimers();
		//restart timers for the double experience event
		if(IniFileReader.getDoubleExpEnabled()) {
			DoubleExperienceStart.runTask(null, null, e, null);
			DoubleExperienceOff.runTask();
		}
		//clear temporary Hashes
		ClearHashes.runTask();
		//check for users that are still muted even though the time elapsed
		VerifyMutedMembers.runTask(null, null, e, false);
	}
}
