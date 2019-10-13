package constructors;

public class Level {
	private int level;
	private int experience;
	private int currency;
	
	public Level(int _level, int _experience, int _currency) {
		this.level = _level;
		this.experience = _experience;
		this.currency = _currency;
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
}
