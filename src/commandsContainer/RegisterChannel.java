package commandsContainer;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Channels;
import core.Hashes;
import core.UserPrivs;
import enums.Translation;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import preparedMessages.ReactionMessage;
import sql.Azrael;
import timerTask.ParseSubscription;
import util.STATIC;

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
			logger.error("Channel types from Azrael.channeltypes couldn't be retrieved in guild {}", e.getGuild().getId());
		}
	}
	
	public static void RegisterChannelHelperURL(GuildMessageReceivedEvent e) {
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(IniFileReader.getSettingsThumbnail());
		e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_CHANNEL_URL_HELP)).build()).queue();
	}
	
	public static void RegisterChannelHelperTxt(GuildMessageReceivedEvent e) {
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(IniFileReader.getSettingsThumbnail());
		e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_CHANNEL_TXT_HELP)).build()).queue();
	}
	
	public static void runCommand(GuildMessageReceivedEvent e, long _guild_id, String [] _args, boolean adminPermission) {
		String channel;
		long channel_id;
		String channel_type;
		
		final var commandLevel = GuildIni.getRegisterTextChannelLevel(e.getGuild().getIdLong());
		if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
			Pattern pattern = Pattern.compile("(all|bot|eng|fre|ger|log|mus|tra|tur|rus|spa|por|ita|rea|qui|rss|wat|del|edi|vot|co1|co2|co3)");
			Matcher matcher = pattern.matcher(_args[1]);
			if(_args.length > 2 && matcher.find()) {
				channel_type = matcher.group();
				channel = _args[2].replaceAll("[^0-9]*", "");
				if(channel.length() == 18) {
					channel_id = Long.parseLong(channel);
					var result = 0;
					switch(channel_type) {
						case "eng", "ger", "fre", "tur", "rus", "spa", "por", "ita", "all" -> {
							Azrael.SQLInsertChannel_Conf(channel_id, _guild_id, channel_type);
							Azrael.SQLDeleteChannel_Filter(channel_id);
							result = Azrael.SQLInsertChannel_Filter(channel_id, channel_type);
							if(result == 0)
								logger.error("New channel type {} couldn't be inserted for channel {} in guild {} into Azrael.channel_filter", channel_type, channel_id, _guild_id);
						}
						case "bot", "co1", "co2", "co3" -> {
							result = Azrael.SQLInsertChannel_Conf(channel_id, _guild_id, channel_type);
							Azrael.SQLDeleteChannel_Filter(channel_id);
							if(result == 0)
								logger.error("New channel filter all couldn't be inserted for channel {} in guild {} into Azrael.channel_filter", channel_id, _guild_id);
							
						}
						case "log", "tra", "rea", "qui", "rss", "wat", "del", "edi", "vot" -> {
							Azrael.SQLDeleteChannelType(channel_type, _guild_id);
							result = Azrael.SQLInsertChannel_Conf(channel_id, _guild_id, channel_type);
							if(result == 0)
								logger.error("New channel type {} couldn't be inserted for channel {} in guild {} into Azrael.channel_filter", channel_type, channel_id, _guild_id);
						}
					}
					Hashes.removeChannels(_guild_id);
					if(result > 0) {
						logger.debug("{} has registered the channel {} as {} channel in guild {}", e.getMember().getUser().getId(), channel_type, channel_type, _guild_id);
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_CHANNEL_REGISTERED)).build()).queue();
						if(channel_type.equals("rea")) {
							//use the temp cache to append reactions after the bot sends a message
							if(Azrael.SQLUpdateReaction(_guild_id, true) > 0) {
								ReactionMessage.print(e, channel_id);
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
								logger.error("Role reactions couldn't be set to enable in Azrael.guild for guild {}", _guild_id);
							}
						}
						else if(channel_type.equals("rss") && !ParseSubscription.timerIsRunning(e.getGuild().getIdLong())) {
							ParseSubscription.runTask(e.getJDA(), e.getGuild().getIdLong());
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("text channel {} couldn't be registered in guild {}", channel_id, _guild_id);
					}
				}
			}
			else{
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
			}
		}
		else {
			EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DENIED));
			e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(commandLevel, e.getMember())).build()).queue();
		}
	}
	
	public static void runCommandURL(GuildMessageReceivedEvent e, long _guild_id, String [] args, boolean adminPermission) {
		final var commandLevel = GuildIni.getRegisterTextChannelURLLevel(e.getGuild().getIdLong());
		if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
			var channel = args[1].replaceAll("[<>#]", "");
			if(args.length > 2 && e.getGuild().getTextChannelById(channel) != null) {
				var channel_id = Long.parseLong(channel);
				if(args[2].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE)) || args[2].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISABLE))) {
					var url_censoring = (args[2].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE)) ? true : false);
					if(Azrael.SQLInsertChannel_ConfURLCensoring(channel_id, _guild_id, url_censoring) > 0) {
						Hashes.removeChannels(_guild_id);
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_CHANNEL_REGISTERED)).build()).queue();
						logger.debug("{} has registered the channel {} for url censoring in the guild {}", e.getMember().getUser().getId(), channel_id, e.getGuild().getId());
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Azrael.channel_conf couldn't be updated for channel {} and guild {}", channel_id, e.getGuild().getId());
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.NO_TEXT_CHANNEL)).build()).queue();
			}
		}
		else {
			EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
			e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(commandLevel, e.getMember())).build()).queue();
		}
	}
	
	public static void runCommandTxt(GuildMessageReceivedEvent e, long _guild_id, String [] args, boolean adminPermission) {
		final var commandLevel = GuildIni.getRegisterTextChannelTXTLevel(e.getGuild().getIdLong());
		if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
			var channel = args[1].replaceAll("[<>#]", "");
			if(args.length > 2 && e.getGuild().getTextChannelById(channel) != null) {
				var channel_id = Long.parseLong(channel);
				if(args[2].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE)) || args[2].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISABLE))) {
					var txt_removal = (args[2].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ENABLE)) ? true : false);
					if(Azrael.SQLInsertChannel_ConfTXTCensoring(channel_id, _guild_id, txt_removal) > 0) {
						Hashes.removeChannels(_guild_id);
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_CHANNEL_REGISTERED)).build()).queue();
						logger.debug("{} has registered the channel {} for text removal in the guild {}", e.getMember().getUser().getId(), channel_id, e.getGuild().getId());
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Azrael.channel_conf couldn't be updated for channel {} and guild {}", channel_id, e.getGuild().getId());
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.NO_TEXT_CHANNEL)).build()).queue();
			}
		}
		else {
			EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
			e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(commandLevel, e.getMember())).build()).queue();
		}
	}
	
	public static void runChannelsRegistration(GuildMessageReceivedEvent e, long _guild_id, boolean adminPermission) {
		final var commandLevel = GuildIni.getRegisterTextChannelsLevel(e.getGuild().getIdLong());
		if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || adminPermission) {
			for(TextChannel tc : e.getGuild().getTextChannels()) {
				if(Azrael.SQLInsertChannels(tc.getIdLong(), tc.getName()) == 0) {
					e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.REGISTER_CHANNEL_ERR).replace("{}", tc.getName())).queue();
					logger.error("channel {} couldn't be registered in guild {}", tc.getId(), e.getGuild().getId());
				}
			}
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_ALL_CHANNELS)).build()).queue();
			logger.debug("{} has registered all available channels on guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
		}
		else {
			EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DENIED));
			e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(commandLevel, e.getMember())).build()).queue();
		}
	}
}
