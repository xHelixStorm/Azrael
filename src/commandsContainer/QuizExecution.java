package commandsContainer;

import java.awt.Color;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import core.Quizes;
import fileManagement.FileSetting;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import util.Pastebin;

public class QuizExecution {
	private static final Logger logger = LoggerFactory.getLogger(QuizExecution.class);
	
	public static void registerRewards(MessageReceivedEvent e, String _link) {
		//check if it is a link that was inserted and if yes call readPublicPasteLink and then
		//split the returned String in an array
		if(_link.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _link.startsWith("http")) {
			String [] rewards = Pastebin.readPublicPasteLink(_link, e.getGuild().getIdLong()).split("[\\r\\n]+");
			
			if(!rewards[0].equals("Reading paste failed!")) {
				int index = 1;
				boolean interrupted = false;
				Quizes quiz;
				//Insert the rewards into the HashMap
				for(String reward: rewards) {
					if(reward.length() > 0) {
						if(!reward.equals("START") && !reward.matches("(1|2|3|4|5|6|7|8|9)[0-9]*[.][\\s\\d\\w?!.\\,/+-]*")) {
							if(Hashes.getQuiz(index) == null) {
								quiz = new Quizes();
							}
							else {
								quiz = Hashes.getQuiz(index);
							}
							quiz.setReward(reward);
							Hashes.addQuiz(index, quiz);
							index++;
						}
						else {
							interrupted = true;
							break;
						}
					}
				}
				//if the HashMap is bigger than the current index, then either clear the rest of the HashMap
				//or set the reward field to blank in case questions exist
				if(interrupted == false) {
					clearRewards(index);
				}

				//print message that it either worked or that an error occurred. Print error if there's any
				if(interrupted == false) {
					String integrity = IntegrityCheck();
					if(integrity.equals("0")) {
						e.getTextChannel().sendMessage("All rewards have been registered successfully!").queue();
						logger.debug("Quiz rewards have been registered");
					}
					else {
						e.getTextChannel().sendMessage("An error occured while registering the rewards. All inserted rewards have been cleared. Please check the error log:\n"
								+ ""+Pastebin.unlistedPaste("Error on registering rewards", integrity, e.getGuild().getIdLong())).queue();
						logger.error("Quiz rewards couldn't be registered");
						clearRewards(1);
					}
				}
				else {
					e.getTextChannel().sendMessage("Please don't try to register questions with this parameter! Please register only rewards!").queue();
					clearRewards(1);
				}
			}
			else {
				EmbedBuilder error = new EmbedBuilder().setTitle(rewards[0]).setColor(Color.RED);
				e.getTextChannel().sendMessage(error.setDescription("Please ensure that a valid Pastebin link has been inserted and that the API key inside the config.ini file is correct").build()).queue();
				logger.error("internal pastebin execution error for Quiz reward registration");
			}
		}
		else {
			EmbedBuilder error = new EmbedBuilder().setTitle("Invalid url!").setColor(Color.RED);
			e.getTextChannel().sendMessage(error.setDescription("An invalid url has been inserted. Please insert a Pastebin link").build()).queue();
			logger.warn("Wrong pastebin link has been inserted for the reward registration");
		}
	}
	
