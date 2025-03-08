package de.azrael.constructors;

/**
 * User class
 * @author xHelixStorm
 *
 */

public class User {
	private boolean isDefault = false;
	private long user_id;
	private String user_name;
	private String lang;
	private String avatar;
	private String creationDate;
	private String originalJoinDate;
	private String newestJoinDate;
	
	/**
	 * Default constructor for not found users
	 */
	
	public User() {
		isDefault = true;
	}
	
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
	 * @param _lang
	 * @param _avatar
	 * @param _creationDate
	 * @param _originalJoinDate
	 * @param _newestJoinDate
	 */
	
	public User(long _user_id, String _user_name, String _lang, String _avatar, String _creationDate, String _originalJoinDate, String _newestJoinDate) {
		this.user_id = _user_id;
		this.user_name = _user_name;
		this.lang = _lang;
		this.avatar = _avatar;
		this.creationDate = _creationDate;
		this.originalJoinDate = _originalJoinDate;
		this.newestJoinDate = _newestJoinDate;
	}
	
	/**
	 * Check if the default constructor was used
	 * @return
	 */
	
	public boolean isDefault() {
		return isDefault;
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
	 * Retrieve the language that the user is using
	 * @return lang
	 */
	
	public String getLang() {
		return lang;
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