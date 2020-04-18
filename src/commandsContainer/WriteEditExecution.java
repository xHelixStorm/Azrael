package commandsContainer;

import java.awt.Color;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import core.Hashes;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.DiscordRoles;

public class WriteEditExecution {
	private final static Logger logger = LoggerFactory.getLogger(WriteEditExecution.class);
	
	public static void writeHelp(GuildMessageReceivedEvent e, Cache cache) {
		e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription("Now please submit the message to post!").build()).queue();
		cache.updateDescription("WE");
		Hashes.addTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache);
	}
	
	public static void runWrite(GuildMessageReceivedEvent e, Cache cache, String message) {
		if(message.length() > 0) {
			if(message.length() <= 2000) {
				if(e.getGuild().getSelfMember().hasPermission(e.getGuild().getTextChannelById(cache.getAdditionalInfo2()), Permission.MESSAGE_WRITE)) {
					e.getGuild().getTextChannelById(cache.getAdditionalInfo2()).sendMessage(message).queue(success -> {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription("Message sent!").build()).queue();
						Hashes.clearTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					});
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Write message permission required to write in this channel!").build()).queue();
					Hashes.clearTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Message can't be longer than 2000 letters!").build()).queue();
				cache.setExpiration(180000);
				Hashes.addTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache);
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Screenshots are not allowed!").build()).queue();
			cache.setExpiration(180000);
			Hashes.addTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache);
		}
	}
	
	public static void editHelp(GuildMessageReceivedEvent e, Cache cache) {
		e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription("Now please submit the message to update the existing message!").build()).queue();
		cache.updateDescription("EE");
		Hashes.addTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache);
	}
	
	public static void runEdit(GuildMessageReceivedEvent e, Cache cache, String message) {
		if(message.length() > 0) {
			if(message.length() <= 2000) {
				if(e.getGuild().getSelfMember().hasPermission(e.getGuild().getTextChannelById(cache.getAdditionalInfo2()), Permission.MESSAGE_WRITE)) {
					if(e.getGuild().getSelfMember().hasPermission(e.getGuild().getTextChannelById(cache.getAdditionalInfo2()), Permission.MESSAGE_HISTORY)) {
						e.getGuild().getTextChannelById(cache.getAdditionalInfo2()).retrieveMessageById(cache.getAdditionalInfo3()).queue(m -> {
							m.editMessage(message).queue(success -> {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription("Message successfully updated!").build()).queue();
								Hashes.clearTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
							});
						}, error -> {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Message doesn't exist or is too old!").build()).queue();
							Hashes.clearTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
						});
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Read message history permission required to look up for the existing message in this channel!").build()).queue();
						Hashes.clearTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Write message permission required to write in this channel!").build()).queue();
					Hashes.clearTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Message can't be longer than 2000 letters!").build()).queue();
				cache.setExpiration(180000);
				Hashes.addTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache);
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Screenshots are not allowed!").build()).queue();
			cache.setExpiration(180000);
			Hashes.addTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache);
		}
	}
	
	public static void reactionAddHelp(GuildMessageReceivedEvent e, Cache cache) {
		e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription("Now please add a reaction to this message to add it to the directed message!").build()).queue(message -> {
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
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription("These are the roles which the bot is able to assign. Select the digit for the role you wish to assign with a reaction:\n\n"+out.toString()).build()).queue();
				Hashes.addTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.updateDescription("RA2").setExpiration(180000).setObject(roles));
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("There are no available roles that the bot could assign!").build()).queue();
				Hashes.clearTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
			}
		}
		else if(message.equals("no")) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription("Action complete!").build()).queue();
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
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription("Action complete!").build()).queue();
					logger.debug("The user {} has set a reaction role with the role id {} on message id in guild {}", e.getMember().getUser().getId(), role_id, cache.getAdditionalInfo2(), e.getGuild().getId());
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Error!").setDescription("An internal error occurred! Reaction role couldn't be set. Please contact an administrator!").build()).queue();
					logger.error("Reaction role couldn't be inserted into DiscordRoles.reactions with message id {}, emoji {} and role id {} in guild {}", cache.getAdditionalInfo2(), cache.getAdditionalInfo3(), role_id, e.getGuild().getId());
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Please select one of the available digits!").build()).queue();
				Hashes.addTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), cache.setExpiration(180000));
			}
		}
	}
	
	public static void runClearReactions(GuildMessageReceivedEvent e, Cache cache) {
		e.getGuild().getTextChannelById(cache.getAdditionalInfo2()).retrieveMessageById(cache.getAdditionalInfo3()).queue(message -> {
			if(e.getGuild().getSelfMember().hasPermission(e.getGuild().getTextChannelById(cache.getAdditionalInfo2()), Permission.MESSAGE_MANAGE)) {
				message.clearReactions().queue(success -> {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription("Reactions have been removed from this message!").build()).queue();
					Hashes.clearTempCache("write_edit_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId());
					DiscordRoles.SQLDeleteReactions(Long.parseLong(cache.getAdditionalInfo3()));
					logger.debug("The user {} has cleared all reactions from message id {} in guild {}", e.getMember().getUser().getId(), cache.getAdditionalInfo3(), e.getGuild().getId());
				});
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Missing Permission!").setDescription("MANAGE_MESSAGES permission required to remove all reactions from a message for this channel! Action aborted!").build()).queue();
				logger.error("MANAGE_MESSAGES permission required for channel {} in guild {} to remove reactions from a message", cache.getAdditionalInfo2(), e.getGuild().getId());
			}
		});
	}
}
