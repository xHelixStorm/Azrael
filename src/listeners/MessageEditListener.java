package listeners;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import core.Hashes;
import filter.LanguageEditFilter;
import net.dv8tion.jda.core.events.message.MessageUpdateEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.Azrael;

public class MessageEditListener extends ListenerAdapter{
	
	@Override
	public void onMessageUpdate(MessageUpdateEvent e){
		long channel_id = e.getTextChannel().getIdLong();
		Azrael.SQLgetChannel_Filter(channel_id);;
		
		if(Hashes.getFilterLang(channel_id).size() > 0){
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.execute(new LanguageEditFilter(e, Hashes.getFilterLang(channel_id)));
			executor.shutdown();
		}
		Azrael.clearAllVariables();
	}
}
