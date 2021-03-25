package de.azrael.google;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.docs.v1.DocsScopes;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import de.azrael.enums.Channel;
import de.azrael.enums.GoogleDD;
import de.azrael.enums.GoogleEvent;
import de.azrael.enums.Translation;
import de.azrael.sql.Azrael;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Utils for google docs and spreadsheets API
 * @author xHelixStorm
 *
 */

public class GoogleUtils {
	private static final Logger logger = LoggerFactory.getLogger(GoogleUtils.class);
	private static final JacksonFactory jacksonFactory = JacksonFactory.getDefaultInstance();
	private static final List<String> SCOPESDOCS = Collections.singletonList(DocsScopes.DOCUMENTS);
	private static final List<String> SCOPESSHEETS = Collections.singletonList(SheetsScopes.SPREADSHEETS);
	private static final List<String> SCOPESDRIVE = Collections.singletonList(DriveScopes.DRIVE);
	private static final List<String> SCOPESYOUTUBE = Collections.singletonList(YouTubeScopes.YOUTUBE_READONLY);
	
	private static final ConcurrentHashMap<String, Integer> timeoutRepeat = new ConcurrentHashMap<String, Integer>();
	
	/**
	 * Retrieve credentials
	 * @param httpTransport Trusted GoogleNetHttpTransport
	 * @return Credential
	 * @throws Exception File error / other errors
	 */
	
