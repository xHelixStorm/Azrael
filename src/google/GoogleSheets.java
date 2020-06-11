package google;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
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
	 * Retrieve all rows on a sheet
	 * @param service Sheets client service
	 * @param file_id ID of the file on google drive
	 * @param sheet Sheet to retrieve the rows from
	 * @return ValueRange object with all entries
	 * @throws IOException
	 */
	
	public static ValueRange readWholeSpreadsheet(final Sheets service, final String file_id, final String sheet) throws IOException {
		ValueRange reponse = service.spreadsheets().values()
			.get(file_id, sheet)
			.execute();
		
		return reponse;
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
	
	/**
	 * Update row in the spreadsheet table
	 * @param service
	 * @param file_id
	 * @param list
	 * @param rowStart
	 * @return
	 * @throws IOException
	 */
	
	public static int overwriteRowOnSpreadsheet(final Sheets service, final String file_id, final List<List<Object>> list, final String rowStart) throws IOException {
		ValueRange values = new ValueRange().setValues(list).setMajorDimension("COLUMNS");
		UpdateValuesResponse result = service.spreadsheets().values()
			.update(file_id, rowStart, values)
			.setValueInputOption("RAW")
			.execute();
		return result.size();
	}
	
	public static int deleteRowOnSpreadsheet(final Sheets service, final String file_id, final int rowStart, final int sheet_id) throws IOException {
		BatchUpdateSpreadsheetResponse result = service.spreadsheets().batchUpdate(file_id, new BatchUpdateSpreadsheetRequest()
			.setRequests(Arrays.asList(new Request()
					.setDeleteDimension(new DeleteDimensionRequest()
							.setRange(new DimensionRange()
									.setDimension("ROWS")
									.setSheetId(sheet_id)
									.setStartIndex(rowStart-1)
									.setEndIndex(rowStart))))))
			.execute();
		return result.size();
	}
}
