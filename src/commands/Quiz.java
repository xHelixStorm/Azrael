package commands;

import java.awt.Color;
import java.io.File;

import commandsContainer.QuizExecution;
import core.Hashes;
import core.UserPrivs;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.SqlConnect;

public class Quiz implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(IniFileReader.getQuizCommand().equals("true")) {
			EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setTitle("Access denied!").setThumbnail(IniFileReader.getDeniedThumbnail());
			if(UserPrivs.isUserAdmin(e.getMember().getUser(), e.getGuild().getIdLong()) || UserPrivs.isUserMod(e.getMember().getUser(), e.getGuild().getIdLong()) || IniFileReader.getAdmin().equals(e.getMember().getUser().getId())) {
				EmbedBuilder message = new EmbedBuilder().setTitle("It's Quiz time!").setColor(Color.BLUE);
				if(e.getMessage().getContentRaw().equals(IniFileReader.getCommandPrefix()+"quiz")) {
					e.getTextChannel().sendMessage(message.setDescription("The Quiz command will allow you to register questions to provide the best quiz experience in a Discord server! At your disposal are parameters to register questions from a pastebin link, register rewards in form of codes that get sent to the user who answers a question correctly in private message and to start and interrupt the quiz session.\n\n"
							+ "If a question gets answered, the next questions will appear after a 30 seconds intervall and if they don't get replied within 30 seconds, users will receive a reminder that no one has yet answered the question and if available, it will write a hint to facilitate the question. These are the available parameters. Use them together with the quiz command:\n\n"
							+ "**-register-codes**: To register the rewards that get sent to one user who answers a question correctly\n"
							+ "**-register-questions**: To register questions with answer possibilities and hints\n"
							+ "**-clear**: To clear all questions and rewards that were saved in the cache\n"
							+ "**-run**: To start the quiz in a designated channel\n"
							+ "**-save**: To save the settings on the local machine\n"
							+ "**-load**: To load presaved questions").build()).queue();
				}
				else if(e.getMessage().getContentRaw().equals(IniFileReader.getCommandPrefix()+"quiz -register-rewards")) {
					e.getTextChannel().sendMessage("To register the rewards, attach a pastebin link after the full command. The pastebin format should look like this:\n"
							+ "```\nxxx-xx-xx-0001\n"
							+ "xxx-xx-xx-0002\n"
							+ "xxx-xx-xx-0003\n"
							+ "...```"
							+ "write one reward per line so that the bot can recognize every single reward!").queue();
				}
				else if(e.getMessage().getContentRaw().contains(IniFileReader.getCommandPrefix()+"quiz -register-rewards ")) {
					QuizExecution.registerRewards(e, e.getMessage().getContentRaw().substring(IniFileReader.getCommandPrefix().length()+23));
				}
				else if(e.getMessage().getContentRaw().equals(IniFileReader.getCommandPrefix()+"quiz -register-questions")) {
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
				else if(e.getMessage().getContentRaw().contains(IniFileReader.getCommandPrefix()+"quiz -register-questions ")) {
					QuizExecution.registerQuestions(e, e.getMessage().getContentRaw().substring(IniFileReader.getCommandPrefix().length()+25), false);
				}
				else if(e.getMessage().getContentRaw().contains(IniFileReader.getCommandPrefix()+"quiz -clear")) {
					Hashes.clearQuiz();
					e.getTextChannel().sendMessage("Cache has been cleared from registered questions and rewards!").queue();
				}
				else if(e.getMessage().getContentRaw().equals(IniFileReader.getCommandPrefix()+"quiz -run")) {
					if(Hashes.getWholeQuiz().size() > 0) {
						if(Hashes.getQuiz(1).getQuestion().length() == 0) {
							e.getTextChannel().sendMessage("Please register questions and answers before proceeding!").queue();
						}
						if(Hashes.getQuiz(1).getReward().length() == 0) {
							e.getTextChannel().sendMessage("Please register the rewards before proceeding!").queue();
						}
						else {
							//run the quiz in a thread. 
							SqlConnect.SQLgetChannelID(e.getGuild().getIdLong(), "qui");
							if(SqlConnect.getChannelID() != 0) {
								e.getTextChannel().sendMessage("The quiz will run shortly in <#"+SqlConnect.getChannelID()+">!").queue();
							}
							else {
								e.getTextChannel().sendMessage("Please register a quiz channel before starting the quiz!").queue();
							}
							SqlConnect.setChannelID(0);
						}
					}
					else {
						e.getTextChannel().sendMessage("Please register questions and rewards before this parameter is used!").queue();
					}
				}
				else if(e.getMessage().getContentRaw().equals(IniFileReader.getCommandPrefix()+"quiz -save")) {
					if(Hashes.getWholeQuiz().size() > 0) {
						//save all settings
						QuizExecution.saveQuestions(e);
					}
					else {
						e.getTextChannel().sendMessage("There is nothing to save. Please use register-rewards and register-questions before this parameter is used.").queue();
					}
				}
				else if(e.getMessage().getContentRaw().equals(IniFileReader.getCommandPrefix()+"quiz -load")) {
					File file = new File("./files/QuizBackup/quizsettings.azr");
					if(file.exists()) {
						QuizExecution.registerQuestions(e, "", true);
					}
					else {
						e.getTextChannel().sendMessage("No saved settings have been found. Please save them first.").queue();
					}
				}
			}
			else {
				e.getTextChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + " **My apologies young padawan. This command can be used only from an Administrator or an Moderator. Here a cookie** :cookie:").build()).queue();
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
