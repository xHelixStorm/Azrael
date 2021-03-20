package de.azrael.constructors;

public class Weapons {
	private int weapon_id;
	private String description;
	private long price;
	private String weapon_abbv;
	private int skin;
	private int stat;
	private String stat_description;
	private int percent_damage;
	private int category_id;
	private String category_description;
	private int overlay_level;
	private String overlay_name;
	private boolean enabled;
	private String fullDescription;
	private String thumbnail;
	private boolean skill;
	private boolean close_range;
	private boolean long_range;
	private int magazine;
	private int base_damage;
	
	private String attack1_name;
	private int attack1_damage_plus;
	private int attack1_damage_drop;
	private int attack1_damage_drop_distance;
	private int attack1_hit_chance_close;
	private int attack1_hit_chance_medium;
	private int attack1_hit_chance_distant;
	private int attack1_sp_consumption;
	private int attack1_ammo_usage;
	private String attack1_description;
	
	private String attack2_name;
	private int attack2_damage_plus;
	private int attack2_damage_drop;
	private int attack2_damage_drop_distance;
	private int attack2_hit_chance_close;
	private int attack2_hit_chance_medium;
	private int attack2_hit_chance_distant;
	private int attack2_sp_consumption;
	private int attack2_ammo_usage;
	private String attack2_description;
	
	private String attack3_name;
	private int attack3_damage_plus;
	private int attack3_damage_drop;
	private int attack3_damage_drop_distance;
	private int attack3_hit_chance_close;
	private int attack3_hit_chance_medium;
	private int attack3_hit_chance_distant;
	private int attack3_sp_consumption;
	private int attack3_ammo_usage;
	private String attack3_description;
	
	private String attack4_name;
	private int attack4_damage_plus;
	private int attack4_damage_drop;
	private int attack4_damage_drop_distance;
	private int attack4_hit_chance_close;
	private int attack4_hit_chance_medium;
	private int attack4_hit_chance_distant;
	private int attack4_sp_consumption;
	private int attack4_ammo_usage;
	private String attack4_description;
	
	private String special1_name;
	private int special1_damage_plus;
	private int special1_damage_drop;
	private int special1_damage_drop_distance;
	private int special1_hit_chance_close;
	private int special1_hit_chance_medium;
	private int special1_hit_chance_distant;
	private int special1_sp_consumption;
	private int special1_ammo_usage;
	private String special1_description;
	
	private String special2_name;
	private int special2_damage_plus;
	private int special2_damage_drop;
	private int special2_damage_drop_distance;
	private int special2_hit_chance_close;
	private int special2_hit_chance_medium;
	private int special2_hit_chance_distant;
	private int special2_sp_consumption;
	private int special2_ammo_usage;
	private String special2_description;
	
	private String special3_name;
	private int special3_damage_plus;
	private int special3_damage_drop;
	private int special3_damage_drop_distance;
	private int special3_hit_chance_close;
	private int special3_hit_chance_medium;
	private int special3_hit_chance_distant;
	private int special3_sp_consumption;
	private int special3_ammo_usage;
	private String special3_description;
	
	private String special4_name;
	private int special4_damage_plus;
	private int special4_damage_drop;
	private int special4_damage_drop_distance;
	private int special4_hit_chance_close;
	private int special4_hit_chance_medium;
	private int special4_hit_chance_distant;
	private int special4_sp_consumption;
	private int special4_ammo_usage;
	private String special4_description;
	
	private String attack_desc1;
	private String attack_desc2;
	private String attack_desc3;
	
