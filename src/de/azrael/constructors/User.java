package de.azrael.constructors;

/**
 * User class
 * @author xHelixStorm
 *
 */

public class User {
	private long user_id;
	private String user_name;
	private String avatar;
	private String creationDate;
	private String originalJoinDate;
	private String newestJoinDate;
	
	/**
	 * Constructor with user_id and name
	 * @param _user_id
	 * @param _user_name
	 */
	
	public User(long _user_id, String _user_name) {
		this.user_id = _user_id;
		this.user_name = _user_name;
	}
	
	/**
	 * Constructor with user_id, name and avatar url
	 * @param _user_id
	 * @param _user_name
	 * @param _avatar
	 * @param _creationDate
	 */
	
	public User(long _user_id, String _user_name, String _avatar, String _creationDate) {
		this.user_id = _user_id;
		this.user_name = _user_name;
		this.avatar = _avatar;
		this.creationDate = _creationDate;
	}
	
	/**
	 * Join dates setter
	 * @param _originalJoinDate
	 * @param _newestJoinDate
	 * @return
	 */
	
	public User setJoinDates(String _originalJoinDate, String _newestJoinDate) {
		this.originalJoinDate = _originalJoinDate;
		this.newestJoinDate = _newestJoinDate;
		return this;
	}
	
	/**
	 * Retrieve user id
	 * @return user_id
	 */
	
	public long getUserID() {
		return user_id;
	}
	
	/**
	 * Retrieve user name (only original name)
	 * @return user_name
	 */
	
	public String getUserName() {
		return user_name;
	}
	
	/**
	 * Retrieve avatar url
	 * @return avatar
	 */
	
	public String getAvatar() {
		return avatar;
	}
	
	/**
	 * Retrieve creation date
	 * @return creation date
	 */
	
	public String getCreationDate() {
		return creationDate;
	}
	
	/**
	 * Retrieve the very first join date
	 * @return originalJoinDate
	 */
	
	public String getOriginalJoinDate() {
		return this.originalJoinDate;
	}
	
	/**
	 * Retrieve the newest join date
	 * @return newestJoinDate
	 */
	
	public String getNewestJoinDate() {
		return this.newestJoinDate;
	}
}