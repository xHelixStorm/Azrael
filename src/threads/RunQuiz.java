package threads;

import java.awt.Color;
import java.util.concurrent.ThreadLocalRandom;

import core.Hashes;
import core.Quizes;
import fileManagement.FileSetting;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class RunQuiz implements Runnable{
	private MessageReceivedEvent e;
	private long channel_id;
	private long log_channel_id;
	
	public RunQuiz(MessageReceivedEvent _e, long _channel_id, long _log_channel_id) {
		this.e = _e;
		this.channel_id = _channel_id;
		this.log_channel_id = _log_channel_id;
	}
	
	@Override
	public void run() {
		try {
			EmbedBuilder message = new EmbedBuilder().setColor(Color.getHSBColor(268, 81, 88)).setTitle("Reward was sent to user!");
			long channel = channel_id;
			long log_channel = log_channel_id;
			Thread.sleep(3000);
			//send the starting messages with delays in sending them
			e.getGuild().getTextChannelById(channel).sendMessage("Hello everyone! The quiz will begin in one minute. Prepare for the questions!").queue();
			Thread.sleep(60000);
			e.getGuild().getTextChannelById(channel).sendMessage("Now it's time for the first question! Check it out!").queue();
			Thread.sleep(5000);
			int index = 1;
			int hints = 0;
			while(true) {
				FileSetting.createFile(IniFileReader.getTempDirectory()+"AutoDelFiles/quiztime.azr", ""+index);
				Quizes quiz = Hashes.getQuiz(index);
				
				//Start to print questions
				e.getGuild().getTextChannelById(channel).sendMessage("**"+quiz.getQuestion()+"**").complete();
				Thread.sleep(20000);
				
				//check the created file if someone was able to answer the question
				if(FileSetting.readFile(IniFileReader.getTempDirectory()+"AutoDelFiles/quiztime.azr").length() == 18 || FileSetting.readFile(IniFileReader.getTempDirectory()+"AutoDelFiles/quiztime.azr").length() == 17) {
					long user_id = Long.parseLong(FileSetting.readFile(IniFileReader.getTempDirectory()+"AutoDelFiles/quiztime.azr"));
					e.getGuild().getTextChannelById(channel).sendMessage("Question number "+index+" goes to "+e.getGuild().getMemberById(user_id).getAsMention()+". Congratulations!").queue();
					try {
						//send the reward in private message to the user and log the reward and user at the same time in the log channel.
						PrivateChannel pc = e.getGuild().getMemberById(user_id).getUser().openPrivateChannel().complete();
						pc.sendMessage("Congratulations. Here is your reward. Note that you need to wait 3 questions before you get eligible to get another reward:\n"
								+ "**"+quiz.getReward()+"**").queue();
						pc.close();
						e.getGuild().getTextChannelById(log_channel).sendMessage(message.setDescription("The user **"+e.getGuild().getMemberById(user_id).getUser().getName()+"#"+e.getGuild().getMemberById(user_id).getUser().getDiscriminator()+"** "
								+ "received the following reward:\n**"+quiz.getReward()+"**").build()).queue();
					} catch(Exception ex) {
						//When the reward couldn't be sent, throw this error.
						EmbedBuilder error = new EmbedBuilder().setColor(Color.RED).setTitle("Reward couldn't be sent to the user");
						e.getGuild().getTextChannelById(log_channel).sendMessage(error.setDescription("The user **"+e.getGuild().getMemberById(user_id).getUser().getName()+"#"+e.getGuild().getMemberById(user_id).getUser().getDiscriminator()+"** "
								+ "couldn't receive the reward. A possible reason could be that private messages were disabled. This is the code he should receive:\n"
								+ "**"+quiz.getReward()+"**").build()).queue();
					}
					Hashes.addQuizWinners(e.getGuild().getMemberById(user_id), 3);
					hints = 0;
					index++;
					Thread.sleep(20000);
				}
				//skip the question when an administrator types skip-question
				else if(FileSetting.readFile(IniFileReader.getTempDirectory()+"AutoDelFiles/quiztime.azr").equals("skip-question")) {
					e.getGuild().getTextChannelById(channel).sendMessage("Question skipped! Here is the next questions!").queue();
					index++;
					Thread.sleep(5000);
				}
				//interrupt the quiz when an administrator types interrupt-questions
				else if(FileSetting.readFile(IniFileReader.getTempDirectory()+"AutoDelFiles/quiztime.azr").equals("interrupt-questions")) {
					e.getGuild().getTextChannelById(channel).sendMessage("The quiz has been interrupted! Thank you all for participating! See you next time!").queue();
					break;
				}
				else {
					//if any hints exist for that question and if no 3 hints were given already, then throw a hint
					if(quiz.getHint1().length() > 0  && hints == 0) {
						e.getGuild().getTextChannelById(channel).sendMessage("No right answer has been given yet. Here is a hint:\n "+quiz.getHint1()).queue();
						hints++;
					}
					else if(quiz.getHint2().length() > 0 && hints == 1) {
						e.getGuild().getTextChannelById(channel).sendMessage("Sadly still no answer has been found. Here is another hint:\n"+quiz.getHint2()).queue();
						hints++;
					}
					else if(quiz.getHint3().length() > 0 && hints == 2) {
						e.getGuild().getTextChannelById(channel).sendMessage("We are getting desperate here. Here is the last available hint:\n"+quiz.getHint3()).queue();
						hints++;
					}
					else {
						int random = ThreadLocalRandom.current().nextInt(1, 3);
						e.getGuild().getTextChannelById(channel).sendMessage(replyList(random)+" Here is the question again:\n"
								+ "**"+quiz.getQuestion()+"**").queue();
					}
				}
				
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String replyList(int digit) {
		switch(digit) {
			case 1: return "I didn't see any valid replies yet. See you again in 20 seconds.";
			case 2: return "Retry again! No one got it right yet!";
			case 3: return "Sadly no hints are availale... Keep on trying!";
			default: return "";
		}
	}
}