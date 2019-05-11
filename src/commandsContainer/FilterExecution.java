package commandsContainer;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import fileManagement.FileSetting;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.Azrael;
import threads.DelayDelete;
import util.Pastebin;

public class FilterExecution {
	public static void runHelp(MessageReceivedEvent _e) {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE).setTitle("Actions for the filter command!");
		_e.getTextChannel().sendMessage(message.setDescription("Type one of the following word types right after the command to choose a respective action! These types are available:\n\n**word-filter\nname-filter\nfunny-names\nstaff-names**").build()).queue();
	}
	
	public static void runTask(MessageReceivedEvent _e, String _message) {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
		File file = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/filter_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId()+"_0.azr");
		String file_name = IniFileReader.getTempDirectory()+"AutoDelFiles/filter_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId()+"_0.azr";
		boolean break_while = false;
		int i = 0;
		
		while(i < 19 && break_while == false){
			if(file.exists()){
				file = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/filter_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId()+"_"+(i+1)+".azr");
				file_name = IniFileReader.getTempDirectory()+"AutoDelFiles/filter_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId()+"_"+(i+1)+".azr";
			}
			else{
				break_while = true;
			}
			i++;
		}
		
		switch(_message) {
			case "word-filter":
				message.setTitle("You chose the word-filter!");
				_e.getTextChannel().sendMessage(message.setDescription("Choose now the desired action:\n\n**display\ninsert\nremove\nload-pastebin**").build()).queue();
				FileSetting.createFile(file_name, "word-filter");
				new Thread(new DelayDelete(file_name, 180000)).start();
				break;
			case "name-filter":
				message.setTitle("You chose the name-filter!");
				_e.getTextChannel().sendMessage(message.setDescription("Choose now the desired action:\n\n**display\ninsert\nremove\nload-pastebin**").build()).queue();
				FileSetting.createFile(file_name, "name-filter");
				new Thread(new DelayDelete(file_name, 180000)).start();
				break;
			case "funny-names":
				message.setTitle("You chose funny-names!");
				_e.getTextChannel().sendMessage(message.setDescription("Choose now the desired action:\n\n**display\ninsert\nremove\nload-pastebin**").build()).queue();
				FileSetting.createFile(file_name, "funny-names");
				new Thread(new DelayDelete(file_name, 180000)).start();
				break;
			case "staff-names":
				message.setTitle("You chose staff-names!");
				_e.getTextChannel().sendMessage(message.setDescription("Choose now the desired action:\n\n**display\ninsert\nremove\nload-pastebin**").build()).queue();
				FileSetting.createFile(file_name, "staff-names");
				new Thread(new DelayDelete(file_name, 180000)).start();
				break;
			default:
				_e.getTextChannel().sendMessage("Please choose between word-filter, name-filter, funny-names or staff-names").queue();
		}
	}
	
	public static void performAction(MessageReceivedEvent _e, String _message, String _file_name) {
		Logger logger = LoggerFactory.getLogger(FilterExecution.class);
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
		String file_path = _file_name;
		String file_value = FileSetting.readFile(file_path);
		if(!file_value.equals("complete")) {
			switch(file_value) {
				case "word-filter":
					if(_message.equalsIgnoreCase("display")) {
						message.setTitle("You chose to display the current word filter!");
						StringBuilder out = new StringBuilder();
						for(String lang : Azrael.SQLgetFilterLanguages()) {
							out.append(lang+"\n");
						}
						_e.getTextChannel().sendMessage(message.setDescription("Please choose one of the available languages to display the filter!\n\n**"+(out.length() > 0 ? out.toString() : "<no languages available>")+"**").build()).queue();
						out.setLength(0);
						FileSetting.createFile(file_path, "display-word-filter");
					}
					else if(_message.equalsIgnoreCase("insert")) {
						message.setTitle("You chose to insert a new word into the filter!");
						StringBuilder out = new StringBuilder();
						for(String lang : Azrael.SQLgetFilterLanguages()) {
							out.append(lang+"\n");
						}
						_e.getTextChannel().sendMessage(message.setDescription("Please choose a language for the new word!\n\n**"+(out.length() > 0 ? out.toString()+"All" : "<no languages available>")+"**").build()).queue();
						out.setLength(0);
						FileSetting.createFile(file_path, "insert-word-filter");
					}
					else if(_message.equalsIgnoreCase("remove")) {
						message.setTitle("You chose to remove a word from the filter!");
						StringBuilder out = new StringBuilder();
						for(String lang : Azrael.SQLgetFilterLanguages()) {
							out.append(lang+"\n");
						}
						_e.getTextChannel().sendMessage(message.setDescription("Please choose a language for the word you want to remove!\n\n**"+(out.length() > 0 ? out.toString()+"All" : "<no languages available>")+"**").build()).queue();
						out.setLength(0);
						FileSetting.createFile(file_path, "remove-word-filter");
					}
					else if(_message.equalsIgnoreCase("load-file")) {
						message.setTitle("You chose to load a file which contains filter words!");
						StringBuilder out = new StringBuilder();
						for(String lang : Azrael.SQLgetFilterLanguages()) {
							out.append(lang+"\n");
						}
						_e.getTextChannel().sendMessage(message.setDescription("Please choose a language for the words in the file you want to add!\n\n**"+(out.length() > 0 ? out.toString() : "<no languages available>")+"**").build()).queue();
						out.setLength(0);
						FileSetting.createFile(file_path, "load-word-filter");
					}
					break;
				case "name-filter":
					if(_message.equalsIgnoreCase("display")) {
						StringBuilder out = new StringBuilder();
						for(String word : Azrael.SQLgetNameFilter(_e.getGuild().getIdLong())) {
							out.append(word+"\n");
						}
						if(out.length() > 0) {
							String paste_link = Pastebin.unlistedPaste("Name filter", out.toString(), _e.getGuild().getIdLong());
							if(!paste_link.equals("Creating paste failed!")) {
								message.setTitle("Name filter!");
								out.setLength(0);
								_e.getTextChannel().sendMessage(message.setDescription("Every name that includes one of this words, will receive a funny name: "+paste_link).build()).queue();
							}
							else {
								message.setColor(Color.RED).setTitle(paste_link);
								_e.getTextChannel().sendMessage(message.setDescription("An error occurred with posting on pastebin. Please verify that the login credentials are set correctly!").build()).queue();
							}
						}
						else {
							message.setColor(Color.RED).setTitle("No results have been returned!");
							_e.getTextChannel().sendMessage("The name-filter is empty! Nothing to display!").queue();
						}
						FileSetting.createFile(file_path, "complete");
					}
					else if(_message.equalsIgnoreCase("insert")) {
						message.setTitle("You chose to insert a new word into the name filter!");
						_e.getTextChannel().sendMessage(message.setDescription("Please insert a word into the text field!").build()).queue();
						FileSetting.createFile(file_path, "insert-name-filter");
					}
					else if(_message.equalsIgnoreCase("remove")) {
						message.setTitle("You chose to remove a word from the name filter!");
						_e.getTextChannel().sendMessage(message.setDescription("Please insert a word into the text field!").build()).queue();
						FileSetting.createFile(file_path, "remove-name-filter");
					}
					else if(_message.equalsIgnoreCase("load-file")) {
						message.setTitle("You chose to add the words from a file into the name filter!");
						_e.getTextChannel().sendMessage(message.setDescription("Please submit a public pastebin link with all required names to upload.").build()).queue();
						FileSetting.createFile(file_path, "load-name-filter");
					}
					break;
				case "funny-names":
					if(_message.equalsIgnoreCase("display")) {
						StringBuilder out = new StringBuilder();
						for(String word : Azrael.SQLgetFunnyNames(_e.getGuild().getIdLong())) {
							out.append(word+"\n");
						}
						if(out.length() > 0) {
							String paste_link = Pastebin.unlistedPaste("Funny names for the name filter", out.toString(), _e.getGuild().getIdLong());
							if(!paste_link.equals("Creating paste failed!")) {
								message.setTitle("Funny names!");
								out.setLength(0);
								_e.getTextChannel().sendMessage(message.setDescription("A user will receive one of these names, if the name filter gets triggered: "+paste_link).build()).queue();
							}
							else {
								message.setColor(Color.RED).setTitle(paste_link);
								_e.getTextChannel().sendMessage(message.setDescription("An error occurred with posting on pastebin. Please verify that the login credentials are set correctly!").build()).queue();
							}
						}
						else {
							message.setColor(Color.RED).setTitle("No results have been returned!");
							_e.getTextChannel().sendMessage("The funny-names is empty! Nothing to display!").queue();
						}
						FileSetting.createFile(file_path, "complete");
					}
					else if(_message.equalsIgnoreCase("insert")) {
						message.setTitle("You chose to insert a new name into the funny names for the name filter!");
						_e.getTextChannel().sendMessage(message.setDescription("Please insert a name into the text field!").build()).queue();
						FileSetting.createFile(file_path, "insert-funny-names");
					}
					else if(_message.equalsIgnoreCase("remove")) {
						message.setTitle("You chose to remove a name out of the funny names!");
						_e.getTextChannel().sendMessage(message.setDescription("Please insert a name into the text field!").build()).queue();
						FileSetting.createFile(file_path, "remove-funny-names");
					}
					else if(_message.equalsIgnoreCase("load-file")) {
						message.setTitle("You chose to add the names from a file into the funny names list!");
						_e.getTextChannel().sendMessage(message.setDescription("Please submit a public pastebin link with all required names to upload.").build()).queue();
						FileSetting.createFile(file_path, "load-funny-names");
					}
					break;
				case "staff-names":
					if(_message.equalsIgnoreCase("display")) {
						StringBuilder out = new StringBuilder();
						for(String word : Azrael.SQLgetStaffNames(_e.getGuild().getIdLong())) {
							out.append(word+"\n");
						}
						if(out.length() > 0) {
							String paste_link = Pastebin.unlistedPaste("Staff names for the name filter", out.toString(), _e.getGuild().getIdLong());
							if(!paste_link.equals("Creating paste failed!")) {
								message.setTitle("Staff names!");
								out.setLength(0);
								_e.getTextChannel().sendMessage(message.setDescription("Here the list to censor names containing staff names: "+paste_link).build()).queue();
							}
							else {
								message.setColor(Color.RED).setTitle(paste_link);
								_e.getTextChannel().sendMessage(message.setDescription("An error occurred with posting on pastebin. Please verify that the login credentials are set correctly!").build()).queue();
							}
						}
						else {
							message.setColor(Color.RED).setTitle("No results have been returned!");
							_e.getTextChannel().sendMessage("The staff-names is empty! Nothing to display!").queue();
						}
						FileSetting.createFile(file_path, "complete");
					}
					else if(_message.equalsIgnoreCase("insert")) {
						message.setTitle("You chose to insert a new name into the staff names for the name filter!");
						_e.getTextChannel().sendMessage(message.setDescription("Please insert a name into the text field!").build()).queue();
						FileSetting.createFile(file_path, "insert-staff-names");
					}
					else if(_message.equalsIgnoreCase("remove")) {
						message.setTitle("You chose to remove a name out of the staff names!");
						_e.getTextChannel().sendMessage(message.setDescription("Please insert a name into the text field!").build()).queue();
						FileSetting.createFile(file_path, "remove-staff-names");
					}
					else if(_message.equalsIgnoreCase("load-file")) {
						message.setTitle("You chose to add the names from a file into the staff names list!");
						_e.getTextChannel().sendMessage(message.setDescription("Please submit a public pastebin link with all required names to upload.").build()).queue();
						FileSetting.createFile(file_path, "load-staff-names");
					}
					break;
				case "display-word-filter":
					callFilterLangContent(_e, message, logger, file_path, _message.toLowerCase());
					break;
				case "insert-word-filter":
					var langInsert = _message.toLowerCase();
					if(langInsert.equalsIgnoreCase("english") || langInsert.equalsIgnoreCase("german") || langInsert.equalsIgnoreCase("french") || langInsert.equalsIgnoreCase("turkish") || langInsert.equalsIgnoreCase("russian") || 
							langInsert.equalsIgnoreCase("spanish") || langInsert.equalsIgnoreCase("portuguese") || langInsert.equalsIgnoreCase("italian") || langInsert.equalsIgnoreCase("all")) {
						message.setTitle("You chose to insert an "+langInsert+" word!");
						_e.getTextChannel().sendMessage(message.setDescription("Please type the word").build()).queue();
						FileSetting.createFile(file_path, langInsert+"-insert-word-filter");
					}
					break;
				case "english-insert-word-filter":
					insertLangWord(_e, message, logger, file_path, file_value.split("-")[0], _message);
					break;
				case "german-insert-word-filter":
					insertLangWord(_e, message, logger, file_path, file_value.split("-")[0], _message);
					break;
				case "french-insert-word-filter":
					insertLangWord(_e, message, logger, file_path, file_value.split("-")[0], _message);
					break;
				case "turkish-insert-word-filter":
					insertLangWord(_e, message, logger, file_path, file_value.split("-")[0], _message);
					break;
				case "russian-insert-word-filter":
					insertLangWord(_e, message, logger, file_path, file_value.split("-")[0], _message);
					break;
				case "spanish-insert-word-filter":
					insertLangWord(_e, message, logger, file_path, file_value.split("-")[0], _message);
					break;
				case "portuguese-insert-word-filter":
					insertLangWord(_e, message, logger, file_path, file_value.split("-")[0], _message);
					break;
				case "italian-insert-word-filter":
					insertLangWord(_e, message, logger, file_path, file_value.split("-")[0], _message);
					break;
				case "all-insert-word-filter":
					insertLangWord(_e, message, logger, file_path, file_value.split("-")[0], _message);
					break;
				case "remove-word-filter":
					var langRemove = _message.toLowerCase();
					if(langRemove.equalsIgnoreCase("english") || langRemove.equalsIgnoreCase("german") || langRemove.equalsIgnoreCase("french") || langRemove.equalsIgnoreCase("turkish") || langRemove.equalsIgnoreCase("russian") || 
							langRemove.equalsIgnoreCase("spanish") || langRemove.equalsIgnoreCase("portuguese") || langRemove.equalsIgnoreCase("italian") || langRemove.equalsIgnoreCase("all")) {
						message.setTitle("You chose to remove an "+langRemove+" word!");
						_e.getTextChannel().sendMessage(message.setDescription("Please type the word").build()).queue();
						FileSetting.createFile(file_path, langRemove+"-remove-word-filter");
					}
					break;
				case "english-remove-word-filter":
					removeLangWord(_e, message, logger, file_path, file_value.split("-")[0], _message);
					break;
				case "german-remove-word-filter":
					removeLangWord(_e, message, logger, file_path, file_value.split("-")[0], _message);
					break;
				case "french-remove-word-filter":
					removeLangWord(_e, message, logger, file_path, file_value.split("-")[0], _message);
					break;
				case "turkish-remove-word-filter":
					removeLangWord(_e, message, logger, file_path, file_value.split("-")[0], _message);
					break;
				case "russian-remove-word-filter":
					removeLangWord(_e, message, logger, file_path, file_value.split("-")[0], _message);
					break;
				case "spanish-remove-word-filter":
					removeLangWord(_e, message, logger, file_path, file_value.split("-")[0], _message);
					break;
				case "portuguese-remove-word-filter":
					removeLangWord(_e, message, logger, file_path, file_value.split("-")[0], _message);
					break;
				case "italian-remove-word-filter":
					removeLangWord(_e, message, logger, file_path, file_value.split("-")[0], _message);
					break;
				case "all-remove-word-filter":
					removeLangWord(_e, message, logger, file_path, file_value.split("-")[0], _message);
					break;
				case "load-word-filter":
					var langLoad = _message.toLowerCase();
					if(langLoad.equalsIgnoreCase("english") || langLoad.equalsIgnoreCase("german") || langLoad.equalsIgnoreCase("french") || langLoad.equalsIgnoreCase("turkish") || langLoad.equalsIgnoreCase("russian") || 
							langLoad.equalsIgnoreCase("spanish") || langLoad.equalsIgnoreCase("portuguese") || langLoad.equalsIgnoreCase("italian")) {
						message.setTitle("You chose to add "+langLoad+" words!");
						_e.getTextChannel().sendMessage(message.setDescription("Please submit a public pastebin link with all required words to upload.").build()).queue();
						FileSetting.createFile(file_path, langLoad+"-load-word-filter");
					}
					break;
				case "english-load-word-filter":
					loadLangWords(_e, message, logger, file_path, file_value.split("-")[0], _message);
					break;
				case "german-load-word-filter":
					loadLangWords(_e, message, logger, file_path, file_value.split("-")[0], _message);
					break;
				case "french-load-word-filter":
					loadLangWords(_e, message, logger, file_path, file_value.split("-")[0], _message);
					break;
				case "turkish-load-word-filter":
					loadLangWords(_e, message, logger, file_path, file_value.split("-")[0], _message);
					break;
				case "russian-load-word-filter":
					loadLangWords(_e, message, logger, file_path, file_value.split("-")[0], _message);
					break;
				case "spanish-load-word-filter":
					loadLangWords(_e, message, logger, file_path, file_value.split("-")[0], _message);
					break;
				case "portuguese-load-word-filter":
					loadLangWords(_e, message, logger, file_path, file_value.split("-")[0], _message);
					break;
				case "italian-load-word-filter":
					loadLangWords(_e, message, logger, file_path, file_value.split("-")[0], _message);
					break;
				case "insert-name-filter":
					if(Azrael.SQLInsertNameFilter(_message, _e.getGuild().getIdLong()) > 0) {
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("The word has been inserted into name filter!").build()).queue();
						Hashes.removeQuerryResult("bad-names_"+_e.getGuild().getId());
						logger.debug("{} has inserted the word {} into the name filter", _e.getMember().getUser().getIdLong(), _message);
					}
					else {
						message.setColor(Color.RED).setTitle("Name couldn't be inserted!");
						_e.getTextChannel().sendMessage("Name couldn't be inserted into Azrael.name_filter. Either the name already exists or an internal error has occurred!").queue();
						logger.error("Name couldn't be inserted into Azrael.name_filter for guild {}", _e.getGuild().getName());
					}
					FileSetting.createFile(file_path, "complete");
					break;
				case "remove-name-filter":
					if(Azrael.SQLDeleteNameFilter(_message, _e.getGuild().getIdLong()) > 0) {
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("The word has been removed from the name filter!").build()).queue();
						Hashes.removeQuerryResult("bad-names_"+_e.getGuild().getId());
						logger.debug("{} has removed the word {} from the name filter", _e.getMember().getUser().getIdLong(), _message);
					}
					else {
						message.setColor(Color.RED).setTitle("Name couldn't be removed!");
						_e.getTextChannel().sendMessage(message.setDescription("Name couldn't be removed. Name doesn't exist or an internal error occurred!").build()).queue();
						logger.error("Name couldn't be removed from Azrael.name_filter for guild {}", _e.getGuild().getName());
					}
					FileSetting.createFile(file_path, "complete");
					break;
				case "load-name-filter":
					if(_message.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _message.startsWith("http")) {
						String [] words = Pastebin.readPublicPasteLink(_message, _e.getGuild().getIdLong()).split("[\\r\\n]+");
						if(!words[0].equals("Reading paste failed!") && !words[0].equals("Error with this ID!")) {
							var querryResult = Azrael.SQLReplaceNameFilter(words, _e.getGuild().getIdLong());
							if(querryResult == 0) {
								message.setTitle("Success!");
								_e.getTextChannel().sendMessage(message.setDescription("Words have been inserted!").build()).queue();
								Hashes.removeQuerryResult("bad-names_"+_e.getGuild().getId());
								logger.debug("{} has inserted words out of pastebin into the name filter", _e.getMember().getUser().getIdLong());
							}
							else if(querryResult == 1) {
								//throw error for failing the db replacement
								message.setColor(Color.RED).setTitle("Execution failed");
								var duplicates = checkDuplicates(words);
								if(duplicates == null || duplicates.size() == 0) {
									_e.getTextChannel().sendMessage(message.setDescription("An unexpected error occurred while replacing the current name-filter with the names from inside the pastebin link!").build()).queue();
									logger.warn("The name-filter couldn't be updated in guild {}", _e.getGuild().getId());
								}
								else {
									StringBuilder out = new StringBuilder();
									for(var word : duplicates) {
										out.append("**"+word+"**\n");
									}
									_e.getTextChannel().sendMessage(message.setDescription("Words couldn't be loaded from the pastebin link because duplicates have been found. Please remove these duplicates and then try again!\n\n").build()).queue();
								}
							}
							else {
								//thow error for failing the rollback
								message.setColor(Color.RED).setTitle("Execution failed");
								_e.getTextChannel().sendMessage(message.setDescription("A critical error occurred. The filter table has been altered but couldn't be reverted on error. Current filter data could have been lost!").build()).queue();
								logger.error("Update on name-filter table couldn't be rolled back on error. Affected guild {}", _e.getGuild().getId());
							}
						}
						else {
							message.setColor(Color.RED).setTitle("Invalid pastebin link!");
							_e.getTextChannel().sendMessage(message.setDescription("Please provide a valid pastebin link from https://pastebin.com!").build()).queue();
						}
						FileSetting.createFile(file_path, "complete");
					}
					break;
				case "insert-funny-names":
					if(Azrael.SQLInsertFunnyNames(_message, _e.getGuild().getIdLong()) > 0) {
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("The name has been inserted into the funny names list!").build()).queue();
						Hashes.removeQuerryResult("funny-names_"+_e.getGuild().getId());
						logger.debug("{} has inserted the word {} into the funny names", _e.getMember().getUser().getIdLong(), _message);
					}
					else {
						message.setColor(Color.RED).setTitle("Name couldn't be inserted!");
						_e.getTextChannel().sendMessage(message.setDescription("Name couldn't be inserted into Azrael.names. Either the name already exists or an internal error has occurred!").build()).queue();
						logger.error("Name couldn't be inserted into Azrael.names for guild {}", _e.getGuild().getName());
					}
					FileSetting.createFile(file_path, "complete");
					break;
				case "remove-funny-names":
					if(Azrael.SQLDeleteFunnyNames(_message, _e.getGuild().getIdLong()) > 0) {
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("The name has been removed from the funny names list!").build()).queue();
						Hashes.removeQuerryResult("funny-names_"+_e.getGuild().getId());
						logger.debug("{} has removed the word {} from the funny names", _e.getMember().getUser().getIdLong(), _message);
					}
					else {
						message.setColor(Color.RED).setTitle("Name couldn't be removed!");
						_e.getTextChannel().sendMessage(message.setDescription("Name couldn't be removed. Name doesn't exist or an internal error occurred!").build()).queue();
						logger.error("Name couldn't be removed from Azrael.names for guild {}", _e.getGuild().getName());
					}
					FileSetting.createFile(file_path, "complete");
					break;
				case "load-funny-names":
					if(_message.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _message.startsWith("http")) {
						String [] words = Pastebin.readPublicPasteLink(_message, _e.getGuild().getIdLong()).split("[\\r\\n]+");
						if(!words[0].equals("Reading paste failed!") && !words[0].equals("Error with this ID!")) {
							var querryResult = Azrael.SQLReplaceFunnyNames(words, _e.getGuild().getIdLong());
							if(querryResult == 0) {
								message.setTitle("Success!");
								_e.getTextChannel().sendMessage(message.setDescription("Names have been inserted!").build()).queue();
								Hashes.removeQuerryResult("funny-names_"+_e.getGuild().getId());
								logger.debug("{} has inserted words out of pastebin into the funny-names", _e.getMember().getUser().getIdLong());
							}
							else if(querryResult == 1) {
								//throw error for failing the db replacement
								message.setColor(Color.RED).setTitle("Execution failed");
								var duplicates = checkDuplicates(words);
								if(duplicates == null || duplicates.size() == 0) {
									_e.getTextChannel().sendMessage(message.setDescription("An unexpected error occurred while replacing the current funny-names with the names from inside the pastebin link!").build()).queue();
									logger.warn("The name-filter couldn't be updated in guild {}", _e.getGuild().getId());
								}
								else {
									StringBuilder out = new StringBuilder();
									for(var word : duplicates) {
										out.append("**"+word+"**\n");
									}
									_e.getTextChannel().sendMessage(message.setDescription("Names couldn't be loaded from the pastebin link because duplicates have been found. Please remove these duplicates and then try again!\n\n").build()).queue();
								}
							}
							else {
								//thow error for failing the rollback
								message.setColor(Color.RED).setTitle("Execution failed");
								_e.getTextChannel().sendMessage(message.setDescription("A critical error occurred. The filter table has been altered but couldn't be reverted on error. Current filter data could have been lost!").build()).queue();
								logger.error("Update on funny-names table couldn't be rolled back on error. Affected guild {}", _e.getGuild().getId());
							}
						}
						else {
							message.setColor(Color.RED).setTitle("Invalid pastebin link!");
							_e.getTextChannel().sendMessage(message.setDescription("Please provide a valid pastebin link from https://pastebin.com!").build()).queue();
						}
						FileSetting.createFile(file_path, "complete");
					}
					break;
				case "insert-staff-names":
					if(Azrael.SQLInsertStaffName(_message, _e.getGuild().getIdLong()) > 0) {
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("The name has been inserted into the staff names list!").build()).queue();
						Hashes.removeQuerryResult("staff-names_"+_e.getGuild().getId());
						logger.debug("{} has inserted the word {} into the staff names", _e.getMember().getUser().getIdLong(), _message);
					}
					else {
						message.setColor(Color.RED).setTitle("Name couldn't be inserted!");
						_e.getTextChannel().sendMessage(message.setDescription("Name couldn't be inserted into Azrael.staff_name_filter. Either the name already exists or an internal error has occurred!").build()).queue();
						logger.error("Name couldn't be inserted into Azrael.staff_name_filter for guild {}", _e.getGuild().getName());
					}
					FileSetting.createFile(file_path, "complete");
					break;
				case "remove-staff-names":
					if(Azrael.SQLDeleteStaffNames(_message, _e.getGuild().getIdLong()) > 0) {
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("The name has been removed from the staff names list!").build()).queue();
						Hashes.removeQuerryResult("staff-names_"+_e.getGuild().getId());
						logger.debug("{} has removed the word {} from the staff names", _e.getMember().getUser().getIdLong(), _message);
					}
					else {
						message.setColor(Color.RED).setTitle("Name couldn't be removed!");
						_e.getTextChannel().sendMessage(message.setDescription("Name couldn't be removed from Azrael.staff_names. Name doesn't exist or an internal error occurred!").build()).queue();
						logger.error("Name couldn't be removed from Azrael.staff_names for guild {}", _e.getGuild().getName());
					}
					FileSetting.createFile(file_path, "complete");
					break;
				case "load-staff-names":
					if(_message.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _message.startsWith("http")) {
						String [] words = Pastebin.readPublicPasteLink(_message, _e.getGuild().getIdLong()).split("[\\r\\n]+");
						if(!words[0].equals("Reading paste failed!") && !words[0].equals("Error with this ID!")) {
							var querryResult = Azrael.SQLReplaceStaffNames(words, _e.getGuild().getIdLong());
							if(querryResult == 0) {
								message.setTitle("Success!");
								_e.getTextChannel().sendMessage(message.setDescription("Names have been inserted!").build()).queue();
								Hashes.removeQuerryResult("staff-names_"+_e.getGuild().getId());
								logger.debug("{} has inserted words out of pastebin into the staff-names", _e.getMember().getUser().getIdLong());
							}
							else if(querryResult == 1) {
								//throw error for failing the db replacement
								message.setColor(Color.RED).setTitle("Execution failed");
								var duplicates = checkDuplicates(words);
								if(duplicates == null || duplicates.size() == 0) {
									_e.getTextChannel().sendMessage(message.setDescription("An unexpected error occurred while replacing the current staff-names with the names from inside the pastebin link!").build()).queue();
									logger.warn("The staff-names couldn't be updated in guild {}", _e.getGuild().getId());
								}
								else {
									StringBuilder out = new StringBuilder();
									for(var word : duplicates) {
										out.append("**"+word+"**\n");
									}
									_e.getTextChannel().sendMessage(message.setDescription("Names couldn't be loaded from the pastebin link because duplicates have been found. Please remove these duplicates and then try again!\n\n").build()).queue();
								}
							}
							else {
								//thow error for failing the rollback
								message.setColor(Color.RED).setTitle("Execution failed");
								_e.getTextChannel().sendMessage(message.setDescription("A critical error occurred. The staf-names table has been altered but couldn't be reverted on error. Current names data could have been lost!").build()).queue();
								logger.error("Update on staff-names table couldn't be rolled back on error. Affected guild {}", _e.getGuild().getId());
							}
						}
						else {
							message.setColor(Color.RED).setTitle("Invalid pastebin link!");
							_e.getTextChannel().sendMessage(message.setDescription("Please provide a valid pastebin link from https://pastebin.com!").build()).queue();
						}
						FileSetting.createFile(file_path, "complete");
					}
					break;
			}
		}
	}
	
	private static void callFilterLangContent(MessageReceivedEvent _e, EmbedBuilder message, Logger logger, final String file_path, final String lang) {
		var langAbbreviation = "";
		var definitiveLang = "";
		switch(lang) {
			case "english":    langAbbreviation = "eng";  definitiveLang = "English";    break;
			case "german":     langAbbreviation = "ger";  definitiveLang = "German";     break;
			case "french":     langAbbreviation = "fre";  definitiveLang = "French";     break;
			case "turkish":    langAbbreviation = "tur";  definitiveLang = "Turkish";    break;
			case "russian":    langAbbreviation = "rus";  definitiveLang = "Russian";    break;
			case "spanish":    langAbbreviation = "spa";  definitiveLang = "Spanish";    break;
			case "portuguese": langAbbreviation = "por";  definitiveLang = "Portuguese"; break;
			case "italian":    langAbbreviation = "ita";  definitiveLang = "Italian";    break;
		}
		
		StringBuilder out = new StringBuilder();
		for(String word : Azrael.SQLgetFilter(langAbbreviation, _e.getGuild().getIdLong())) {
			out.append(word+"\n");
		}
		if(out.length() > 0) {
			String paste_link = Pastebin.unlistedPaste(definitiveLang+" word filter", out.toString(), _e.getGuild().getIdLong());
			if(!paste_link.equals("Creating paste failed!")) {
				message.setTitle(definitiveLang+" word filter!");
				out.setLength(0);
				_e.getTextChannel().sendMessage(message.setDescription("Here is the requested word filter: "+paste_link).build()).queue();
				logger.debug("{} has called the "+lang+" word filter", _e.getMember().getUser().getIdLong());
			}
			else {
				message.setColor(Color.RED).setTitle(paste_link);
				_e.getTextChannel().sendMessage(message.setDescription("An error occurred with posting on pastebin. Please verify that the login credentials are set correctly!").build()).queue();
			}
		}
		else {
			message.setColor(Color.RED).setTitle("No results have been returned!");
			_e.getTextChannel().sendMessage("The filter for this language is empty! Nothing to display!").queue();
		}
		FileSetting.createFile(file_path, "complete");
	}
	
	private static void insertLangWord(MessageReceivedEvent _e, EmbedBuilder message, Logger logger, final String file_path, final String lang, String word) {
		if(Azrael.SQLInsertWordFilter(lang.substring(0, 3), word, _e.getGuild().getIdLong()) > 0) {
			message.setTitle("Success!");
			_e.getTextChannel().sendMessage(message.setDescription("The word has been inserted into the "+lang+" word filter!").build()).queue();
			clearHash(_e, lang, true);
			Hashes.removeQuerryResult("all_"+_e.getGuild().getId());
			logger.debug("{} has inserted the word {} into the "+lang+" word filter", _e.getMember().getUser().getIdLong(), word);
		}
		else {
			message.setColor(Color.RED).setTitle("Word couldn't be inserted!");
			_e.getTextChannel().sendMessage(message.setDescription("Word couldn't be inserted into the word-filter table. Either the word already exists or an internal error has occurred!").build()).queue();
			logger.error("Word couldn't be inserted into Azrael.filter for guild {}", _e.getGuild().getName());
		}
		FileSetting.createFile(file_path, "complete");
	}
	
	private static void removeLangWord(MessageReceivedEvent _e, EmbedBuilder message, Logger logger, final String file_path, final String lang, String word) {
		if(Azrael.SQLDeleteWordFilter(lang.substring(0, 3), word, _e.getGuild().getIdLong()) > 0) {
			message.setTitle("Success!");
			_e.getTextChannel().sendMessage(message.setDescription("The word has been removed from the "+lang+" word filter!").build()).queue();
			clearHash(_e, lang, true);
			Hashes.removeQuerryResult("all_"+_e.getGuild().getId());
			logger.debug("{} has removed the word {} from the english word filter", _e.getMember().getUser().getIdLong(), word);
		}
		else {
			message.setColor(Color.RED).setTitle("Word couldn't be inserted!");
			_e.getTextChannel().sendMessage(message.setDescription("An internal error occurred. Word couldn't be removed from the word-filter. Either the word wasn't inside the filter or an internal error occurred!").build()).queue();
			logger.error("Word couldn't be removed from Azrael.filter in guild {}", _e.getGuild().getName());
		}
		FileSetting.createFile(file_path, "complete");
	}
	
	private static void loadLangWords(MessageReceivedEvent _e, EmbedBuilder message, Logger logger, final String file_path, final String lang, String _message) {
		if(_message.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _message.startsWith("http")) {
			var langAbbreviation = lang.substring(0, 3);
			String [] words = Pastebin.readPublicPasteLink(_message, _e.getGuild().getIdLong()).split("[\\r\\n]+");
			if(!words[0].equals("Reading paste failed!") && !words[0].equals("Error with this ID!")) {
				var querryResult = Azrael.SQLReplaceWordFilter(langAbbreviation, words, _e.getGuild().getIdLong());
				if(querryResult == 0) {
					message.setTitle("Success!");
					_e.getTextChannel().sendMessage(message.setDescription("Words have been inserted!").build()).queue();
					clearHash(_e, lang, false);
					Hashes.removeQuerryResult("all_"+_e.getGuild().getId());
					logger.debug("{} has inserted words from a file into the "+lang+" word filter", _e.getMember().getUser().getIdLong());
				}
				else if(querryResult == 1) {
					//throw error for failing the db replacement
					message.setColor(Color.RED).setTitle("Execution failed");
					var duplicates = checkDuplicates(words);
					if(duplicates == null || duplicates.size() == 0) {
						_e.getTextChannel().sendMessage(message.setDescription("An unexpected error occurred while replacing the current lang filter with the words from inside the pastebin link!").build()).queue();
						logger.warn("The {} filter couldn't be updated in guild {}", lang, _e.getGuild().getId());
					}
					else {
						StringBuilder out = new StringBuilder();
						for(var word : duplicates) {
							out.append("**"+word+"**\n");
						}
						_e.getTextChannel().sendMessage(message.setDescription("Words couldn't be loaded from the pastebin link because duplicates have been found. Please remove these duplicates and then try again!\n\n"+out.toString()).build()).queue();
					}
				}
				else {
					//throw error for failing the rollback
					message.setColor(Color.RED).setTitle("Execution failed");
					_e.getTextChannel().sendMessage(message.setDescription("A critical error occurred. The filter table has been altered but couldn't be reverted on error. Current filter data could have been lost!").build()).queue();
					logger.error("Update on filter table couldn't be rolled back on error. Affected language is {} for guild {}", lang, _e.getGuild().getId());
				}
			}
			else {
				message.setColor(Color.RED).setTitle("Invalid pastebin link!");
				_e.getTextChannel().sendMessage(message.setDescription("Please provide a valid pastebin link from https://pastebin.com!").build()).queue();
			}
			FileSetting.createFile(file_path, "complete");
		}
	}
	
	private static void clearHash(MessageReceivedEvent _e, final String lang, final boolean allowAll) {
		if(lang.equals("english") || (allowAll && lang.equals("all")))
			Hashes.removeQuerryResult("eng_"+_e.getGuild().getId());
		if(lang.equals("german") || (allowAll && lang.equals("all")))
			Hashes.removeQuerryResult("ger_"+_e.getGuild().getId());
		if(lang.equals("french") || (allowAll && lang.equals("all")))
			Hashes.removeQuerryResult("fre_"+_e.getGuild().getId());
		if(lang.equals("turkish") || (allowAll && lang.equals("all")))
			Hashes.removeQuerryResult("tur_"+_e.getGuild().getId());
		if(lang.equals("russian") || (allowAll && lang.equals("all")))
			Hashes.removeQuerryResult("rus_"+_e.getGuild().getId());
		if(lang.equals("spanish") || (allowAll && lang.equals("all")))
			Hashes.removeQuerryResult("spa_"+_e.getGuild().getId());
		if(lang.equals("portuguese") || (allowAll && lang.equals("all")))
			Hashes.removeQuerryResult("por_"+_e.getGuild().getId());
		if(lang.equals("italian") || (allowAll && lang.equals("all")))
			Hashes.removeQuerryResult("ita_"+_e.getGuild().getId());
	}
	
	private static List<String> checkDuplicates(String [] words) {
		ArrayList<String> duplicates = new ArrayList<String>();
		for(int i = 0; i < words.length; i++) {
			var word = words[i];
			for(int y = 0; y < words.length; y++) {
				final var iteration = y;
				if(i != y && word.equalsIgnoreCase(words[y]) && duplicates.parallelStream().filter(f -> f.equalsIgnoreCase(words[iteration])).findAny().orElse(null) == null) {
					duplicates.add(words[y]);
					break;
				}
			}
		}
		return duplicates;
	}
}