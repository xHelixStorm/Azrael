package de.azrael.timerTask;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.core.Hashes;

/**
 * clear the records of users that have commented during a certain interval, to allow the user to gain experience points again
 * @author xHelixStorm
 *
 */

public class ClearCommentedUser extends TimerTask {
	private final static Logger logger = LoggerFactory.getLogger(ClearHashes.class);
	
	@Override
	public void run() {
		Hashes.clearCommentedUsers();
		logger.trace("Commented ranking user list cleared");
	}
	
	public static void runTask(long timeout) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		Timer time = new Timer("ClearCommentedUser");
		time.schedule(new ClearCommentedUser(), calendar.getTime(), TimeUnit.MILLISECONDS.toMillis(timeout));
	}
}