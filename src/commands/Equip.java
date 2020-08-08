package commands;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import core.Hashes;
import core.UserPrivs;
import enums.Translation;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import interfaces.CommandPrivate;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import sql.RankingSystem;
import util.STATIC;

/**
 * The Equip command will allow a user to equip weapons
 * and skills in private message. 
 * @author xHelixStorm
 *
 */

public class Equip implements CommandPublic, CommandPrivate {
	private final static Logger logger = LoggerFactory.getLogger(Equip.class);
	
	//PUBLIC COMMAND SECTION START
	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		//check if the command is enabled
		if(GuildIni.getEquipCommand(e.getGuild().getIdLong()))
			return true;
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		//print message to write the command in private message
		e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.EQUIP_WRONG_CHANNEL).replace("{}", e.getGuild().getSelfMember().getAsMention())).build()).queue();
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.debug("{} has used Equip command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}

	//PRIVATE COMMAND SECTION START
	@Override
	public boolean called(String[] args, PrivateMessageReceivedEvent e) {
		//private message is always enabled
		return true;
	}

	@Override
	public void action(String[] args, PrivateMessageReceivedEvent e) {
		//save mutual guilds into an array where the ranking state is enabled
		List<Guild> mutualGuilds = e.getAuthor().getMutualGuilds().parallelStream().filter(f -> RankingSystem.SQLgetGuild(f.getIdLong()).getRankingState() && GuildIni.getEquipCommand(f.getIdLong())).collect(Collectors.toList());
		//If there's at least 1 guild with an enabled ranking state, proceed!
		if(mutualGuilds != null && mutualGuilds.size() > 0) {
			//if only one guild has been found
			if(mutualGuilds.size() == 1) {
				//check if the user is allowed to use this command
				if(UserPrivs.comparePrivilege(e.getJDA().getGuildById(mutualGuilds.get(0).getId()).getMemberById(e.getAuthor().getId()), GuildIni.getEquipLevel(mutualGuilds.get(0).getIdLong()))) {
					//directly make the selection screen appear (e.g. show, set, etc)
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation3(e.getAuthor(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_HELP)).build()).queue();
					Hashes.addTempCache("equip_us"+e.getAuthor().getId(), new Cache(180000, mutualGuilds.get(0).getId()));
				}
				else {
					EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setTitle(STATIC.getTranslation3(e.getAuthor(), Translation.EMBED_TITLE_DENIED));
					e.getChannel().sendMessage(denied.setDescription(e.getAuthor().getAsMention()+STATIC.getTranslation3(e.getAuthor(), Translation.HIGHER_PRIVILEGES_ROLE)+UserPrivs.retrieveRequiredRoles(GuildIni.getEquipLevel(mutualGuilds.get(0).getIdLong()), mutualGuilds.get(0).getMemberById(e.getAuthor().getIdLong()))).build()).queue();
				}
			}
			//if multiple guilds have been found
			else {
				//show the guild selection screen and then go over to the action selection screen
				StringBuilder out = new StringBuilder();
				var guilds = "";
				var i = 1;
				for(final var guild : mutualGuilds) {
					if(UserPrivs.comparePrivilege(guild.getMemberById(e.getAuthor().getId()), GuildIni.getEquipLevel(guild.getIdLong()))) {
						out.append("**"+i+": "+guild.getName()+" ("+guild.getId()+")**\n");
					}
					else {
						out.append("**"+i+": "+guild.getName()+" ("+guild.getId()+") "+STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_DENIED)+"**\n");
					}
					if(i != mutualGuilds.size())
						guilds += guild.getId()+"-";
					else
						guilds += guild.getId();
					i++;
				}
				final var printMessage = STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_SERVER_SELECT)+out.toString();
				if(printMessage.length() <= 2048) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(printMessage).build()).queue();
					Hashes.addTempCache("equip_us"+e.getAuthor().getId(), new Cache(180000, guilds, "wait"));
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation3(e.getAuthor(), Translation.EQUIP_SERVER_SELECT_2)).build()).queue();
					Hashes.addTempCache("equip_us"+e.getAuthor().getId(), new Cache(180000, guilds, "err"));
				}
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(e.getAuthor().getAsMention()+STATIC.getTranslation3(e.getAuthor(), Translation.LEVEL_SYSTEM_NOT_ENABLED)).build()).queue();
		}
	}

	@Override
	public void executed(boolean success, PrivateMessageReceivedEvent e) {
		logger.trace("{} has used Equip command in private message", e.getAuthor().getId());
	}

}
