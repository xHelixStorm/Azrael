package rankingSystem;

import java.io.File;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class DoubleExperienceOff extends TimerTask{

	@Override
	public void run() {
		File doubleweekend = new File("./files/double.azr");
		doubleweekend.delete();
	}
	
	public static void runTask(){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		Timer time = new Timer();
		time.schedule(new DoubleExperienceOff(), calendar.getTime(), TimeUnit.DAYS.toMillis(7));
	}
}
