package commandsContainer;

import java.awt.Color;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;

import core.Guilds;
import core.Hashes;
import core.Messages;
import fileManagement.FileSetting;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import rankingSystem.Rank;
import rankingSystem.Ranks;
import sql.RankingDB;
import sql.ServerRoles;
import sql.SqlConnect;
import threads.DelayDelete;
import util.Pastebin;

public class UserExecution {
	public static void getHelp(MessageReceivedEvent _e) {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE).setTitle("Help for the user command!");
		_e.getTextChannel().sendMessage(message.setDescription("Mention a user right after the command and then choose an action to take. For example to display information, to mute, to ban, to set a warning value, to set a level or to gift experience points for the ranking system").build()).queue();
	}
	
	public static void runTask(MessageReceivedEvent _e, String _input, String _displayed_input) {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE).setTitle("Choose the desired action");
		File file = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/user_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId()+"_0.azr");
		String file_name = IniFileReader.getTempDirectory()+"AutoDelFiles/user_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId()+"_0.azr";
		boolean break_while = false;
		int i = 0;
		
		while(i < 19 && break_while == false){
			if(file.exists()){
				file = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/user_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId()+"_"+(i+1)+".azr");
				file_name = IniFileReader.getTempDirectory()+"AutoDelFiles/user_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId()+"_"+(i+1)+".azr";
			}
			else{
				break_while = true;
			}
			i++;
		}
		
		String name = _displayed_input.replaceAll("@", "");
		String raw_input = _input;
		String user_name = "";
		
		if(raw_input.length() != 18 && raw_input.length() != 17){
			SqlConnect.SQLgetUser(name);
			if(SqlConnect.getUser_id() != 0){
				raw_input = ""+SqlConnect.getUser_id();
			}
			if(SqlConnect.getName() != null){
				user_name = SqlConnect.getName();
			}
		}
		else{
			SqlConnect.SQLgetUserThroughID(raw_input);
			if(SqlConnect.getName() != null){
				user_name = SqlConnect.getName();
			}
		}
		
		if(raw_input != null && (raw_input.length() == 18 || raw_input.length() == 17)) {
			if(user_name != null && user_name.length() > 0) {
				RankingDB.SQLgetWholeRankView(Long.parseLong(raw_input));
				_e.getTextChannel().sendMessage(message.setDescription("The user has been found in this guild! Now type one of the following words within 3 minutes to execute an action!\n\n"
						+ "**information**: To display all details of the selected user\n"
						+ "**delete-messages**: To remove up to 100 messages from the selected user\n"
						+ "**warning**: To change the current warning value\n"
						+ "**mute**: To assign the mute role\n"
						+ "**ban**: To ban the user\n"
						+ "**kick**: To kick the user\n"
						+ "**gift-experience**: To gift experience points\n"
						+ "**set-experience**: To set an experience value\n"
						+ "**set-level**: To assign a level\n"
						+ "**gift-currency**: To gift money\n"
						+ "**set-currency**: To se a money value").build()).queue();
				FileSetting.createFile(file_name, raw_input);
				new Thread(new DelayDelete(file_name, 180000)).start();
			}
			else {
				_e.getTextChannel().sendMessage(_e.getMember().getAsMention()+" Error, user doesn't exist. Please try again!").queue();
			}
		}
		else {
			_e.getTextChannel().sendMessage(_e.getMember().getAsMention()+" Error, user doesn't exist. Please try again!").queue();
		}
	}
	
	public static void performAction(MessageReceivedEvent _e, String _message, String _file_name) {
		EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Session Expired!");
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
		String file_path = _file_name;
		String file_value = FileSetting.readFile(file_path);
		if(!file_value.equals("complete")) {
			if(_message.equalsIgnoreCase("information") || _message.equalsIgnoreCase("delete-messages") || _message.equalsIgnoreCase("warning") || _message.equalsIgnoreCase("mute") || _message.equalsIgnoreCase("ban") || _message.equalsIgnoreCase("kick") || _message.equalsIgnoreCase("gift-experience") || _message.equalsIgnoreCase("set-experience") || _message.equalsIgnoreCase("set-level") || _message.equalsIgnoreCase("gift-currency") || _message.equalsIgnoreCase("set-currency")) {
				switch(_message) {
					case "information": 
						SqlConnect.SQLgetUserThroughID(file_value);
						message.setTitle("Here the requested information!");
						message.setThumbnail(SqlConnect.getAvatar());
						message.setAuthor(SqlConnect.getName());
						message.setDescription("Here you can inspect all current information for this user!");
						message.addBlankField(false);
						SqlConnect.SQLgetData(Long.parseLong(file_value), _e.getGuild().getIdLong());
						int warning_id = SqlConnect.getWarningID();
						SqlConnect.SQLgetMaxWarning(_e.getGuild().getIdLong());
						message.addField("CURRENT WARNING", "**"+warning_id+"**/**"+SqlConnect.getWarningID()+"**", true);
						SqlConnect.SQLgetSingleActionEventCount("MEMBER_MUTE_ADD", Long.parseLong(file_value), _e.getGuild().getIdLong());
						message.addField("TOTAL WARNINGS", "**"+SqlConnect.getCount()+"**", true);
						SqlConnect.SQLgetSingleActionEventCount("MEMBER_BAN_ADD", Long.parseLong(file_value), _e.getGuild().getIdLong());
						message.addField("TOTAL BANS", "**"+SqlConnect.getCount()+"**", true);
						message.addField("BANNED", SqlConnect.getBanID() == 2 ? "**YES**" : "**NO**", true);
						message.addField("JOIN DATE", "**"+SqlConnect.getJoinDate()+"**", true);
						message.addField("USER ID", "**"+file_value+"**", true);
						message.addBlankField(false);
						Rank user_details = Hashes.getRanking(Long.parseLong(file_value));
						Guilds guild_settings = Hashes.getStatus(_e.getGuild().getIdLong());
						if(guild_settings.getRankingState() == true) {
							message.addField("LEVEL", "**"+user_details.getLevel()+"**/**"+guild_settings.getMaxLevel()+"**", true);
							message.addField("EXPERIENCE", "**"+user_details.getCurrentExperience()+"**/**"+user_details.getRankUpExperience()+"**", true);
							if(user_details.getCurrentRole() != 0){
								message.addField("UNLOCKED ROLE", _e.getGuild().getRoleById(user_details.getCurrentRole()).getAsMention(), false);
							}
							else{
								message.addField("UNLOCKED ROLE", "**N/A**", false);
							}
						}
						StringBuilder out = new StringBuilder();
						SqlConnect.SQLgetDoubleActionEventDescriptions("MEMBER_NAME_UPDATE", "GUILD_MEMBER_JOIN", Long.parseLong(file_value), _e.getGuild().getIdLong());
						for(String description : SqlConnect.getDescriptions()) {
							out.append("[`"+description+"`] ");
						}
						out = out.toString().replaceAll("[\\s]*", "").length() == 0 ? out.append("**N/A**") : out;
						message.addField("USED NAMES", out.toString(), false);
						SqlConnect.clearDescriptions();
						out.setLength(0);
						SqlConnect.SQLgetSingleActionEventDescriptions("MEMBER_NICKNAME_UPDATE", Long.parseLong(file_value), _e.getGuild().getIdLong());
						for(String description : SqlConnect.getDescriptions()) {
							out.append("[`"+description+"`] ");
						}
						out = out.toString().replaceAll("[\\s]*", "").length() == 0 ? out.append("**N/A**") : out;
						message.addField("USED NICKNAMES", out.toString(), false);
						SqlConnect.clearDescriptions();
						out.setLength(0);
						_e.getTextChannel().sendMessage(message.build()).queue();
						message.clear();
						message.setColor(Color.BLUE).setTitle("EVENTS");
						SqlConnect.SQLgetCriticalActionEvents(Long.parseLong(file_value), _e.getGuild().getIdLong());
						for(String description : SqlConnect.getDescriptions()) {
							out.append(description+"\n");
						}
						message.setDescription(out);
						SqlConnect.clearDescriptions();
						out.setLength(0);
						_e.getTextChannel().sendMessage(message.build()).queue();
						FileSetting.deleteFile(file_path);
						break;
					case "delete-messages":
						message.setTitle("You chose to delete a number of messages!");
						_e.getTextChannel().sendMessage(message.setDescription("Please choose how many messages should be removed between 1 and 100!").build()).queue();
						FileSetting.createFile(file_path, "delete-messages"+file_value);
						break;
					case "warning":
						message.setTitle("You chose to set a warning value!");
						_e.getTextChannel().sendMessage(message.setDescription("Please choose a numerical warning value that is lower or identical to the max warning value!").build()).queue();
						FileSetting.createFile(file_path, "warning"+file_value);
						break;
					case "mute":
						message.setTitle("You chose to mute!");
						message.addField("YES", "Provide a mute time", true);
						message.addField("NO", "Don't provide a mute time", true);
						_e.getTextChannel().sendMessage(message.setDescription("Do you wish to provide a self chosen mute timer? By providing a self chosen mute timer, the warning value won't increment unless the user has been never warned before!").build()).queue();
						FileSetting.createFile(file_path, "mute"+file_value);
						break;
					case "ban":
						message.setTitle("You chose to ban!");
						message.addField("YES", "Provide a reason", true);
						message.addField("NO", "Don't provide a reason", true);
						_e.getTextChannel().sendMessage(message.setDescription("Do you wish to provide a reson to the ban?").build()).queue();
						FileSetting.createFile(file_path, "ban"+file_value);
						break;
					case "kick":
						message.setTitle("You chose to kick!");
						message.addField("YES", "Provide a reason", true);
						message.addField("NO", "Don't provide a reason", true);
						_e.getTextChannel().sendMessage(message.setDescription("Do you wish to provide a reson to the kick?").build()).queue();
						FileSetting.createFile(file_path, "kick"+file_value);
						break;
					case "gift-experience":
						if(Hashes.getStatus(_e.getGuild().getIdLong()).getRankingState()) {
							message.setTitle("You chose to gift experience points!");
							_e.getTextChannel().sendMessage(message.setDescription("Choose a number of experience points to add to the total number of experience points").build()).queue();
							FileSetting.createFile(file_path, "gift-experience"+file_value);
						}
						else {
							denied.setTitle("Access Denied!");
							_e.getTextChannel().sendMessage(denied.setDescription(_e.getMember().getAsMention()+" This action can't be used because the ranking system is disabled! Please choose a different action!").build()).queue();
						}
						break;
					case "set-experience":
						if(Hashes.getStatus(_e.getGuild().getIdLong()).getRankingState()) {
							message.setTitle("You chose to set experience points!");
							_e.getTextChannel().sendMessage(message.setDescription("Choose a number of experience points to set for the user").build()).queue();
							FileSetting.createFile(file_path, "set-experience"+file_value);
						}
						else {
							denied.setTitle("Access Denied!");
							_e.getTextChannel().sendMessage(denied.setDescription(_e.getMember().getAsMention()+" This action can't be used because the ranking system is disabled! Please choose a different action!").build()).queue();
						}
						break;
					case "set-level":
						if(Hashes.getStatus(_e.getGuild().getIdLong()).getRankingState()) {
							message.setTitle("You chose to set a level!");
							_e.getTextChannel().sendMessage(message.setDescription("Choose a level to assign the user").build()).queue();
							FileSetting.createFile(file_path, "set-level"+file_value);
						}
						else {
							denied.setTitle("Access Denied!");
							_e.getTextChannel().sendMessage(denied.setDescription(_e.getMember().getAsMention()+" This action can't be used because the ranking system is disabled! Please choose a different action!").build()).queue();
						}
						break;
					case "gift-currency":
						if(Hashes.getStatus(_e.getGuild().getIdLong()).getRankingState()) {
							message.setTitle("You chose to gift money!");
							_e.getTextChannel().sendMessage(message.setDescription("Choose the amount of money to gift the user").build()).queue();
							FileSetting.createFile(file_path, "gift-currency"+file_value);
						}
						else {
							denied.setTitle("Access Denied!");
							_e.getTextChannel().sendMessage(denied.setDescription(_e.getMember().getAsMention()+" This action can't be used because the ranking system is disabled! Please choose a different action!").build()).queue();
						}
						break;
					case "set-currency":
						if(Hashes.getStatus(_e.getGuild().getIdLong()).getRankingState()) {
							message.setTitle("You chose to set money!");
							_e.getTextChannel().sendMessage(message.setDescription("Choose the amount of money to set for the user").build()).queue();
							FileSetting.createFile(file_path, "set-currency"+file_value);
						}
						else {
							denied.setTitle("Access Denied!");
							_e.getTextChannel().sendMessage(denied.setDescription(_e.getMember().getAsMention()+" This action can't be used because the ranking system is disabled! Please choose a different action!").build()).queue();
						}
						break;
				}
			}
			else if(file_value.replaceAll("[0-9]*", "").equalsIgnoreCase("delete-messages")){
				if(_message.replaceAll("[0-9]*", "").length() == 0){
					EmbedBuilder error = new EmbedBuilder().setColor(Color.RED);
					int value = Integer.parseInt(_message);
					if(value == 0){
						_e.getTextChannel().sendMessage("You chose to not remove any messages at all!").queue();
					}
					else if(value > 100){
						_e.getTextChannel().sendMessage("Please choose a number between 1 and 100!").queue();
					}
					else{
						ArrayList<Messages> messages = new ArrayList<Messages>();
						for(Messages collectedMessage : Hashes.getWholeMessagePool().values()) {
							if(collectedMessage.getUserID() == Long.parseLong(file_value.replaceAll("[^0-9]*", "")) && collectedMessage.getGuildID() == _e.getGuild().getIdLong()) {
								messages.add(collectedMessage);
							}
						}
						
						int hash_counter = 0;
						StringBuilder collected_messages = new StringBuilder();
						for(int i = messages.size()-1; i >= 0; i--) {
							hash_counter++;
							try {
								Message m = _e.getGuild().getTextChannelById(messages.get(i).getChannelID()).getMessageById(messages.get(i).getMessageID()).complete();
								collected_messages.append("["+messages.get(i).getTime().toString()+"]: "+messages.get(i).getMessage());
								Hashes.removeMessagePool(messages.get(i).getMessageID());
								m.delete().queue();
								if(i == 0 || hash_counter == value) {
									break;
								}
							}catch(InsufficientPermissionException ipe) {
								error.setTitle("Message couldn't be removed");
								_e.getTextChannel().sendMessage(error.setDescription("Message couldn't be removed from <#"+messages.get(i).getChannelID()+"> due to lack of permissions: **"+ipe.getPermission().getName()+"**").build()).queue();
								hash_counter--;
							}
						}
						
						if(messages.size() > 0) {
							String paste_link = Pastebin.unlistedPaste("Found comments have been succesfully removed!", hash_counter+" messages from "+messages.get(0).getUserName()+" have been removed:\n\n"+collected_messages.toString());
							if(!paste_link.equals("Creating paste failed!")) {
								_e.getTextChannel().sendMessage(message.setDescription("The comments of the selected user have been succesfully removed: "+paste_link).build()).queue();
							}
							else {
								error.setTitle("New Paste couldn't be created!");
								_e.getTextChannel().sendMessage(error.setDescription("A new Paste couldn't be created. Please ensure that valid login credentials and a valid Pastebing API key has been inserted into the config.ini file!").build()).queue();
							}
						}
						else {
							_e.getTextChannel().sendMessage(message.setDescription("Nothing has been found to delete....").build()).queue();
						}
					}
				}
			}
			else if(file_value.replaceAll("[0-9]*", "").equalsIgnoreCase("warning")) {
				if(_message.replaceAll("[0-9]*", "").length() == 0) {
					SqlConnect.SQLgetData(Long.parseLong(file_value.replaceAll("[^0-9]*", "")), _e.getGuild().getIdLong());
					int db_warning = SqlConnect.getWarningID();
					if(db_warning != 0) {
						SqlConnect.SQLgetMaxWarning(_e.getGuild().getIdLong());
						int warning_id = Integer.parseInt(_message.replaceAll("[^0-9]*", ""));
						int max_warning_id = SqlConnect.getWarningID();
						if(warning_id == 0){
							SqlConnect.SQLDeleteData(Long.parseLong(file_value.replaceAll("[^0-9]",  "")), _e.getGuild().getIdLong());
							_e.getTextChannel().sendMessage("The warnings of this user has been cleared!").queue();
						}
						else if(warning_id <= max_warning_id) {
							SqlConnect.SQLUpdateWarning(Long.parseLong(file_value.replaceAll("[^0-9]", "")), _e.getGuild().getIdLong(), warning_id);
							_e.getTextChannel().sendMessage("Warning value "+warning_id+" has been set!").queue();
						}
						else {
							SqlConnect.SQLUpdateWarning(Long.parseLong(file_value.replaceAll("[^0-9]", "")), _e.getGuild().getIdLong(), max_warning_id);
							_e.getTextChannel().sendMessage("The max possible value "+max_warning_id+" has been set because your input exceeded the max possible warning!").queue();
						}
					}
					else {
						_e.getTextChannel().sendMessage("A custom warning can't be set because the player was never muted or was freshly unbanned!").queue();
					}
					FileSetting.deleteFile(file_path);
				}
			}
			else if(file_value.replaceAll("[0-9]*", "").equalsIgnoreCase("mute")) {
				if(_message.equalsIgnoreCase("yes")) {
					message.setTitle("You chose to provide a mute time!");
					_e.getTextChannel().sendMessage(message.setDescription("Please provide a mute time in the following format:\n\n"
							+ "to set the time in minutes: eg. **1m**\n"
							+ "to set the time in hours: eg.**1h**\n"
							+ "to set the time in days: eg. **1d**").build()).queue();
					FileSetting.createFile(file_path, "mute-time"+file_value.replaceAll("[^0-9]*", ""));
				}
				else if(_message.equalsIgnoreCase("no")) {
					ServerRoles.SQLgetRole(_e.getGuild().getIdLong(), "mut");
					_e.getGuild().getController().addSingleRoleToMember(_e.getGuild().getMemberById(file_value.replaceAll("[^0-9]*", "")), _e.getGuild().getRoleById(ServerRoles.getRole_ID())).queue();
					FileSetting.deleteFile(file_path);
					ServerRoles.clearAllVariables();
				}
			}
			else if(file_value.replaceAll("[0-9]*", "").equalsIgnoreCase("mute-time")) {
				if(_message.replaceAll("[0-9]*", "").length() == 1 && (_message.endsWith("m") || _message.endsWith("h") || _message.endsWith("d"))) {					
					long mute_time = (Long.parseLong(_message.replaceAll("[^0-9]*", ""))*1000);
					if(_message.endsWith("m")) {
						mute_time *= 60;
					}
					else if(_message.endsWith("h")) {
						mute_time = mute_time*60*60;
					}
					else if(_message.endsWith("d")) {
						mute_time = mute_time*60*60*24;
					}
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					Timestamp unmute_timestamp = new Timestamp(System.currentTimeMillis()+mute_time);
					
					ServerRoles.SQLgetRole(_e.getGuild().getIdLong(), "mut");
					_e.getGuild().getController().addSingleRoleToMember(_e.getGuild().getMemberById(file_value.replaceAll("[^0-9]*", "")), _e.getGuild().getRoleById(ServerRoles.getRole_ID())).queue();
					SqlConnect.SQLgetData(Long.parseLong(file_value.replaceAll("[^0-9]*", "")), _e.getGuild().getIdLong());
					if(SqlConnect.getWarningID() != 0) {
						SqlConnect.SQLUpdateUnmute(Long.parseLong(file_value.replaceAll("[^0-9]*", "")), _e.getGuild().getIdLong(), timestamp, unmute_timestamp, false, true);
					}
					else {
						SqlConnect.SQLInsertData(Long.parseLong(file_value.replaceAll("[^0-9]*", "")), _e.getGuild().getIdLong(), 1, 1, timestamp, unmute_timestamp, false, true);
					}
					FileSetting.deleteFile(file_path);
					FileSetting.createFile(IniFileReader.getTempDirectory()+"AutoDelFiles/mute_time_"+file_value.replaceAll("[^0-9]*", ""), ""+mute_time);
					ServerRoles.clearAllVariables();
					SqlConnect.clearAllVariables();
					SqlConnect.clearUnmute();
					SqlConnect.clearTimestamp();
				}
				else {
					_e.getTextChannel().sendMessage(_e.getMember().getAsMention()+" Please type a numerical value in minutes!").queue();
				}
			}
			else if(file_value.replaceAll("[0-9]*", "").equalsIgnoreCase("ban")) {
				if(_message.equalsIgnoreCase("yes")) {
					message.setTitle("You chose to provide a reason!");
					_e.getTextChannel().sendMessage(message.setDescription("Please provide a reason!").build()).queue();
					FileSetting.createFile(file_path, "ban-reason"+file_value.replaceAll("[^0-9]*", ""));
				}
				else if(_message.equalsIgnoreCase("no")) {
					SqlConnect.SQLgetData(Long.parseLong(file_value.replaceAll("[^0-9]*", "")), _e.getGuild().getIdLong());
					int warning_id = SqlConnect.getWarningID();
					SqlConnect.SQLgetMaxWarning(_e.getGuild().getIdLong());
					int max_warning_id = SqlConnect.getWarningID();
					if(warning_id == max_warning_id) {
						Timestamp timestamp = new Timestamp(System.currentTimeMillis());
						denied.setThumbnail(IniFileReader.getBanThumbnail()).setTitle("User Banned!");
						SqlConnect.SQLgetChannelID(_e.getGuild().getIdLong(), "log");
						if(SqlConnect.getChannelID() != 0) {_e.getGuild().getTextChannelById(SqlConnect.getChannelID()).sendMessage(denied.setDescription("["+timestamp.toString()+"] **" + _e.getGuild().getMemberById(file_value.replaceAll("[^0-9]*", "")).getUser().getName()+"#"+_e.getGuild().getMemberById(file_value.replaceAll("[^0-9]*", "")).getUser().getDiscriminator() + " with the ID Number " + file_value.replaceAll("[^0-9]*", "") + " Has been banned after reaching the limit of allowed mutes on this server!**\nReason: User has been banned with the bot command!").build()).queue();}
						PrivateChannel pc = _e.getGuild().getMemberById(file_value.replaceAll("[^0-9]*", "")).getUser().openPrivateChannel().complete();
						pc.sendMessage("You have been banned from "+_e.getGuild().getName()+", since you have exceeded the max amount of allowed mutes on this server. Thank you for your understanding.\n"
								+ "On a important note, this is an automatic reply. You'll receive no reply in any way.").queue();
						pc.close();
					}
					_e.getGuild().getController().ban(_e.getGuild().getMemberById(file_value.replaceAll("[^0-9]*", "")), 0).reason("User has been banned with the bot command!").queue();
					FileSetting.deleteFile(file_path);
				}
			}
			else if(file_value.replaceAll("[0-9]*", "").equalsIgnoreCase("ban-reason")) {
				SqlConnect.SQLgetData(Long.parseLong(file_value.replaceAll("[^0-9]*", "")), _e.getGuild().getIdLong());
				int warning_id = SqlConnect.getWarningID();
				SqlConnect.SQLgetMaxWarning(_e.getGuild().getIdLong());
				int max_warning_id = SqlConnect.getWarningID();
				if(warning_id == max_warning_id) {
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					denied.setThumbnail(IniFileReader.getBanThumbnail()).setTitle("User Banned!");
					SqlConnect.SQLgetChannelID(_e.getGuild().getIdLong(), "log");
					if(SqlConnect.getChannelID() != 0) {_e.getGuild().getTextChannelById(SqlConnect.getChannelID()).sendMessage(denied.setDescription("["+timestamp.toString()+"] **" + _e.getGuild().getMemberById(file_value.replaceAll("[^0-9]*", "")).getUser().getName()+"#"+_e.getGuild().getMemberById(file_value.replaceAll("[^0-9]*", "")).getUser().getDiscriminator() + " with the ID Number " + file_value.replaceAll("[^0-9]*", "") + " Has been banned after reaching the limit of allowed mutes on this server!**\nReason: "+_message).build()).queue();}
					PrivateChannel pc = _e.getGuild().getMemberById(file_value.replaceAll("[^0-9]*", "")).getUser().openPrivateChannel().complete();
					pc.sendMessage("You have been banned from "+_e.getGuild().getName()+", since you have exceeded the max amount of allowed mutes on this server. Thank you for your understanding.\n"
							+ "On a important note, this is an automatic reply. You'll receive no reply in any way.").queue();
					pc.close();
				}
				_e.getGuild().getController().ban(_e.getGuild().getMemberById(file_value.replaceAll("[^0-9]*", "")), 0).reason(_message).queue();
				FileSetting.deleteFile(file_path);
			}
			else if(file_value.replaceAll("[0-9]*", "").equalsIgnoreCase("kick")) {
				if(_message.equalsIgnoreCase("yes")) {
					message.setTitle("You chose to provide a reason!");
					_e.getTextChannel().sendMessage(message.setDescription("Please provide a reason!").build()).queue();
					FileSetting.createFile(file_path, "kick-reason"+file_value.replaceAll("[^0-9]*", ""));
				}
				else if(_message.equalsIgnoreCase("no")) {
					_e.getGuild().getController().kick(_e.getGuild().getMemberById(file_value.replaceAll("[^0-9]*", ""))).reason("User has been kicked with the bot command!").queue();
					FileSetting.deleteFile(file_path);
				}
			}
			else if(file_value.replaceAll("[0-9]*", "").equalsIgnoreCase("kick-reason")) {
				_e.getGuild().getController().kick(_e.getGuild().getMemberById(file_value.replaceAll("[^0-9]*", ""))).reason(_message).queue();
				FileSetting.deleteFile(file_path);
			}
			else if(file_value.replaceAll("[0-9]*",	"").equalsIgnoreCase("gift-experience")) {
				if(_message.replaceAll("[0-9]*", "").length() == 0) {
					Rank user_details = Hashes.getRanking(Long.parseLong(file_value.replaceAll("[^0-9]*", "")));
					long experience = Integer.parseInt(_message);
					long totExperience = 0;
					long currentExperience = 0;
					long rankUpExperience = 0;
					int level = 0;
					long assign_role = 0;
					boolean toBreak = false;
					for(Ranks ranks : Hashes.getMapOfRankingLevels().values()){
						if((user_details.getExperience() + experience) >= ranks.getExperience()){
							totExperience = ranks.getExperience();
							level = ranks.getLevel();
							currentExperience = (user_details.getExperience() + experience) - ranks.getExperience();
							if(ranks.getAssignRole() != 0){
								assign_role = ranks.getAssignRole();
							}
						}
						else{
							if(toBreak == false) {
								rankUpExperience = ranks.getExperience() - totExperience;
								toBreak = true;
							}
							else
								break;
						}
					}
					user_details.setExperience(user_details.getExperience()+experience);
					user_details.setCurrentExperience((int) currentExperience);
					user_details.setRankUpExperience((int) rankUpExperience);
					user_details.setLevel(level);
					user_details.setCurrentRole(assign_role);
					RankingDB.SQLsetLevelUp(user_details.getUser_ID(), user_details.getLevel(), user_details.getExperience(), user_details.getCurrency(), user_details.getCurrentRole());
					RankingDB.SQLInsertActionLog("low", user_details.getUser_ID(), "Experience points gifted", "User received "+experience+" experience points");
					Hashes.addRanking(user_details.getUser_ID(), user_details);
					for(Role r : _e.getMember().getRoles()){
						for(Rank role : Hashes.getMapOfRankingRoles().values()){
							if(r.getIdLong() == role.getRoleID() && role.getGuildID() == _e.getGuild().getIdLong()){
								_e.getGuild().getController().removeSingleRoleFromMember(_e.getGuild().getMemberById(user_details.getUser_ID()), _e.getGuild().getRoleById(r.getIdLong())).queue();
							}
						}
					}
					if(assign_role != 0){
						_e.getGuild().getController().addSingleRoleToMember(_e.getGuild().getMemberById(user_details.getUser_ID()), _e.getGuild().getRoleById(assign_role)).queue();
					}
					_e.getTextChannel().sendMessage("Experience points have been updated!").queue();
					FileSetting.deleteFile(file_path);
				}
			}
			else if(file_value.replaceAll("[0-9]*", "").equalsIgnoreCase("set-experience")) {
				if(_message.replaceAll("[0-9]*", "").length() == 0) {
					Rank user_details = Hashes.getRanking(Long.parseLong(file_value.replaceAll("[^0-9]*", "")));
					long experience = Long.parseLong(_message);
					long totExperience = 0;
					long currentExperience = 0;
					long rankUpExperience = 0;
					int level = 0;
					long assign_role = 0;
					boolean toBreak = false;
					for(Ranks ranks : Hashes.getMapOfRankingLevels().values()){
						if(experience >= ranks.getExperience()){
							totExperience = ranks.getExperience();
							level = ranks.getLevel();
							currentExperience = experience - ranks.getExperience();
							if(ranks.getAssignRole() != 0){
								assign_role = ranks.getAssignRole();
							}
						}
						else{
							if(toBreak == false) {
								rankUpExperience = ranks.getExperience() - totExperience;
								toBreak = true;
							}
							else
								break;
						}
					}
					user_details.setExperience(experience);
					user_details.setCurrentExperience((int) currentExperience);
					user_details.setRankUpExperience((int) rankUpExperience);
					user_details.setLevel(level);
					user_details.setCurrentRole(assign_role);
					RankingDB.SQLsetLevelUp(user_details.getUser_ID(), user_details.getLevel(), user_details.getExperience(), user_details.getCurrency(), user_details.getCurrentRole());
					RankingDB.SQLInsertActionLog("low", user_details.getUser_ID(), "Experience points edited", "User has been set to "+experience+" experience points");
					Hashes.addRanking(user_details.getUser_ID(), user_details);
					for(Role r : _e.getMember().getRoles()){
						for(Rank role : Hashes.getMapOfRankingRoles().values()){
							if(r.getIdLong() == role.getRoleID() && role.getGuildID() == _e.getGuild().getIdLong()){
								_e.getGuild().getController().removeSingleRoleFromMember(_e.getGuild().getMemberById(user_details.getUser_ID()), _e.getGuild().getRoleById(r.getIdLong())).queue();
							}
						}
					}
					if(assign_role != 0){
						_e.getGuild().getController().addSingleRoleToMember(_e.getGuild().getMemberById(user_details.getUser_ID()), _e.getGuild().getRoleById(assign_role)).queue();
					}
					_e.getTextChannel().sendMessage("Experience points have been updated!").queue();
					FileSetting.deleteFile(file_path);
				}
			}
			else if(file_value.replaceAll("[0-9]*", "").equalsIgnoreCase("set-level")) {
				if(_message.replaceAll("[0-9]*", "").length() == 0) {
					int level = Integer.parseInt(_message);
					if(level <= Hashes.getStatus(_e.getGuild().getIdLong()).getMaxLevel()) {
						Rank user_details = Hashes.getRanking(Long.parseLong(file_value.replaceAll("[^0-9]*", "")));
						long experience = 0;
						long rankUpExperience = 0;
						long assign_role = 0;
						boolean toBreak = false;
						for(Ranks ranks : Hashes.getMapOfRankingLevels().values()){
							if(ranks.getAssignRole() != 0 && toBreak == false){
								assign_role = ranks.getAssignRole();
							}
							if(level == ranks.getLevel()){
								experience = ranks.getExperience();
								toBreak = true;
							}
							else if(toBreak == true){
								rankUpExperience = ranks.getExperience() - experience;
								break;
							}
						}
						user_details.setLevel(level);
						user_details.setExperience(experience);
						user_details.setCurrentExperience(0);
						user_details.setRankUpExperience((int) rankUpExperience);
						user_details.setCurrentRole(assign_role);
						RankingDB.SQLsetLevelUp(user_details.getUser_ID(), user_details.getLevel(), user_details.getExperience(), user_details.getCurrency(), user_details.getCurrentRole());
						RankingDB.SQLInsertActionLog("low", user_details.getUser_ID(), "Level changed", "User is now level "+user_details.getLevel());
						Hashes.addRanking(user_details.getUser_ID(), user_details);
						for(Role r : _e.getMember().getRoles()){
							for(Rank role : Hashes.getMapOfRankingRoles().values()){
								if(r.getIdLong() == role.getRoleID() && role.getGuildID() == _e.getGuild().getIdLong()){
									_e.getGuild().getController().removeSingleRoleFromMember(_e.getGuild().getMemberById(user_details.getUser_ID()), _e.getGuild().getRoleById(r.getIdLong())).queue();
								}
							}
						}
						if(assign_role != 0){
							_e.getGuild().getController().addSingleRoleToMember(_e.getGuild().getMemberById(user_details.getUser_ID()), _e.getGuild().getRoleById(assign_role)).queue();
						}
						_e.getTextChannel().sendMessage("The level has been updated!").queue();
						FileSetting.deleteFile(file_path);
					}
					else {
						_e.getTextChannel().sendMessage(_e.getMember().getAsMention()+" Please choose a level that is lower or equal to "+Hashes.getStatus(_e.getGuild().getIdLong()).getMaxLevel()).queue();
					}
				}
			}
			else if(file_value.replaceAll("[0-9]*", "").equalsIgnoreCase("gift-currency")) {
				if(_message.replaceAll("[0-9]*", "").length() == 0) {
					Rank user_details = Hashes.getRanking(Long.parseLong(file_value.replaceAll("[^0-9]*", "")));
					long currency = Long.parseLong(_message);
					user_details.setCurrency(user_details.getCurrency()+currency);
					RankingDB.SQLUpdateCurrency(user_details.getUser_ID(), user_details.getCurrency());
					RankingDB.SQLInsertActionLog("low", user_details.getUser_ID(), "Money gifted", "User received money in value of "+currency+" PEN");
					Hashes.addRanking(user_details.getUser_ID(), user_details);
					_e.getTextChannel().sendMessage("Currency has been updated!").queue();
					FileSetting.deleteFile(file_path);
				}
			}
			else if(file_value.replaceAll("[0-9]*", "").equalsIgnoreCase("set-currency")) {
				if(_message.replaceAll("[0-9]*", "").length() == 0) {
					Rank user_details = Hashes.getRanking(Long.parseLong(file_value.replaceAll("[^0-9]*", "")));
					long currency = Long.parseLong(_message);
					user_details.setCurrency(currency);
					RankingDB.SQLUpdateCurrency(user_details.getUser_ID(), user_details.getCurrency());
					RankingDB.SQLInsertActionLog("low", user_details.getUser_ID(), "Money set", "Currency value for the user has been changed to "+currency+" PEN");
					Hashes.addRanking(user_details.getUser_ID(), user_details);
					_e.getTextChannel().sendMessage("Currency has been updated!").queue();
					FileSetting.deleteFile(file_path);
				}
			}
		}
		else {
			_e.getTextChannel().sendMessage(denied.setDescription("Session has expired! Please retype the command!").build()).queue();
			FileSetting.deleteFile(IniFileReader.getTempDirectory()+"AutoDelFiles/user_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId()+".azr");
		}
	}
}
