package enums;

import java.util.HashMap;
import java.util.Map;

public enum WeaponEffect {
	HIT				(1, "HIT", "HIT"),
	EXPLOSION		(2, "EXPLOSION", "EXPLOSION"),
	HEAL			(3, "HEAL", "HEAL"),
	STUN			(4, "STUN", "STUN"),
	BLOW			(5, "BLOW", "BLOW"),
	THREE_SHOT		(6, "THREE_SHOT", "THREE SHOT"),
	SPRAY			(7, "SPRAY", "SPRAY"),
	CLOSE_SPRAY		(8, "CLOSE_SPRAY", "CLOSE SPRAY"),
	TWO_SHOT		(9, "TWO_SHOT", "TWO SHOT"),
	TEN_SHOT		(10, "TEN_SHOT", "TEN SHOT"),
	DEPLOY			(11, "DEPLOY", "DEPLOY"),
	UNDO			(12, "UNDO", "UNDO"),
	ZOOM			(13, "ZOOM", "ZOOM"),
	SHOOT			(14, "SHOOT", "SHOOT"),
	STUN2			(15, "STUN2", "STUN"),
	AOE_HEAL		(16, "AOE_HEAL", "AOE HEAL"),
	AOE_AMMO		(17, "AOE_AMMO", "AOE AMMO"),
	PARALYSE		(18, "PARALYSE", "PARALYSE"),
	SHOOT_MINE		(19, "SHOOT_MINE", "SHOOT MINE"),
	TWO_HIT			(20, "TWO_HIT", "TWO HIT"),
	THREE_HIT		(21, "THREE_HIT", "THREE HIT"),
	MOVE_UP			(22, "MOVE_UP", "MOVE UP"),
	PUSH			(23, "PUSH", "PUSH"),
	BLOCK			(24, "BLOCK", "BLOCK"),
	RELEASE			(25, "RELEASE", "RELEASE"),
	PULL			(26, "PULL", "PULL"),
	FOLLOWUP		(27, "FOLLOWUP", "FOLLOWUP"),
	INTERRUPT		(28, "INTERRUPT", "INTERRUPT"),
	FOUR_SHOT		(29, "FOUR_SHOT", "FOUR SHOT"),
	CHARGE			(30, "CHARGE", "CHARGE"),
	AOE_DAMAGE		(31, "AOE_DAMAGE", "AOE_DAMAGE"),
	COUNTER			(32, "COUNTER", "COUNTER"),
	UNLOAD_EXPLOSION(33, "UNLOAD_EXPLOSION", "UNLOAD EXPLOSION"),
	ASSASSINATION	(34, "ASSASSINATION", "ASSASSINATION"),
	PLACE			(35, "PLACE", "PLACE"),
	AOE_STUN		(36, "AOE_STUN", "AOE STUN"),
	CRIT			(37, "CRIT", "CRIT"),
	AOE_HIT			(38, "AOE_HIT", "AOE HIT");
	
	/**
	 * Maps defined here to retrieve Enum either by id or description
	 */
	
	private static final Map<Integer, WeaponEffect> BY_ID = new HashMap<Integer, WeaponEffect>();
	private static final Map<String, WeaponEffect> BY_DESC = new HashMap<String, WeaponEffect>();
	
	/**
	 * Map setter
	 */
	
	static {
		for(WeaponEffect e : values()) {
			BY_ID.put(e.id, e);
			BY_DESC.put(e.desc, e);
		}
	}
	
	public final int id;
	public final String desc;
	public final String desc2;
	
	/**
	 * Enum constructor
	 * @param _id
	 * @param _desc
	 */
	
	private WeaponEffect(int _id, String _desc, String _desc2) {
		this.id = _id;
		this.desc = _desc;
		this.desc2 = _desc2;
	}
	
	/**
	 * Retrieve Enum by id
	 * @param id
	 * @return
	 */
	
	public static WeaponEffect valueOfId(int id) {
		return BY_ID.get(id);
	}
	
	/**
	 * Retrieve Enum by description
	 * @param desc
	 * @return
	 */
	
	public static WeaponEffect valueOfDesc(String desc) {
		return BY_DESC.get(desc);
	}
}
