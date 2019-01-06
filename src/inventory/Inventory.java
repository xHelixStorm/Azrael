package inventory;

public class Inventory {
	private int item_id;
	private String skin_type;
	private String status;
	
	public Inventory(int _item_id, String _skin_type, String _status) {
		this.item_id = _item_id;
		this.skin_type = _skin_type;
		this.status = _status;
	}
	
	public int getItemID() {
		return item_id;
	}
	public String getSkinType() {
		return skin_type;
	}
	public String getStatus() {
		return status;
	}
}
