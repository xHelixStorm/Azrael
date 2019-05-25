package core;

public class Cache {
	private long expiration;
	private String additionalInfo;
	private boolean expire;
	
	public Cache(long _expiration) {
		this.expiration = System.currentTimeMillis() + _expiration;
		this.additionalInfo = "";
		this.expire = true;
	}
	
	public Cache(long _expiration, String _additionalInfo) {
		this.expiration = System.currentTimeMillis() + _expiration;
		this.additionalInfo = _additionalInfo;
		this.expire = true;
	}
	
	public Cache(String _additionalInfo) {
		this.expiration = System.currentTimeMillis();
		this.additionalInfo = _additionalInfo;
		this.expire = false;
	}
	
	public long getExpiration() {
		return expiration;
	}
	public String getAdditionalInfo() {
		return additionalInfo;
	}
	public boolean getExpire() {
		return expire;
	}
	
	public Cache setExpiration(long time) {
		this.expiration = System.currentTimeMillis() + time;
		return this;
	}
	public Cache updateDescription(String description) {
		this.additionalInfo = description;
		return this;
	}
}
