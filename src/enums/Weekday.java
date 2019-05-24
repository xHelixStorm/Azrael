package enums;

import java.util.Calendar;

public enum Weekday {
	Monday 		(Calendar.MONDAY),
	Tuesday 	(Calendar.TUESDAY),
	Wednesday 	(Calendar.WEDNESDAY),
	Thursday 	(Calendar.THURSDAY),
	Friday 		(Calendar.FRIDAY),
	Saturday 	(Calendar.SATURDAY),
	Sunday 		(Calendar.SUNDAY);
	
	private int day;
	
	private Weekday(int _day) {
		this.day = _day;
	}
	
	public static int getDay(Weekday _day) {
		return _day.day;
	}
}
