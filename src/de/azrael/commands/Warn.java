package de.azrael.commands;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Warn implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Warn.class);

	@Override
	public boolean called(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.WARN);
	}

	@Override
	public boolean action(String[] args, MessageReceivedEvent e, BotConfigs botConfig) {
		if(args.length == 0) {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.WARN_HELP)).build()).queue();
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
								channel.sendMessageEmbeds(new EmbedBuilder().setColor(Color.ORANGE).setDescription(STATIC.getTranslation2(e.getGuild(), Translation.WARN_SENT).replaceFirst("\\{\\}", e.getGuild().getName()).replace("{}", reason)).build()).queue(success -> {
									e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.WARN_SENT_2).replace("{}", member.getUser().getName()+"#"+member.getUser().getDiscriminator())).build()).queue();
									Azrael.SQLInsertHistory(member.getUser().getIdLong(), e.getGuild().getIdLong(), "warning", reason, 0, e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator());
									logger.info("User {} has sent a warning message to user {} in guild {}", e.getMember().getUser().getId(), member.getUser().getId(), e.getGuild().getId());
								}, err -> {
									e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.WARN_ERR).replace("{}", member.getUser().getName()+"#"+member.getUser().getDiscriminator())).build()).queue();
								});
							});
						}
						else {
							e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.WARN_ERR_2)).build()).queue();
						}
					}
					else {
						e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.WARN_ERR_3)).build()).queue();
					}
				}
				else {
					e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.WARN_ERR_4)).build()).queue();
				}
			}
			else {
				e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.WARN_ERR_4)).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
		}
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, MessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Warn command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.WARN.getColumn(), out.toString().trim());
		}
	}

}
