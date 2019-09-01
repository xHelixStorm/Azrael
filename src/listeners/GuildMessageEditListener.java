package listeners;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import filter.LanguageEditFilter;
import filter.URLFilter;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.Azrael;

public class GuildMessageEditListener extends ListenerAdapter{
	
	@Override
	public void onGuildMessageUpdate(GuildMessageUpdateEvent e){
		long channel_id = e.getChannel().getIdLong();
		var filter_lang = Azrael.SQLgetChannel_Filter(channel_id);
		
		var allChannels = Azrael.SQLgetChannels(e.getGuild().getIdLong());
		ExecutorService executor = Executors.newSingleThreadExecutor();
		if(filter_lang.size() > 0) {
			executor.execute(new LanguageEditFilter(e, filter_lang, allChannels));
			if(allChannels.parallelStream().filter(f -> f.getChannel_ID() == channel_id && f.getURLCensoring()).findAny().orElse(null) != null)
				executor.execute(new URLFilter(null, e, filter_lang, allChannels));
		}
		else if(allChannels.parallelStream().filter(f -> f.getChannel_ID() == channel_id && f.getURLCensoring()).findAny().orElse(null) != null)
			executor.execute(new URLFilter(null, e, filter_lang, allChannels));
		
		executor.shutdown();
	}
}
