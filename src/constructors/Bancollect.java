package constructors;

import java.sql.Timestamp;

public class Bancollect {
	private long user_id;
	private long guild_id;
	private int warning_id;
	private int ban_id;
	private Timestamp timestamp;
	private Timestamp unmute;
	private boolean muted;
	private boolean customTime;
	private boolean guildLeft;
	
	public Bancollect() {
		this.user_id = 0;
		this.guild_id = 0;
		this.warning_id = 0;
		this.ban_id = 1;
		this.timestamp = null;
		this.unmute = null;
		this.muted = false;
		this.customTime = false;
		this.guildLeft = false;
	}
	
	public Bancollect(long _user_id, long _guild_id, int _warning_id, int _ban_id, Timestamp _timestamp, Timestamp _unmute, boolean _muted, boolean _customTime, boolean _guildLeft) {
		this.user_id = _user_id;
		this.guild_id = _guild_id;
		this.warning_id = _warning_id;
		this.ban_id = _ban_id;
		this.timestamp = _timestamp;
		this.unmute = _unmute;
		this.muted = _muted;
		this.customTime = _customTime;
		this.guildLeft = _guildLeft;
	}
	
	public long getUserID() {
		return user_id;
	}
	public long getGuildID() {
		return guild_id;
	}
	public int getWarningID() {
		return warning_id;
	}
	public int getBanID() {
		return ban_id;
	}
	public Timestamp getTimestamp() {
		return timestamp;
	}
	public Timestamp getUnmute() {
		return unmute;
	}
	public boolean getMuted() {
		return muted;
	}
	public boolean getCustomTime() {
		return customTime;
	}
	public boolean getGuildLeft() {
		return guildLeft;
	}
}
