package constructors;

public class WeaponAbbvs {
	String abbv;
	String description;
	
	public WeaponAbbvs(String _abbv, String _description) {
		this.abbv = _abbv;
		this.description = _description;
	}
	
	public String getAbbv() {
		return abbv;
	}
	public String getDescription() {
		return description;
	}
}
