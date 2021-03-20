package de.azrael.constructors;

public class Dailies {
	private int item_id;
	private String description;
	private int weight;
	private String type;
	private String type_description;
	private String action;
	
	
		public void setItemId(int _item_id){
		this.item_id = _item_id;
	}
	public void setDescription(String _description){
		this.description = _description;
	}
	public void setWeight(int _weight){
		this.weight = _weight;
	}
	public void SetType(String _type){
		this.type = _type;
	}
	public void setTypeDescription(String _type_description){
		this.type_description = _type_description;
	}
	public void setAction(String _action){
		this.action = _action;
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
