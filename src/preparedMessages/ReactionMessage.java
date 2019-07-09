package preparedMessages;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiManager;

import core.Hashes;
import fileManagement.FileSetting;
import fileManagement.GuildIni;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sql.DiscordRoles;

public class ReactionMessage {
	public static int print(MessageReceivedEvent e, long channel_id) {
		Logger logger = LoggerFactory.getLogger(ReactionMessage.class);
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
		if(DiscordRoles.SQLgetRolesByCategory(e.getGuild().getIdLong(), "rea")) {
			String [] reactions = GuildIni.getReactions(e.getGuild().getIdLong());
			StringBuilder sb = new StringBuilder();
			int counter = 0;
			for(int i = 1; i < 10; i++) {
				if(Hashes.getRoles(i+"_"+e.getGuild().getIdLong()) != null) {
					String reaction;
					if(!reactions[0].equals("true")) {
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
					sb.append(reaction+" **"+Hashes.getRoles(i+"_"+e.getGuild().getIdLong()).getRole_Name()+"**\n");
					counter ++;
				}
			}
			String reactionMessage = FileSetting.readFile("./files/reactionmessage.txt");
			if(reactionMessage.length() > 0)
				e.getGuild().getTextChannelById(channel_id).sendMessage(reactionMessage+"\n\n"
						+ ""+sb.toString()).complete();
			
			else
				e.getGuild().getTextChannelById(channel_id).sendMessage(message.setDescription("This channel can be used to assign yourself unique roles to display your main game style in the game. "
						+ "To assign yourself a role, react with one or more of the available emojis that are below this message. "
						+ "It can be used to remove the same role as well. These are the currently available emojis to react to with their unique role:\n\n"
						+ ""+sb.toString()).build()).complete();
			
			return counter;
		}
		else {
			logger.error("Reaction roles couldn't be retrieved from DiscordRoles.roles in guild {}", e.getGuild().getName());
			return 0;
		}
	}
	
	@SuppressWarnings("preview")
	public static String getReaction(int counter) {
		return switch(counter) {
			case 1  -> ":one:";
			case 2  -> ":two:";
			case 3  -> ":three:";
			case 4  -> ":four:";
			case 5  -> ":five:";
			case 6  -> ":six:";
			case 7  -> ":seven:";
			case 8  ->":eight:";
			case 9  -> ":nine:";
			default -> "empty";
		};
	}
}
