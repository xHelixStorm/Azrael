package threads;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;

public class CollectUsers implements Runnable{
	private static EmbedBuilder message = new EmbedBuilder().setColor(Color.GREEN).setTitle("Collection complete!");
	GuildMessageReceivedEvent e;
	
	public CollectUsers(GuildMessageReceivedEvent _e){
		e = _e;
	}

	@Override
	public void run() {
		long guild_id = e.getGuild().getIdLong();
		Azrael.SQLBulkInsertUsers(e.getJDA().getGuildById(guild_id).getMembers());
		Logger logger = LoggerFactory.getLogger(CollectUsers.class);
		logger.debug("{} has registered all users from the guild {}", e.getMember().getUser().getId(), e.getGuild().getName());
		e.getChannel().sendMessage(message.setDescription("User registration is complete!").build()).queue();
	}
}
