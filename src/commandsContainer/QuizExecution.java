package commandsContainer;

import java.awt.Color;
import java.io.File;

import core.Hashes;
import core.Quizes;
import fileManagement.FileSetting;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import util.Pastebin;

public class QuizExecution {
	public static void registerRewards(MessageReceivedEvent e, String _link) {
		//check if it is a link that was inserted and if yes call readPublicPasteLink and then
		//split the returned String in an array
		if(_link.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _link.startsWith("http")) {
			String [] rewards = Pastebin.readPublicPasteLink(_link).split("[\\r\\n]+");
			
			if(!rewards[0].equals("Reading paste failed!")) {
				int index = 1;
				Quizes quiz;
				//Insert the rewards into the HashMap
				for(String reward: rewards) {
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
				//if the HashMap is bigger than the current index, then either clear the rest of the HashMap
				//or set the reward field to blank in case questions exist
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
				
				//print message that it either worked or that an error occurred. Print error if there's any
				String integrity = IntegrityCheck();
				if(integrity.equals("0")) {
					e.getTextChannel().sendMessage("All rewards have been registered successfully!").queue();
				}
				else {
					e.getTextChannel().sendMessage("An error occured while registering the rewards. Please check the error log:\n"
							+ ""+Pastebin.unlistedPaste("Error on registering rewards", integrity)).queue();
				}
			}
			else {
				EmbedBuilder error = new EmbedBuilder().setTitle(rewards[0]).setColor(Color.RED);
				e.getTextChannel().sendMessage(error.setDescription("Please ensure that a valid Pastebin link has been inserted and that the API key inside the config.ini file is correct").build()).queue();
			}
		}
		else {
			EmbedBuilder error = new EmbedBuilder().setTitle("Invalid url!").setColor(Color.RED);
			e.getTextChannel().sendMessage(error.setDescription("An invalid url has been inserted. Please insert a Pastebin link").build()).queue();
		}
	}
	
	public static void registerQuestions(MessageReceivedEvent e, String _link, boolean _readFile) {
		//check if it is a link that was inserted and if yes call readPublicPasteLink and then
		//split the returned String in an array. Or if it's being registered from a file, the file should be checked
		if((_link.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _link.startsWith("http") && _readFile == false)
				|| (new File("./files/QuizBackup/quizsettings.azr").exists() && _readFile == true)) {
			String [] content;
			if(_readFile == false) {
				content = Pastebin.readPublicPasteLink(_link).split("[\\r\\n]+");
			}
			else {
				content = FileSetting.readFile(".files/QuizBackup/quizsetting.azr").split("[\\r\\n]+");
			}
			
			if(!content[0].equals("Reading paste failed!") || content.length == 0) {
				int index = 0;
				int answers = 0;
				int hints = 0;
				int rewards = 0;
				Quizes quiz;
				//Insert questions, answers and hints into the HashMap
				for(String line : content) {
					index++;
					if(Hashes.getQuiz(index == 1 ? 1 : index - 1) == null) {
						quiz = new Quizes();
					}
					else {
						quiz = Hashes.getQuiz(index == 1 ? 1 : index - 1);
					}
					
					if(line.replaceAll("[^0-9.]*", "").matches("(1|2|3|4|5|6|7|8|9)[0-9]*[.]")) {
						quiz.setQuestion(line);
						answers = 0;
						hints = 0;
						rewards = 0;
					}
					else if(line.startsWith(":") && answers != 3) {
						index--;
						switch(answers) {
							case 0: quiz.setAnswer1(line.substring(1)); break;
							case 1: quiz.setAnswer2(line.substring(1)); break;
							case 2: quiz.setAnswer3(line.substring(1)); break;
						}
						answers++;
					}
					else if(line.startsWith(";") && hints != 3) {
						index--;
						switch(hints) {
							case 0: quiz.setHint1(line.substring(1)); break;
							case 1: quiz.setHint2(line.substring(1)); break;
							case 2: quiz.setHint3(line.substring(1)); break;
						}
						hints++;
					}
					else if(line.startsWith("=") && rewards != 1) {
						index--;
						quiz.setReward(line.substring(1));
						rewards++;
					}
					Hashes.addQuiz(index, quiz);
				}
				//if the HashMap is bigger than the current index, then either clear the rest of the HashMap
				//or set the unneeded fields to blank, if they aren't entirely empty
				for(int i = index; i < Hashes.getWholeQuiz().size(); i++) {
					quiz = Hashes.getQuiz(i);
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
				}
				
				String integrity = IntegrityCheck();
				if(integrity.equals("0")) {
					e.getTextChannel().sendMessage("All questions have been registered successfully!").queue();
				}
				else {
					e.getTextChannel().sendMessage("An error occured while registering the questions. Please check the error log:\n"
							+ ""+Pastebin.unlistedPaste("Error on registering rewards", integrity)).queue();
				}
			}
			else {
				if(_readFile == false) {
					EmbedBuilder error = new EmbedBuilder().setTitle(content[0]).setColor(Color.RED);
					e.getTextChannel().sendMessage(error.setDescription("Please ensure that a valid Pastebin link has been inserted and that the API key inside the config.ini file is correct").build()).queue();
				}
				else {
					EmbedBuilder error = new EmbedBuilder().setTitle("An error occurred while reading file!").setColor(Color.RED);
					e.getTextChannel().sendMessage(error.setDescription("An unexpected error occurred while reading the file. Please check the content or create a new save file.").build()).queue();
				}
			}
		}
		else {
			if(_readFile == false) {
				EmbedBuilder error = new EmbedBuilder().setTitle("Invalid url!").setColor(Color.RED);
				e.getTextChannel().sendMessage(error.setDescription("An invalid url has been inserted. Please insert a Pastebin link").build()).queue();
			}
			else {
				EmbedBuilder error = new EmbedBuilder().setTitle("File doesn't exist!").setColor(Color.RED);
				e.getTextChannel().sendMessage(error.setDescription("Please confirm that the settings file exists before continuing .").build()).queue();
			}
		}
	}
	
	public static void saveQuestions(MessageReceivedEvent e) {
		//Iterate through the HashMap values and write all information into a file
		StringBuilder sb = new StringBuilder();
		for(Quizes quiz : Hashes.getWholeQuiz().values()) {
			if(quiz.getQuestion().length() > 0) {
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
		}
		else if(sb.toString().equals("No questions!")){
			EmbedBuilder error = new EmbedBuilder().setTitle("Questions missing!").setColor(Color.RED);
			e.getTextChannel().sendMessage(error.setDescription("A backup from the settings couldn't be created because the questions are missing. Please register them first").build()).queue();
		}
		else if(sb.toString().equals("No rewards!")) {
			EmbedBuilder error = new EmbedBuilder().setTitle("Rewards missing!").setColor(Color.RED);
			e.getTextChannel().sendMessage(error.setDescription("A backup from the settings couldn't be created because the rewards are missing. Please register them first").build()).queue();
		}
	}
	
	private static String IntegrityCheck() {
		int index = 1;
		StringBuilder sb = new StringBuilder();
		boolean onlyRewardError = true;
		boolean onlyQuestionsError = true;
		
		for(int i = 0; i < Hashes.getWholeQuiz().size(); i++) {
			Quizes quiz = Hashes.getQuiz(index);
			if(index != 1) {
				sb.append("\n");
			}
			//check if questions are inserted and write into the error log when a question
			//in between is missing. For example question 1 and 3 are there but not 2.
			if(quiz.getQuestion().length() > 0) {
				onlyQuestionsError = false;
				if(Integer.parseInt(quiz.getQuestion().replaceAll("[^0-9]", "") ) != index) {
					sb.append("Question "+index+": Question is missing together with rewards and answers!\n");
				}
				//check if at least one answer is available for this question. Else throw error
				if(quiz.getAnswer1().length() == 0 && quiz.getAnswer2().length() == 0 && quiz.getAnswer3().length() == 0) {
					sb.append("Question "+index+": has no available answer!\n");
				}
			}
			else if(quiz.getQuestion().length() == 0) {
				//check if answers and hints are inserted while there's no question
				if(quiz.getAnswer1().length() > 0 || quiz.getAnswer2().length() > 0 || quiz.getAnswer3().length() > 0) {
					sb.append("Question "+index+": Has at least one answer but no question!\n");
					onlyQuestionsError = false;
				}
				if(quiz.getHint1().length() > 0 || quiz.getHint2().length() > 0 || quiz.getHint3().length() > 0) {
					sb.append("Question "+index+": Has at least one hint but no question!\n");
					onlyQuestionsError = false;
				}
			}
			
			//check if rewards are available and if questions are available but no rewards,
			//then write into error log.
			if(quiz.getReward().length() > 0) {
				onlyRewardError = false;
			}
			index++;
		}
		
		if(onlyQuestionsError == true || onlyRewardError == true) {
			return "0";
		}
		else {
			return sb.toString();
		}
	}
}
