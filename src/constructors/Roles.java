package constructors;

public class Roles {
	private long role_id;
	private String role_name;
	private int level;
	private String category_abv;
	private String category_name;
	
	public Roles(String _category_abv, String _category_name) {
		this.role_id = 0;
		this.role_name = "";
		this.level = 0;
		this.category_abv = _category_abv;
		this.category_name = _category_name;
	}
	
	public Roles(long _role_id, String _role_name, int _level) {
		this.role_id = _role_id;
		this.role_name = _role_name;
		this.level = _level;
		this.category_abv = null;
		this.category_name = null;
	}
	
	public Roles(long _role_id, String _role_name, int _level, String _category_abv, String _category_name) {
		this.role_id = _role_id;
		this.role_name = _role_name;
		this.level = _level;
		this.category_abv = _category_abv;
		this.category_name = _category_name;
	}
	
	public long getRole_ID() {
		return role_id;
	}
	public String getRole_Name() {
		return role_name;
	}
	public int getLevel() {
		return level;
	}
	public String getCategory_ABV() {
		return category_abv;
	}
	public String getCategory_Name() {
		return category_name;
	}
	
}
