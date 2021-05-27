package de.azrael.google;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import de.azrael.enums.GoogleDD;
import de.azrael.enums.GoogleEvent;
import net.dv8tion.jda.api.entities.Guild;

public class GoogleSheets {
	
	/**
	 * Retrieve Sheets client service
	 * @return Sheets service
	 * @throws SocketTimeoutException
	 * @throws Exception Any error along the way
	 */
	
	public static Sheets getSheetsClientService() throws SocketTimeoutException, Exception {
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
	 * @throws SocketTimeoutException
	 * @throws Exception Any error along the way
	 */
	
	public static Spreadsheet getSheet(final Sheets service, final String spreadsheet_id) throws SocketTimeoutException, Exception {
		//get Spreadsheet
		return service.spreadsheets().get(spreadsheet_id).execute();
	}
	
	/**
	 * Create a new spreadsheet and return the id
	 * @param service Sheets client service
	 * @param title Title for the new spreadsheet
	 * @return spreadsheet id
	 * @throws SocketTimeoutException
	 * @throws IOException
	 */
	
	public static String createSpreadsheet(final Sheets service, final String title) throws SocketTimeoutException, IOException {
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
	 * @throws SocketTimeoutException
	 * @throws IOException
	 */
	
	public static Spreadsheet getSpreadsheet(final Sheets service, final String file_id) throws SocketTimeoutException, IOException {
		Spreadsheet result = service.spreadsheets().get(file_id).execute();
		return result;
	}
	
	/**
	 * Retrieve all rows on a sheet
	 * @param service Sheets client service
	 * @param file_id ID of the file on google drive
	 * @param sheet Sheet to retrieve the rows from
	 * @return ValueRange object with all entries
	 * @throws SocketTimeoutException
	 * @throws IOException
	 */
	
	public static ValueRange readWholeSpreadsheet(final Sheets service, final String file_id, final String sheet) throws SocketTimeoutException, IOException {
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
	 * @param dimension column arrangement
	 * @return Number of updated rows
	 * @throws SocketTimeoutException
	 * @throws IOException
	 */
	
	public static int appendRawDataToSpreadsheet(final Sheets service, final String file_id, final List<List<Object>> list, final String rowStart, String dimension) throws SocketTimeoutException, IOException {
		if(dimension == null)
			dimension = "COLUMNS";
		ValueRange values = new ValueRange().setValues(list).setMajorDimension(dimension);
		AppendValuesResponse result = service.spreadsheets().values()
			.append(file_id, rowStart, values)
			.setValueInputOption("USER_ENTERED")
			.setInsertDataOption("INSERT_ROWS")
			.execute();
		return result.getUpdates().getUpdatedRows();
	}
	
	/**
	 * Update row in the spreadsheet table
	 * @param service Sheets client service
	 * @param file_id ID of the file on google drive
	 * @param list Values in form of list
	 * @param rowStart Sheet and row where it should start to overwrite
	 * @return Number of updated rows
	 * @throws SocketTimeoutException
	 * @throws IOException
	 */
	
	public static int overwriteRowOnSpreadsheet(final Sheets service, final String file_id, final List<List<Object>> list, final String rowStart) throws SocketTimeoutException, IOException {
		ValueRange values = new ValueRange().setValues(list).setMajorDimension("COLUMNS");
		UpdateValuesResponse result = service.spreadsheets().values()
			.update(file_id, rowStart, values)
			.setValueInputOption("USER_ENTERED")
			.execute();
		return result.size();
	}
	
	/**
	 * Update multiple rows on the spreadsheet
	 * @param service Sheets client service
	 * @param file_id ID of the file on google drive
	 * @param data Rows data with update location
	 * @return Number of updated rows
	 * @throws SocketTimeoutException
	 * @throws IOException
	 */
	
	public static int batchUpdateRowsOnSpreadsheet(final Sheets service, final String file_id, ArrayList<ValueRange> data) throws SocketTimeoutException, IOException {
		BatchUpdateValuesRequest requestBody = new BatchUpdateValuesRequest().setValueInputOption("USER_ENTERED").setData(data);
		BatchUpdateValuesResponse request = service.spreadsheets().values().batchUpdate(file_id, requestBody).execute();
		return request.size();
	}
	
	/**
	 * Delete single row on spreadsheet
	 * @param service Sheets client service
	 * @param file_id ID of the file on google drive
	 * @param rowStart Row on a sheet to delete
	 * @param sheet_id ID of the sheet
	 * @return Number of updated rows
	 * @throws SocketTimeoutException
	 * @throws IOException
	 */
	
	public static int deleteRowOnSpreadsheet(final Sheets service, final String file_id, final int rowStart, final int sheet_id) throws SocketTimeoutException, IOException {
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
	
	/**
	 * Delete multiple rows on the spreadsheet
	 * @param service service Sheets client service
	 * @param file_id ID of the file on google drive
	 * @param requests Multiple DeleteDimension requests to delete rows on a sheet
	 * @return Number of updated rows
	 * @throws SocketTimeoutException
	 * @throws IOException
	 */
	
	public static int batchDeleteRowsOnSpreadsheet(final Sheets service, final String file_id, List<Request> requests) throws SocketTimeoutException, IOException {
		BatchUpdateSpreadsheetResponse result = service.spreadsheets().batchUpdate(file_id, new BatchUpdateSpreadsheetRequest().setRequests(requests)).execute();
		return result.size();
	}
	
	/**
	 * Google spreadsheet requests for mutes
	 * @param event
	 * @param guild
	 * @param channel_id
	 * @param user_id
	 * @param timestamp
	 * @param name
	 * @param effectiveName
	 * @param reporterName
	 * @param reporterEffectiveName
	 * @param reason
	 * @param time
	 * @param warning_id
	 * @param unmute_timestamp
	 */
	
	public static void spreadsheetMuteRequest(String [] event, Guild guild, String channel_id, String user_id, Timestamp timestamp, String name, String effectiveName, String reporterName, String reporterEffectiveName, String reason, String time, String warning_id, Timestamp unmute_timestamp) {
		if(GoogleUtils.spreadsheetRequestBody(event, guild, channel_id, GoogleEvent.MUTE.name())) {
			final var columns = GoogleUtils.retrieveSpreadsheetColumns(event[0], GoogleEvent.MUTE, guild);
			if(columns != null && columns.size() > 0) {
				ArrayList<List<Object>> values = new ArrayList<List<Object>>();
				for(final var column : columns) {
					GoogleDD item = column.getItem();
					switch(item) {
						case TIMESTAMP -> 			values.add(Arrays.asList(item.valueFormatter(timestamp, column.getFormatter())));
						case USER_ID -> 			values.add(Arrays.asList(item.valueFormatter(user_id, column.getFormatter())));
						case NAME ->				values.add(Arrays.asList(item.valueFormatter(name, column.getFormatter())));
						case USERNAME ->			values.add(Arrays.asList(item.valueFormatter(effectiveName, column.getFormatter())));
						case REPORTER_NAME -> 		values.add(Arrays.asList(item.valueFormatter(reporterName, column.getFormatter())));
						case REPORTER_USERNAME -> 	values.add(Arrays.asList(item.valueFormatter(reporterEffectiveName, column.getFormatter())));
						case REASON	->				values.add(Arrays.asList(item.valueFormatter(reason, column.getFormatter())));
						case TIME ->				values.add(Arrays.asList(item.valueFormatter(time, column.getFormatter())));
						case ACTION ->				values.add(Arrays.asList(item.valueFormatter(GoogleEvent.MUTE.name(), column.getFormatter())));
						case WARNING ->				values.add(Arrays.asList(item.valueFormatter(warning_id, column.getFormatter())));
						case UNMUTE_TIME -> 		values.add(Arrays.asList(item.valueFormatter(unmute_timestamp, column.getFormatter())));
						case PLACEHOLDER ->			values.add(Arrays.asList(item.valueFormatter("", column.getFormatter())));
						case GUILD_ID ->			values.add(Arrays.asList(item.valueFormatter(guild.getId(), column.getFormatter())));
						case GUILD_NAME ->			values.add(Arrays.asList(item.valueFormatter(guild.getName(), column.getFormatter())));
						default -> {}
					}
				}
				final SocketTimeoutException exception = GoogleUtils.appendRawDataToSpreadsheet(guild, GoogleEvent.MUTE, values, event[0], event[1]);
				if(exception != null && GoogleUtils.timeoutHandler(guild, event[0], GoogleEvent.MUTE.name(), exception)) {
					spreadsheetMuteRequest(event, guild, channel_id, user_id, timestamp, name, effectiveName, reporterName, reporterEffectiveName, reason, time, warning_id, unmute_timestamp);
				}
			}
		}
	}
	
	/**
	 * Google spreadsheets requests for re added mutes before the mute expires
	 * @param event
	 * @param guild
	 * @param channel_id
	 * @param user_id
	 * @param timestamp
	 * @param name
	 * @param effectiveName
	 * @param reporterName
	 * @param reporterEffectiveName
	 * @param reason
	 * @param time
	 * @param warning_id
	 * @param unmute_timestamp
	 */
	
	public static void spreadsheetMuteReaddRequest(String [] event, Guild guild, String channel_id, String user_id, Timestamp timestamp, String name, String effectiveName, String reporterName, String reporterEffectiveName, String reason, String time, String warning_id, Timestamp unmute_timestamp) {
		if(GoogleUtils.spreadsheetRequestBody(event, guild, channel_id, GoogleEvent.MUTE_READD.name())) {
			final var columns = GoogleUtils.retrieveSpreadsheetColumns(event[0], GoogleEvent.MUTE_READD, guild);
			if(columns != null && columns.size() > 0) {
				ArrayList<List<Object>> values = new ArrayList<List<Object>>();
				for(final var column : columns) {
					GoogleDD item = column.getItem();
					switch(item) {
						case TIMESTAMP -> 			values.add(Arrays.asList(item.valueFormatter(timestamp, column.getFormatter())));
						case USER_ID -> 			values.add(Arrays.asList(item.valueFormatter(user_id, column.getFormatter())));
						case NAME ->				values.add(Arrays.asList(item.valueFormatter(name, column.getFormatter())));
						case USERNAME ->			values.add(Arrays.asList(item.valueFormatter(effectiveName, column.getFormatter())));
						case REPORTER_NAME -> 		values.add(Arrays.asList(item.valueFormatter(reporterName, column.getFormatter())));
						case REPORTER_USERNAME -> 	values.add(Arrays.asList(item.valueFormatter(reporterEffectiveName, column.getFormatter())));
						case REASON	->				values.add(Arrays.asList(item.valueFormatter(reason, column.getFormatter())));
						case TIME ->				values.add(Arrays.asList(item.valueFormatter(time, column.getFormatter())));
						case ACTION ->				values.add(Arrays.asList(item.valueFormatter(GoogleEvent.MUTE_READD.name(), column.getFormatter())));
						case WARNING ->				values.add(Arrays.asList(item.valueFormatter(warning_id, column.getFormatter())));
						case UNMUTE_TIME -> 		values.add(Arrays.asList(item.valueFormatter(unmute_timestamp, column.getFormatter())));
						case PLACEHOLDER ->			values.add(Arrays.asList(item.valueFormatter("", column.getFormatter())));
						case GUILD_ID ->			values.add(Arrays.asList(item.valueFormatter(guild.getId(), column.getFormatter())));
						case GUILD_NAME ->			values.add(Arrays.asList(item.valueFormatter(guild.getName(), column.getFormatter())));
						default -> {}
					}
				}
				final SocketTimeoutException exception = GoogleUtils.appendRawDataToSpreadsheet(guild, GoogleEvent.MUTE_READD, values, event[0], event[1]);
				if(exception != null && GoogleUtils.timeoutHandler(guild, event[0], GoogleEvent.MUTE_READD.name(), exception)) {
					spreadsheetMuteReaddRequest(event, guild, channel_id, user_id, timestamp, name, effectiveName, reporterName, reporterEffectiveName, reason, time, warning_id, unmute_timestamp);
				}
			}
		}
	}
	
	/**
	 * Google spreadsheet requests for mute removals on end
	 * @param event
	 * @param guild
	 * @param channel_id
	 * @param user_id
	 * @param timestamp
	 * @param name
	 * @param effectiveName
	 * @param reporterName
	 * @param reporterEffectiveName
	 * @param reason
	 * @param role_id
	 * @param role_name
	 */
	
	public static void spreadsheetUnmuteRequest(String [] event, Guild guild, String channel_id, String user_id, Timestamp timestamp, String name, String effectiveName, String reporterName, String reporterEffectiveName, String reason, String role_id, String role_name) {
		if(GoogleUtils.spreadsheetRequestBody(event, guild, channel_id, GoogleEvent.UNMUTE.name())) {
			final var columns = GoogleUtils.retrieveSpreadsheetColumns(event[0], GoogleEvent.UNMUTE, guild);
			if(columns != null && columns.size() > 0) {
				ArrayList<List<Object>> values = new ArrayList<List<Object>>();
				for(final var column : columns) {
					GoogleDD item = column.getItem();
					switch(item) {
						case TIMESTAMP -> 			values.add(Arrays.asList(item.valueFormatter(timestamp, column.getFormatter())));
						case USER_ID -> 			values.add(Arrays.asList(item.valueFormatter(user_id, column.getFormatter())));
						case NAME ->				values.add(Arrays.asList(item.valueFormatter(name, column.getFormatter())));
						case USERNAME ->			values.add(Arrays.asList(item.valueFormatter(effectiveName, column.getFormatter())));
						case REPORTER_NAME -> 		values.add(Arrays.asList(item.valueFormatter(reporterName, column.getFormatter())));
						case REPORTER_USERNAME -> 	values.add(Arrays.asList(item.valueFormatter(reporterEffectiveName, column.getFormatter())));
						case REASON	->				values.add(Arrays.asList(item.valueFormatter(reason, column.getFormatter())));
						case ACTION ->				values.add(Arrays.asList(item.valueFormatter(GoogleEvent.UNMUTE.name(), column.getFormatter())));
						case PLACEHOLDER ->			values.add(Arrays.asList(item.valueFormatter("", column.getFormatter())));
						case GUILD_ID ->			values.add(Arrays.asList(item.valueFormatter(guild.getId(), column.getFormatter())));
						case GUILD_NAME ->			values.add(Arrays.asList(item.valueFormatter(guild.getName(), column.getFormatter())));
						case ROLE_ID ->				values.add(Arrays.asList(item.valueFormatter(role_id, column.getFormatter())));
						case ROLE_NAME ->			values.add(Arrays.asList(item.valueFormatter(role_name, column.getFormatter())));
						default -> {}
					}
				}
				final SocketTimeoutException exception = GoogleUtils.appendRawDataToSpreadsheet(guild, GoogleEvent.UNMUTE, values, event[0], event[1]);
				if(exception != null && GoogleUtils.timeoutHandler(guild, event[0], GoogleEvent.UNMUTE.name(), exception)) {
					spreadsheetUnmuteRequest(event, guild, channel_id, user_id, timestamp, name, effectiveName, reporterName, reporterEffectiveName, reason, role_id, role_name);
				}
			}
		}
	}
	
	/**
	 * Google spreadsheet requests for manual removal of a mute
	 * @param event
	 * @param guild
	 * @param channel_id
	 * @param user_id
	 * @param timestamp
	 * @param name
	 * @param effectiveName
	 * @param reporterName
	 * @param reporterEffectiveName
	 * @param reason
	 * @param role_id
	 * @param role_name
	 */
	
	public static void spreadsheetUnmuteManualRequest(String [] event, Guild guild, String channel_id, String user_id, Timestamp timestamp, String name, String effectiveName, String reporterName, String reporterEffectiveName, String reason, String role_id, String role_name) {
		if(GoogleUtils.spreadsheetRequestBody(event, guild, channel_id, GoogleEvent.UNMUTE_MANUAL.name())) {
			final var columns = GoogleUtils.retrieveSpreadsheetColumns(event[0], GoogleEvent.UNMUTE_MANUAL, guild);
			if(columns != null && columns.size() > 0) {
				ArrayList<List<Object>> values = new ArrayList<List<Object>>();
				for(final var column : columns) {
					GoogleDD item = column.getItem();
					switch(item) {
						case TIMESTAMP -> 			values.add(Arrays.asList(item.valueFormatter(timestamp, column.getFormatter())));
						case USER_ID -> 			values.add(Arrays.asList(item.valueFormatter(user_id, column.getFormatter())));
						case NAME ->				values.add(Arrays.asList(item.valueFormatter(name, column.getFormatter())));
						case USERNAME ->			values.add(Arrays.asList(item.valueFormatter(effectiveName, column.getFormatter())));
						case REPORTER_NAME -> 		values.add(Arrays.asList(item.valueFormatter(reporterName, column.getFormatter())));
						case REPORTER_USERNAME -> 	values.add(Arrays.asList(item.valueFormatter(reporterEffectiveName, column.getFormatter())));
						case REASON	->				values.add(Arrays.asList(item.valueFormatter(reason, column.getFormatter())));
						case ACTION ->				values.add(Arrays.asList(item.valueFormatter(GoogleEvent.UNMUTE_MANUAL.name(), column.getFormatter())));
						case PLACEHOLDER ->			values.add(Arrays.asList(item.valueFormatter("", column.getFormatter())));
						case GUILD_ID ->			values.add(Arrays.asList(item.valueFormatter(guild.getId(), column.getFormatter())));
						case GUILD_NAME ->			values.add(Arrays.asList(item.valueFormatter(guild.getName(), column.getFormatter())));
						case ROLE_ID ->				values.add(Arrays.asList(item.valueFormatter(role_id, column.getFormatter())));
						case ROLE_NAME ->			values.add(Arrays.asList(item.valueFormatter(role_name, column.getFormatter())));
						default -> {}
					}
				}
				final SocketTimeoutException exception = GoogleUtils.appendRawDataToSpreadsheet(guild, GoogleEvent.UNMUTE_MANUAL, values, event[0], event[1]);
				if(exception != null && GoogleUtils.timeoutHandler(guild, event[0], GoogleEvent.UNMUTE_MANUAL.name(), exception)) {
					spreadsheetUnmuteManualRequest(event, guild, channel_id, user_id, timestamp, name, effectiveName, reporterName, reporterEffectiveName, reason, role_id, role_name);
				}
			}
		}
	}
	
	/**
	 * Google spreadsheet requests for kicks
	 * @param event
	 * @param guild
	 * @param channel_id
	 * @param user_id
	 * @param timestamp
	 * @param name
	 * @param effectiveName
	 * @param reporterName
	 * @param reporterEffectiveName
	 * @param reason
	 */
	
	public static void spreadsheetKickRequest(String [] event, Guild guild, String channel_id, String user_id, Timestamp timestamp, String name, String effectiveName, String reporterName, String reporterEffectiveName, String reason) {
		if(GoogleUtils.spreadsheetRequestBody(event, guild, channel_id, GoogleEvent.KICK.name())) {
			final var columns = GoogleUtils.retrieveSpreadsheetColumns(event[0], GoogleEvent.KICK, guild);
			if(columns != null && columns.size() > 0) {
				ArrayList<List<Object>> values = new ArrayList<List<Object>>();
				for(final var column : columns) {
					GoogleDD item = column.getItem();
					switch(item) {
						case TIMESTAMP -> 			values.add(Arrays.asList(item.valueFormatter(timestamp, column.getFormatter())));
						case USER_ID -> 			values.add(Arrays.asList(item.valueFormatter(user_id, column.getFormatter())));
						case NAME ->				values.add(Arrays.asList(item.valueFormatter(name, column.getFormatter())));
						case USERNAME ->			values.add(Arrays.asList(item.valueFormatter(effectiveName, column.getFormatter())));
						case REPORTER_NAME -> 		values.add(Arrays.asList(item.valueFormatter(reporterName, column.getFormatter())));
						case REPORTER_USERNAME -> 	values.add(Arrays.asList(item.valueFormatter(reporterEffectiveName, column.getFormatter())));
						case REASON	->				values.add(Arrays.asList(item.valueFormatter(reason, column.getFormatter())));
						case ACTION ->				values.add(Arrays.asList(item.valueFormatter(GoogleEvent.KICK.name(), column.getFormatter())));
						case PLACEHOLDER ->			values.add(Arrays.asList(item.valueFormatter("", column.getFormatter())));
						case GUILD_ID ->			values.add(Arrays.asList(item.valueFormatter(guild.getId(), column.getFormatter())));
						case GUILD_NAME ->			values.add(Arrays.asList(item.valueFormatter(guild.getName(), column.getFormatter())));
						default -> {}
					}
				}
				final SocketTimeoutException exception = GoogleUtils.appendRawDataToSpreadsheet(guild, GoogleEvent.KICK, values, event[0], event[1]);
				if(exception != null && GoogleUtils.timeoutHandler(guild, event[0], GoogleEvent.KICK.name(), exception)) {
					spreadsheetKickRequest(event, guild, channel_id, user_id, timestamp, name, effectiveName, reporterName, reporterEffectiveName, reason);
				}
			}
		}
	}
	
	/**
	 * Google spreadsheet requests for ban removals
	 * @param event
	 * @param guild
	 * @param channel_id
	 * @param user_id
	 * @param timestamp
	 * @param name
	 * @param effectiveName
	 * @param reporterName
	 * @param reporterEffectiveName
	 * @param reason
	 */
	
	public static void spreadsheetUnbanRequest(String [] event, Guild guild, String channel_id, String user_id, Timestamp timestamp, String name, String effectiveName, String reporterName, String reporterEffectiveName, String reason) {
		if(GoogleUtils.spreadsheetRequestBody(event, guild, channel_id, GoogleEvent.UNBAN.name())) {
			final var columns = GoogleUtils.retrieveSpreadsheetColumns(event[0], GoogleEvent.UNBAN, guild);
			if(columns != null && columns.size() > 0) {
				ArrayList<List<Object>> values = new ArrayList<List<Object>>();
				for(final var column : columns) {
					GoogleDD item = column.getItem();
					switch(item) {
						case TIMESTAMP -> 			values.add(Arrays.asList(item.valueFormatter(timestamp, column.getFormatter())));
						case USER_ID -> 			values.add(Arrays.asList(item.valueFormatter(user_id, column.getFormatter())));
						case NAME ->				values.add(Arrays.asList(item.valueFormatter(name, column.getFormatter())));
						case USERNAME ->			values.add(Arrays.asList(item.valueFormatter(effectiveName, column.getFormatter())));
						case REPORTER_NAME -> 		values.add(Arrays.asList(item.valueFormatter(reporterName, column.getFormatter())));
						case REPORTER_USERNAME -> 	values.add(Arrays.asList(item.valueFormatter(reporterEffectiveName, column.getFormatter())));
						case REASON	->				values.add(Arrays.asList(item.valueFormatter(reason, column.getFormatter())));
						case ACTION ->				values.add(Arrays.asList(item.valueFormatter(GoogleEvent.UNBAN.name(), column.getFormatter())));
						case PLACEHOLDER ->			values.add(Arrays.asList(item.valueFormatter("", column.getFormatter())));
						case GUILD_ID ->			values.add(Arrays.asList(item.valueFormatter(guild.getId(), column.getFormatter())));
						case GUILD_NAME ->			values.add(Arrays.asList(item.valueFormatter(guild.getName(), column.getFormatter())));
						default -> {}
					}
				}
				final SocketTimeoutException exception = GoogleUtils.appendRawDataToSpreadsheet(guild, GoogleEvent.UNBAN, values, event[0], event[1]);
				if(exception != null && GoogleUtils.timeoutHandler(guild, event[0], GoogleEvent.UNBAN.name(), exception)) {
					spreadsheetUnbanRequest(event, guild, channel_id, user_id, timestamp, name, effectiveName, reporterName, reporterEffectiveName, reason);
				}
			}
		}
	}
	
	/**
	 * Google spreadsheet requests for bans
	 * @param event
	 * @param guild
	 * @param channel_id
	 * @param user_id
	 * @param timestamp
	 * @param name
	 * @param effectiveName
	 * @param reporterName
	 * @param reporterEffectiveName
	 * @param reason
	 * @param warning_id
	 */
	
	public static void spreadsheetBanRequest(String [] event, Guild guild, String channel_id, String user_id, Timestamp timestamp, String name, String effectiveName, String reporterName, String reporterEffectiveName, String reason, String warning_id) {
		if(GoogleUtils.spreadsheetRequestBody(event, guild, channel_id, GoogleEvent.BAN.name())) {
			final var columns = GoogleUtils.retrieveSpreadsheetColumns(event[0], GoogleEvent.BAN, guild);
			if(columns != null && columns.size() > 0) {
				ArrayList<List<Object>> values = new ArrayList<List<Object>>();
				for(final var column : columns) {
					GoogleDD item = column.getItem();
					switch(item) {
						case TIMESTAMP -> 			values.add(Arrays.asList(item.valueFormatter(timestamp, column.getFormatter())));
						case USER_ID -> 			values.add(Arrays.asList(item.valueFormatter(user_id, column.getFormatter())));
						case NAME ->				values.add(Arrays.asList(item.valueFormatter(name, column.getFormatter())));
						case USERNAME ->			values.add(Arrays.asList(item.valueFormatter(effectiveName, column.getFormatter())));
						case REPORTER_NAME -> 		values.add(Arrays.asList(item.valueFormatter(reporterName, column.getFormatter())));
						case REPORTER_USERNAME -> 	values.add(Arrays.asList(item.valueFormatter(reporterEffectiveName, column.getFormatter())));
						case REASON	->				values.add(Arrays.asList(item.valueFormatter(reason, column.getFormatter())));
						case ACTION ->				values.add(Arrays.asList(item.valueFormatter(GoogleEvent.BAN.name(), column.getFormatter())));
						case PLACEHOLDER ->			values.add(Arrays.asList(item.valueFormatter("", column.getFormatter())));
						case WARNING ->				values.add(Arrays.asList(item.valueFormatter(warning_id, column.getFormatter())));
						case GUILD_ID ->			values.add(Arrays.asList(item.valueFormatter(guild.getId(), column.getFormatter())));
						case GUILD_NAME ->			values.add(Arrays.asList(item.valueFormatter(guild.getName(), column.getFormatter())));
						default -> {}
					}
				}
				final SocketTimeoutException exception = GoogleUtils.appendRawDataToSpreadsheet(guild, GoogleEvent.BAN, values, event[0], event[1]);
				if(exception != null && GoogleUtils.timeoutHandler(guild, event[0], GoogleEvent.BAN.name(), exception)) {
					spreadsheetBanRequest(event, guild, channel_id, user_id, timestamp, name, effectiveName, reporterName, reporterEffectiveName, reason, warning_id);
				}
			}
		}
	}
	
	/**
	 * Google spreadsheet requests for user renames
	 * @param event
	 * @param guild
	 * @param channel_id
	 * @param user_id
	 * @param timestamp
	 * @param name
	 * @param reporterName
	 * @param reporterEffectiveName
	 * @param reason
	 * @param oldName
	 * @param newName
	 */
	
	public static void spreadsheetRenameRequest(String [] event, Guild guild, String channel_id, String user_id, Timestamp timestamp, String name, String reporterName, String reporterEffectiveName, String reason, String oldName, String newName) {
		if(GoogleUtils.spreadsheetRequestBody(event, guild, channel_id, GoogleEvent.RENAME.name())) {
			final var columns = GoogleUtils.retrieveSpreadsheetColumns(event[0], GoogleEvent.RENAME, guild);
			if(columns != null && columns.size() > 0) {
				ArrayList<List<Object>> values = new ArrayList<List<Object>>();
				for(final var column : columns) {
					GoogleDD item = column.getItem();
					switch(item) {
						case TIMESTAMP -> 			values.add(Arrays.asList(item.valueFormatter(timestamp, column.getFormatter())));
						case USER_ID -> 			values.add(Arrays.asList(item.valueFormatter(user_id, column.getFormatter())));
						case NAME ->				values.add(Arrays.asList(item.valueFormatter(name, column.getFormatter())));
						case REPORTER_NAME -> 		values.add(Arrays.asList(item.valueFormatter(reporterName, column.getFormatter())));
						case REPORTER_USERNAME -> 	values.add(Arrays.asList(item.valueFormatter(reporterEffectiveName, column.getFormatter())));
						case REASON	->				values.add(Arrays.asList(item.valueFormatter(reason, column.getFormatter())));
						case ACTION ->				values.add(Arrays.asList(item.valueFormatter(GoogleEvent.RENAME.name(), column.getFormatter())));
						case PLACEHOLDER ->			values.add(Arrays.asList(item.valueFormatter("", column.getFormatter())));
						case GUILD_ID ->			values.add(Arrays.asList(item.valueFormatter(guild.getId(), column.getFormatter())));
						case GUILD_NAME ->			values.add(Arrays.asList(item.valueFormatter(guild.getName(), column.getFormatter())));
						case OLD_NAME -> 			values.add(Arrays.asList(item.valueFormatter(oldName, column.getFormatter())));
						case NEW_NAME -> 			values.add(Arrays.asList(item.valueFormatter(newName, column.getFormatter())));
						default -> {}
					}
				}
				final SocketTimeoutException exception = GoogleUtils.appendRawDataToSpreadsheet(guild, GoogleEvent.RENAME, values, event[0], event[1]);
				if(exception != null && GoogleUtils.timeoutHandler(guild, event[0], GoogleEvent.RENAME.name(), exception)) {
					spreadsheetRenameRequest(event, guild, channel_id, user_id, timestamp, name, reporterName, reporterEffectiveName, reason, oldName, newName);
				}
			}
		}
	}
	
	/**
	 * Google spreadsheet requests for manual user renames
	 * @param event
	 * @param guild
	 * @param channel_id
	 * @param user_id
	 * @param timestamp
	 * @param name
	 * @param reporterName
	 * @param reporterEffectiveName
	 * @param oldName
	 * @param newName
	 */
	
	public static void spreadsheetRenameManualRequest(String [] event, Guild guild, String channel_id, String user_id, Timestamp timestamp, String name, String reporterName, String reporterEffectiveName, String oldName, String newName) {
		if(GoogleUtils.spreadsheetRequestBody(event, guild, channel_id, GoogleEvent.RENAME_MANUAL.name())) {
			final var columns = GoogleUtils.retrieveSpreadsheetColumns(event[0], GoogleEvent.RENAME_MANUAL, guild);
			if(columns != null && columns.size() > 0) {
				ArrayList<List<Object>> values = new ArrayList<List<Object>>();
				for(final var column : columns) {
					GoogleDD item = column.getItem();
					switch(item) {
						case TIMESTAMP -> 			values.add(Arrays.asList(item.valueFormatter(timestamp, column.getFormatter())));
						case USER_ID -> 			values.add(Arrays.asList(item.valueFormatter(user_id, column.getFormatter())));
						case NAME ->				values.add(Arrays.asList(item.valueFormatter(name, column.getFormatter())));
						case REPORTER_NAME -> 		values.add(Arrays.asList(item.valueFormatter(reporterName, column.getFormatter())));
						case REPORTER_USERNAME -> 	values.add(Arrays.asList(item.valueFormatter(reporterEffectiveName, column.getFormatter())));
						case ACTION ->				values.add(Arrays.asList(item.valueFormatter(GoogleEvent.RENAME_MANUAL.name(), column.getFormatter())));
						case PLACEHOLDER ->			values.add(Arrays.asList(item.valueFormatter("", column.getFormatter())));
						case GUILD_ID ->			values.add(Arrays.asList(item.valueFormatter(guild.getId(), column.getFormatter())));
						case GUILD_NAME ->			values.add(Arrays.asList(item.valueFormatter(guild.getName(), column.getFormatter())));
						case OLD_NAME -> 			values.add(Arrays.asList(item.valueFormatter(oldName, column.getFormatter())));
						case NEW_NAME -> 			values.add(Arrays.asList(item.valueFormatter(newName, column.getFormatter())));
						default -> {}
					}
				}
				final SocketTimeoutException exception = GoogleUtils.appendRawDataToSpreadsheet(guild, GoogleEvent.RENAME_MANUAL, values, event[0], event[1]);
				if(exception != null && GoogleUtils.timeoutHandler(guild, event[0], GoogleEvent.RENAME_MANUAL.name(), exception)) {
					spreadsheetRenameManualRequest(event, guild, channel_id, user_id, timestamp, name, reporterName, reporterEffectiveName, oldName, newName);
				}
			}
		}
	}
	
	/**
	 * Google spreadsheet request builder for the vote event
	 * @param event
	 * @param guild
	 * @param channel_id
	 * @param user_id
	 * @param timestamp
	 * @param name
	 * @param effectiveName
	 * @param message_id
	 * @param message
	 * @param up_vote
	 * @param down_vote
	 * @param shrug_vote
	 * @return
	 */
	
	public static ArrayList<List<Object>> spreadsheetVoteRequest(String [] event, Guild guild, String channel_id, String user_id, Timestamp timestamp, String name, String effectiveName, long message_id, String message, int up_vote, int down_vote, int shrug_vote) {
		if(GoogleUtils.spreadsheetRequestBody(event, guild, channel_id, GoogleEvent.VOTE.name())) {
			final var columns = GoogleUtils.retrieveSpreadsheetColumns(event[0], GoogleEvent.VOTE, guild);
			if(columns != null && columns.size() > 0) {
				ArrayList<List<Object>> values = new ArrayList<List<Object>>();
				List<Object> row = new ArrayList<Object>();
				for(final var column : columns) {
					GoogleDD item = column.getItem();
					switch(item) {
						case TIMESTAMP -> 			row.add(item.valueFormatter(timestamp, column.getFormatter()));
						case USER_ID -> 			row.add(item.valueFormatter(user_id, column.getFormatter()));
						case NAME ->				row.add(item.valueFormatter(name, column.getFormatter()));
						case USERNAME ->			row.add(item.valueFormatter(effectiveName, column.getFormatter()));
						case ACTION ->				row.add(item.valueFormatter(GoogleEvent.VOTE.name(), column.getFormatter()));
						case PLACEHOLDER ->			row.add(item.valueFormatter("", column.getFormatter()));
						case GUILD_ID ->			row.add(item.valueFormatter(guild.getId(), column.getFormatter()));
						case GUILD_NAME ->			row.add(item.valueFormatter(guild.getName(), column.getFormatter()));
						case MESSAGE_ID -> 			row.add(item.valueFormatter(message_id, column.getFormatter()));
						case MESSAGE ->				row.add(item.valueFormatter(message, column.getFormatter()));
						case UP_VOTE -> 			row.add(item.valueFormatter(up_vote, column.getFormatter()));
						case DOWN_VOTE -> 			row.add(item.valueFormatter(down_vote, column.getFormatter()));
						case SHRUG_VOTE -> 			row.add(item.valueFormatter(shrug_vote, column.getFormatter()));
						default -> {}
					}
				}
				values.add(row);
				if(values.get(0).size() > 0)
					return values;
			}
		}
		return null;
	}
	
	/**
	 * Google spreadsheet request builder for comments (forced channel restrictions)
	 * @param event
	 * @param guild
	 * @param channel_id
	 * @param user_id
	 * @param timestamp
	 * @param name
	 * @param effectiveName
	 * @param message_id
	 * @param message
	 * @param screenshots
	 * @return
	 */
	
	public static ArrayList<List<Object>> spreadsheetCommentRequest(String [] event, Guild guild, String channel_id, String user_id, Timestamp timestamp, String name, String effectiveName, long message_id, String message, String screenshots) {
		if(GoogleUtils.spreadsheetRequestBody(event, guild, channel_id, GoogleEvent.COMMENT.name())) {
			final var columns = GoogleUtils.retrieveSpreadsheetColumns(event[0], GoogleEvent.COMMENT, guild);
			if(columns != null && columns.size() > 0) {
				ArrayList<List<Object>> values = new ArrayList<List<Object>>();
				List<Object> row = new ArrayList<Object>();
				for(final var column : columns) {
					GoogleDD item = column.getItem();
					switch(item) {
						case TIMESTAMP -> 			row.add(item.valueFormatter(timestamp, column.getFormatter()));
						case USER_ID -> 			row.add(item.valueFormatter(user_id, column.getFormatter()));
						case NAME ->				row.add(item.valueFormatter(name, column.getFormatter()));
						case USERNAME ->			row.add(item.valueFormatter(effectiveName, column.getFormatter()));
						case ACTION ->				row.add(item.valueFormatter(GoogleEvent.COMMENT.name(), column.getFormatter()));
						case PLACEHOLDER ->			row.add(item.valueFormatter("", column.getFormatter()));
						case GUILD_ID ->			row.add(item.valueFormatter(guild.getId(), column.getFormatter()));
						case GUILD_NAME ->			row.add(item.valueFormatter(guild.getName(), column.getFormatter()));
						case MESSAGE_ID -> 			row.add(item.valueFormatter(message_id, column.getFormatter()));
						case MESSAGE ->				row.add(item.valueFormatter(message, column.getFormatter()));
						case SCREEN_URL ->			row.add(item.valueFormatter(screenshots, column.getFormatter()));
						default -> {}
					}
				}
				values.add(row);
				if(values.get(0).size() > 0)
					return values;
			}
		}
		return null;
	}
	
	/**
	 * Google spreadsheet requests for rest calls in JSON format
	 * @param event
	 * @param guild
	 * @param channel_id
	 * @param user_id
	 * @param timestamp
	 * @param ping
	 * @param member_count
	 * @param guilds_count
	 * @return
	 */
	
	public static boolean spreadsheetExportRequest(String [] event, Guild guild, String channel_id, String user_id, Timestamp timestamp, long ping, int member_count, long guilds_count) {
		if(GoogleUtils.spreadsheetRequestBody(event, guild, channel_id, GoogleEvent.EXPORT.name())) {
			final var columns = GoogleUtils.retrieveSpreadsheetColumns(event[0], GoogleEvent.EXPORT, guild);
			if(columns != null && columns.size() > 0) {
				ArrayList<List<Object>> values = new ArrayList<List<Object>>();
				for(final var column : columns) {
					GoogleDD item = column.getItem();
					switch(item) {
						case TIMESTAMP -> 			values.add(Arrays.asList(item.valueFormatter(timestamp, column.getFormatter())));
						case USER_ID -> 			values.add(Arrays.asList(item.valueFormatter(user_id, column.getFormatter())));
						case PING ->				values.add(Arrays.asList(item.valueFormatter(ping, column.getFormatter())));
						case MEMBER_COUNT ->		values.add(Arrays.asList(item.valueFormatter(member_count, column.getFormatter())));
						case GUILDS_COUNT ->		values.add(Arrays.asList(item.valueFormatter(guilds_count, column.getFormatter())));
						case ACTION ->				values.add(Arrays.asList(item.valueFormatter(GoogleEvent.EXPORT, column.getFormatter())));
						case PLACEHOLDER ->			values.add(Arrays.asList(item.valueFormatter("", column.getFormatter())));
						case GUILD_ID ->			values.add(Arrays.asList(item.valueFormatter(guild.getId(), column.getFormatter())));
						case GUILD_NAME ->			values.add(Arrays.asList(item.valueFormatter(guild.getName(), column.getFormatter())));
						default -> {}
					}
				}
				final SocketTimeoutException exception = GoogleUtils.appendRawDataToSpreadsheet(guild, GoogleEvent.EXPORT, values, event[0], event[1]);
				if(exception != null && GoogleUtils.timeoutHandler(guild, event[0], GoogleEvent.EXPORT.name(), exception)) {
					spreadsheetExportRequest(event, guild, channel_id, user_id, timestamp, ping, member_count, guilds_count);
				}
				else if(exception == null)
					return true;
			}
		}
		return false;
	}
	
	/**
	 * Google spreadsheet requests for invites created with the Invite command
	 * @param event
	 * @param guild
	 * @param channel_id
	 * @param user_id
	 * @param timestamp
	 * @param name
	 * @param effectiveName
	 * @return
	 */
	
	public static ArrayList<List<Object>> spreadsheetInvitesRequest(String [] event, Guild guild, String channel_id, String user_id, Timestamp timestamp, String name, String effectiveName, List<String> invites, Timestamp timestampUpdated) {
		if(GoogleUtils.spreadsheetRequestBody(event, guild, channel_id, GoogleEvent.INVITES.name())) {
			final var columns = GoogleUtils.retrieveSpreadsheetColumns(event[0], GoogleEvent.INVITES, guild);
			if(columns != null && columns.size() > 0) {
				ArrayList<List<Object>> values = new ArrayList<List<Object>>();
				for(final String invite : invites) {
					List<Object> row = new ArrayList<Object>();
					for(final var column : columns) {
						GoogleDD item = column.getItem();
						switch(item) {
							case TIMESTAMP -> 			row.add(item.valueFormatter(timestamp, column.getFormatter()));
							case USER_ID -> 			row.add(item.valueFormatter(user_id, column.getFormatter()));
							case NAME ->				row.add(item.valueFormatter(name, column.getFormatter()));
							case USERNAME ->			row.add(item.valueFormatter(effectiveName, column.getFormatter()));
							case ACTION ->				row.add(item.valueFormatter(GoogleEvent.INVITES.name(), column.getFormatter()));
							case PLACEHOLDER ->			row.add(item.valueFormatter("", column.getFormatter()));
							case GUILD_ID ->			row.add(item.valueFormatter(guild.getId(), column.getFormatter()));
							case GUILD_NAME ->			row.add(item.valueFormatter(guild.getName(), column.getFormatter()));
							case INVITE ->				row.add(item.valueFormatter(invite, column.getFormatter()));
							case TIMESTAMP_UPDATED -> 	row.add(item.valueFormatter(timestampUpdated, column.getFormatter()));
							default -> {}
						}
					}
					values.add(row);
				}
				if(values.get(0).size() > 0)
					return values;
			}
		}
		return null;
	}
}
