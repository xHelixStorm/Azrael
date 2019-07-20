package constructors;

public class Skills {
	private int skill_id;
	private String description;
	private String fullDescription;
	private long price;
	private String thumbnail;
	private boolean enabled;
	
	public Skills(int _skill_id, String _description, String _fullDescription, long _price, String _thumbnail, boolean _enabled) {
		this.skill_id = _skill_id;
		this.description = _description;
		this.fullDescription = _fullDescription;
		this.price = _price;
		this.thumbnail = _thumbnail;
		this.enabled = _enabled;
	}
	
	public int getSkillId() {
		return skill_id;
	}
	public String getDescription() {
		return description;
	}
	public String getFullDescription() {
		return fullDescription;
	}
	public long getPrice() {
		return price;
	}
	public String getThumbnail() {
		return thumbnail;
	}
	public boolean getEnabled() {
		return enabled;
	}
}
