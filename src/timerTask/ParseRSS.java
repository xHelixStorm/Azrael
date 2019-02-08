package timerTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiParser;

import core.Hashes;
import core.RSS;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.events.ReadyEvent;

public class ParseRSS extends TimerTask{
	private static final Logger logger = LoggerFactory.getLogger(ParseRSS.class);
	
	private ReadyEvent e;
	private long guild_id;
	private long rss_channel;
	
	public ParseRSS(ReadyEvent _e, long _guild_id, long _rss_channel) {
		this.e = _e;
		this.guild_id = _guild_id;
		this.rss_channel = _rss_channel;
	}

	@Override
	public void run() {
		logger.info("task running for guild {}", e.getJDA().getGuildById(guild_id).getName());
		if(rss_channel != 0 && Hashes.getFeed(guild_id) != null) {
			for(RSS rss : Hashes.getFeed(guild_id)) {
				try {
					String format = rss.getFormat();
					logger.debug("Retrieving rss feed for {} in guild {}", rss.getURL(), e.getJDA().getGuildById(guild_id).getName());
					URL rssUrl = new URL(rss.getURL());
					URLConnection con = rssUrl.openConnection();
					con.setConnectTimeout(5000);
					con.setReadTimeout(10000);
					BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
					
					boolean itemTagFound = false;
					String title = "";
					String description = "";
					String pubDate = "";
					String link = "";
					
					String line;
					while((line = in.readLine()) != null) {
						if(line.startsWith("<item>"))
							itemTagFound = true;
						else if(line.endsWith("</item>"))
							break;
						if(itemTagFound == true) {
							if(line.contains("<title>")) {
								int firstPos = line.indexOf("<title>");
								int lastPos = line.indexOf("</title>");
								title = line.substring(firstPos, lastPos).replaceAll("(<title>|</title>)", "");
							}
							if(line.contains("<description>")) {
								int firstPos = line.indexOf("<description>");
								int lastPos = line.indexOf("</description>");
								description = line.substring(firstPos, lastPos).replaceAll("(<description>|</descrption>)", "");
							}
							if(line.contains("<pubDate>")) {
								int firstPos = line.indexOf("<pubDate>");
								int lastPos = line.indexOf("</pubDate>");
								pubDate = line.substring(firstPos, lastPos).replaceAll("(<pubDate>|</pubDate>)", "");
							}
							if(line.contains("<link>")) {
								int firstPos = line.indexOf("<link>");
								int lastPos = line.indexOf("</link>");
								link = line.substring(firstPos, lastPos).replaceAll("(<link>|</link>)", "");
							}
						}
					}
					if(title.length() > 0 || description.length() > 0 || pubDate.length() > 0 || link.length() > 0) {
						String out = format.replace("{title}", title);
						out = out.replace("{description}", description);
						out = out.replace("{pubDate}", pubDate);
						out = out.replace("{link}", link);
						final String outMessage = EmojiParser.parseToUnicode(out);
						MessageHistory history = new MessageHistory(e.getJDA().getGuildById(guild_id).getTextChannelById(rss_channel));
						List<Message> msg = history.retrievePast(30).complete();
						Message historyMessage = msg.parallelStream().filter(f -> f.getContentRaw().equals(outMessage)).findAny().orElse(null);
						if(historyMessage == null)
							e.getJDA().getGuildById(guild_id).getTextChannelById(rss_channel).sendMessage(outMessage).queue();
					}
					in.close();
				} catch (Exception e1) {
					logger.error("Error on retrieving feed", e1);
				}
			}
		}
	}
	
	public static void runTask(ReadyEvent _e, long _guild_id, long _rss_channel){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		Timer time = new Timer();
		time.schedule(new ParseRSS(_e, _guild_id, _rss_channel), calendar.getTime(), TimeUnit.MINUTES.toMillis(10));
	}
}
