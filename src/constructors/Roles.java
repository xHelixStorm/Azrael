package constructors;

public class Roles {
	private long role_id;
	private String role_name;
	private int level;
	private String category_abv;
	private String category_name;
	private boolean persistant;
	
	public Roles(String _category_abv, String _category_name) {
		this.role_id = 0;
		this.role_name = "";
		this.level = 0;
		this.category_abv = _category_abv;
		this.category_name = _category_name;
		this.persistant = false;
	}
	
	public Roles(long _role_id, String _role_name, int _level) {
		this.role_id = _role_id;
		this.role_name = _role_name;
		this.level = _level;
		this.category_abv = null;
		this.category_name = null;
		this.persistant = false;
	}
	
	public Roles(long _role_id, String _role_name, int _level, String _category_abv, String _category_name, boolean _persistant) {
		this.role_id = _role_id;
		this.role_name = _role_name;
		this.level = _level;
		this.category_abv = _category_abv;
		this.category_name = _category_name;
		this.persistant = _persistant;
	}
	
	public long getRole_ID() {
		return this.role_id;
	}
	public String getRole_Name() {
		return this.role_name;
	}
	public int getLevel() {
		return this.level;
	}
	public String getCategory_ABV() {
		return this.category_abv;
	}
	public String getCategory_Name() {
		return this.category_name;
	}
	public boolean isPersistant() {
		return this.persistant;
	}
	
}
