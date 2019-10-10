package listeners;

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
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.execute(() -> {
				var filter_lang = Azrael.SQLgetChannel_Filter(channel_id);
				if(filter_lang.size() > 0) {
					new Thread(new LanguageEditFilter(e, filter_lang, allChannels)).start();
					if(allChannels.parallelStream().filter(f -> f.getChannel_ID() == channel_id && f.getURLCensoring()).findAny().orElse(null) != null)
						new Thread(new URLFilter(null, e, filter_lang, allChannels)).start();
				}
				else if(allChannels.parallelStream().filter(f -> f.getChannel_ID() == channel_id && f.getURLCensoring()).findAny().orElse(null) != null)
					new Thread(new URLFilter(null, e, filter_lang, allChannels)).start();
			});
			
			executor.execute(() -> {
				if(GuildIni.getEditedMessage(e.getGuild().getIdLong())) {
					var traAndEdiChannels = allChannels.parallelStream().filter(f -> f.getChannel_Type() != null && (f.getChannel_Type().equals("tra") || f.getChannel_Type().equals("edi"))).collect(Collectors.toList());
					var tra_channel = traAndEdiChannels.parallelStream().filter(f -> f.getChannel_Type().equals("tra")).findAny().orElse(null);
					var edi_channel = traAndEdiChannels.parallelStream().filter(f -> f.getChannel_Type().equals("edi")).findAny().orElse(null);
					if(tra_channel != null || edi_channel != null) {
						if(edi_channel != null) {
							e.getGuild().getTextChannelById(edi_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.YELLOW).setTitle("User has edited his message!")
								.setDescription("["+LocalDateTime.now().toString()+" - "+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+" ("+e.getMember().getUser().getId()+")]: "+e.getMessage().getContentRaw()).build()).queue();
						}
						else {
							e.getGuild().getTextChannelById(tra_channel.getChannel_ID()).sendMessage(new EmbedBuilder().setColor(Color.YELLOW).setTitle("User has edited his message!")
									.setDescription("["+LocalDateTime.now().toString()+" - "+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+" ("+e.getMember().getUser().getId()+")]: "+e.getMessage().getContentRaw()).build()).queue();
						}
					}
				}
				
				var log = GuildIni.getChannelAndCacheLog(e.getGuild().getIdLong());
				if((log[0] || log[1]) && !UserPrivs.isUserBot(e.getMember().getUser(), e.getGuild().getIdLong())) {
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
					collectedMessage.setIsEdit(true);
					
					if(log[0]) 	FileSetting.appendFile("./message_log/"+e.getChannel().getId()+".txt", "EDIT ["+collectedMessage.getTime().toString()+" - "+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+" ("+e.getMember().getUser().getId()+")]: "+collectedMessage.getMessage());
					if(log[1]) {
						var messages = Hashes.getMessagePool(e.getMessageIdLong());
						if(messages != null) {
							messages.add(collectedMessage);
							Hashes.addMessagePool(e.getMessageIdLong(), messages);
						}
					}
				}
				var watchedMember = Hashes.getWatchlist(e.getGuild().getId()+"-"+e.getMember().getUser().getId());
				var sentMessage = Hashes.getMessagePool(e.getMessageIdLong());
				if(watchedMember != null && watchedMember.getLevel() == 2 && sentMessage != null) {
					var cachedMessage = sentMessage.get(0);
					e.getGuild().getTextChannelById(watchedMember.getWatchChannel()).sendMessage(new EmbedBuilder()
						.setTitle("Logged edited message due to watching!").setColor(Color.YELLOW)
						.setDescription("["+cachedMessage.getTime().toString()+" - "+cachedMessage.getUserName()+"]: "+cachedMessage.getMessage()).build()).queue();
				}
				else if(watchedMember != null && watchedMember.getLevel() == 3 && sentMessage == null) {
					e.getGuild().getTextChannelById(watchedMember.getWatchChannel()).sendMessage(new EmbedBuilder()
						.setTitle("CacheLog disabled!").setColor(Color.RED)
						.setDescription("Please enable the CacheLog to display messages! Message from "+e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator()+" couldn't be displayed!").build()).queue();
				}
			});
		}).start();
	}
}
