package threads;

import java.awt.Color;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.RankingDB;
import sql.SqlConnect;

public class CollectUsers implements Runnable{
	private static EmbedBuilder message = new EmbedBuilder().setColor(Color.GREEN).setTitle("Collection complete!");
	MessageReceivedEvent e;
	
	public CollectUsers(MessageReceivedEvent _e){
		e = _e;
	}

	@Override
	public void run() {
		long guild_id = e.getGuild().getIdLong();
		SqlConnect.SQLBulkInsertUsers(e.getJDA().getGuildById(guild_id).getMembers());
		e.getTextChannel().sendMessage(message.setDescription("User registration is complete!").build()).queue();
		RankingDB.clearAllVariables();
	}
}
