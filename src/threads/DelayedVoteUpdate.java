package threads;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiManager;

import google.GoogleSheets;
import net.dv8tion.jda.api.entities.Guild;
import util.STATIC;

public class DelayedVoteUpdate implements Runnable {
	private Logger logger = LoggerFactory.getLogger(DelayedVoteUpdate.class);
	
	private final String thumbsup = EmojiManager.getForAlias(":thumbsup:").getUnicode();
	private final String thumbsdown = EmojiManager.getForAlias(":thumbsdown:").getUnicode();
	
	private Guild guild;
	private List<List<Object>> values;
	private long channel_id;
	private long message_id;
	private String file_id;
	private String row;
	private int upVoteColumn;
	private int downVoteColumn;
	
	public DelayedVoteUpdate(Guild _guild, List<List<Object>> _values, long _channel_id, long _message_id, String _file_id, String _row, int _upVoteColumn, int _downVoteColumn) {
		this.guild = _guild;
		this.values = _values;
		this.channel_id = _channel_id;
		this.message_id = _message_id;
		this.file_id = _file_id;
		this.row = _row;
		this.upVoteColumn = _upVoteColumn;
		this.downVoteColumn = _downVoteColumn;
	}

	@Override
	public void run() {
		//track this thread in case the message gets updated. For those cases, this thread needs to be interrupted
		STATIC.addThread(Thread.currentThread(), "vote"+message_id);
		
		try {
			Thread.sleep(TimeUnit.MINUTES.toMillis(3));
			
			guild.getTextChannelById(channel_id).retrieveMessageById(message_id).queue(message -> {
				for(final var reaction : message.getReactions()) {
					if(upVoteColumn > 0 && reaction.getReactionEmote().getName().equals(thumbsup))
						values.set(upVoteColumn-1, Arrays.asList(""+(reaction.getCount()-1)));
					else if(downVoteColumn > 0 && reaction.getReactionEmote().getName().equals(thumbsdown))
						values.set(downVoteColumn-1, Arrays.asList(""+(reaction.getCount()-1)));
				}
				try {
					GoogleSheets.overwriteRowOnSpreadsheet(GoogleSheets.getSheetsClientService(), file_id, values, row);
				} catch (Exception e) {
					logger.error("Google Webservice error in guild {}", guild.getId(), e);
				}
			});
		} catch (InterruptedException e) {
			logger.debug("Vote task of message ID {} terminated for guild {}", message_id, guild.getId());
		}
		
		//task completed! Remove this thread from the array of currently running threads
		STATIC.removeThread(Thread.currentThread());
	}

}