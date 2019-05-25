package rankingSystem;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import core.Cache;
import core.Hashes;
import enums.Weekday;
import fileManagement.IniFileReader;
import util.STATIC;

public class DoubleExperienceOff extends TimerTask{

	@Override
	public void run() {
		Hashes.addTempCache("doubleExp", new Cache(0, "off"));
	}
	
	public static void runTask(){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_WEEK, Weekday.getDay(IniFileReader.getDoubleExpStart()));
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		
		Timer time = new Timer("doubleExpEnd");
		STATIC.addTimer(time);
		time.schedule(new DoubleExperienceOff(), calendar.getTime(), TimeUnit.DAYS.toMillis(7));
	}
}
