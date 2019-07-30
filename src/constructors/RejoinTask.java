package constructors;

public class RejoinTask {
	private long user_id;
	private long guild_id;
	private String info;
	private String type;
	private String reason;
	
	public RejoinTask(long _user_id, long _guild_id, String _info, String _type, String _reason) {
		this.user_id = _user_id;
		this.guild_id = _guild_id;
		this.info = _info;
		this.type = _type;
		this.reason = _reason;
	}
	
	public long getUserID() {
		return user_id;
	}
	public long getGuildID() {
		return guild_id;
	}
	public String getInfo() {
		return info;
	}
	public String getType() {
		return type;
	}
	public String getReason() {
		return reason;
	}
}
