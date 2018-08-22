package listeners;

import java.awt.Color;

import core.Hashes;
import core.UserPrivs;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.audit.AuditLogEntry;
import net.dv8tion.jda.core.events.message.MessageDeleteEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.restaction.pagination.AuditLogPaginationAction;
import sql.SqlConnect;

public class MessageRemovedListener extends ListenerAdapter{
	
	@Override
	public void onMessageDelete(MessageDeleteEvent e) {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE);
		
		String trigger_user_id = "";
		String trigger_user_name = "";
		String removed_from = "";
		boolean send_message = false;
		long audit_id = 0;
		AuditLogPaginationAction logs = e.getGuild().getAuditLogs();
		first_entry: for (AuditLogEntry entry : logs)
		{
			if(Hashes.getMessageRemoved(entry.getIdLong()) == null || Hashes.getMessageRemoved(entry.getIdLong()) < Integer.parseInt(entry.getOptionByName("count"))){
				send_message = true;
			}
			
			if(entry.getType().toString().equals("MESSAGE_DELETE") && send_message == true && entry.getGuild().getIdLong() == e.getGuild().getIdLong() && e.getTextChannel().getIdLong() == Long.parseLong(entry.getOptionByName("channel_id").toString()) && (UserPrivs.isUserAdmin(e.getGuild().getMemberById(entry.getUser().getId()).getUser(), e.getGuild().getIdLong()) || UserPrivs.isUserMod(e.getGuild().getMemberById(entry.getUser().getId()).getUser(), e.getGuild().getIdLong()))) {
				trigger_user_id = entry.getUser().getId();
				trigger_user_name = entry.getUser().getName()+"#"+entry.getUser().getDiscriminator();
				removed_from = entry.getTargetId();
				audit_id = entry.getIdLong();
				Hashes.addMessageRemoved(audit_id, Integer.parseInt(entry.getOptionByName("count")));
				break first_entry;
			}
		}
		
		long message_id = e.getMessageIdLong();
		String removed_message = Hashes.getMessagePool(message_id);
		
		if(send_message == true && !UserPrivs.isUserBot(e.getGuild().getMemberById(removed_from).getUser(), e.getGuild().getIdLong()) && !trigger_user_id.equals(removed_from)){
			if(removed_message != null && removed_message.length() > 0) {
				Hashes.removeMessagePool(message_id);
				if(trigger_user_id.length() > 0) {
					message.setTitle(trigger_user_name+" has removed a message from #"+e.getTextChannel().getName()+"!");
					SqlConnect.SQLgetChannelID(e.getGuild().getIdLong(), "tra");
					if(removed_message.length() > 0) {
						if(SqlConnect.getChannelID() != 0){e.getGuild().getTextChannelById(SqlConnect.getChannelID()).sendMessage(message.setDescription(removed_message).build()).queue();}
					}
				}
			}
		}
		SqlConnect.clearAllVariables();
	}
}
