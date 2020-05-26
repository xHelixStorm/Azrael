package constructors;

public class RejoinTask {
	private long user_id;
	private long guild_id;
	private String type;
	private String reason;
	private String reporter;
	private String time;
	
	public RejoinTask(long _user_id, long _guild_id, String _type, String _reason, String _reporter, String _time) {
		this.user_id = _user_id;
		this.guild_id = _guild_id;
		this.type = _type;
		this.reason = _reason;
		this.reporter = _reporter;
		this.time = _time;
	}
	
	public long getUserID() {
		return this.user_id;
	}
	public long getGuildID() {
		return this.guild_id;
	}
	public String getType() {
		return this.type;
	}
	public String getReason() {
		return this.reason;
	}
	public String getReporter() {
		return this.reporter;
	}
	public String getTime() {
		return this.time;
	}
}
