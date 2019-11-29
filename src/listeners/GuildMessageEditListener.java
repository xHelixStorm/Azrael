package listeners;

/**
 * this class gets executed when an already printed message
 * gets edited.
 * 
 * after a message has been edited, the new message will be 
 * compared with the filters if languages to filter have
 * been applied or url censoring for that channel has been
 * enabled. Additionally, the edited messages can be added
 * to the system cache and written to file
 */

import java.awt.Color;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import constructors.Messages;
import core.Hashes;
import core.UserPrivs;
import fileManagement.FileSetting;
import fileManagement.GuildIni;
import filter.LanguageEditFilter;
import filter.URLFilter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.Azrael;

public class GuildMessageEditListener extends ListenerAdapter{
	
	@Override
	public void onGuildMessageUpdate(GuildMessageUpdateEvent e) {
		new Thread(() -> {
			long channel_id = e.getChannel().getIdLong();
			var allChannels = Azrael.SQLgetChannels(e.getGuild().getIdLong());
			//execute only one thread at the same time
			ExecutorService executor = Executors.newSingleThreadExecutor();
			//check if the edited message has been already checked, in case if it's the same message like before an edit
			var messages = Hashes.getMessagePool(e.getMessageIdLong());
			var sameMessage = false;
			if(messages != null) {
				if(messages.parallelStream().filter(f -> f.getMessage().equalsIgnoreCase(e.getMessage().getContentRaw())).findAny().orElse(null) != null)
					sameMessage = true;
			}
			if(messages == null || sameMessage == false) {
				executor.execute(() -> {
					//collect all gilter languages for the current channel
					var filter_lang = Azrael.SQLgetChannel_Filter(channel_id);
					if(filter_lang.size() > 0) {
						//run the word filter, if languages have been found for edited messages
						new Thread(new LanguageEditFilter(e, filter_lang, allChannels)).start();
						//if url censoring is allowed, execute that filter as well
						if(allChannels.parallelStream().filter(f -> f.getChannel_ID() == channel_id && f.getURLCensoring()).findAny().orElse(null) != null)
							new Thread(new URLFilter(null, e, filter_lang, allChannels)).start();
					}
					//if the url censoring is enabled but no languages to filter have been set, start the url censoring anyway
					else if(allChannels.parallelStream().filter(f -> f.getChannel_ID() == channel_id && f.getURLCensoring()).findAny().orElse(null) != null)
						new Thread(new URLFilter(null, e, filter_lang, allChannels)).start();
				});
			}
			
			executor.execute(() -> {
				long destinationChannel = 0;
				boolean printEditHistory = false;
				//check if edited messages should be collected and printed in a channel
				if(GuildIni.getEditedMessage(e.getGuild().getIdLong())) {
					//retrieve any registered trash and edit channels but use the edit channel if it exists and print the message
					var traAndEdiChannels = allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && (f.getChannel_Type().equals("tra") || f.getChannel_Type().equals("edi"))).collect(Collectors.toList());
					var tra_channel = traAndEdiChannels.parallelStream().filter(f -> f.getChannel_Type().equals("tra")).findAny().orElse(null);
					var edi_channel = traAndEdiChannels.parallelStream().filter(f -> f.getChannel_Type().equals("edi")).findAny().orElse(null);
					if(tra_channel != null || edi_channel != null) {
						//verify if the message history has to be printed and not just the edited message
						if(GuildIni.getEditedMessageHistory(e.getGuild().getIdLong())) {
							printEditHistory = true;
							destinationChannel = (edi_channel != null ? edi_channel.getChannel_ID() : tra_channel.getChannel_ID());
						}
						else {
							final var printMessage = "["+LocalDateTime.now().toString()+" - "+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+" ("+e.getMember().getUser().getId()+")]: "+e.getMessage().getContentRaw();
							if(edi_channel != null) {
								e.getGuild().getTextChannelById(edi_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setTitle("User has edited his message!")
									.setDescription((printMessage.length() <= 2048 ? printMessage : printMessage.substring(0, 2040)+"...")).build()).queue();
							}
							else {
								e.getGuild().getTextChannelById(tra_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setTitle("User has edited his message!")
										.setDescription((printMessage.length() <= 2048 ? printMessage : printMessage.substring(0, 2040)+"...")).build()).queue();
							}
						}
					}
				}
				
				//check if the channel log and cache log is enabled and if one of the two or bot is/are enabled then write message to file or/and log to system cache
				var log = GuildIni.getChannelAndCacheLog(e.getGuild().getIdLong());
				if((log[0] || log[1]) && !UserPrivs.isUserBot(e.getMember())) {
					StringBuilder image_url = new StringBuilder();
					for(Attachment attch : e.getMessage().getAttachments()){
						image_url.append((e.getMessage().getContentRaw().length() == 0 && image_url.length() == 0) ? "("+attch.getProxyUrl()+")" : "\n("+attch.getProxyUrl()+")");
					}
					Messages collectedMessage = new Messages();
					collectedMessage.setUserID(e.getMember().getUser().getIdLong());
					collectedMessage.setUsername(e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator());
					collectedMessage.setGuildID(e.getGuild().getIdLong());
					collectedMessage.setChannelID(channel_id);
					collectedMessage.setChannelName(e.getChannel().getName());
					collectedMessage.setMessage(e.getMessage().getContentRaw()+image_url.toString()+"\n");
					collectedMessage.setMessageID(e.getMessageIdLong());
					collectedMessage.setTime(LocalDateTime.now());
					collectedMessage.setIsEdit(true); // note: flag set to true for edited message
					
					if(log[0]) 	FileSetting.appendFile("./message_log/"+e.getChannel().getId()+".txt", "EDIT ["+collectedMessage.getTime().toString()+" - "+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+" ("+e.getMember().getUser().getId()+")]: "+collectedMessage.getMessage());
					if(log[1]) {
						if(messages != null) {
							messages.add(collectedMessage);
							Hashes.addMessagePool(e.getMessageIdLong(), messages);
						}
					}
				}
				
				//if true, print the message history on message edit
				if(printEditHistory && destinationChannel != 0) {
					var messageCounter = 0;
					for(final var message : messages) {
						final var printMessage = "["+message.getTime().toString()+" - "+message.getUserName()+" ("+message.getUserID()+")]:\n"+message.getMessage();
						e.getGuild().getTextChannelById(destinationChannel).sendMessage(new EmbedBuilder().setTitle("Message history after edit. Message "+(++messageCounter)+" / "+messages.size())
							.setDescription((printMessage.length() <= 2048 ? printMessage : printMessage.substring(0, 2040)+"...")).build()).queue();
					}
				}
				
				//check if the current user is being watched and that the cache log is enabled
				var watchedMember = Azrael.SQLgetWatchlist(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong());
				var sentMessage = Hashes.getMessagePool(e.getMessageIdLong());
				//if the watched member level equals 2, then print all written messages from that user in a separate channel
				if(watchedMember != null && watchedMember.getLevel() == 2 && sentMessage != null) {
					var cachedMessage = sentMessage.get(0);
					e.getGuild().getTextChannelById(watchedMember.getWatchChannel()).sendMessage(new EmbedBuilder()
						.setTitle("Logged edited message due to watching!").setColor(Color.YELLOW)
						.setDescription("["+cachedMessage.getTime().toString()+" - "+cachedMessage.getUserName()+"]: "+cachedMessage.getMessage()).build()).queue();
				}
				//print an error if the cache log is not enabled
				else if(watchedMember != null && watchedMember.getLevel() == 2 && sentMessage == null) {
					e.getGuild().getTextChannelById(watchedMember.getWatchChannel()).sendMessage(new EmbedBuilder()
						.setTitle("CacheLog disabled!").setColor(Color.RED)
						.setDescription("Please enable the CacheLog to display messages! Message from "+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+" couldn't be displayed!").build()).queue();
				}
			});
		}).start();
	}
}
