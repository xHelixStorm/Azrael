package de.azrael.threads;

import java.awt.Color;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.enums.Translation;
import de.azrael.sql.Azrael;
import de.azrael.sql.RankingSystem;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CollectUsers implements Runnable {
	private final static Logger logger = LoggerFactory.getLogger(CollectUsers.class);
	private static EmbedBuilder message = new EmbedBuilder().setColor(Color.GREEN);
	private MessageReceivedEvent e;
	private boolean suppressMessage;
	
	public CollectUsers(MessageReceivedEvent _e, boolean _suppressMessage) {
		e = _e;
		suppressMessage = _suppressMessage;
	}

	@Override
	public void run() {
		long guild_id = e.getGuild().getIdLong();
		var guild_settings = RankingSystem.SQLgetGuild(guild_id);
		List<Member> members = e.getGuild().loadMembers().get();
		Azrael.SQLBulkInsertUsers(members);
		Azrael.SQLBulkInsertJoinDates(members);
		if(guild_settings != null && guild_settings.getRankingState()) {
			RankingSystem.SQLBulkInsertUsers(members, guild_settings.getLevelID(), guild_settings.getRankID(), guild_settings.getProfileID(), guild_settings.getIconID());
			RankingSystem.SQLBulkInsertUserDetails(members, 0, 0, guild_settings.getStartCurrency(), 0);
		}
		if(!suppressMessage)
			e.getChannel().sendMessageEmbeds(message.setDescription(STATIC.getTranslation2(e.getGuild(), Translation.USER_REGISTER_COMPLETE)).build()).queue();
		logger.info("User {} has registered all available users in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}
}
