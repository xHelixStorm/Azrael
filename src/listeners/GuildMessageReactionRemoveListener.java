package listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiParser;

import core.Hashes;
import core.UserPrivs;
import fileManagement.GuildIni;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
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
						var rea_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("rea")).findAny().orElse(null);
						String reactionName = "";
						if((EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":one:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":two:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":three:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":four:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":five:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":six:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":seven:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":eight:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":nine:")) && e.getChannel().getIdLong() == rea_channel.getChannel_ID()) {
							reactionName = EmojiParser.parseToAliases(e.getReactionEmote().getName()).replaceAll(":", "");
						}
						else if(e.getChannel().getIdLong() == rea_channel.getChannel_ID()) {
							reactionName = e.getReactionEmote().getName();
						}
						
						if(reactionName.length() > 0) {
							String [] reactions = GuildIni.getReactions(e.getGuild().getIdLong());
							boolean emoteFound = false;
							if(reactions[0].equals("true")) {
								for(int i = 1; i < 10; i++) {
									if(reactions[i].length() > 0 && (reactionName.equals(reactions[i]) || EmojiParser.parseToAliases(reactionName).replaceAll(":", "").equals(reactions[i]))) {
										e.getGuild().removeRoleFromMember(e.getMember(), e.getGuild().getRoleById(Hashes.getRoles(i+"_"+e.getGuild().getId()).getRole_ID())).queue();
										emoteFound = true;
										break;
									}
								}
								if(emoteFound == false) {
									int emote = returnEmote(reactionName);
									e.getGuild().removeRoleFromMember(e.getMember(), e.getGuild().getRoleById(Hashes.getRoles(emote+"_"+e.getGuild().getId()).getRole_ID())).queue();
								}
							}
							else {
								int emote = returnEmote(reactionName);
								e.getGuild().removeRoleFromMember(e.getMember(), e.getGuild().getRoleById(Hashes.getRoles(emote+"_"+e.getGuild().getId()).getRole_ID())).queue();
							}
						}
						logger.debug("{} got a role removed upon reacting in guild {}", e.getUser().getId(), e.getGuild().getId());
					}
					else
						logger.error("Reaction roles couldn't be retrieved from DiscordRoles.roles in guild {}", e.getGuild().getId());
				}
			}
		}
	}
	
	@SuppressWarnings("preview")
	private int returnEmote(String reactionName) {
		return switch(reactionName) {
			case "one" 	 -> 1;
			case "two"   -> 2;
			case "three" -> 3;
			case "four"  -> 4;
			case "five"  -> 5;
			case "six" 	 -> 6;
			case "seven" -> 7;
			case "eight" -> 8;
			case "nine"  -> 9;
			default 	 -> 0;
		};
	}
}
