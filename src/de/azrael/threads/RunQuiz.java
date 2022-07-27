package de.azrael.threads;

import java.awt.Color;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Cache;
import de.azrael.constructors.Quizes;
import de.azrael.core.Hashes;
import de.azrael.enums.Translation;
import de.azrael.sql.Azrael;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class RunQuiz implements Runnable{
	private final static Logger logger = LoggerFactory.getLogger(RunQuiz.class);
	public final static ConcurrentMap<Long, String> quizState = new ConcurrentHashMap<Long, String>();
	
	private GuildMessageReceivedEvent e;
	private long quizChannel;
	private long logChannel;
	private int mode;
	
	public RunQuiz(GuildMessageReceivedEvent _e, long _quizChannel, long _logChannel, int _mode) {
		this.e = _e;
		this.quizChannel = _quizChannel;
		this.logChannel = _logChannel;
		this.mode = _mode;
	}
	
	@Override
	public void run() {
		try {
			EmbedBuilder message = new EmbedBuilder().setColor(Color.getHSBColor(268, 81, 88)).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.QUIZ_REWARD_SENT_TITLE));
			final TextChannel quizChannel = e.getGuild().getTextChannelById(this.quizChannel);
			final TextChannel logChannel = this.logChannel > 0 ? e.getGuild().getTextChannelById(this.logChannel) : null;
			int modality = mode;
			quizState.put(e.getGuild().getIdLong(), ""+0);
			//send the beginning messages after short delays
			Thread.sleep(3000);
			quizChannel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.QUIZ_STARTING)).queue();
			Thread.sleep(60000);
			quizChannel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.QUIZ_FIRST_QUESTION)).queue();
			Thread.sleep(5000);
			int hints = 0;
			int index = 1;
			while(true) {
				quizState.put(e.getGuild().getIdLong(), ""+index);
				Quizes quiz = Hashes.getQuiz(e.getGuild().getIdLong(), index);
				
				//check if there are still questions left by checking, if quiz is empty. If empty, terminate the program
				if(quiz != null) {
					//jump over already used rewards
					if(quiz.isUsed()) {
						index++;
						continue;
					}
					
					//Start to print questions
					quizChannel.sendMessage("**"+quiz.getQuestion()+"**").queue();
					Hashes.addTempCache("quiztime_gu"+e.getGuild().getId(), new Cache(quizChannel.getId()));
					//allow this timer to be interrupted when a question has to be skipped or when the quiz has to be interrupted
					STATIC.addThread(Thread.currentThread(), "quiz_gu"+e.getGuild().getId());
					try {
						Thread.sleep(20000);
					} catch(InterruptedException e2) {
						//skip the question when an administrator types skip-question
						if(quizState.get(e.getGuild().getIdLong()).equalsIgnoreCase(STATIC.getTranslation2(e.getGuild(), Translation.PARAM_SKIP_QUESTION))) {
							quizChannel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.QUIZ_QUESTION_SKIP)).queue();
							index++;
							Thread.sleep(5000);
							continue;
						}
						//interrupt the quiz when an administrator types interrupt-questions
						else if(quizState.get(e.getGuild().getIdLong()).equalsIgnoreCase(STATIC.getTranslation2(e.getGuild(), Translation.PARAM_INTERRUPT_QUESTIONS))) {
							quizChannel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.QUIZ_INTERRUPTED)).queue();
							break;
						}
					} finally {
						STATIC.removeThread(Thread.currentThread());
					}
					Hashes.clearTempCache("quiztime_gu"+e.getGuild().getId());
					
					//check the created file if someone was able to answer the question
					final Member member = e.getGuild().getMemberById(quizState.get(e.getGuild().getIdLong()));
					if(member != null) {
						logger.info("User {} received the reward {} from the quiz in guild {}", e.getMember().getUser().getId(), quiz.getReward(), e.getGuild().getId());
						quizChannel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.QUIZ_WINNER).replaceFirst("\\{\\}", ""+index).replace("{}", member.getAsMention())).queue();
						
						//send the reward in private message to the user and log the reward and user at the same time in the log channel.
						member.getUser().openPrivateChannel().queue(pchannel -> {
							pchannel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.QUIZ_WINNER_DM)
									+ "**"+quiz.getReward()+"**").queue(success -> {
										Azrael.SQLUpdateUsedQuizReward(e.getGuild().getIdLong(), quiz.getReward());
										if(logChannel != null)
											logChannel.sendMessage(message.setDescription(STATIC.getTranslation2(e.getGuild(), Translation.QUIZ_WINNER_NOTIFICATION).replaceFirst("\\{\\}", member.getUser().getName()+"#"+member.getUser().getDiscriminator())+quiz.getReward()).build()).queue();
										pchannel.close().queue();
									}, error -> {
										//When the reward couldn't be sent, throw this error.
										Azrael.SQLUpdateUsedQuizReward(e.getGuild().getIdLong(), quiz.getReward());
										EmbedBuilder err = new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.QUIZ_REWARD_SEND_ERR));
										if(logChannel != null)
											logChannel.sendMessage(err.setDescription(STATIC.getTranslation2(e.getGuild(), Translation.QUIZ_WINNER_NOTIFICATION_2).replaceFirst("\\{\\}", member.getUser().getName()+"#"+member.getUser().getDiscriminator())+quiz.getReward()).build()).queue();
										pchannel.close().queue();
									});
						});
						
						//if mode is 2 then allow a 3 questions threshold for everyone who wins a price and collect the name of winners only when the mode isn't set to 1 with no limits
						if(modality == 2) {
							Hashes.removeQuizWinners(e.getGuild().getIdLong());
						}
						if(modality != 1) {
							Hashes.addQuizWinners(member, 3);
						}
						hints = 0;
						index++;
						Thread.sleep(5000);
					}
					else {
						//if any hints exist for that question and if no 3 hints were given already, then throw a hint
						if(quiz.getHint1() != null && hints == 0) {
							quizChannel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.QUIZ_HINT_1)+quiz.getHint1()).queue();
							hints++;
						}
						else if(quiz.getHint2() != null && hints == 1) {
							quizChannel.sendMessage(STATIC.getTranslation2(e.getGuild(), Translation.QUIZ_HINT_2)+quiz.getHint2()).queue();
							hints++;
						}
						else if(quiz.getHint3() != null && hints == 2) {
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
			Hashes.clearQuizWinners(e.getGuild().getIdLong());
			quizState.remove(e.getGuild().getIdLong());
		} catch (InterruptedException e1) {
			logger.trace("Thread sleep has been interrupted on RunQuiz in guild {}", e.getGuild().getId());
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
