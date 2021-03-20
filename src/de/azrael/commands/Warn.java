package de.azrael.commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.core.UserPrivs;
import de.azrael.enums.Translation;
import de.azrael.fileManagement.GuildIni;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Warn implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Warn.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		//check if the command is enabled and that the user has enough permissions
		if(GuildIni.getWarnCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getWarnLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		if(args.length == 0) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.WARN_HELP)).build()).queue();
		}
		else if(args.length > 1) {
			String user_id = args[0].replaceAll("[^0-9]*", "");
			if(user_id.length() > 0) {
				Member member = e.getGuild().getMemberById(user_id);
				if(member != null) {
					if(member.getUser().getIdLong() != e.getMember().getUser().getIdLong()) {
						if(e.getGuild().getSelfMember().canInteract(member)) {
							StringBuilder out = new StringBuilder();
							for(int i = 1; i < args.length; i++) {
								out.append(args[i]+" ");
							}
							final String reason = out.toString().trim();
							member.getUser().openPrivateChannel().queue(channel -> {
								channel.sendMessage(new EmbedBuilder().setColor(Color.ORANGE).setDescription(STATIC.getTranslation2(e.getGuild(), Translation.WARN_SENT).replaceFirst("\\{\\}", reason).replace("{}", e.getGuild().getName())).build()).queue(success -> {
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.WARN_SENT_2).replace("{}", member.getUser().getName()+"#"+member.getUser().getDiscriminator())).build()).queue();
									Azrael.SQLInsertHistory(member.getUser().getIdLong(), e.getGuild().getIdLong(), "warning", reason, 0, e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator());
									logger.info("User {} has sent a warning message to user {} in guild {}", e.getMember().getUser().getId(), member.getUser().getId(), e.getGuild().getId());
								}, err -> {
									e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.WARN_ERR).replace("{}", member.getUser().getName()+"#"+member.getUser().getDiscriminator())).build()).queue();
								});
							});
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.WARN_ERR_2)).build()).queue();
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.WARN_ERR_3)).build()).queue();
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.WARN_ERR_4)).build()).queue();
				}
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.WARN_ERR_4)).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.trace("{} has used Warn command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}

}
