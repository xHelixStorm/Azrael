package de.azrael.commandsContainer;

import java.awt.Color;
import java.net.MalformedURLException;

import org.jpastebin.exceptions.PasteException;
import org.jpastebin.pastebin.exceptions.LoginException;
import org.jpastebin.pastebin.exceptions.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Quizes;
import de.azrael.core.Hashes;
import de.azrael.enums.Translation;
import de.azrael.sql.Azrael;
import de.azrael.util.Pastebin;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class QuizExecution {
	private static final Logger logger = LoggerFactory.getLogger(QuizExecution.class);
	
	public static void registerRewards(GuildMessageReceivedEvent e, String link) {
		//check if it is a link that was inserted and if yes call readPublicPasteLink and then
		//split the returned String in an array
		if(link.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && link.startsWith("http")) {
			try {
				String [] rewards = Pastebin.readPasteLink(link, e.getGuild().getIdLong()).split("[\\r\\n]+");
				int index = 1;
				boolean interrupted = false;
				Quizes quiz;
				//Insert the rewards into the HashMap
				for(String reward: rewards) {
					if(reward.length() > 0) {
						if(!reward.equals("START") && !reward.matches("(1|2|3|4|5|6|7|8|9)[0-9]*[.][\\s\\d\\w?!.\\,/+-]*")) {
							if(Hashes.getQuiz(e.getGuild().getIdLong(), index) == null) {
								quiz = new Quizes();
							}
							else {
								quiz = Hashes.getQuiz(e.getGuild().getIdLong(), index);
							}
							quiz.setReward(reward);
							Hashes.addQuiz(e.getGuild().getIdLong(), index, quiz);
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
				if(!interrupted) {
					clearRewards(e, index);
				}

				//print message that it either worked or that an error occurred. Print error if there's any
				if(!interrupted) {
					String integrity = IntegrityCheck(e);
					if(integrity.equals("0")) {
						//Overwrite table
						if(Azrael.SQLOverwriteQuizData(e.getGuild().getIdLong()) == 1) {
							e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.QUIZ_REWARDS_REGISTERED)).queue();
							logger.info("User {} has registered the quiz rewards with the pastebin url {} in guild {}", e.getMember().getUser().getId(), link, e.getGuild().getId());
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("Quiz rewards couldn't be saved in guild {}", e.getGuild().getId());
						}
					}
					else {
						try {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)+"\n"+Pastebin.unlistedPaste(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR), integrity, e.getGuild().getIdLong())).build()).queue();
							logger.error("Quiz rewards couldn't be registered in guild {}", e.getGuild().getId());
						} catch (IllegalStateException | LoginException | PasteException e1) {
							logger.warn("Error on creating pastebin page for quiz rewards in guild {}", e.getGuild().getId(), e1);
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						}
						clearRewards(e, 1);
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).build()).queue();
					clearRewards(e, 1);
				}
			} catch (MalformedURLException | RuntimeException | LoginException | ParseException e2) {
				EmbedBuilder error = new EmbedBuilder().setColor(Color.RED);
				e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
				logger.error("Reading pastebin url {} failed in guild {}", link, e.getGuild().getId(), e2);
			}
		}
		else {
			EmbedBuilder error = new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_URL));
			e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.URL_INVALID)).build()).queue();
			logger.warn("Wrong pastebin link has been inserted for the reward registration in guild {}", e.getGuild().getId());
		}
	}
	
	public static void registerQuestions(GuildMessageReceivedEvent e, String link) {
		//check if it is a link that was inserted and if yes call readPublicPasteLink and then
		//split the returned String in an array. Or if it's being registered from a file, the file should be checked
		if(link.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && link.startsWith("http")) {
			String [] content = null;
			try {
				content = Pastebin.readPasteLink(link, e.getGuild().getIdLong()).split("[\\r\\n]+");
			} catch (MalformedURLException | RuntimeException | LoginException | ParseException e1) {
				EmbedBuilder error = new EmbedBuilder().setColor(Color.RED);
				e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
				logger.error("Reading paste pastebin {} failed in guild {}", link, e.getGuild().getId(), e1);
			}
			
			if(content != null && content.length > 0) {
				int index = 1;
				int answers = 0;
				int hints = 0;
				int rewards = 0;
				Quizes quiz = new Quizes();
				//Insert questions, answers and hints into the HashMap
				for(String line : content) {
					if(line.length() > 0) {
						if(line.contains("START")) {
							if(Hashes.getQuiz(e.getGuild().getIdLong(), index) == null) {
								if(index != 1) {
									quiz = new Quizes();
								}
							}
							else {
								quiz = Hashes.getQuiz(e.getGuild().getIdLong(), index);
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
								case 0 -> quiz.setAnswer1(line.substring(1));
								case 1 -> quiz.setAnswer2(line.substring(1));
								case 2 -> quiz.setAnswer3(line.substring(1));
							}
							answers++;
						}
						else if(line.startsWith(";") && hints != 3) {
							switch(hints) {
								case 0 -> quiz.setHint1(line.substring(1));
								case 1 -> quiz.setHint2(line.substring(1));
								case 2 -> quiz.setHint3(line.substring(1));
							}
							hints++;
						}
						else if(line.startsWith("=") && rewards != 1) {
							quiz.setReward(line.substring(1));
							rewards++;
						}
						else if(line.contains("END")) {
							Hashes.addQuiz(e.getGuild().getIdLong(), index, quiz);
							index++;
						}
					}
				}
				//if the HashMap is bigger than the current index, then either clear the rest of the HashMap
				//or set the unneeded fields to blank, if they aren't entirely empty
				clearQuestions(e, index);
				
				String integrity = IntegrityCheck(e);
				if(integrity.equals("0")) {
					//Overwrite table
					if(Azrael.SQLOverwriteQuizData(e.getGuild().getIdLong()) == 1) {
						e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.QUIZ_QUESTIONS_REGISTERED)).queue();
						logger.info("User {} has registered quiz questions with the pastebin url {} in guild {}", e.getMember().getUser().getId(), link, e.getGuild().getId());
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Quiz question couldn't be saved in guild {}", e.getGuild().getId());
					}
				}
				else {
					try {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)+"\n"+Pastebin.unlistedPaste(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR), integrity, e.getGuild().getIdLong())).build()).queue();
						logger.error("Quiz questions couldn't be registered in guild {}", e.getGuild().getId());
					} catch (IllegalStateException | LoginException | PasteException e1) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.warn("Error on creating paste in guild {}", e.getGuild().getId(), e1);
					}
					clearQuestions(e, 1);
				}
			}
			else {
				EmbedBuilder error = new EmbedBuilder().setColor(Color.RED);
				e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.QUIZ_NO_SETTINGS)).build()).queue();
				logger.warn("Quiz information couldn't be retrieved from pastebin url {} in guild {}", link, e.getGuild().getId());
			}
		}
		else {
			EmbedBuilder error = new EmbedBuilder().setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_URL)).setColor(Color.RED);
			e.getChannel().sendMessage(error.setDescription(STATIC.getTranslation(e.getMember(), Translation.URL_INVALID)).build()).queue();
			logger.warn("Pastebin url {} may be invalid to retrieve quiz information in guild {}", link, e.getGuild().getId());
		}
	}
	
	private static String IntegrityCheck(GuildMessageReceivedEvent e) {
		int index = 1;
		StringBuilder sb = new StringBuilder();
		boolean errorFound = false;
		
		for(int i = 0; i < Hashes.getWholeQuiz(e.getGuild().getIdLong()).size(); i++) {
			Quizes quiz = Hashes.getQuiz(e.getGuild().getIdLong(), index);
			if(errorFound == true) {
				sb.append("\n");
				errorFound = false;
			}
			//check if questions are inserted and write into the error log when a question
			//in between is missing. For example question 1 and 3 are there but not 2.
			if(quiz.getQuestion() != null) {
				if(Integer.parseInt(quiz.getQuestion().replaceAll("[^0-9](.)[\\s\\d\\w?!.\\,/+-]*", "") ) != index) {
					sb.append(STATIC.getTranslation(e.getMember(), Translation.QUIZ_ERR_1).replace("{}", ""+index));
					errorFound = true;
				}
				//check if at least one answer is available for this question. Else throw error
				if(quiz.getAnswer1() == null && quiz.getAnswer2() == null && quiz.getAnswer3() == null) {
					sb.append(STATIC.getTranslation(e.getMember(), Translation.QUIZ_ERR_2).replace("{}", ""+index));
					errorFound = true;
				}
			}
			else if(quiz.getQuestion().length() == 0) {
				//check if answers and hints are inserted while there's no question
				if(quiz.getAnswer1() != null || quiz.getAnswer2() != null || quiz.getAnswer3() != null) {
					sb.append(STATIC.getTranslation(e.getMember(), Translation.QUIZ_ERR_3).replace("{}", ""+index));
					errorFound = true;
				}
				if(quiz.getHint1() != null || quiz.getHint2() != null || quiz.getHint3() != null) {
					sb.append(STATIC.getTranslation(e.getMember(), Translation.QUIZ_ERR_4).replace("{}", ""+index));
					errorFound = true;
				}
			}
			
			//check if rewards are available and if questions are available but no rewards,
			//then write into error log.
			if(quiz.getReward() != null) {
				if(Hashes.getQuiz(e.getGuild().getIdLong(), (index-1)) != null && Hashes.getQuiz(e.getGuild().getIdLong(), (index-1)).getQuestion().length() > 0 && quiz.getQuestion().length() == 0) {
					sb.append(STATIC.getTranslation(e.getMember(), Translation.QUIZ_ERR_5).replace("{}", ""+index));
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
	
	private static void clearRewards(GuildMessageReceivedEvent e, int index) {
		Quizes quiz;
		for(int i = index-1; i <= Hashes.getWholeQuiz(e.getGuild().getIdLong()).size()+1; i++) {
			quiz = Hashes.getQuiz(e.getGuild().getIdLong(), index);
			if(quiz != null && quiz.getQuestion().length() > 0) {
				quiz.setReward(null);
				quiz.setUsed(false);
			}
			else {
				Hashes.removeQuiz(e.getGuild().getIdLong(), index);
			}
			index++;
		}
	}
	
	private static void clearQuestions(GuildMessageReceivedEvent e, int index) {
		Quizes quiz;
		for(int i = index-1; i < Hashes.getWholeQuiz(e.getGuild().getIdLong()).size(); i++) {
			quiz = Hashes.getQuiz(e.getGuild().getIdLong(), index);
			if(quiz != null && quiz.getReward().length() > 0) {
				quiz.setQuestion(null);
				quiz.setAnswer1(null);
				quiz.setAnswer2(null);
				quiz.setAnswer3(null);
				quiz.setHint1(null);
				quiz.setHint2(null);
				quiz.setHint3(null);
			}
			else {
				Hashes.removeQuiz(e.getGuild().getIdLong(), index);
			}
			index++;
		}
	}
}
