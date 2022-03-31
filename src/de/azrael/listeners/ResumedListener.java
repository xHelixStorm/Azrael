package de.azrael.listeners;

import de.azrael.commandsContainer.ScheduleExecution;
import de.azrael.rankingSystem.DoubleExperienceStart;
import de.azrael.timerTask.ClearHashes;
import de.azrael.timerTask.VerifyMutedMembers;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.events.ResumedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

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
		//restart all timers
		DoubleExperienceStart.runTask(null, null, e, null);
		for(final var guild : e.getJDA().getGuilds()) {
			ScheduleExecution.restartTimers(guild);
		}
		//clear temporary Hashes
		ClearHashes.runTask();
		//check for users that are still muted even though the time elapsed
		VerifyMutedMembers.runTask(null, null, e, false);
	}
}
