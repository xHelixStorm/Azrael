package constructors;

public class Skins {
	private int item_id;
	private String shop_description;
	private long price;
	private String skin_type;
	private String skin_description;
	
	public void setItemID(int _item_id){
		item_id = _item_id;
	}
	public void setShopDescription(String _shop_description){
		shop_description = _shop_description;
	}
	public void setPrice(long _price){
		price = _price;
	}
	public void setSkinType(String _skin_type){
		skin_type = _skin_type;
	}
	public void setSkinDescription(String _skin_description){
		skin_description = _skin_description;
	}
	
	public int getItemID(){
		return item_id;
	}
	public String getShopDescription(){
		return shop_description;
	}
	public long getPrice(){
		return price;
	}
	public String getSkinType(){
		return skin_type;
	}
	public String getSkinDescription(){
		return skin_description;
	}
}