	public Weapons(int _weapon_id, String _description, long _price, String _weapon_abbv, int _skin, int _stat, String _stat_description, int _percent_damage, int _category_id, String _category_description, int _overlay_level, String _overlay_name, boolean _enabled, String _fullDescription, String _thumbnail, boolean _skill, boolean _close_range, boolean _long_range, int _magazine, int _base_damage,
				   String _attack1_name, int _attack1_damage_plus, int _attack1_damage_drop, int _attack1_damage_drop_distance, int _attack1_hit_chance_close, int _attack1_hit_chance_medium, int _attack1_hit_chance_distant, int _attack1_sp_consumption, int _attack1_ammo_usage, String _attack1_description,
				   String _attack2_name, int _attack2_damage_plus, int _attack2_damage_drop, int _attack2_damage_drop_distance, int _attack2_hit_chance_close, int _attack2_hit_chance_medium, int _attack2_hit_chance_distant, int _attack2_sp_consumption, int _attack2_ammo_usage, String _attack2_description,
				   String _attack3_name, int _attack3_damage_plus, int _attack3_damage_drop, int _attack3_damage_drop_distance, int _attack3_hit_chance_close, int _attack3_hit_chance_medium, int _attack3_hit_chance_distant, int _attack3_sp_consumption, int _attack3_ammo_usage, String _attack3_description,
				   String _attack4_name, int _attack4_damage_plus, int _attack4_damage_drop, int _attack4_damage_drop_distance, int _attack4_hit_chance_close, int _attack4_hit_chance_medium, int _attack4_hit_chance_distant, int _attack4_sp_consumption, int _attack4_ammo_usage, String _attack4_description,
				   String _special1_name, int _special1_damage_plus, int _special1_damage_drop, int _special1_damage_drop_distance, int _special1_hit_chance_close, int _special1_hit_chance_medium, int _special1_hit_chance_distant, int _special1_sp_consumption, int _special1_ammo_usage, String _special1_description,
				   String _special2_name, int _special2_damage_plus, int _special2_damage_drop, int _special2_damage_drop_distance, int _special2_hit_chance_close, int _special2_hit_chance_medium, int _special2_hit_chance_distant, int _special2_sp_consumption, int _special2_ammo_usage, String _special2_description,
				   String _special3_name, int _special3_damage_plus, int _special3_damage_drop, int _special3_damage_drop_distance, int _special3_hit_chance_close, int _special3_hit_chance_medium, int _special3_hit_chance_distant, int _special3_sp_consumption, int _special3_ammo_usage, String _special3_description,
				   String _special4_name, int _special4_damage_plus, int _special4_damage_drop, int _special4_damage_drop_distance, int _special4_hit_chance_close, int _special4_hit_chance_medium, int _special4_hit_chance_distant, int _special4_sp_consumption, int _special4_ammo_usage, String _special4_description,
				   String _attack_desc1, String _attack_desc2, String _attack_desc3) {
		
		this.weapon_id = _weapon_id;
		this.description = _description;
		this.price = _price;
		this.weapon_abbv = _weapon_abbv;
		this.skin = _skin;
		this.stat = _stat;
		this.stat_description = _stat_description;
		this.percent_damage = _percent_damage;
		this.category_id = _category_id;
		this.category_description = _category_description;
		this.overlay_level = _overlay_level;
		this.overlay_name = _overlay_name;
		this.enabled = _enabled;
		this.fullDescription = _fullDescription;
		this.thumbnail = _thumbnail;
		this.skill = _skill;
		this.close_range = _close_range;
		this.long_range = _close_range;
		this.magazine = _magazine;
		this.base_damage = _base_damage;
		
		this.attack1_name = _attack1_name;
		this.attack1_damage_plus = _attack1_damage_plus;
		this.attack1_damage_drop = _attack1_damage_drop;
		this.attack1_damage_drop_distance = _attack1_damage_drop_distance;
		this.attack1_hit_chance_close = _attack1_hit_chance_close;
		this.attack1_hit_chance_medium = _attack1_hit_chance_medium;
		this.attack1_hit_chance_distant = _attack1_hit_chance_distant;
		this.attack1_sp_consumption = _attack1_sp_consumption;
		this.attack1_ammo_usage = _attack1_ammo_usage;
		this.attack1_description = _attack1_description;
		
		this.attack2_name = _attack2_name;
		this.attack2_damage_plus = _attack2_damage_plus;
		this.attack2_damage_drop = _attack2_damage_drop;
		this.attack2_damage_drop_distance = _attack2_damage_drop_distance;
		this.attack2_hit_chance_close = _attack2_hit_chance_close;
		this.attack2_hit_chance_medium = _attack2_hit_chance_medium;
		this.attack2_hit_chance_distant = _attack2_hit_chance_distant;
		this.attack2_sp_consumption = _attack2_sp_consumption;
		this.attack2_ammo_usage = _attack2_ammo_usage;
		this.attack2_description = _attack2_description;
		
		this.attack3_name = _attack3_name;
		this.attack3_damage_plus = _attack3_damage_plus;
		this.attack3_damage_drop = _attack3_damage_drop;
		this.attack3_damage_drop_distance = _attack3_damage_drop_distance;
		this.attack3_hit_chance_close = _attack3_hit_chance_close;
		this.attack3_hit_chance_medium = _attack3_hit_chance_medium;
		this.attack3_hit_chance_distant = _attack3_hit_chance_distant;
		this.attack3_sp_consumption = _attack3_sp_consumption;
		this.attack3_ammo_usage = _attack3_ammo_usage;
		this.attack3_description = _attack3_description;
		
		this.attack4_name = _attack4_name;
		this.attack4_damage_plus = _attack4_damage_plus;
		this.attack4_damage_drop = _attack4_damage_drop;
		this.attack4_damage_drop_distance = _attack4_damage_drop_distance;
		this.attack4_hit_chance_close = _attack4_hit_chance_close;
		this.attack4_hit_chance_medium = _attack4_hit_chance_medium;
		this.attack4_hit_chance_distant = _attack4_hit_chance_distant;
		this.attack4_sp_consumption = _attack4_sp_consumption;
		this.attack4_ammo_usage = _attack4_ammo_usage;
		this.attack4_description = _attack4_description;
		
		this.special1_name = _special1_name;
		this.special1_damage_plus = _special1_damage_plus;
		this.special1_damage_drop = _special1_damage_drop;
		this.special1_damage_drop_distance = _special1_damage_drop_distance;
		this.special1_hit_chance_close = _special1_hit_chance_close;
		this.special1_hit_chance_medium = _special1_hit_chance_medium;
		this.special1_hit_chance_distant = _special1_hit_chance_distant;
		this.special1_sp_consumption = _special1_sp_consumption;
		this.special1_ammo_usage = _special1_ammo_usage;
		this.special1_description = _special1_description;
		
		this.special2_name = _special2_name;
		this.special2_damage_plus = _special2_damage_plus;
		this.special2_damage_drop = _special2_damage_drop;
		this.special2_damage_drop_distance = _special2_damage_drop_distance;
		this.special2_hit_chance_close = _special2_hit_chance_close;
		this.special2_hit_chance_medium = _special2_hit_chance_medium;
		this.special2_hit_chance_distant = _special2_hit_chance_distant;
		this.special2_sp_consumption = _special2_sp_consumption;
		this.special2_ammo_usage = _special2_ammo_usage;
		this.special2_description = _special2_description;
		
		this.special3_name = _special3_name;
		this.special3_damage_plus = _special3_damage_plus;
		this.special3_damage_drop = _special3_damage_drop;
		this.special3_damage_drop_distance = _special3_damage_drop_distance;
		this.special3_hit_chance_close = _special3_hit_chance_close;
		this.special3_hit_chance_medium = _special3_hit_chance_medium;
		this.special3_hit_chance_distant = _special3_hit_chance_distant;
		this.special3_sp_consumption = _special3_sp_consumption;
		this.special3_ammo_usage = _special3_ammo_usage;
		this.special3_description = _special3_description;
		
		this.special4_name = _special4_name;
		this.special4_damage_plus = _special4_damage_plus;
		this.special4_damage_drop = _special4_damage_drop;
		this.special4_damage_drop_distance = _special4_damage_drop_distance;
		this.special4_hit_chance_close = _special4_hit_chance_close;
		this.special4_hit_chance_medium = _special4_hit_chance_medium;
		this.special4_hit_chance_distant = _special4_hit_chance_distant;
		this.special4_sp_consumption = _special4_sp_consumption;
		this.special4_ammo_usage = _special4_ammo_usage;
		this.special4_description = _special4_description;
		
		this.attack_desc1 = _attack_desc1;
		this.attack_desc2 = _attack_desc2;
		this.attack_desc3 = _attack_desc3;
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
	public int getPercentDamage() {
		return this.percent_damage;
	}
	public int getCategoryID() {
		return this.category_id;
	}
	public String getCategoryDescription() {
		return this.category_description;
	}
	public int getOverlayLevel() {
		return this.overlay_level;
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
	public int getBaseDamage() {
		return this.base_damage;
	}
	
	public String getAttack1Name() {
		return this.attack1_name;
	}
	public int getAttack1DamagePlus() {
		return this.attack1_damage_plus;
	}
	public int getAttack1DamageDrop() {
		return this.attack1_damage_drop;
	}
	public int getAttack1DamageDropDistance() {
		return this.attack1_damage_drop_distance;
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
	public int getAttack2DamageDrop() {
		return this.attack2_damage_drop;
	}
	public int getAttack2DamageDropDistance() {
		return this.attack2_damage_drop_distance;
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
	public int getAttack3DamageDrop() {
		return this.attack3_damage_drop;
	}
	public int getAttack3DamageDropDistance() {
		return this.attack3_damage_drop_distance;
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
	public int getAttack4DamageDrop() {
		return this.attack4_damage_drop;
	}
	public int getAttack4DamageDropDistance() {
		return this.attack4_damage_drop_distance;
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
	
	public String getSpecial1Name() {
		return this.special1_name;
	}
	public int getSpecial1DamagePlus() {
		return this.special1_damage_plus;
	}
	public int getSpecial1DamageDrop() {
		return this.special1_damage_drop;
	}
	public int getSpecial1DamageDropDistance() {
		return this.special1_damage_drop_distance;
	}
	public int getSpecial1HitChanceClose() {
		return this.special1_hit_chance_close;
	}
	public int getSpecial1HitChanceMedium() {
		return this.special1_hit_chance_medium;
	}
	public int getSpecial1HitChanceDistant() {
		return this.special1_hit_chance_distant;
	}
	public int getSpecial1SPConsumption() {
		return this.special1_sp_consumption;
	}
	public int getSpecial1AmmoUsage() {
		return this.special1_ammo_usage;
	}
	public String getSpecial1Description() {
		return this.special1_description;
	}
	
	public String getSpecial2Name() {
		return this.special2_name;
	}
	public int getSpecial2DamagePlus() {
		return this.special2_damage_plus;
	}
	public int getSpecial2DamageDrop() {
		return this.special2_damage_drop;
	}
	public int getSpecial2DamageDropDistance() {
		return this.special2_damage_drop_distance;
	}
	public int getSpecial2HitChanceClose() {
		return this.special2_hit_chance_close;
	}
	public int getSpecial2HitChanceMedium() {
		return this.special2_hit_chance_medium;
	}
	public int getSpecial2HitChanceDistant() {
		return this.special2_hit_chance_distant;
	}
	public int getSpecial2SPConsumption() {
		return this.special2_sp_consumption;
	}
	public int getSpecial2AmmoUsage() {
		return this.special2_ammo_usage;
	}
	public String getSpecial2Description() {
		return this.special2_description;
	}
	
	public String getSpecial3Name() {
		return this.special3_name;
	}
	public int getSpecial3DamagePlus() {
		return this.special3_damage_plus;
	}
	public int getSpecial3DamageDrop() {
		return this.special3_damage_drop;
	}
	public int getSpecial3DamageDropDistance() {
		return this.special3_damage_drop_distance;
	}
	public int getSpecial3HitChanceClose() {
		return this.special3_hit_chance_close;
	}
	public int getSpecial3HitChanceMedium() {
		return this.special3_hit_chance_medium;
	}
	public int getSpecial3HitChanceDistant() {
		return this.special3_hit_chance_distant;
	}
	public int getSpecial3SPConsumption() {
		return this.special3_sp_consumption;
	}
	public int getSpecial3AmmoUsage() {
		return this.special3_ammo_usage;
	}
	public String getSpecial3Description() {
		return this.special3_description;
	}
	
	public String getSpecial4Name() {
		return this.special4_name;
	}
	public int getSpecial4DamagePlus() {
		return this.special4_damage_plus;
	}
	public int getSpecial4DamageDrop() {
		return this.special4_damage_drop;
	}
	public int getSpecial4DamageDropDistance() {
		return this.special4_damage_drop_distance;
	}
	public int getSpecial4HitChanceClose() {
		return this.special4_hit_chance_close;
	}
	public int getSpecial4HitChanceMedium() {
		return this.special4_hit_chance_medium;
	}
	public int getSpecial4HitChanceDistant() {
		return this.special4_hit_chance_distant;
	}
	public int getSpecial4SPConsumption() {
		return this.special4_sp_consumption;
	}
	public int getSpecial4AmmoUsage() {
		return this.special4_ammo_usage;
	}
	public String getSpecial4Description() {
		return this.special4_description;
	}
	
	public String getAttackDesc1() {
		return this.attack_desc1;
	}
	public String getAttackDesc2() {
		return this.attack_desc2;
	}
	public String getAttackDesc3() {
		return this.attack_desc3;
	}
}
