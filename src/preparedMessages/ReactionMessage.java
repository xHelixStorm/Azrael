package preparedMessages;

import java.awt.Color;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiManager;

import constructors.Roles;
import core.Hashes;
import fileManagement.FileSetting;
import fileManagement.GuildIni;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
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
				if(i == 8) break;
			}
			String reactionMessage = FileSetting.readFile("./files/Guilds/"+e.getGuild().getId()+"/reactionmessage.txt");
			if(reactionMessage.length() > 0)
				e.getGuild().getTextChannelById(channel_id).sendMessage(reactionMessage+"\n\n"
						+ ""+sb.toString()).queue(response -> {
							addReactions(response, e, reactionRoles, reactionEnabled, reactions);
						});
			
			else
				e.getGuild().getTextChannelById(channel_id).sendMessage(message.setDescription("Assign yourself a unique role!. "
						+ "To assign yourself a role, react with one or more of the available emojis that are below this message. "
						+ "It can be used to remove the same role as well. These are the currently available emojis to react to with their unique role:\n\n"
						+ ""+sb.toString()).build()).queue(response -> {
							addReactions(response, e, reactionRoles, reactionEnabled, reactions);
						});
		}
		else {
			logger.error("Reaction roles couldn't be retrieved from DiscordRoles.roles in guild {}", e.getGuild().getId());
		}
	}
	
	@SuppressWarnings("preview")
	private static String getReaction(int counter) {
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
	
	private static void addReactions(Message message, GuildMessageReceivedEvent e, ArrayList<Roles> reactionRoles, boolean reactionEnabled, String [] reactions) {
		for(int i = 0; i < reactionRoles.size(); i++) {
			if(!reactionEnabled) {
				message.addReaction(EmojiManager.getForAlias(ReactionMessage.getReaction(i)).getUnicode()).queue();
			}
			else {
				if(reactions[i].length() > 0) {
					try {
						message.addReaction(e.getGuild().getEmotesByName(reactions[i], false).get(0)).queue();
					} catch(Exception exc) {
						message.addReaction(EmojiManager.getForAlias(":"+reactions[i]+":").getUnicode()).queue();
					}
				}
				else {
					message.addReaction(EmojiManager.getForAlias(ReactionMessage.getReaction(i)).getUnicode()).queue();
				}
			}
			if(i == 8) break;
		}
		Hashes.addReactionMessage(e.getGuild().getIdLong(), message.getIdLong());
	}
}
