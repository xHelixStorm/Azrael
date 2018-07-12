package inventory;

public class Dailies {
	private int item_id;
	private String description;
	private int weight;
	private String type;
	private String type_description;
	private String action;
	
	public void setItemId(int _item_id){
		item_id = _item_id;
	}
	public void setDescription(String _description){
		description = _description;
	}
	public void setWeight(int _weight){
		weight = _weight;
	}
	public void SetType(String _type){
		type = _type;
	}
	public void setTypeDescription(String _type_description){
		type_description = _type_description;
	}
	public void setAction(String _action){
		action = _action;
	}
	
	public int getItemID(){
		return item_id;
	}
	public String getDescription(){
		return description;
	}
	public int getWeight(){
		return weight;
	}
	public String getType(){
		return type;
	}
	public String getTypeDescription(){
		return type_description;
	}
	public String getAction(){
		return action;
	}
}
