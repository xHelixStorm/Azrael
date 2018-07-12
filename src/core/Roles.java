package core;

public class Roles {
	private long guild_id;
	private String guild_name;
	private long role_id;
	private String role_name;
	private String category_abv;
	private String category_name;
	
	public void setGuild_ID(long _guild_id){
		guild_id = _guild_id;
	}
	public void setGuild_Name(String _guild_name){
		guild_name = _guild_name;
	}
	public void setRole_ID(long _role_id){
		role_id = _role_id;
	}
	public void setRole_Name(String _role_name){
		role_name = _role_name;
	}
	public void setCategory_ABV(String _category_abv){
		category_abv = _category_abv;
	}
	public void setCategory_Name(String _category_name){
		category_name = _category_name;
	}
	
	public long getGuild_ID(){
		return guild_id;
	}
	public String getGuild_Name(){
		return guild_name;
	}
	public long getRole_ID(){
		return role_id;
	}
	public String getRole_Name(){
		return role_name;
	}
	public String getCategory_ABV(){
		return category_abv;
	}
	public String getCategory_Name(){
		return category_name;
	}
	
}
