package constructors;

import java.sql.Timestamp;

public class Clan {
	private int clan_id;
	private String name;
	private int members;
	private int matches;
	private int wins;
	private int losses;
	private String clan_mark;
	private Timestamp creation_date;
	
	public Clan(int _clan_id, String _name, int _members, int _matches, int _wins, int _losses, String _clan_mark, Timestamp _creation_date) {
		this.clan_id = _clan_id;
		this.name = _name;
		this.members = _members;
		this.matches = _matches;
		this.wins = _wins;
		this.losses = _losses;
		this.clan_mark = _clan_mark;
		this.creation_date = _creation_date;
	}
	
	public int getClanID() {
		return this.clan_id;
	}
	public String getName() {
		return this.name;
	}
	public int getMembers() {
		return this.members;
	}
	public int getMatches() {
		return this.matches;
	}
	public int getWins() {
		return this.wins;
	}
	public int getLosses() {
		return this.losses;
	}
	public String getClanMark() {
		return this.clan_mark;
	}
	public Timestamp getCreationDate() {
		return this.creation_date;
	}
}
