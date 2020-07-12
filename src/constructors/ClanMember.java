package constructors;

import java.sql.Timestamp;

public class ClanMember {
	private long user_id;
	private long guild_id;
	private int clan_id;
	private String clan_name;
	private int members;
	private int matches;
	private int wins;
	private int losses;
	private String clan_mark;
	private String username;
	private int member_level;
	private int elo;
	private Timestamp join_date;
	private Timestamp creation_date;
	
	public ClanMember() {
		//empty constructor
	}
	
	public ClanMember(long _user_id, long _guild_id, int _clan_id, String _clan_name, int _members, int _matches, int _wins, int _losses, String _clan_mark, String _username, int _member_level, int _elo, Timestamp _join_date, Timestamp _creation_date) {
		this.user_id = _user_id;
		this.guild_id = _guild_id;
		this.clan_id = _clan_id;
		this.clan_name = _clan_name;
		this.members = _members;
		this.matches = _matches;
		this.wins = _wins;
		this.losses = _losses;
		this.clan_mark = _clan_mark;
		this.username = _username;
		this.member_level = _member_level;
		this.elo = _elo;
		this.join_date = _join_date;
		this.creation_date = _creation_date;
	}
	
	public long getUserID() {
		return this.user_id;
	}
	public long getGuildID() {
		return this.guild_id;
	}
	public int getClanID() {
		return this.clan_id;
	}
	public String getClanName() {
		return this.clan_name;
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
	public String getUsername() {
		return this.username;
	}
	public int getMemberLevel() {
		return this.member_level;
	}
	public int getElo() {
		return this.elo;
	}
	public Timestamp getJoinDate() {
		return this.join_date;
	}
	public Timestamp getCreationDate() {
		return this.creation_date;
	}
}
