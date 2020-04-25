package google;

import java.io.IOException;
import java.util.List;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.ValueRange;

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
	
	/**
	 * Append raw data into the spreadsheet table
	 * @param service Sheets client service
	 * @param file_id ID of the file on google drive
	 * @param list Values in form of list
	 * @param rowStart Sheet and row where it should start to append
	 * @return Number of updated rows
	 * @throws IOException
	 */
	
	public static int appendRawDataToSpreadsheet(final Sheets service, final String file_id, final List<List<Object>> list, final String rowStart) throws IOException {
		ValueRange values = new ValueRange().setValues(list).setMajorDimension("COLUMNS");
		AppendValuesResponse result = service.spreadsheets().values()
			.append(file_id, rowStart, values)
			.setValueInputOption("RAW")
			.setInsertDataOption("INSERT_ROWS")
			.execute();
		return result.getUpdates().getUpdatedRows();
	}
}
