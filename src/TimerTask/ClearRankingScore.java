package TimerTask;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import core.Hashes;

public class ClearRankingScore extends TimerTask{
	//this class is meant to clear the cache of ranks, shop and dailies every 12h

	@Override
	public void run() {
		Hashes.clearRankList();
		Hashes.clearShopContent();
		Hashes.clearDailyItems();
		
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		System.out.println("["+timestamp+"] Cache has been cleared!");
	}
	
	public static void runTask(){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 12);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		Timer time = new Timer();
		time.schedule(new ClearRankingScore(), calendar.getTime(), TimeUnit.HOURS.toMillis(12));
	}
}
