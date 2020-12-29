package enums;

import java.util.HashMap;
import java.util.Map;

public enum WeaponEffect {
	HIT				(1, "HIT"),
	EXPLOSION		(2, "EXPLOSION"),
	HEAL			(3, "HEAL"),
	STUN			(4, "STUN"),
	BLOW			(5, "BLOW"),
	THREE_SHOT		(6, "THREE_SHOT"),
	SPRAY			(7, "SPRAY"),
	CLOSE_SPRAY		(8, "CLOSE_SPRAY"),
	TWO_SHOT		(9, "TWO_SHOT"),
	TEN_SHOT		(10, "TEN_SHOT"),
	DEPLOY			(11, "DEPLOY"),
	UNDO			(12, "UNDO"),
	ZOOM			(13, "ZOOM"),
	SHOOT			(14, "SHOOT"),
	STUN2			(15, "STUN2"),
	AOE_HEAL		(16, "AOE_HEAL"),
	AOE_AMMO		(17, "AOE_AMMO"),
	PARALYSE		(18, "PARALYSE"),
	SHOOT_MINE		(19, "SHOOT_MINE"),
	TWO_HIT			(20, "TWO_HIT"),
	THREE_HIT		(21, "THREE_HIT"),
	MOVE_UP			(22, "MOVE_UP"),
	PUSH			(23, "PUSH"),
	BLOCK			(24, "BLOCK"),
	RELEASE			(25, "RELEASE"),
	PULL			(26, "PULL"),
	FOLLOWUP		(27, "FOLLOWUP"),
	INTERRUPT		(28, "INTERRUPT"),
	FOUR_SHOT		(29, "FOUR_SHOT");
	
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
	
	/**
	 * Enum constructor
	 * @param _id
	 * @param _desc
	 */
	
	private WeaponEffect(int _id, String _desc) {
		this.id = _id;
		this.desc = _desc;
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
