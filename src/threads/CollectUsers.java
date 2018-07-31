package threads;

import java.awt.Color;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
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
		
		RankingDB.SQLgetGuild(guild_id);
		boolean ranking_state = RankingDB.getRankingState();
		
		for(Member m : e.getJDA().getGuildById(guild_id).getMembers()){
			long user_id = m.getUser().getIdLong();
			String name = m.getUser().getName()+"#"+m.getUser().getDiscriminator();
			SqlConnect.SQLInsertUser(user_id, name);
			if(ranking_state == true){
				RankingDB.SQLInsertUser(user_id, name, RankingDB.getRankingLevel(), RankingDB.getRankingRank(), RankingDB.getRankingProfile(), RankingDB.getRankingIcon());
				RankingDB.SQLgetUserDetails(user_id);
				RankingDB.SQLInsertUserDetails(user_id, 0, 0, 300, 0, 50000, 0);
			}
		}
		e.getTextChannel().sendMessage(message.setDescription("User registration is complete!").build()).queue();
		RankingDB.clearAllVariables();
	}
}
