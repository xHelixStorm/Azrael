package constructors;

public class Weapons {
	private int weapon_id;
	private String description;
	private long price;
	private String weapon_abbv;
	private int skin;
	private int stat;
	private String stat_description;
	private int category_id;
	private String category_description;
	private String overlay_name;
	private boolean enabled;
	
	public Weapons(int _weapon_id, String _description, long _price, String _weapon_abbv, int _skin, int _stat, String _stat_description, int _category_id, String _category_description, String _overlay_name, boolean _enabled) {
		this.weapon_id = _weapon_id;
		this.description = _description;
		this.price = _price;
		this.weapon_abbv = _weapon_abbv;
		this.skin = _skin;
		this.stat = _stat;
		this.stat_description = _stat_description;
		this.category_id = _category_id;
		this.category_description = _category_description;
		this.overlay_name = _overlay_name;
		this.enabled = _enabled;
	}
	
	public int getWeaponID() {
		return weapon_id;
	}
	public String getDescription() {
		return description;
	}
	public long getPrice() {
		return price;
	}
	public String getWeaponAbbv() {
		return weapon_abbv;
	}
	public int getSkin() {
		return skin;
	}
	public int getStat() {
		return stat;
	}
	public String getStatDescription() {
		return stat_description;
	}
	public int getCategoryID() {
		return category_id;
	}
	public String getCategoryDescription() {
		return category_description;
	}
	public String getOverlayName() {
		return overlay_name;
	}
	public boolean getEnabled() {
		return enabled;
	}
}
