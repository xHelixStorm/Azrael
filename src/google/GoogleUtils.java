package google;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.docs.v1.DocsScopes;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.sheets.v4.SheetsScopes;

import constructors.Channels;
import enums.GoogleDD;
import enums.GoogleEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import sql.Azrael;

/**
 * Utils for google docs and spreadsheets API
 * @author xHelixStorm
 *
 */

@SuppressWarnings("deprecation")
public class GoogleUtils {
	private static final Logger logger = LoggerFactory.getLogger(GoogleUtils.class);
	private static final JacksonFactory jacksonFactory = JacksonFactory.getDefaultInstance();
	private static final List<String> SCOPESDOCS = Collections.singletonList(DocsScopes.DOCUMENTS);
	private static final List<String> SCOPESSHEETS = Collections.singletonList(SheetsScopes.SPREADSHEETS);
	private static final List<String> SCOPESDRIVE = Collections.singletonList(DriveScopes.DRIVE);
	
	/**
	 * Retrieve credentials
	 * @param httpTransport Trusted GoogleNetHttpTransport
	 * @return Credential
	 * @throws Exception File error / other errors
	 */
	
	@SuppressWarnings({ "preview" })
	public static Credential getCredentials(final NetHttpTransport httpTransport, final String type) throws Exception {
		//use the fitting scopes
		List<String> scopes;
		switch(type) {
			case "docs" -> scopes = SCOPESDOCS;
			case "sheets" -> scopes = SCOPESSHEETS;
			case "drive" -> scopes = SCOPESDRIVE;
			default -> scopes = null;
		}
		
		return GoogleCredential.fromStream(new FileInputStream(new File("files/Google/credentials.json"))).createScoped(scopes);
	}
	
	/**
	 * Retrieve jackson factory
	 * @return jacksonFactory
	 */
	
	public static JacksonFactory getJacksonFactory() {
		return jacksonFactory;
	}
	
	/**
	 * Build a url depending on the api id
	 * @param file_id
	 * @param api_id
	 * @return
	 */
	
	@SuppressWarnings("preview")
	public static String buildFileURL(String file_id, int api_id) {
		return switch(api_id) {
			case 1 -> "https://docs.google.com/document/d/"+file_id;
			case 2 -> "https://docs.google.com/spreadsheets/d/"+file_id;
			default -> "";
		};
	}
	
