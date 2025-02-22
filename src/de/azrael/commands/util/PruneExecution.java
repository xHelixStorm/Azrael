package de.azrael.commands.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.commands.Prune;
import de.azrael.constructors.Cache;
import de.azrael.constructors.GuildPrune;
import de.azrael.constructors.PruneManagement;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.sql.Azrael;
import de.azrael.util.Hashes;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PruneExecution {
	private final static Logger logger = LoggerFactory.getLogger(PruneExecution.class);
	
	public static void runTask(MessageReceivedEvent e, Cache cache) {
		if(e.getGuild().getSelfMember().hasPermission(Permission.KICK_MEMBERS)) {
			Thread.currentThread().setName("prune_gu"+e.getGuild().getId());
			STATIC.addThread(Thread.currentThread(), "prune_gu"+e.getGuild().getId());
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.PRUNE.getColumn(), e.getMessage().getContentRaw());
			final GuildPrune prune = (GuildPrune)cache.getObject();
			final List<Member> kickMembers = prune.getKickMembers();
			final List<Member> excludedMembers = prune.getExcludedMembes();
			final List<Member> kickedMembers = new ArrayList<Member>();
			Prune.pruneStatus.put(e.getGuild().getIdLong(), new PruneManagement(kickMembers.size()));
			long totalMembers = 0;
			long kicked = 0;
			long excluded = 0;
			long err = 0;
			
			Azrael.SQLInsertActionLog("PRUNE", 0, e.getGuild().getIdLong(), "Prune command used");
			for(final Member member : kickMembers) {
				if(!excludedMembers.contains(member)) {
					if(e.getGuild().getMember(member.getUser()) != null && e.getGuild().getSelfMember().canInteract(member)) {
						Hashes.addTempCache("kick-ignore_gu"+e.getGuild().getId()+"us"+member.getUser().getId(), cache);
						e.getGuild().kick(member).reason("Prune").queue(success -> {}, error -> {/*If the command has been used multiple times, ignore in case of failure (e.g. permission changes)*/});
						kickedMembers.add(member);
						kicked++;
					}
					else {
						err++;
					}
				}
				else {
					excluded ++;
				}
				totalMembers ++;
				Prune.pruneStatus.put(e.getGuild().getIdLong(), Prune.pruneStatus.get(e.getGuild().getIdLong()).incrementKickCount());
			}
			
			if(kickedMembers.size() > 0) {
				//Log kicked members into history
				Azrael.SQLBulkInsertHistory(kickedMembers, e.getGuild().getIdLong(), "kick", "Prune", 0, e.getMember().getUser().getName()+"#"+e.getMember().getUser().getDiscriminator());
			}
			
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.PRUNE_COMPLETE).replaceFirst("\\{\\}", ""+totalMembers).replaceFirst("\\{\\}", ""+kicked).replace("\\{\\}", ""+excluded).replace("{}", ""+err)).build()).queue();
			STATIC.removeThread(Thread.currentThread());
		}
		else {
			e.getChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PERMISSIONS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.MISSING_PERMISSION)+Permission.KICK_MEMBERS.getName()).build()).queue();
			logger.error("KICK_MEMBERS permissions required to use the prune command in guild {}", e.getGuild().getId());
		}
	}
}
