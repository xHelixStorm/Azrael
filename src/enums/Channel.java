package enums;

public enum Channel {
	LOG ("log"),
	TRA ("tra"),
	EDI	("edi"),
	DEL	("del"),
	BOT	("bot"),
	DEF	("def"),
	QUI	("qui");
	
	private String type;
	
	private Channel(String _type) {
		this.type = _type;
	}
	
	public String getType() {
		return this.type;
	}
}
