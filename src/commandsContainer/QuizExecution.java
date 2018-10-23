package commandsContainer;

import java.awt.Color;

import core.Hashes;
import core.Quizes;
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
				for(int i = index; i < Hashes.getWholeQuiz().size(); i++) {
					quiz = Hashes.getQuiz(i);
					if(quiz.getQuestion().length() > 0) {
						quiz.setReward("");
					}
					else {
						Hashes.removeQuiz(index);
					}
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
	
	public static void registerQuestions(MessageReceivedEvent e, String _link) {
		//check if it is a link that was inserted and if yes call readPublicPasteLink and then
		//split the returned String in an array
		if(_link.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _link.startsWith("http")) {
			String [] content = Pastebin.readPublicPasteLink(_link).split("[\\r\\n]+");
			
			if(!content[0].equals("Reading paste failed!")) {
				int index = 0;
				int answers = 0;
				int hints = 0;
				int rewards = 0;
				Quizes quiz;
				//Insert questions, answers and hints into the HashMap
				for(String line : content) {
					if(Hashes.getQuiz(index == 0 ? 1 : index) == null) {
						quiz = new Quizes();
					}
					else {
						quiz = Hashes.getQuiz(index == 0 ? 1 : index);
					}
					
					if(line.matches("(1|2|3|4|5|6|7|8|9)[0-9]{0, 2}[.]")) {
						index++;
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
					Hashes.addQuiz(index, quiz);
				}
				//if the HashMap is bigger than the current index, then either clear the rest of the HashMap
				//or set the unneeded fields to blank, if they aren't entirely empty
				for(int i = index+1; i < Hashes.getWholeQuiz().size(); i++) {
					quiz = Hashes.getQuiz(i);
					if(quiz.getReward().length() > 0) {
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
				EmbedBuilder error = new EmbedBuilder().setTitle(content[0]).setColor(Color.RED);
				e.getTextChannel().sendMessage(error.setDescription("Please ensure that a valid Pastebin link has been inserted and that the API key inside the config.ini file is correct").build()).queue();
			}
		}
		else {
			EmbedBuilder error = new EmbedBuilder().setTitle("Invalid url!").setColor(Color.RED);
			e.getTextChannel().sendMessage(error.setDescription("An invalid url has been inserted. Please insert a Pastebin link").build()).queue();
		}
	}
	
	private static String IntegrityCheck() {
		int index = 1;
		StringBuilder sb = new StringBuilder();
		boolean onlyRewardError = true;
		boolean onlyQuestionsError = true;
		
		for(Quizes quiz: Hashes.getWholeQuiz().values()) {
			if(index != 1) {
				sb.append("\n");
			}
			//check if questions are inserted and write into the error log when a question
			//in between is missing. For example question 1 and 3 are there but not 2.
			if(quiz.getQuestion().length() >= 0) {
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
			else if(quiz.getQuestion().length() > 0 && quiz.getReward().length() == 0) {
				sb.append("Question "+index+": The reward for the question is missing!\n");
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
