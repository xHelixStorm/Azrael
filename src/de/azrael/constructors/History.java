package de.azrael.constructors;

import java.sql.Timestamp;

public class History {
	private String type;
	private String reason;
	private Timestamp time;
	private long penalty;
	private String info;
	
	public History(String _type, String _reason, Timestamp _time, long _penalty, String _info) {
		this.type = _type;
		this.reason = _reason;
		this.time = _time;
		this.penalty = _penalty;
		this.info = _info;
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
	public String getInfo() {
		return this.info;
	}
}
