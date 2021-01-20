package listeners;

import commands.ScheduleExecution;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rankingSystem.DoubleExperienceOff;
import rankingSystem.DoubleExperienceStart;
import timerTask.ClearHashes;
import timerTask.VerifyMutedMembers;
import util.STATIC;

/**
 * This class gets executed when the bot was able to 
 * reconnect to Discord. 
 * 
 * Main task is the restart of timers.
 * @author xHelixStorm
 * 
 */

public class ReconnectedListener extends ListenerAdapter{
	
	@Override
	public void onReconnect(ReconnectedEvent e) {
		//clear all timers
		STATIC.killAllTimers();
		//restart all timers
		if(IniFileReader.getDoubleExpEnabled()) {
			DoubleExperienceStart.runTask(null, e, null, null);
			DoubleExperienceOff.runTask();
		}
		for(final var guild : e.getJDA().getGuilds()) {
			ScheduleExecution.restartTimers(guild);
		}
		//clear temporary Hashes
		ClearHashes.runTask();
		//check for users that are still muted even though the time elapsed
		VerifyMutedMembers.runTask(null, e, null, false);
	}
}
