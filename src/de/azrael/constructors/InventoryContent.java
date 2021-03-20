package de.azrael.constructors;

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
	private String skill_description;
	private String file_type;
	
	public void setUserID(long _user_id){
		this.user_id = _user_id;
	}
	public void setDescription(String _description){
		this.description = _description;
	}
	public void setTimestamp(Timestamp _timestamp){
		this.timestamp = _timestamp;
	}
	public void setNumber(int _number){
		this.number = _number;
	}
	public void setType(String _type){
		this.type = _type;
	}
	public void setStatus(String _status){
		this.status = _status;
	}
	public void setExpiration(Timestamp _expiration){
		this.expiration = _expiration;
	}
	public void setWeaponDescription(String _weapon_description) {
		this.weapon_description = _weapon_description;
	}
	public void setStat(String _stat) {
		this.stat = _stat;
	}
	public void setWeaponCategoryID(int _weapon_category_id) {
		this.weapon_category_id = _weapon_category_id;
	}
	public void setWeaponCategoryDescription(String _weapon_category_description) {
		this.weapon_category_description = _weapon_category_description;
	}
	public void setSkillDescription(String _skill_description) {
		this.skill_description = _skill_description;
	}
	public void setFileType(String _file_type) {
		this.file_type = _file_type;
	}
	
	public long getUserID(){
		return this.user_id;
	}
	public String getDescription(){
		return this.description;
	}
	public Timestamp getTimestamp(){
		return this.timestamp;
	}
	public int getNumber(){
		return this.number;
	}
	public String getType(){
		return this.type;
	}
	public String getStatus(){
		return this.status;
	}
	public Timestamp getExpiration(){
		return this.expiration;
	}
	public String getWeaponDescription(){
		return this.weapon_description;
	}
	public String getStat() {
		return this.stat;
	}
	public int getWeaponCategoryID() {
		return this.weapon_category_id;
	}
	public String getWeaponCategoryDescription() {
		return this.weapon_category_description;
	}
	public String getSkillDescription() {
		return this.skill_description;
	}
	public String getFileType() {
		return this.file_type;
	}
}
