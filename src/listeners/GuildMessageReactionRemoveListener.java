package listeners;

import com.vdurmont.emoji.EmojiParser;

import core.Hashes;
import core.UserPrivs;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.ServerRoles;
import sql.SqlConnect;

public class GuildMessageReactionRemoveListener extends ListenerAdapter{
	
	@Override
	public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent e) {
		if(!UserPrivs.isUserBot(e.getUser(), e.getGuild().getIdLong())) {
			if(!UserPrivs.isUserMuted(e.getUser(), e.getGuild().getIdLong())) {
				ServerRoles.SQLgetRolesByCategory(e.getGuild().getIdLong(), "rea");
				SqlConnect.SQLgetChannelID(e.getGuild().getIdLong(), "rea");
				String reactionName = "";
				if((EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":one:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":two:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":three:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":four:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":five:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":six:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":seven:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":eight:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":nine:")) && e.getChannel().getIdLong() == SqlConnect.getChannelID()) {
					reactionName = EmojiParser.parseToAliases(e.getReactionEmote().getName()).replaceAll(":", "");
				}
				else if(e.getChannel().getIdLong() == SqlConnect.getChannelID()) {
					reactionName = e.getReactionEmote().getName();
				}
				
				if(reactionName.length() > 0) {
					String [] reactions = IniFileReader.getReactions();
					boolean emoteFound = false;
					if(reactions[0].equals("true")) {
						for(int i = 1; i < 10; i++) {
							if(reactions[i].length() > 0 && reactionName.equals(reactions[i])) {
								e.getGuild().getController().removeSingleRoleFromMember(e.getMember(), e.getGuild().getRoleById(Hashes.getRoles(i+"_"+e.getGuild().getId()).getRole_ID())).queue();
								emoteFound = true;
								break;
							}
						}
						if(emoteFound == false) {
							int emote = returnEmote(reactionName);
							e.getGuild().getController().removeSingleRoleFromMember(e.getMember(), e.getGuild().getRoleById(Hashes.getRoles(emote+"_"+e.getGuild().getId()).getRole_ID())).queue();
						}
					}
					else {
						int emote = returnEmote(reactionName);
						e.getGuild().getController().removeSingleRoleFromMember(e.getMember(), e.getGuild().getRoleById(Hashes.getRoles(emote+"_"+e.getGuild().getId()).getRole_ID())).queue();
					}
				}
				
			}
		}
	}
	
	private int returnEmote(String reactionName) {
		switch(reactionName) {
			case "one":
				return 1;
			case "two":
				return 2;
			case "three":
				return 3;
			case "four":
				return 4;
			case "five":
				return 5;
			case "six":
				return 6;
			case "seven":
				return 7;
			case "eight":
				return 8;
			case "nine":
				return 9;
			default:
				return 0;
		}
	}
}
