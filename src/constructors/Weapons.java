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
	private int base_damage;
	
	private String attack1_name;
	private int attack1_damage_plus;
	private int attack1_damage_plus_percent;
	private int attack1_damage_drop;
	private int attack1_damage_drop_percent;
	private int attack1_hit_chance_close;
	private int attack1_hit_chance_medium;
	private int attack1_hit_chance_distant;
	private int attack1_sp_consumption;
	private int attack1_ammo_usage;
	private String attack1_description;
	
	private String attack2_name;
	private int attack2_damage_plus;
	private int attack2_damage_plus_percent;
	private int attack2_damage_drop;
	private int attack2_damage_drop_percent;
	private int attack2_hit_chance_close;
	private int attack2_hit_chance_medium;
	private int attack2_hit_chance_distant;
	private int attack2_sp_consumption;
	private int attack2_ammo_usage;
	private String attack2_description;
	
	private String attack3_name;
	private int attack3_damage_plus;
	private int attack3_damage_plus_percent;
	private int attack3_damage_drop;
	private int attack3_damage_drop_percent;
	private int attack3_hit_chance_close;
	private int attack3_hit_chance_medium;
	private int attack3_hit_chance_distant;
	private int attack3_sp_consumption;
	private int attack3_ammo_usage;
	private String attack3_description;
	
	private String attack4_name;
	private int attack4_damage_plus;
	private int attack4_damage_plus_percent;
	private int attack4_damage_drop;
	private int attack4_damage_drop_percent;
	private int attack4_hit_chance_close;
	private int attack4_hit_chance_medium;
	private int attack4_hit_chance_distant;
	private int attack4_sp_consumption;
	private int attack4_ammo_usage;
	private String attack4_description;
	
	public Weapons(int _weapon_id, String _description, long _price, String _weapon_abbv, int _skin, int _stat, String _stat_description, int _category_id, String _category_description, String _overlay_name, boolean _enabled, String _fullDescription, String _thumbnail, boolean _skill, boolean _close_range, boolean _long_range, int _magazine, int _ammunition, int _base_damage,
				   String _attack1_name, int _attack1_damage_plus, int _attack1_damage_plus_percent, int _attack1_damage_drop, int _attack1_damage_drop_percent, int _attack1_hit_chance_close, int _attack1_hit_chance_medium, int _attack1_hit_chance_distant, int _attack1_sp_consumption, int _attack1_ammo_usage, String _attack1_description,
				   String _attack2_name, int _attack2_damage_plus, int _attack2_damage_plus_percent, int _attack2_damage_drop, int _attack2_damage_drop_percent, int _attack2_hit_chance_close, int _attack2_hit_chance_medium, int _attack2_hit_chance_distant, int _attack2_sp_consumption, int _attack2_ammo_usage, String _attack2_description,
				   String _attack3_name, int _attack3_damage_plus, int _attack3_damage_plus_percent, int _attack3_damage_drop, int _attack3_damage_drop_percent, int _attack3_hit_chance_close, int _attack3_hit_chance_medium, int _attack3_hit_chance_distant, int _attack3_sp_consumption, int _attack3_ammo_usage, String _attack3_description,
				   String _attack4_name, int _attack4_damage_plus, int _attack4_damage_plus_percent, int _attack4_damage_drop, int _attack4_damage_drop_percent, int _attack4_hit_chance_close, int _attack4_hit_chance_medium, int _attack4_hit_chance_distant, int _attack4_sp_consumption, int _attack4_ammo_usage, String _attack4_description) {
		
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
		this.base_damage = _base_damage;
		
		this.attack1_name = _attack1_name;
		this.attack1_damage_plus = _attack1_damage_plus;
		this.attack1_damage_plus_percent = _attack1_damage_plus_percent;
		this.attack1_damage_drop = _attack1_damage_drop;
		this.attack1_damage_drop_percent = _attack1_damage_drop_percent;
		this.attack1_hit_chance_close = _attack1_hit_chance_close;
		this.attack1_hit_chance_medium = _attack1_hit_chance_medium;
		this.attack1_hit_chance_distant = _attack1_hit_chance_distant;
		this.attack1_sp_consumption = _attack1_sp_consumption;
		this.attack1_ammo_usage = _attack1_ammo_usage;
		this.attack1_description = _attack1_description;
		
		this.attack2_name = _attack2_name;
		this.attack2_damage_plus = _attack2_damage_plus;
		this.attack2_damage_plus_percent = _attack2_damage_plus_percent;
		this.attack2_damage_drop = _attack2_damage_drop;
		this.attack2_damage_drop_percent = _attack2_damage_drop_percent;
		this.attack2_hit_chance_close = _attack2_hit_chance_close;
		this.attack2_hit_chance_medium = _attack2_hit_chance_medium;
		this.attack2_hit_chance_distant = _attack2_hit_chance_distant;
		this.attack2_sp_consumption = _attack2_sp_consumption;
		this.attack2_ammo_usage = _attack2_ammo_usage;
		this.attack2_description = _attack2_description;
		
		this.attack3_name = _attack3_name;
		this.attack3_damage_plus = _attack3_damage_plus;
		this.attack3_damage_plus_percent = _attack3_damage_plus_percent;
		this.attack3_damage_drop = _attack3_damage_drop;
		this.attack3_damage_drop_percent = _attack3_damage_drop_percent;
		this.attack3_hit_chance_close = _attack3_hit_chance_close;
		this.attack3_hit_chance_medium = _attack3_hit_chance_medium;
		this.attack3_hit_chance_distant = _attack3_hit_chance_distant;
		this.attack3_sp_consumption = _attack3_sp_consumption;
		this.attack3_ammo_usage = _attack3_ammo_usage;
		this.attack3_description = _attack3_description;
		
		this.attack4_name = _attack4_name;
		this.attack4_damage_plus = _attack4_damage_plus;
		this.attack4_damage_plus_percent = _attack4_damage_plus_percent;
		this.attack4_damage_drop = _attack4_damage_drop;
		this.attack4_damage_drop_percent = _attack4_damage_drop_percent;
		this.attack4_hit_chance_close = _attack4_hit_chance_close;
		this.attack4_hit_chance_medium = _attack4_hit_chance_medium;
		this.attack4_hit_chance_distant = _attack4_hit_chance_distant;
		this.attack4_sp_consumption = _attack4_sp_consumption;
		this.attack4_ammo_usage = _attack4_ammo_usage;
		this.attack4_description = _attack4_description;
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
	public int getBaseDamage() {
		return this.base_damage;
	}
	
	public String getAttack1Name() {
		return this.attack1_name;
	}
	public int getAttack1DamagePlus() {
		return this.attack1_damage_plus;
	}
	public int getAttack1DamagePlusPercent() {
		return this.attack1_damage_plus_percent;
	}
	public int getAttack1DamageDrop() {
		return this.attack1_damage_drop;
	}
	public int getAttack1DamageDropPercent() {
		return this.attack1_damage_drop_percent;
	}
	public int getAttack1HitChanceClose() {
		return this.attack1_hit_chance_close;
	}
	public int getAttack1HitChanceMedium() {
		return this.attack1_hit_chance_medium;
	}
	public int getAttack1HitChanceDistant() {
		return this.attack1_hit_chance_distant;
	}
	public int getAttack1SPConsumption() {
		return this.attack1_sp_consumption;
	}
	public int getAttack1AmmoUsage() {
		return this.attack1_ammo_usage;
	}
	public String getAttack1Description() {
		return this.attack1_description;
	}
	
	public String getAttack2Name() {
		return this.attack2_name;
	}
	public int getAttack2DamagePlus() {
		return this.attack2_damage_plus;
	}
	public int getAttack2DamagePlusPercent() {
		return this.attack2_damage_plus_percent;
	}
	public int getAttack2DamageDrop() {
		return this.attack2_damage_drop;
	}
	public int getAttack2DamageDropPercent() {
		return this.attack2_damage_drop_percent;
	}
	public int getAttack2HitChanceClose() {
		return this.attack2_hit_chance_close;
	}
	public int getAttack2HitChanceMedium() {
		return this.attack2_hit_chance_medium;
	}
	public int getAttack2HitChanceDistant() {
		return this.attack2_hit_chance_distant;
	}
	public int getAttack2SPConsumption() {
		return this.attack2_sp_consumption;
	}
	public int getAttack2AmmoUsage() {
		return this.attack2_ammo_usage;
	}
	public String getAttack2Description() {
		return this.attack2_description;
	}
	
	public String getAttack3Name() {
		return this.attack3_name;
	}
	public int getAttack3DamagePlus() {
		return this.attack3_damage_plus;
	}
	public int getAttack3DamagePlusPercent() {
		return this.attack3_damage_plus_percent;
	}
	public int getAttack3DamageDrop() {
		return this.attack3_damage_drop;
	}
	public int getAttack3DamageDropPercent() {
		return this.attack3_damage_drop_percent;
	}
	public int getAttack3HitChanceClose() {
		return this.attack3_hit_chance_close;
	}
	public int getAttack3HitChanceMedium() {
		return this.attack3_hit_chance_medium;
	}
	public int getAttack3HitChanceDistant() {
		return this.attack3_hit_chance_distant;
	}
	public int getAttack3SPConsumption() {
		return this.attack3_sp_consumption;
	}
	public int getAttack3AmmoUsage() {
		return this.attack3_ammo_usage;
	}
	public String getAttack3Description() {
		return this.attack3_description;
	}
	
	public String getAttack4Name() {
		return this.attack4_name;
	}
	public int getAttack4DamagePlus() {
		return this.attack4_damage_plus;
	}
	public int getAttack4DamagePlusPercent() {
		return this.attack4_damage_plus_percent;
	}
	public int getAttack4DamageDrop() {
		return this.attack4_damage_drop;
	}
	public int getAttack4DamageDropPercent() {
		return this.attack4_damage_drop_percent;
	}
	public int getAttack4HitChanceClose() {
		return this.attack4_hit_chance_close;
	}
	public int getAttack4HitChanceMedium() {
		return this.attack4_hit_chance_medium;
	}
	public int getAttack4HitChanceDistant() {
		return this.attack4_hit_chance_distant;
	}
	public int getAttack4SPConsumption() {
		return this.attack4_sp_consumption;
	}
	public int getAttack4AmmoUsage() {
		return this.attack4_ammo_usage;
	}
	public String getAttack4Description() {
		return this.attack4_description;
	}
}
