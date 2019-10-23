package threads;

import java.awt.Color;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;
import sql.RankingSystem;

public class CollectUsers implements Runnable {
	private final static Logger logger = LoggerFactory.getLogger(CollectUsers.class);
	private static EmbedBuilder message = new EmbedBuilder().setColor(Color.GREEN).setTitle("Collection complete!");
	GuildMessageReceivedEvent e;
	
	public CollectUsers(GuildMessageReceivedEvent _e){
		e = _e;
	}

	@Override
	public void run() {
		long guild_id = e.getGuild().getIdLong();
		var guild_settings = RankingSystem.SQLgetGuild(guild_id);
		List<Member> members = e.getGuild().getMembers();
		Azrael.SQLBulkInsertUsers(members);
		if(guild_settings != null && guild_settings.getRankingState()) {
			RankingSystem.SQLBulkInsertUsers(members, guild_settings.getLevelID(), guild_settings.getRankID(), guild_settings.getProfileID(), guild_settings.getIconID());
			RankingSystem.SQLBulkInsertUserDetails(members, 0, 0, 50000, 0);
		}
		logger.debug("{} has registered all users from the guild {}", e.getMember().getUser().getId(), e.getGuild().getName());
		e.getChannel().sendMessage(message.setDescription("User registration is complete!").build()).queue();
	}
}
