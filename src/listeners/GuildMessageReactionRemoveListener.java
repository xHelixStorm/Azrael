package listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiParser;

import core.Hashes;
import core.UserPrivs;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sql.DiscordRoles;
import sql.Azrael;

public class GuildMessageReactionRemoveListener extends ListenerAdapter{
	
	@Override
	public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent e) {
		if(!UserPrivs.isUserBot(e.getUser(), e.getGuild().getIdLong())) {
			if(!UserPrivs.isUserMuted(e.getUser(), e.getGuild().getIdLong())) {
				if(Azrael.SQLgetCommandExecutionReaction(e.getGuild().getIdLong())) {
					Logger logger = LoggerFactory.getLogger(GuildMessageReactionRemoveListener.class);
					if(DiscordRoles.SQLgetRolesByCategory(e.getGuild().getIdLong(), "rea")) {
						var rea_channel = Azrael.SQLgetChannelID(e.getGuild().getIdLong(), "rea");
						String reactionName = "";
						if((EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":one:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":two:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":three:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":four:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":five:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":six:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":seven:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":eight:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":nine:")) && e.getChannel().getIdLong() == rea_channel) {
							reactionName = EmojiParser.parseToAliases(e.getReactionEmote().getName()).replaceAll(":", "");
						}
						else if(e.getChannel().getIdLong() == rea_channel) {
							reactionName = e.getReactionEmote().getName();
						}
						
						if(reactionName.length() > 0) {
							String [] reactions = IniFileReader.getReactions();
							boolean emoteFound = false;
							if(reactions[0].equals("true")) {
								for(int i = 1; i < 10; i++) {
									if(reactions[i].length() > 0 && (reactionName.equals(reactions[i]) || EmojiParser.parseToAliases(reactionName).replaceAll(":", "").equals(reactions[i]))) {
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
						logger.debug("{} got a role removed upon reacting in guild {}", e.getUser().getId(), e.getGuild().getName());
					}
					else
						logger.error("Reaction roles couldn't be retrieved from DiscordRoles.roles in guild {}", e.getGuild().getName());
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