	public static void registerQuestions(MessageReceivedEvent e, String _link, boolean _readFile) {
		//check if it is a link that was inserted and if yes call readPublicPasteLink and then
		//split the returned String in an array. Or if it's being registered from a file, the file should be checked
		if((_link.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _link.startsWith("http") && _readFile == false)
				|| (new File("./files/QuizBackup/quizsettings.azr").exists() && _readFile == true)) {
			String [] content;
			if(_readFile == false) {
				content = Pastebin.readPublicPasteLink(_link, e.getGuild().getIdLong()).split("[\\r\\n]+");
			}
			else {
				content = FileSetting.readFileIntoFixedArray("./files/QuizBackup/quizsettings.azr");
			}
			
			if(!content[0].equals("Reading paste failed!") || content.length == 0) {
				int index = 1;
				int answers = 0;
				int hints = 0;
				int rewards = 0;
				Quizes quiz = new Quizes();
				//Insert questions, answers and hints into the HashMap
				for(String line : content) {
					if(line.length() > 0) {
						if(line.contains("START")) {
							if(Hashes.getQuiz(index) == null) {
								if(index != 1) {
									quiz = new Quizes();
								}
							}
							else {
								quiz = Hashes.getQuiz(index);
							}
						}
						else if(line.matches("(1|2|3|4|5|6|7|8|9)[0-9]*[.][\\s\\d\\w?!.\\,/+-]*")) {
							quiz.setQuestion(line);
							answers = 0;
							hints = 0;
							rewards = 0;
						}
						else if(line.startsWith(":") && answers != 3) {
							switch(answers) {
								case 0: quiz.setAnswer1(line.substring(1)); break;
								case 1: quiz.setAnswer2(line.substring(1)); break;
								case 2: quiz.setAnswer3(line.substring(1)); break;
							}
							answers++;
						}
						else if(line.startsWith(";") && hints != 3) {
							switch(hints) {
								case 0: quiz.setHint1(line.substring(1)); break;
								case 1: quiz.setHint2(line.substring(1)); break;
								case 2: quiz.setHint3(line.substring(1)); break;
							}
							hints++;
						}
						else if(line.startsWith("=") && rewards != 1) {
							quiz.setReward(line.substring(1));
							rewards++;
						}
						else if(line.contains("END")) {
							Hashes.addQuiz(index, quiz);
							index++;
						}
					}
				}
				//if the HashMap is bigger than the current index, then either clear the rest of the HashMap
				//or set the unneeded fields to blank, if they aren't entirely empty
				clearQuestions(index);
				
				String integrity = IntegrityCheck();
				if(integrity.equals("0")) {
					e.getTextChannel().sendMessage("All questions have been registered successfully!").queue();
					logger.debug("Quiz questions have been registered");
				}
				else {
					e.getTextChannel().sendMessage("An error occured while registering the questions. Inserted questions have been cleared. Please check the error log:\n"
							+ ""+Pastebin.unlistedPaste("Error on registering rewards", integrity, e.getGuild().getIdLong())).queue();
					logger.error("Internal pastebin error in registering questions for the quiz");
					clearQuestions(1);
				}
			}
			else {
				if(_readFile == false) {
					EmbedBuilder error = new EmbedBuilder().setTitle(content[0]).setColor(Color.RED);
					e.getTextChannel().sendMessage(error.setDescription("Please ensure that a valid Pastebin link has been inserted and that the API key inside the config.ini file is correct").build()).queue();
					logger.error("Pastebin link couldn't be read while registering questions");
					
				}
				else {
					EmbedBuilder error = new EmbedBuilder().setTitle("An error occurred while reading file!").setColor(Color.RED);
					e.getTextChannel().sendMessage(error.setDescription("An unexpected error occurred while reading the file. Please check the content or create a new save file.").build()).queue();
					logger.error("Settings from the quiz backup file couldn't be loaded");
				}
			}
		}
		else {
			if(_readFile == false) {
				EmbedBuilder error = new EmbedBuilder().setTitle("Invalid url!").setColor(Color.RED);
				e.getTextChannel().sendMessage(error.setDescription("An invalid url has been inserted. Please insert a Pastebin link").build()).queue();
				logger.warn("Wrong pastebin link has been inserted for the question registration");
			}
			else {
				EmbedBuilder error = new EmbedBuilder().setTitle("File doesn't exist!").setColor(Color.RED);
				e.getTextChannel().sendMessage(error.setDescription("Please confirm that the settings file exists before continuing .").build()).queue();
				logger.warn("Quiz backup file doesn't exist");
			}
		}
	}
	
	public static void saveQuestions(MessageReceivedEvent e) {
		//Iterate through the HashMap values and write all information into a file
		StringBuilder sb = new StringBuilder();
		for(Quizes quiz : Hashes.getWholeQuiz().values()) {
			if(quiz.getQuestion().length() > 0) {
				sb.append("START\n");
				sb.append(quiz.getQuestion()+"\n");
			}
			else {
				sb.setLength(0);
				sb.append("No questions!");
				break;
			}
			if(quiz.getAnswer1().length() > 0) {
				sb.append(":"+quiz.getAnswer1()+"\n");
			}
			if(quiz.getAnswer2().length() > 0) {
				sb.append(":"+quiz.getAnswer2()+"\n");
			}
			if(quiz.getAnswer3().length() > 0) {
				sb.append(":"+quiz.getAnswer3()+"\n");
			}
			if(quiz.getHint1().length() > 0) {
				sb.append(";"+quiz.getHint1()+"\n");
			}
			if(quiz.getHint2().length() > 0) {
				sb.append(";"+quiz.getHint2()+"\n");
			}
			if(quiz.getHint3().length() > 0) {
				sb.append(";"+quiz.getHint3()+"\n");
			}
			if(quiz.getReward().length() > 0) {
				sb.append("="+quiz.getReward()+"\n");
				sb.append("END\n");
			}
			else {
				sb.setLength(0);
				sb.append("No rewards!");
				break;
			}
		}
		
		if(!sb.toString().equals("No questions!") && !sb.toString().equals("No rewards!")) {
			new File("./files/QuizBackup").mkdirs();
			FileSetting.createFile("./files/QuizBackup/quizsettings.azr", sb.toString());
			e.getTextChannel().sendMessage("Quiz settings have been saved successfully!").queue();
			logger.debug("The settings for the quiz have been registered");
		}
		else if(sb.toString().equals("No questions!")){
			EmbedBuilder error = new EmbedBuilder().setTitle("Questions missing!").setColor(Color.RED);
			e.getTextChannel().sendMessage(error.setDescription("A backup from the settings couldn't be created because the questions are missing. Please register them first").build()).queue();
			logger.warn("Quiz backup file couldn't be created due to the missing questions");
		}
		else if(sb.toString().equals("No rewards!")) {
			EmbedBuilder error = new EmbedBuilder().setTitle("Rewards missing!").setColor(Color.RED);
			e.getTextChannel().sendMessage(error.setDescription("A backup from the settings couldn't be created because the rewards are missing. Please register them first").build()).queue();
			logger.warn("Quiz backup file couldn't be created due to the missing rewards");
		}
	}
	
