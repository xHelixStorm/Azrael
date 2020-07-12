package constructors;

import java.sql.Timestamp;

public class ClanReservation {
	private long guild_id;
	private long user_id;
	private int clan_id;
	private int type;
	private boolean done;
	private int action;
	private long channel_id;
	private Timestamp timestamp;
	
	public ClanReservation() {
		//empty constructor
	}
	
	public ClanReservation(long _guild_id, long _user_id, int _clan_id, int _type, boolean _done, int _action, long _channel_id, Timestamp _timestamp) {
		this.guild_id = _guild_id;
		this.user_id = _user_id;
		this.clan_id = _clan_id;
		this.type = _type;
		this.done = _done;
		this.action = _action;
		this.channel_id = _channel_id;
		this.timestamp = _timestamp;
	}
	
	public long getGuildID() {
		return this.guild_id;
	}
	public long getUserID() {
		return this.user_id;
	}
	public int getClanID() {
		return this.clan_id;
	}
	public int getType() {
		return this.type;
	}
	public boolean isDone() {
		return this.done;
	}
	public int getAction() {
		return this.action;
	}
	public long getChannelID() {
		return this.channel_id;
	}
	public Timestamp getTimestamp() {
		return this.timestamp;
	}
}
