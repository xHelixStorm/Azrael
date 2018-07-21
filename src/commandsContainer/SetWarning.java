package commandsContainer;

import java.awt.Color;
import java.util.concurrent.TimeUnit;

import fileManagement.FileSetting;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.SqlConnect;
import threads.DelayDelete;

public class SetWarning {
	private static EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.WHITE);
	
	public static void runHelp(MessageReceivedEvent _e) {
		_e.getTextChannel().sendMessage(messageBuild.setDescription("Type a number from 1-5 to set the max allowed number of warnings, that occurs before a ban, for the mute system.\n_Note that this setting will override all previous user warnings, if the current warning of a user is higher than the one being set!_").build()).queue();
	}
	
	public static void runTask(MessageReceivedEvent _e, String _message) {
		int warning_value = 0;
		try {
			warning_value = Integer.parseInt(_message.replaceAll("[^0-9]", ""));
		} catch(NumberFormatException nfe) {
			//do nothing
		}
		
		if(warning_value != 0 && warning_value <= 5) {
			SqlConnect.SQLgetMaxWarning(_e.getGuild().getIdLong());
			if(warning_value < SqlConnect.getWarningID()) {
				SqlConnect.SQLLowerTotalWarning(_e.getGuild().getIdLong(), warning_value);
			}
			else {
				SqlConnect.SQLInsertWarning(_e.getGuild().getIdLong(), warning_value);
			}
			_e.getTextChannel().sendMessage("The system has been set to warn "+warning_value+" time(s) before banning").queue();
			
			FileSetting.createFile(IniFileReader.getTempDirectory()+"AutoDelFiles/warnings_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId(), "1");
			new Thread(new DelayDelete(IniFileReader.getTempDirectory()+"AutoDelFiles/warnings_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId(), 60000)).start();
			_e.getTextChannel().sendMessage("To complete the warning setup, you'll be asked to enter the time in minutes for every single warning. You have a total time of 10 minutes for the final setup.\n\nPlease insert the time in minutes for warning 1.").queueAfter(3, TimeUnit.SECONDS);
		}
		else {
			_e.getTextChannel().sendMessage(_e.getMember().getAsMention()+" Please insert a valid warning value between 1-5").queue();
		}
		SqlConnect.clearAllVariables();
	}
	
	public static void performUpdate(MessageReceivedEvent _e, String _message) {
		if(_message.replaceAll("[0-9]*", "").equals("")) {
			String file_value = FileSetting.readFile(IniFileReader.getTempDirectory()+"AutoDelFiles/warnings_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId());
			SqlConnect.SQLgetMaxWarning(_e.getGuild().getIdLong());
			if(Integer.parseInt(file_value) < SqlConnect.getWarningID()) {
				SqlConnect.SQLUpdateMuteTimeOfWarning(_e.getGuild().getIdLong(), Integer.parseInt(file_value), (Long.parseLong(_message)*60*1000));
				_e.getTextChannel().sendMessage("The mute time of warning "+Integer.parseInt(file_value)+" has been updated!").queue();
				_e.getTextChannel().sendMessage("Please insert the mute time for warning "+(Integer.parseInt(file_value)+1)+"!").queueAfter(1, TimeUnit.SECONDS);
				FileSetting.createFile(IniFileReader.getTempDirectory()+"AutoDelFiles/warnings_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId(), ""+(Integer.parseInt(file_value)+1));
			}
			else if(Integer.parseInt(file_value) == SqlConnect.getWarningID()) {
				SqlConnect.SQLUpdateMuteTimeOfWarning(_e.getGuild().getIdLong(), Integer.parseInt(file_value), (Long.parseLong(_message)*60*1000));
				_e.getTextChannel().sendMessage("The warnings have been configured successfully!").queue();
				FileSetting.deleteFile(IniFileReader.getTempDirectory()+"AutoDelFiles/warnings_gu"+_e.getGuild().getId()+"ch"+_e.getTextChannel().getId()+"us"+_e.getMember().getUser().getId());
			}
		}
		SqlConnect.clearAllVariables();
	}
}