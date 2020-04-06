package constructors;

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
	private boolean expire;
	
	/**
	 * Empty cache with only the expiration time in miliseconds + the time of now
	 * @param _expiration
	 */
	
	public Cache(long _expiration) {
		this.expiration = System.currentTimeMillis() + _expiration;
		this.additionalInfo = "";
		this.additionalInfo2 = "";
		this.additionalInfo3 = "";
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
		this.expire = false;
	}
	
	/**
	 * Constructor to write two values which doesn't expire
	 * @param _additionalInfo
	 * @param _additionalInfo2
	 */
	
	public Cache(String _additionalInfo, String _additionalInfo2) {
		this.expiration = System.currentTimeMillis();
		this.additionalInfo = _additionalInfo;
		this.additionalInfo2 = _additionalInfo2;
		this.additionalInfo3 = "";
		this.expire = false;
	}
	
	/**
	 * Constructor to write three values which doesn't expire
	 * @param _additionalInfo
	 * @param _additionalInfo2
	 * @param _additionalInfo3
	 */
	
	public Cache(String _additionalInfo, String _additionalInfo2, String _additionalInfo3) {
		this.expiration = System.currentTimeMillis();
		this.additionalInfo = _additionalInfo;
		this.additionalInfo2 = _additionalInfo2;
		this.additionalInfo3 = _additionalInfo3;
		this.expire = false;
	}
	
	/**
	 * Retrieve the expiration time in miliseconds
	 * @return
	 */
	
	public long getExpiration() {
		return expiration;
	}
	
	/**
	 * Retrieve the first temporary value
	 * @return
	 */
	
	public String getAdditionalInfo() {
		return additionalInfo;
	}
	
	/**
	 * Retrieve the second temporary value
	 * @return
	 */
	
	public String getAdditionalInfo2() {
		return additionalInfo2;
	}
	
	/**
	 * Retrieve the third temporary value
	 * @return
	 */
	
	public String getAdditionalInfo3() {
		return additionalInfo3;
	}
	
	/**
	 * Return the value if it can expire
	 * @return
	 */
	
	public boolean getExpire() {
		return expire;
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
}
