package core;

public class Cache {
	private long expiration;
	private String additionalInfo;
	
	public Cache(long _expiration) {
		this.expiration = System.currentTimeMillis() + _expiration;
		this.additionalInfo = "";
	}
	
	public Cache(long _expiration, String _additionalInfo) {
		this.expiration = System.currentTimeMillis() + _expiration;
		this.additionalInfo = _additionalInfo;
	}
	public long getExpiration() {
		return expiration;
	}
	public String getAdditionalInfo() {
		return additionalInfo;
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
