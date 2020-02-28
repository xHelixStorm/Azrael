package constructors;

/**
 * General API setup information
 * @author xHelixStorm
 *
 */

public class GoogleAPISetup {
	private String file_id;
	private String title;
	private int api_id;
	private String api;
	
	/**
	 * Main constructor
	 * @param _file_id
	 * @param _title
	 * @param _api_id
	 * @param _api
	 */
	
	public GoogleAPISetup(String _file_id, String _title, int _api_id, String _api) {
		this.file_id = _file_id;
		this.title = _title;
		this.api_id = _api_id;
		this.api = _api;
	}
	
	/**
	 * Retrieve the file id
	 * @return
	 */
	
	public String getFileID() {
		return this.file_id;
	}
	
	/**
	 * Retrieve the title of the file
	 * @return
	 */
	
	public String getTitle() {
		return this.title;
	}
	
	/**
	 * Retrieve the type of the file
	 * @return
	 */
	
	public int getApiID() {
		return this.api_id;
	}
	
	/**
	 * Retrieve the API name
	 * @return
	 */
	
	public String getAPI() {
		return this.api;
	}
}
