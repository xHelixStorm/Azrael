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

import enums.Channel;
import enums.GoogleDD;
import enums.GoogleEvent;
import enums.Translation;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import sql.Azrael;
import util.STATIC;

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
	
	public static String buildFileURL(String file_id, int api_id) {
		return switch(api_id) {
			case 1 -> "https://docs.google.com/document/d/"+file_id;
			case 2 -> "https://docs.google.com/spreadsheets/d/"+file_id;
			default -> "";
		};
	}
	
	public static void handleSpreadsheetRequest(Guild guild, String channel_id, String user_id, Timestamp timestamp, String name, String effectiveName, String reporterName, String reporterEffectiveName, String reason, String time, String warning_id, String action, Timestamp unmute_timestamp, String role_id, String role_name, String oldname, String newname, long message_id, String message, int up_vote, int down_vote, int event_id) {
		logger.debug("Initializing spreadsheet request for event {} in guild {}", action, guild.getId());
		//Retrieve the file id and row start for this event
		final String [] array = Azrael.SQLgetGoogleFilesAndEvent(guild.getIdLong(), 2, event_id, channel_id);
		//If nothing has been found, don't try to write into the spreadsheet
		if(array != null && array[0].equals("empty"))
			return;
		final String file_id = array[0];
		final String sheetRowStart = array[1];
		if((array[2] == null || array[2].length() == 0) || array[2].equals(channel_id)) {
			if(file_id != null && file_id.length() > 0 && sheetRowStart != null && !sheetRowStart.isBlank()) {
				//retrieve the saved mapping for the current event
				final var columns = Azrael.SQLgetGoogleSpreadsheetMapping(file_id, event_id, guild.getIdLong());
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
							case VOTE -> {
								switch(item) {
									case TIMESTAMP -> 			values.add(Arrays.asList(item.valueFormatter(timestamp, column.getFormatter())));
									case USER_ID -> 			values.add(Arrays.asList(item.valueFormatter(user_id, column.getFormatter())));
									case NAME ->				values.add(Arrays.asList(item.valueFormatter(name, column.getFormatter())));
									case USERNAME ->			values.add(Arrays.asList(item.valueFormatter(effectiveName, column.getFormatter())));
									case ACTION ->				values.add(Arrays.asList(item.valueFormatter(action, column.getFormatter())));
									case PLACEHOLDER ->			values.add(Arrays.asList(item.valueFormatter("", column.getFormatter())));
									case GUILD_ID ->			values.add(Arrays.asList(item.valueFormatter(guild.getId(), column.getFormatter())));
									case GUILD_NAME ->			values.add(Arrays.asList(item.valueFormatter(guild.getName(), column.getFormatter())));
									case MESSAGE_ID -> 			values.add(Arrays.asList(item.valueFormatter(message_id, column.getFormatter())));
									case MESSAGE ->				values.add(Arrays.asList(item.valueFormatter(message, column.getFormatter())));
									case UP_VOTE -> 			values.add(Arrays.asList(item.valueFormatter(up_vote, column.getFormatter())));
									case DOWN_VOTE -> 			values.add(Arrays.asList(item.valueFormatter(down_vote, column.getFormatter())));
									default -> {}
								}
							}
							default -> {}
						}
					}
					if(values.size() > 0) {
						try {
							GoogleSheets.appendRawDataToSpreadsheet(GoogleSheets.getSheetsClientService(), file_id, values, sheetRowStart);
						} catch (IOException e1) {
							STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.GOOGLE_SHEET_NOT_INSERTED)), e1.getMessage(), Channel.LOG.getType());
							logger.error("Values couldn't be added into spredsheet on Mute for file id {} in guild {}", file_id, guild.getId(), e1);
						} catch (Exception e1) {
							STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.GOOGLE_SHEET_NO_SERVICE)), e1.getMessage(), Channel.LOG.getType());
							logger.error("Values couldn't be added into spredsheet on Mute for file id {} in guild {}", file_id, guild.getId(), e1);
						}
					}
				}
				else if(columns.size() == 0) {
					STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_WARNING)), STATIC.getTranslation2(guild, Translation.GOOGLE_SHEET_NO_MAPPING), Channel.LOG.getType());
					logger.warn("Mute spreadsheet {} is not mapped in guild {}", file_id, guild.getId());
				}
				else {
					STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_ERROR)), STATIC.getTranslation2(guild, Translation.GENERAL_ERROR), Channel.LOG.getType());
					logger.error("Mapping couldn't be retrieved from Azrael.google_spreadsheet_mapping for file id {} in guild ", file_id, guild.getId());
				}
			}
			else if(sheetRowStart == null) {
				STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_ERROR)), STATIC.getTranslation2(guild, Translation.GENERAL_ERROR), Channel.LOG.getType());
				logger.error("Spreadsheet starting point couldn't be retrieved from google_files_and_events in guild {}", guild.getId());
			}
			else if(sheetRowStart.isBlank()) {
				STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_WARNING)), STATIC.getTranslation2(guild, Translation.GOOGLE_SHEET_NO_START_POINT), Channel.LOG.getType());
				logger.warn("Spreadsheet starting point couldn't be found in guild {}", guild.getId());
			}
			else if(file_id == null) {
				STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_ERROR)), STATIC.getTranslation2(guild, Translation.GENERAL_ERROR), Channel.LOG.getType());
				logger.error("Spreadsheet couldn't be retrieved from google_files_and_events in guild {}", guild.getId());
			}
			else {
				STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_WARNING)), STATIC.getTranslation2(guild, Translation.GOOGLE_SHEET_NOT_FOUND), Channel.LOG.getType());
				logger.warn("Spreadsheet couldn't be found in guild {}", guild.getId());
			}
		}
	}
	
	public static boolean handleSpreadsheetRequest(Guild guild, String channel_id, String user_id, Timestamp timestamp, String name, String action, long ping, long member_count, long guilds_count, int event_id) {
		logger.debug("Initializing spreadsheet request for event {} in guild {}", action, guild.getId());
		//Retrieve the file id and row start for this event
		final String [] array = Azrael.SQLgetGoogleFilesAndEvent(guild.getIdLong(), 2, event_id, channel_id);
		//If nothing has been found, don't try to write into the spreadsheet
		if(array != null && array[0].equals("empty"))
			return false;
		final String file_id = array[0];
		final String sheetRowStart = array[1];
		if((array[2] == null || array[2].length() == 0) || array[2].equals(channel_id)) {
			if(file_id != null && file_id.length() > 0 && sheetRowStart != null && !sheetRowStart.isBlank()) {
				//retrieve the saved mapping for the current event
				final var columns = Azrael.SQLgetGoogleSpreadsheetMapping(file_id, event_id, guild.getIdLong());
				if(columns != null && columns.size() > 0) {
					final var EVENT = GoogleEvent.valueOfId(event_id);
					//format rows to write
					ArrayList<List<Object>> values = new ArrayList<List<Object>>();
					for(final var column : columns) {
						GoogleDD item = column.getItem();
						switch(EVENT) {
							case EXPORT -> {
								switch(item) {
									case TIMESTAMP -> 			values.add(Arrays.asList(item.valueFormatter(timestamp, column.getFormatter())));
									case USER_ID -> 			values.add(Arrays.asList(item.valueFormatter(user_id, column.getFormatter())));
									case NAME ->				values.add(Arrays.asList(item.valueFormatter(name, column.getFormatter())));
									case PING ->				values.add(Arrays.asList(item.valueFormatter(ping, column.getFormatter())));
									case MEMBER_COUNT ->		values.add(Arrays.asList(item.valueFormatter(member_count, column.getFormatter())));
									case GUILDS_COUNT ->		values.add(Arrays.asList(item.valueFormatter(guilds_count, column.getFormatter())));
									case ACTION ->				values.add(Arrays.asList(item.valueFormatter(action, column.getFormatter())));
									case PLACEHOLDER ->			values.add(Arrays.asList(item.valueFormatter("", column.getFormatter())));
									case GUILD_ID ->			values.add(Arrays.asList(item.valueFormatter(guild.getId(), column.getFormatter())));
									case GUILD_NAME ->			values.add(Arrays.asList(item.valueFormatter(guild.getName(), column.getFormatter())));
									default -> {}
								}
							}
							default -> {}
						}
					}
					if(values.size() > 0) {
						try {
							GoogleSheets.appendRawDataToSpreadsheet(GoogleSheets.getSheetsClientService(), file_id, values, sheetRowStart);
							return true;
						} catch (IOException e1) {
							STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.GOOGLE_SHEET_NOT_INSERTED)), e1.getMessage(), Channel.LOG.getType());
							logger.error("Values couldn't be added into spredsheet on Mute for file id {} in guild {}", file_id, guild.getId(), e1);
						} catch (Exception e1) {
							STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.GOOGLE_SHEET_NO_SERVICE)), e1.getMessage(), Channel.LOG.getType());
							logger.error("Values couldn't be added into spredsheet on Mute for file id {} in guild {}", file_id, guild.getId(), e1);
						}
					}
				}
				else if(columns.size() == 0) {
					STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_WARNING)), STATIC.getTranslation2(guild, Translation.GOOGLE_SHEET_NO_MAPPING), Channel.LOG.getType());
					logger.warn("Mute spreadsheet {} is not mapped in guild {}", file_id, guild.getId());
				}
				else {
					STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_ERROR)), STATIC.getTranslation2(guild, Translation.GENERAL_ERROR), Channel.LOG.getType());
					logger.error("Mapping couldn't be retrieved from Azrael.google_spreadsheet_mapping for file id {} in guild ", file_id, guild.getId());
				}
			}
			else if(sheetRowStart == null) {
				STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_ERROR)), STATIC.getTranslation2(guild, Translation.GENERAL_ERROR), Channel.LOG.getType());
				logger.error("Spreadsheet starting point couldn't be retrieved from google_files_and_events in guild {}", guild.getId());
			}
			else if(sheetRowStart.isBlank()) {
				STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_WARNING)), STATIC.getTranslation2(guild, Translation.GOOGLE_SHEET_NO_START_POINT), Channel.LOG.getType());
				logger.warn("Spreadsheet starting point couldn't be found in guild {}", guild.getId());
			}
			else if(file_id == null) {
				STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_ERROR)), STATIC.getTranslation2(guild, Translation.GENERAL_ERROR), Channel.LOG.getType());
				logger.error("Spreadsheet couldn't be retrieved from google_files_and_events in guild {}", guild.getId());
			}
			else {
				STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_WARNING)), STATIC.getTranslation2(guild, Translation.GOOGLE_SHEET_NOT_FOUND), Channel.LOG.getType());
				logger.warn("Spreadsheet couldn't be found in guild {}", guild.getId());
			}
		}
		return false;
	}
}
