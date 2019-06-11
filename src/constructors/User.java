package constructors;

public class User {
	private long user_id;
	private String user_name;
	private String avatar;
	private String joinDate;
	
	public User(long _user_id, String _user_name) {
		this.user_id = _user_id;
		this.user_name = _user_name;
	}
	
	public User(long _user_id, String _user_name, String _avatar, String _joinDate) {
		this.user_id = _user_id;
		this.user_name = _user_name;
		this.avatar = _avatar;
		this.joinDate = _joinDate;
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
	public String getJoinDate() {
		return joinDate;
	}
}