package de.azrael.constructors;

/**
 * Cache application. Write a certain action into the ram which can expire
 * @author xHelixStorm
 *
 */

public class Cache {
	private long expiration;
	private String additionalInfo;
	private String additionalInfo2;
	private String additionalInfo3;
	private String additionalInfo4;
	private boolean expire;
	private Object object;
	
	/**
	 * Empty cache with only the expiration time in miliseconds + the time of now
	 * @param _expiration
	 */
	
	public Cache(long _expiration) {
		this.expiration = System.currentTimeMillis() + _expiration;
		this.additionalInfo = "";
		this.additionalInfo2 = "";
		this.additionalInfo3 = "";
		this.additionalInfo4 = "";
		this.expire = true;
	}
	
	/**
	 * Constructor for the expiration time and a value
	 * @param _expiration
	 * @param _additionalInfo 
	 */
	
	public Cache(long _expiration, String _additionalInfo) {
		this.expiration = System.currentTimeMillis() + _expiration;
		this.additionalInfo = _additionalInfo;
		this.additionalInfo2 = "";
		this.additionalInfo3 = "";
		this.additionalInfo4 = "";
		this.expire = true;
	}
	
	/**
	 * Constructor for the expiration time and two values
	 * @param _expiration
	 * @param _additionalInfo
	 * @param _additionalInfo2
	 */
	
	public Cache(long _expiration, String _additionalInfo, String _additionalInfo2) {
		this.expiration = System.currentTimeMillis() + _expiration;
		this.additionalInfo = _additionalInfo;
		this.additionalInfo2 = _additionalInfo2;
		this.additionalInfo3 = "";
		this.additionalInfo4 = "";
		this.expire = true;
	}
	
	/**
	 * Constructor for the expiration time and three values
	 * @param _expiration
	 * @param _additionalInfo
	 * @param _additionalInfo2
	 * @param _additionalInfo3
	 */
	
	public Cache(long _expiration, String _additionalInfo, String _additionalInfo2, String _additionalInfo3) {
		this.expiration = System.currentTimeMillis() + _expiration;
		this.additionalInfo = _additionalInfo;
		this.additionalInfo2 = _additionalInfo2;
		this.additionalInfo3 = _additionalInfo3;
		this.additionalInfo4 = "";
		this.expire = true;
	}
	
	/**
	 * Constructor for the expiration time and four values
	 * @param _expiration
	 * @param _additionalInfo
	 * @param _additionalInfo2
	 * @param _additionalInfo3
	 * @param _additionalInfo4
	 */
	
	public Cache(long _expiration, String _additionalInfo, String _additionalInfo2, String _additionalInfo3, String _additionalInfo4) {
		this.expiration = System.currentTimeMillis() + _expiration;
		this.additionalInfo = _additionalInfo;
		this.additionalInfo2 = _additionalInfo2;
		this.additionalInfo3 = _additionalInfo3;
		this.additionalInfo4 = _additionalInfo4;
		this.expire = true;
	}
	
	/**
	 * Constructor to write a value which doesn't expire
	 * @param _additionalInfo
	 */
	
	public Cache(String _additionalInfo) {
		this.expiration = System.currentTimeMillis();
		this.additionalInfo = _additionalInfo;
		this.additionalInfo2 = "";
		this.additionalInfo3 = "";
		this.additionalInfo4 = "";
		this.expire = false;
	}
	
	/**
	 * Constructor to write two values which don't expire
	 * @param _additionalInfo
	 * @param _additionalInfo2
	 */
	
	public Cache(String _additionalInfo, String _additionalInfo2) {
		this.expiration = System.currentTimeMillis();
		this.additionalInfo = _additionalInfo;
		this.additionalInfo2 = _additionalInfo2;
		this.additionalInfo3 = "";
		this.additionalInfo4 = "";
		this.expire = false;
	}
	
	/**
	 * Constructor to write three values which don't expire
	 * @param _additionalInfo
	 * @param _additionalInfo2
	 * @param _additionalInfo3
	 */
	
	public Cache(String _additionalInfo, String _additionalInfo2, String _additionalInfo3) {
		this.expiration = System.currentTimeMillis();
		this.additionalInfo = _additionalInfo;
		this.additionalInfo2 = _additionalInfo2;
		this.additionalInfo3 = _additionalInfo3;
		this.additionalInfo4 = "";
		this.expire = false;
	}
	
	/**
	 * Constructor to write four values which don't expire
	 * @param _additionalInfo
	 * @param _additionalInfo2
	 * @param _additionalInfo3
	 * @param _additionalInfo4
	 */
	
	public Cache(String _additionalInfo, String _additionalInfo2, String _additionalInfo3, String _additionalInfo4) {
		this.expiration = System.currentTimeMillis();
		this.additionalInfo = _additionalInfo;
		this.additionalInfo2 = _additionalInfo2;
		this.additionalInfo3 = _additionalInfo3;
		this.additionalInfo4 = _additionalInfo4;
		this.expire = false;
	}
	
	/**
	 * Save any type of object
	 * @param _object
	 * @return
	 */
	
	public Cache setObject(Object _object) {
		this.object = _object;
		return this;
	}
	
	/**
	 * Retrieve the expiration time in miliseconds
	 * @return
	 */
	
	public long getExpiration() {
		return this.expiration;
	}
	
	/**
	 * Retrieve the first temporary value
	 * @return
	 */
	
	public String getAdditionalInfo() {
		return this.additionalInfo;
	}
	
	/**
	 * Retrieve the second temporary value
	 * @return
	 */
	
	public String getAdditionalInfo2() {
		return this.additionalInfo2;
	}
	
	/**
	 * Retrieve the third temporary value
	 * @return
	 */
	
	public String getAdditionalInfo3() {
		return this.additionalInfo3;
	}
	
	/**
	 * Retrieve the fourth temporary value
	 * @return
	 */
	
	public String getAdditionalInfo4() {
		return this.additionalInfo4;
	}
	
	/**
	 * Return the value if it can expire
	 * @return
	 */
	
	public boolean getExpire() {
		return this.expire;
	}
	
	/**
	 * Update the expiration time
	 * @param time
	 * @return
	 */
	
	public Cache setExpiration(long time) {
		this.expiration = System.currentTimeMillis() + time;
		return this;
	}
	
	/**
	 * Update the first value
	 * @param description
	 * @return
	 */
	
	public Cache updateDescription(String description) {
		this.additionalInfo = description;
		return this;
	}
	
	/**
	 * Update the second value
	 * @param description
	 * @return
	 */
	
	public Cache updateDescription2(String description) {
		this.additionalInfo2 = description;
		return this;
	}
	
	/**
	 * Update the third value
	 * @param description
	 * @return
	 */
	
	public Cache updateDescription3(String description) {
		this.additionalInfo3 = description;
		return this;
	}
	
	/**
	 * Update the fourth value
	 * @param description
	 * @return
	 */
	
	public Cache updateDescription4(String description) {
		this.additionalInfo4 = description;
		return this;
	}
	
	/**
	 * Return saved object
	 * @return
	 */
	
	public Object getObject() {
		return this.object;
	}
}
