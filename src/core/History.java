package core;

import java.sql.Timestamp;

public class History {
	private String type;
	private String reason;
	private Timestamp time;
	
	public History(String _type, String _reason, Timestamp _time) {
		this.type = _type;
		this.reason = _reason;
		this.time = _time;
	}
	
	public String getType() {
		return type;
	}
	public String getReason() {
		return reason;
	}
	public String getTime() {
		return time.toString();
	}
}
