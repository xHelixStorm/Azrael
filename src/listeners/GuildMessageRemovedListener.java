package listeners;

import java.awt.Color;
import java.util.stream.Collectors;

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

public class GuildMessageRemovedListener extends ListenerAdapter {
	private static final EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE);
	
	@Override
	public void onGuildMessageDelete(GuildMessageDeleteEvent e) {
		new Thread(() ->{
			if(GuildIni.getCacheLog(e.getGuild().getIdLong())) {
				long message_id = e.getMessageIdLong();
				var removed_messages = Hashes.getMessagePool(message_id);
				Hashes.removeMessagePool(message_id);
				
				if(e.getGuild().getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS)) {
					var cache = Hashes.getTempCache("message-removed-filter_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+(removed_messages != null ? removed_messages.get(0).getUserID() : "0"));
					if(cache == null) {
						cache = Hashes.getTempCache("message-removed_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+(removed_messages != null ? removed_messages.get(0).getUserID() : "0"));
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
								if(!Hashes.containsActionlog(entry.getId()+entry.getOptionByName("count")) && removed_messages != null && removed_messages.get(0).getUserID() == entry.getTargetIdLong()) {
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
								if(removed_messages != null && removed_messages.get(0).getMessage().length() > 0 && trigger_user_id > 0) {
									var tra_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("tra")).findAny().orElse(null);
									if(tra_channel != null) {
										for(final var cachedMessage : removed_messages) {
											message.setTitle(trigger_user_name+" has removed "+(cachedMessage.isEdit() ? "an **edited message**" : "a **message**")+" from #"+e.getChannel().getName()+"!");
											e.getGuild().getTextChannelById(tra_channel.getChannel_ID()).sendMessage(message.setDescription("["+cachedMessage.getTime().toString()+" - "+cachedMessage.getUserName()+" ("+cachedMessage.getUserID()+")]:\n"+cachedMessage.getMessage()).build()).queue();
										}
									}
								}
							}
							else if(GuildIni.getSelfDeletedMessage(e.getGuild().getIdLong()) && !suppress_deleted && removed_messages != null && !UserPrivs.isUserBot(e.getGuild().getMemberById(removed_messages.get(0).getUserID()).getUser(), e.getGuild().getIdLong())) {
								if(removed_messages != null && removed_messages.get(0).getMessage().length() > 0) {
									var traAndDel_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && (f.getChannel_Type().equals("tra") || f.getChannel_Type().equals("del"))).collect(Collectors.toList());
									var tra_channel = traAndDel_channel.parallelStream().filter(f -> f.getChannel_Type().equals("tra")).findAny().orElse(null);
									var del_channel = traAndDel_channel.parallelStream().filter(f -> f.getChannel_Type().equals("del")).findAny().orElse(null);
									if(tra_channel != null || del_channel != null) {
										for(final var cachedMessage : removed_messages) {
											message.setTitle("User has removed his own "+(cachedMessage.isEdit() ? "**edited message**" : "**message**")+" from #"+e.getChannel().getName()+"!");
											e.getGuild().getTextChannelById((del_channel != null ? del_channel.getChannel_ID() : tra_channel.getChannel_ID())).sendMessage(message.setDescription("["+cachedMessage.getTime().toString()+" - "+cachedMessage.getUserName()+" ("+cachedMessage.getUserID()+")]:\n"+cachedMessage.getMessage()).build()).queue();
										}
									}
								}
							}
						}
						else if(removed_messages != null && removed_messages.get(0).getMessage().length() > 0) {
							var tra_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("tra")).findAny().orElse(null);
							if(tra_channel != null) {
								for(final var cachedMessage : removed_messages) {
									message.setTitle((cachedMessage.isEdit() ? "**Edited message**" : "**Message**")+" removed from #"+e.getChannel().getName()+"!");
									e.getGuild().getTextChannelById(tra_channel.getChannel_ID()).sendMessage(message.setDescription("["+cachedMessage.getTime().toString()+" - "+cachedMessage.getUserName()+" ("+cachedMessage.getUserID()+")]:\n"+cachedMessage.getMessage()).build()).queue();
								}
							}
							Hashes.clearTempCache("message-removed_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+(removed_messages != null ? removed_messages.get(0).getUserID() : "0"));
						}
					}
					else {
						Hashes.clearTempCache("message-removed-filter_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+(removed_messages != null ? removed_messages.get(0).getUserID() : "0"));
					}
				}
				
				//Log additional removed messages from users that are being watched with watch level 1
				if(removed_messages != null) {
					var watchedUser = Azrael.SQLgetWatchlist(removed_messages.get(0).getUserID(), e.getGuild().getIdLong());
					if(watchedUser != null && watchedUser.getLevel() == 1) {
						for(final var cachedMessage : removed_messages) {
							message.setTitle("Logged deleted "+(cachedMessage.isEdit() ? "**edited message**" : "**message**")+" due to watching!");
							e.getGuild().getTextChannelById(watchedUser.getWatchChannel()).sendMessage(message.setDescription("["+cachedMessage.getTime().toString()+" - "+cachedMessage.getUserName()+" ("+cachedMessage.getUserID()+")]:\n"+cachedMessage.getMessage()).build()).queue();
						}
					}
				}
			}
		}).start();
	}
}
