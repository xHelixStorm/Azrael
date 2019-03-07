package listeners;

import java.awt.Color;

import core.Hashes;
import core.Messages;
import core.UserPrivs;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.audit.AuditLogEntry;
import net.dv8tion.jda.core.events.message.MessageDeleteEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.restaction.pagination.AuditLogPaginationAction;
import sql.Azrael;

public class MessageRemovedListener extends ListenerAdapter{
	
	@Override
	public void onMessageDelete(MessageDeleteEvent e) {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE);
		
		int audit_counter = 0;
		long trigger_user_id = 0;
		String trigger_user_name = "";
		long removed_from = 0;
		boolean send_message = false;
		long audit_id = 0;
		AuditLogPaginationAction logs = e.getGuild().getAuditLogs();
		end_entry: for (AuditLogEntry entry : logs)
		{
			if(audit_counter != 3){
				if(entry.getType().toString().equals("MESSAGE_DELETE")){
					if(Hashes.getMessageRemoved(entry.getIdLong()) == null){
						Hashes.addMessageRemoved(entry.getIdLong(), 0);
					}
					if(Hashes.getMessageRemoved(entry.getIdLong()) == (Integer.parseInt(entry.getOptionByName("count")) - 1)){
						send_message = true;
					}
					if(send_message == true && e.getTextChannel().getIdLong() == Long.parseLong(entry.getOptionByName("channel_id").toString()) && (UserPrivs.isUserAdmin(e.getGuild().getMemberById(entry.getUser().getId()).getUser(), e.getGuild().getIdLong()) || UserPrivs.isUserMod(e.getGuild().getMemberById(entry.getUser().getId()).getUser(), e.getGuild().getIdLong()))) {
						removed_from = entry.getTargetIdLong();
						if(Hashes.getMessagePool(e.getMessageIdLong()) != null && Hashes.getMessagePool(e.getMessageIdLong()).getUserID() == removed_from){
							if(!UserPrivs.isUserBot(e.getGuild().getMemberById(removed_from).getUser(), e.getGuild().getIdLong())){
								trigger_user_id = entry.getUser().getIdLong();
								trigger_user_name = entry.getUser().getName()+"#"+entry.getUser().getDiscriminator();
								audit_id = entry.getIdLong();
								Hashes.addMessageRemoved(audit_id, Integer.parseInt(entry.getOptionByName("count")));
								break end_entry;
							}
							else{
								Hashes.removeMessageRemoved(entry.getIdLong());
							}
						}
					}
				}
				audit_counter++;
			}
			else {
				break end_entry;
			}
		}
		
		long message_id = e.getMessageIdLong();
		Messages removed_message = Hashes.getMessagePool(message_id);
		Hashes.removeMessagePool(message_id);
		
		if(send_message == true && removed_from != 0){
			if(trigger_user_id != removed_from){
				if(removed_message != null && removed_message.getMessage().length() > 0 && trigger_user_id > 0) {
					message.setTitle(trigger_user_name+" has removed a message from #"+e.getTextChannel().getName()+"!");
					if(removed_message.getMessage().length() > 0) {
						var tra_channel = Azrael.SQLgetChannelID(e.getGuild().getIdLong(), "tra");
						if(tra_channel != 0){e.getGuild().getTextChannelById(tra_channel).sendMessage(message.setDescription("["+removed_message.getTime().toString()+" - "+removed_message.getUserName()+"]: "+removed_message.getMessage()).build()).queue();}
					}
				}
			}
		}
	}
}
