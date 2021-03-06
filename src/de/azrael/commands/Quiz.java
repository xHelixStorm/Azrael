package de.azrael.commands;

import java.awt.Color;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.commandsContainer.QuizExecution;
import de.azrael.constructors.Cache;
import de.azrael.core.Hashes;
import de.azrael.core.UserPrivs;
import de.azrael.enums.Channel;
import de.azrael.enums.Translation;
import de.azrael.fileManagement.GuildIni;
import de.azrael.fileManagement.IniFileReader;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * The Quiz command can be used to register questions and
 * rewards and to run a quiz session for every participant
 * to enjoy.
 * @author xHelixStorm
 *
 */

public class Quiz implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Quiz.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		//check if the command is enabled and that the user has enough permissions
		if(GuildIni.getQuizCommand(e.getGuild().getIdLong())) {
			var commandLevel = GuildIni.getQuizLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else if(!GuildIni.getIgnoreMissingPermissions(e.getGuild().getIdLong()))
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		//be sure that the quiz session isn't already running
		if(Hashes.getTempCache("quiztime"+e.getGuild().getId()) == null) {
			EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
			//execute the command help if no parameters have been applied to the command
			if(args.length == 0) {
				e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ABOUT)).setDescription(STATIC.getTranslation(e.getMember(), Translation.QUIZ_HELP)).build()).queue();
			}
			//help command to register quiz rewards when there's just one parameter
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTER_REWARDS))) {
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.QUIZ_REWARDS_HELP)).build()).queue();
			}
			//register quiz rewards when we have 2 parameters
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTER_REWARDS))) {
				QuizExecution.registerRewards(e, args[1]);
			}
			//help command to register questions
			else if(args.length == 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTER_QUESTIONS))) {
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.QUIZ_QUESTIONS_HELP)).build()).queue();
			}
			//register quiz questions when we have 2 parameters
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTER_QUESTIONS))) {
				logger.info("{} performed the registration of questions and rewards for the Quiz in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
				QuizExecution.registerQuestions(e, args[1]);
			}
			//clear all questions and rewards
			else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_CLEAR))) {
				if(Azrael.SQLDeleteQuizData(e.getGuild().getIdLong()) >= 0) {
					Hashes.clearQuiz(e.getGuild().getIdLong());
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.QUIZ_CLEAR)).build()).queue();
					logger.info("{} cleared all quiz questions and rewards in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
				}
				else {
					e.getChannel().sendMessage(message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Quiz data couldn't be cleared in guild {}", e.getGuild().getId());
				}
			}
			//start the quiz in the dedicated channel
			else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_RUN))) {
				if(Azrael.SQLgetQuizData(e.getGuild().getIdLong())) {
					final var registeredQuiz = Hashes.getWholeQuiz(e.getGuild().getIdLong());
					if(registeredQuiz != null && registeredQuiz.size() > 0 && registeredQuiz.values().parallelStream().filter(f -> !f.isUsed()).findAny().orElse(null) != null) {
						//check that both questions and rewards have been set
						if(Hashes.getQuiz(e.getGuild().getIdLong(), 1).getQuestion().length() == 0) {
							e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.QUIZ_NO_Q_AND_A)).build()).queue();
						}
						else if(Hashes.getQuiz(e.getGuild().getIdLong(), 1).getReward().length() == 0) {
							e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.QUIZ_NO_REWARDS)).build()).queue();
						}
						else {
							final var qui_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.QUI.getType())).findAny().orElse(null);
							//confirm that a quiz channel exists and then print the message to choose a mode
							if(qui_channel != null) {
								//verify that the permissions are set
								final TextChannel textChannel = e.getGuild().getTextChannelById(qui_channel.getChannel_ID());
								if(textChannel != null && (e.getGuild().getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE) || STATIC.setPermissions(e.getGuild(), textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE)))) {
									e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.QUIZ_RUN_HELP)).build()).queue();
									//write to cache to remind the bot that we're waiting for input
									Hashes.addTempCache("quizstarter_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(180000, e.getMember().getUser().getId()));
								}
								else {
									e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.QUIZ_MISSING_PERMISSION)).build()).queue();
								}
							}
							else {
								e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.NOT_QUIZ_CHANNEL)).build()).queue();
							}
						}
					}
					else {
						e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.QUIZ_NO_QA_REWARDS)).build()).queue();
					}
				}
				else {
					e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.QUIZ_NO_QA_REWARDS)).build()).queue();
				}
			}
		}
		else {
			EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DENIED)).setThumbnail(IniFileReader.getDeniedThumbnail());
			e.getChannel().sendMessage(denied.setDescription(STATIC.getTranslation(e.getMember(), Translation.QUIZ_ERR)).build()).queue();
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.trace("{} has used Quiz command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}
}
