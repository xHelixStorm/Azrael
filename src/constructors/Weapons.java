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
	private String fullDescription;
	private String thumbnail;
	private boolean skill;
	private boolean close_range;
	private boolean long_range;
	private int magazine;
	private int ammunition;
	
	public Weapons(int _weapon_id, String _description, long _price, String _weapon_abbv, int _skin, int _stat, String _stat_description, int _category_id, String _category_description, String _overlay_name, boolean _enabled, String _fullDescription, String _thumbnail, boolean _skill, boolean _close_range, boolean _long_range, int _magazine, int _ammunition) {
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
		this.fullDescription = _fullDescription;
		this.thumbnail = _thumbnail;
		this.skill = _skill;
		this.close_range = _close_range;
		this.long_range = _close_range;
		this.magazine = _magazine;
		this.ammunition = _ammunition;
	}
	
	public int getWeaponID() {
		return this.weapon_id;
	}
	public String getDescription() {
		return this.description;
	}
	public long getPrice() {
		return this.price;
	}
	public String getWeaponAbbv() {
		return this.weapon_abbv;
	}
	public int getSkin() {
		return this.skin;
	}
	public int getStat() {
		return this.stat;
	}
	public String getStatDescription() {
		return this.stat_description;
	}
	public int getCategoryID() {
		return this.category_id;
	}
	public String getCategoryDescription() {
		return this.category_description;
	}
	public String getOverlayName() {
		return this.overlay_name;
	}
	public boolean getEnabled() {
		return this.enabled;
	}
	public String getFullDescription() {
		return this.fullDescription;
	}
	public String getThumbnail() {
		return this.thumbnail;
	}
	public boolean getSkill() {
		return this.skill;
	}
	public boolean getCloseRange() {
		return this.close_range;
	}
	public boolean getLongRange() {
		return this.long_range;
	}
	public int getMagazine() {
		return this.magazine;
	}
	public int getAmmunition() {
		return this.ammunition;
	}
}
