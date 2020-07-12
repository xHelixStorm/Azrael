package constructors;

import java.sql.Timestamp;

public class Room {
	private int room_id;
	private int type;
	private int members;
	private int status;
	private int winner;
	private int clan_id_1;
	private int clan_id_2;
	private int map_id;
	private Timestamp created;
	private Timestamp last_joined;
	private long channel_id;
	private long message_id;
	
	public Room() {
		//empty constructor
	}
	
	public Room(int _room_id, int _type, int _members, int _status, int _winner, int _clan_id_1, int _clan_id_2, int _map_id, Timestamp _created, Timestamp _last_joined, long _channel_id, long _message_id) {
		this.room_id = _room_id;
		this.type = _type;
		this.members = _members;
		this.status = _status;
		this.winner = _winner;
		this.map_id = _map_id;
		this.clan_id_1 = _clan_id_1;
		this.clan_id_2 = _clan_id_2;
		this.created = _created;
		this.last_joined = _last_joined;
		this.channel_id = _channel_id;
		this.message_id = _message_id;
	}
	
	public int getRoomID() {
		return this.room_id;
	}
	public int getType() {
		return this.type;
	}
	public int getMembers() {
		return this.members;
	}
	public int getStatus() {
		return this.status;
	}
	public int getWinner() {
		return this.winner;
	}
	public int getClanID1() {
		return this.clan_id_1;
	}
	public int getClanID2() {
		return this.clan_id_2;
	}
	public int getMapID() {
		return this.map_id;
	}
	public Timestamp getCreated() {
		return this.created;
	}
	public Timestamp getLastJoined() {
		return this.last_joined;
	}
	public long getChannelID() {
		return this.channel_id;
	}
	public long getMessageID() {
		return this.message_id;
	}
	
	public void setStatus(int _status) {
		this.status = _status;
	}
}
