package de.azrael.listeners;

import de.azrael.commandsContainer.ScheduleExecution;
import de.azrael.rankingSystem.DoubleExperienceStart;
import de.azrael.subscription.SubscriptionUtils;
import de.azrael.timerTask.ClearHashes;
import de.azrael.timerTask.VerifyMutedMembers;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

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
		DoubleExperienceStart.runTask(e.getJDA().getGuilds());
		for(final var guild : e.getJDA().getGuilds()) {
			ScheduleExecution.restartTimers(guild);
		}
		SubscriptionUtils.startTimer(e.getJDA());
		//clear temporary Hashes
		ClearHashes.runTask();
		//check for users that are still muted even though the time elapsed
		VerifyMutedMembers.runTask(e.getJDA().getGuilds(), false);
	}
}
