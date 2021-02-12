package constructors;

public class Member {
	private long user_id;
	private int room_id;
	private String username;
	private String server;
	private int elo;
	private int team;
	private boolean leader;
	private boolean picker;
	private boolean master;
	
	public Member(long _user_id, int _room_id, String _username, String _server, int _elo, int _team, boolean _leader, boolean _picker, boolean _master) {
		this.user_id = _user_id;
		this.room_id = _room_id;
		this.username = _username;
		this.server = _server;
		this.elo = _elo;
		this.team = _team;
		this.leader = _leader;
		this.picker = _picker;
		this.master = _master;
	}
	
	public long getUserID() {
		return this.user_id;
	}
	public int getRoomID() {
		return this.room_id;
	}
	public String getUsername() {
		return this.username;
	}
	public String getServer() {
		return this.server;
	}
	public int getElo() {
		return this.elo;
	}
	public int getTeam() {
		return this.team;
	}
	public boolean isLeader() {
		return this.leader;
	}
	public boolean isPicker() {
		return this.picker;
	}
	public boolean isMaster() {
		return this.master;
	}
}
