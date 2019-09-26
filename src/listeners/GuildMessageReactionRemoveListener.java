package listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiParser;

import core.UserPrivs;
import fileManagement.GuildIni;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.DiscordRoles;
import util.STATIC;
import sql.Azrael;

public class GuildMessageReactionRemoveListener extends ListenerAdapter {
	private final static Logger logger = LoggerFactory.getLogger(GuildMessageReactionRemoveListener.class);
	
	@Override
	public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent e) {
		new Thread(() -> {
			if(!UserPrivs.isUserBot(e.getUser(), e.getGuild().getIdLong())) {
				if(!UserPrivs.isUserMuted(e.getUser(), e.getGuild().getIdLong())) {
					if(Azrael.SQLgetCommandExecutionReaction(e.getGuild().getIdLong())) {
						var reactionRoles = DiscordRoles.SQLgetReactionRoles(e.getGuild().getIdLong());
						if(reactionRoles != null && reactionRoles.size() > 0) {
							var rea_channel = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("rea")).findAny().orElse(null);
							String reactionName = "";
							if((EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":one:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":two:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":three:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":four:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":five:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":six:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":seven:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":eight:") || EmojiParser.parseToAliases(e.getReactionEmote().getName()).equals(":nine:")) && e.getChannel().getIdLong() == rea_channel.getChannel_ID()) {
								reactionName = EmojiParser.parseToAliases(e.getReactionEmote().getName()).replaceAll(":", "");
							}
							else if(rea_channel != null && e.getChannel().getIdLong() == rea_channel.getChannel_ID()) {
								reactionName = e.getReactionEmote().getName();
							}
							
							if(reactionName.length() > 0) {
								String [] reactions = GuildIni.getReactions(e.getGuild().getIdLong());
								boolean emoteFound = false;
								if(GuildIni.getReactionEnabled(e.getGuild().getIdLong())) {
									for(int i = 0; i < reactionRoles.size(); i++) {
										if(reactions[i].length() > 0 && (reactionName.equals(reactions[i]) || EmojiParser.parseToAliases(reactionName).replaceAll(":", "").equals(reactions[i]))) {
											e.getGuild().removeRoleFromMember(e.getMember(), e.getGuild().getRoleById(reactionRoles.get(i).getRole_ID())).queue();
											emoteFound = true;
											break;
										}
										if(i == 8) break;
									}
									if(emoteFound == false) {
										int emote = STATIC.returnEmote(reactionName);
										e.getGuild().removeRoleFromMember(e.getMember(), e.getGuild().getRoleById(reactionRoles.get(emote).getRole_ID())).queue();
									}
								}
								else {
									int emote = STATIC.returnEmote(reactionName);
									e.getGuild().removeRoleFromMember(e.getMember(), e.getGuild().getRoleById(reactionRoles.get(emote).getRole_ID())).queue();
								}
							}
							logger.debug("{} got a role removed upon reacting in guild {}", e.getUser().getId(), e.getGuild().getId());
						}
						else
							logger.error("Reaction roles couldn't be retrieved from DiscordRoles.roles in guild {}", e.getGuild().getId());
					}
				}
			}
		}).start();
	}
}