	private static String IntegrityCheck() {
		int index = 1;
		StringBuilder sb = new StringBuilder();
		boolean errorFound = false;
		
		for(int i = 0; i < Hashes.getWholeQuiz().size(); i++) {
			Quizes quiz = Hashes.getQuiz(index);
			if(errorFound == true) {
				sb.append("\n");
				errorFound = false;
			}
			//check if questions are inserted and write into the error log when a question
			//in between is missing. For example question 1 and 3 are there but not 2.
			if(quiz.getQuestion().length() > 0) {
				if(Integer.parseInt(quiz.getQuestion().replaceAll("[^0-9](.)[\\s\\d\\w?!.\\,/+-]*", "") ) != index) {
					sb.append("Question "+index+": Question was skipped and hence missing! Please check if the START and END parameters are properly set!\n");
					errorFound = true;
				}
				//check if at least one answer is available for this question. Else throw error
				if(quiz.getAnswer1().length() == 0 && quiz.getAnswer2().length() == 0 && quiz.getAnswer3().length() == 0) {
					sb.append("Question "+index+": has no available answer!\n");
					errorFound = true;
				}
			}
			else if(quiz.getQuestion().length() == 0) {
				//check if answers and hints are inserted while there's no question
				if(quiz.getAnswer1().length() > 0 || quiz.getAnswer2().length() > 0 || quiz.getAnswer3().length() > 0) {
					sb.append("Question "+index+": Has at least one answer but no question!\n");
					errorFound = true;
				}
				if(quiz.getHint1().length() > 0 || quiz.getHint2().length() > 0 || quiz.getHint3().length() > 0) {
					sb.append("Question "+index+": Has at least one hint but no question!\n");
					errorFound = true;
				}
			}
			
			//check if rewards are available and if questions are available but no rewards,
			//then write into error log.
			if(quiz.getReward().length() > 0) {
				if(Hashes.getQuiz(index-1) != null && Hashes.getQuiz(index-1).getQuestion().length() > 0 && quiz.getQuestion().length() == 0) {
					sb.append("Question "+index+": Reward has been inserted but the question is missing!\n");
					errorFound = true;
				}
			}
			index++;
		}
		
		if(sb.length() == 0) {
			return "0";
		}
		else {
			return sb.toString();
		}
	}
	
	private static void clearRewards(int index) {
		Quizes quiz;
		for(int i = index-1; i <= Hashes.getWholeQuiz().size()+1; i++) {
			quiz = Hashes.getQuiz(index);
			if(quiz != null && quiz.getQuestion().length() > 0) {
				quiz.setReward("");
			}
			else {
				Hashes.removeQuiz(index);
			}
			index++;
		}
	}
	
	private static void clearQuestions(int index) {
		Quizes quiz;
		for(int i = index-1; i < Hashes.getWholeQuiz().size(); i++) {
			quiz = Hashes.getQuiz(index);
			if(quiz != null && quiz.getReward().length() > 0) {
				quiz.setQuestion("");
				quiz.setAnswer1("");
				quiz.setAnswer2("");
				quiz.setAnswer3("");
				quiz.setHint1("");
				quiz.setHint2("");
				quiz.setHint3("");
			}
			else {
				Hashes.removeQuiz(index);
			}
			index++;
		}
	}
}
