package listeners;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import filter.LanguageEditFilter;
import filter.URLFilter;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.Azrael;

public class MessageEditListener extends ListenerAdapter{
	
	@Override
	public void onMessageUpdate(MessageUpdateEvent e){
		long channel_id = e.getTextChannel().getIdLong();
		var filter_lang = Azrael.SQLgetChannel_Filter(channel_id);
		
		ExecutorService executor = Executors.newSingleThreadExecutor();
		if(filter_lang.size() > 0) {
			var allChannels = Azrael.SQLgetChannels(e.getGuild().getIdLong());
			executor.execute(new LanguageEditFilter(e, filter_lang, allChannels));
			executor.execute(new URLFilter(null, e, filter_lang, allChannels));
		}
		else {
			var allChannels = Azrael.SQLgetChannels(e.getGuild().getIdLong());
			executor.execute(new URLFilter(null, e, filter_lang, allChannels));
		}
		executor.shutdown();
	}
}