	public static HttpRequestInitializer getCredentials(final NetHttpTransport httpTransport, final String type) throws Exception {
		//use the fitting scopes
		List<String> scopes;
		switch(type) {
			case "docs" -> scopes = SCOPESDOCS;
			case "sheets" -> scopes = SCOPESSHEETS;
			case "drive" -> scopes = SCOPESDRIVE;
			case "youtube" -> scopes = SCOPESYOUTUBE;
			default -> scopes = null;
		}
		
		
		return new HttpCredentialsAdapter(GoogleCredentials.fromStream(new FileInputStream(new File("files/Google/credentials.json"))).createScoped(scopes));
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
	
	public static void handleSpreadsheetRequest(String [] event, Guild guild, String channel_id, String user_id, Timestamp timestamp, String name, String effectiveName, String reporterName, String reporterEffectiveName, String reason, String time, String warning_id, String action, Timestamp unmute_timestamp, String role_id, String role_name, String oldname, String newname, long message_id, String message, String screenshots, int up_vote, int down_vote, int event_id) {
		logger.info("Initializing spreadsheet request for event {} in guild {}", action, guild.getId());
		//If nothing has been found, don't try to write into the spreadsheet
		if(event != null && event[0].equals("empty"))
			return;
		final String file_id = event[0];
		final String sheetRowStart = event[1];
		final String forceRestriction = event[3];
		if(forceRestriction.equals("1") && (event[2] == null || !event[2].equals(channel_id)))
			return;
		if((event[2] == null || event[2].length() == 0) || event[2].equals(channel_id)) {
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
							case COMMENT -> {
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
									case SCREEN_URL ->			values.add(Arrays.asList(item.valueFormatter(screenshots, column.getFormatter())));
									default -> {}
								}
							}
							default -> {}
						}
					}
					if(values.size() > 0) {
						try {
							GoogleSheets.appendRawDataToSpreadsheet(GoogleSheets.getSheetsClientService(), file_id, values, sheetRowStart);
						} catch(SocketTimeoutException e1) {
							if(timeoutHandler(guild, file_id, action, e1)) {
								handleSpreadsheetRequest(event, guild, channel_id, user_id, timestamp, name, effectiveName, reporterName, reporterEffectiveName, reason, time, warning_id, action, unmute_timestamp, role_id, role_name, oldname, newname, message_id, message, screenshots, up_vote, down_vote, event_id);
							}
						} catch (IOException e1) {
							STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.GOOGLE_SHEET_NOT_INSERTED)), e1.getMessage(), Channel.LOG.getType());
							logger.error("Values couldn't be added into spreadsheet for file id {} and event {} in guild {}", file_id, action, guild.getId(), e1);
						} catch (Exception e1) {
							STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.GOOGLE_SHEET_NO_SERVICE)), e1.getMessage(), Channel.LOG.getType());
							logger.error("Values couldn't be added into spreadsheet on for file id {} and event {} in guild {}", file_id, action, guild.getId(), e1);
						}
					}
				}
				else if(columns.size() == 0) {
					STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_WARNING)), STATIC.getTranslation2(guild, Translation.GOOGLE_SHEET_NO_MAPPING), Channel.LOG.getType());
					logger.warn("Spreadsheet {} is not mapped for event {} in guild {}", file_id, action, guild.getId());
				}
				else {
					STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_ERROR)), STATIC.getTranslation2(guild, Translation.GENERAL_ERROR), Channel.LOG.getType());
					logger.error("Spreadsheet mapping couldn't be retrieved for file id {} and event {} in guild ", file_id, action, guild.getId());
				}
			}
			else if(sheetRowStart == null) {
				STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_ERROR)), STATIC.getTranslation2(guild, Translation.GENERAL_ERROR), Channel.LOG.getType());
				logger.error("Spreadsheet starting point couldn't be retrieved for file id {} and event {} in guild {}", file_id, action, guild.getId());
			}
			else if(sheetRowStart.isBlank()) {
				STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_WARNING)), STATIC.getTranslation2(guild, Translation.GOOGLE_SHEET_NO_START_POINT), Channel.LOG.getType());
				logger.warn("Spreadsheet starting point couldn't be found for file id {} and event {} in guild {}", file_id, action, guild.getId());
			}
			else if(file_id == null) {
				STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_ERROR)), STATIC.getTranslation2(guild, Translation.GENERAL_ERROR), Channel.LOG.getType());
				logger.error("Spreadsheet couldn't be retrieved for event {} in guild {}", action, guild.getId());
			}
			else {
				STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_WARNING)), STATIC.getTranslation2(guild, Translation.GOOGLE_SHEET_NOT_FOUND), Channel.LOG.getType());
				logger.warn("Spreadsheet couldn't be found for event {} in guild {}", action, guild.getId());
			}
		}
	}
	
	public static boolean handleSpreadsheetRequest(String [] event, Guild guild, String channel_id, String user_id, Timestamp timestamp, String name, String action, long ping, long member_count, long guilds_count, int event_id) {
		logger.info("Initializing spreadsheet request for event {} in guild {}", action, guild.getId());
		//If nothing has been found, don't try to write into the spreadsheet
		if(event != null && event[0].equals("empty"))
			return false;
		final String file_id = event[0];
		final String sheetRowStart = event[1];
		final String forceRestriction = event[3];
		if(forceRestriction.equals("1") && (event[2] == null || !event[2].equals(channel_id)))
			return false;
		if((event[2] == null || event[2].length() == 0) || event[2].equals(channel_id)) {
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
						} catch(SocketTimeoutException e1) {
							if(timeoutHandler(guild, (file_id+user_id), action, e1)) {
								handleSpreadsheetRequest(event, guild, channel_id, user_id, timestamp, name, action, ping, member_count, guilds_count, event_id);
							}
						} catch (IOException e1) {
							STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.GOOGLE_SHEET_NOT_INSERTED)), e1.getMessage(), Channel.LOG.getType());
							logger.error("Values couldn't be added into spredsheet for file id {} and event {} in guild {}", file_id, action, guild.getId(), e1);
						} catch (Exception e1) {
							STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.GOOGLE_SHEET_NO_SERVICE)), e1.getMessage(), Channel.LOG.getType());
							logger.error("Values couldn't be added into spredsheet on for file id {} and event {} in guild {}", file_id, action, guild.getId(), e1);
						}
					}
				}
				else if(columns.size() == 0) {
					STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_WARNING)), STATIC.getTranslation2(guild, Translation.GOOGLE_SHEET_NO_MAPPING), Channel.LOG.getType());
					logger.warn("Spreadsheet {} is not mapped for event {} in guild {}", file_id, action, guild.getId());
				}
				else {
					STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_ERROR)), STATIC.getTranslation2(guild, Translation.GENERAL_ERROR), Channel.LOG.getType());
					logger.error("Spreadsheet mapping couldn't be retrieved for file id {} and event {} in guild ", file_id, action, guild.getId());
				}
			}
			else if(sheetRowStart == null) {
				STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_ERROR)), STATIC.getTranslation2(guild, Translation.GENERAL_ERROR), Channel.LOG.getType());
				logger.error("Spreadsheet starting point couldn't be retrieved for file id {} and event {} in guild {}", file_id, action, guild.getId());
			}
			else if(sheetRowStart.isBlank()) {
				STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_WARNING)), STATIC.getTranslation2(guild, Translation.GOOGLE_SHEET_NO_START_POINT), Channel.LOG.getType());
				logger.warn("Spreadsheet starting point couldn't be found for file id {} and event {} in guild {}", file_id, action, guild.getId());
			}
			else if(file_id == null) {
				STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_ERROR)), STATIC.getTranslation2(guild, Translation.GENERAL_ERROR), Channel.LOG.getType());
				logger.error("Spreadsheet couldn't be retrieved for event {} in guild {}", action, guild.getId());
			}
			else {
				STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.ORANGE).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_WARNING)), STATIC.getTranslation2(guild, Translation.GOOGLE_SHEET_NOT_FOUND), Channel.LOG.getType());
				logger.warn("Spreadsheet couldn't be found for event {} in guild {}", action, guild.getId());
			}
		}
		return false;
	}
	
	public static int retrieveTimeoutAttemptCounter(String file_id) {
		if(!timeoutRepeat.containsKey(file_id)) {
			return 0;
		}
		else {
			return timeoutRepeat.get(file_id);
		}
	}
	
	public static boolean timeoutHandler(Guild guild, String file_id, String action, SocketTimeoutException e) {
		if(action == null) {
			action = "SETUP";
		}
		boolean resend = false;
		if(!timeoutRepeat.containsKey(file_id)) {
			timeoutRepeat.put(file_id, 1);
			resend = true;
		}
		else {
			final int attempts = timeoutRepeat.get(file_id);
			timeoutRepeat.put(file_id, (attempts+1));
			if(attempts <= 5)
				resend = true;
		}
		if(resend) {
			logger.warn("Timeout error attempt {}, spreadsheet service request couldn't be executed on file id {} and event {} in guild {}", timeoutRepeat.get(file_id), file_id, action, guild.getId());
			try {
				Thread.sleep(TimeUnit.SECONDS.toMillis(5));
			} catch (InterruptedException e1) {
				//assume that interruptions will never occur
			}
			return true;
		}
		else {
			logger.error("Timeout error after 5 attempts, spreadsheet service request couldn't be executed on file id {} and event {} in guild {}", file_id, action, guild.getId());
			STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.GOOGLE_SHEET_NO_SERVICE)), e.getMessage(), Channel.LOG.getType());
		}
		return false;
	}
}