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
	 * @return Number of updated rows
	 * @throws IOException
	 */
	
	public static int appendRawDataToSpreadsheet(final Sheets service, final String file_id, final List<List<Object>> list, final String rowStart) throws SocketTimeoutException, IOException {
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
	
	public static int overwriteRowOnSpreadsheet(final Sheets service, final String file_id, final List<List<Object>> list, final String rowStart) throws SocketTimeoutException, IOException {
		ValueRange values = new ValueRange().setValues(list).setMajorDimension("COLUMNS");
		UpdateValuesResponse result = service.spreadsheets().values()
			.update(file_id, rowStart, values)
			.setValueInputOption("RAW")
			.execute();
		return result.size();
	}
	
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
	
	public static void spreadsheetVoteRequest(String [] event, Guild guild, String channel_id, String user_id, Timestamp timestamp, String name, String effectiveName, long message_id, String message, int up_vote, int down_vote, int shrug_vote) {
		if(GoogleUtils.spreadsheetRequestBody(event, guild, channel_id, GoogleEvent.VOTE.name())) {
			final var columns = GoogleUtils.retrieveSpreadsheetColumns(event[0], GoogleEvent.VOTE, guild);
			if(columns != null && columns.size() > 0) {
				ArrayList<List<Object>> values = new ArrayList<List<Object>>();
				for(final var column : columns) {
					GoogleDD item = column.getItem();
					switch(item) {
						case TIMESTAMP -> 			values.add(Arrays.asList(item.valueFormatter(timestamp, column.getFormatter())));
						case USER_ID -> 			values.add(Arrays.asList(item.valueFormatter(user_id, column.getFormatter())));
						case NAME ->				values.add(Arrays.asList(item.valueFormatter(name, column.getFormatter())));
						case USERNAME ->			values.add(Arrays.asList(item.valueFormatter(effectiveName, column.getFormatter())));
						case ACTION ->				values.add(Arrays.asList(item.valueFormatter(GoogleEvent.VOTE.name(), column.getFormatter())));
						case PLACEHOLDER ->			values.add(Arrays.asList(item.valueFormatter("", column.getFormatter())));
						case GUILD_ID ->			values.add(Arrays.asList(item.valueFormatter(guild.getId(), column.getFormatter())));
						case GUILD_NAME ->			values.add(Arrays.asList(item.valueFormatter(guild.getName(), column.getFormatter())));
						case MESSAGE_ID -> 			values.add(Arrays.asList(item.valueFormatter(message_id, column.getFormatter())));
						case MESSAGE ->				values.add(Arrays.asList(item.valueFormatter(message, column.getFormatter())));
						case UP_VOTE -> 			values.add(Arrays.asList(item.valueFormatter(up_vote, column.getFormatter())));
						case DOWN_VOTE -> 			values.add(Arrays.asList(item.valueFormatter(down_vote, column.getFormatter())));
						case SHRUG_VOTE -> 			values.add(Arrays.asList(item.valueFormatter(shrug_vote, column.getFormatter())));
						default -> {}
					}
				}
				final SocketTimeoutException exception = GoogleUtils.appendRawDataToSpreadsheet(guild, GoogleEvent.VOTE, values, event[0], event[1]);
				if(exception != null && GoogleUtils.timeoutHandler(guild, event[0], GoogleEvent.VOTE.name(), exception)) {
					spreadsheetVoteRequest(event, guild, channel_id, user_id, timestamp, name, effectiveName, message_id, message, up_vote, down_vote, shrug_vote);
				}
			}
		}
	}
	
	public static void spreadsheetCommentRequest(String [] event, Guild guild, String channel_id, String user_id, Timestamp timestamp, String name, String effectiveName, long message_id, String message, String screenshots) {
		if(GoogleUtils.spreadsheetRequestBody(event, guild, channel_id, GoogleEvent.COMMENT.name())) {
			final var columns = GoogleUtils.retrieveSpreadsheetColumns(event[0], GoogleEvent.COMMENT, guild);
			if(columns != null && columns.size() > 0) {
				ArrayList<List<Object>> values = new ArrayList<List<Object>>();
				for(final var column : columns) {
					GoogleDD item = column.getItem();
					switch(item) {
						case TIMESTAMP -> 			values.add(Arrays.asList(item.valueFormatter(timestamp, column.getFormatter())));
						case USER_ID -> 			values.add(Arrays.asList(item.valueFormatter(user_id, column.getFormatter())));
						case NAME ->				values.add(Arrays.asList(item.valueFormatter(name, column.getFormatter())));
						case USERNAME ->			values.add(Arrays.asList(item.valueFormatter(effectiveName, column.getFormatter())));
						case ACTION ->				values.add(Arrays.asList(item.valueFormatter(GoogleEvent.COMMENT.name(), column.getFormatter())));
						case PLACEHOLDER ->			values.add(Arrays.asList(item.valueFormatter("", column.getFormatter())));
						case GUILD_ID ->			values.add(Arrays.asList(item.valueFormatter(guild.getId(), column.getFormatter())));
						case GUILD_NAME ->			values.add(Arrays.asList(item.valueFormatter(guild.getName(), column.getFormatter())));
						case MESSAGE_ID -> 			values.add(Arrays.asList(item.valueFormatter(message_id, column.getFormatter())));
						case MESSAGE ->				values.add(Arrays.asList(item.valueFormatter(message, column.getFormatter())));
						case SCREEN_URL ->			values.add(Arrays.asList(item.valueFormatter(screenshots, column.getFormatter())));
						default -> {}
					}
				}
				final SocketTimeoutException exception = GoogleUtils.appendRawDataToSpreadsheet(guild, GoogleEvent.COMMENT, values, event[0], event[1]);
				if(exception != null && GoogleUtils.timeoutHandler(guild, event[0], GoogleEvent.COMMENT.name(), exception)) {
					spreadsheetCommentRequest(event, guild, channel_id, user_id, timestamp, name, effectiveName, message_id, message, screenshots);
				}
			}
		}
	}
	
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
}
