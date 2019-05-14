package commandsContainer;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Cache;
import core.Channels;
import core.Hashes;
import core.UserPrivs;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import preparedMessages.ReactionMessage;
import sql.Azrael;

public class RegisterChannel {
	private static final Logger logger = LoggerFactory.getLogger(RegisterChannel.class);
	
	public static void RegisterChannelHelper(MessageReceivedEvent _e){
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.WHITE).setThumbnail(IniFileReader.getSettingsThumbnail()).setTitle("Register text channels to give them unique functions!");
		StringBuilder strB = new StringBuilder();
		String parseMessage = null;
		
		parseMessage = "Please write the command in this format:\n**"+GuildIni.getCommandPrefix(_e.getGuild().getIdLong())+"register -text-channel <channel-type> #channel-name/channel-id**\n\nHere are all available channel-types:\n\n";
		for(Channels channels : Azrael.SQLgetChannelTypes()){
			strB.append("**"+channels.getChannel_Type()+"** for a **"+channels.getChannel_Type_Name()+"**\n");
		}
		if(strB.length() == 0)
			strB.append("<No available channel types found>");
		_e.getTextChannel().sendMessage(messageBuild.setDescription(parseMessage+strB.toString()).build()).queue();
	}
	
	public static void runCommand(MessageReceivedEvent _e, long _guild_id, String [] _args){
		EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Access Denied!");
		String channel;
		long channel_id;
		String channel_type;
		
		if(UserPrivs.isUserAdmin(_e.getMember().getUser(), _guild_id) || _e.getMember().getUser().getIdLong() == GuildIni.getAdmin(_e.getGuild().getIdLong())){
			Pattern pattern = Pattern.compile("(all|bot|eng|fre|ger|log|mus|tra|tur|rus|spa|por|ita|rea|qui|rss)");
			Matcher matcher = pattern.matcher(_args[1]);
			if(_args.length > 2 && matcher.find()){
				channel_type = matcher.group();
				channel = _args[2].replaceAll("[^0-9]*", "");
				if(channel.length() == 18){
					channel_id = Long.parseLong(channel);
					switch(channel_type){
						case "eng":
						case "ger":
						case "fre":
						case "tur":
						case "rus":
						case "spa":
						case "por":
						case "ita":
						case "all":
							Azrael.SQLInsertChannel_Conf(channel_id, _guild_id, channel_type);
							Azrael.SQLDeleteChannel_Filter(channel_id);
							Azrael.SQLInsertChannel_Filter(channel_id, channel_type);
							break;
						case "bot":
						case "mus":
							Azrael.SQLInsertChannel_Conf(channel_id, _guild_id, channel_type);
							Azrael.SQLDeleteChannel_Filter(channel_id);
							Azrael.SQLInsertChannel_Filter(channel_id, "all");
							break;
						case "log":
						case "tra":
						case "rea":
						case "qui":
						case "rss":
							Azrael.SQLDeleteChannelType(channel_type, _guild_id);
							Azrael.SQLInsertChannel_Conf(channel_id, _guild_id, channel_type);
							break;
					}
					Hashes.removeChannels(_guild_id);
					logger.debug("{} has registered the channel {} as {} channel in the guild {}", _e.getMember().getUser().getId(), channel_type, channel_type, _e.getGuild().getName());
					_e.getTextChannel().sendMessage("**The channel has been registered!**").queue();
					if(channel_type.equals("rea")) {
						//create message in the channel and create an auto-delete-file so that the MessageListener can create the needed reactions
						if(Azrael.SQLInsertCommand(_e.getGuild().getIdLong(), 0, true) > 0) {
							String count = ""+ReactionMessage.print(_e, channel_id);
							Hashes.addTempCache("reaction_gu"+_e.getGuild().getId()+"ch"+channel, new Cache(0, count));
						}
						else {
							logger.error("Role reactions couldn't be set to enable for guild {}", _e.getGuild().getName());
							_e.getTextChannel().sendMessage("An internal error occurred. The reactions couldn't be marked as enabled in the table Azrael.commands").queue();
						}
					}
				}
			}
			else{
				_e.getTextChannel().sendMessage(_e.getMember().getAsMention()+" Something went wrong. Please recheck the syntax!").queue();
			}
		}
		else {
			_e.getTextChannel().sendMessage(denied.setDescription(_e.getMember().getAsMention() + " **My apologies young padawan. This command can be used only from an Administrator. Here a cookie** :cookie:").build()).queue();
		}
	}
	
	public static void runChannelsRegistration(MessageReceivedEvent _e, long _guild_id){
		EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Access Denied!");
		if(UserPrivs.isUserAdmin(_e.getMember().getUser(), _guild_id) || _e.getMember().getUser().getIdLong() == IniFileReader.getAdmin()){
			for(TextChannel tc : _e.getGuild().getTextChannels()){
				if(Azrael.SQLInsertChannels(tc.getIdLong(), tc.getName()) == 0) {
					logger.error("channel {} couldn't be registered", tc.getId());
					_e.getTextChannel().sendMessage("An internal error occurred. Channel "+tc.getName()+" couldn't be inserted into Azrael.channels").queue();
				}
			}
			logger.debug("{} has registered all available channels on guild {}", _e.getMember().getUser().getId(), _e.getGuild().getName());
			_e.getTextChannel().sendMessage("**All text channels have been registered!**").queue();
		}
		else {
			_e.getTextChannel().sendMessage(denied.setDescription(_e.getMember().getAsMention() + " **My apologies young padawan. This command can be used only from an Administrator. Here a cookie** :cookie:").build()).queue();
		}
	}
}
