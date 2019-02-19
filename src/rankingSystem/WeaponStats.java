package rankingSystem;

public class WeaponStats {
	int id;
	String stat;
	
	public WeaponStats(int _id, String _stat) {
		this.id = _id;
		this.stat = _stat;
	}
	
	public int getID() {
		return id;
	}
	public String getStat() {
		return stat;
	}
}
