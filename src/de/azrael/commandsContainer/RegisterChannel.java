package de.azrael.commandsContainer;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Channels;
import de.azrael.core.Hashes;
import de.azrael.core.UserPrivs;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.fileManagement.IniFileReader;
import de.azrael.preparedMessages.ReactionMessage;
import de.azrael.sql.Azrael;
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
	
	public static void RegisterChannelHelper(GuildMessageReceivedEvent e) {
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(IniFileReader.getSettingsThumbnail());
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
			e.getChannel().sendMessage(messageBuild.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Channel types couldn't be retrieved in guild {}", e.getGuild().getId());
		}
	}
	
	public static void RegisterChannelHelperURL(GuildMessageReceivedEvent e) {
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(IniFileReader.getSettingsThumbnail());
		e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_CHANNEL_URL_HELP)
				.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE))
				.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_DISABLE))).build()).queue();
	}
	
	public static void RegisterChannelHelperTxt(GuildMessageReceivedEvent e) {
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(IniFileReader.getSettingsThumbnail());
		e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_CHANNEL_TXT_HELP)
				.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE))
				.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_DISABLE))).build()).queue();
	}
	
	public static boolean runCommand(GuildMessageReceivedEvent e, long guild_id, String [] args, boolean adminPermission) {
		String channel;
		long channel_id;
		String channel_type;
		
		final var commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.REGISTER_TEXT_CHANNEL);
		if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
			Pattern pattern = Pattern.compile("(all|bot|eng|fre|ger|log|mus|tra|tur|rus|spa|por|ita|ara|rea|qui|sub|wat|del|edi|vot|vo2|co1|co2|co3|co4|co5|co6|upd)");
			Matcher matcher = pattern.matcher(args[1]);
			if(args.length > 2 && matcher.find()) {
				channel_type = matcher.group();
				channel = args[2].replaceAll("[^0-9]*", "");
				if(channel.length() == 18) {
					channel_id = Long.parseLong(channel);
					var result = 0;
					switch(channel_type) {
						case "eng", "ger", "fre", "tur", "rus", "spa", "por", "ita", "ara", "all" -> {
							result = Azrael.SQLRegisterLanguageChannel(guild_id, channel_id, channel_type);
						}
						case "bot", "co1", "co2", "co3", "co4", "co5", "co6", "vot", "vo2" -> {
							result = Azrael.SQLRegisterSpecialChannel(guild_id, channel_id, channel_type);
						}
						case "log", "tra", "rea", "qui", "sub", "wat", "del", "edi", "upd" -> {
							result = Azrael.SQLRegisterUniqueChannel(guild_id, channel_id, channel_type);
						}
					}
					Hashes.removeChannels(guild_id);
					if(result > 0) {
						logger.info("User {} has registered channel {} as {} channel in guild {}", e.getMember().getUser().getId(), channel_id, channel_type, guild_id);
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_CHANNEL_REGISTERED)).build()).queue();
						if(channel_type.equals("rea")) {
							//use the temp cache to append reactions after the bot sends a message
							if(Azrael.SQLUpdateReaction(guild_id, true) > 0) {
								ReactionMessage.print(e, channel_id);
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
								logger.error("Role reactions couldn't be automatically enabled in guild {}", guild_id);
							}
						}
						else if(channel_type.equals("sub")) {
							SubscriptionUtils.startTimer(e.getJDA());
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Channel {} couldn't be saved as channel type {} in guild {}", channel_id, channel_type, guild_id);
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
			EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DENIED));
			e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(commandLevel, e.getMember())).build()).queue();
		}
		return false;
	}
	
	public static boolean runCommandURL(GuildMessageReceivedEvent e, long _guild_id, String [] args, boolean adminPermission) {
		final var commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.REGISTER_TEXT_CHANNEL_URL);
		if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
			var channel = args[1].replaceAll("[<>#]", "");
			if(args.length > 2 && e.getGuild().getTextChannelById(channel) != null) {
				var channel_id = Long.parseLong(channel);
				if(args[2].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE)) || args[2].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISABLE))) {
					var url_censoring = (args[2].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE)) ? true : false);
					if(Azrael.SQLInsertChannel_ConfURLCensoring(channel_id, _guild_id, url_censoring) > 0) {
						Hashes.removeChannels(_guild_id);
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_CHANNEL_REGISTERED)).build()).queue();
						logger.info("User {} has changed the url censoring status of channel {} to {} in guild {}", e.getMember().getUser().getId(), channel_id, url_censoring, e.getGuild().getId());
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Url censoring setting couldn't be updated for channel {} in guild {}", channel_id, e.getGuild().getId());
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.NO_TEXT_CHANNEL)).build()).queue();
			}
			return true;
		}
		else {
			EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
			e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(commandLevel, e.getMember())).build()).queue();
		}
		return false;
	}
	
	public static boolean runCommandTxt(GuildMessageReceivedEvent e, long _guild_id, String [] args, boolean adminPermission) {
		final var commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.REGISTER_TEXT_CHANNEL_TXT);
		if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
			var channel = args[1].replaceAll("[<>#]", "");
			if(args.length > 2 && e.getGuild().getTextChannelById(channel) != null) {
				var channel_id = Long.parseLong(channel);
				if(args[2].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE)) || args[2].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISABLE))) {
					var txt_removal = (args[2].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE)) ? true : false);
					if(Azrael.SQLInsertChannel_ConfTXTCensoring(channel_id, _guild_id, txt_removal) > 0) {
						Hashes.removeChannels(_guild_id);
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_CHANNEL_REGISTERED)).build()).queue();
						logger.info("User {} has changed the text removal status of channel {} to {} in guild {}", e.getMember().getUser().getId(), channel_id, txt_removal, e.getGuild().getId());
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Text removal setting couldn't be updated for channel {} in guild {}", channel_id, e.getGuild().getId());
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.NO_TEXT_CHANNEL)).build()).queue();
			}
			return false;
		}
		else {
			EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
			e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(commandLevel, e.getMember())).build()).queue();
		}
		return true;
	}
	
	public static boolean runChannelsRegistration(GuildMessageReceivedEvent e, long _guild_id, boolean adminPermission) {
		final var commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.REGISTER_TEXT_CHANNELS);
		if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
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
			EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DENIED));
			e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(commandLevel, e.getMember())).build()).queue();
		}
		return false;
	}
}
