package commandsContainer;

import java.awt.Color;
import java.util.ArrayList;

import fileManagement.FileSetting;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.SqlConnect;
import threads.DelayDelete;
import util.Pastebin;

public class FilterExecution {
	public static void runHelp(MessageReceivedEvent _e) {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE).setTitle("Actions for the filter command!");
		_e.getTextChannel().sendMessage(message.setDescription("Type one of the following word types right after the command to choose a respective action! These types are available:\n\n**word-filter\nname-filter\nfunny-names**").build()).queue();
	}
	
	public static void runTask(MessageReceivedEvent _e, String _message) {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
		switch(_message) {
			case "word-filter":
				message.setTitle("You chose the word-filter!");
				_e.getTextChannel().sendMessage(message.setDescription("Choose now the desired action:\n\n**display\ninsert\nremove\nload-file**").build()).queue();
				FileSetting.createFile(IniFileReader.getTempDirectory()+"AutoDelFiles/filter_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId()+".azr", "word-filter");
				new Thread(new DelayDelete(IniFileReader.getTempDirectory()+"AutoDelFiles/filter_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId()+".azr", 180000, true)).start();
				break;
			case "name-filter":
				message.setTitle("You chose the name-filter!");
				_e.getTextChannel().sendMessage(message.setDescription("Choose now the desired action:\n\n**display\ninsert\nremove\nload-file**").build()).queue();
				FileSetting.createFile(IniFileReader.getTempDirectory()+"AutoDelFiles/filter_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId()+".azr", "name-filter");
				new Thread(new DelayDelete(IniFileReader.getTempDirectory()+"AutoDelFiles/filter_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId()+".azr", 180000, true)).start();
				break;
			case "funny-names":
				message.setTitle("You chose funny-names!");
				_e.getTextChannel().sendMessage(message.setDescription("Choose now the desired action:\n\n**display\ninsert\nremove\nload-file**").build()).queue();
				FileSetting.createFile(IniFileReader.getTempDirectory()+"AutoDelFiles/filter_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId()+".azr", "funny-names");
				new Thread(new DelayDelete(IniFileReader.getTempDirectory()+"AutoDelFiles/filter_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId()+".azr", 180000, true)).start();
				break;
		}
	}
	
	public static void performAction(MessageReceivedEvent _e, String _message) {
		EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Session Expired!");
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
		String file_path = IniFileReader.getTempDirectory()+"AutoDelFiles/filter_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId()+".azr";
		String file_value = FileSetting.readFile(file_path);
		if(!file_value.equals("expired")) {
			switch(file_value) {
				case "word-filter":
					if(_message.equalsIgnoreCase("display")) {
						message.setTitle("You chose to display the current word filter!");
						StringBuilder out = new StringBuilder();
						SqlConnect.SQLgetFilterLanguages();
						for(String lang : SqlConnect.getFilter_Lang()) {
							out.append(lang+"\n");
						}
						SqlConnect.clearFilter_Lang();
						_e.getTextChannel().sendMessage(message.setDescription("Please choose one of the available languages to display the filter!\n\n**"+out.toString()+"**").build()).queue();
						out.setLength(0);
						FileSetting.createFile(file_path, "display-word-filter");
					}
					else if(_message.equalsIgnoreCase("insert")) {
						message.setTitle("You chose to insert a new word into the filter!");
						StringBuilder out = new StringBuilder();
						SqlConnect.SQLgetFilterLanguages();
						for(String lang : SqlConnect.getFilter_Lang()) {
							out.append(lang+"\n");
						}
						SqlConnect.clearFilter_Lang();
						_e.getTextChannel().sendMessage(message.setDescription("Please choose a language for the new word!\n\n**"+out.toString()+"**").build()).queue();
						out.setLength(0);
						FileSetting.createFile(file_path, "insert-word-filter");
					}
					else if(_message.equalsIgnoreCase("remove")) {
						message.setTitle("You chose to remove a word from the filter!");
						StringBuilder out = new StringBuilder();
						SqlConnect.SQLgetFilterLanguages();
						for(String lang : SqlConnect.getFilter_Lang()) {
							out.append(lang+"\n");
						}
						SqlConnect.clearFilter_Lang();
						_e.getTextChannel().sendMessage(message.setDescription("Please choose a language for the word you want to remove!\n\n**"+out.toString()+"**").build()).queue();
						out.setLength(0);
						FileSetting.createFile(file_path, "remove-word-filter");
					}
					else if(_message.equalsIgnoreCase("load-file")) {
						message.setTitle("You chose to load a file which contains filter words!");
						StringBuilder out = new StringBuilder();
						SqlConnect.SQLgetFilterLanguages();
						for(String lang : SqlConnect.getFilter_Lang()) {
							out.append(lang+"\n");
						}
						SqlConnect.clearFilter_Lang();
						_e.getTextChannel().sendMessage(message.setDescription("Please choose a language for the words in the file you want to add!\n\n**"+out.toString()+"**").build()).queue();
						out.setLength(0);
						FileSetting.createFile(file_path, "load-word-filter");
					}
					break;
				case "name-filter":
					if(_message.equalsIgnoreCase("display")) {
						SqlConnect.SQLgetNameFilter();
						StringBuilder out = new StringBuilder();
						for(String word : SqlConnect.getNames()) {
							out.append(word+"\n");
						}
						SqlConnect.clearNames();
						String paste_link = Pastebin.unlistedPaste("Name filter", out.toString());
						message.setTitle("Name filter!");
						out.setLength(0);
						_e.getTextChannel().sendMessage(message.setDescription("Every name that includes one of this words, will receive a funny name: "+paste_link).build()).queue();
						FileSetting.deleteFile(file_path);
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
						SqlConnect.SQLgetFunnyNames();
						StringBuilder out = new StringBuilder();
						for(String word : SqlConnect.getNames()) {
							out.append(word+"\n");
						}
						SqlConnect.clearNames();
						String paste_link = Pastebin.unlistedPaste("Funny names for the name filter", out.toString());
						message.setTitle("Funny names!");
						out.setLength(0);
						_e.getTextChannel().sendMessage(message.setDescription("A user will receive one of these names, if the name filter gets triggered: "+paste_link).build()).queue();
						FileSetting.deleteFile(file_path);
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
				case "display-word-filter":
					if(_message.equalsIgnoreCase("english")) {
						SqlConnect.SQLgetFilter("eng");
						StringBuilder out = new StringBuilder();
						for(String word : SqlConnect.getFilter_Words()) {
							out.append(word+"\n");
						}
						SqlConnect.clearFilter_Words();
						String paste_link = Pastebin.unlistedPaste("English word filter", out.toString());
						message.setTitle("English word filter!");
						out.setLength(0);
						_e.getTextChannel().sendMessage(message.setDescription("Here is the requested word filter: "+paste_link).build()).queue();
						FileSetting.deleteFile(file_path);
					}
					else if(_message.equalsIgnoreCase("german")) {
						SqlConnect.SQLgetFilter("ger");
						StringBuilder out = new StringBuilder();
						for(String word : SqlConnect.getFilter_Words()) {
							out.append(word+"\n");
						}
						SqlConnect.clearFilter_Words();
						String paste_link = Pastebin.unlistedPaste("German word filter", out.toString());
						message.setTitle("German word filter!");
						out.setLength(0);
						_e.getTextChannel().sendMessage(message.setDescription("Here is the requested word filter: "+paste_link).build()).queue();
						FileSetting.deleteFile(file_path);
					}
					else if(_message.equalsIgnoreCase("french")) {
						SqlConnect.SQLgetFilter("fre");
						StringBuilder out = new StringBuilder();
						for(String word : SqlConnect.getFilter_Words()) {
							out.append(word+"\n");
						}
						SqlConnect.clearFilter_Words();
						String paste_link = Pastebin.unlistedPaste("French word filter", out.toString());
						message.setTitle("French word filter!");
						out.setLength(0);
						_e.getTextChannel().sendMessage(message.setDescription("Here is the requested word filter: "+paste_link).build()).queue();
						FileSetting.deleteFile(file_path);
					}
					else if(_message.equalsIgnoreCase("turkish")) {
						SqlConnect.SQLgetFilter("tur");
						StringBuilder out = new StringBuilder();
						for(String word : SqlConnect.getFilter_Words()) {
							out.append(word+"\n");
						}
						SqlConnect.clearFilter_Words();
						String paste_link = Pastebin.unlistedPaste("Turkish word filter", out.toString());
						message.setTitle("Turkish word filter!");
						out.setLength(0);
						_e.getTextChannel().sendMessage(message.setDescription("Here is the requested word filter: "+paste_link).build()).queue();
						FileSetting.deleteFile(file_path);
					}
					else if(_message.equalsIgnoreCase("russian")) {
						SqlConnect.SQLgetFilter("rus");
						StringBuilder out = new StringBuilder();
						for(String word : SqlConnect.getFilter_Words()) {
							out.append(word+"\n");
						}
						SqlConnect.clearFilter_Words();
						String paste_link = Pastebin.unlistedPaste("Russian word filter", out.toString());
						message.setTitle("Russian word filter!");
						out.setLength(0);
						_e.getTextChannel().sendMessage(message.setDescription("Here is the requested word filter: "+paste_link).build()).queue();
						FileSetting.deleteFile(file_path);
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
					break;
				case "english-insert-word-filter":
					SqlConnect.SQLInsertWordFilter("eng", _message);
					message.setTitle("Success!");
					_e.getTextChannel().sendMessage(message.setDescription("The word has been inserted into the english word filter!").build()).queue();
					FileSetting.deleteFile(file_path);
					break;
				case "german-insert-word-filter":
					SqlConnect.SQLInsertWordFilter("ger", _message);
					message.setTitle("Success!");
					_e.getTextChannel().sendMessage(message.setDescription("The word has been inserted into the german word filter!").build()).queue();
					FileSetting.deleteFile(file_path);
					break;
				case "french-insert-word-filter":
					SqlConnect.SQLInsertWordFilter("fre", _message);
					message.setTitle("Success!");
					_e.getTextChannel().sendMessage(message.setDescription("The word has been inserted into the french word filter!").build()).queue();
					FileSetting.deleteFile(file_path);
					break;
				case "turkish-insert-word-filter":
					SqlConnect.SQLInsertWordFilter("tur", _message);
					message.setTitle("Success!");
					_e.getTextChannel().sendMessage(message.setDescription("The word has been inserted into the turkish word filter!").build()).queue();
					FileSetting.deleteFile(file_path);
					break;
				case "russian-insert-word-filter":
					SqlConnect.SQLInsertWordFilter("rus", _message);
					message.setTitle("Success!");
					_e.getTextChannel().sendMessage(message.setDescription("The word has been inserted into the russian word filter!").build()).queue();
					FileSetting.deleteFile(file_path);
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
					break;
				case "english-remove-word-filter":
					SqlConnect.SQLDeleteWordFilter("eng", _message);
					message.setTitle("Success!");
					_e.getTextChannel().sendMessage(message.setDescription("The word has been removed from the english word filter!").build()).queue();
					FileSetting.deleteFile(file_path);
					break;
				case "german-remove-word-filter":
					SqlConnect.SQLDeleteWordFilter("ger", _message);
					message.setTitle("Success!");
					_e.getTextChannel().sendMessage(message.setDescription("The word has been removed from the german word filter!").build()).queue();
					FileSetting.deleteFile(file_path);
					break;
				case "french-remove-word-filter":
					SqlConnect.SQLDeleteWordFilter("fre", _message);
					message.setTitle("Success!");
					_e.getTextChannel().sendMessage(message.setDescription("The word has been removed from the french word filter!").build()).queue();
					FileSetting.deleteFile(file_path);
					break;
				case "turkish-remove-word-filter":
					SqlConnect.SQLDeleteWordFilter("tur", _message);
					message.setTitle("Success!");
					_e.getTextChannel().sendMessage(message.setDescription("The word has been removed from the turkish word filter!").build()).queue();
					FileSetting.deleteFile(file_path);
					break;
				case "russian-remove-word-filter":
					SqlConnect.SQLDeleteWordFilter("rus", _message);
					message.setTitle("Success!");
					_e.getTextChannel().sendMessage(message.setDescription("The word has been removed from the russian word filter!").build()).queue();
					FileSetting.deleteFile(file_path);
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
					break;
				case "english-load-word-filter":
					if(_message.equalsIgnoreCase("continue")) {
						ArrayList<String> words = new ArrayList<String>();
						words = FileSetting.readFileIntoArray("./files/words.txt");
						SqlConnect.SQLDeleteLangWordFilter("eng");
						SqlConnect.SQLReplaceWordFilter("eng", words);
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("Words have been inserted!").build()).queue();
						FileSetting.deleteFile(file_path);
					}
					break;
				case "german-load-word-filter":
					if(_message.equalsIgnoreCase("continue")) {
						ArrayList<String> words = new ArrayList<String>();
						words = FileSetting.readFileIntoArray("./files/words.txt");
						SqlConnect.SQLDeleteLangWordFilter("ger");
						SqlConnect.SQLReplaceWordFilter("ger", words);
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("Words have been inserted!").build()).queue();
						FileSetting.deleteFile(file_path);
					}
					break;
				case "french-load-word-filter":
					if(_message.equalsIgnoreCase("continue")) {
						ArrayList<String> words = new ArrayList<String>();
						words = FileSetting.readFileIntoArray("./files/words.txt");
						SqlConnect.SQLDeleteLangWordFilter("fre");
						SqlConnect.SQLReplaceWordFilter("fre", words);
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("Words have been inserted!").build()).queue();
						FileSetting.deleteFile(file_path);
					}
					break;
				case "turkish-load-word-filter":
					if(_message.equalsIgnoreCase("continue")) {
						ArrayList<String> words = new ArrayList<String>();
						words = FileSetting.readFileIntoArray("./files/words.txt");
						SqlConnect.SQLDeleteLangWordFilter("tur");
						SqlConnect.SQLReplaceWordFilter("tur", words);
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("Words have been inserted!").build()).queue();
						FileSetting.deleteFile(file_path);
					}
					break;
				case "russian-load-word-filter":
					if(_message.equalsIgnoreCase("continue")) {
						ArrayList<String> words = new ArrayList<String>();
						words = FileSetting.readFileIntoArray("./files/words.txt");
						SqlConnect.SQLDeleteLangWordFilter("rus");
						SqlConnect.SQLReplaceWordFilter("rus", words);
						message.setTitle("Success!");
						_e.getTextChannel().sendMessage(message.setDescription("Words have been inserted!").build()).queue();
						FileSetting.deleteFile(file_path);
					}
					break;
				case "insert-name-filter":
					SqlConnect.SQLInsertNameFilter(_message);
					message.setTitle("Success!");
					_e.getTextChannel().sendMessage(message.setDescription("The word has been inserted into name filter!").build()).queue();
					FileSetting.deleteFile(file_path);
					break;
				case "remove-name-filter":
					SqlConnect.SQLDeleteNameFilter(_message);
					message.setTitle("Success!");
					_e.getTextChannel().sendMessage(message.setDescription("The word has been removed from the name filter!").build()).queue();
					FileSetting.deleteFile(file_path);
					break;
				case "load-name-filter":
					ArrayList<String> words = new ArrayList<String>();
					words = FileSetting.readFileIntoArray("./files/words.txt");
					SqlConnect.SQLDeleteWholeNameFilter();
					SqlConnect.SQLReplaceNameFilter(words);
					message.setTitle("Success!");
					_e.getTextChannel().sendMessage(message.setDescription("Words have been inserted!").build()).queue();
					FileSetting.deleteFile(file_path);
					break;
				case "insert-funny-names":
					SqlConnect.SQLInsertFunnyNames(_message);
					message.setTitle("Success!");
					_e.getTextChannel().sendMessage(message.setDescription("The name has been inserted into the funny names list!").build()).queue();
					FileSetting.deleteFile(file_path);
					break;
				case "remove-funny-names":
					SqlConnect.SQLDeleteFunnyNames(_message);
					message.setTitle("Success!");
					_e.getTextChannel().sendMessage(message.setDescription("The name has been removed from the funny names list!").build()).queue();
					FileSetting.deleteFile(file_path);
					break;
				case "load-funny-names":
					ArrayList<String> names = new ArrayList<String>();
					names = FileSetting.readFileIntoArray("./files/words.txt");
					SqlConnect.SQLDeleteWholeFunnyNames();
					SqlConnect.SQLReplaceFunnyNames(names);
					message.setTitle("Success!");
					_e.getTextChannel().sendMessage(message.setDescription("Names have been inserted!").build()).queue();
					FileSetting.deleteFile(file_path);
					break;
					
			}
		}
		else {
			_e.getTextChannel().sendMessage(denied.setDescription("Session has expired! Please retype the command!").build()).queue();
			FileSetting.deleteFile(IniFileReader.getTempDirectory()+"AutoDelFiles/filter_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId()+".azr");
		}
	}
}
