package listeners;

import java.awt.Color;

import core.Hashes;
import core.UserPrivs;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.audit.AuditLogEntry;
import net.dv8tion.jda.core.events.message.MessageDeleteEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.restaction.pagination.AuditLogPaginationAction;
import sql.ServerRoles;

public class MessageRemovedListener extends ListenerAdapter{
	
	@Override
	public void onMessageDelete(MessageDeleteEvent e) {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE);
		
		String trigger_user_id = "";
		String trigger_user_name = "";
		AuditLogPaginationAction logs = e.getGuild().getAuditLogs();
		first_entry: for (AuditLogEntry entry : logs)
		{
			if(entry.getType().toString().equals("MESSAGE_DELETE") && entry.getGuild().getIdLong() == e.getGuild().getIdLong()) {
				trigger_user_id = entry.getUser().getId();
				trigger_user_name = entry.getUser().getName()+"#"+entry.getUser().getDiscriminator();
			}
			break first_entry;
		}
		
		long message_id = e.getMessageIdLong();
		String removed_message = Hashes.getMessagePool(message_id);
		
		if(removed_message.length() > 0) {
			Hashes.removeMessagePool(message_id);
		}
		
		if(UserPrivs.isUserAdmin(e.getGuild().getMemberById(trigger_user_id).getUser(), e.getGuild().getIdLong()) || UserPrivs.isUserMod(e.getGuild().getMemberById(trigger_user_id).getUser(), e.getGuild().getIdLong())) {
			message.setTitle(trigger_user_name+" has removed a message from a user!");
			ServerRoles.SQLgetRole(e.getGuild().getIdLong(), "tra");
			if(removed_message.length() > 0) {
				if(ServerRoles.getRole_ID() != 0){e.getGuild().getTextChannelById(ServerRoles.getRole_ID()).sendMessage(message.setDescription(removed_message).build()).queue();}
			}
		}
	}
}
