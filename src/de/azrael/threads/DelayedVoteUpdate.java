package de.azrael.threads;

import java.awt.Color;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiManager;

import de.azrael.enums.Channel;
import de.azrael.enums.GoogleEvent;
import de.azrael.enums.Translation;
import de.azrael.google.GoogleSheets;
import de.azrael.google.GoogleUtils;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public class DelayedVoteUpdate implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(DelayedVoteUpdate.class);
	
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
			
			TextChannel textChannel = guild.getTextChannelById(channel_id);
			if(textChannel != null) {
				if(guild.getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY) || STATIC.setPermissions(guild, textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY))) {
					textChannel.retrieveMessageById(message_id).queue(message -> {
						for(final var reaction : message.getReactions()) {
							if(upVoteColumn > 0 && reaction.getReactionEmote().getName().equals(thumbsup))
								values.set(upVoteColumn-1, Arrays.asList(""+(reaction.getCount()-1)));
							else if(downVoteColumn > 0 && reaction.getReactionEmote().getName().equals(thumbsdown))
								values.set(downVoteColumn-1, Arrays.asList(""+(reaction.getCount()-1)));
						}
						overwriteRowOnSpreadsheet(guild, file_id, values, row);
					});
				}
				else {
					STATIC.writeToRemoteChannel(guild, new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(guild, Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(guild, Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MESSAGE_HISTORY.getName())+textChannel.getAsMention(), Channel.LOG.getType());
					logger.error("MESSAGE_HISTORY permission required to retrieve the votes from a message on channel {} in guild {}", textChannel.getId(), guild.getId());
				}
			}
			else {
				logger.warn("Vote channel {} doesn't exist anymore in guild {}", channel_id, guild.getId());
			}
		} catch (InterruptedException e) {
			logger.trace("Vote task of message {} terminated in guild {}", message_id, guild.getId());
		}
		
		//task completed! Remove this thread from the array of currently running threads
		STATIC.removeThread(Thread.currentThread());
	}

	private static void overwriteRowOnSpreadsheet(Guild guild, String file_id, List<List<Object>> values, String row) {
		try {
			GoogleSheets.overwriteRowOnSpreadsheet(GoogleSheets.getSheetsClientService(), file_id, values, row);
		} catch(SocketTimeoutException e) {
			if(GoogleUtils.timeoutHandler(guild, file_id, GoogleEvent.VOTE.name(), e)) {
				overwriteRowOnSpreadsheet(guild, file_id, values, row);
			}
		} catch (Exception e) {
			logger.error("Google Webservice error for event VOTE in guild {}", guild.getId(), e);
		}
	}
}
