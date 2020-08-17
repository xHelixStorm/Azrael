package enums;

public enum Channel {
	LOG ("log"),
	TRA ("tra"),
	EDI	("edi"),
	DEL	("del"),
	BOT	("bot"),
	DEF	("def"),
	QUI	("qui"),
	CO1	("co1"),
	CO2	("co2"),
	CO3	("co3"),
	REA	("rea"),
	WAT	("wat"),
	VOT	("vot"),
	RSS	("rss");
	
	private String type;
	
	private Channel(String _type) {
		this.type = _type;
	}
	
	public String getType() {
		return this.type;
	}
}
