package constructors;

public class Watchlist {
	private int level;
	private long watchChannel;
	private boolean higherPrivileges;
	
	public Watchlist(int _level, long _watchChannel, boolean _higherPrivileges) {
		this.level = _level;
		this.watchChannel = _watchChannel;
		this.higherPrivileges = _higherPrivileges;
	}
	
	public int getLevel() {
		return this.level;
	}
	public long getWatchChannel() {
		return this.watchChannel;
	}
	public boolean hasHigherPrivileges() {
		return this.higherPrivileges;
	}
}
