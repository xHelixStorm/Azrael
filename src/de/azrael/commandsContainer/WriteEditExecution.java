package de.azrael.commandsContainer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Cache;
import de.azrael.core.Hashes;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.sql.Azrael;
import de.azrael.sql.DiscordRoles;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * Addition to the edit command
 * @author xHelixStorm
 *
 */

public class WriteEditExecution {
	private final static Logger logger = LoggerFactory.getLogger(WriteEditExecution.class);
	
	public static void writeHelp(GuildMessageReceivedEvent e, String channelId, String delay) {
		e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.WRITE_UPDATE)).build()).queue();
		Hashes.addTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "WE", channelId, delay));
	}
	
	public static void runWrite(GuildMessageReceivedEvent e, Cache cache, String message) {
		if(message.length() > 0) {
			if(message.length() <= 2000) {
				TextChannel textChannel = e.getGuild().getTextChannelById(cache.getAdditionalInfo2());
				if(e.getGuild().getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES) || STATIC.setPermissions(e.getGuild(), textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES))) {
					final String delay = cache.getAdditionalInfo3();
					if(delay == null) {
						e.getGuild().getTextChannelById(cache.getAdditionalInfo2()).sendMessage(message).queue(success -> {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.WRITE_SENT)).build()).queue();
						});
					}
					else {
						e.getGuild().getTextChannelById(cache.getAdditionalInfo2()).sendMessage(message).queueAfter(Long.parseLong(delay), TimeUnit.MINUTES, success -> {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.WRITE_SENT)).build()).queue();
						});
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.WRITE_DELAYED).replace("{}", delay)).build()).queue();
					}
					Hashes.clearTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MESSAGE_WRITE.getName()).build()).queue();
					Hashes.clearTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.WRITE_TOO_LONG)).build()).queue();
				cache.setExpiration(180000);
				Hashes.addTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache);
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.WRITE_NO_SCREENS)).build()).queue();
			cache.setExpiration(180000);
			Hashes.addTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache);
		}
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.WRITE.getColumn(), e.getMessage().getContentRaw());
	}
	
	public static void editHelp(GuildMessageReceivedEvent e, String channelId, String messageId) {
		e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_UPDATE)).build()).queue();
		Hashes.addTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000, "EE", channelId, messageId));
	}
	
	public static void runEdit(GuildMessageReceivedEvent e, Cache cache, String message) {
		if(message.length() > 0) {
			if(message.length() <= 2000) {
				TextChannel textChannel = e.getGuild().getTextChannelById(cache.getAdditionalInfo2());
				if(e.getGuild().getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES) || STATIC.setPermissions(e.getGuild(), textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES))) {
					if(e.getGuild().getSelfMember().hasPermission(e.getGuild().getTextChannelById(cache.getAdditionalInfo2()), Permission.MESSAGE_HISTORY) || STATIC.setPermissions(e.getGuild(), textChannel, EnumSet.of(Permission.MESSAGE_HISTORY))) {
						e.getGuild().getTextChannelById(cache.getAdditionalInfo2()).retrieveMessageById(cache.getAdditionalInfo3()).queue(m -> {
							if(m.getAuthor().getIdLong() == e.getGuild().getSelfMember().getIdLong()) {
								m.editMessage(message).queue(success -> {
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_UPDATED)).build()).queue();
									Hashes.clearTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
								});
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_NOT_BOT)).build()).queue();
								Hashes.clearTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
							}
						}, error -> {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_NOT_EXISTS)).build()).queue();
							Hashes.clearTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
						});
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MESSAGE_HISTORY.getName()).build()).queue();
						Hashes.clearTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MESSAGE_WRITE.getName()).build()).queue();
					Hashes.clearTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.WRITE_TOO_LONG)).build()).queue();
				cache.setExpiration(180000);
				Hashes.addTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache);
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.WRITE_NO_SCREENS)).build()).queue();
			cache.setExpiration(180000);
			Hashes.addTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache);
		}
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.EDIT.getColumn(), e.getMessage().getContentRaw());
	}
	
	public static void reactionAddHelp(GuildMessageReceivedEvent e, String channelId, String messageId) {
		e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_REACTION_ADD_HELP)).build()).queue(message -> {
			Hashes.addTempCache("write_edit_reaction_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId()+"me"+message.getId(), new Cache(180000, channelId, messageId));
		});
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.EDIT.getColumn(), e.getMessage().getContentRaw());
	}
	
	public static void reactionAnswer(GuildMessageReceivedEvent e, Cache cache, String message) {
		if(message.equals("yes")) {
			int count = 0;
			ArrayList<Long> roles = new ArrayList<Long>();
			ArrayList<String> allRoles = new ArrayList<String>();
			StringBuilder out = new StringBuilder();
			int breaker = 10;
			for(final var role : e.getGuild().getRoles()) {
				if(e.getGuild().getSelfMember().canInteract(role) && !role.getName().equals("@everyone")) {
					if(count < breaker)
						out.append("**"+(count+1)+"**: "+role.getName()+" ("+role.getId()+")\n");
					allRoles.add("**"+(count+1)+"**: "+role.getName()+" ("+role.getId()+")");
					roles.add(role.getIdLong());
					count++;
				}
			}
			if(roles.size() > 0) {
				final String appendMessage = STATIC.getTranslation(e.getMember(), Translation.EDIT_SELECT_ROLE);
				final int maxPage = (allRoles.size()/breaker)+(allRoles.size()%breaker > 0 ? 1 : 0);
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setFooter("1/"+maxPage).setDescription(appendMessage+out.toString()).build()).queue(m -> {
					STATIC.addPaginationReactions(e, m, maxPage, "3", ""+breaker, allRoles, appendMessage);
				});
				Hashes.addTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.updateDescription("RA2").setExpiration(180000).setObject(roles));
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_NO_ROLES)).build()).queue();
				Hashes.clearTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
			}
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.EDIT.getColumn(), e.getMessage().getContentRaw());
		}
		else if(message.equals("no")) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_COMPLETE)).build()).queue();
			Hashes.clearTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.EDIT.getColumn(), e.getMessage().getContentRaw());
		}
	}
	
	public static void reactionBindRole(GuildMessageReceivedEvent e, Cache cache, String message) {
		if(message.replaceAll("[0-9]*", "").length() == 0) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			ArrayList<Long> roles = (ArrayList) cache.getObject();
			int input = Integer.parseInt(message);
			if(input > 0 && input <= roles.size()) {
				long role_id = roles.get(input-1);
				if(DiscordRoles.SQLInsertReaction(Long.parseLong(cache.getAdditionalInfo2()), cache.getAdditionalInfo3(), role_id) > 0) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_COMPLETE)).build()).queue();
					logger.info("User {} has added the reaction role {} on message {} and emoji {} as reaction in guild {}", e.getMember().getUser().getId(), role_id, cache.getAdditionalInfo2(), cache.getAdditionalInfo3(), e.getGuild().getId());
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Reaction role couldn't be set to message {}, emoji {} and role {} in guild {}", cache.getAdditionalInfo2(), cache.getAdditionalInfo3(), role_id, e.getGuild().getId());
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_NO_NUMBER)).build()).queue();
				Hashes.addTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
			}
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.EDIT.getColumn(), e.getMessage().getContentRaw());
		}
	}
	
	public static void runClearReactions(GuildMessageReceivedEvent e, String channelId, String messageId) {
		final TextChannel textChannel = e.getGuild().getTextChannelById(channelId);
		textChannel.retrieveMessageById(messageId).queue(message -> {
			if(e.getGuild().getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_MANAGE) || STATIC.setPermissions(e.getGuild(), textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_MANAGE))) {
				message.clearReactions().queue(success -> {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_REACTIONS_CLEAR)).build()).queue();
					Hashes.clearTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					DiscordRoles.SQLDeleteReactions(Long.parseLong(messageId));
					logger.info("User {} has cleared all reactions and roles from message {} in guild {}", e.getMember().getUser().getId(), messageId, e.getGuild().getId());
				});
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MESSAGE_MANAGE.getName()).build()).queue();
				logger.error("MANAGE_MESSAGES permission required for channel {} to remove reactions from a message in guild {}", channelId, e.getGuild().getId());
			}
		});
		Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.EDIT.getColumn(), e.getMessage().getContentRaw());
	}
}
