package de.azrael.constructors;

/**
 * Class which will contain individual thumbnails for each guild
 * @author xHelixStorm
 *
 */

public class Thumbnails {
	private boolean isDefault = false;
	private String pug = null;
	private String meow = null;
	private String ban = null;
	private String settings = null;
	private String denied = null;
	private String shop = null;
	private String unban = null;
	private String unmute = null;
	private String kick = null;
	private String caught = null;
	private String falseAlarm = null;
	private String heavy = null;
	private String heavyEnd = null;
	
	
	/**
	 * Empty constructor in case of errors
	 */
	public Thumbnails() {
		this.isDefault = true;
	}
	
	/**
	 * Constructor to initialize all variables
	 * @param pug
	 * @param meow
	 * @param ban
	 * @param settings
	 * @param denied
	 * @param shop
	 * @param unban
	 * @param unmute
	 * @param kick
	 * @param caught
	 * @param falseAlarm
	 * @param heavy
	 * @param heavyEnd
	 */
	public Thumbnails(String pug, String meow, String ban, String settings, String denied, String shop, String unban,
			String unmute, String kick, String caught, String falseAlarm, String heavy, String heavyEnd) {
		this.pug = pug;
		this.meow = meow;
		this.ban = ban;
		this.settings = settings;
		this.denied = denied;
		this.shop = shop;
		this.unban = unban;
		this.unmute = unmute;
		this.kick = kick;
		this.caught = caught;
		this.falseAlarm = falseAlarm;
		this.heavy = heavy;
		this.heavyEnd = heavyEnd;
	}
	
	public boolean isDefault() {
		return isDefault;
	}
	
	public String getPug() {
		return pug;
	}

	public String getMeow() {
		return meow;
	}

	public String getBan() {
		return ban;
	}

	public String getSettings() {
		return settings;
	}

	public String getDenied() {
		return denied;
	}

	public String getShop() {
		return shop;
	}

	public String getUnban() {
		return unban;
	}

	public String getUnmute() {
		return unmute;
	}

	public String getKick() {
		return kick;
	}

	public String getCaught() {
		return caught;
	}

	public String getFalseAlarm() {
		return falseAlarm;
	}

	public String getHeavy() {
		return heavy;
	}

	public String getHeavyEnd() {
		return heavyEnd;
	}
}
