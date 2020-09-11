package commandsContainer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import core.Hashes;
import enums.Translation;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.DiscordRoles;
import util.STATIC;

/**
 * Addition to the edit command
 * @author xHelixStorm
 *
 */

public class WriteEditExecution {
	private final static Logger logger = LoggerFactory.getLogger(WriteEditExecution.class);
	
	public static void writeHelp(GuildMessageReceivedEvent e, Cache cache) {
		e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.WRITE_UPDATE)).build()).queue();
		cache.updateDescription("WE");
		Hashes.addTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache);
	}
	
	public static void runWrite(GuildMessageReceivedEvent e, Cache cache, String message) {
		if(message.length() > 0) {
			if(message.length() <= 2000) {
				TextChannel textChannel = e.getGuild().getTextChannelById(cache.getAdditionalInfo2());
				if(e.getGuild().getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES) || STATIC.setPermissions(e.getGuild(), textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES))) {
					e.getGuild().getTextChannelById(cache.getAdditionalInfo2()).sendMessage(message).queue(success -> {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.WRITE_SENT)).build()).queue();
						Hashes.clearTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					});
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
	}
	
	public static void editHelp(GuildMessageReceivedEvent e, Cache cache) {
		e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_UPDATE)).build()).queue();
		cache.updateDescription("EE");
		Hashes.addTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache);
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
	}
	
	public static void reactionAddHelp(GuildMessageReceivedEvent e, Cache cache) {
		e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_REACTION_ADD_HELP)).build()).queue(message -> {
			Hashes.addTempCache("write_edit_reaction_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId()+"me"+message.getId(), new Cache(180000, cache.getAdditionalInfo2(), cache.getAdditionalInfo3()));
			Hashes.clearTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
		});
	}
	
	public static void reactionAnswer(GuildMessageReceivedEvent e, Cache cache, String message) {
		if(message.equals("yes")) {
			int count = 0;
			ArrayList<Long> roles = new ArrayList<Long>();
			StringBuilder out = new StringBuilder();
			for(final var role : e.getGuild().getRoles()) {
				if(e.getGuild().getSelfMember().canInteract(role) && !role.getName().equals("@everyone")) {
					out.append(++count+": "+role.getName()+" ("+role.getId()+")\n");
					roles.add(role.getIdLong());
				}
			}
			if(roles.size() > 0) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_SELECT_ROLE)+out.toString()).build()).queue();
				Hashes.addTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.updateDescription("RA2").setExpiration(180000).setObject(roles));
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_NO_ROLES)).build()).queue();
				Hashes.clearTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
			}
		}
		else if(message.equals("no")) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_COMPLETE)).build()).queue();
			Hashes.clearTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
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
					logger.debug("The user {} has set a reaction role with the role id {} on message id in guild {}", e.getMember().getUser().getId(), role_id, cache.getAdditionalInfo2(), e.getGuild().getId());
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
					logger.error("Reaction role couldn't be inserted into DiscordRoles.reactions with message id {}, emoji {} and role id {} in guild {}", cache.getAdditionalInfo2(), cache.getAdditionalInfo3(), role_id, e.getGuild().getId());
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_NO_NUMBER)).build()).queue();
				Hashes.addTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
			}
		}
	}
	
	public static void runClearReactions(GuildMessageReceivedEvent e, Cache cache) {
		e.getGuild().getTextChannelById(cache.getAdditionalInfo2()).retrieveMessageById(cache.getAdditionalInfo3()).queue(message -> {
			TextChannel textChannel = e.getGuild().getTextChannelById(cache.getAdditionalInfo2());
			if(e.getGuild().getSelfMember().hasPermission(textChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_MANAGE) || STATIC.setPermissions(e.getGuild(), textChannel, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_MANAGE))) {
				message.clearReactions().queue(success -> {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.EDIT_REACTIONS_CLEAR)).build()).queue();
					Hashes.clearTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					DiscordRoles.SQLDeleteReactions(Long.parseLong(cache.getAdditionalInfo3()));
					logger.debug("The user {} has cleared all reactions from message id {} in guild {}", e.getMember().getUser().getId(), cache.getAdditionalInfo3(), e.getGuild().getId());
				});
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.MESSAGE_MANAGE.getName()).build()).queue();
				logger.error("MANAGE_MESSAGES permission required for channel {} in guild {} to remove reactions from a message", cache.getAdditionalInfo2(), e.getGuild().getId());
			}
		});
	}
}
