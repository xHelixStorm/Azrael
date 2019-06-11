package commands;

import java.awt.Color;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commandsContainer.QuizExecution;
import constructors.Cache;
import core.Hashes;
import core.UserPrivs;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.Azrael;

public class Quiz implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(GuildIni.getQuizCommand(e.getGuild().getIdLong())) {
			Logger logger = LoggerFactory.getLogger(Quiz.class);
			logger.debug("{} has used Quiz command", e.getMember().getUser().getId());
			EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setTitle("Access denied!").setThumbnail(IniFileReader.getDeniedThumbnail());
			if(UserPrivs.comparePrivilege(e.getMember(), GuildIni.getQuizLevel(e.getGuild().getIdLong())) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong()) {
				if(Hashes.getTempCache("quiztime"+e.getGuild().getId()) == null) {
					EmbedBuilder message = new EmbedBuilder().setTitle("It's Quiz time!").setColor(Color.BLUE);
					if(args.length == 0) {
						e.getTextChannel().sendMessage(message.setDescription("The Quiz command will allow you to register questions to provide the best quiz experience in a Discord server! At your disposal are parameters to register questions from a pastebin link, register rewards in form of codes that get sent to the user who answers a question correctly in private message and to start and interrupt the quiz session.\n\n"
								+ "If a question gets answered, the next questions will appear after a 20 seconds and if they don't get replied within 20 seconds, users will receive a reminder that no one has yet answered the question and if available, it will write a hint to facilitate the question. These are the available parameters. Use them together with the quiz command:\n\n"
								+ "**-register-codes**: To register the rewards that get sent to one user who answers a question correctly\n"
								+ "**-register-questions**: To register questions with answer possibilities and hints\n"
								+ "**-clear**: To clear all questions and rewards that were saved in the cache\n"
								+ "**-run**: To start the quiz in a designated channel\n"
								+ "**-save**: To save the settings on the local machine\n"
								+ "**-load**: To load presaved questions").build()).queue();
					}
					else if(args.length == 1 && args[0].equalsIgnoreCase("-register-rewards")) {
						e.getTextChannel().sendMessage("To register the rewards, attach a pastebin link after the full command. The pastebin format should look like this:\n"
								+ "```\nxxx-xx-xx-0001\n"
								+ "xxx-xx-xx-0002\n"
								+ "xxx-xx-xx-0003\n"
								+ "...```"
								+ "write one reward per line so that the bot can recognize every single reward!").queue();
					}
					else if(args.length > 1 && args[0].equalsIgnoreCase("-register-rewards")) {
						QuizExecution.registerRewards(e, args[1]);
					}
					else if(args.length == 1 && args[0].equalsIgnoreCase("-register-questions")) {
						e.getTextChannel().sendMessage("To register questions, attach a pastebin link after the full command. The pastebin format should look like this:\n"
								+ "```\n"
								+ "START\n"
								+ "1. Guess my hair color.\n"
								+ ":black\n"
								+ ";It's typical south oriental.\n"
								+ "END\nSTART\n"
								+ "2. What is the color of a watermelon?\n"
								+ ":green\n"
								+ ":red\n"
								+ "END```"
								+ "Questions have to be separated by a numerical value. For example 1. and 2.. Solutions to the questions should begin with **:**. The words written after the : doesn't have to be written together but it will be counted as one answer. It's possible to choose up to 3 possible answers for a question. Hints are given with the **;** symbol. Also here maximal 3 hints are allowed."
								+ "It's also possible to include rewards while registering questions in case it is easier to avoid errors. This can be done by applying the **=** before the reward.").queue();
					}
					else if(args.length > 1 && args[0].equalsIgnoreCase("-register-questions")) {
						logger.debug("{} performed the registration of questions and rewards for the Quiz", e.getMember().getUser().getId());
						QuizExecution.registerQuestions(e, args[1], false);
					}
					else if(args[0].equalsIgnoreCase("-clear")) {
						logger.debug("{} cleared all quiz questions and rewards", e.getMember().getUser().getId());
						Hashes.clearQuiz();
						e.getTextChannel().sendMessage("Cache has been cleared from registered questions and rewards!").queue();
					}
					else if(args[0].equalsIgnoreCase("-run")) {
						if(Hashes.getWholeQuiz().size() > 0) {
							if(Hashes.getQuiz(1).getQuestion().length() == 0) {
								e.getTextChannel().sendMessage("Please register questions and answers before proceeding!").queue();
							}
							if(Hashes.getQuiz(1).getReward().length() == 0) {
								e.getTextChannel().sendMessage("Please register the rewards before proceeding!").queue();
							}
							else {
								if(Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type().equals("qui")).findAny().orElse(null) != null) {
									e.getTextChannel().sendMessage(message.setDescription("Please select the fitting mode for the quiz with one of the following digits:\n"
											+ "1: **no restrictions**\n"
											+ "2: **participants will receive a 3 questions threshold on right answer**\n"
											+ "3: **participants win only once for the entire quiz**").build()).queue();
									Hashes.addTempCache("quizstarter_gu"+e.getGuild().getId()+"ch"+e.getTextChannel().getId(), new Cache(180000, e.getMember().getUser().getId()));
								}
								else {
									e.getTextChannel().sendMessage("Please register a quiz channel before starting the quiz!").queue();
								}
							}
						}
						else {
							e.getTextChannel().sendMessage("Please register questions and rewards before this parameter is used!").queue();
						}
					}
					else if(args[0].equalsIgnoreCase("-save")) {
						if(Hashes.getWholeQuiz().size() > 0) {
							//save all settings
							QuizExecution.saveQuestions(e);
							logger.debug("{} has saved the quiz questions and rewards to file", e.getMember().getUser().getId());
						}
						else {
							e.getTextChannel().sendMessage("There is nothing to save. Please use register-rewards and register-questions before this parameter is used.").queue();
						}
					}
					else if(args[0].equalsIgnoreCase("-load")) {
						File file = new File("./files/QuizBackup/quizsettings"+e.getGuild().getId()+".azr");
						if(file.exists()) {
							QuizExecution.registerQuestions(e, "", true);
							logger.debug("{} has loaded the quiz questions and rewards from file", e.getMember().getUser().getId());
						}
						else {
							e.getTextChannel().sendMessage("No saved settings have been found. Please save them first.").queue();
						}
					}
				}
				else {
					e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **A quiz process is already running. It cannot run twice!**").build()).queue();
				}
			}
			else {
				e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:").build()).queue();
			}
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		
	}

	@Override
	public String help() {
		return null;
	}

}
