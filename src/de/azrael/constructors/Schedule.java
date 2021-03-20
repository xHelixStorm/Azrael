package de.azrael.constructors;

public class Schedule {
	private int schedule_id;
	private long channel_id;
	private String message;
	private int time;
	private boolean monday;
	private boolean tuesday;
	private boolean wednesday;
	private boolean thursday;
	private boolean friday;
	private boolean saturday;
	private boolean sunday;
	
	/**
	 * Default constructor without parameters to initialize all days
	 */
	
	public Schedule() {
		this.monday = true;
		this.tuesday = true;
		this.wednesday = true;
		this.thursday = true;
		this.friday = true;
		this.saturday = true;
		this.sunday = true;
	}
	
	/**
	 * constructor for a scheduled task with parameters
	 * @param _schedule_id
	 * @param _channel_id
	 * @param _message
	 * @param _time
	 * @param _monday
	 * @param _tuesday
	 * @param _wednesday
	 * @param _thursday
	 * @param _friday
	 * @param _saturday
	 * @param _sunday
	 */
	
	public Schedule(int _schedule_id, long _channel_id, String _message, int _time, boolean _monday, boolean _tuesday, boolean _wednesday, boolean _thursday, boolean _friday, boolean _saturday, boolean _sunday) {
		this.schedule_id = _schedule_id;
		this.channel_id = _channel_id;
		this.message = _message;
		this.time = _time;
		this.monday = _monday;
		this.tuesday = _tuesday;
		this.wednesday = _wednesday;
		this.thursday = _thursday;
		this.friday = _friday;
		this.saturday = _saturday;
		this.sunday = _sunday;
	}
	
	/**
	 * Set channel id
	 * @param _channel_id
	 */
	public void setChannelId(long _channel_id) {
		this.channel_id = _channel_id;
	}
	
	/**
	 * Set the message
	 * @param _message
	 */
	public void setMessage(String _message) {
		this.message = _message;
	}
	
	/**
	 * Set the time of day
	 * @param _time
	 */
	public void setTime(int _time) {
		this.time = _time;
	}
	
	/**
	 * Enable or disable Monday
	 * @param monday
	 */
	public void setMonday(boolean monday) {
		this.monday = monday;
	}

	/**
	 * Enable or disable Tuesday
	 * @param tuesday
	 */
	public void setTuesday(boolean tuesday) {
		this.tuesday = tuesday;
	}

	/**
	 * Enable or disable Wednesday
	 * @param wednesday
	 */
	public void setWednesday(boolean wednesday) {
		this.wednesday = wednesday;
	}

	/**
	 * Enable or disable Thursday
	 * @param thursday
	 */
	public void setThursday(boolean thursday) {
		this.thursday = thursday;
	}

	/**
	 * Enable or disable Friday
	 * @param friday
	 */
	public void setFriday(boolean friday) {
		this.friday = friday;
	}

	/**
	 * Enable or disable Saturday
	 * @param saturday
	 */
	public void setSaturday(boolean saturday) {
		this.saturday = saturday;
	}

	/**
	 * Enable or disable Sunday
	 * @param sunday
	 */
	public void setSunday(boolean sunday) {
		this.sunday = sunday;
	}
	
	/**
	 * Retrieve the schedule id
	 * @return
	 */
	public int getSchedule_id() {
		return schedule_id;
	}

	/**
	 * Retrive the channel id
	 * @return
	 */
	public long getChannel_id() {
		return channel_id;
	}
	
	/**
	 * Retrieve the message
	 * @return
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Retrieve time of send
	 * @return
	 */
	public int getTime() {
		return time;
	}

	/**
	 * is Monday enabled
	 * @return
	 */
	public boolean isMonday() {
		return monday;
	}

	/**
	 * is Tuesday enabled
	 * @return
	 */
	public boolean isTuesday() {
		return tuesday;
	}

	/**
	 * is Wednesday enabled
	 * @return
	 */
	public boolean isWednesday() {
		return wednesday;
	}

	/**
	 * is Thursday enabled
	 * @return
	 */
	public boolean isThursday() {
		return thursday;
	}

	/**
	 * is Friday enabled
	 * @return
	 */
	public boolean isFriday() {
		return friday;
	}

	/**
	 * is Saturday enabled
	 * @return
	 */
	public boolean isSaturday() {
		return saturday;
	}

	/**
	 * is Sunday enabled
	 * @return
	 */
	public boolean isSunday() {
		return sunday;
	}
}
