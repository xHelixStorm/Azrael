package de.azrael.commands;

import java.awt.Color;
import java.util.EnumSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Cache;
import de.azrael.constructors.Channels;
import de.azrael.constructors.Quizes;
import de.azrael.core.Hashes;
import de.azrael.enums.Channel;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.sql.BotConfiguration;
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
	private static ConcurrentHashMap<Long, Integer> commandLevel = new ConcurrentHashMap<Long, Integer>();

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.QUIZ);
	}

	@Override
	public boolean action(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		//be sure that the quiz session isn't already running
		if(Hashes.getTempCache("quiztime_gu"+e.getGuild().getId()) == null) {
			EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
			//execute the command help if no parameters have been applied to the command
			if(args.length == 0) {
				e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ABOUT)).setDescription(STATIC.getTranslation(e.getMember(), Translation.QUIZ_HELP)
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTER_REWARDS))
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTER_QUESTIONS))
						.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_CLEAR))
						.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_RUN))).build()).queue();
			}
			else {
				final String key = "quiz_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId();
				Hashes.clearTempCache(key);
				//help command to register quiz rewards when there's just one parameter
				if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTER_REWARDS))) {
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.QUIZ_REWARDS_HELP)).build()).queue();
					Hashes.addTempCache(key, new Cache(TimeUnit.MINUTES.toMillis(3), "r"));
				}
				//help command to register questions
				else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REGISTER_QUESTIONS))) {
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.QUIZ_QUESTIONS_HELP)).build()).queue();
					Hashes.addTempCache(key, new Cache(TimeUnit.MINUTES.toMillis(3), "q"));
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
							Quizes quiz = Hashes.getQuiz(e.getGuild().getIdLong(), 1);
							if(quiz.getQuestion() == null || quiz.getQuestion().length() == 0) {
								e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.QUIZ_NO_Q_AND_A)).build()).queue();
							}
							else if(quiz.getReward() == null || quiz.getReward().length() == 0) {
								e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.QUIZ_NO_REWARDS)).build()).queue();
							}
							else {
								var qui_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals(Channel.QUI.getType())).findAny().orElse(null);
								if(qui_channel == null) {
									qui_channel = new Channels();
									qui_channel.setChannel_ID(e.getChannel().getIdLong());
								}
								//print the message to choose a mode and verify that the permissions are set
								final TextChannel textChannel = e.getGuild().getTextChannelById(qui_channel.getChannel_ID());
								if(textChannel != null && (e.getGuild().getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE) || STATIC.setPermissions(e.getGuild(), textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE)))) {
									e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.QUIZ_RUN_HELP)).build()).queue();
									//write to cache to remind the bot that we're waiting for input
									Hashes.addTempCache("quizstarter_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId(), new Cache(180000, e.getMember().getUser().getId()));
									commandLevel.put(e.getGuild().getIdLong(), STATIC.getCommandLevel(e.getGuild(), Command.QUIZ));
								}
								else {
									e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.QUIZ_MISSING_PERMISSION)).build()).queue();
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
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
				}
			}
		}
		else {
			EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DENIED)).setThumbnail(BotConfiguration.SQLgetThumbnails(e.getGuild().getIdLong()).getDenied());
			e.getChannel().sendMessage(denied.setDescription(STATIC.getTranslation(e.getMember(), Translation.QUIZ_ERR)).build()).queue();
		}
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Quiz command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.QUIZ.getColumn(), out.toString().trim());
		}
	}
	
	public static int getCommandLevel(long guild_id) {
		return commandLevel.get(guild_id);
	}
}
