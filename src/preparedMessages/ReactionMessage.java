package preparedMessages;

import java.awt.Color;

import core.Hashes;
import fileManagement.FileSetting;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.ServerRoles;

public class ReactionMessage {
	public static int print(MessageReceivedEvent e, long channel_id) {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
		ServerRoles.SQLgetRolesByCategory(e.getGuild().getIdLong(), "rea");
		String [] reactions = IniFileReader.getReactions();
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
						reaction = e.getGuild().getEmotesByName(reactions[i], false).get(0).getAsMention();
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
			e.getGuild().getTextChannelById(channel_id).sendMessage(message.setDescription(reactionMessage+"\n\n"
					+ ""+sb.toString()).build()).complete();
		
		else
			e.getGuild().getTextChannelById(channel_id).sendMessage(message.setDescription("This channel can be used to assign yourself unique roles to display your main game style in the game. "
					+ "To assign yourself a role, react with one or more of the available emojis that are below this message. "
					+ "It can be used to remove the same role as well. These are the currently available emojis to react to with their unique role:\n\n"
					+ ""+sb.toString()).build()).complete();
		
		return counter;
	}
	
	public static String getReaction(int counter) {
		switch(counter) {
			case 1:
				return ":one:";
			case 2:
				return ":two:";
			case 3:
				return ":three:";
			case 4:
				return ":four:";
			case 5:
				return ":five:";
			case 6:
				return ":six:";
			case 7:
				return ":seven:";
			case 8:
				return ":eight:";
			case 9:
				return ":nine:";
			default:
				return "empty";
		}
	}
}
