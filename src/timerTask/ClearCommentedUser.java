package timerTask;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;

public class ClearCommentedUser extends TimerTask {
	private final static Logger logger = LoggerFactory.getLogger(ClearHashes.class);
	//clear the records of users that has commented during a certain interval, to allow the user to gain experience points again
	
	@Override
	public void run() {
		Hashes.clearCommentedUsers();
		logger.debug("Commented user list cleared!");
	}
	
	public static void runTask(long timeout) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		Timer time = new Timer("ClearCommentedUser");
		time.schedule(new ClearCommentedUser(), calendar.getTime(), TimeUnit.MILLISECONDS.toMillis(timeout));
	}
}
