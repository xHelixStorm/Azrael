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
}
