package listeners;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import filter.LanguageEditFilter;
import net.dv8tion.jda.core.events.message.MessageUpdateEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.SqlConnect;

public class MessageEditListener extends ListenerAdapter{
	
	@Override
	@SuppressWarnings("unlikely-arg-type")
	public void onMessageUpdate(MessageUpdateEvent e){
		long channel_id = e.getTextChannel().getIdLong();
		SqlConnect.SQLgetChannel_Filter(channel_id);;
		ArrayList<String> filter_lang = SqlConnect.getFilter_Lang();
		
		if(!filter_lang.equals("")){
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.execute(new LanguageEditFilter(e, filter_lang));
			executor.shutdown();
		}
		SqlConnect.clearAllVariables();
	}
}
