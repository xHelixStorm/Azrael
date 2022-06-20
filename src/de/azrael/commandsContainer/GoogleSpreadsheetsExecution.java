package de.azrael.commandsContainer;

import java.awt.Color;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.sheets.v4.model.Spreadsheet;

import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Cache;
import de.azrael.constructors.GoogleAPISetup;
import de.azrael.core.Hashes;
import de.azrael.enums.Command;
import de.azrael.enums.GoogleDD;
import de.azrael.enums.GoogleEvent;
import de.azrael.enums.Translation;
import de.azrael.google.GoogleDrive;
import de.azrael.google.GoogleSheets;
import de.azrael.google.GoogleUtils;
import de.azrael.sql.Azrael;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

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
		final int breaker = 5;
		if(spreadsheets == null) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Google API information couldn't be retrieved in guild {}", e.getGuild().getId());
			Hashes.clearTempCache(key);
			return;
		}
		else if(spreadsheets.size() == 0)
			out.append("**"+NA+"**");
		else {
			int count = 0;
			for(final var spreadsheet : spreadsheets) {
				if(count == breaker) break;
				out.append("**"+GoogleUtils.buildFileURL(spreadsheet.getFileID(), 2)+"**\n");
				count++;
			}
		}
		e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_HELP)
				.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_EXIT))
				.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_CREATE))
				.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD))
				.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))
				.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_EVENTS))
				.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_SHEET))
				.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_MAP))
				.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_RESTRICT))).build()).queue();
		
		final int maxPage = (spreadsheets.size()/breaker)+(spreadsheets.size()%breaker > 0 ? 1 : 0);
		e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_REGISTERED)+out.toString()).build()).queue(m -> {
			if(spreadsheets.size() > 0)
				STATIC.addPaginationReactions(e, m, maxPage, "1", ""+breaker, spreadsheets);
		});
		Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
	}
	
	public static void create(GuildMessageReceivedEvent e, String title, final String key, BotConfigs botConfig) {
		if(title == null) {
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_CREATE)).build()).queue();
		}
		else {
			//create and transfer ownership only if an email has been provided
			final String email = botConfig.getGoogleMainEmail();
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
				} catch(SocketTimeoutException e1) {
					final int counter = GoogleUtils.retrieveTimeoutAttemptCounter(e.getGuild().getId()+"create");
					if(counter < 5) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_TIMEOUT).replace("{}", ""+(counter+1))).build()).queue();
					}
					if(GoogleUtils.timeoutHandler(e.getGuild(), (e.getGuild().getId()+"create"), null, e1)) {
						create(e, title, key, botConfig);
					}
					err = true;
				} catch (Exception e1) {
					logger.error("An error occurred on process step \"{}\" in guild {}", processStep, e.getGuild().getId(), e1);
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_WEBSERVICE)+e1.getMessage()).build()).queue();
					err = true;
				}
				
				if(!err) {
					if(Azrael.SQLInsertGoogleAPISetup(file_id, e.getGuild().getIdLong(), title, 2) > 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_CREATED)+file_id).build()).queue();
						logger.info("Spreadsheet with the file id {} has been created in guild {}", file_id, e.getGuild().getIdLong());
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_CREATED_2)
								.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD))+file_id).build()).queue();
						logger.warn("Spreadsheet with the file id {} has been created in guild {} but without linkage", file_id, e.getGuild().getIdLong());
					}
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_EMAIL)).build()).queue();
			}
		}
		Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
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
				} catch(SocketTimeoutException e1) {
					final int counter = GoogleUtils.retrieveTimeoutAttemptCounter(file_id);
					if(counter < 5) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_TIMEOUT).replace("{}", ""+(counter+1))).build()).queue();
					}
					if(GoogleUtils.timeoutHandler(e.getGuild(), file_id, null, e1)) {
						add(e, file_id, key);
					}
					err = true;
				} catch (Exception e1) {
					logger.error("An error occurred on process step \"Retrieve Spreadsheet\" in guild {}", e.getGuild().getId(), e1);
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_WEBSERVICE)+e1.getMessage()).build()).queue();
					err = true;
				}
				
				if(!err) {
					if(Azrael.SQLInsertGoogleAPISetup(spreadsheet.getSpreadsheetId(), e.getGuild().getIdLong(), spreadsheet.getProperties().getTitle(), 2) > 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_ADDED)).build()).queue();
						logger.info("Spreadsheet with the file id {} has been linked to the bot in guild {}", file_id, e.getGuild().getIdLong());
						Azrael.SQLInsertActionLog("GOOGLE_SPREADSHEET_LINK", 0, e.getGuild().getIdLong(), file_id);
						Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_NO_LINK)).build()).queue();
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
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
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
				} catch(SocketTimeoutException e1) {
					final int counter = GoogleUtils.retrieveTimeoutAttemptCounter(file_id);
					if(counter < 5) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_TIMEOUT).replace("{}", ""+(counter+1))).build()).queue();
					}
					if(GoogleUtils.timeoutHandler(e.getGuild(), file_id, null, e1)) {
						remove(e, file_id, key);
					}
					err = true;
				} catch (Exception e1) {
					logger.error("An error occurred on process step \"Retrieve Spreadsheet\" in guild {}", e.getGuild().getId(), e1);
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_WEBSERVICE)+e1.getMessage()).build()).queue();
					err = true;
				}
				
				if(!err) {
					Azrael.SQLDeleteGoogleSpreadsheetSheet(spreadsheet.getSpreadsheetId(), e.getGuild().getIdLong());
					Azrael.SQLDeleteGoogleSpreadsheetMapping(spreadsheet.getSpreadsheetId(), e.getGuild().getIdLong());
					Azrael.SQLDeleteGoogleFileToEvent(spreadsheet.getSpreadsheetId(), e.getGuild().getIdLong());
					if(Azrael.SQLDeleteGoogleAPISetup(spreadsheet.getSpreadsheetId(), e.getGuild().getIdLong()) > 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_REMOVED)).build()).queue();
						logger.info("Link of the spreadsheet with the file id {} has been severed from the bot in guild {}", file_id, e.getGuild().getIdLong());
						Azrael.SQLInsertActionLog("GOOGLE_SPREADSHEET_UNLINK", 0, e.getGuild().getIdLong(), file_id);
						Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
						Hashes.removeSpreadsheetProperty(file_id);
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
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
	}
	
	public static void events(GuildMessageReceivedEvent e, final String key) {
		var setup = Azrael.SQLgetGoogleAPISetupOnGuildAndAPI(e.getGuild().getIdLong(), 2);
		if(setup == null) {
			//db error
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Google API information couldn't be retrieved in guild {}", e.getGuild().getId());
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
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
	}
	
	public static void eventsFileSelection(GuildMessageReceivedEvent e, int selection, final String key) {
		final var setup = Azrael.SQLgetGoogleAPISetupOnGuildAndAPI(e.getGuild().getIdLong(), 2);
		if(setup == null) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Google API information couldn't be retrieved in guild {}", e.getGuild().getId());
			Hashes.clearTempCache(key);
		}
		else if(selection >= 0 && selection < setup.size()) {
			final var events = Azrael.SQLgetGoogleEventsSupportSpreadsheet();
			if(events == null) {
				//db error
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_EVENT_HELP)).build()).queue();
				logger.error("Google events couldn't be retrieved in guild {}", e.getGuild().getId());
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
				
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_EVENTS_ADD)
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD))
						.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))
						+out.toString()+"\n"+STATIC.getTranslation(e.getMember(), Translation.GOOGLE_EVENTS)+out2.toString()).build()).queue();
				Hashes.addTempCache(key, new Cache(180000, "spreadsheets-events-update", file.getFileID()));
			}
			else {
				//nothing found information
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_NO_EVENTS)).build()).queue();
				logger.error("Google spreadsheets events are not defined in guild {}", e.getGuild().getId());
				Hashes.clearTempCache(key);
			}
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
		}
	}
	
	public static void eventsFileHandler(GuildMessageReceivedEvent e, String userMessage, String file_id, final String key) {
		boolean addEvents;
		String [] events;
		final String ADD = STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD);
		final String REMOVE = STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE);
		if(userMessage.startsWith(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD))) {
			userMessage = userMessage.substring(ADD.length()+1);
			events = userMessage.split(",");
			addEvents = true;
		}
		else {
			userMessage = userMessage.substring(REMOVE.length()+1);
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
					logger.info("Events added to file id {} in guild {}", file_id, e.getGuild().getId());
					Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
					Hashes.removeSpreadsheetProperty(file_id);
				}
			}
			else {
				if(Azrael.SQLBatchDeleteGoogleSpreadsheetSheet(file_id, e.getGuild().getIdLong(), handleEvents)) {
					if(Azrael.SQLBatchDeleteGoogleSpreadsheetMapping(file_id, e.getGuild().getIdLong(), handleEvents)) {
						if(Azrael.SQLBatchDeleteGoogleFileToEvent(file_id, e.getGuild().getIdLong(), handleEvents)) {
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_EVENTS_REMOVED)).build()).queue();
							logger.info("Events removed for file_id {} in guild {}", file_id, e.getGuild().getId());
							Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("Registered events couldn't be removed for file_id {} in guild {}", file_id, e.getGuild().getId());
							Hashes.clearTempCache(key);
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Mapped events couldn't be removed for file_id {} in guild {}", file_id, e.getGuild().getId());
						Hashes.clearTempCache(key);
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Table events couldn't be removed for file_id {} in guild {}", file_id, e.getGuild().getId());
					Hashes.clearTempCache(key);
				}
			}
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
		}
	}
	
	public static void sheet(GuildMessageReceivedEvent e, final String key) {
		var setup = Azrael.SQLgetGoogleAPISetupOnGuildAndAPI(e.getGuild().getIdLong(), 2);
		if(setup == null) {
			//db error
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Google API information couldn't be retrieved for guild {}", e.getGuild().getId());
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
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
	}
	
	public static void sheetSelection(GuildMessageReceivedEvent e, int selection, final String key) {
		final var setup = Azrael.SQLgetGoogleAPISetupOnGuildAndAPI(e.getGuild().getIdLong(), 2);
		if(setup == null) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Google API information couldn't be retrieved for guild {}", e.getGuild().getId());
			Hashes.clearTempCache(key);
		}
		else if(selection >= 0 && selection < setup.size()) {
			final var file = setup.get(selection);
			final var events = Azrael.SQLgetGoogleLinkedEvents(file.getFileID(), e.getGuild().getIdLong());
			if(events == null) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Linked google events couldn't be retrieved for spreadsheet id {} in guild {}", file.getFileID(), e.getGuild().getId());
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
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
		}
	}
	
	public static void sheetEvents(GuildMessageReceivedEvent e, String file_id, String event, final String key) {
		final var events = Azrael.SQLgetGoogleLinkedEvents(file_id, e.getGuild().getIdLong());
		if(events == null || events.size() == 0) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Linked google events couldn't be retrieved for spreadsheet id {} in guild {}", file_id, e.getGuild().getId());
			Hashes.clearTempCache(key);
		}
		else {
			GoogleEvent EVENT = GoogleEvent.valueOfEvent(event);
			if(EVENT != null && events.parallelStream().filter(f -> f == EVENT.id).findAny().orElse(null) != null) {
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_START_SET)).build()).queue();
				Hashes.addTempCache(key, new Cache(180000, "spreadsheets-sheet-update", file_id, event));
			}
		}
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
	}
	
	public static void sheetUpdate(GuildMessageReceivedEvent e, String file_id, String event, String startingPoint, final String key) {
		if(startingPoint.matches("[a-zA-Z0-9\\[\\]\\s]{1,}[a-zA-Z\\[\\]]{1}[!]{1}[A-Z]{1,3}[1-9]{1}[0-9]*")) {
			if(Azrael.SQLInsertGoogleSpreadsheetSheet(file_id, GoogleEvent.valueOfEvent(event).id, startingPoint, e.getGuild().getIdLong()) > 0) {
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_START_UPDATED)).build()).queue();
				logger.info("Google spreadsheet starting point updated for spreadsheet {} and event {} with value {} in guild {}", file_id, event, startingPoint, e.getGuild().getId());
				Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
				Hashes.removeSpreadsheetProperty(file_id);
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Google spreadsheet starting point couldn't be updated for spreadsheet {} and event {} with value {} in guild {}", file_id, event, startingPoint, e.getGuild().getId());
				Hashes.clearTempCache(key);
			}
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
		}
	}
	
	public static void map(GuildMessageReceivedEvent e, final String key) {
		var setup = Azrael.SQLgetGoogleAPISetupOnGuildAndAPI(e.getGuild().getIdLong(), 2);
		if(setup == null) {
			//db error
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Google API information couldn't be retrieved in guild {}", e.getGuild().getId());
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
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
	}
	
	public static void mapSelection(GuildMessageReceivedEvent e, int selection, final String key) {
		final var setup = Azrael.SQLgetGoogleAPISetupOnGuildAndAPI(e.getGuild().getIdLong(), 2);
		if(setup == null) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Google API information couldn't be retrieved in guild {}", e.getGuild().getId());
			Hashes.clearTempCache(key);
		}
		else if(selection >= 0 && selection < setup.size()) {
			final var file = setup.get(selection);
			final var events = Azrael.SQLgetGoogleLinkedEvents(file.getFileID(), e.getGuild().getIdLong());
			if(events == null) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Linked google events couldn't be retrieved for spreadsheet id {} in guild {}", file.getFileID());
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
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
		}
	}
	
	public static void mapEvents(GuildMessageReceivedEvent e, String file_id, String event, final String key) {
		final var events = Azrael.SQLgetGoogleLinkedEvents(file_id, e.getGuild().getIdLong());
		if(events == null || events.size() == 0) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Linked google events couldn't be retrieved for spreadsheet id {} in guild {}", file_id, e.getGuild().getId());
			Hashes.clearTempCache(key);
		}
		else {
			GoogleEvent EVENT = GoogleEvent.valueOfEvent(event);
			if(EVENT != null && events.parallelStream().filter(f -> f == EVENT.id).findAny().orElse(null) != null) {
				final var items = Azrael.SQLgetGoogleEventsToDD(2, EVENT.id);
				if(items == null) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Data dictionary items couldn't be retrieved for api id 2 and event id {} in guild {}", EVENT.id, e.getGuild().getId());
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
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
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
			if(Azrael.SQLDeleteGoogleSpreadsheetMapping(file_id, e.getGuild().getIdLong(), event) != -1) {
				int [] result = Azrael.SQLBatchInsertGoogleSpreadsheetMapping(file_id, event, e.getGuild().getIdLong(), item_ids, item_formats);
				if(result[0] > 0) {
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_DD_ADDED)).build()).queue();
					Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
					Hashes.removeSpreadsheetProperty(file_id);
				}
				else if(result[0] == -1) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Mapping set error for the spreadsheet {} and event id {} in guild {}", file_id, event, e.getGuild().getId());
					Hashes.clearTempCache(key);
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).build()).queue();
				logger.error("Current mapping couldn't be cleared for spreadsheet {} and event id {} in guild {}", file_id, event, e.getGuild().getId());
				Hashes.clearTempCache(key);
			}
		}
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
	}
	
	public static void restrict(GuildMessageReceivedEvent e, final String key) {
		var setup = Azrael.SQLgetGoogleAPISetupOnGuildAndAPI(e.getGuild().getIdLong(), 2);
		if(setup == null) {
			//db error
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Google API information couldn't be retrieved in guild {}", e.getGuild().getId());
			Hashes.clearTempCache(key);
		}
		else if(setup.size() > 0) {
			//list files
			StringBuilder out = new StringBuilder();
			for(int i = 0; i < setup.size(); i++) {
				final GoogleAPISetup currentSetup = setup.get(i);
				out.append("**"+(i+1)+": "+currentSetup.getTitle()+"**\n"+GoogleUtils.buildFileURL(currentSetup.getFileID(), currentSetup.getApiID())+"\n\n");
			}
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_RESTRICT_HELP)+out.toString()).build()).queue();
			Hashes.addTempCache(key, new Cache(180000, "spreadsheets-restrict"));
		}
		else {
			//nothing found information
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_NO_FILE)).build()).queue();
			Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
		}
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
	}
	
	public static void restrictSelection(GuildMessageReceivedEvent e, int selection, final String key) {
		final var setup = Azrael.SQLgetGoogleAPISetupOnGuildAndAPI(e.getGuild().getIdLong(), 2);
		if(setup == null) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Google API information couldn't be retrieved for guild {}", e.getGuild().getId());
			Hashes.clearTempCache(key);
		}
		else if(selection >= 0 && selection < setup.size()) {
			final var file = setup.get(selection);
			final var events = Azrael.SQLgetGoogleLinkedEventsRestrictions(file.getFileID(), e.getGuild().getIdLong());
			if(events == null) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Linked google events couldn't be retrieved for spreadsheet id {} in guild {}", file.getFileID(), e.getGuild().getId());
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
				Hashes.addTempCache(key, new Cache(180000, "spreadsheets-restrict-events", file.getFileID()));
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_RESTRICT_ERR)).build()).queue();
				Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
			}
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
		}
	}
	
	public static void restrictEvents(GuildMessageReceivedEvent e, String file_id, String event, final String key) {
		final var events = Azrael.SQLgetGoogleLinkedEventsRestrictions(file_id, e.getGuild().getIdLong());
		if(events == null || events.size() == 0) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Linked google events couldn't be retrieved for spreadsheet id {} in guild {}", file_id, e.getGuild().getId());
			Hashes.clearTempCache(key);
		}
		else {
			GoogleEvent EVENT = GoogleEvent.valueOfEvent(event);
			if(EVENT != null && events.parallelStream().filter(f -> f == EVENT.id).findAny().orElse(null) != null) {
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_RESTRICT_SET)
						.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_NONE))).build()).queue();
				Hashes.addTempCache(key, new Cache(180000, "spreadsheets-restrict-update", file_id, event));
			}
		}
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
	}
	
	public static void restrictUpdate(GuildMessageReceivedEvent e, String file_id, String event, String channel, final String key) {
		if(channel.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_NONE))) {
			final var result = Azrael.SQLUpdateGoogleRemoveChannelRestriction(e.getGuild().getIdLong(), file_id, GoogleEvent.valueOfEvent(event).id);
			if(result > 0) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_RESTRICT_REMOVE)).build()).queue();
				logger.info("Channel restriction removed for file id {} and event {} in guild {}", file_id, event, e.getGuild().getId());
				Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
			}
			else if(result == 0) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_RESTRICT_ERR_2)).build()).queue();
				Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Channel restriction couldn't be removed for file id {} and event {} in guild {}", file_id, event, e.getGuild().getId());
				Hashes.clearTempCache(key);
			}
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
		}
		else {
			final String channel_id = channel.replaceAll("[<#>]*", "");
			if(channel_id.replaceAll("[0-9]*", "").length() == 0) {
				final TextChannel textChannel = e.getGuild().getTextChannelById(channel_id);
				if(textChannel != null) {
					final var result = Azrael.SQLUpdateGoogleChannelRestriction(e.getGuild().getIdLong(), file_id, GoogleEvent.valueOfEvent(event).id, textChannel.getIdLong());
					if(result > 0) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_RESTRICT_ADD)).build()).queue();
						logger.info("Channel restriction {} added for file id {} and event {} in guild {}", textChannel.getId(), file_id, event, e.getGuild().getId());
						Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
					}
					else if (result == 0) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.GOOGLE_SHEET_RESTRICT_ERR_3)).build()).queue();
						Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Channel restriction {} couldn't be set for file id {} and event {} in guild {}", textChannel.getId(), file_id, event, e.getGuild().getId());
						Hashes.clearTempCache(key);
					}
				}
				Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.GOOGLE.getColumn(), e.getMessage().getContentRaw());
			}
		}
		Hashes.addTempCache(key, new Cache(180000, "spreadsheets-selection"));
	}
}
