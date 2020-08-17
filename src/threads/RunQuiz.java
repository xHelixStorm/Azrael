package threads;

import java.awt.Color;
import java.io.File;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Quizes;
import core.Hashes;
import enums.Translation;
import fileManagement.FileSetting;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import util.STATIC;

public class RunQuiz implements Runnable{
	private final static Logger logger = LoggerFactory.getLogger(RunQuiz.class);
	
	private GuildMessageReceivedEvent e;
	private long channel_id;
	private long log_channel_id;
	private int mode;
	
	public RunQuiz(GuildMessageReceivedEvent _e, long _channel_id, long _log_channel_id, int _mode) {
		this.e = _e;
		this.channel_id = _channel_id;
		this.log_channel_id = _log_channel_id;
		this.mode = _mode;
	}
	
	@Override
	public void run() {
		try {
			EmbedBuilder message = new EmbedBuilder().setColor(Color.getHSBColor(268, 81, 88)).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.QUIZ_REWARD_SENT_TITLE));
			final TextChannel quizChannel = e.getGuild().getTextChannelById(channel_id);
			final TextChannel logChannel = e.getGuild().getTextChannelById(log_channel_id);
			int modality = mode;
			FileSetting.createFile(IniFileReader.getTempDirectory()+"quiztime_gu"+e.getGuild().getId()+".azr", ""+0);
			Thread.sleep(3000);
			//send the starting messages with delays in sending them
			quizChannel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.QUIZ_STARTING)).queue();
			Thread.sleep(60000);
			quizChannel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.QUIZ_FIRST_QUESTION)).queue();
			Thread.sleep(5000);
			int hints = 0;
			int index = 1;
			while(true) {
				FileSetting.createFile(IniFileReader.getTempDirectory()+"quiztime_gu"+e.getGuild().getId()+".azr", ""+index);
				Quizes quiz = Hashes.getQuiz(index);
				
				//check if there are still questions left by checking, if quiz is empty. If empty, terminate the program
				if(quiz != null) {
					//Start to print questions
					quizChannel.sendMessage("**"+quiz.getQuestion()+"**").queue();
					Thread.sleep(20000);
					
					//check the created file if someone was able to answer the question
					if(FileSetting.readFile(IniFileReader.getTempDirectory()+"quiztime_gu"+e.getGuild().getId()+".azr").length() == 18 || FileSetting.readFile(IniFileReader.getTempDirectory()+"quiztime_gu"+e.getGuild().getId()+".azr").length() == 17) {
						long user_id = Long.parseLong(FileSetting.readFile(IniFileReader.getTempDirectory()+"quiztime_gu"+e.getGuild().getId()+".azr"));
						logger.debug("{} received the reward {} out of the quiz in guild {}", e.getMember().getUser().getId(), quiz.getReward(), e.getGuild().getId());
						quizChannel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.QUIZ_WINNER).replaceFirst("\\{\\}", ""+index).replace("{}", e.getGuild().getMemberById(user_id).getAsMention())).queue();
						
						//send the reward in private message to the user and log the reward and user at the same time in the log channel.
						e.getGuild().getMemberById(user_id).getUser().openPrivateChannel().queue(pchannel -> {
							pchannel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.QUIZ_WINNER_DM)
									+ "**"+quiz.getReward()+"**").queue(success -> {
										logChannel.sendMessage(message.setDescription(STATIC.getTranslation2(e.getGuild(), Translation.QUIZ_WINNER_NOTIFICATION).replaceFirst("\\{\\}", e.getGuild().getMemberById(user_id).getUser().getName()+"#"+e.getGuild().getMemberById(user_id).getUser().getDiscriminator())+quiz.getReward()).build()).queue();
										pchannel.close().queue();
									}, error -> {
										//When the reward couldn't be sent, throw this error.
										EmbedBuilder err = new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.QUIZ_REWARD_SEND_ERR));
										logChannel.sendMessage(err.setDescription(STATIC.getTranslation2(e.getGuild(), Translation.QUIZ_WINNER_NOTIFICATION_2).replaceFirst("\\{\\}", e.getGuild().getMemberById(user_id).getUser().getName()+"#"+e.getGuild().getMemberById(user_id).getUser().getDiscriminator())+quiz.getReward()).build()).queue();
										pchannel.close().queue();
									});
						});
						
						//if mode is 2 then allow a 3 questions threshold for everyone who wins a price and collect the name of winners only when the mode isn't set to 1 with no limits
						if(modality == 2) {
							Hashes.removeQuizWinners();
						}
						if(modality != 1) {
							Hashes.addQuizWinners(e.getGuild().getMemberById(user_id), 3);
						}
						hints = 0;
						index++;
						Thread.sleep(5000);
					}
					//skip the question when an administrator types skip-question
					else if(FileSetting.readFile(IniFileReader.getTempDirectory()+"quiztime_gu"+e.getGuild().getId()+".azr").equals("skip-question")) {
						quizChannel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.QUIZ_QUESTION_SKIP)).queue();
						index++;
						Thread.sleep(5000);
					}
					//interrupt the quiz when an administrator types interrupt-questions
					else if(FileSetting.readFile(IniFileReader.getTempDirectory()+"quiztime_gu"+e.getGuild().getId()+".azr").equals("interrupt-questions")) {
						quizChannel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.QUIZ_INTERRUPTED)).queue();
						break;
					}
					else {
						//if any hints exist for that question and if no 3 hints were given already, then throw a hint
						if(quiz.getHint1().length() > 0  && hints == 0) {
							quizChannel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.QUIZ_HINT_1)+quiz.getHint1()).queue();
							hints++;
						}
						else if(quiz.getHint2().length() > 0 && hints == 1) {
							quizChannel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.QUIZ_HINT_2)+quiz.getHint2()).queue();
							hints++;
						}
						else if(quiz.getHint3().length() > 0 && hints == 2) {
							quizChannel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.QUIZ_HINT_3)+quiz.getHint3()).queue();
							hints++;
						}
						else {
							int random = ThreadLocalRandom.current().nextInt(1, 4);
							quizChannel.sendMessage(replyList(e, random)+STATIC.getTranslation2(e.getGuild(), Translation.QUIZ_QUESTION_REPEAT)).queue();
						}
					}
				}
				else {
					quizChannel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.QUIZ_END)).queue();
					break;
				}
			}
			
			//remove the temp file that shows the task for still running and clear the winners cache
			Hashes.clearQuizWinners();
			File file = new File(IniFileReader.getTempDirectory()+"quiztime_gu"+e.getGuild().getId()+".azr");
			file.delete();
		} catch (InterruptedException e) {
			logger.error("Thread sleep has been interrupted on RunQuiz", e);
		}
	}
	
	private String replyList(GuildMessageReceivedEvent e, int digit) {
		return switch(digit) {
			case 1  -> STATIC.getTranslation2(e.getGuild(), Translation.QUIZ_REPLY_1);
			case 2  -> STATIC.getTranslation2(e.getGuild(), Translation.QUIZ_REPLY_2);
			case 3  -> STATIC.getTranslation2(e.getGuild(), Translation.QUIZ_REPLY_3);
			default -> "";
		};
	}
}
