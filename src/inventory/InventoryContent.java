package inventory;

import java.sql.Timestamp;

public class InventoryContent {
	private long user_id;
	private String description;
	private Timestamp timestamp;
	private int number;
	private String type;
	private String status;
	private Timestamp expiration;
	private String weapon_description;
	private String stat;
	private int weapon_category_id;
	private String weapon_category_description;
	
	public void setUserID(long _user_id){
		user_id = _user_id;
	}
	public void setDescription(String _description){
		description = _description;
	}
	public void setTimestamp(Timestamp _timestamp){
		timestamp = _timestamp;
	}
	public void setNumber(int _number){
		number = _number;
	}
	public void setType(String _type){
		type = _type;
	}
	public void setStatus(String _status){
		status = _status;
	}
	public void setExpiration(Timestamp _expiration){
		expiration = _expiration;
	}
	public void setWeaponDescription(String _weapon_description) {
		weapon_description = _weapon_description;
	}
	public void setStat(String _stat) {
		stat = _stat;
	}
	public void setWeaponCategoryID(int _weapon_category_id) {
		weapon_category_id = _weapon_category_id;
	}
	public void setWeaponCategoryDescription(String _weapon_category_description) {
		weapon_category_description = _weapon_category_description;
	}
	
	public long getUserID(){
		return user_id;
	}
	public String getDescription(){
		return description;
	}
	public Timestamp getTimestamp(){
		return timestamp;
	}
	public int getNumber(){
		return number;
	}
	public String getType(){
		return type;
	}
	public String getStatus(){
		return status;
	}
	public Timestamp getExpiration(){
		return expiration;
	}
	public String getWeaponDescription(){
		return weapon_description;
	}
	public String getStat() {
		return stat;
	}
	public int getWeaponCategoryID() {
		return weapon_category_id;
	}
	public String getWeaponCategoryDescription() {
		return weapon_category_description;
	}
}
