package de.azrael.commandsContainer;

import java.awt.Color;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Channels;
import de.azrael.constructors.Thumbnails;
import de.azrael.core.Hashes;
import de.azrael.core.UserPrivs;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.preparedMessages.ReactionMessage;
import de.azrael.sql.Azrael;
import de.azrael.sql.BotConfiguration;
import de.azrael.subscription.SubscriptionUtils;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * Extension of the register command
 * @author xHelixStorm
 *
 */

public class RegisterChannel {
	private static final Logger logger = LoggerFactory.getLogger(RegisterChannel.class);
	
	public static void RegisterChannelHelper(GuildMessageReceivedEvent e, Thumbnails thumbnails) {
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(thumbnails.getSettings());
		StringBuilder strB = new StringBuilder();
		StringBuilder strB2 = new StringBuilder();
		
		final var channelTypes = Azrael.SQLgetChannelTypes();
		if(channelTypes != null) {
			for(Channels channels : channelTypes) {
				strB.append("**"+channels.getChannel_Type()+"**\n");
				strB2.append(channels.getChannel_Type_Name()+"\n");
			}
			if(strB.length() > 0) {
				e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_CHANNEL_HELP)).addField("", strB.toString(), true).addField("", strB2.toString(), true).build()).queue();
			}
			if(strB.length() == 0)
				e.getChannel().sendMessage(messageBuild.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_CHANNEL_NO_TYPES)).build()).queue();
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Channel types couldn't be retrieved in guild {}", e.getGuild().getId());
		}
	}
	
	public static void RegisterChannelHelperURL(GuildMessageReceivedEvent e) {
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(BotConfiguration.SQLgetThumbnails(e.getGuild().getIdLong()).getSettings());
		e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_CHANNEL_URL_HELP)
				.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE))
				.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_DISABLE))).build()).queue();
	}
	
	public static void RegisterChannelHelperTxt(GuildMessageReceivedEvent e) {
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(BotConfiguration.SQLgetThumbnails(e.getGuild().getIdLong()).getSettings());
		e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_CHANNEL_TXT_HELP)
				.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE))
				.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_DISABLE))).build()).queue();
	}
	
	public static boolean runCommand(GuildMessageReceivedEvent e, String [] args, Thumbnails thumbnails, BotConfigs botConfig) {
		String channel;
		String channel_type;
		
		final var commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.REGISTER_TEXT_CHANNEL);
		if(UserPrivs.comparePrivilege(e.getMember(), commandLevel)) {
			final var channelTypes = Azrael.SQLgetChannelTypes();
			if(channelTypes != null && channelTypes.size() > 0) {
				final HashMap<String, Integer> registerTypes = new HashMap<String, Integer>();
				StringBuilder out = new StringBuilder();
				for(final var channelType : channelTypes) {
					if(out.length() > 0)
						out.append("|");
					out.append(channelType.getChannel_Type());
					registerTypes.put(channelType.getChannel_Type(), channelType.getRegisterType());
				}
				Pattern pattern = Pattern.compile("("+out.toString()+")");
				Matcher matcher = pattern.matcher(args[1]);
				if(args.length == 3 && matcher.find()) {
					channel_type = matcher.group();
					channel = args[2].replaceAll("[^0-9]*", "");
					if(channel.length() > 0) {
						TextChannel textChannel = e.getGuild().getTextChannelById(channel);
						if(textChannel != null) {
							var result = 0;
							switch(registerTypes.get(channel_type)) {
								case 1 -> {
									final var languages = Azrael.SQLgetFilterLanguages();
									if(languages.size() == 0) {
										e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
										logger.error("Filter languages couldn't be retrieved in guild {}", e.getGuild().getId());
										return true;
									}
									else if(!languages.contains(channel_type)) {
										e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
										logger.error("Filter language {} doesn't exist in guild {}", channel_type, e.getGuild().getId());
										return true;
									}
									result = Azrael.SQLRegisterLanguageChannel(e.getGuild().getIdLong(), textChannel.getIdLong(), channel_type);
								}
								case 2 -> {
									result = Azrael.SQLRegisterSpecialChannel(e.getGuild().getIdLong(), textChannel.getIdLong(), channel_type);
								}
								case 3 -> {
									result = Azrael.SQLRegisterUniqueChannel(e.getGuild().getIdLong(), textChannel.getIdLong(), channel_type);
								}
							}
							Hashes.removeChannels(e.getGuild().getIdLong());
							if(result > 0) {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_CHANNEL_REGISTERED)).build()).queue();
								logger.info("User {} has registered channel {} as {} channel in guild {}", e.getMember().getUser().getId(), textChannel.getId(), channel_type, e.getGuild().getId());
								if(channel_type.equals("rea")) {
									//use the temp cache to append reactions after the bot sends a message
									if(Azrael.SQLUpdateReaction(e.getGuild().getIdLong(), true) > 0) {
										ReactionMessage.print(e, textChannel.getIdLong(), botConfig);
									}
									else {
										e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
										logger.error("Role reactions couldn't be automatically enabled in guild {}", e.getGuild().getId());
									}
								}
								else if(channel_type.equals("sub")) {
									SubscriptionUtils.startTimer(e.getJDA());
								}
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
								logger.error("Channel {} couldn't be saved as channel type {} in guild {}", textChannel.getId(), channel_type, e.getGuild().getId());
							}
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TEXT_CHANNEL_NOT_EXISTS)).build()).queue();
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
					}
				}
				else{
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
				}
				return true;
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Channel types couldn't be retrieved in guild {}", e.getGuild().getId());
			}
		}
		else {
			EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(thumbnails.getDenied()).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DENIED));
			e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(commandLevel, e.getMember())).build()).queue();
		}
		return false;
	}
	
	public static boolean runCommandURL(GuildMessageReceivedEvent e, String [] args, Thumbnails thumbnails) {
		final var commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.REGISTER_TEXT_CHANNEL_URL);
		if(UserPrivs.comparePrivilege(e.getMember(), commandLevel)) {
			if(args.length == 3) {
				args[2] = args[2].replaceAll("[<>#]", "");
				if(args[2].matches("[0-9]*")) {
					TextChannel textChannel = e.getGuild().getTextChannelById(args[2]);
					if(textChannel != null) {
						if(args[1].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE)) || args[1].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISABLE))) {
							var url_censoring = (args[1].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE)) ? true : false);
							if(Azrael.SQLInsertChannel_ConfURLCensoring(textChannel.getIdLong(), e.getGuild().getIdLong(), url_censoring) > 0) {
								Hashes.removeChannels(e.getGuild().getIdLong());
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_CHANNEL_REGISTERED)).build()).queue();
								logger.info("User {} has changed the url censoring status of channel {} to {} in guild {}", e.getMember().getUser().getId(), textChannel.getId(), url_censoring, e.getGuild().getId());
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
								logger.error("Url censoring setting couldn't be updated for channel {} in guild {}", textChannel.getId(), e.getGuild().getId());
							}
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TEXT_CHANNEL_NOT_EXISTS)).build()).queue();
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.NO_TEXT_CHANNEL)).build()).queue();
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
			}
			return true;
		}
		else {
			EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(thumbnails.getDenied()).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
			e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(commandLevel, e.getMember())).build()).queue();
		}
		return false;
	}
	
	public static boolean runCommandTxt(GuildMessageReceivedEvent e, String [] args, Thumbnails thumbnails) {
		final var commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.REGISTER_TEXT_CHANNEL_TXT);
		if(UserPrivs.comparePrivilege(e.getMember(), commandLevel)) {
			if(args.length == 3) {
				args[2] = args[2].replaceAll("[<>#]", "");
				if(args[2].matches("[0-9]*")) {
					TextChannel textChannel = e.getGuild().getTextChannelById(args[2]);
					if(textChannel != null) {
						if(args[1].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE)) || args[1].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISABLE))) {
							var txt_removal = (args[1].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE)) ? true : false);
							if(Azrael.SQLInsertChannel_ConfTXTCensoring(textChannel.getIdLong(), e.getGuild().getIdLong(), txt_removal) > 0) {
								Hashes.removeChannels(e.getGuild().getIdLong());
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_CHANNEL_REGISTERED)).build()).queue();
								logger.info("User {} has changed the text removal status of channel {} to {} in guild {}", e.getMember().getUser().getId(), textChannel.getId(), txt_removal, e.getGuild().getId());
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
								logger.error("Text removal setting couldn't be updated for channel {} in guild {}", textChannel.getId(), e.getGuild().getId());
							}
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.TEXT_CHANNEL_NOT_EXISTS)).build()).queue();
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.NO_TEXT_CHANNEL)).build()).queue();
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
			}
			return false;
		}
		else {
			EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(thumbnails.getDenied()).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
			e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(commandLevel, e.getMember())).build()).queue();
		}
		return true;
	}
	
	public static boolean runChannelsRegistration(GuildMessageReceivedEvent e, Thumbnails thumbnails) {
		final var commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.REGISTER_TEXT_CHANNELS);
		if(UserPrivs.comparePrivilege(e.getMember(), commandLevel)) {
			for(TextChannel tc : e.getGuild().getTextChannels()) {
				if(Azrael.SQLInsertChannels(tc.getIdLong(), tc.getName()) == 0) {
					e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.REGISTER_CHANNEL_ERR).replace("{}", tc.getName())).queue();
					logger.error("channel {} couldn't be registered in guild {}", tc.getId(), e.getGuild().getId());
				}
			}
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_ALL_CHANNELS)).build()).queue();
			logger.info("User {} has registered all available channels in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			return true;
		}
		else {
			EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(thumbnails.getDenied()).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DENIED));
			e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(commandLevel, e.getMember())).build()).queue();
		}
		return false;
	}
}
