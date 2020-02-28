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
import fileManagement.GuildIni;
import google.GoogleDrive;
import google.GoogleSheets;
import google.GoogleUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;

public class GoogleSpreadsheetsExecution {
	private final static Logger logger = LoggerFactory.getLogger(GoogleSpreadsheetsExecution.class);
	private final static EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
	private final static EmbedBuilder error = new EmbedBuilder().setColor(Color.RED);

	public static void runTask(GuildMessageReceivedEvent e, final String key) {
		StringBuilder out = new StringBuilder();
		final var spreadsheets = Azrael.SQLgetGoogleAPISetupOnGuildAndAPI(e.getGuild().getIdLong(), 2);
		if(spreadsheets == null)
			logger.error("Data couldn't be retrieved from Azrael.google_api_setup in guild {}", e.getGuild().getId());
		else if(spreadsheets.size() == 0)
			out.append("**N/A**");
		else {
			for(final var spreadsheet : spreadsheets) {
				out.append("**"+GoogleUtils.buildFileURL(spreadsheet.getFileID(), 2)+"**\n");
			}
		}
		e.getChannel().sendMessage(message.setDescription("Use one of the below parameters to navigate through the spreadsheets setup or type **exit** to close the setup any time. This message is shown only once\n\n"
			+ "**create**: create a spreadsheets table\n"
			+ "**add**: add an already existing spreadsheet table\n"
			+ "**remove**: remove a linked spreadsheet table (no deletion)"
			+ "**events**: select an already existing spreadsheet table\n"
			+ "**sheet**: Define the sheet and row/column to operate on\n"
			+ "**map**: map the available table fields with values from the bot\n\n"
			+ "Current Spreadsheets:\n"+out.toString()).build()).queue();
		Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
	}
	
