package listeners;

import java.awt.Color;

import constructors.Messages;
import core.Hashes;
import core.UserPrivs;
import fileManagement.GuildIni;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;
import sql.Azrael;

public class GuildMessageRemovedListener extends ListenerAdapter{
	
	@Override
	public void onGuildMessageDelete(GuildMessageDeleteEvent e) {
		if(GuildIni.getCacheLog(e.getGuild().getIdLong())) {
			EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE);
			
			long message_id = e.getMessageIdLong();
			Messages removed_message = Hashes.getMessagePool(message_id);
			Hashes.removeMessagePool(message_id);
			
			if(e.getGuild().getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS)) {
				var cache = Hashes.getTempCache("message-removed-filter_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+(removed_message != null ? removed_message.getUserID() : "0"));
				if(cache == null) {
					cache = Hashes.getTempCache("message-removed_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+(removed_message != null ? removed_message.getUserID() : "0"));
					if(cache == null) {
						long trigger_user_id = 0;
						String trigger_user_name = "";
						long removed_from = 0;
						boolean send_message = false;
						boolean suppress_deleted = false;
						var counter = 0;
						AuditLogPaginationAction logs = e.getGuild().retrieveAuditLogs().type(ActionType.MESSAGE_DELETE);
						for (AuditLogEntry entry : logs)
						{
							if(!Hashes.containsActionlog(entry.getId()+entry.getOptionByName("count")) && removed_message != null && removed_message.getUserID() == entry.getTargetIdLong()) {
								Hashes.addActionlog(entry.getId()+entry.getOptionByName("count"));
								send_message = true;
								if(e.getChannel().getId().equals(entry.getOptionByName("channel_id").toString()) && (UserPrivs.isUserAdmin(e.getGuild().getMemberById(entry.getUser().getId()).getUser(), e.getGuild().getIdLong()) || UserPrivs.isUserMod(e.getGuild().getMemberById(entry.getUser().getId()).getUser(), e.getGuild().getIdLong()))) {
									removed_from = entry.getTargetIdLong();
									if(!UserPrivs.isUserBot(e.getGuild().getMemberById(removed_from).getUser(), e.getGuild().getIdLong())) {
										trigger_user_id = entry.getUser().getIdLong();
										trigger_user_name = entry.getUser().getName()+"#"+entry.getUser().getDiscriminator();
										break;
									}
									else if(removed_from != e.getJDA().getSelfUser().getIdLong() && UserPrivs.isUserBot(e.getGuild().getMemberById(removed_from).getUser(), e.getGuild().getIdLong())) {
										suppress_deleted = true;
										break;
									}
								}
							}
							counter ++;
							if(counter == 3)
								break;
						}
						
						if(send_message == true && removed_from != 0 && trigger_user_id != removed_from) {
							if(removed_message != null && removed_message.getMessage().length() > 0 && trigger_user_id > 0) {
								message.setTitle(trigger_user_name+" has removed a message from #"+e.getChannel().getName()+"!");
								var tra_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("tra")).findAny().orElse(null);
								if(tra_channel != null) {e.getGuild().getTextChannelById(tra_channel.getChannel_ID()).sendMessage(message.setDescription("["+removed_message.getTime().toString()+" - "+removed_message.getUserName()+"]: "+removed_message.getMessage()).build()).queue();}
							}
						}
						else if(GuildIni.getSelfDeletedMessage(e.getGuild().getIdLong()) && !suppress_deleted && removed_message != null && !UserPrivs.isUserBot(e.getGuild().getMemberById(removed_message.getUserID()).getUser(), e.getGuild().getIdLong())) {
							if(removed_message != null && removed_message.getMessage().length() > 0) {
								message.setTitle("User has removed his own message from #"+e.getChannel().getName()+"!");
								var tra_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("tra")).findAny().orElse(null);
								if(tra_channel != null) {e.getGuild().getTextChannelById(tra_channel.getChannel_ID()).sendMessage(message.setDescription("["+removed_message.getTime().toString()+" - "+removed_message.getUserName()+"]: "+removed_message.getMessage()).build()).queue();}
							}
						}
					}
					else if(removed_message != null && removed_message.getMessage().length() > 0) {
						message.setTitle("Message removed from #"+e.getChannel().getName()+"!");
						var tra_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("tra")).findAny().orElse(null);
						if(tra_channel != null) {e.getGuild().getTextChannelById(tra_channel.getChannel_ID()).sendMessage(message.setDescription("["+removed_message.getTime().toString()+" - "+removed_message.getUserName()+"]: "+removed_message.getMessage()).build()).queue();}
						Hashes.clearTempCache("message-removed_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+(removed_message != null ? removed_message.getUserID() : "0"));
					}
				}
				else {
					Hashes.clearTempCache("message-removed-filter_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+(removed_message != null ? removed_message.getUserID() : "0"));
				}
			}
			
			//Log additional removed messages from users that are being watched with watch level 1
			var watchedUser = Hashes.getWatchlist(e.getGuild().getId()+"-"+removed_message.getUserID());
			if(watchedUser != null && watchedUser.getLevel() == 1) {
				message.setTitle("Logged message from "+removed_message.getUserName()+" due to watching!");
				e.getGuild().getTextChannelById(watchedUser.getWatchChannel()).sendMessage(message.setDescription("["+removed_message.getTime().toString()+"] - "+removed_message.getMessage()).build()).queue();
			}
		}
	}
}
