package commandsContainer;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import core.Channels;
import core.UserPrivs;
import fileManagement.FileSetting;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import preparedMessages.ReactionMessage;
import sql.SqlConnect;

public class RegisterChannel {
	
	public static void RegisterChannelHelper(MessageReceivedEvent _e){
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.WHITE).setThumbnail(IniFileReader.getSettingsThumbnail()).setTitle("Register text channels to give them unique functions!");
		StringBuilder strB = new StringBuilder();
		String parseMessage = null;
		
		parseMessage = "Please write the command in this format:\n**"+IniFileReader.getCommandPrefix()+"register -text-channel <channel-type> #channel-name/channel-id**\n\nHere are all available channel-types:\n\n";
		SqlConnect.SQLgetChannelTypes();
		for(Channels channels : SqlConnect.getChannels()){
			strB.append("**"+channels.getChannel_Type()+"** for a **"+channels.getChannel_Type_Name()+"**\n");
		}
		SqlConnect.clearChannelsArray();
		_e.getTextChannel().sendMessage(messageBuild.setDescription(parseMessage+strB.toString()).build()).queue();
	}
	
	public static void runCommand(MessageReceivedEvent _e, long _guild_id, String _message){
		EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Access Denied!");
		String channel;
		long channel_id;
		String channel_type;
		
		if(UserPrivs.isUserAdmin(_e.getMember().getUser(), _guild_id) || _e.getMember().getUser().getId().equals(IniFileReader.getAdmin())){
			Pattern pattern = Pattern.compile("(all|bot|eng|fre|ger|log|mus|tra|tur|rus|spa|por|ita|rea)");
			Matcher matcher = pattern.matcher(_message);
			if(matcher.find()){
				channel_type = matcher.group();
				channel = _message.replaceAll("[^0-9]*", "");
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
							SqlConnect.SQLInsertChannel_Conf(channel_id, _guild_id, channel_type);
							SqlConnect.SQLDeleteChannel_Filter(channel_id);
							SqlConnect.SQLInsertChannel_Filter(channel_id, channel_type);
							break;
						case "bot":
						case "mus":
							SqlConnect.SQLInsertChannel_Conf(channel_id, _guild_id, channel_type);
							SqlConnect.SQLDeleteChannel_Filter(channel_id);
							SqlConnect.SQLInsertChannel_Filter(channel_id, "all");
							break;
						case "log":
						case "tra":
						case "rea":
							SqlConnect.SQLDeleteChannelType(channel_type);
							SqlConnect.SQLInsertChannel_Conf(channel_id, _guild_id, channel_type);
							break;
					}
					_e.getTextChannel().sendMessage("**The channel has been registered!**").queue();
					if(channel_type.equals("rea")) {
						//create message in the channel and create an auto-delete-file so that the MessageListener can create the needed reactions
						String count = ""+ReactionMessage.print(_e, channel_id);
						FileSetting.createFile(IniFileReader.getTempDirectory()+"AutoDelFiles/reaction_gu"+_e.getGuild().getId()+"ch"+channel+".azr", count);
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
		if(UserPrivs.isUserAdmin(_e.getMember().getUser(), _guild_id) || _e.getMember().getUser().getId().equals(IniFileReader.getAdmin())){
			for(TextChannel tc : _e.getGuild().getTextChannels()){
				SqlConnect.SQLInsertChannels(tc.getIdLong(), tc.getName());
			}
			_e.getTextChannel().sendMessage("**All text channels have been registered!**").queue();
		}
		else {
			_e.getTextChannel().sendMessage(denied.setDescription(_e.getMember().getAsMention() + " **My apologies young padawan. This command can be used only from an Administrator. Here a cookie** :cookie:").build()).queue();
		}
	}
}
