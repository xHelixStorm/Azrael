package commandsContainer;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;

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
				_e.getTextChannel().sendMessage(message.setDescription("Choose now the desired action:\n\n**display\ninsert\nremove\nload-file**").build()).queue();
				FileSetting.createFile(file_name, "word-filter");
				new Thread(new DelayDelete(file_name, 180000)).start();
				break;
			case "name-filter":
				message.setTitle("You chose the name-filter!");
				_e.getTextChannel().sendMessage(message.setDescription("Choose now the desired action:\n\n**display\ninsert\nremove\nload-file**").build()).queue();
				FileSetting.createFile(file_name, "name-filter");
				new Thread(new DelayDelete(file_name, 180000)).start();
				break;
			case "funny-names":
				message.setTitle("You chose funny-names!");
				_e.getTextChannel().sendMessage(message.setDescription("Choose now the desired action:\n\n**display\ninsert\nremove\nload-file**").build()).queue();
				FileSetting.createFile(file_name, "funny-names");
				new Thread(new DelayDelete(file_name, 180000)).start();
				break;
			case "staff-names":
				message.setTitle("You chose staff-names!");
				_e.getTextChannel().sendMessage(message.setDescription("Choose now the desired action:\n\n**display\ninsert\nremove\nload-file**").build()).queue();
				FileSetting.createFile(file_name, "staff-names");
				new Thread(new DelayDelete(file_name, 180000)).start();
				break;
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
						_e.getTextChannel().sendMessage(message.setDescription("Please choose a language for the new word!\n\n**"+(out.length() > 0 ? out.toString() : "<no languages available>")+"**").build()).queue();
						out.setLength(0);
						FileSetting.createFile(file_path, "insert-word-filter");
					}
					else if(_message.equalsIgnoreCase("remove")) {
						message.setTitle("You chose to remove a word from the filter!");
						StringBuilder out = new StringBuilder();
						for(String lang : Azrael.SQLgetFilterLanguages()) {
							out.append(lang+"\n");
						}
						_e.getTextChannel().sendMessage(message.setDescription("Please choose a language for the word you want to remove!\n\n**"+(out.length() > 0 ? out.toString() : "<no languages available>")+"**").build()).queue();
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
						Azrael.SQLgetNameFilter(_e.getGuild().getIdLong());
						StringBuilder out = new StringBuilder();
						for(String word : Hashes.getQuerryResult("bad-names_"+_e.getGuild().getId())) {
							out.append(word+"\n");
						}
						String paste_link = Pastebin.unlistedPaste("Name filter", out.toString(), _e.getGuild().getIdLong());
						message.setTitle("Name filter!");
						out.setLength(0);
						_e.getTextChannel().sendMessage(message.setDescription("Every name that includes one of this words, will receive a funny name: "+paste_link).build()).queue();
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
						_e.getTextChannel().sendMessage(message.setDescription("Please put a file named **words.txt** into the files folder of the bot.\nType **continue** if you're done.").build()).queue();
						FileSetting.createFile(file_path, "load-name-filter");
					}
					break;
				case "funny-names":
					if(_message.equalsIgnoreCase("display")) {
						StringBuilder out = new StringBuilder();
						for(String word : Azrael.SQLgetFunnyNames(_e.getGuild().getIdLong())) {
							out.append(word+"\n");
						}
						String paste_link = Pastebin.unlistedPaste("Funny names for the name filter", (out.length() > 0 ? out.toString() : "<No funny names found>"), _e.getGuild().getIdLong());
						message.setTitle("Funny names!");
						out.setLength(0);
						_e.getTextChannel().sendMessage(message.setDescription("A user will receive one of these names, if the name filter gets triggered: "+paste_link).build()).queue();
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
						_e.getTextChannel().sendMessage(message.setDescription("Please put a file named **words.txt** into the files folder of the bot.\nType **continue** if you're done.").build()).queue();
						FileSetting.createFile(file_path, "load-funny-names");
					}
					break;
				case "staff-names":
					if(_message.equalsIgnoreCase("display")) {
						StringBuilder out = new StringBuilder();
						for(String word : Azrael.SQLgetStaffNames(_e.getGuild().getIdLong())) {
							out.append(word+"\n");
						}
						String paste_link = Pastebin.unlistedPaste("Staff names for the name filter", (out.length() > 0 ? out.toString() : "<staff-names list is empty>"), _e.getGuild().getIdLong());
						message.setTitle("Staff names!");
						out.setLength(0);
						_e.getTextChannel().sendMessage(message.setDescription("Here the list to censor names containing staff names: "+paste_link).build()).queue();
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
						_e.getTextChannel().sendMessage(message.setDescription("Please put a file named **words.txt** into the files folder of the bot.\nType **continue** if you're done.").build()).queue();
						FileSetting.createFile(file_path, "load-staff-names");
					}
					break;
				case "display-word-filter":
					if(_message.equalsIgnoreCase("english")) {
						Azrael.SQLgetFilter("eng", _e.getGuild().getIdLong());
						StringBuilder out = new StringBuilder();
						for(String word : Hashes.getQuerryResult("eng_"+_e.getGuild().getIdLong())) {
							out.append(word+"\n");
						}
						String paste_link = Pastebin.unlistedPaste("English word filter", out.toString(), _e.getGuild().getIdLong());
						message.setTitle("English word filter!");
						out.setLength(0);
						_e.getTextChannel().sendMessage(message.setDescription("Here is the requested word filter: "+paste_link).build()).queue();
						logger.debug("{} has called the english word filter", _e.getMember().getUser().getIdLong());
						FileSetting.createFile(file_path, "complete");
					}
					else if(_message.equalsIgnoreCase("german")) {
						Azrael.SQLgetFilter("ger", _e.getGuild().getIdLong());
						StringBuilder out = new StringBuilder();
						for(String word : Hashes.getQuerryResult("ger_"+_e.getGuild().getIdLong())) {
							out.append(word+"\n");
						}
						String paste_link = Pastebin.unlistedPaste("German word filter", out.toString(), _e.getGuild().getIdLong());
						message.setTitle("German word filter!");
						out.setLength(0);
						_e.getTextChannel().sendMessage(message.setDescription("Here is the requested word filter: "+paste_link).build()).queue();
						logger.debug("{} has called the german word filter", _e.getMember().getUser().getIdLong());
						FileSetting.createFile(file_path, "complete");
					}
					else if(_message.equalsIgnoreCase("french")) {
						Azrael.SQLgetFilter("fre", _e.getGuild().getIdLong());
						StringBuilder out = new StringBuilder();
						for(String word : Hashes.getQuerryResult("fre_"+_e.getGuild().getIdLong())) {
							out.append(word+"\n");
						}
						String paste_link = Pastebin.unlistedPaste("French word filter", out.toString(), _e.getGuild().getIdLong());
						message.setTitle("French word filter!");
						out.setLength(0);
						_e.getTextChannel().sendMessage(message.setDescription("Here is the requested word filter: "+paste_link).build()).queue();
						logger.debug("{} has called the french word filter", _e.getMember().getUser().getIdLong());
						FileSetting.createFile(file_path, "complete");
					}
					else if(_message.equalsIgnoreCase("turkish")) {
						Azrael.SQLgetFilter("tur", _e.getGuild().getIdLong());
						StringBuilder out = new StringBuilder();
						for(String word : Hashes.getQuerryResult("tur_"+_e.getGuild().getIdLong())) {
							out.append(word+"\n");
						}
						String paste_link = Pastebin.unlistedPaste("Turkish word filter", out.toString(), _e.getGuild().getIdLong());
						message.setTitle("Turkish word filter!");
						out.setLength(0);
						_e.getTextChannel().sendMessage(message.setDescription("Here is the requested word filter: "+paste_link).build()).queue();
						logger.debug("{} has called the turkish word filter", _e.getMember().getUser().getIdLong());
						FileSetting.createFile(file_path, "complete");
					}
					else if(_message.equalsIgnoreCase("russian")) {
						Azrael.SQLgetFilter("rus", _e.getGuild().getIdLong());
						StringBuilder out = new StringBuilder();
						for(String word : Hashes.getQuerryResult("rus_"+_e.getGuild().getIdLong())) {
							out.append(word+"\n");
						}
						String paste_link = Pastebin.unlistedPaste("Russian word filter", out.toString(), _e.getGuild().getIdLong());
						message.setTitle("Russian word filter!");
						out.setLength(0);
						_e.getTextChannel().sendMessage(message.setDescription("Here is the requested word filter: "+paste_link).build()).queue();
						logger.debug("{} has called the russian word filter", _e.getMember().getUser().getIdLong());
						FileSetting.createFile(file_path, "complete");
					}
					else if(_message.equalsIgnoreCase("spanish")) {
						Azrael.SQLgetFilter("spa", _e.getGuild().getIdLong());
						StringBuilder out = new StringBuilder();
						for(String word : Hashes.getQuerryResult("eng_"+_e.getGuild().getIdLong())) {
							out.append(word+"\n");
						}
						String paste_link = Pastebin.unlistedPaste("Spanish word filter", out.toString(), _e.getGuild().getIdLong());
						message.setTitle("Spanish word filter!");
						out.setLength(0);
						_e.getTextChannel().sendMessage(message.setDescription("Here is the requested word filter: "+paste_link).build()).queue();
						logger.debug("{} has called the spanish word filter", _e.getMember().getUser().getIdLong());
						FileSetting.createFile(file_path, "complete");
					}
					else if(_message.equalsIgnoreCase("portuguese")) {
						Azrael.SQLgetFilter("por", _e.getGuild().getIdLong());
						StringBuilder out = new StringBuilder();
						for(String word : Hashes.getQuerryResult("por_"+_e.getGuild().getIdLong())) {
							out.append(word+"\n");
						}
						String paste_link = Pastebin.unlistedPaste("Portuguese word filter", out.toString(), _e.getGuild().getIdLong());
						message.setTitle("Portuguese word filter!");
						out.setLength(0);
						_e.getTextChannel().sendMessage(message.setDescription("Here is the requested word filter: "+paste_link).build()).queue();
						logger.debug("{} has called the portuguese word filter", _e.getMember().getUser().getIdLong());
						FileSetting.createFile(file_path, "complete");
					}
					else if(_message.equalsIgnoreCase("italian")) {
						Azrael.SQLgetFilter("ita", _e.getGuild().getIdLong());
						StringBuilder out = new StringBuilder();
						for(String word : Hashes.getQuerryResult("ita_"+_e.getGuild().getIdLong())) {
							out.append(word+"\n");
						}
						String paste_link = Pastebin.unlistedPaste("Italian word filter", out.toString(), _e.getGuild().getIdLong());
						message.setTitle("Italian word filter!");
						out.setLength(0);
						_e.getTextChannel().sendMessage(message.setDescription("Here is the requested word filter: "+paste_link).build()).queue();
						logger.debug("{} has called the italian word filter", _e.getMember().getUser().getIdLong());
						FileSetting.createFile(file_path, "complete");
					}
					break;
				case "insert-word-filter":
					if(_message.equalsIgnoreCase("english")) {
						message.setTitle("You chose to insert an english word!");
						_e.getTextChannel().sendMessage(message.setDescription("Please type the word").build()).queue();
						FileSetting.createFile(file_path, "english-insert-word-filter");
					}
					else if(_message.equalsIgnoreCase("german")) {
						message.setTitle("You chose to insert an german word!");
						_e.getTextChannel().sendMessage(message.setDescription("Please type the word").build()).queue();
						FileSetting.createFile(file_path, "german-insert-word-filter");
					}
					else if(_message.equalsIgnoreCase("french")) {
						message.setTitle("You chose to insert an french word!");
						_e.getTextChannel().sendMessage(message.setDescription("Please type the word").build()).queue();
						FileSetting.createFile(file_path, "french-insert-word-filter");
					}
					else if(_message.equalsIgnoreCase("turkish")) {
						message.setTitle("You chose to insert an turkish word!");
						_e.getTextChannel().sendMessage(message.setDescription("Please type the word").build()).queue();
						FileSetting.createFile(file_path, "turkish-insert-word-filter");
					}
					else if(_message.equalsIgnoreCase("russian")) {
						message.setTitle("You chose to insert an russian word!");
						_e.getTextChannel().sendMessage(message.setDescription("Please type the word").build()).queue();
						FileSetting.createFile(file_path, "russian-insert-word-filter");
					}
					else if(_message.equalsIgnoreCase("spanish")) {
						message.setTitle("You chose to insert an spanish word!");
						_e.getTextChannel().sendMessage(message.setDescription("Please type the word").build()).queue();
						FileSetting.createFile(file_path, "spanish-insert-word-filter");
					}
					else if(_message.equalsIgnoreCase("portuguese")) {
						message.setTitle("You chose to insert an portuguese word!");
						_e.getTextChannel().sendMessage(message.setDescription("Please type the word").build()).queue();
						FileSetting.createFile(file_path, "portuguese-insert-word-filter");
					}
					else if(_message.equalsIgnoreCase("italian")) {
						message.setTitle("You chose to insert an italian word!");
						_e.getTextChannel().sendMessage(message.setDescription("Please type the word").build()).queue();
						FileSetting.createFile(file_path, "italian-insert-word-filter");
					}
					break;
				case "english-insert-word-filter":
					if(Azrael.SQLInsertWordFilter("eng", _message, _e.getGuild().getIdLong()) > 0) {
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("The word has been inserted into the english word filter!").build()).queue();
						Hashes.removeQuerryResult("eng_"+_e.getGuild().getId());
						Hashes.removeQuerryResult("all_"+_e.getGuild().getId());
						logger.debug("{} has inserted the word {} into the english word filter", _e.getMember().getUser().getIdLong(), _message);
						FileSetting.createFile(file_path, "complete");
					}
					else {
						_e.getTextChannel().sendMessage("An internal error occurred. Word couldn't be inserted into the word-filter table.").queue();
						logger.error("Word couldn't be inserted into Azrael.filter for guild {}", _e.getGuild().getName());
					}
					break;
				case "german-insert-word-filter":
					if(Azrael.SQLInsertWordFilter("ger", _message, _e.getGuild().getIdLong()) > 0) {
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("The word has been inserted into the german word filter!").build()).queue();
						Hashes.removeQuerryResult("ger_"+_e.getGuild().getId());
						Hashes.removeQuerryResult("all_"+_e.getGuild().getId());
						logger.debug("{} has inserted the word {} into the german word filter", _e.getMember().getUser().getIdLong(), _message);
						FileSetting.createFile(file_path, "complete");
					}
					else {
						_e.getTextChannel().sendMessage("An internal error occurred. Word couldn't be inserted into the word-filter table.").queue();
						logger.error("Word couldn't be inserted into Azrael.filter for guild {}", _e.getGuild().getName());
					}
					break;
				case "french-insert-word-filter":
					if(Azrael.SQLInsertWordFilter("fre", _message, _e.getGuild().getIdLong()) > 0) {
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("The word has been inserted into the french word filter!").build()).queue();
						Hashes.removeQuerryResult("fre_"+_e.getGuild().getId());
						Hashes.removeQuerryResult("all_"+_e.getGuild().getId());
						logger.debug("{} has inserted the word {} into the french word filter", _e.getMember().getUser().getIdLong(), _message);
						FileSetting.createFile(file_path, "complete");
					}
					else {
						_e.getTextChannel().sendMessage("An internal error occurred. Word couldn't be inserted into the word-filter table.").queue();
						logger.error("Word couldn't be inserted into Azrael.filter for guild {}", _e.getGuild().getName());
					}
					break;
				case "turkish-insert-word-filter":
					if(Azrael.SQLInsertWordFilter("tur", _message, _e.getGuild().getIdLong()) > 0) {
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("The word has been inserted into the turkish word filter!").build()).queue();
						Hashes.removeQuerryResult("tur_"+_e.getGuild().getId());
						Hashes.removeQuerryResult("all_"+_e.getGuild().getId());
						logger.debug("{} has inserted the word {} into the turkish word filter", _e.getMember().getUser().getIdLong(), _message);
						FileSetting.createFile(file_path, "complete");
					}
					else {
						_e.getTextChannel().sendMessage("An internal error occurred. Word couldn't be inserted into the word-filter table.").queue();
						logger.error("Word couldn't be inserted into Azrael.filter for guild {}", _e.getGuild().getName());
					}
					break;
				case "russian-insert-word-filter":
					if(Azrael.SQLInsertWordFilter("rus", _message, _e.getGuild().getIdLong()) > 0) {
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("The word has been inserted into the russian word filter!").build()).queue();
						Hashes.removeQuerryResult("rus_"+_e.getGuild().getId());
						Hashes.removeQuerryResult("all_"+_e.getGuild().getId());
						logger.debug("{} has inserted the word {} into the russian word filter", _e.getMember().getUser().getIdLong(), _message);
						FileSetting.createFile(file_path, "complete");
					}
					else {
						_e.getTextChannel().sendMessage("An internal error occurred. Word couldn't be inserted into the word-filter table.").queue();
						logger.error("Word couldn't be inserted into Azrael.filter for guild {}", _e.getGuild().getName());
					}
					break;
				case "spanish-insert-word-filter":
					if(Azrael.SQLInsertWordFilter("spa", _message, _e.getGuild().getIdLong()) > 0) {
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("The word has been inserted into the spanish word filter!").build()).queue();
						Hashes.removeQuerryResult("spa_"+_e.getGuild().getId());
						Hashes.removeQuerryResult("all_"+_e.getGuild().getId());
						logger.debug("{} has inserted the word {} into the spanish word filter", _e.getMember().getUser().getIdLong(), _message);
						FileSetting.createFile(file_path, "complete");
					}
					else {
						_e.getTextChannel().sendMessage("An internal error occurred. Word couldn't be inserted into the word-filter table.").queue();
						logger.error("Word couldn't be inserted into Azrael.filter for guild {}", _e.getGuild().getName());
					}
					break;
				case "portuguese-insert-word-filter":
					if(Azrael.SQLInsertWordFilter("por", _message, _e.getGuild().getIdLong()) > 0) {
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("The word has been inserted into the portuguese word filter!").build()).queue();
						Hashes.removeQuerryResult("por_"+_e.getGuild().getId());
						Hashes.removeQuerryResult("all_"+_e.getGuild().getId());
						logger.debug("{} has inserted the word {} into the portuguese word filter", _e.getMember().getUser().getIdLong(), _message);
						FileSetting.createFile(file_path, "complete");
					}
					else {
						_e.getTextChannel().sendMessage("An internal error occurred. Word couldn't be inserted into the word-filter table.").queue();
						logger.error("Word couldn't be inserted into Azrael.filter for guild {}", _e.getGuild().getName());
					}
					break;
				case "italian-insert-word-filter":
					if(Azrael.SQLInsertWordFilter("ita", _message, _e.getGuild().getIdLong()) > 0) {
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("The word has been inserted into the italian word filter!").build()).queue();
						Hashes.removeQuerryResult("ita_"+_e.getGuild().getId());
						Hashes.removeQuerryResult("all_"+_e.getGuild().getId());
						logger.debug("{} has inserted the word {} into the italian word filter", _e.getMember().getUser().getIdLong(), _message);
						FileSetting.createFile(file_path, "complete");
					}
					else {
						_e.getTextChannel().sendMessage("An internal error occurred. Word couldn't be inserted into the word-filter table.").queue();
						logger.error("Word couldn't be inserted into Azrael.filter for guild {}", _e.getGuild().getName());
					}
					break;
				case "remove-word-filter":
					if(_message.equalsIgnoreCase("english")) {
						message.setTitle("You chose to remove an english word!");
						_e.getTextChannel().sendMessage(message.setDescription("Please type the word").build()).queue();
						FileSetting.createFile(file_path, "english-remove-word-filter");
					}
					else if(_message.equalsIgnoreCase("german")) {
						message.setTitle("You chose to remove an german word!");
						_e.getTextChannel().sendMessage(message.setDescription("Please type the word").build()).queue();
						FileSetting.createFile(file_path, "german-remove-word-filter");
					}
					else if(_message.equalsIgnoreCase("french")) {
						message.setTitle("You chose to remove an french word!");
						_e.getTextChannel().sendMessage(message.setDescription("Please type the word").build()).queue();
						FileSetting.createFile(file_path, "french-remove-word-filter");
					}
					else if(_message.equalsIgnoreCase("turkish")) {
						message.setTitle("You chose to remove an turkish word!");
						_e.getTextChannel().sendMessage(message.setDescription("Please type the word").build()).queue();
						FileSetting.createFile(file_path, "turkish-remove-word-filter");
					}
					else if(_message.equalsIgnoreCase("russian")) {
						message.setTitle("You chose to remove an russian word!");
						_e.getTextChannel().sendMessage(message.setDescription("Please type the word").build()).queue();
						FileSetting.createFile(file_path, "russian-remove-word-filter");
					}
					else if(_message.equalsIgnoreCase("spanish")) {
						message.setTitle("You chose to remove an spanish word!");
						_e.getTextChannel().sendMessage(message.setDescription("Please type the word").build()).queue();
						FileSetting.createFile(file_path, "spanish-remove-word-filter");
					}
					else if(_message.equalsIgnoreCase("portuguese")) {
						message.setTitle("You chose to remove an portuguese word!");
						_e.getTextChannel().sendMessage(message.setDescription("Please type the word").build()).queue();
						FileSetting.createFile(file_path, "portuguese-remove-word-filter");
					}
					else if(_message.equalsIgnoreCase("italian")) {
						message.setTitle("You chose to remove an italian word!");
						_e.getTextChannel().sendMessage(message.setDescription("Please type the word").build()).queue();
						FileSetting.createFile(file_path, "italian-remove-word-filter");
					}
					break;
				case "english-remove-word-filter":
					if(Azrael.SQLDeleteWordFilter("eng", _message, _e.getGuild().getIdLong()) > 0) {
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("The word has been removed from the english word filter!").build()).queue();
						Hashes.removeQuerryResult("eng_"+_e.getGuild().getId());
						Hashes.removeQuerryResult("all_"+_e.getGuild().getId());
						logger.debug("{} has removed the word {} from the english word filter", _e.getMember().getUser().getIdLong(), _message);
						FileSetting.createFile(file_path, "complete");
					}
					else {
						_e.getTextChannel().sendMessage("An internal error occurred. Word couldn't be removed from the word-filter").queue();
						logger.error("Word couldn't be removed from Azrael.filter in guild {}", _e.getGuild().getName());
					}
					break;
				case "german-remove-word-filter":
					if(Azrael.SQLDeleteWordFilter("ger", _message, _e.getGuild().getIdLong()) > 0) {
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("The word has been removed from the german word filter!").build()).queue();
						Hashes.removeQuerryResult("ger_"+_e.getGuild().getId());
						Hashes.removeQuerryResult("all_"+_e.getGuild().getId());
						logger.debug("{} has removed the word {} from the german word filter", _e.getMember().getUser().getIdLong(), _message);
						FileSetting.createFile(file_path, "complete");
					}
					else {
						_e.getTextChannel().sendMessage("An internal error occurred. Word couldn't be removed from the word-filter").queue();
						logger.error("Word couldn't be removed from Azrael.filter in guild {}", _e.getGuild().getName());
					}
					break;
				case "french-remove-word-filter":
					if(Azrael.SQLDeleteWordFilter("fre", _message, _e.getGuild().getIdLong()) > 0) {
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("The word has been removed from the french word filter!").build()).queue();
						Hashes.removeQuerryResult("fre_"+_e.getGuild().getId());
						Hashes.removeQuerryResult("all_"+_e.getGuild().getId());
						logger.debug("{} has removed the word {} from the french word filter", _e.getMember().getUser().getIdLong(), _message);
						FileSetting.createFile(file_path, "complete");
					}
					else {
						_e.getTextChannel().sendMessage("An internal error occurred. Word couldn't be removed from the word-filter").queue();
						logger.error("Word couldn't be removed from Azrael.filter in guild {}", _e.getGuild().getName());
					}
					break;
				case "turkish-remove-word-filter":
					if(Azrael.SQLDeleteWordFilter("tur", _message, _e.getGuild().getIdLong()) > 0) {
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("The word has been removed from the turkish word filter!").build()).queue();
						Hashes.removeQuerryResult("tur_"+_e.getGuild().getId());
						Hashes.removeQuerryResult("all_"+_e.getGuild().getId());
						logger.debug("{} has removed the word {} from the turkish word filter", _e.getMember().getUser().getIdLong(), _message);
						FileSetting.createFile(file_path, "complete");
					}
					else {
						_e.getTextChannel().sendMessage("An internal error occurred. Word couldn't be removed from the word-filter").queue();
						logger.error("Word couldn't be removed from Azrael.filter in guild {}", _e.getGuild().getName());
					}
					break;
				case "russian-remove-word-filter":
					if(Azrael.SQLDeleteWordFilter("rus", _message, _e.getGuild().getIdLong()) > 0) {
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("The word has been removed from the russian word filter!").build()).queue();
						Hashes.removeQuerryResult("rus_"+_e.getGuild().getId());
						Hashes.removeQuerryResult("all_"+_e.getGuild().getId());
						logger.debug("{} has removed the word {} from the russian word filter", _e.getMember().getUser().getIdLong(), _message);
						FileSetting.createFile(file_path, "complete");
					}
					else {
						_e.getTextChannel().sendMessage("An internal error occurred. Word couldn't be removed from the word-filter").queue();
						logger.error("Word couldn't be removed from Azrael.filter in guild {}", _e.getGuild().getName());
					}
					break;
				case "spanish-remove-word-filter":
					if(Azrael.SQLDeleteWordFilter("spa", _message, _e.getGuild().getIdLong()) > 0) {
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("The word has been removed from the spanish word filter!").build()).queue();
						Hashes.removeQuerryResult("spa_"+_e.getGuild().getId());
						Hashes.removeQuerryResult("all_"+_e.getGuild().getId());
						logger.debug("{} has removed the word {} from the spanish word filter", _e.getMember().getUser().getIdLong(), _message);
						FileSetting.createFile(file_path, "complete");
					}
					else {
						_e.getTextChannel().sendMessage("An internal error occurred. Word couldn't be removed from the word-filter").queue();
						logger.error("Word couldn't be removed from Azrael.filter in guild {}", _e.getGuild().getName());
					}
					break;
				case "portuguese-remove-word-filter":
					if(Azrael.SQLDeleteWordFilter("por", _message, _e.getGuild().getIdLong()) > 0) {
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("The word has been removed from the portuguese word filter!").build()).queue();
						Hashes.removeQuerryResult("por_"+_e.getGuild().getId());
						Hashes.removeQuerryResult("all_"+_e.getGuild().getId());
						logger.debug("{} has removed the word {} from the portuguese word filter", _e.getMember().getUser().getIdLong(), _message);
						FileSetting.createFile(file_path, "complete");
					}
					else {
						_e.getTextChannel().sendMessage("An internal error occurred. Word couldn't be removed from the word-filter").queue();
						logger.error("Word couldn't be removed from Azrael.filter in guild {}", _e.getGuild().getName());
					}
					break;
				case "italian-remove-word-filter":
					if(Azrael.SQLDeleteWordFilter("ita", _message, _e.getGuild().getIdLong()) > 0) {
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("The word has been removed from the italian word filter!").build()).queue();
						Hashes.removeQuerryResult("ita_"+_e.getGuild().getId());
						Hashes.removeQuerryResult("all_"+_e.getGuild().getId());
						logger.debug("{} has removed the word {} from the italian word filter", _e.getMember().getUser().getIdLong(), _message);
						FileSetting.createFile(file_path, "complete");
					}
					else {
						_e.getTextChannel().sendMessage("An internal error occurred. Word couldn't be removed from the word-filter").queue();
						logger.error("Word couldn't be removed from Azrael.filter in guild {}", _e.getGuild().getName());
					}
					break;
				case "load-word-filter":
					if(_message.equalsIgnoreCase("english")) {
						message.setTitle("You chose to add english words!");
						_e.getTextChannel().sendMessage(message.setDescription("Please put a file named **words.txt** into the files folder of the bot.\nType **continue** if you're done.").build()).queue();
						FileSetting.createFile(file_path, "english-load-word-filter");
					}
					else if(_message.equalsIgnoreCase("german")) {
						message.setTitle("You chose to add german words!");
						_e.getTextChannel().sendMessage(message.setDescription("Please put a file named **words.txt** into the files folder of the bot.\\nType **continue** if you're done.").build()).queue();
						FileSetting.createFile(file_path, "german-load-word-filter");
					}
					else if(_message.equalsIgnoreCase("french")) {
						message.setTitle("You chose to add french words!");
						_e.getTextChannel().sendMessage(message.setDescription("Please put a file named **words.txt** into the files folder of the bot.\\nType **continue** if you're done.").build()).queue();
						FileSetting.createFile(file_path, "french-load-word-filter");
					}
					else if(_message.equalsIgnoreCase("turkish")) {
						message.setTitle("You chose to add turkish words!");
						_e.getTextChannel().sendMessage(message.setDescription("Please put a file named **words.txt** into the files folder of the bot.\\nType **continue** if you're done.").build()).queue();
						FileSetting.createFile(file_path, "turkish-load-word-filter");
					}
					else if(_message.equalsIgnoreCase("russian")) {
						message.setTitle("You chose to add russian words!");
						_e.getTextChannel().sendMessage(message.setDescription("Please put a file named **words.txt** into the files folder of the bot.\\nType **continue** if you're done.").build()).queue();
						FileSetting.createFile(file_path, "russian-load-word-filter");
					}
					else if(_message.equalsIgnoreCase("spanish")) {
						message.setTitle("You chose to add spanish words!");
						_e.getTextChannel().sendMessage(message.setDescription("Please put a file named **words.txt** into the files folder of the bot.\\nType **continue** if you're done.").build()).queue();
						FileSetting.createFile(file_path, "spanish-load-word-filter");
					}
					else if(_message.equalsIgnoreCase("portuguese")) {
						message.setTitle("You chose to add portuguese words!");
						_e.getTextChannel().sendMessage(message.setDescription("Please put a file named **words.txt** into the files folder of the bot.\\nType **continue** if you're done.").build()).queue();
						FileSetting.createFile(file_path, "portuguese-load-word-filter");
					}
					else if(_message.equalsIgnoreCase("italian")) {
						message.setTitle("You chose to add italian words!");
						_e.getTextChannel().sendMessage(message.setDescription("Please put a file named **words.txt** into the files folder of the bot.\\nType **continue** if you're done.").build()).queue();
						FileSetting.createFile(file_path, "italian-load-word-filter");
					}
					break;
				case "english-load-word-filter":
					if(_message.equalsIgnoreCase("continue")) {
						ArrayList<String> words = new ArrayList<String>();
						words = FileSetting.readFileIntoArray("./files/words.txt");
						Azrael.SQLDeleteLangWordFilter("eng", _e.getGuild().getIdLong());
						Azrael.SQLReplaceWordFilter("eng", words, _e.getGuild().getIdLong());
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("Words have been inserted!").build()).queue();
						Hashes.removeQuerryResult("eng_"+_e.getGuild().getId());
						Hashes.removeQuerryResult("all_"+_e.getGuild().getId());
						logger.debug("{} has inserted words from a file into the english word filter", _e.getMember().getUser().getIdLong());
						FileSetting.createFile(file_path, "complete");
					}
					break;
				case "german-load-word-filter":
					if(_message.equalsIgnoreCase("continue")) {
						ArrayList<String> words = new ArrayList<String>();
						words = FileSetting.readFileIntoArray("./files/words.txt");
						Azrael.SQLDeleteLangWordFilter("ger", _e.getGuild().getIdLong());
						Azrael.SQLReplaceWordFilter("ger", words, _e.getGuild().getIdLong());
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("Words have been inserted!").build()).queue();
						Hashes.removeQuerryResult("ger_"+_e.getGuild().getId());
						Hashes.removeQuerryResult("all_"+_e.getGuild().getId());
						logger.debug("{} has inserted words from a file into the german word filter", _e.getMember().getUser().getIdLong());
						FileSetting.createFile(file_path, "complete");
					}
					break;
				case "french-load-word-filter":
					if(_message.equalsIgnoreCase("continue")) {
						ArrayList<String> words = new ArrayList<String>();
						words = FileSetting.readFileIntoArray("./files/words.txt");
						Azrael.SQLDeleteLangWordFilter("fre", _e.getGuild().getIdLong());
						Azrael.SQLReplaceWordFilter("fre", words, _e.getGuild().getIdLong());
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("Words have been inserted!").build()).queue();
						Hashes.removeQuerryResult("fre_"+_e.getGuild().getId());
						Hashes.removeQuerryResult("all_"+_e.getGuild().getId());
						logger.debug("{} has inserted words from a file into the french word filter", _e.getMember().getUser().getIdLong());
						FileSetting.createFile(file_path, "complete");
					}
					break;
				case "turkish-load-word-filter":
					if(_message.equalsIgnoreCase("continue")) {
						ArrayList<String> words = new ArrayList<String>();
						words = FileSetting.readFileIntoArray("./files/words.txt");
						Azrael.SQLDeleteLangWordFilter("tur", _e.getGuild().getIdLong());
						Azrael.SQLReplaceWordFilter("tur", words, _e.getGuild().getIdLong());
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("Words have been inserted!").build()).queue();
						Hashes.removeQuerryResult("tur_"+_e.getGuild().getId());
						Hashes.removeQuerryResult("all_"+_e.getGuild().getId());
						logger.debug("{} has inserted words from a file into the turkish word filter", _e.getMember().getUser().getIdLong());
						FileSetting.createFile(file_path, "complete");
					}
					break;
				case "russian-load-word-filter":
					if(_message.equalsIgnoreCase("continue")) {
						ArrayList<String> words = new ArrayList<String>();
						words = FileSetting.readFileIntoArray("./files/words.txt");
						Azrael.SQLDeleteLangWordFilter("rus", _e.getGuild().getIdLong());
						Azrael.SQLReplaceWordFilter("rus", words, _e.getGuild().getIdLong());
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("Words have been inserted!").build()).queue();
						Hashes.removeQuerryResult("rus_"+_e.getGuild().getId());
						Hashes.removeQuerryResult("all_"+_e.getGuild().getId());
						logger.debug("{} has inserted words from a file into the russian word filter", _e.getMember().getUser().getIdLong());
						FileSetting.createFile(file_path, "complete");
					}
					break;
				case "spanish-load-word-filter":
					if(_message.equalsIgnoreCase("continue")) {
						ArrayList<String> words = new ArrayList<String>();
						words = FileSetting.readFileIntoArray("./files/words.txt");
						Azrael.SQLDeleteLangWordFilter("spa", _e.getGuild().getIdLong());
						Azrael.SQLReplaceWordFilter("spa", words, _e.getGuild().getIdLong());
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("Words have been inserted!").build()).queue();
						Hashes.removeQuerryResult("spa_"+_e.getGuild().getId());
						Hashes.removeQuerryResult("all_"+_e.getGuild().getId());
						logger.debug("{} has inserted words from a file into the spanish word filter", _e.getMember().getUser().getIdLong());
						FileSetting.createFile(file_path, "complete");
					}
					break;
				case "portuguese-load-word-filter":
					if(_message.equalsIgnoreCase("continue")) {
						ArrayList<String> words = new ArrayList<String>();
						words = FileSetting.readFileIntoArray("./files/words.txt");
						Azrael.SQLDeleteLangWordFilter("por", _e.getGuild().getIdLong());
						Azrael.SQLReplaceWordFilter("por", words, _e.getGuild().getIdLong());
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("Words have been inserted!").build()).queue();
						Hashes.removeQuerryResult("por_"+_e.getGuild().getId());
						Hashes.removeQuerryResult("all_"+_e.getGuild().getId());
						logger.debug("{} has inserted words from a file into the portuguese word filter", _e.getMember().getUser().getIdLong());
						FileSetting.createFile(file_path, "complete");
					}
					break;
				case "italian-load-word-filter":
					if(_message.equalsIgnoreCase("continue")) {
						ArrayList<String> words = new ArrayList<String>();
						words = FileSetting.readFileIntoArray("./files/words.txt");
						Azrael.SQLDeleteLangWordFilter("ita", _e.getGuild().getIdLong());
						Azrael.SQLReplaceWordFilter("ita", words, _e.getGuild().getIdLong());
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("Words have been inserted!").build()).queue();
						Hashes.removeQuerryResult("ita_"+_e.getGuild().getId());
						Hashes.removeQuerryResult("all_"+_e.getGuild().getId());
						logger.debug("{} has inserted words from a file into the italian word filter", _e.getMember().getUser().getIdLong());
						FileSetting.createFile(file_path, "complete");
					}
					break;
				case "insert-name-filter":
					if(Azrael.SQLInsertNameFilter(_message, _e.getGuild().getIdLong()) > 0) {
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("The word has been inserted into name filter!").build()).queue();
						Hashes.removeQuerryResult("bad-names_"+_e.getGuild().getId());
						logger.debug("{} has inserted the word {} into the name filter", _e.getMember().getUser().getIdLong(), _message);
						FileSetting.createFile(file_path, "complete");
					}
					else {
						_e.getTextChannel().sendMessage("An internal error occurred. Name couldn't be inserted into Azrael.name_filter").queue();
						logger.error("Name couldn't be inserted into Azrael.name_filter for guild {}", _e.getGuild().getName());
					}
					break;
				case "remove-name-filter":
					if(Azrael.SQLDeleteNameFilter(_message, _e.getGuild().getIdLong()) > 0) {
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("The word has been removed from the name filter!").build()).queue();
						Hashes.removeQuerryResult("bad-names_"+_e.getGuild().getId());
						logger.debug("{} has removed the word {} from the name filter", _e.getMember().getUser().getIdLong(), _message);
						FileSetting.createFile(file_path, "complete");
					}
					else {
						_e.getTextChannel().sendMessage("Name couldn't be removed. Name doesn't exist").queue();
						logger.error("Name couldn't be removed from Azrael.name_filter for guild {}", _e.getGuild().getName());
					}
					break;
				case "load-name-filter":
					ArrayList<String> words = new ArrayList<String>();
					words = FileSetting.readFileIntoArray("./files/words.txt");
					Azrael.SQLDeleteWholeNameFilter(_e.getGuild().getIdLong());
					Azrael.SQLReplaceNameFilter(words, _e.getGuild().getIdLong());
					message.setTitle("Success!");
					_e.getTextChannel().sendMessage(message.setDescription("Words have been inserted!").build()).queue();
					Hashes.removeQuerryResult("bad-names_"+_e.getGuild().getId());
					logger.debug("{} has inserted words out from a file into the name filter", _e.getMember().getUser().getIdLong());
					FileSetting.createFile(file_path, "complete");
					break;
				case "insert-funny-names":
					if(Azrael.SQLInsertFunnyNames(_message, _e.getGuild().getIdLong()) > 0) {
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("The name has been inserted into the funny names list!").build()).queue();
						Hashes.removeQuerryResult("funny-names_"+_e.getGuild().getId());
						logger.debug("{} has inserted the word {} into the funny names", _e.getMember().getUser().getIdLong(), _message);
						FileSetting.createFile(file_path, "complete");
					}
					else {
						_e.getTextChannel().sendMessage("An internal error occurred. Name couldn't be inserted into Azrael.names").queue();
						logger.error("Name couldn't be inserted into Azrael.names for guild {}", _e.getGuild().getName());
					}
					break;
				case "remove-funny-names":
					if(Azrael.SQLDeleteFunnyNames(_message, _e.getGuild().getIdLong()) > 0) {
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("The name has been removed from the funny names list!").build()).queue();
						Hashes.removeQuerryResult("funny-names_"+_e.getGuild().getId());
						logger.debug("{} has removed the word {} from the funny names", _e.getMember().getUser().getIdLong(), _message);
						FileSetting.createFile(file_path, "complete");
					}
					else {
						_e.getTextChannel().sendMessage("Name couldn't be removed. Name doesn't exist").queue();
						logger.error("Name couldn't be removed from Azrael.names for guild {}", _e.getGuild().getName());
					}
					break;
				case "load-funny-names":
					ArrayList<String> names = new ArrayList<String>();
					names = FileSetting.readFileIntoArray("./files/words.txt");
					Azrael.SQLDeleteWholeFunnyNames(_e.getGuild().getIdLong());
					Azrael.SQLReplaceFunnyNames(names, _e.getGuild().getIdLong());
					message.setTitle("Success!");
					_e.getTextChannel().sendMessage(message.setDescription("Names have been inserted!").build()).queue();
					Hashes.removeQuerryResult("funny-names_"+_e.getGuild().getId());
					logger.debug("{} has inserted words out from a file into the funny names", _e.getMember().getUser().getIdLong());
					FileSetting.createFile(file_path, "complete");
					break;
				case "insert-staff-names":
					if(Azrael.SQLInsertStaffName(_message, _e.getGuild().getIdLong()) > 0) {
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("The name has been inserted into the staff names list!").build()).queue();
						Hashes.removeQuerryResult("staff-names_"+_e.getGuild().getId());
						logger.debug("{} has inserted the word {} into the staff names", _e.getMember().getUser().getIdLong(), _message);
						FileSetting.createFile(file_path, "complete");
					}
					else {
						_e.getTextChannel().sendMessage("An internal error occurred. Name couldn't be inserted into Azrael.staff_name_filter").queue();
						logger.error("Name couldn't be inserted into Azrael.staff_name_filter for guild {}", _e.getGuild().getName());
					}
					break;
				case "remove-staff-names":
					if(Azrael.SQLDeleteStaffNames(_message, _e.getGuild().getIdLong()) > 0) {
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("The name has been removed from the staff names list!").build()).queue();
						Hashes.removeQuerryResult("staff-names_"+_e.getGuild().getId());
						logger.debug("{} has removed the word {} from the staff names", _e.getMember().getUser().getIdLong(), _message);
						FileSetting.createFile(file_path, "complete");
					}
					else {
						_e.getTextChannel().sendMessage("Name couldn't be removed from Azrael.staff_names. Name doesn't exist").queue();
						logger.error("Name couldn't be removed from Azrael.staff_names for guild {}", _e.getGuild().getName());
					}
					break;
				case "load-staff-names":
					ArrayList<String> staff_names = new ArrayList<String>();
					staff_names = FileSetting.readFileIntoArray("./files/words.txt");
					Azrael.SQLDeleteWholeStaffNames(_e.getGuild().getIdLong());
					Azrael.SQLBatchInsertStaffNames(staff_names, _e.getGuild().getIdLong());
					message.setTitle("Success!");
					_e.getTextChannel().sendMessage(message.setDescription("Names have been inserted!").build()).queue();
					Hashes.removeQuerryResult("staff-names_"+_e.getGuild().getId());
					logger.debug("{} has inserted word out from a file into the staff names", _e.getMember().getUser().getIdLong());
					FileSetting.createFile(file_path, "complete");
					break;
			}
		}
	}
}