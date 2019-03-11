package commandsContainer;

import java.awt.Color;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Bancollect;
import core.Guilds;
import core.Hashes;
import core.Messages;
import core.User;
import fileManagement.FileSetting;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import rankingSystem.Rank;
import rankingSystem.Ranks;
import sql.RankingSystem;
import sql.DiscordRoles;
import sql.Azrael;
import threads.DelayDelete;
import util.Pastebin;

public class UserExecution {
	private static final Logger logger = LoggerFactory.getLogger(UserExecution.class);
	
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
			User user = Azrael.SQLgetUser(name);
			if(user.getUserID() != 0) {
				raw_input = ""+user.getUserID();
				user_name = user.getUserName();
			}
		}
		else{
			user_name = Azrael.SQLgetUserThroughID(raw_input).getUserName();
		}
		
		if(raw_input != null && (raw_input.length() == 18 || raw_input.length() == 17)) {
			if(user_name != null && user_name.length() > 0) {
				RankingSystem.SQLgetWholeRankView(Long.parseLong(raw_input), _e.getGuild().getIdLong());
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
						User user = Azrael.SQLgetUserThroughID(file_value);
						message.setTitle("Here the requested information!");
						if(user.getAvatar() != null)
							message.setThumbnail(user.getAvatar());
						message.setAuthor(user.getUserName());
						message.setDescription("Here you can inspect all current information for this user!");
						message.addBlankField(false);
						Bancollect warnedUser = Azrael.SQLgetData(Long.parseLong(file_value), _e.getGuild().getIdLong());
						message.addField("CURRENT WARNING", "**"+warnedUser.getWarningID()+"**/**"+Azrael.SQLgetMaxWarning(_e.getGuild().getIdLong())+"**", true);
						message.addField("TOTAL WARNINGS", "**"+Azrael.SQLgetSingleActionEventCount("MEMBER_MUTE_ADD", Long.parseLong(file_value), _e.getGuild().getIdLong())+"**", true);
						message.addField("TOTAL BANS", "**"+Azrael.SQLgetSingleActionEventCount("MEMBER_BAN_ADD", Long.parseLong(file_value), _e.getGuild().getIdLong())+"**", true);
						message.addField("BANNED", warnedUser.getBanID() == 2 ? "**YES**" : "**NO**", true);
						message.addField("JOIN DATE", "**"+user.getJoinDate()+"**", true);
						message.addField("USER ID", "**"+file_value+"**", true);
						message.addBlankField(false);
						Rank user_details = Hashes.getRanking(_e.getGuild().getId()+"_"+file_value);
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
						for(String description : Azrael.SQLgetDoubleActionEventDescriptions("MEMBER_NAME_UPDATE", "GUILD_MEMBER_JOIN", Long.parseLong(file_value), _e.getGuild().getIdLong())) {
							out.append("[`"+description+"`] ");
						}
						out = out.toString().replaceAll("[\\s]*", "").length() == 0 ? out.append("**N/A**") : out;
						message.addField("USED NAMES", out.toString(), false);
						out.setLength(0);
						for(String description : Azrael.SQLgetSingleActionEventDescriptions("MEMBER_NICKNAME_UPDATE", Long.parseLong(file_value), _e.getGuild().getIdLong())) {
							out.append("[`"+description+"`] ");
						}
						out = out.toString().replaceAll("[\\s]*", "").length() == 0 ? out.append("**N/A**") : out;
						message.addField("USED NICKNAMES", out.toString(), false);
						out.setLength(0);
						_e.getTextChannel().sendMessage(message.build()).queue();
						message.clear();
						message.setColor(Color.BLUE).setTitle("EVENTS");
						for(String description : Azrael.SQLgetCriticalActionEvents(Long.parseLong(file_value), _e.getGuild().getIdLong())) {
							out.append(description+"\n");
						}
						message.setDescription(out);
						out.setLength(0);
						_e.getTextChannel().sendMessage(message.build()).queue();
						logger.debug("{} has displayed information of the user {}", _e.getMember().getUser().getId(), file_value);
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
							String paste_link = Pastebin.unlistedPaste("Found comments have been succesfully removed!", hash_counter+" messages from "+messages.get(0).getUserName()+" have been removed:\n\n"+collected_messages.toString(), _e.getGuild().getIdLong());
							if(!paste_link.equals("Creating paste failed!")) {
								_e.getTextChannel().sendMessage(message.setDescription("The comments of the selected user have been succesfully removed: "+paste_link).build()).queue();
								logger.debug("{} has bulk deleted messages from {}", _e.getMember().getUser().getId(), messages.get(0).getUserID());
							}
							else {
								error.setTitle("New Paste couldn't be created!");
								_e.getTextChannel().sendMessage(error.setDescription("A new Paste couldn't be created. Please ensure that valid login credentials and a valid Pastebing API key has been inserted into the config.ini file!").build()).queue();
								logger.error("New paste couldn't be created. Missing login information");
							}
						}
						else {
							_e.getTextChannel().sendMessage(message.setDescription("Nothing has been found to delete....\nPlease check the config file if the Bot is allowed to cache messages").build()).queue();
							logger.warn("No message deleted");
						}
					}
				}
			}
			else if(file_value.replaceAll("[0-9]*", "").equalsIgnoreCase("warning")) {
				if(_message.replaceAll("[0-9]*", "").length() == 0) {
					int db_warning = Azrael.SQLgetData(Long.parseLong(file_value.replaceAll("[^0-9]*", "")), _e.getGuild().getIdLong()).getWarningID();
					if(db_warning != 0) {
						int warning_id = Integer.parseInt(_message.replaceAll("[^0-9]*", ""));
						int max_warning_id = Azrael.SQLgetMaxWarning(_e.getGuild().getIdLong());
						if(warning_id == 0){
							if(Azrael.SQLDeleteData(Long.parseLong(file_value.replaceAll("[^0-9]",  "")), _e.getGuild().getIdLong()) > 0) {
								_e.getTextChannel().sendMessage("The warnings of this user has been cleared!").queue();
								logger.debug("{} has cleared the warnings from {} in guild {}", _e.getMember().getUser().getId(), file_value.replaceAll("[^0-9]",  ""), _e.getGuild().getName());
							}
							else {
								_e.getTextChannel().sendMessage("An internal error occurred. The warnings of this user couldn't be cleared from Azrael.bancollect").queue();
								logger.error("The warnings of the user {} in guild {} couldn't be cleared", file_value.replaceAll("[^0-9]",  ""), _e.getGuild().getName());
							}
						}
						else if(warning_id <= max_warning_id) {
							if(Azrael.SQLUpdateWarning(Long.parseLong(file_value.replaceAll("[^0-9]", "")), _e.getGuild().getIdLong(), warning_id) > 0) {
								_e.getTextChannel().sendMessage("Warning value "+warning_id+" has been set!").queue();
								logger.debug("{} has set the warning level to {} from {} in guild {}", _e.getMember().getUser().getId(), warning_id, file_value.replaceAll("[^0-9]",  ""), _e.getGuild().getName());
							}
							else {
								_e.getTextChannel().sendMessage("An internal error occurred. The warning level of the selected user couldn't be updated on Azrael.bancollect").queue();
								logger.error("Warning on user {} couldn't be updated on Azrael.bancollect in guild {}", file_value.replaceAll("[^0-9]",  ""), _e.getGuild().getName());
							}
						}
						else {
							if(Azrael.SQLUpdateWarning(Long.parseLong(file_value.replaceAll("[^0-9]", "")), _e.getGuild().getIdLong(), max_warning_id) > 0) {
								_e.getTextChannel().sendMessage("The max possible value "+max_warning_id+" has been set because your input exceeded the max possible warning!").queue();
								logger.debug("{} has set the warning level to {} from {} in guild {}", _e.getMember().getUser().getId(), max_warning_id, file_value.replaceAll("[^0-9]",  ""), _e.getGuild().getName());
							}
							else {
								_e.getTextChannel().sendMessage("An internal error occurred. The warning level of the selected user couldn't be updated on Azrael.bancollect").queue();
								logger.error("Warning on user {} couldn't be updated on Azrael.bancollect in guild {}", file_value.replaceAll("[^0-9]",  ""), _e.getGuild().getName());
							}
						}
					}
					else {
						_e.getTextChannel().sendMessage("A custom warning can't be set because the player was never muted or was freshly unbanned!").queue();
						logger.warn("{} got no available warnings to be edited", file_value.replaceAll("[^0-9]",  ""));
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
					_e.getGuild().getController().addSingleRoleToMember(_e.getGuild().getMemberById(file_value.replaceAll("[^0-9]*", "")), _e.getGuild().getRoleById(DiscordRoles.SQLgetRole(_e.getGuild().getIdLong(), "mut"))).queue();
					_e.getTextChannel().sendMessage(message.setDescription("Mute order has been issued!").build()).queue();
					FileSetting.deleteFile(file_path);
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
					
					_e.getGuild().getController().addSingleRoleToMember(_e.getGuild().getMemberById(file_value.replaceAll("[^0-9]*", "")), _e.getGuild().getRoleById(DiscordRoles.SQLgetRole(_e.getGuild().getIdLong(), "mut"))).queue();
					if(Azrael.SQLgetData(Long.parseLong(file_value.replaceAll("[^0-9]*", "")), _e.getGuild().getIdLong()).getWarningID() != 0) {
						if(Azrael.SQLUpdateUnmute(Long.parseLong(file_value.replaceAll("[^0-9]*", "")), _e.getGuild().getIdLong(), timestamp, unmute_timestamp, false, true) == 0) {
							logger.error("The unmute timer couldn't be updated from user {} in guild {} for the table Azrael.bancollect", file_value.replaceAll("[^0-9]*", ""), _e.getGuild().getName());
							_e.getTextChannel().sendMessage("An internal error occurred. The unmute time couldn't be updated on Azrael.bancollect").queue();
						}
					}
					else {
						if(Azrael.SQLInsertData(Long.parseLong(file_value.replaceAll("[^0-9]*", "")), _e.getGuild().getIdLong(), 1, 1, timestamp, unmute_timestamp, false, true) == 0) {
							logger.error("muted user {} couldn't be inserted into Azrael.bancollect for guild {}", file_value.replaceAll("[^0-9]",  ""), _e.getGuild().getName());
							_e.getTextChannel().sendMessage("An internal error occurred. Muted user couldn't be inserted into Azrael.bancollect").queue();
						}
					}
					_e.getTextChannel().sendMessage(message.setDescription("Mute order has been issued!").build()).queue();
					logger.debug("{} has muted {} in guild {}", _e.getMember().getUser().getId(), file_value.replaceAll("[^0-9]",  ""), _e.getGuild().getName());
					FileSetting.deleteFile(file_path);
					FileSetting.createFile(IniFileReader.getTempDirectory()+"AutoDelFiles/mute_time_"+file_value.replaceAll("[^0-9]*", ""), ""+mute_time);
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
					int warning_id = Azrael.SQLgetData(Long.parseLong(file_value.replaceAll("[^0-9]*", "")), _e.getGuild().getIdLong()).getWarningID();
					int max_warning_id = Azrael.SQLgetMaxWarning(_e.getGuild().getIdLong());
					if(warning_id == max_warning_id) {
						Timestamp timestamp = new Timestamp(System.currentTimeMillis());
						denied.setThumbnail(IniFileReader.getBanThumbnail()).setTitle("User Banned!");
						var log_channel = Azrael.SQLgetChannelID(_e.getGuild().getIdLong(), "log");
						if(log_channel != 0) {_e.getGuild().getTextChannelById(log_channel).sendMessage(denied.setDescription("["+timestamp.toString()+"] **" + _e.getGuild().getMemberById(file_value.replaceAll("[^0-9]*", "")).getUser().getName()+"#"+_e.getGuild().getMemberById(file_value.replaceAll("[^0-9]*", "")).getUser().getDiscriminator() + " with the ID Number " + file_value.replaceAll("[^0-9]*", "") + " Has been banned after reaching the limit of allowed mutes on this server!**\nReason: User has been banned with the bot command!").build()).queue();}
						PrivateChannel pc = _e.getGuild().getMemberById(file_value.replaceAll("[^0-9]*", "")).getUser().openPrivateChannel().complete();
						pc.sendMessage("You have been banned from "+_e.getGuild().getName()+", since you have exceeded the max amount of allowed mutes on this server. Thank you for your understanding.\n"
								+ "On a important note, this is an automatic reply. You'll receive no reply in any way.").queue();
						pc.close();
						logger.debug("{} has banned {} from guild {}", _e.getMember().getUser().getId(), file_value.replaceAll("[^0-9]",  ""), _e.getGuild().getName());
					}
					_e.getTextChannel().sendMessage(message.setDescription("Ban order has been issued!").build()).queue();
					_e.getGuild().getController().ban(_e.getGuild().getMemberById(file_value.replaceAll("[^0-9]*", "")), 0).reason("User has been banned with the bot command!").queue();
					FileSetting.deleteFile(file_path);
				}
			}
			else if(file_value.replaceAll("[0-9]*", "").equalsIgnoreCase("ban-reason")) {
				int warning_id = Azrael.SQLgetData(Long.parseLong(file_value.replaceAll("[^0-9]*", "")), _e.getGuild().getIdLong()).getWarningID();
				int max_warning_id = Azrael.SQLgetMaxWarning(_e.getGuild().getIdLong());
				if(warning_id == max_warning_id) {
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					denied.setThumbnail(IniFileReader.getBanThumbnail()).setTitle("User Banned!");
					var log_channel = Azrael.SQLgetChannelID(_e.getGuild().getIdLong(), "log");
					if(log_channel != 0) {_e.getGuild().getTextChannelById(log_channel).sendMessage(denied.setDescription("["+timestamp.toString()+"] **" + _e.getGuild().getMemberById(file_value.replaceAll("[^0-9]*", "")).getUser().getName()+"#"+_e.getGuild().getMemberById(file_value.replaceAll("[^0-9]*", "")).getUser().getDiscriminator() + " with the ID Number " + file_value.replaceAll("[^0-9]*", "") + " Has been banned after reaching the limit of allowed mutes on this server!**\nReason: "+_message).build()).queue();}
					PrivateChannel pc = _e.getGuild().getMemberById(file_value.replaceAll("[^0-9]*", "")).getUser().openPrivateChannel().complete();
					pc.sendMessage("You have been banned from "+_e.getGuild().getName()+", since you have exceeded the max amount of allowed mutes on this server. Thank you for your understanding.\n"
							+ "On a important note, this is an automatic reply. You'll receive no reply in any way.").queue();
					pc.close();
					_e.getTextChannel().sendMessage(message.setDescription("Ban order has been issued!").build()).queue();
					logger.debug("{} has banned {} in guild {}", _e.getMember().getUser().getId(), file_value.replaceAll("[^0-9]",  ""), _e.getGuild().getName());
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
					_e.getTextChannel().sendMessage(message.setDescription("Kick order has been issued!").build()).queue();
					_e.getGuild().getController().kick(_e.getGuild().getMemberById(file_value.replaceAll("[^0-9]*", ""))).reason("User has been kicked with the bot command!").queue();
					logger.debug("{} has kicked {} from guild {}", _e.getMember().getUser().getId(), file_value.replaceAll("[^0-9]",  ""), _e.getGuild().getName());
					FileSetting.deleteFile(file_path);
				}
			}
			else if(file_value.replaceAll("[0-9]*", "").equalsIgnoreCase("kick-reason")) {
				_e.getTextChannel().sendMessage(message.setDescription("Kick order has been issued!").build()).queue();
				_e.getGuild().getController().kick(_e.getGuild().getMemberById(file_value.replaceAll("[^0-9]*", ""))).reason(_message).queue();
				logger.debug("{} has kicked {} from guild {}", _e.getMember().getUser().getId(), file_value.replaceAll("[^0-9]",  ""), _e.getGuild().getName());
				FileSetting.deleteFile(file_path);
			}
			else if(file_value.replaceAll("[0-9]*",	"").equalsIgnoreCase("gift-experience")) {
				if(_message.replaceAll("[0-9]*", "").length() == 0) {
					Rank user_details = Hashes.getRanking(_e.getGuild().getId()+"_"+file_value.replaceAll("[^0-9]*", ""));
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
					if(RankingSystem.SQLsetLevelUp(user_details.getUser_ID(), _e.getGuild().getIdLong(), user_details.getLevel(), user_details.getExperience(), user_details.getCurrency(), user_details.getCurrentRole()) > 0) {
						RankingSystem.SQLInsertActionLog("low", user_details.getUser_ID(), _e.getGuild().getIdLong(), "Experience points gifted", "User received "+experience+" experience points");
						Hashes.addRanking(_e.getGuild().getId()+"_"+user_details.getUser_ID(), user_details);
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
						logger.debug("{} has gifted {} experience points to {} in guild {}", _e.getMember().getUser().getId(), _message, file_value.replaceAll("[^0-9]",  ""), _e.getGuild().getName());
						FileSetting.deleteFile(file_path);
					}
					else {
						_e.getTextChannel().sendMessage("An internal error occurred in updating the experience and level information in the table RankingSystem.user_details").queue();
						logger.error("RankingSystem.user_details table couldn't be updated with the latest experience and level information");
					}
				}
			}
			else if(file_value.replaceAll("[0-9]*", "").equalsIgnoreCase("set-experience")) {
				if(_message.replaceAll("[0-9]*", "").length() == 0) {
					Rank user_details = Hashes.getRanking(_e.getGuild().getId()+"_"+file_value.replaceAll("[^0-9]*", ""));
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
					if(RankingSystem.SQLsetLevelUp(user_details.getUser_ID(), _e.getGuild().getIdLong(), user_details.getLevel(), user_details.getExperience(), user_details.getCurrency(), user_details.getCurrentRole()) > 0) {
						RankingSystem.SQLInsertActionLog("low", user_details.getUser_ID(), _e.getGuild().getIdLong(), "Experience points edited", "User has been set to "+experience+" experience points");
						Hashes.addRanking(_e.getGuild().getId()+"_"+user_details.getUser_ID(), user_details);
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
						logger.debug("{} has set {} experience points to {} in guild {}", _e.getMember().getUser().getId(), _message, file_value.replaceAll("[^0-9]",  ""), _e.getGuild().getName());
						FileSetting.deleteFile(file_path);
					}
					else {
						_e.getTextChannel().sendMessage("An internal error occurred in updating the experience and level information in the table RankingSystem.user_details").queue();
						logger.error("RankingSystem.user_details table couldn't be updated with the latest experience and level information");
					}
				}
			}
			else if(file_value.replaceAll("[0-9]*", "").equalsIgnoreCase("set-level")) {
				if(_message.replaceAll("[0-9]*", "").length() == 0) {
					int level = Integer.parseInt(_message);
					if(level <= Hashes.getStatus(_e.getGuild().getIdLong()).getMaxLevel()) {
						Rank user_details = Hashes.getRanking(_e.getGuild().getId()+"_"+file_value.replaceAll("[^0-9]*", ""));
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
						if(RankingSystem.SQLsetLevelUp(user_details.getUser_ID(), _e.getGuild().getIdLong(), user_details.getLevel(), user_details.getExperience(), user_details.getCurrency(), user_details.getCurrentRole()) > 0) {
							RankingSystem.SQLInsertActionLog("low", user_details.getUser_ID(), _e.getGuild().getIdLong(), "Level changed", "User is now level "+user_details.getLevel());
							Hashes.addRanking(_e.getGuild().getId()+"_"+user_details.getUser_ID(), user_details);
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
							logger.debug("{} has set the level {} to {} in guild {}", _e.getMember().getUser().getId(), _message, file_value.replaceAll("[^0-9]",  ""), _e.getGuild().getName());
							FileSetting.deleteFile(file_path);
						}
						else {
							_e.getTextChannel().sendMessage("An internal error occurred in updating the experience and level information in the table RankingSystem.user_details").queue();
							logger.error("RankingSystem.user_details table couldn't be updated with the latest experience and level information");
						}
					}
					else {
						_e.getTextChannel().sendMessage(_e.getMember().getAsMention()+" Please choose a level that is lower or equal to "+Hashes.getStatus(_e.getGuild().getIdLong()).getMaxLevel()).queue();
					}
				}
			}
			else if(file_value.replaceAll("[0-9]*", "").equalsIgnoreCase("gift-currency")) {
				if(_message.replaceAll("[0-9]*", "").length() == 0) {
					Rank user_details = Hashes.getRanking(_e.getGuild().getId()+"_"+file_value.replaceAll("[^0-9]*", ""));
					long currency = Long.parseLong(_message);
					user_details.setCurrency(user_details.getCurrency()+currency);
					if(RankingSystem.SQLUpdateCurrency(user_details.getUser_ID(), _e.getGuild().getIdLong(), user_details.getCurrency()) > 0) {
						RankingSystem.SQLInsertActionLog("low", user_details.getUser_ID(), _e.getGuild().getIdLong(), "Money gifted", "User received money in value of "+currency+" "+GuildIni.getCurrency(_e.getGuild().getIdLong()));
						Hashes.addRanking(_e.getGuild().getId()+"_"+user_details.getUser_ID(), user_details);
						_e.getTextChannel().sendMessage("Currency has been updated!").queue();
						logger.debug("{} has gifted {} currency value to {} in guild {}", _e.getMember().getUser().getId(), _message, file_value.replaceAll("[^0-9]",  ""), _e.getGuild().getName());
						FileSetting.deleteFile(file_path);
					}
					else {
						_e.getTextChannel().sendMessage("An internal error occurred in updating the currency information in the table RankingSystem.user_details").queue();
						logger.error("RankingSystem.user_details table couldn't be updated with the latest currency information");
					}
				}
			}
			else if(file_value.replaceAll("[0-9]*", "").equalsIgnoreCase("set-currency")) {
				if(_message.replaceAll("[0-9]*", "").length() == 0) {
					Rank user_details = Hashes.getRanking(_e.getGuild().getId()+"_"+file_value.replaceAll("[^0-9]*", ""));
					long currency = Long.parseLong(_message);
					user_details.setCurrency(currency);
					if(RankingSystem.SQLUpdateCurrency(user_details.getUser_ID(), _e.getGuild().getIdLong(), user_details.getCurrency()) > 0) {
						RankingSystem.SQLInsertActionLog("low", user_details.getUser_ID(), _e.getGuild().getIdLong(), "Money set", "Currency value for the user has been changed to "+currency+" "+GuildIni.getCurrency(_e.getGuild().getIdLong()));
						Hashes.addRanking(_e.getGuild().getId()+"_"+user_details.getUser_ID(), user_details);
						_e.getTextChannel().sendMessage("Currency has been updated!").queue();
						logger.debug("{} has set {} currency value to {} in guild {}", _e.getMember().getUser().getId(), _message, file_value.replaceAll("[^0-9]",  ""), _e.getGuild().getName());
						FileSetting.deleteFile(file_path);
					}
					else {
						_e.getTextChannel().sendMessage("An internal error occurred in updating the currency information in the table RankingSystem.user_details").queue();
						logger.error("RankingSystem.user_details table couldn't be updated with the latest currency information");
					}
				}
			}
		}
		else {
			_e.getTextChannel().sendMessage(denied.setDescription("Session has expired! Please retype the command!").build()).queue();
			FileSetting.deleteFile(IniFileReader.getTempDirectory()+"AutoDelFiles/user_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId()+".azr");
		}
	}
}
