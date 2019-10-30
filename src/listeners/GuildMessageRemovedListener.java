package listeners;

/**
 * This class gets executed when a message has been removed.
 * 
 * For this bot, there are many ways to remove a message. for
 * example by the word or url filter, with H!user delete-messages
 * or manual message deletions. In this class all deleted messages
 * will get printed with the message that was deleted and with the 
 * user who deleted the message, depending on the delete type.
 */

import java.awt.Color;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private final static Logger logger = LoggerFactory.getLogger(GuildMessageRemovedListener.class);
	private static final EmbedBuilder message = new EmbedBuilder().setColor(Color.ORANGE);
	
	@Override
	public void onGuildMessageDelete(GuildMessageDeleteEvent e) {
		new Thread(() ->{
			//verify that the message in cache logger is enabled
			if(GuildIni.getCacheLog(e.getGuild().getIdLong())) {
				//retrieve current message id, the message as a whole that got deleted and delete the message from cache
				long message_id = e.getMessageIdLong();
				var removed_messages = Hashes.getMessagePool(message_id);
				Hashes.removeMessagePool(message_id);
				
				//be sure that the removed message has been cached, else it won't make sense to display a message which the bot doesn't know about
				//reason can be that either the message is too old or all messages that occurred before a reboot aren't known
				if(removed_messages != null) {
					//verify that the bot has permission to view the audit log, else throw an error
					if(e.getGuild().getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS)) {
						//be sure that the message didn't get removed by a language filter, if yes ignore the message
						var cache = Hashes.getTempCache("message-removed-filter_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+removed_messages.get(0).getUserID());
						if(cache == null) {
							//be sure that the message didn't get removed from a channel where text input is not allowed
							cache = Hashes.getTempCache("message-removed_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+removed_messages.get(0).getUserID());
							if(cache == null) {
								long trigger_user_id = 0;
								String trigger_user_name = "";
								long removed_from = 0;
								boolean send_message = false;
								boolean suppress_deleted = false;
								var counter = 0;
								//retrieve the audit log for deleted messages
								AuditLogPaginationAction logs = e.getGuild().retrieveAuditLogs().type(ActionType.MESSAGE_DELETE);
								for (AuditLogEntry entry : logs)
								{
									//only execute if the current action log hasn't been read before and that the user id of the removed message is the same from the audit log
									if(!Hashes.containsActionlog(entry.getId()+entry.getOptionByName("count")) && removed_messages.get(0).getUserID() == entry.getTargetIdLong()) {
										//add action log a read and allow a message to be printed afterwards
										Hashes.addActionlog(entry.getId()+entry.getOptionByName("count"));
										send_message = true;
										//confirm that the event and audit log both point to the same text channel from where the message got removed and allow messages to be displayed
										//which have been deleted by an administrator or moderator
										if(e.getChannel().getId().equals(entry.getOptionByName("channel_id").toString()) && (UserPrivs.isUserAdmin(e.getGuild().getMemberById(entry.getUser().getId())) || UserPrivs.isUserMod(e.getGuild().getMemberById(entry.getUser().getId())))) {
											removed_from = entry.getTargetIdLong();
											//be sure to avoid printing a message which got deleted from a bot or by a bot
											if(!UserPrivs.isUserBot(e.getGuild().getMemberById(removed_from))) {
												trigger_user_id = entry.getUser().getIdLong();
												trigger_user_name = entry.getUser().getName()+"#"+entry.getUser().getDiscriminator();
												break;
											}
											else if(removed_from != e.getJDA().getSelfUser().getIdLong() && UserPrivs.isUserBot(e.getGuild().getMemberById(removed_from))) {
												suppress_deleted = true;
												break;
											}
										}
									}
									//read the newest 3 audit logs and then stop
									counter ++;
									if(counter == 3)
										break;
								}
								
								//print any deleted message which got removed by an admin or moderator
								if(send_message == true && removed_from != 0 && trigger_user_id != removed_from) {
									//confirm that we have a message to print and a user who deleted the message
									if(removed_messages.get(0).getMessage().length() > 0 && trigger_user_id > 0) {
										//retrieve the trash channel to print the removed message
										var tra_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("tra")).findAny().orElse(null);
										if(tra_channel != null) {
											//iterate through removed_messages to print the main message and if available, all edited messages belonging to the same message id
											for(final var cachedMessage : removed_messages) {
												message.setTitle(trigger_user_name+" has removed "+(cachedMessage.isEdit() ? "an **edited message**" : "a **message**")+" from #"+e.getChannel().getName()+"!");
												e.getGuild().getTextChannelById(tra_channel.getChannel_ID()).sendMessage(message.setDescription("["+cachedMessage.getTime().toString()+" - "+cachedMessage.getUserName()+" ("+cachedMessage.getUserID()+")]:\n"+cachedMessage.getMessage()).build()).queue();
											}
										}
									}
								}
								//if enabled, display all deleted messages which weren't deleted by someone else but from the same user and still isn't a bot
								else if(GuildIni.getSelfDeletedMessage(e.getGuild().getIdLong()) && !suppress_deleted && !UserPrivs.isUserBot(e.getGuild().getMemberById(removed_messages.get(0).getUserID()))) {
									//confirm that we have a message to print
									if(removed_messages.get(0).getMessage().length() > 0) {
										//look up for a registered trash and delete channel. If a delete channel has been found, this channel should be used instead of the trash channel
										var traAndDel_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && (f.getChannel_Type().equals("tra") || f.getChannel_Type().equals("del"))).collect(Collectors.toList());
										var tra_channel = traAndDel_channel.parallelStream().filter(f -> f.getChannel_Type().equals("tra")).findAny().orElse(null);
										var del_channel = traAndDel_channel.parallelStream().filter(f -> f.getChannel_Type().equals("del")).findAny().orElse(null);
										if(tra_channel != null || del_channel != null) {
											//iterate through removed_messages to print the main message and if available, all edited messages belonging to the same message id
											for(final var cachedMessage : removed_messages) {
												message.setTitle("User has removed his own "+(cachedMessage.isEdit() ? "**edited message**" : "**message**")+" from #"+e.getChannel().getName()+"!");
												e.getGuild().getTextChannelById((del_channel != null ? del_channel.getChannel_ID() : tra_channel.getChannel_ID())).sendMessage(message.setDescription("["+cachedMessage.getTime().toString()+" - "+cachedMessage.getUserName()+" ("+cachedMessage.getUserID()+")]:\n"+cachedMessage.getMessage()).build()).queue();
											}
										}
									}
								}
							}
							//log messages which were removed from a channel that don't allow text input as long there is a message to print
							else if(removed_messages.get(0).getMessage().length() > 0) {
								//retrieve the trash channel to print the removed message
								var tra_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("tra")).findAny().orElse(null);
								if(tra_channel != null) {
									//iterate through removed_messages to print the main message and if available, all edited messages belonging to the same message id
									for(final var cachedMessage : removed_messages) {
										message.setTitle((cachedMessage.isEdit() ? "**Edited message**" : "**Message**")+" removed from #"+e.getChannel().getName()+"!");
										e.getGuild().getTextChannelById(tra_channel.getChannel_ID()).sendMessage(message.setDescription("["+cachedMessage.getTime().toString()+" - "+cachedMessage.getUserName()+" ("+cachedMessage.getUserID()+")]:\n"+cachedMessage.getMessage()).build()).queue();
									}
								}
								Hashes.clearTempCache("message-removed_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+removed_messages.get(0).getUserID());
							}
						}
						else {
							Hashes.clearTempCache("message-removed-filter_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+removed_messages.get(0).getUserID());
						}
					}
					else {
						var tra_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("tra")).findAny().orElse(null);
						if(tra_channel != null) e.getGuild().getTextChannelById(tra_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Missing permission!").setDescription("Removed message detected. Message couldn't be displayed because VIEW AUDIT LOG permission is missing!").build()).queue();
						logger.warn("VIEW AUDIT LOG permission missing in guild {}!", e.getGuild().getId());
					}
					
					//Log additional removed messages from users that are being watched with watch level 1
					var watchedUser = Azrael.SQLgetWatchlist(removed_messages.get(0).getUserID(), e.getGuild().getIdLong());
					if(watchedUser != null && watchedUser.getLevel() == 1) {
						//iterate through removed_messages to print the main message and if available, all edited messages belonging to the same message id
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
