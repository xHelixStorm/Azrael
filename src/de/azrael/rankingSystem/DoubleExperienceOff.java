package de.azrael.rankingSystem;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Cache;
import de.azrael.core.Hashes;
import de.azrael.enums.Weekday;
import de.azrael.fileManagement.IniFileReader;
import de.azrael.util.STATIC;

public class DoubleExperienceOff extends TimerTask {
	private static final Logger logger = LoggerFactory.getLogger(DoubleExperienceOff.class);

	@Override
	public void run() {
		Hashes.addTempCache("doubleExp", new Cache(0, "off"));
		logger.info("Double experience has been turned off globaly");
	}
	
	public static void runTask() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_WEEK, Weekday.getDay(IniFileReader.getDoubleExpEnd()));
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		
		Timer time = new Timer("doubleExpEnd");
		STATIC.addTimer(time);
		time.schedule(new DoubleExperienceOff(), calendar.getTime(), TimeUnit.DAYS.toMillis(7));
	}
}
