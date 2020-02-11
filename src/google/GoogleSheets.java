package google;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Spreadsheet;

public class GoogleSheets {
	
	/**
	 * Retrieve Sheets client service
	 * @param guild_id to retrieve the credentials from
	 * @return Sheets service
	 * @throws Exception Any error along the way
	 */
	
	public static Sheets getSheetsClientService(final long guild_id) throws Exception {
		//Build a new authorized API client service
		final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		return new Sheets.Builder(httpTransport, GoogleUtils.getJacksonFactory(), GoogleUtils.getCredentials(httpTransport, guild_id, "sheets"))
			.setApplicationName("Azrael")
			.build();
	}
	
	/**
	 * Retrieve a specific spreadsheet
	 * @param service Sheets client service
	 * @param sheet_id ID of the spreadsheet to retrieve
	 * @return Spreadsheet Object
	 * @throws Exception Any error along the way
	 */
	
	public static Spreadsheet getSheet(final Sheets service, final String spreadsheet_id) throws Exception {
		//get Spreadsheet
		return service.spreadsheets().get(spreadsheet_id).execute();
	}
}
