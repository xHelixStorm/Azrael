package constructors;

public class Level {
	private int level;
	private int experience;
	private int currency;
	private int rank_icon;
	private int fail_rate;
	private int exp_loss;
	
	public Level(int _level, int _experience, int _currency, int _rank_icon, int _fail_rate, int _exp_loss) {
		this.level = _level;
		this.experience = _experience;
		this.currency = _currency;
		this.rank_icon = _rank_icon;
		this.fail_rate = _fail_rate;
		this.exp_loss = _exp_loss;
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
	public int getFailRate() {
		return this.fail_rate;
	}
	public int getExpLoss() {
		return this.exp_loss;
	}
}
