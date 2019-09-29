package commandsContainer;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Channels;
import core.Hashes;
import core.UserPrivs;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import preparedMessages.ReactionMessage;
import sql.Azrael;

public class RegisterChannel {
	private static final Logger logger = LoggerFactory.getLogger(RegisterChannel.class);
	
	public static void RegisterChannelHelper(GuildMessageReceivedEvent _e) {
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.WHITE).setThumbnail(IniFileReader.getSettingsThumbnail()).setTitle("Register text channels to give them unique functions!");
		StringBuilder strB = new StringBuilder();
		String parseMessage = null;
		
		parseMessage = "Please write the command in this format:\n**"+GuildIni.getCommandPrefix(_e.getGuild().getIdLong())+"register -text-channel <channel-type> #channel-name/channel-id**\n\nHere are all available channel-types:\n\n";
		for(Channels channels : Azrael.SQLgetChannelTypes()) {
			strB.append("**"+channels.getChannel_Type()+"** for a **"+channels.getChannel_Type_Name()+"**\n");
		}
		if(strB.length() == 0)
			strB.append("<No available channel types found>");
		_e.getChannel().sendMessage(messageBuild.setDescription(parseMessage+strB.toString()).build()).queue();
	}
	
	public static void RegisterChannelHelperURL(GuildMessageReceivedEvent _e) {
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.WHITE).setThumbnail(IniFileReader.getSettingsThumbnail()).setTitle("Register text channels to enable or disable the url censoring!");
		_e.getChannel().sendMessage(messageBuild.setDescription("Enable or disable the url censoring for one text-channel. By default, the url censoring is disabled on every channel. Please write the command in this format:\n**"+GuildIni.getCommandPrefix(_e.getGuild().getIdLong())+"register -text-channel-url #<text-channel-name> enable/disable**").build()).queue();
	}
	
	public static void RegisterChannelHelperTxt(GuildMessageReceivedEvent _e) {
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.WHITE).setThumbnail(IniFileReader.getSettingsThumbnail()).setTitle("Register text channels to enable or disable the text removal!");
		_e.getChannel().sendMessage(messageBuild.setDescription("Enable or disable the text removal for one text-channel. By default, the text removal is disabled on every channel. Please write the command in this format:\n**"+GuildIni.getCommandPrefix(_e.getGuild().getIdLong())+"register -text-channel-txt #<text-channel-name> enable/disable**").build()).queue();
	}
	
	@SuppressWarnings("preview")
	public static void runCommand(GuildMessageReceivedEvent _e, long _guild_id, String [] _args, boolean adminPermission) {
		EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Access Denied!");
		String channel;
		long channel_id;
		String channel_type;
		
		final var commandLevel = GuildIni.getRegisterTextChannelLevel(_e.getGuild().getIdLong());
		if(UserPrivs.comparePrivilege(_e.getMember(), commandLevel) || adminPermission) {
			Pattern pattern = Pattern.compile("(all|bot|eng|fre|ger|log|mus|tra|tur|rus|spa|por|ita|rea|qui|rss|wat|del|edi)");
			Matcher matcher = pattern.matcher(_args[1]);
			if(_args.length > 2 && matcher.find()) {
				channel_type = matcher.group();
				channel = _args[2].replaceAll("[^0-9]*", "");
				if(channel.length() == 18) {
					channel_id = Long.parseLong(channel);
					switch(channel_type) {
						case "eng", "ger", "fre", "tur", "rus", "spa", "por", "ita", "all" -> {
							Azrael.SQLInsertChannel_Conf(channel_id, _guild_id, channel_type);
							Azrael.SQLDeleteChannel_Filter(channel_id);
							Azrael.SQLInsertChannel_Filter(channel_id, channel_type);
						}
						case "bot", "mus" -> {
							Azrael.SQLInsertChannel_Conf(channel_id, _guild_id, channel_type);
							Azrael.SQLDeleteChannel_Filter(channel_id);
							Azrael.SQLInsertChannel_Filter(channel_id, "all");
						}
						case "log", "tra", "rea", "qui", "rss", "wat", "del", "edi" -> {
							Azrael.SQLDeleteChannelType(channel_type, _guild_id);
							Azrael.SQLInsertChannel_Conf(channel_id, _guild_id, channel_type);
						}
					}
					Hashes.removeChannels(_guild_id);
					logger.debug("{} has registered the channel {} as {} channel in guild {}", _e.getMember().getUser().getId(), channel_type, channel_type, _e.getGuild().getId());
					_e.getChannel().sendMessage("**The channel has been registered!**").queue();
					if(channel_type.equals("rea")) {
						//use the temp cache to append reactions after the bot sends a message
						if(Azrael.SQLInsertCommand(_e.getGuild().getIdLong(), 0, true) > 0) {
							ReactionMessage.print(_e, channel_id);
						}
						else {
							logger.error("Role reactions couldn't be set to enable for guild {}", _e.getGuild().getName());
							_e.getChannel().sendMessage("An internal error occurred. The reactions couldn't be marked as enabled in the table Azrael.commands").queue();
						}
					}
				}
			}
			else{
				_e.getChannel().sendMessage(_e.getMember().getAsMention()+" Something went wrong. Please recheck the syntax!").queue();
			}
		}
		else {
			_e.getChannel().sendMessage(denied.setDescription(_e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(commandLevel, _e.getGuild())).build()).queue();
		}
	}
	
	public static void runCommandURL(GuildMessageReceivedEvent _e, long _guild_id, String [] args, boolean adminPermission) {
		final var commandLevel = GuildIni.getRegisterTextChannelURLLevel(_e.getGuild().getIdLong());
		if(UserPrivs.comparePrivilege(_e.getMember(), commandLevel) || adminPermission) {
			try {
				var channel_id = Long.parseLong(args[1].replaceAll("[<>#]", ""));
				if(args.length > 2 && _e.getGuild().getTextChannelById(channel_id) != null) {
					if(args[2].equalsIgnoreCase("enable") || args[2].equalsIgnoreCase("disable")) {
						var url_censoring = (args[2].equalsIgnoreCase("enable") ? true : false);
						if(Azrael.SQLInsertChannel_ConfURLCensoring(channel_id, _guild_id, url_censoring) > 0) {
							Hashes.removeChannels(_guild_id);
							logger.debug("{} has registered the channel {} for url censoring in the guild {}", _e.getMember().getUser().getId(), channel_id, _e.getGuild().getName());
							_e.getChannel().sendMessage("**The channel has been registered!**").queue();
						}
						else {
							logger.error("Azrael.channel_conf couldn't be updated for channel {} and guild {}", channel_id, _e.getGuild().getName());
							_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Internal error!").setDescription("An internal error occurred! Azrael.channel_conf couldn't be updated!").build()).queue();
						}
					}
					else {
						_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Syntax error!").setDescription("Please use either enable or disable!").build()).queue();
					}
				}
				else {
					_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Syntax error!").setDescription("Please insert a valid text channel!").build()).queue();
				}
			} catch(NumberFormatException | NullPointerException exc) {
				_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Syntax error!").setDescription("Please insert a valid text channel!").build()).queue();
			}
		}
		else {
			EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Access Denied!");
			_e.getChannel().sendMessage(denied.setDescription(_e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(commandLevel, _e.getGuild())).build()).queue();
		}
	}
	
	public static void runCommandTxt(GuildMessageReceivedEvent _e, long _guild_id, String [] args, boolean adminPermission) {
		final var commandLevel = GuildIni.getRegisterTextChannelTXTLevel(_e.getGuild().getIdLong());
		if(UserPrivs.comparePrivilege(_e.getMember(), commandLevel) || adminPermission) {
			try {
				var channel_id = Long.parseLong(args[1].replaceAll("[<>#]", ""));
				if(args.length > 2 && _e.getGuild().getTextChannelById(channel_id) != null) {
					if(args[2].equalsIgnoreCase("enable") || args[2].equalsIgnoreCase("disable")) {
						var txt_removal = (args[2].equalsIgnoreCase("enable") ? true : false);
						if(Azrael.SQLInsertChannel_ConfTXTCensoring(channel_id, _guild_id, txt_removal) > 0) {
							Hashes.removeChannels(_guild_id);
							logger.debug("{} has registered the channel {} for text removal in the guild {}", _e.getMember().getUser().getId(), channel_id, _e.getGuild().getName());
							_e.getChannel().sendMessage("**The channel has been registered!**").queue();
						}
						else {
							logger.error("Azrael.channel_conf couldn't be updated for channel {} and guild {}", channel_id, _e.getGuild().getName());
							_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Internal error!").setDescription("An internal error occurred! Azrael.channel_conf couldn't be updated!").build()).queue();
						}
					}
					else {
						_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Syntax error!").setDescription("Please use either enable or disable!").build()).queue();
					}
				}
				else {
					_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Syntax error!").setDescription("Please insert a valid text channel!").build()).queue();
				}
			} catch(NumberFormatException | NullPointerException exc) {
				_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Syntax error!").setDescription("Please insert a valid text channel!").build()).queue();
			}
		}
		else {
			EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Access Denied!");
			_e.getChannel().sendMessage(denied.setDescription(_e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(commandLevel, _e.getGuild())).build()).queue();
		}
	}
	
	public static void runChannelsRegistration(GuildMessageReceivedEvent _e, long _guild_id, boolean adminPermission) {
		EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Access Denied!");
		final var commandLevel = GuildIni.getRegisterTextChannelsLevel(_e.getGuild().getIdLong());
		if(UserPrivs.comparePrivilege(_e.getMember(), commandLevel) || adminPermission) {
			for(TextChannel tc : _e.getGuild().getTextChannels()) {
				if(Azrael.SQLInsertChannels(tc.getIdLong(), tc.getName()) == 0) {
					logger.error("channel {} couldn't be registered", tc.getId());
					_e.getChannel().sendMessage("An internal error occurred. Channel "+tc.getName()+" couldn't be inserted into Azrael.channels").queue();
				}
			}
			logger.debug("{} has registered all available channels on guild {}", _e.getMember().getUser().getId(), _e.getGuild().getName());
			_e.getChannel().sendMessage("**All text channels have been registered!**").queue();
		}
		else {
			_e.getChannel().sendMessage(denied.setDescription(_e.getMember().getAsMention() + " **My apologies young padawan. Higher privileges are required. Here a cookie** :cookie:\nOne of these roles are required: "+UserPrivs.retrieveRequiredRoles(commandLevel, _e.getGuild())).build()).queue();
		}
	}
}