	public static void create(GuildMessageReceivedEvent e, String title, final String key) {
		if(title == null) {
			e.getChannel().sendMessage(message.setDescription("Use this parameter together with a title name to create a new spreadsheet and directly link it with the bot."
				+ " Note that for this action, an email address needs to be provided in the bot settings!").build()).queue();
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
					GoogleDrive.transferOwnerOfFile(GoogleDrive.getDriveClientService(), file_id, GuildIni.getGoogleMainEmail(e.getGuild().getIdLong()));
					processStep = "Ownership Transfer";
				} catch (Exception e1) {
					logger.error("An error occurred on process step \"{}\"", processStep, e1);
					e.getChannel().sendMessage(error.setDescription(e1.getMessage()).build()).queue();
					err = true;
				}
				
				if(!err) {
					if(Azrael.SQLInsertGoogleAPISetup(file_id, e.getGuild().getIdLong(), title, 2) > 0) {
						e.getChannel().sendMessage(message.setDescription("Google Spreadsheet has been created and linked to the bot.\n"
							+ "URL: https://docs.google.com/spreadsheets/d/"+file_id).build()).queue();
						logger.debug("Spreadsheet with the file id {} has been created from guild {}", file_id, e.getGuild().getIdLong());
					}
					else {
						e.getChannel().sendMessage(error.setDescription("Google Spreadsheet has been created but could not be linked to the bot. Try to link the created spreadsheet with the following url and add parameter.\n"
							+ "URL: https://docs.google.com/spreadsheets/d/"+file_id).build()).queue();
						logger.warn("Spreadsheet with the file id {} has been created from guild {} but without linkage", file_id, e.getGuild().getIdLong());
					}
				}
			}
			else {
				e.getChannel().sendMessage(error.setDescription("Please configure an email address before using this command, so that the ownership can be moved to that email address!").build()).queue();
			}
		}
		Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
	}
	
	public static void add(GuildMessageReceivedEvent e, String file_id, final String key) {
		if(file_id == null) {
			e.getChannel().sendMessage(message.setDescription("Use this parameter together with a spreadsheet url or just the file id to link it with the bot.").build()).queue();
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
					e.getChannel().sendMessage(error.setDescription(e1.getMessage()).build()).queue();
					err = true;
				}
				
				if(!err) {
					if(Azrael.SQLInsertGoogleAPISetup(spreadsheet.getSpreadsheetId(), e.getGuild().getIdLong(), spreadsheet.getProperties().getTitle(), 2) > 0) {
						e.getChannel().sendMessage(message.setDescription("Google Spreadsheet has been linked to the bot.").build()).queue();
						logger.debug("Spreadsheet with the file id {} has been linked to the bot in guild {}", file_id, e.getGuild().getIdLong());
					}
					else {
						e.getChannel().sendMessage(error.setDescription("An internal error has occurred. Google Spreadsheet couldn't be linked to the bot.").build()).queue();
						logger.error("Spreadsheet with the file id {} couldn't be linked to the bot in guild {}");
					}
				}
			}
			else {
				e.getChannel().sendMessage(error.setDescription("Please provide a spreadsheet url or spreadsheet id which can be accessed by the bot").build()).queue();
			}
		}
		Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
	}
	
	public static void remove(GuildMessageReceivedEvent e, String file_id, final String key) {
		if(file_id == null) {
			e.getChannel().sendMessage(message.setDescription("Use this parameter together with a spreadsheet url or just the file id to remove the link with the bot.").build()).queue();
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
					e.getChannel().sendMessage(error.setDescription(e1.getMessage()).build()).queue();
					err = true;
				}
				
				if(!err) {
					Azrael.SQLDeleteGoogleSpreadsheetSheet(spreadsheet.getSpreadsheetId());
					Azrael.SQLDeleteGoogleSpreadsheetMapping(spreadsheet.getSpreadsheetId());
					Azrael.SQLDeleteGoogleFileToEvent(spreadsheet.getSpreadsheetId());
					if(Azrael.SQLDeleteGoogleAPISetup(spreadsheet.getSpreadsheetId(), e.getGuild().getIdLong()) > 0) {
						e.getChannel().sendMessage(message.setDescription("Link of the Google Spreadsheet has been severed!").build()).queue();
						logger.debug("Link of the spreadsheet with the file id {} has been severed from the bot in guild {}", file_id, e.getGuild().getIdLong());
					}
					else {
						e.getChannel().sendMessage(error.setDescription("An internal error has occurred. Link to the Google Spreadsheet couldn't be severed from the bot.").build()).queue();
						logger.error("Link of the spreadsheet with the file id {} couldn't be severed from the bot in guild {}");
					}
				}
			}
			else {
				e.getChannel().sendMessage(error.setDescription("Please provide a spreadsheet url or spreadsheet id which can be accessed by the bot").build()).queue();
			}
		}
		Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
	}
	
	public static void events(GuildMessageReceivedEvent e, final String key) {
		var setup = Azrael.SQLgetGoogleAPISetupOnGuildAndAPI(e.getGuild().getIdLong(), 2);
		if(setup == null) {
			//db error
			e.getChannel().sendMessage(error.setDescription("An internal error has occurred! Files from database couldn't be retrieved").build()).queue();
			logger.error("Data from Azrael.google_api_setup_view couldn't be retrieved for guild {}", e.getGuild().getId());
			Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
		}
		else if(setup.size() > 0) {
			//list files
			StringBuilder out = new StringBuilder();
			for(int i = 0; i < setup.size(); i++) {
				final GoogleAPISetup currentSetup = setup.get(i);
				out.append("**"+(i+1)+"**:\nTITLE: "+currentSetup.getTitle()+"\nURL:   "+GoogleUtils.buildFileURL(currentSetup.getFileID(), currentSetup.getApiID())+"\nTYPE:  "+currentSetup.getAPI()+"\n\n");
			}
			e.getChannel().sendMessage(message.setDescription("The following spreadsheets are linked. Please select a file that you wish to add or remove events from:\n\n"+out.toString()).build()).queue();
			Hashes.addTempCache(key, new Cache(180000, "spreadsheets-events"));
		}
		else {
			//nothing found information
			e.getChannel().sendMessage(message.setDescription("No linked file has been found. Please link / create a google file before using this command!").build()).queue();
			Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
		}
	}
	
	public static void eventsFileSelection(GuildMessageReceivedEvent e, int selection, final String key) {
		final var setup = Azrael.SQLgetGoogleAPISetupOnGuildAndAPI(e.getGuild().getIdLong(), 2);
		if(setup == null) {
			e.getChannel().sendMessage(error.setDescription("An internal error has occurred! Files from database couldn't be retrieved. Returning to the spreadsheets parameters selection page...").build()).queue();
			logger.error("Data from Azrael.google_api_setup_view couldn't be retrieved for guild {}", e.getGuild().getId());
			Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
		}
		else if(selection >= 0 && selection < setup.size()) {
			final var events = Azrael.SQLgetGoogleEventsSupportSpreadsheet();
			if(events == null) {
				//db error
				e.getChannel().sendMessage(error.setDescription("An internal error has occurred! Events from database couldn't be retrieved. Returning to the spreadsheets parameters selection page...").build()).queue();
				logger.error("Data from Azrael.google_events couldn't be retrieved for guild {}", e.getGuild().getId());
				Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
			}
			else if(events.size() > 0) {
				//list events
				final var file = setup.get(selection);
				StringBuilder out = new StringBuilder();
				for(int i = 0; i<events.size(); i++) {
					final var event = events.get(i);
					if(i == 0)
						out.append("`"+event.getEvent()+"`");
					else
						out.append(", `"+event.getEvent()+"`");
				}
				
				e.getChannel().sendMessage(message.setDescription("Now type in the required events starting with either add or remove. If multiple events should be inserted, separate them with a ',' These are the supported events for spreadsheets: \n\n"+out.toString()).build()).queue();
				Hashes.addTempCache(key, new Cache(180000, "spreadsheets-events-update", file.getFileID()));
			}
			else {
				//nothing found information
				e.getChannel().sendMessage(error.setDescription("Events have not been configured! Please contact the administrator for the Bot! Returning to the spreadsheets parameters selection page...").build()).queue();
				logger.error("Google spreadsheets events are not defined!");
				Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
			}
		}
	}
	
	public static void eventsFileHandler(GuildMessageReceivedEvent e, String userMessage, String file_id, final String key) {
		boolean addEvents;
		String [] events;
		if(userMessage.startsWith("add")) {
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
			final var event = fixedEvents.parallelStream().filter(f -> f.getEvent().equalsIgnoreCase(events[iterator])).findAny().orElse(null);
			if(event == null)
				e.getChannel().sendMessage(error.setDescription("The event **"+events[iterator]+"** either doesn't exist or is not supported for this event").build()).queue();
			else
				handleEvents.add(event.getEventID());
		}
		if(handleEvents.size() > 0) {
			if(addEvents)
				if(Azrael.SQLBatchInsertGoogleFileToEventLink(file_id, handleEvents)) {
					e.getChannel().sendMessage(message.setDescription("Event(s) have been linked with the file. Don't forget to map the columns and to define the starting point!").build()).queue();
					logger.debug("Events added for file_id {} in guild {}", file_id, e.getGuild().getId());
				}
			else {
				if(Azrael.SQLBatchDeleteGoogleSpreadsheetSheet(file_id, handleEvents)) {
					logger.debug("Events removed from Azrael.google_spreadsheetsheet for file_id {} in guild {}", file_id, e.getGuild().getId());
					if(Azrael.SQLBatchDeleteGoogleSpreadsheetMapping(file_id, handleEvents)) {
						logger.debug("Events removed from Azrael.google_spreadsheet_mapping for file_id {} in guild {}", file_id, e.getGuild().getId());
						if(Azrael.SQLBatchDeleteGoogleFileToEvent(file_id, handleEvents)) {
							e.getChannel().sendMessage(message.setDescription("Events have been removed from the file").build()).queue();
							logger.debug("Events removed from Azrael.google_file_to_event for file_id {} in guild {}", file_id, e.getGuild().getId());
						}
						else {
							e.getChannel().sendMessage(error.setDescription("An internal error occurred! Event(s) couldn't be removed from the spreadsheet file. Returning to the spreadsheets parameter selection page...").build()).queue();
							logger.error("Events couldn't be removed from Azrael.google_file_to_event with file_id {} in guild {}", file_id, e.getGuild().getId());
						}
					}
					else {
						e.getChannel().sendMessage(error.setDescription("An internal error occurred! Event(s) couldn't be removed from the spreadsheet file. Returning to the spreadsheets parameter selection page...").build()).queue();
						logger.error("Events couldn't be removed from Azrael.google_spreadsheet_mapping with file_id {} in guild {}", file_id, e.getGuild().getId());
					}
				}
				else {
					e.getChannel().sendMessage(error.setDescription("An internal error occurred! Event(s) couldn't be removed from the spreadsheet file. Returning to the spreadsheets parameter selection page...").build()).queue();
					logger.error("Events couldn't be removed from Azrael.google_spreadsheet_sheet with file_id {} in guild {}", file_id, e.getGuild().getId());
				}
			}
			Hashes.addTempCache(key, new Cache(180000, "spreadsheet-selection"));
		}
	}
	
	public static void sheet(GuildMessageReceivedEvent e, final String key) {
		var setup = Azrael.SQLgetGoogleAPISetupOnGuildAndAPI(e.getGuild().getIdLong(), 2);
		if(setup == null) {
			//db error
			e.getChannel().sendMessage(error.setDescription("An internal error has occurred! Files from database couldn't be retrieved").build()).queue();
			logger.error("Data from Azrael.google_api_setup_view couldn't be retrieved for guild {}", e.getGuild().getId());
			Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
		}
		else if(setup.size() > 0) {
			//list files
			StringBuilder out = new StringBuilder();
			for(int i = 0; i < setup.size(); i++) {
				final GoogleAPISetup currentSetup = setup.get(i);
				out.append("**"+(i+1)+"**:\nTITLE: "+currentSetup.getTitle()+"\nURL:   "+GoogleUtils.buildFileURL(currentSetup.getFileID(), currentSetup.getApiID())+"\nTYPE:  "+currentSetup.getAPI()+"\n\n");
			}
			e.getChannel().sendMessage(message.setDescription("The following spreadsheets are linked. Please select a file that you wish to configure the starting point:\n\n"+out.toString()).build()).queue();
			Hashes.addTempCache(key, new Cache(180000, "spreadsheets-sheet"));
		}
		else {
			//nothing found information
			e.getChannel().sendMessage(message.setDescription("No linked file has been found. Please link / create a google file before using this command!").build()).queue();
			Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
		}
	}
	
	public static void map(GuildMessageReceivedEvent e, final String key) {
		
	}
}
