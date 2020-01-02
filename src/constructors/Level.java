package constructors;

public class Level {
	private int level;
	private int experience;
	private int currency;
	private int rank_icon;
	
	public Level(int _level, int _experience, int _currency, int _rank_icon) {
		this.level = _level;
		this.experience = _experience;
		this.currency = _currency;
		this.rank_icon = _rank_icon;
	}
	
	public int getLevel() {
		return this.level;
	}
	public int getExperience() {
		return this.experience;
	}
	public int getCurrency() {
		return this.currency;
	}
	public int getRankIcon() {
		return this.rank_icon;
	}
}
