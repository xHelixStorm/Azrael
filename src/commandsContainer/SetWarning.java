package commandsContainer;

import java.awt.Color;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fileManagement.FileSetting;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.Azrael;
import threads.DelayDelete;

public class SetWarning {
	private static final Logger logger = LoggerFactory.getLogger(SetWarning.class);
	private static EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.WHITE).setThumbnail(IniFileReader.getSettingsThumbnail()).setTitle("Define the max amount of mutes that are tolerated in this server!");;
	
	public static void runHelp(MessageReceivedEvent _e) {
		_e.getTextChannel().sendMessage(messageBuild.setDescription("Type a number from 1-5 to set the max allowed number of warnings, that occurs before a ban, for the mute system.\n\n_Note that this setting will override all previous user warnings, if the current warning of a user is higher than the one being set!_").build()).queue();
	}
	
	public static void runTask(MessageReceivedEvent _e, String _message) {
		int warning_value = 0;
		try {
			warning_value = Integer.parseInt(_message.replaceAll("[^0-9]", ""));
		} catch(NumberFormatException nfe) {
			//do nothing
		}
		
		if(warning_value != 0 && warning_value <= 5) {
			var editedRows = 0;
			if(warning_value < Azrael.SQLgetMaxWarning(_e.getGuild().getIdLong())) {
				editedRows = Azrael.SQLLowerTotalWarning(_e.getGuild().getIdLong(), warning_value);
			}
			else {
				editedRows = Azrael.SQLInsertWarning(_e.getGuild().getIdLong(), warning_value);
			}
			
			if(editedRows > 0) {
				logger.debug("{} has edited the warning level in guild {}", _e.getMember().getUser().getId(), _e.getGuild().getName());
				_e.getTextChannel().sendMessage("The system has been set to warn "+warning_value+" time(s) before banning").queue();
				
				FileSetting.createFile(IniFileReader.getTempDirectory()+"AutoDelFiles/warnings_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId()+".azr", "1");
				new Thread(new DelayDelete(IniFileReader.getTempDirectory()+"AutoDelFiles/warnings_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId()+".azr", 600000)).start();
				_e.getTextChannel().sendMessage("To complete the warning setup, you'll be asked to enter the time in minutes for every single warning. You have a total time of 10 minutes for the final setup.\n\nPlease insert the time in minutes for warning 1.").queueAfter(3, TimeUnit.SECONDS);
			}
			else {
				logger.error("The warning level for the guild {} couldn't be edited on Azrael.warnings", _e.getGuild().getName());
				_e.getTextChannel().sendMessage("An internal error occurred. The warning level couldn't be updated on Azrael.warnings").queue();
			}
		}
		else {
			_e.getTextChannel().sendMessage(_e.getMember().getAsMention()+" Please insert a valid warning value between 1-5").queue();
		}
	}
	
	public static void performUpdate(MessageReceivedEvent _e, String _message) {
		EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle("Session Expired!");
		if(_message.replaceAll("[0-9]*", "").equals("")) {
			String file_value = FileSetting.readFile(IniFileReader.getTempDirectory()+"AutoDelFiles/warnings_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId()+".azr");
			if(!file_value.equals("expired")) {
				var max_warning = Azrael.SQLgetMaxWarning(_e.getGuild().getIdLong());
				if(Integer.parseInt(file_value) < max_warning) {
					if(Azrael.SQLUpdateMuteTimeOfWarning(_e.getGuild().getIdLong(), Integer.parseInt(file_value), (Long.parseLong(_message)*60*1000)) > 0) {
						_e.getTextChannel().sendMessage("The mute time of warning "+Integer.parseInt(file_value)+" has been updated!").queue();
						_e.getTextChannel().sendMessage("Please insert the mute time for warning "+(Integer.parseInt(file_value)+1)+"!").queueAfter(1, TimeUnit.SECONDS);
						FileSetting.createFile(IniFileReader.getTempDirectory()+"AutoDelFiles/warnings_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId()+".azr", ""+(Integer.parseInt(file_value)+1));
					}
					else {
						logger.error("warning timer couldn't be updated in guild {}", _e.getGuild().getName());
						_e.getTextChannel().sendMessage("An internal error occurred. The timer couldn't be inserted into Azrael.warnings. Please insert the time again").queue();
					}
				}
				else if(Integer.parseInt(file_value) == max_warning) {
					if(Azrael.SQLUpdateMuteTimeOfWarning(_e.getGuild().getIdLong(), Integer.parseInt(file_value), (Long.parseLong(_message)*60*1000)) > 0) {
						_e.getTextChannel().sendMessage("The warnings have been configured successfully!").queue();
						logger.debug("Warnings have been configured");
						FileSetting.deleteFile(IniFileReader.getTempDirectory()+"AutoDelFiles/warnings_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId()+".azr");
					}
					else {
						logger.error("warning timer couldn't be updated in guild {}", _e.getGuild().getName());
						_e.getTextChannel().sendMessage("An internal error occurred. The timer couldn't be inserted into Azrael.warnings. Please insert the time again").queue();
					}
				}
			}
			else {
				_e.getTextChannel().sendMessage(denied.setDescription("Session has expired! Please retype the command!").build()).queue();
				FileSetting.deleteFile(IniFileReader.getTempDirectory()+"AutoDelFiles/warnings_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId()+".azr");
			}
		}
	}
}
