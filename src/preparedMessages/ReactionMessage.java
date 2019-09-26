package preparedMessages;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiManager;

import constructors.Cache;
import core.Hashes;
import fileManagement.FileSetting;
import fileManagement.GuildIni;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.DiscordRoles;

public class ReactionMessage {
	private final static Logger logger = LoggerFactory.getLogger(ReactionMessage.class);
	
	public static void print(GuildMessageReceivedEvent e, long channel_id) {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
		var reactionRoles = DiscordRoles.SQLgetReactionRoles(e.getGuild().getIdLong());
		if(reactionRoles != null && reactionRoles.size() > 0) {
			String [] reactions = GuildIni.getReactions(e.getGuild().getIdLong());
			StringBuilder sb = new StringBuilder();
			int counter = 0;
			var reactionEnabled = GuildIni.getReactionEnabled(e.getGuild().getIdLong());
			for(int i = 0; i < reactionRoles.size(); i++) {
				String reaction;
				if(!reactionEnabled) {
					reaction = getReaction(i);
				}
				else {
					if(reactions[i].length() > 0) {
						try {
							reaction = e.getGuild().getEmotesByName(reactions[i], false).get(0).getAsMention();
						} catch(Exception exc) {
							reaction = EmojiManager.getForAlias(":"+reactions[i]+":").getUnicode();
						}
					}
					else {
						reaction = getReaction(i);
					}
				}
				sb.append(reaction+" **"+reactionRoles.get(i).getRole_Name()+"**\n");
				counter ++;
				if(i == 8) break;
			}
			Hashes.addTempCache("reaction_gu"+e.getGuild().getId()+"ch"+channel_id, new Cache(0, ""+counter));
			String reactionMessage = FileSetting.readFile("./files/Guilds/"+e.getGuild().getId()+"/reactionmessage.txt");
			if(reactionMessage.length() > 0)
				e.getGuild().getTextChannelById(channel_id).sendMessage(reactionMessage+"\n\n"
						+ ""+sb.toString()).complete();
			
			else
				e.getGuild().getTextChannelById(channel_id).sendMessage(message.setDescription("This channel can be used to assign yourself unique roles to display your main game style in the game. "
						+ "To assign yourself a role, react with one or more of the available emojis that are below this message. "
						+ "It can be used to remove the same role as well. These are the currently available emojis to react to with their unique role:\n\n"
						+ ""+sb.toString()).build()).complete();
		}
		else {
			logger.error("Reaction roles couldn't be retrieved from DiscordRoles.roles in guild {}", e.getGuild().getId());
		}
	}
	
	@SuppressWarnings("preview")
	public static String getReaction(int counter) {
		return switch(counter) {
			case 0  -> ":one:";
			case 1  -> ":two:";
			case 2  -> ":three:";
			case 3  -> ":four:";
			case 4  -> ":five:";
			case 5  -> ":six:";
			case 6  -> ":seven:";
			case 7  ->":eight:";
			case 8  -> ":nine:";
			default -> "empty";
		};
	}
}
