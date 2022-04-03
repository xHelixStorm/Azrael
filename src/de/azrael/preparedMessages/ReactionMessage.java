package de.azrael.preparedMessages;

import java.awt.Color;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiManager;

import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Roles;
import de.azrael.core.Hashes;
import de.azrael.enums.Channel;
import de.azrael.enums.Translation;
import de.azrael.sql.Azrael;
import de.azrael.sql.DiscordRoles;
import de.azrael.util.FileHandler;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class ReactionMessage {
	private final static Logger logger = LoggerFactory.getLogger(ReactionMessage.class);
	
	public static void print(GuildMessageReceivedEvent e, long channel_id, BotConfigs botConfig) {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
		var reactionRoles = DiscordRoles.SQLgetReactionRoles(e.getGuild().getIdLong());
		if(reactionRoles != null && reactionRoles.size() > 0) {
			TextChannel textChannel = e.getGuild().getTextChannelById(channel_id);
			if(textChannel != null) {
				if(e.getGuild().getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS) || STATIC.setPermissions(e.getGuild(), textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS))) {
					var roles = reactionRoles.parallelStream().filter(f -> !f.isPersistent()).collect(Collectors.toList());
					if(roles.size() > 0) {
						String [] reactions = botConfig.getReactionEmojis();
						StringBuilder sb = new StringBuilder();
						var reactionEnabled =  botConfig.isReactionsEnabled();
						for(int i = 0; i < roles.size(); i++) {
							String reaction;
							if(!reactionEnabled) {
								reaction = getReaction(i);
							}
							else {
								if(reactions[i] != null && reactions[i].length() > 0) {
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
							sb.append(reaction+" **"+roles.get(i).getRole_Name()+"**\n");
							if(i == 8) break;
						}
						String reactionMessage = FileHandler.readFile("./files/Guilds/"+e.getGuild().getId()+"/reactionmessage.txt");
						if(reactionMessage.length() > 0) {
							textChannel.sendMessage(message.setDescription(reactionMessage+"\n\n"
								+ ""+sb.toString()).build()).queue(response -> {
									addReactions(response, e, roles, reactionEnabled, reactions);
								});
						}
						
						else {
							textChannel.sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.ROLE_REACTION_PRINT)
								+ ""+sb.toString()).build()).queue(response -> {
									addReactions(response, e, roles, reactionEnabled, reactions);
								});
						}
					}
				}
				else {
					STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MESSAGE_WRITE.getName()+" and "+Permission.MESSAGE_EMBED_LINKS.getName())+textChannel.getAsMention(), Channel.LOG.getType());
					logger.error("MESSAGE_WRITE and MESSAGE_EMBED_LINKS permissions required to print a reaction message on channel {} in guild {}", channel_id, e.getGuild().getId());
				}
			}
			else {
				//remove not anymore existing channel
				if(Azrael.SQLDeleteChannelConf(channel_id, e.getGuild().getIdLong()) > 0) {
					Azrael.SQLDeleteChannel_Filter(channel_id);
					Azrael.SQLDeleteChannels(channel_id);
					logger.info("Reaction channel {} doesn't exist anymore in guild {}", channel_id, e.getGuild().getId());
					Hashes.removeFilterLang(channel_id);
					Hashes.removeChannels(e.getGuild().getIdLong());
				}
			}
		}
		else if(reactionRoles == null) {
			logger.error("Reaction roles couldn't be retrieved in guild {}", e.getGuild().getId());
		}
	}
	
	private static String getReaction(int counter) {
		return switch(counter) {
			case 0  -> ":one:";
			case 1  -> ":two:";
			case 2  -> ":three:";
			case 3  -> ":four:";
			case 4  -> ":five:";
			case 5  -> ":six:";
			case 6  -> ":seven:";
			case 7  -> ":eight:";
			default -> ":nine:";
		};
	}
	
	private static void addReactions(Message message, GuildMessageReceivedEvent e, List<Roles> reactionRoles, boolean reactionEnabled, String [] reactions) {
		if(e.getGuild().getSelfMember().hasPermission(message.getTextChannel(), Permission.MESSAGE_ADD_REACTION) || STATIC.setPermissions(e.getGuild(), message.getTextChannel(), EnumSet.of(Permission.MESSAGE_ADD_REACTION))) {
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
		}
		else {
			STATIC.writeToRemoteChannel(e.getGuild(), new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation2(e.getGuild(), Translation.EMBED_TITLE_PERMISSIONS)), STATIC.getTranslation2(e.getGuild(), Translation.MISSING_PERMISSION_IN).replace("{}", Permission.MESSAGE_ADD_REACTION.getName())+message.getTextChannel().getAsMention(), Channel.LOG.getType());
			logger.error("MESSAGE_ADD_REACTION permission required to add reactions to a message on channel {} in guild {}", message.getTextChannel().getId(), e.getGuild().getId());
		}
		Hashes.addReactionMessage(e.getGuild().getIdLong(), message.getIdLong());
	}
}
