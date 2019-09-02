package constructors;

public class Watchlist {
	private int level;
	private boolean useWatchChannel;
	
	public Watchlist(int _level, boolean _useWatchChannel) {
		this.level = _level;
		this.useWatchChannel = _useWatchChannel;
	}
	
	public int getLevel() {
		return this.level;
	}
	public boolean getUseWatchChannel() {
		return this.useWatchChannel;
	}
}
