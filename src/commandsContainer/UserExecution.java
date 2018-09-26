package commandsContainer;

import java.awt.Color;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import core.Hashes;
import fileManagement.FileSetting;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import sql.RankingDB;
import sql.ServerRoles;
import sql.SqlConnect;
import threads.DelayDelete;
import util.Pastebin;
import util.RankingSystemPreferences;

public class UserExecution {
	public static void getHelp(MessageReceivedEvent _e) {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE).setTitle("Help for the user command!");
		_e.getTextChannel().sendMessage(message.setDescription("Mention a user right after the command and then choose an action to take. For example to display information, to mute, to ban, to set a warning value, to set a level or to gift experience points for the ranking system").build()).queue();
	}
	
	public static void runTask(MessageReceivedEvent _e, String _input, String _displayed_input) {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE).setTitle("Choose the desired action");
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
				_e.getTextChannel().sendMessage(message.setDescription("The user has been found in this guild! Now type one of the following words within 3 minutes to execute an action!\n\n"
						+ "**information**: To display all details of the selected user\n"
						+ "**delete-messages**: To remove up to 100 messages from the selected user\n"
						+ "**warning**: To change the current warning value\n"
						+ "**mute**: To assign the mute role\n"
						+ "**ban**: To ban the user\n"
						+ "**kick**: To kick the user\n"
						+ "**gift-experience**: To gift experience points\n"
						+ "**set-experience**: To set an experience value\n"
						+ "**set-level**: To assign a level").build()).queue();
				FileSetting.createFile(IniFileReader.getTempDirectory()+"AutoDelFiles/user_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId()+".azr", raw_input);
				new Thread(new DelayDelete(IniFileReader.getTempDirectory()+"AutoDelFiles/user_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId()+".azr", 180000)).start();
			}
			else {
				_e.getTextChannel().sendMessage(_e.getMember().getAsMention()+" Error, user doesn't exist. Please try again!").queue();
			}
		}
		else {
			_e.getTextChannel().sendMessage(_e.getMember().getAsMention()+" Error, user doesn't exist. Please try again!").queue();
		}
	}
	
	public static void performAction(MessageReceivedEvent _e, String _message) {
		EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Session Expired!");
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
		String file_path = IniFileReader.getTempDirectory()+"AutoDelFiles/user_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId()+".azr";
		String file_value = FileSetting.readFile(file_path);
		if(!file_value.equals("expired")) {
			if(_message.equals("information") || _message.equals("delete-messages") || _message.equals("warning") || _message.equals("mute") || _message.equals("ban") || _message.equals("kick") || _message.equals("gift-experience") || _message.equals("set-experience") || _message.equals("set-level")) {
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
						RankingDB.SQLgetGuild(_e.getGuild().getIdLong());
						if(RankingDB.getRankingState()) {
							RankingDB.SQLgetUserDetails(Long.parseLong(file_value));
							message.addField("LEVEL", "**"+RankingDB.getLevel()+"**/**"+RankingDB.getMaxLevel()+"**", true);
							message.addField("EXPERIENCE", "**"+RankingDB.getCurrentExperience()+"**/**"+RankingDB.getRankUpExperience()+"**", true);
							if(RankingDB.getAssignedRole() != 0){
								message.addField("UNLOCKED ROLE", _e.getGuild().getRoleById(RankingDB.getAssignedRole()).getAsMention(), false);
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
						message.setTitle("You choose to delete a number of messages!");
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
						_e.getTextChannel().sendMessage(message.setDescription("Do you wish to provide a self chosen mute time in minutes? By providing a self chosen mute timer, the warning value won't increment!").build()).queue();
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
						RankingDB.SQLgetGuild(_e.getGuild().getIdLong());
						if(RankingDB.getRankingState()) {
							message.setTitle("You choose to gift experience points!");
							_e.getTextChannel().sendMessage(message.setDescription("Choose a number of experience points to add to the total number of experience points").build()).queue();
							FileSetting.createFile(file_path, "gift-experience"+file_value);
						}
						else {
							denied.setTitle("Access Denied!");
							_e.getTextChannel().sendMessage(denied.setDescription(_e.getMember().getAsMention()+" This action can't be used because the ranking system is disabled! Please choose a different action!").build()).queue();
						}
						break;
					case "set-experience":
						RankingDB.SQLgetGuild(_e.getGuild().getIdLong());
						if(RankingDB.getRankingState()) {
							message.setTitle("You choose to set experience points!");
							_e.getTextChannel().sendMessage(message.setDescription("Choose a number of experience points to set for the user").build()).queue();
							FileSetting.createFile(file_path, "set-experience"+file_value);
						}
						else {
							denied.setTitle("Access Denied!");
							_e.getTextChannel().sendMessage(denied.setDescription(_e.getMember().getAsMention()+" This action can't be used because the ranking system is disabled! Please choose a different action!").build()).queue();
						}
						break;
					case "set-level":
						RankingDB.SQLgetGuild(_e.getGuild().getIdLong());
						if(RankingDB.getRankingState()) {
							message.setTitle("You choose to set a level!");
							_e.getTextChannel().sendMessage(message.setDescription("Choose a level to assign the user").build()).queue();
							FileSetting.createFile(file_path, "set-level"+file_value);
						}
						else {
							denied.setTitle("Access Denied!");
							_e.getTextChannel().sendMessage(denied.setDescription(_e.getMember().getAsMention()+" This action can't be used because the ranking system is disabled! Please choose a different action!").build()).queue();
						}
						break;
				}
			}
			else if(file_value.replaceAll("[0-9]*", "").equals("delete-messages")){
				if(_message.replaceAll("[0-9]*", "").length() == 0){
					int value = Integer.parseInt(_message);
					if(value == 0){
						_e.getTextChannel().sendMessage("You chose to not remove any messages at all!").queue();
					}
					else if(value > 100){
						_e.getTextChannel().sendMessage("Please choose a number between 1 and 100!").queue();
					}
					else{
						SqlConnect.SQLgetUserThroughID(file_value.replaceAll("[^0-9]*", ""));
						ArrayList<Long> message_ids = new ArrayList<Long>();
						Set<Long> message_pool_key = Hashes.getWholeMessagePool().keySet();
						for(Long key : message_pool_key){
							message_ids.add(key);
						}
						int hash_counter = 0;
						StringBuilder collected_messages = new StringBuilder();
						search: for(int count = message_ids.size()-1; count >= 0; count--){
							if(hash_counter < value){
								if(Hashes.getMessagePool(message_ids.get(count)).contains(SqlConnect.getName().substring(0, SqlConnect.getName().length()-5))){
									inspect: for(TextChannel tc : _e.getGuild().getTextChannels()){
										try{
											MessageHistory history = new MessageHistory(tc);
											List<Message> retrieved_message = history.retrievePast(100).complete();
											for(Message msg : retrieved_message){
												String author_name = msg.getAuthor().getName()+"#"+msg.getAuthor().getDiscriminator();
												if(msg.getIdLong() == message_ids.get(count) && author_name.equals(SqlConnect.getName())){
													msg.delete().queue();
													if(!collected_messages.toString().contains(collected_messages+""+Hashes.getMessagePool(message_ids.get(count)))){
														collected_messages.append(Hashes.getMessagePool(message_ids.get(count))+"\n");
													}
													Hashes.removeMessagePool(message_ids.get(count));
													hash_counter++;
													break inspect;
												}
											}
										} catch(InsufficientPermissionException ipe){
											//jump to next channel
										}
									}
								}
							}
							else{
								break search;
							}
						}
						if(collected_messages.length() > 0){
							String paste_link = Pastebin.unlistedPaste("Found words have been succesfully removed!", collected_messages.toString());
							_e.getTextChannel().sendMessage(message.setDescription("The comments of the selected user have been succesfully removed: "+paste_link).build()).queue();
							collected_messages.setLength(0);
						}
						else{
							_e.getTextChannel().sendMessage(message.setDescription("Nothing has been found to delete....").build()).queue();
						}
					}
				}
			}
			else if(file_value.replaceAll("[0-9]*", "").equals("warning")) {
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
			else if(file_value.replaceAll("[0-9]*", "").equals("mute")) {
				if(_message.equalsIgnoreCase("yes")) {
					message.setTitle("You chose to provide a mute time!");
					_e.getTextChannel().sendMessage(message.setDescription("Please provide a mute time in minutes!").build()).queue();
					FileSetting.createFile(file_path, "mute-time"+file_value.replaceAll("[^0-9]*", ""));
				}
				else if(_message.equalsIgnoreCase("no")) {
					ServerRoles.SQLgetRole(_e.getGuild().getIdLong(), "mut");
					_e.getGuild().getController().addSingleRoleToMember(_e.getGuild().getMemberById(file_value.replaceAll("[^0-9]*", "")), _e.getGuild().getRoleById(ServerRoles.getRole_ID())).queue();
					FileSetting.deleteFile(file_path);
					ServerRoles.clearAllVariables();
				}
			}
			else if(file_value.replaceAll("[0-9]*", "").equals("mute-time")) {
				if(_message.replaceAll("[0-9]*", "").length() == 0) {					
					long mute_time = (Long.parseLong(_message)*60*1000);
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
			else if(file_value.replaceAll("[0-9]*", "").equals("ban")) {
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
			else if(file_value.replaceAll("[0-9]*", "").equals("ban-reason")) {
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
			else if(file_value.replaceAll("[0-9]*", "").equals("kick")) {
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
			else if(file_value.replaceAll("[0-9]*", "").equals("kick-reason")) {
				_e.getGuild().getController().kick(_e.getGuild().getMemberById(file_value.replaceAll("[^0-9]*", ""))).reason(_message).queue();
				FileSetting.deleteFile(file_path);
			}
			else if(file_value.replaceAll("[0-9]*",	"").equals("gift-experience")) {
				if(_message.replaceAll("[0-9]*", "").length() == 0) {
					long experience = Long.parseLong(_message);
					if(experience <= 2147483647) {
						RankingDB.SQLgetUserDetails(Long.parseLong(file_value.replaceAll("[^0-9]*", "")));
						RankingDB.SQLUpdateExperience(Long.parseLong(file_value.replaceAll("[^0-9]*", "")), (RankingDB.getCurrentExperience()+(int) experience), (RankingDB.getExperience()+experience));
						_e.getTextChannel().sendMessage("Experience points have been updated!").queue();
						FileSetting.deleteFile(file_path);
					}
					else {
						_e.getTextChannel().sendMessage(_e.getMember().getAsMention()+" Please choose a number that is lower than 2147483648").queue();
					}
				}
			}
			else if(file_value.replaceAll("[0-9]*", "").equals("set-experience")) {
				if(_message.replaceAll("[0-9]*", "").length() == 0) {
					long experience = Long.parseLong(_message);
					if(experience <= 2147483647) {
						RankingDB.SQLgetUserDetails(Long.parseLong(file_value.replaceAll("[^0-9]*", "")));
						RankingDB.SQLUpdateExperience(Long.parseLong(file_value.replaceAll("[^0-9]*", "")), (int) experience, (RankingDB.getExperience()-RankingDB.getCurrentExperience()+experience));
						_e.getTextChannel().sendMessage("Experience points have been updated!").queue();
						FileSetting.deleteFile(file_path);
					}
					else {
						_e.getTextChannel().sendMessage(_e.getMember().getAsMention()+" Please choose a number that is lower than 2147483648").queue();
					}
				}
			}
			else if(file_value.replaceAll("[0-9]*", "").equals("set-level")) {
				if(_message.replaceAll("[0-9]*", "").length() == 0) {
					int level = Integer.parseInt(_message);
					RankingDB.SQLgetGuild(_e.getGuild().getIdLong());
					if(level <= RankingDB.getMaxLevel()) {
						RankingDB.SQLgetUserDetails(Long.parseLong(file_value.replaceAll("[^0-9]*", "")));
						RankingDB.SQLgetRole(level);
						RankingDB.SQLUpdateLevel(Long.parseLong(file_value.replaceAll("[^0-9]*", "")), level, 0, RankingSystemPreferences.getExperienceForRankUp(level), RankingDB.getRoleID());
						_e.getTextChannel().sendMessage("The level has been updated!").queue();
						FileSetting.deleteFile(file_path);
					}
					else {
						_e.getTextChannel().sendMessage(_e.getMember().getAsMention()+" Please choose a level that is lower or equal to "+RankingDB.getMaxLevel()).queue();
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
