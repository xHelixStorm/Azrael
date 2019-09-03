package constructors;

import java.sql.Timestamp;

public class History {
	private String type;
	private String reason;
	private Timestamp time;
	private long penalty;
	
	public History(String _type, String _reason, Timestamp _time, long _penalty) {
		this.type = _type;
		this.reason = _reason;
		this.time = _time;
		this.penalty = _penalty;
	}
	
	public String getType() {
		return this.type;
	}
	public String getReason() {
		return this.reason;
	}
	public String getTime() {
		return this.time.toString();
	}
	public long getPenalty() {
		return this.penalty;
	}
}
