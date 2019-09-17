package constructors;

public class RSS {
	private String url; 
	private String format;
	private int type;
	
	public RSS(String _url, String _format, int _type) {
		this.url = _url;
		this.format = _format;
		this.type = _type;
	}
	
	public String getURL() {
		return url;
	}
	public String getFormat() {
		return format;
	}
	public int getType() {
		return type;
	}
}
