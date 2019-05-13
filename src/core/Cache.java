package core;

public class Cache {
	private long user;
	private long guild;
	private long channel;
	private long expiration;
	private String additionalInfo;
	
	public Cache(long _user, long _guild, long _channel, long _expiration) {
		this.user = _user;
		this.guild = _guild;
		this.channel = _channel;
		this.expiration = _expiration;
		this.additionalInfo = "";
	}
	
	public Cache(long _user, long _guild, long _channel, long _expiration, String _additionalInfo) {
		this.user = _user;
		this.guild = _guild;
		this.channel = _channel;
		this.expiration = _expiration;
		this.additionalInfo = _additionalInfo;
	}
	
	public long getUser() {
		return user;
	}
	public long getGuild() {
		return guild;
	}
	public long getChannel() {
		return channel;
	}
	public long getExpiration() {
		return expiration;
	}
	public String getAdditionalInfo() {
		return additionalInfo;
	}
}
