package commands;

import core.UserPrivs;
import fileManagement.FileSetting;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class Reboot implements Command{

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(IniFileReader.getRebootCommand().equals("true")){
			if(e.getMember().getUser().getId().equals(IniFileReader.getAdmin()) || UserPrivs.isUserAdmin(e.getMember().getUser(), e.getGuild().getIdLong())){
				e.getTextChannel().sendMessage("**Now rebooting!**").queue();
				FileSetting.createFile("./files/reboot", "1");			
				e.getJDA().shutdown();
			}
			else {
				e.getTextChannel().sendMessage(":warning: " + e.getMember().getAsMention() + " **My apologies young padawan. This command can be used only from [GS]Heiliger or from an Administrator. Here a cookie** :cookie:").queue();
			}
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		System.out.println("Reboot commenced!");
	}

	@Override
	public String help() {
		return null;
	}

}
