package constructors;

public class Cache {
	private long expiration;
	private String additionalInfo;
	private String additionalInfo2;
	private String additionalInfo3;
	private boolean expire;
	
	public Cache(long _expiration) {
		this.expiration = System.currentTimeMillis() + _expiration;
		this.additionalInfo = "";
		this.additionalInfo2 = "";
		this.additionalInfo3 = "";
		this.expire = true;
	}
	
	public Cache(long _expiration, String _additionalInfo) {
		this.expiration = System.currentTimeMillis() + _expiration;
		this.additionalInfo = _additionalInfo;
		this.additionalInfo2 = "";
		this.additionalInfo3 = "";
		this.expire = true;
	}
	
	public Cache(long _expiration, String _additionalInfo, String _additionalInfo2) {
		this.expiration = System.currentTimeMillis() + _expiration;
		this.additionalInfo = _additionalInfo;
		this.additionalInfo2 = _additionalInfo2;
		this.additionalInfo3 = "";
		this.expire = true;
	}
	
	public Cache(String _additionalInfo) {
		this.expiration = System.currentTimeMillis();
		this.additionalInfo = _additionalInfo;
		this.additionalInfo2 = "";
		this.additionalInfo3 = "";
		this.expire = false;
	}
	
	public Cache(String _additionalInfo, String _additionalInfo2) {
		this.expiration = System.currentTimeMillis();
		this.additionalInfo = _additionalInfo;
		this.additionalInfo2 = _additionalInfo2;
		this.additionalInfo3 = "";
		this.expire = false;
	}
	
	public Cache(String _additionalInfo, String _additionalInfo2, String _additionalInfo3) {
		this.expiration = System.currentTimeMillis();
		this.additionalInfo = _additionalInfo;
		this.additionalInfo2 = _additionalInfo2;
		this.additionalInfo3 = _additionalInfo3;
		this.expire = false;
	}
	
	public long getExpiration() {
		return expiration;
	}
	public String getAdditionalInfo() {
		return additionalInfo;
	}
	public String getAdditionalInfo2() {
		return additionalInfo2;
	}
	public String getAdditionalInfo3() {
		return additionalInfo3;
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
	public Cache updateDescription2(String description) {
		this.additionalInfo2 = description;
		return this;
	}
	public Cache updateDescription3(String description) {
		this.additionalInfo3 = description;
		return this;
	}
}