	@SuppressWarnings({ "preview" })
	public static void handleSpreadsheetRequest(Guild guild, String user_id, Timestamp timestamp, String name, String effectiveName, String reporterName, String reporterEffectiveName, String reason, String time, String warning_id, String action, Timestamp unmute_timestamp, String role_id, String role_name, String oldname, String newname, int event_id, Channels log_channel) {
		//Retrieve the file id and row start for this event
		final String [] array = Azrael.SQLgetGoogleFilesAndEvent(guild.getIdLong(), 2, event_id);
		//If nothing has been found, don't try to write into the spreadsheet
		if(array != null && array[0].equals("empty"))
			return;
		final String file_id = array[0];
		final String sheetRowStart = array[1];
		if(file_id != null && file_id.length() > 0 && sheetRowStart != null && !sheetRowStart.isBlank()) {
			//retrieve the saved mapping for the current event
			final var columns = Azrael.SQLgetGoogleSpreadsheetMapping(file_id, event_id);
			if(columns != null && columns.size() > 0) {
				final var EVENT = GoogleEvent.valueOfId(event_id);
				//format rows to write
				ArrayList<List<Object>> values = new ArrayList<List<Object>>();
				for(final var column : columns) {
					GoogleDD item = column.getItem();
					switch(EVENT) {
						case MUTE, MUTE_READD -> {
							switch(item) {
								case TIMESTAMP -> 			values.add(Arrays.asList(item.valueFormatter(timestamp, column.getFormatter())));
								case USER_ID -> 			values.add(Arrays.asList(item.valueFormatter(user_id, column.getFormatter())));
								case NAME ->				values.add(Arrays.asList(item.valueFormatter(name, column.getFormatter())));
								case USERNAME ->			values.add(Arrays.asList(item.valueFormatter(effectiveName, column.getFormatter())));
								case REPORTER_NAME -> 		values.add(Arrays.asList(item.valueFormatter(reporterName, column.getFormatter())));
								case REPORTER_USERNAME -> 	values.add(Arrays.asList(item.valueFormatter(reporterEffectiveName, column.getFormatter())));
								case REASON	->				values.add(Arrays.asList(item.valueFormatter(reason, column.getFormatter())));
								case TIME ->				values.add(Arrays.asList(item.valueFormatter(time, column.getFormatter())));
								case ACTION ->				values.add(Arrays.asList(item.valueFormatter(action, column.getFormatter())));
								case WARNING ->				values.add(Arrays.asList(item.valueFormatter(warning_id, column.getFormatter())));
								case UNMUTE_TIME -> 		values.add(Arrays.asList(item.valueFormatter(unmute_timestamp, column.getFormatter())));
								case PLACEHOLDER ->			values.add(Arrays.asList(item.valueFormatter("", column.getFormatter())));
								case GUILD_ID ->			values.add(Arrays.asList(item.valueFormatter(guild.getId(), column.getFormatter())));
								case GUILD_NAME ->			values.add(Arrays.asList(item.valueFormatter(guild.getName(), column.getFormatter())));
								default -> {}
							}
						}
						case UNMUTE, UNMUTE_MANUAL -> {
							switch(item) {
								case TIMESTAMP -> 			values.add(Arrays.asList(item.valueFormatter(timestamp, column.getFormatter())));
								case USER_ID -> 			values.add(Arrays.asList(item.valueFormatter(user_id, column.getFormatter())));
								case NAME ->				values.add(Arrays.asList(item.valueFormatter(name, column.getFormatter())));
								case USERNAME ->			values.add(Arrays.asList(item.valueFormatter(effectiveName, column.getFormatter())));
								case REPORTER_NAME -> 		values.add(Arrays.asList(item.valueFormatter(reporterName, column.getFormatter())));
								case REPORTER_USERNAME -> 	values.add(Arrays.asList(item.valueFormatter(reporterEffectiveName, column.getFormatter())));
								case REASON	->				values.add(Arrays.asList(item.valueFormatter(reason, column.getFormatter())));
								case ACTION ->				values.add(Arrays.asList(item.valueFormatter(action, column.getFormatter())));
								case PLACEHOLDER ->			values.add(Arrays.asList(item.valueFormatter("", column.getFormatter())));
								case GUILD_ID ->			values.add(Arrays.asList(item.valueFormatter(guild.getId(), column.getFormatter())));
								case GUILD_NAME ->			values.add(Arrays.asList(item.valueFormatter(guild.getName(), column.getFormatter())));
								case ROLE_ID ->				values.add(Arrays.asList(item.valueFormatter(role_id, column.getFormatter())));
								case ROLE_NAME ->			values.add(Arrays.asList(item.valueFormatter(role_name, column.getFormatter())));
								default -> {}
							}
						}
						case KICK, UNBAN -> {
							switch(item) {
								case TIMESTAMP -> 			values.add(Arrays.asList(item.valueFormatter(timestamp, column.getFormatter())));
								case USER_ID -> 			values.add(Arrays.asList(item.valueFormatter(user_id, column.getFormatter())));
								case NAME ->				values.add(Arrays.asList(item.valueFormatter(name, column.getFormatter())));
								case USERNAME ->			values.add(Arrays.asList(item.valueFormatter(effectiveName, column.getFormatter())));
								case REPORTER_NAME -> 		values.add(Arrays.asList(item.valueFormatter(reporterName, column.getFormatter())));
								case REPORTER_USERNAME -> 	values.add(Arrays.asList(item.valueFormatter(reporterEffectiveName, column.getFormatter())));
								case REASON	->				values.add(Arrays.asList(item.valueFormatter(reason, column.getFormatter())));
								case ACTION ->				values.add(Arrays.asList(item.valueFormatter(action, column.getFormatter())));
								case PLACEHOLDER ->			values.add(Arrays.asList(item.valueFormatter("", column.getFormatter())));
								case GUILD_ID ->			values.add(Arrays.asList(item.valueFormatter(guild.getId(), column.getFormatter())));
								case GUILD_NAME ->			values.add(Arrays.asList(item.valueFormatter(guild.getName(), column.getFormatter())));
								default -> {}
							}
						}
						case BAN -> {
							switch(item) {
								case TIMESTAMP -> 			values.add(Arrays.asList(item.valueFormatter(timestamp, column.getFormatter())));
								case USER_ID -> 			values.add(Arrays.asList(item.valueFormatter(user_id, column.getFormatter())));
								case NAME ->				values.add(Arrays.asList(item.valueFormatter(name, column.getFormatter())));
								case USERNAME ->			values.add(Arrays.asList(item.valueFormatter(effectiveName, column.getFormatter())));
								case REPORTER_NAME -> 		values.add(Arrays.asList(item.valueFormatter(reporterName, column.getFormatter())));
								case REPORTER_USERNAME -> 	values.add(Arrays.asList(item.valueFormatter(reporterEffectiveName, column.getFormatter())));
								case REASON	->				values.add(Arrays.asList(item.valueFormatter(reason, column.getFormatter())));
								case ACTION ->				values.add(Arrays.asList(item.valueFormatter(action, column.getFormatter())));
								case WARNING ->				values.add(Arrays.asList(item.valueFormatter(warning_id, column.getFormatter())));
								case PLACEHOLDER ->			values.add(Arrays.asList(item.valueFormatter("", column.getFormatter())));
								case GUILD_ID ->			values.add(Arrays.asList(item.valueFormatter(guild.getId(), column.getFormatter())));
								case GUILD_NAME ->			values.add(Arrays.asList(item.valueFormatter(guild.getName(), column.getFormatter())));
								default -> {}
							}
						}
						case RENAME -> {
								switch(item) {
								case TIMESTAMP -> 			values.add(Arrays.asList(item.valueFormatter(timestamp, column.getFormatter())));
								case USER_ID -> 			values.add(Arrays.asList(item.valueFormatter(user_id, column.getFormatter())));
								case NAME ->				values.add(Arrays.asList(item.valueFormatter(name, column.getFormatter())));
								case REPORTER_NAME -> 		values.add(Arrays.asList(item.valueFormatter(reporterName, column.getFormatter())));
								case REPORTER_USERNAME -> 	values.add(Arrays.asList(item.valueFormatter(reporterEffectiveName, column.getFormatter())));
								case REASON	->				values.add(Arrays.asList(item.valueFormatter(reason, column.getFormatter())));
								case ACTION ->				values.add(Arrays.asList(item.valueFormatter(action, column.getFormatter())));
								case PLACEHOLDER ->			values.add(Arrays.asList(item.valueFormatter("", column.getFormatter())));
								case GUILD_ID ->			values.add(Arrays.asList(item.valueFormatter(guild.getId(), column.getFormatter())));
								case GUILD_NAME ->			values.add(Arrays.asList(item.valueFormatter(guild.getName(), column.getFormatter())));
								case OLD_NAME -> 			values.add(Arrays.asList(item.valueFormatter(oldname, column.getFormatter())));
								case NEW_NAME -> 			values.add(Arrays.asList(item.valueFormatter(newname, column.getFormatter())));
								default -> {}
							}
						}
						case RENAME_MANUAL -> {
							switch(item) {
								case TIMESTAMP -> 			values.add(Arrays.asList(item.valueFormatter(timestamp, column.getFormatter())));
								case USER_ID -> 			values.add(Arrays.asList(item.valueFormatter(user_id, column.getFormatter())));
								case NAME ->				values.add(Arrays.asList(item.valueFormatter(name, column.getFormatter())));
								case REPORTER_NAME -> 		values.add(Arrays.asList(item.valueFormatter(reporterName, column.getFormatter())));
								case REPORTER_USERNAME -> 	values.add(Arrays.asList(item.valueFormatter(reporterEffectiveName, column.getFormatter())));
								case ACTION ->				values.add(Arrays.asList(item.valueFormatter(action, column.getFormatter())));
								case PLACEHOLDER ->			values.add(Arrays.asList(item.valueFormatter("", column.getFormatter())));
								case GUILD_ID ->			values.add(Arrays.asList(item.valueFormatter(guild.getId(), column.getFormatter())));
								case GUILD_NAME ->			values.add(Arrays.asList(item.valueFormatter(guild.getName(), column.getFormatter())));
								case OLD_NAME -> 			values.add(Arrays.asList(item.valueFormatter(oldname, column.getFormatter())));
								case NEW_NAME -> 			values.add(Arrays.asList(item.valueFormatter(newname, column.getFormatter())));
								default -> {}
							}
						}
						default -> {}
					}
				}
				try {
					GoogleSheets.appendRawDataToSpreadsheet(GoogleSheets.getSheetsClientService(), file_id, values, sheetRowStart);
				} catch (IOException e1) {
					if(log_channel != null) guild.getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Row couldn't be inserted into the Spreadsheet!").setDescription(e1.getMessage()).build()).queue();
					logger.error("Values couldn't be added into spredsheet on Mute for file id {} in guild {}", file_id, guild.getId(), e1);
				} catch (Exception e1) {
					if(log_channel != null) guild.getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Spreadsheet service couldn't be retrieved!").setDescription(e1.getMessage()).build()).queue();
					logger.error("Values couldn't be added into spredsheet on Mute for file id {} in guild {}", file_id, guild.getId(), e1);
				}
			}
			else if(columns.size() == 0) {
				if(log_channel != null) guild.getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.ORANGE).setTitle("Warning!").setDescription("No column mapping has been found!").build()).queue();
				logger.warn("Mute spreadsheet {} is not mapped for event id 1 in guild {}", file_id, guild.getId());
			}
			else {
				if(log_channel != null) guild.getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Error!").setDescription("An internal error occurred! The columns mapping couldn't be retrieved!").build()).queue();
				logger.error("Mapping couldn't be retrieved from Azrael.google_spreadsheet_mapping for file id {}, event id 1 and guild ", file_id, guild.getId());
			}
		}
		else if(sheetRowStart == null) {
			if(log_channel != null) guild.getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Error!").setDescription("An internal error occurred! Mute Spreadsheet couldn't be retrieved!").build()).queue();
			logger.error("Spreadsheet starting point couldn't be retrieved from google_files_and_events for event id 1 and guild {}", guild.getId());
		}
		else if(sheetRowStart.isBlank()) {
			if(log_channel != null) guild.getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.ORANGE).setTitle("Warning!").setDescription("No Spreadsheet starting point couldn't be found for this event!").build()).queue();
			logger.warn("Spreadsheet starting point couldn't be found for event id 1 and guild {}", guild.getId());
		}
		else if(file_id == null) {
			if(log_channel != null) guild.getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Error!").setDescription("An internal error occurred! Mute Spreadsheet couldn't be retrieved!").build()).queue();
			logger.error("Mute spreadsheet couldn't be retrieved from google_files_and_events for event id 1 and guild {}", guild.getId());
		}
		else {
			if(log_channel != null) guild.getTextChannelById(log_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.ORANGE).setTitle("Warning!").setDescription("No spreadsheet has been found with the mute event!").build()).queue();
			logger.warn("Mute spreadsheet couldn't be found for event id 1 and guild {}", guild.getId());
		}
	}
}
