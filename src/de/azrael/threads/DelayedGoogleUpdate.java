package de.azrael.threads;

import java.awt.Color;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.ValueRange;

import de.azrael.enums.Channel;
import de.azrael.enums.GoogleEvent;
import de.azrael.enums.Translation;
import de.azrael.google.GoogleSheets;
import de.azrael.google.GoogleUtils;
import de.azrael.listeners.ShutdownListener;
import de.azrael.sql.Azrael;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;

public class DelayedGoogleUpdate implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(DelayedGoogleUpdate.class);
	
	private final static LinkedHashMap<String, ValueRange> add = new LinkedHashMap<String, ValueRange>();
	private final static LinkedHashMap<String, ValueRange> update = new LinkedHashMap<String, ValueRange>();
	private final static LinkedHashMap<String, ValueRange> remove = new LinkedHashMap<String, ValueRange>();
	
	private final static ConcurrentHashMap<String, ValueRange> wholeSpreadsheet = new ConcurrentHashMap<String, ValueRange>();
	
	private Guild guild;
	private ValueRange valueRange;
	private long message_id;
	private String file_id;
	private String channel_id;
	private String action;
	private GoogleEvent event;
	
	public DelayedGoogleUpdate(Guild _guild, ValueRange _valueRange, long _message_id, String _file_id, String _channel_id, String _action, GoogleEvent _event) {
		this.guild = _guild;
		this.valueRange = _valueRange;
		this.message_id = _message_id;
		this.file_id = _file_id;
		this.channel_id = _channel_id;
		this.action = _action;
		this.event = _event;
	}

	@Override
	public void run() {
		STATIC.addThread(Thread.currentThread(), event.name()+guild.getId()+channel_id);
		
		switch(action) {
			case "add" -> {
				add.put(guild.getId()+"_"+channel_id+"_"+message_id, valueRange);
			}
			case "update" -> {
				update.put(guild.getId()+"_"+channel_id+"_"+message_id, valueRange);
			}
			case "remove" -> {
				remove.put(guild.getId()+"_"+channel_id+"_"+message_id, valueRange);
			}
		}
		
		boolean interrupted = false;
		
		try {
			Thread.sleep(TimeUnit.MINUTES.toMillis(Integer.parseInt(System.getProperty("SPREADSHEET_UPDATE_DELAY"))));
		} catch (InterruptedException e) {
			interrupted = true;
		}
		
		handleBatchUpdate(guild, channel_id, file_id, new LinkedHashMap<String, ValueRange>(update), event);
		handleBatchAppend(guild, channel_id, file_id, new LinkedHashMap<String, ValueRange>(add), event);
		handleBatchRemove(guild, channel_id, file_id, new LinkedHashMap<String, ValueRange>(remove), event);
		
		STATIC.removeThread(Thread.currentThread());
		
		if(interrupted) {
			ShutdownListener.decreaseShutdownCountdown();
		}
	}
	
	public static synchronized void handleAdditionalRequest(Guild guild, String channel_id, ValueRange valueRange, long message_id, String action) {
		final String key = guild.getId()+"_"+channel_id+"_"+message_id;
		switch(action) {
			case "add" -> {
				add.put(key, valueRange);
			}
			case "update" -> {
				if(!add.containsKey(key))
					update.put(key, valueRange);
				else
					add.put(key, valueRange);
			}
			case "remove" -> {
				remove.put(key, valueRange);
			}
		}
	}
	
	private static synchronized void handleRequestRemoval(String key, String action) {
		switch(action) {
			case "add" -> {
				if(add.containsKey(key))
					add.remove(key);
			}
			case "update" -> {
				if(update.containsKey(key))
					update.remove(key);
			}
			case "remove" -> {
				if(remove.containsKey(key))
					remove.remove(key);
			}
		}
	}
	
	public static synchronized boolean containsMessage(String key, String action) {
		return switch(action) {
			case "add" -> {
				yield add.containsKey(key);
			}
			case "update" -> {
				yield update.containsKey(key);
			}
			case "remove" -> {
				yield remove.containsKey(key);
			}
			default -> {
				yield false;
			}
		};
	}
	
	public static synchronized void handleRequestRemoval(String key) {
		if(add.containsKey(key))
			add.remove(key);
		else if(update.containsKey(key))
			update.remove(key);
	}
	
	private static void handleBatchUpdate(Guild guild, String channel_id, String file_id, LinkedHashMap<String, ValueRange> update, GoogleEvent event) {
		final ArrayList<ValueRange> updateList = new ArrayList<ValueRange>();
		final Set<String> keys = new HashSet<String>();
		update.forEach((k, v) -> {
			if(k.startsWith(guild.getId()+"_"+channel_id)) {
				updateList.add(v);
				keys.add(k);
			}
		});
		if(updateList.size() > 0) {
			keys.parallelStream().forEach(k -> handleRequestRemoval(k, "update"));
			try {
				GoogleSheets.batchUpdateRowsOnSpreadsheet(GoogleSheets.getSheetsClientService(), file_id, updateList);
				logger.info("Google spreadsheet batch update executed for file {} and event {} in guild {}", file_id, event.name(), guild.getId());
			} catch (SocketTimeoutException e) {
				if(GoogleUtils.timeoutHandler(guild, file_id, event.name(), e)) {
					handleBatchUpdate(guild, channel_id, file_id, update, event);
				}
			} catch (Exception e) {
				STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED), STATIC.getTranslation2(guild, Translation.GOOGLE_WEBSERVICE)+e.getMessage(), Channel.LOG.name());
				logger.error("Google Webservice error for event {} in guild {}", event, guild.getId(), e);
			}
		}
	}
	
	private static void handleBatchAppend(Guild guild, String channel_id, String file_id, LinkedHashMap<String, ValueRange> add, GoogleEvent event) {
		List<List<Object>> values = new ArrayList<List<Object>>();
		final Set<String> keys = new HashSet<String>();
		add.forEach((k, v) -> {
			if(k.startsWith(guild.getId()+"_"+channel_id)) {
				values.add(v.getValues().get(0));
				keys.add(k);
			}
		});
		if(values.size() > 0) {
			keys.parallelStream().forEach(k -> handleRequestRemoval(k, "add"));
			String [] array = Azrael.SQLgetGoogleFilesAndEvent(guild.getIdLong(), 2, event.id, channel_id);
			if(array != null && !array[0].equals("empty")) {
				try {
					GoogleSheets.appendRawDataToSpreadsheet(GoogleSheets.getSheetsClientService(), file_id, values, array[1], "ROWS");
					logger.info("Google spreadsheet batch append executed for file {} and event {} in guild {}", file_id, event.name(), guild.getId());
					removeCachedValueRange(event.name()+guild.getId()+channel_id);
				} catch (SocketTimeoutException e) {
					if(GoogleUtils.timeoutHandler(guild, file_id, event.name(), e)) {
						handleBatchAppend(guild, channel_id, file_id, add, event);
					}
				} catch (Exception e) {
					STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED), STATIC.getTranslation2(guild, Translation.GOOGLE_WEBSERVICE)+e.getMessage(), Channel.LOG.name());
					logger.error("Google Webservice error for event {} in guild {}", event, guild.getId(), e);
				}
			}
		}
	}
	
	private static void handleBatchRemove(Guild guild, String channel_id, String file_id, LinkedHashMap<String, ValueRange> remove, GoogleEvent event) {
		List<Request> values = new ArrayList<Request>();
		final Set<String> keys = new HashSet<String>();
		remove.forEach((k, v) -> {
			if(k.startsWith(guild.getId()+"_"+channel_id)) {
				values.add((Request)v.getValues().get(0).get(0));
				keys.add(k);
			}
		});
		if(values.size() > 0) {
			List<Request> sortedValues = new ArrayList<Request>();
			for(int i = 0; i < values.size(); i++) {
				boolean lowest = true;
				for(int y = 0; y < sortedValues.size(); y++) {
					if(sortedValues.get(y).getDeleteDimension().getRange().getEndIndex() < values.get(i).getDeleteDimension().getRange().getEndIndex()) {
						sortedValues.add(y, values.get(i));
						lowest = false;
						break;
					}
				}
				if(lowest)
					sortedValues.add(values.get(i));
			}
			keys.parallelStream().forEach(k -> handleRequestRemoval(k, "remove"));
			try {
				GoogleSheets.batchDeleteRowsOnSpreadsheet(GoogleSheets.getSheetsClientService(), file_id, sortedValues);
				logger.info("Google spreadsheet batch delete executed for file {} and event {} in guild {}", file_id, event.name(), guild.getId());
				removeCachedValueRange(event.name()+guild.getId()+channel_id);
			} catch (SocketTimeoutException e) {
				if(GoogleUtils.timeoutHandler(guild, file_id, event.name(), e)) {
					handleBatchRemove(guild, channel_id, file_id, add, event);
				}
			} catch (Exception e) {
				STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED), STATIC.getTranslation2(guild, Translation.GOOGLE_WEBSERVICE)+e.getMessage(), Channel.LOG.name());
				logger.error("Google Webservice error for event {} in guild {}", event, guild.getId(), e);
			}
		}
	}
	
	public static void cacheRetrievedSheetValueRange(String key, ValueRange valueRange) {
		wholeSpreadsheet.put(key, valueRange);
	}
	
	public static ValueRange getCachedValueRange(String key) {
		if(wholeSpreadsheet.contains(key))
			return wholeSpreadsheet.get(key);
		else 
			return null;
	}
	
	private static void removeCachedValueRange(String key) {
		wholeSpreadsheet.remove(key);
	}
}
