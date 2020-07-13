package commandsContainer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.sheets.v4.model.Spreadsheet;

import constructors.Cache;
import constructors.GoogleAPISetup;
import core.Hashes;
import enums.GoogleDD;
import enums.GoogleEvent;
import enums.Translation;
import fileManagement.GuildIni;
import google.GoogleDrive;
import google.GoogleSheets;
import google.GoogleUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;
import util.STATIC;

/**
 * Extension of the google spreadsheets command
 * @author xHelixStorm
 *
 */

public class GoogleSpreadsheetsExecution {
	private final static Logger logger = LoggerFactory.getLogger(GoogleSpreadsheetsExecution.class);
	private final static EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);

	public static void runTask(GuildMessageReceivedEvent e, final String key) {
		StringBuilder out = new StringBuilder();
		final var spreadsheets = Azrael.SQLgetGoogleAPISetupOnGuildAndAPI(e.getGuild().getIdLong(), 2);
		final String NA = STATIC.getTranslation(e.getMember(), Translation.NOT_AVAILABLE);
		if(spreadsheets == null)
			logger.error("Data couldn't be retrieved from Azrael.google_api_setup in guild {}", e.getGuild().getId());
		else if(spreadsheets.size() == 0)
			out.append("**"+NA+"**");
		else {
			for(final var spreadsheet : spreadsheets) {
				out.append("**"+GoogleUtils.buildFileURL(spreadsheet.getFileID(), 2)+"**\n");
			}
		}
		e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_HELP)+out.toString()).build()).queue();
		Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
	}
	
	public static void create(GuildMessageReceivedEvent e, String title, final String key) {
		if(title == null) {
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_CREATE)).build()).queue();
		}
		else {
			//create and transfer ownership only if an email has been provided
			final String email = GuildIni.getGoogleMainEmail(e.getGuild().getIdLong());
			if(email != null && email.length() > 0) {
				String file_id = "";
				String processStep = "";
				boolean err = false;
				try {
					processStep = "Spreadsheet Creation";
					file_id = GoogleSheets.createSpreadsheet(GoogleSheets.getSheetsClientService(), title);
					Azrael.SQLInsertActionLog("GOOGLE_SPREADSHEET_CREATE", 0, e.getGuild().getIdLong(), file_id);
					processStep = "Ownership Transfer";
					Azrael.SQLInsertActionLog("GOOGLE_SPREADSHEET_OWNERSHIP", 0, e.getGuild().getIdLong(), email);
					GoogleDrive.transferOwnerOfFile(GoogleDrive.getDriveClientService(), file_id, email);
				} catch (Exception e1) {
					logger.error("An error occurred on process step \"{}\"", processStep, e1);
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_WEBSERVICE)+e1.getMessage()).build()).queue();
					err = true;
				}
				
				if(!err) {
					if(Azrael.SQLInsertGoogleAPISetup(file_id, e.getGuild().getIdLong(), title, 2) > 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_CREATED)+file_id).build()).queue();
						logger.debug("Spreadsheet with the file id {} has been created from guild {}", file_id, e.getGuild().getIdLong());
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_CREATED_2)+file_id).build()).queue();
						logger.warn("Spreadsheet with the file id {} has been created from guild {} but without linkage", file_id, e.getGuild().getIdLong());
					}
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_EMAIL)).build()).queue();
			}
		}
		Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
	}
	
	public static void add(GuildMessageReceivedEvent e, String file_id, final String key) {
		if(file_id == null) {
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_ADD_HELP)).build()).queue();
		}
		else if(file_id != null && file_id.length() > 0) {
			Matcher matcher = Pattern.compile("[-\\w]{25,}").matcher(file_id);
			if(matcher.find()) {
				file_id = matcher.group();
				Spreadsheet spreadsheet = null;
				boolean err = false;
				try {
					spreadsheet = GoogleSheets.getSpreadsheet(GoogleSheets.getSheetsClientService(), file_id);
				}catch (Exception e1) {
					logger.error("An error occurred on process step \"Retrieve Spreadsheet\"", e1);
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_WEBSERVICE)+e1.getMessage()).build()).queue();
					err = true;
				}
				
				if(!err) {
					if(Azrael.SQLInsertGoogleAPISetup(spreadsheet.getSpreadsheetId(), e.getGuild().getIdLong(), spreadsheet.getProperties().getTitle(), 2) > 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_ADDED)).build()).queue();
						logger.debug("Spreadsheet with the file id {} has been linked to the bot in guild {}", file_id, e.getGuild().getIdLong());
						Azrael.SQLInsertActionLog("GOOGLE_SPREADSHEET_LINK", 0, e.getGuild().getIdLong(), file_id);
						Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription("An internal error has occurred. Google Spreadsheet couldn't be linked to the bot.").build()).queue();
						logger.error("Spreadsheet with the file id {} couldn't be linked to the bot in guild {}");
						Hashes.clearTempCache(key);
					}
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_URL)).build()).queue();
				Hashes.clearTempCache(key);
			}
		}
	}
	
	public static void remove(GuildMessageReceivedEvent e, String file_id, final String key) {
		if(file_id == null) {
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_REMOVE_HELP)).build()).queue();
		}
		else if(file_id != null && file_id.length() > 0) {
			Matcher matcher = Pattern.compile("[-\\w]{25,}").matcher(file_id);
			if(matcher.find()) {
				file_id = matcher.group();
				Spreadsheet spreadsheet = null;
				boolean err = false;
				try {
					spreadsheet = GoogleSheets.getSpreadsheet(GoogleSheets.getSheetsClientService(), file_id);
				}catch (Exception e1) {
					logger.error("An error occurred on process step \"Retrieve Spreadsheet\"", e1);
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_WEBSERVICE)+e1.getMessage()).build()).queue();
					err = true;
				}
				
				if(!err) {
					Azrael.SQLDeleteGoogleSpreadsheetSheet(spreadsheet.getSpreadsheetId(), e.getGuild().getIdLong());
					Azrael.SQLDeleteGoogleSpreadsheetMapping(spreadsheet.getSpreadsheetId(), e.getGuild().getIdLong());
					Azrael.SQLDeleteGoogleFileToEvent(spreadsheet.getSpreadsheetId(), e.getGuild().getIdLong());
					if(Azrael.SQLDeleteGoogleAPISetup(spreadsheet.getSpreadsheetId(), e.getGuild().getIdLong()) > 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_REMOVED)).build()).queue();
						logger.debug("Link of the spreadsheet with the file id {} has been severed from the bot in guild {}", file_id, e.getGuild().getIdLong());
						Azrael.SQLInsertActionLog("GOOGLE_SPREADSHEET_UNLINK", 0, e.getGuild().getIdLong(), file_id);
						Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Link of the spreadsheet with the file id {} couldn't be severed from the bot in guild {}");
						Hashes.clearTempCache(key);
					}
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_URL)).build()).queue();
				Hashes.clearTempCache(key);
			}
		}
	}
	
	public static void events(GuildMessageReceivedEvent e, final String key) {
		var setup = Azrael.SQLgetGoogleAPISetupOnGuildAndAPI(e.getGuild().getIdLong(), 2);
		if(setup == null) {
			//db error
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription("An internal error has occurred! Spreadsheets from database couldn't be retrieved. Returning to the spreadsheets parameters selection page...").build()).queue();
			logger.error("Data from Azrael.google_api_setup_view couldn't be retrieved for guild {}", e.getGuild().getId());
			Hashes.clearTempCache(key);
		}
		else if(setup.size() > 0) {
			//list files
			StringBuilder out = new StringBuilder();
			for(int i = 0; i < setup.size(); i++) {
				final GoogleAPISetup currentSetup = setup.get(i);
				out.append("**"+(i+1)+": "+currentSetup.getTitle()+"**\n"+GoogleUtils.buildFileURL(currentSetup.getFileID(), currentSetup.getApiID())+"\n\n");
			}
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_EVENT_HELP)+out.toString()).build()).queue();
			Hashes.addTempCache(key, new Cache(180000, "spreadsheets-events"));
		}
		else {
			//nothing found information
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_NO_FILE)).build()).queue();
			Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
		}
	}
	
	public static void eventsFileSelection(GuildMessageReceivedEvent e, int selection, final String key) {
		final var setup = Azrael.SQLgetGoogleAPISetupOnGuildAndAPI(e.getGuild().getIdLong(), 2);
		if(setup == null) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Data from Azrael.google_api_setup_view couldn't be retrieved for guild {}", e.getGuild().getId());
			Hashes.clearTempCache(key);
		}
		else if(selection >= 0 && selection < setup.size()) {
			final var events = Azrael.SQLgetGoogleEventsSupportSpreadsheet();
			if(events == null) {
				//db error
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_EVENT_HELP)).build()).queue();
				logger.error("Data from Azrael.google_events couldn't be retrieved for guild {}", e.getGuild().getId());
				Hashes.clearTempCache(key);
			}
			else if(events.size() > 0) {
				//list events
				final String NA = STATIC.getTranslation(e.getMember(), Translation.NOT_AVAILABLE);
				final var file = setup.get(selection);
				StringBuilder out = new StringBuilder();
				for(int i = 0; i<events.size(); i++) {
					final var event = events.get(i);
					if(i == 0)
						out.append("`"+event.getEvent()+"`");
					else
						out.append(", `"+event.getEvent()+"`");
				}
				
				final var registeredEvents = Azrael.SQLgetGoogleLinkedEvents(file.getFileID(), e.getGuild().getIdLong());
				StringBuilder out2 = new StringBuilder();
				int count = 0;
				for(final int event_id : registeredEvents) {
					if(count == 0)
						out2.append("`"+GoogleEvent.valueOfId(event_id).name()+"`");
					else
						out2.append(", `"+GoogleEvent.valueOfId(event_id).name()+"`");
					count++; 
				}
				if(registeredEvents.size() == 0)
					out2.append(NA);
				
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_EVENTS_ADD)+out.toString()+"\n"+STATIC.getTranslation(e.getMember(), Translation.GOOGLE_EVENTS)+out2.toString()).build()).queue();
				Hashes.addTempCache(key, new Cache(180000, "spreadsheets-events-update", file.getFileID()));
			}
			else {
				//nothing found information
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_NO_EVENTS)).build()).queue();
				logger.error("Google spreadsheets events are not defined!");
				Hashes.clearTempCache(key);
			}
		}
	}
	
	public static void eventsFileHandler(GuildMessageReceivedEvent e, String userMessage, String file_id, final String key) {
		boolean addEvents;
		String [] events;
		if(userMessage.startsWith(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD))) {
			userMessage = userMessage.substring(4);
			events = userMessage.split(",");
			addEvents = true;
		}
		else {
			userMessage = userMessage.substring(7);
			events = userMessage.split(",");
			addEvents = false;
		}
		
		final var fixedEvents = Azrael.SQLgetGoogleEventsSupportSpreadsheet();
		ArrayList<Integer> handleEvents = new ArrayList<Integer>();
		for(int i = 0; i < events.length; i++) {
			final int iterator = i;
			final var event = fixedEvents.parallelStream().filter(f -> f.getEvent().equalsIgnoreCase(events[iterator].trim())).findAny().orElse(null);
			if(event == null)
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_NOT_EVENT).replace("{}", events[iterator].trim())).build()).queue();
			else
				handleEvents.add(event.getEventID());
		}
		if(handleEvents.size() > 0) {
			if(addEvents) {
				if(Azrael.SQLBatchInsertGoogleFileToEventLink(file_id, e.getGuild().getIdLong(), handleEvents)) {
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_EVENTS_ADDED)).build()).queue();
					logger.debug("Events added for file_id {} in guild {}", file_id, e.getGuild().getId());
					Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
				}
			}
			else {
				if(Azrael.SQLBatchDeleteGoogleSpreadsheetSheet(file_id, e.getGuild().getIdLong(), handleEvents)) {
					logger.debug("Events removed from Azrael.google_spreadsheetsheet for file_id {} in guild {}", file_id, e.getGuild().getId());
					if(Azrael.SQLBatchDeleteGoogleSpreadsheetMapping(file_id, e.getGuild().getIdLong(), handleEvents)) {
						logger.debug("Events removed from Azrael.google_spreadsheet_mapping for file_id {} in guild {}", file_id, e.getGuild().getId());
						if(Azrael.SQLBatchDeleteGoogleFileToEvent(file_id, e.getGuild().getIdLong(), handleEvents)) {
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_EVENTS_REMOVED)).build()).queue();
							logger.debug("Events removed from Azrael.google_file_to_event for file_id {} in guild {}", file_id, e.getGuild().getId());
							Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("Events couldn't be removed from Azrael.google_file_to_event with file_id {} in guild {}", file_id, e.getGuild().getId());
							Hashes.clearTempCache(key);
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Events couldn't be removed from Azrael.google_spreadsheet_mapping with file_id {} in guild {}", file_id, e.getGuild().getId());
						Hashes.clearTempCache(key);
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Events couldn't be removed from Azrael.google_spreadsheet_sheet with file_id {} in guild {}", file_id, e.getGuild().getId());
					Hashes.clearTempCache(key);
				}
			}
		}
	}
	
	public static void sheet(GuildMessageReceivedEvent e, final String key) {
		var setup = Azrael.SQLgetGoogleAPISetupOnGuildAndAPI(e.getGuild().getIdLong(), 2);
		if(setup == null) {
			//db error
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Data from Azrael.google_api_setup_view couldn't be retrieved for guild {}", e.getGuild().getId());
			Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
		}
		else if(setup.size() > 0) {
			//list files
			StringBuilder out = new StringBuilder();
			for(int i = 0; i < setup.size(); i++) {
				final GoogleAPISetup currentSetup = setup.get(i);
				out.append("**"+(i+1)+": "+currentSetup.getTitle()+"**\n"+GoogleUtils.buildFileURL(currentSetup.getFileID(), currentSetup.getApiID())+"\n\n");
			}
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_START_HELP)+out.toString()).build()).queue();
			Hashes.addTempCache(key, new Cache(180000, "spreadsheets-sheet"));
		}
		else {
			//nothing found information
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_NO_FILE)).build()).queue();
			Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
		}
	}
	
	public static void sheetSelection(GuildMessageReceivedEvent e, int selection, final String key) {
		final var setup = Azrael.SQLgetGoogleAPISetupOnGuildAndAPI(e.getGuild().getIdLong(), 2);
		if(setup == null) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Data from Azrael.google_api_setup_view couldn't be retrieved for guild {}", e.getGuild().getId());
			Hashes.clearTempCache(key);
		}
		else if(selection >= 0 && selection < setup.size()) {
			final var file = setup.get(selection);
			final var events = Azrael.SQLgetGoogleLinkedEvents(file.getFileID(), e.getGuild().getIdLong());
			if(events == null) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Linked google events couldn't be retrieved from Azrael.google_file_to_event for spreadsheet id {}", file.getFileID());
				Hashes.clearTempCache(key);
			}
			else if(events.size() > 0) {
				final String NA = STATIC.getTranslation(e.getMember(), Translation.NOT_AVAILABLE);
				StringBuilder out = new StringBuilder();
				int count = 0;
				for(final int event : events) {
					if(count == 0)
						out.append("`"+GoogleEvent.valueOfId(event).value+"`");
					else
						out.append(",`"+GoogleEvent.valueOfId(event).value+"`");
					count++;
				}
				final var sheets = Azrael.SQLgetGoogleSpreadsheetSheets(file.getFileID(), e.getGuild().getIdLong());
				StringBuilder out2 = new StringBuilder();
				int i = 0;
				for(final var sheet : sheets) {
					if(i == 0)
						out2.append("`"+sheet.getEvent().name()+":"+sheet.getSheetRowStart()+"`");
					else
						out2.append(", `"+sheet.getEvent().name()+":"+sheet.getSheetRowStart()+"`");
					i++;
				}
				if(out2.length() == 0)
					out2.append(NA);
				
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_EVENT_SELECT)+out.toString()+"\n"+STATIC.getTranslation(e.getMember(), Translation.GOOGLE_EVENT_START)+out2.toString()).build()).queue();
				Hashes.addTempCache(key, new Cache(180000, "spreadsheets-sheet-events", file.getFileID()));
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_NOT_LINKED_EVENT)).build()).queue();
				Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
			}
		}
	}
	
	public static void sheetEvents(GuildMessageReceivedEvent e, String file_id, String event, final String key) {
		final var events = Azrael.SQLgetGoogleLinkedEvents(file_id, e.getGuild().getIdLong());
		if(events == null || events.size() == 0) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Linked google events couldn't be retrieved from Azrael.google_file_to_event for spreadsheet id {}", file_id);
			Hashes.clearTempCache(key);
		}
		else {
			GoogleEvent EVENT = GoogleEvent.valueOfEvent(event);
			if(EVENT != null && events.parallelStream().filter(f -> f == EVENT.id).findAny().orElse(null) != null) {
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_START_SET)).build()).queue();
				Hashes.addTempCache(key, new Cache(180000, "spreadsheets-sheet-update", file_id, event));
			}
		}
	}
	
	public static void sheetUpdate(GuildMessageReceivedEvent e, String file_id, String event, String startingPoint, final String key) {
		if(startingPoint.matches("[a-zA-Z0-9\\[\\]\\s]{1,}[a-zA-Z\\[\\]]{1}[!]{1}[A-Z]{1,3}[1-9]{1}[0-9]*")) {
			if(Azrael.SQLInsertGoogleSpreadsheetSheet(file_id, GoogleEvent.valueOfEvent(event).id, startingPoint, e.getGuild().getIdLong()) > 0) {
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_START_UPDATED)).build()).queue();
				logger.debug("Google spreadsheet starting point updated for guild {}, spreadsheet {}, event {} with value {}", e.getGuild().getId(), file_id, event, startingPoint);
				Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Google spreadsheet starting point couldn't be updated for guild {}, spreadsheet {}, event {} with value {}", e.getGuild().getId(), file_id, event, startingPoint);
				Hashes.clearTempCache(key);
			}
		}
	}
	
	public static void map(GuildMessageReceivedEvent e, final String key) {
		var setup = Azrael.SQLgetGoogleAPISetupOnGuildAndAPI(e.getGuild().getIdLong(), 2);
		if(setup == null) {
			//db error
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Data from Azrael.google_api_setup_view couldn't be retrieved for guild {}", e.getGuild().getId());
			Hashes.clearTempCache(key);
		}
		else if(setup.size() > 0) {
			//list files
			StringBuilder out = new StringBuilder();
			for(int i = 0; i < setup.size(); i++) {
				final GoogleAPISetup currentSetup = setup.get(i);
				out.append("**"+(i+1)+": "+currentSetup.getTitle()+"**\n"+GoogleUtils.buildFileURL(currentSetup.getFileID(), currentSetup.getApiID())+"\n\n");
			}
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_MAP_HELP)+out.toString()).build()).queue();
			Hashes.addTempCache(key, new Cache(180000, "spreadsheets-map"));
		}
		else {
			//nothing found information
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_NO_FILE)).build()).queue();
			Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
		}
	}
	
	public static void mapSelection(GuildMessageReceivedEvent e, int selection, final String key) {
		final var setup = Azrael.SQLgetGoogleAPISetupOnGuildAndAPI(e.getGuild().getIdLong(), 2);
		if(setup == null) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Data from Azrael.google_api_setup_view couldn't be retrieved for guild {}", e.getGuild().getId());
			Hashes.clearTempCache(key);
		}
		else if(selection >= 0 && selection < setup.size()) {
			final var file = setup.get(selection);
			final var events = Azrael.SQLgetGoogleLinkedEvents(file.getFileID(), e.getGuild().getIdLong());
			if(events == null) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Linked google events couldn't be retrieved from Azrael.google_file_to_event for spreadsheet id {}", file.getFileID());
				Hashes.clearTempCache(key);
			}
			else if(events.size() > 0) {
				StringBuilder out = new StringBuilder();
				int count = 0;
				for(final int event : events) {
					if(count == 0)
						out.append("`"+GoogleEvent.valueOfId(event).value+"`");
					else
						out.append(",`"+GoogleEvent.valueOfId(event).value+"`");
					count++;
				}
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_EVENT_SELECT)+out.toString()).build()).queue();
				Hashes.addTempCache(key, new Cache(180000, "spreadsheets-map-events", file.getFileID()));
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_NOT_LINKED_EVENT)).build()).queue();
				Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
			}
		}
	}
	
	public static void mapEvents(GuildMessageReceivedEvent e, String file_id, String event, final String key) {
		final var events = Azrael.SQLgetGoogleLinkedEvents(file_id, e.getGuild().getIdLong());
		if(events == null || events.size() == 0) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Linked google events couldn't be retrieved from Azrael.google_file_to_event for spreadsheet id {}", file_id);
			Hashes.clearTempCache(key);
		}
		else {
			GoogleEvent EVENT = GoogleEvent.valueOfEvent(event);
			if(EVENT != null && events.parallelStream().filter(f -> f == EVENT.id).findAny().orElse(null) != null) {
				final var items = Azrael.SQLgetGoogleEventsToDD(2, EVENT.id);
				if(items == null) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Data dictionary items couldn't be retrieved from Azrael.google_events_to_dd for api id 2 and event id {} for guild {}", EVENT.id, e.getGuild().getId());
					Hashes.clearTempCache(key);
				}
				else if(items.size() > 0) {
					StringBuilder out = new StringBuilder();
					int count = 0;
					for(final String item : items) {
						if(count == 0)
							out.append("`"+item+"`");
						else
							out.append(", `"+item+"`");
						count++;
					}
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_MAP_ADD)+out.toString()).build()).queue();
					Hashes.addTempCache(key, new Cache(180000, "spreadsheets-map-update", file_id, ""+EVENT.id));
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_NO_DD)).build()).queue();
					Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
				}
			}
		}
	}
	
	public static void mapUpdate(GuildMessageReceivedEvent e, String file_id, int event, String dditems, final String key) {
		ArrayList<Integer> item_ids = new ArrayList<Integer>();
		ArrayList<String> item_formats = new ArrayList<String>();
		int invalidCount = 0;
		StringBuilder invalidItems = new StringBuilder();
		String [] items = dditems.split(",");
		for(var i = 0; i < items.length; i++) {
			String [] dd = items[i].trim().split("\\+");
			dd[0] = dd[0].toUpperCase();
			GoogleDD ITEM = GoogleDD.valueOfItem(dd[0]);
			if(ITEM != null) {
				item_ids.add(ITEM.id);
				if(dd.length >= 2)
					item_formats.add(dd[1]);
				else
					item_formats.add("");
			}
			else {
				if(invalidCount == 0)
					invalidItems.append("`"+dd[0]+"`");
				else
					invalidItems.append(",`"+dd[0]+"`");
				invalidCount++;
			}
		}
		if(invalidCount > 0) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_INVALID_DDS)+invalidItems.toString()).build()).queue();
			Hashes.addTempCache(key, new Cache(180000, "spreadsheets-map-update", file_id, ""+event));
		}
		else if(item_ids.size() > 0) {
			if(Azrael.SQLDeleteGoogleSpreadsheetMapping(file_id, event) != -1) {
				int [] result = Azrael.SQLBatchInsertGoogleSpreadsheetMapping(file_id, event, e.getGuild().getIdLong(), item_ids, item_formats);
				if(result[0] > 0) {
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_DD_ADDED)).build()).queue();
					Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
				}
				else if(result[0] == -1) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Batch insert error occurred on Azrael.google_spreadsheet_mapping for the spreadsheet {}, event id {} from guild {}", file_id, event, e.getGuild().getId());
					Hashes.clearTempCache(key);
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).build()).queue();
				logger.error("Table Azrael.google_spreadsheet_mapping couldn't be cleared for spreadsheet {} and event id {} for guild {}", file_id, event, e.getGuild().getId());
				Hashes.clearTempCache(key);
			}
		}
	}
}
