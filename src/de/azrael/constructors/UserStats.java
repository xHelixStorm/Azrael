package de.azrael.constructors;

public class UserStats {
	private long user_id;
	private String name;
	private int elo;
	private int games;
	private int wins;
	private int losses;
	private String server;
	private int clan_id;
	
	public UserStats() {
		//empty constructor
	}
	
	public UserStats(long _user_id, String _name, int _elo, int _games, int _wins, int _losses, String _server, int _clan_id) {
		this.user_id = _user_id;
		this.name = _name;
		this.elo = _elo;
		this.games = _games;
		this.wins = _wins;
		this.losses = _losses;
		this.server = _server;
		this.clan_id = _clan_id;
	}
	
	public long getUserID() {
		return this.user_id;
	}
	public String getName() {
		return this.name;
	}
	public int getElo() {
		return this.elo;
	}
	public int getGames() {
		return this.games;
	}
	public int getWins() {
		return this.wins;
	}
	public int getLosses() {
		return this.losses;
	}
	public String getServer() {
		return this.server;
	}
	public int getClanID() {
		return this.clan_id;
	}
}
