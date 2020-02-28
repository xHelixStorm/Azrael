package google;

import java.io.IOException;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;

public class GoogleSheets {
	
	/**
	 * Retrieve Sheets client service
	 * @param guild_id to retrieve the credentials from
	 * @return Sheets service
	 * @throws Exception Any error along the way
	 */
	
	public static Sheets getSheetsClientService() throws Exception {
		//Build a new authorized API client service
		final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		return new Sheets.Builder(httpTransport, GoogleUtils.getJacksonFactory(), GoogleUtils.getCredentials(httpTransport, "sheets"))
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
	
	/**
	 * Create a new spreadsheet and return the id
	 * @param service Sheets client service
	 * @param title Title for the new spreadsheet
	 * @return spreadsheet id
	 * @throws IOException
	 */
	
	public static String createSpreadsheet(final Sheets service, final String title) throws IOException {
		//create Spreadsheet
		Spreadsheet spreadsheet = new Spreadsheet().setProperties(new SpreadsheetProperties().setTitle(title));
		Spreadsheet result = service.spreadsheets().create(spreadsheet).execute();
		return result.getSpreadsheetId();
	}
	
	/**
	 * Retrieve an accessible spreadsheet
	 * @param service Sheets client service
	 * @param file_id ID of the file on google drive
	 * @return Spreadsheet object
	 * @throws IOException
	 */
	
	public static Spreadsheet getSpreadsheet(final Sheets service, final String file_id) throws IOException {
		Spreadsheet result = service.spreadsheets().get(file_id).execute();
		return result;
	}
}
