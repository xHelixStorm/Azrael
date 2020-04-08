package commandsContainer;

import java.awt.Color;

import constructors.Cache;
import core.Hashes;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class WriteEditExecution {
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
}
