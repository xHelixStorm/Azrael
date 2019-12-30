package constructors;

public class User {
	private long user_id;
	private String user_name;
	private String avatar;
	private String originalJoinDate;
	private String newestJoinDate;
	
	public User(long _user_id, String _user_name) {
		this.user_id = _user_id;
		this.user_name = _user_name;
	}
	
	public User(long _user_id, String _user_name, String _avatar, String _originalJoinDate, String _newestJoinDate) {
		this.user_id = _user_id;
		this.user_name = _user_name;
		this.avatar = _avatar;
		this.originalJoinDate = _originalJoinDate;
		this.newestJoinDate = _newestJoinDate;
	}
	
	public long getUserID() {
		return user_id;
	}
	public String getUserName() {
		return user_name;
	}
	public String getAvatar() {
		return avatar;
	}
	public String getOriginalJoinDate() {
		return this.originalJoinDate;
	}
	public String getNewestJoinDate() {
		return this.newestJoinDate;
	}
}