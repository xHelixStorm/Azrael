package commands;

import java.awt.Color;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commandsContainer.RandomshopExecution;
import constructors.Guilds;
import core.Hashes;
import core.UserPrivs;
import enums.Translation;
import fileManagement.GuildIni;
import interfaces.CommandPublic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;
import sql.RankingSystem;
import sql.RankingSystemItems;
import util.STATIC;

/**
 * Win weapons through the randomshop with various stats
 * @author xHelixStorm
 *
 */

public class Randomshop implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Randomshop.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e) {
		if(GuildIni.getRandomshopCommand(e.getGuild().getIdLong())) {
			final var commandLevel = GuildIni.getRandomshopLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), commandLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong())
				return true;
			else
				UserPrivs.throwNotEnoughPrivilegeError(e, commandLevel);
		}
		return false;
	}

	@Override
	public void action(String[] args, GuildMessageReceivedEvent e) {
		var bot_channels = Azrael.SQLgetChannels(e.getGuild().getIdLong()).parallelStream().filter(f -> f.getChannel_Type() != null && f.getChannel_Type().equals("bot")).collect(Collectors.toList());
		if(bot_channels.size() == 0 || bot_channels.parallelStream().filter(f -> f.getChannel_ID() == e.getChannel().getIdLong()).findAny().orElse(null) != null) {
			Guilds guild_settings = RankingSystem.SQLgetGuild(e.getGuild().getIdLong());
			if(args.length == 0) {
				//run help and collect all possible parameters
				RandomshopExecution.runHelp(e, RankingSystemItems.SQLgetWeaponAbbvs(e.getGuild().getIdLong(), guild_settings.getThemeID()), RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong(), guild_settings.getThemeID(), false));
			}
			else if(args.length > 1 && args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_PLAY))) {
				//start a round
				RandomshopExecution.runRound(e, RankingSystemItems.SQLgetWeaponAbbvs(e.getGuild().getIdLong(), guild_settings.getThemeID()), RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong(), guild_settings.getThemeID(), false), bundleArguments(args, 1));
			}
			else if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REPLAY))) {
				//play another round if a match occurred within 3 minutes
				var cache = Hashes.getTempCache("randomshop_play_"+e.getMember().getUser().getId());
				if(cache != null && cache.getExpiration() - System.currentTimeMillis() > 0) {
					RandomshopExecution.runRound(e, RankingSystemItems.SQLgetWeaponAbbvs(e.getGuild().getIdLong(), guild_settings.getThemeID()), RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong(), guild_settings.getThemeID(), false), cache.getAdditionalInfo());
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.RANDOMSHOP_REPLAY_ERR)).build()).queue();
					if(cache != null)
						Hashes.clearTempCache("randomshop_play_"+e.getMember().getUser().getId());
				}
			}
			else if(args.length > 0) {
				//display the weapons that can be obtained.
				RandomshopExecution.inspectItems(e, null, RankingSystemItems.SQLgetWeaponAbbvs(e.getGuild().getIdLong(), guild_settings.getThemeID()), RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong(), guild_settings.getThemeID(), false), bundleArguments(args, 0), 1);
			}
			else {
				//if typos occur, run help
				RandomshopExecution.runHelp(e, RankingSystemItems.SQLgetWeaponAbbvs(e.getGuild().getIdLong(), guild_settings.getThemeID()), RankingSystemItems.SQLgetWeaponCategories(e.getGuild().getIdLong(), guild_settings.getThemeID(), false));
			}
		}
		else {
			e.getChannel().sendMessage(e.getMember().getAsMention()+STATIC.getTranslation(e.getMember(), Translation.NOT_BOT_CHANNEL)+STATIC.getChannels(bot_channels)).queue();
		}
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		logger.trace("The user {} has executed the Randomshop command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
	}
	
	private String bundleArguments(String [] args, int start) {
		var weapon = "";
		for(int i = start; i < args.length; i++) {
			weapon += args[i]+" ";
		}
		return weapon.trim();
	}

}
