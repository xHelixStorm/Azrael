package de.azrael.commandsContainer;

import java.awt.Color;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.jpastebin.exceptions.PasteException;
import org.jpastebin.pastebin.exceptions.LoginException;
import org.jpastebin.pastebin.exceptions.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Cache;
import de.azrael.core.Hashes;
import de.azrael.core.UserPrivs;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.fileManagement.IniFileReader;
import de.azrael.sql.Azrael;
import de.azrael.sql.BotConfiguration;
import de.azrael.util.CharacterReplacer;
import de.azrael.util.Pastebin;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class FilterExecution {
	private final static Logger logger = LoggerFactory.getLogger(FilterExecution.class);
	
	public static void runHelp(GuildMessageReceivedEvent e) {
		//parameters are disabled by default in case of errors
		boolean filterWordFilter = false;
		boolean filterNameFilter = false;
		boolean filterNameKick = false;
		boolean filterFunnyNames = false;
		boolean filterStaffNames = false;
		boolean filterProhibitedUrls = false;
		boolean filterAllowedUrls = false;
		boolean filterProhibitedSubs = false;
		
		final var subCommands = BotConfiguration.SQLgetCommand(e.getGuild().getIdLong(), 1, Command.FILTER_WORD_FILTER, Command.FILTER_NAME_FILTER, Command.FILTER_NAME_KICK
				, Command.FILTER_FUNNY_NAMES, Command.FILTER_STAFF_NAMES, Command.FILTER_PROHIBITED_URLS, Command.FILTER_ALLOWED_URLS, Command.FILTER_PROHIBITED_SUBS);
		
		for(final var values : subCommands) {
			boolean enabled = false;
			String name = "";
			if(values instanceof Boolean)
				enabled = (Boolean)values;
			else if(values instanceof String)
				name = ((String)values).split(":")[0];
			
			if(name.equals(Command.FILTER_WORD_FILTER.getColumn()))
				filterWordFilter = enabled;
			else if(name.equals(Command.FILTER_NAME_FILTER.getColumn()))
				filterNameFilter = enabled;
			else if(name.equals(Command.FILTER_NAME_KICK.getColumn()))
				filterNameKick = enabled;
			else if(name.equals(Command.FILTER_FUNNY_NAMES.getColumn()))
				filterFunnyNames = enabled;
			else if(name.equals(Command.FILTER_STAFF_NAMES.getColumn()))
				filterStaffNames = enabled;
			else if(name.equals(Command.FILTER_PROHIBITED_URLS.getColumn()))
				filterProhibitedUrls = enabled;
			else if(name.equals(Command.FILTER_ALLOWED_URLS.getColumn()))
				filterAllowedUrls = enabled;
			else if(name.endsWith(Command.FILTER_PROHIBITED_SUBS.getColumn()))
				filterProhibitedSubs = enabled;
		}
		
		StringBuilder sf = new StringBuilder();
		if(filterWordFilter)	sf.append(STATIC.getTranslation(e.getMember(), Translation.FILTER_PARAM_1).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_WORD_FILTER)));
		if(filterNameFilter)	sf.append(STATIC.getTranslation(e.getMember(), Translation.FILTER_PARAM_2).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_NAME_FILTER)));
		if(filterNameKick)		sf.append(STATIC.getTranslation(e.getMember(), Translation.FILTER_PARAM_3).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_NAME_KICK)));
		if(filterFunnyNames)	sf.append(STATIC.getTranslation(e.getMember(), Translation.FILTER_PARAM_4).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_FUNNY_NAMES)));
		if(filterStaffNames)	sf.append(STATIC.getTranslation(e.getMember(), Translation.FILTER_PARAM_5).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_STAFF_NAMES)));
		if(filterProhibitedUrls)sf.append(STATIC.getTranslation(e.getMember(), Translation.FILTER_PARAM_6).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_PROHIBITED_URLS)));
		if(filterAllowedUrls)	sf.append(STATIC.getTranslation(e.getMember(), Translation.FILTER_PARAM_7).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_ALLOWED_URLS)));
		if(filterProhibitedSubs)sf.append(STATIC.getTranslation(e.getMember(), Translation.FILTER_PARAM_8).replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_PROHIBITED_SUBS)));
		
		if(sf.length() > 0) {
			EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS));
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_HELP)
					.replace("{}", STATIC.getTranslation(e.getMember(), Translation.PARAM_EXIT))+sf.toString()).build()).queue();
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_DISABLED)).build()).queue();
		}
	}
	
	public static void runTask(GuildMessageReceivedEvent e, String _message) {
		String key = "filter_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId();
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
		
		if(_message.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_WORD_FILTER)) && STATIC.getCommandEnabled(e.getGuild(), Command.FILTER_WORD_FILTER)) {
			final var wordFilterLevel = STATIC.getCommandLevel(e.getGuild(), Command.FILTER_WORD_FILTER);
			if(UserPrivs.comparePrivilege(e.getMember(), wordFilterLevel) || BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
				message.setTitle("WORD-FILTER");
				printFilterActions(e, message);
				Hashes.addTempCache(key, new Cache(180000, "word-filter"));
			}
			else {
				e.getChannel().sendMessage(message.setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(wordFilterLevel, e.getMember())).build()).queue();
			}
		}
		else if(_message.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_NAME_FILTER)) && STATIC.getCommandEnabled(e.getGuild(), Command.FILTER_NAME_FILTER)) {
			final var nameFilterLevel = STATIC.getCommandLevel(e.getGuild(), Command.FILTER_NAME_FILTER);
			if(UserPrivs.comparePrivilege(e.getMember(), nameFilterLevel) || BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
				message.setTitle("NAME-FILTER");
				printFilterActions(e, message);
				Hashes.addTempCache(key, new Cache(180000, "name-filter"));
			}
			else {
				e.getChannel().sendMessage(message.setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(nameFilterLevel, e.getMember())).build()).queue();
			}
		}
		else if(_message.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_NAME_KICK)) && STATIC.getCommandEnabled(e.getGuild(), Command.FILTER_NAME_KICK)) {
			final var nameKickLevel = STATIC.getCommandLevel(e.getGuild(), Command.FILTER_NAME_KICK);
			if(UserPrivs.comparePrivilege(e.getMember(), nameKickLevel) || BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
				message.setTitle("NAME-KICK");
				printFilterActions(e, message);
				Hashes.addTempCache(key, new Cache(180000, "name-kick"));
			}
			else {
				e.getChannel().sendMessage(message.setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(nameKickLevel, e.getMember())).build()).queue();
			}
		}
		else if(_message.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_FUNNY_NAMES)) && STATIC.getCommandEnabled(e.getGuild(), Command.FILTER_FUNNY_NAMES)) {
			final var funnyNamesLevel = STATIC.getCommandLevel(e.getGuild(), Command.FILTER_FUNNY_NAMES);
			if(UserPrivs.comparePrivilege(e.getMember(), funnyNamesLevel) || BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
				message.setTitle("FUNNY-NAMES");
				printFilterActions(e, message);
				Hashes.addTempCache(key, new Cache(180000, "funny-names"));
			}
			else {
				e.getChannel().sendMessage(message.setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(funnyNamesLevel, e.getMember())).build()).queue();
			}
		}
		else if(_message.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_STAFF_NAMES)) && STATIC.getCommandEnabled(e.getGuild(), Command.FILTER_STAFF_NAMES)) {
			final var staffNamesLevel = STATIC.getCommandLevel(e.getGuild(), Command.FILTER_STAFF_NAMES);
			if(UserPrivs.comparePrivilege(e.getMember(), staffNamesLevel) || BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
				message.setTitle("STAFF-NAMES");
				printFilterActions(e, message);
				Hashes.addTempCache(key, new Cache(180000, "staff-names"));
			}
			else {
				e.getChannel().sendMessage(message.setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(staffNamesLevel, e.getMember())).build()).queue();
			}
		}
		else if(_message.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_PROHIBITED_URLS)) && STATIC.getCommandEnabled(e.getGuild(), Command.FILTER_PROHIBITED_URLS)) {
			final var urlBlacklistLevel = STATIC.getCommandLevel(e.getGuild(), Command.FILTER_PROHIBITED_URLS);
			if(UserPrivs.comparePrivilege(e.getMember(), urlBlacklistLevel) || BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
				message.setTitle("PROHIBITED-URLS");
				printFilterActions(e, message);
				Hashes.addTempCache(key, new Cache(180000, "prohibited-urls"));
			}
			else {
				e.getChannel().sendMessage(message.setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(urlBlacklistLevel, e.getMember())).build()).queue();
			}
		}
		else if(_message.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_ALLOWED_URLS)) && STATIC.getCommandEnabled(e.getGuild(), Command.FILTER_ALLOWED_URLS)) {
			final var urlWhitelistLevel = STATIC.getCommandLevel(e.getGuild(), Command.FILTER_ALLOWED_URLS);
			if(UserPrivs.comparePrivilege(e.getMember(), urlWhitelistLevel) || BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
				message.setTitle("ALLOWED-URLS");
				printFilterActions(e, message);
				Hashes.addTempCache(key, new Cache(180000, "allowed-urls"));
			}
			else {
				e.getChannel().sendMessage(message.setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(urlWhitelistLevel, e.getMember())).build()).queue();
			}
		}
		else if(_message.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_PROHIBITED_SUBS)) && STATIC.getCommandEnabled(e.getGuild(), Command.FILTER_PROHIBITED_SUBS)) {
			final var tweetBlacklistLevel = STATIC.getCommandLevel(e.getGuild(), Command.FILTER_PROHIBITED_SUBS);
			if(UserPrivs.comparePrivilege(e.getMember(), tweetBlacklistLevel) || BotConfiguration.SQLisAdministrator(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong())) {
				message.setTitle("PROHIBITED-SUBS");
				printFilterActions(e, message);
				Hashes.addTempCache(key, new Cache(180000, "prohibited-subs"));
			}
			else {
				e.getChannel().sendMessage(message.setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(tweetBlacklistLevel, e.getMember())).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
		}
	}
	
	public static void performAction(GuildMessageReceivedEvent e, String _message, Cache cache) {
		String key = "filter_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId();
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
		if(cache.getExpiration() - System.currentTimeMillis() > 0) {
			if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_EXIT))) {
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_ABORT)).build()).queue();
				Hashes.clearTempCache(key);
				Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER.getColumn(), e.getMessage().getContentRaw());
				return;
			}
			switch(cache.getAdditionalInfo()) {
				case "word-filter" -> {
					if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISPLAY))) {
						message.setTitle("WORD-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_DISPLAY));
						StringBuilder out = new StringBuilder();
						for(String lang : Azrael.SQLgetFilterLanguages(STATIC.getLanguage(e.getMember()))) {
							out.append(lang+"\n");
						}
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LANG_SELECTION)+"**"+(out.length() > 0 ? out.toString() : STATIC.getTranslation(e.getMember(), Translation.FILTER_NO_LANGS))+"**").build()).queue();
						cache.updateDescription("display-word-filter").setExpiration(180000);
						Hashes.addTempCache(key, cache);
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_WORD_FILTER.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_INSERT))) {
						message.setTitle("WORD-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_INSERT));
						StringBuilder out = new StringBuilder();
						for(String lang : Azrael.SQLgetFilterLanguages(STATIC.getLanguage(e.getMember()))) {
							out.append(lang+"\n");
						}
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LANG_SELECTION)+"**"+(out.length() > 0 ? out.toString() : STATIC.getTranslation(e.getMember(), Translation.FILTER_NO_LANGS))+"**").build()).queue();
						cache.updateDescription("insert-word-filter").setExpiration(180000);
						Hashes.addTempCache(key, cache);
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_WORD_FILTER.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))) {
						message.setTitle("WORD-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_REMOVE));
						StringBuilder out = new StringBuilder();
						for(String lang : Azrael.SQLgetFilterLanguages(STATIC.getLanguage(e.getMember()))) {
							out.append(lang+"\n");
						}
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LANG_SELECTION)+"**"+(out.length() > 0 ? out.toString() : STATIC.getTranslation(e.getMember(), Translation.FILTER_NO_LANGS))+"**").build()).queue();
						cache.updateDescription("remove-word-filter").setExpiration(180000);
						Hashes.addTempCache(key, cache);
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_WORD_FILTER.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_PASTEBIN))) {
						message.setTitle("WORD-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_ADD_PASTEBIN));
						StringBuilder out = new StringBuilder();
						for(String lang : Azrael.SQLgetFilterLanguages(STATIC.getLanguage(e.getMember()))) {
							out.append(lang+"\n");
						}
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LANG_SELECTION)+"**"+(out.length() > 0 ? out.toString() : STATIC.getTranslation(e.getMember(), Translation.FILTER_NO_LANGS))+"**").build()).queue();
						cache.updateDescription("add-load-word-filter").setExpiration(180000);
						Hashes.addTempCache(key, cache);
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_WORD_FILTER.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_LOAD_PASTEBIN))) {
						message.setTitle("WORD-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_LOAD_PASTEBIN));
						StringBuilder out = new StringBuilder();
						for(String lang : Azrael.SQLgetFilterLanguages(STATIC.getLanguage(e.getMember()))) {
							out.append(lang+"\n");
						}
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LANG_SELECTION)+"**"+(out.length() > 0 ? out.toString() : STATIC.getTranslation(e.getMember(), Translation.FILTER_NO_LANGS))+"**").build()).queue();
						cache.updateDescription("load-word-filter").setExpiration(180000);
						Hashes.addTempCache(key, cache);
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_WORD_FILTER.getColumn(), _message);
					}
				}
				case "name-filter" -> {
					if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISPLAY))) {
						StringBuilder out = new StringBuilder();
						for(var word : Azrael.SQLgetNameFilter(e.getGuild().getIdLong())) {
							if(!word.getKick())
								out.append(word.getName()+"\n");
						}
						if(out.length() > 0) {
							try {
								String paste_link = Pastebin.unlistedPaste("NAME-FILTER", out.toString());
								message.setTitle("NAME-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_DISPLAY));
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LIST)+paste_link).build()).queue();
							} catch (IllegalStateException | LoginException | PasteException e2) {
								message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PASTE));
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_PASTE_ERR)).build()).queue();
								logger.error("Error on creating pastebin with the name filter in guild {}", e.getGuild().getId(), e2);
							}
						}
						else {
							message.setColor(Color.RED);
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LIST_EMPTY)).build()).queue();
						}
						Hashes.clearTempCache(key);
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_NAME_FILTER.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_INSERT))) {
						message.setTitle("NAME-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_INSERT));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_WORD)).build()).queue();
						cache.updateDescription("insert-name-filter").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_NAME_FILTER.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))) {
						message.setTitle("NAME-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_REMOVE));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_WORD)).build()).queue();
						cache.updateDescription("remove-name-filter").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_NAME_FILTER.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_PASTEBIN))) {
						message.setTitle("NAME-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_ADD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE)).build()).queue();
						cache.updateDescription("add-load-name-filter").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_NAME_FILTER.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_LOAD_PASTEBIN))) {
						message.setTitle("NAME-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_LOAD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE)).build()).queue();
						cache.updateDescription("load-name-filter").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_NAME_FILTER.getColumn(), _message);
					}
				}
				case "name-kick" -> {
					if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISPLAY))) {
						StringBuilder out = new StringBuilder();
						for(var word : Azrael.SQLgetNameFilter(e.getGuild().getIdLong())) {
							if(word.getKick())
								out.append(word.getName()+"\n");
						}
						if(out.length() > 0) {
							try {
								String paste_link = Pastebin.unlistedPaste("NAME-KICK", out.toString());
								message.setTitle("NAME-KICK "+STATIC.getTranslation(e.getMember(), Translation.FILTER_DISPLAY));
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LIST)+paste_link).build()).queue();
							} catch (IllegalStateException | LoginException | PasteException e2) {
								message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PASTE));
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_PASTE_ERR)).build()).queue();
								logger.warn("Error on creating pastebin with the kick list in guild {}", e.getGuild().getId(), e2);
							}
						}
						else {
							message.setColor(Color.RED);
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LIST_EMPTY)).build()).queue();
						}
						Hashes.clearTempCache(key);
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_NAME_KICK.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_INSERT))) {
						message.setTitle("NAME-KICK "+STATIC.getTranslation(e.getMember(), Translation.FILTER_INSERT));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_WORD)).build()).queue();
						cache.updateDescription("insert-name-kick").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_NAME_KICK.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))) {
						message.setTitle("NAME-KICK "+STATIC.getTranslation(e.getMember(), Translation.FILTER_REMOVE));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_WORD)).build()).queue();
						cache.updateDescription("remove-name-kick").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_NAME_KICK.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_PASTEBIN))) {
						message.setTitle("NAME-KICK "+STATIC.getTranslation(e.getMember(), Translation.FILTER_ADD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE)).build()).queue();
						cache.updateDescription("add-load-name-kick").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_NAME_KICK.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_LOAD_PASTEBIN))) {
						message.setTitle("NAME-KICK "+STATIC.getTranslation(e.getMember(), Translation.FILTER_LOAD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE)).build()).queue();
						cache.updateDescription("load-name-kick").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_NAME_KICK.getColumn(), _message);
					}
				}
				case "funny-names" -> {
					if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISPLAY))) {
						StringBuilder out = new StringBuilder();
						for(String word : Azrael.SQLgetFunnyNames(e.getGuild().getIdLong())) {
							out.append(word+"\n");
						}
						if(out.length() > 0) {
							try {
								String paste_link = Pastebin.unlistedPaste("FUNNY-NAMES", out.toString());
								message.setTitle("FUNNY-NAMES "+STATIC.getTranslation(e.getMember(), Translation.FILTER_DISPLAY));
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LIST)+paste_link).build()).queue();
							} catch (IllegalStateException | LoginException | PasteException e2) {
								message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PASTE));
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_PASTE_ERR)).build()).queue();
								logger.warn("Error on creating pastebin with funky names in guild {}", e.getGuild().getId(), e2);
							}
						}
						else {
							message.setColor(Color.RED);
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LIST_EMPTY)).build()).queue();
						}
						Hashes.clearTempCache(key);
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_FUNNY_NAMES.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_INSERT))) {
						message.setTitle("FUNNY-NAMES "+STATIC.getTranslation(e.getMember(), Translation.FILTER_INSERT));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_WORD)).build()).queue();
						cache.updateDescription("insert-funny-names").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_FUNNY_NAMES.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))) {
						message.setTitle("FUNNY-NAMES "+STATIC.getTranslation(e.getMember(), Translation.FILTER_REMOVE));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_WORD)).build()).queue();
						cache.updateDescription("remove-funny-names").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_FUNNY_NAMES.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_PASTEBIN))) {
						message.setTitle("FUNNY-NAMES "+STATIC.getTranslation(e.getMember(), Translation.FILTER_ADD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE)).build()).queue();
						cache.updateDescription("add-load-funny-names").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_FUNNY_NAMES.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_LOAD_PASTEBIN))) {
						message.setTitle("FUNNY-NAMES "+STATIC.getTranslation(e.getMember(), Translation.FILTER_LOAD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE)).build()).queue();
						cache.updateDescription("load-funny-names").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_FUNNY_NAMES.getColumn(), _message);
					}
				}
				case "staff-names" -> {
					if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISPLAY))) {
						StringBuilder out = new StringBuilder();
						for(String word : Azrael.SQLgetStaffNames(e.getGuild().getIdLong())) {
							out.append(word+"\n");
						}
						if(out.length() > 0) {
							try {
								String paste_link = Pastebin.unlistedPaste("STAFF-NAMES", out.toString());
								message.setTitle("STAFF-NAMES "+STATIC.getTranslation(e.getMember(), Translation.FILTER_DISPLAY));
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LIST)+paste_link).build()).queue();
							} catch (IllegalStateException | LoginException | PasteException e2) {
								message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PASTE));
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_PASTE_ERR)).build()).queue();
								logger.warn("Error on creating pastebin with staff names in guild {}", e.getGuild().getId(), e2);
							}
						}
						else {
							message.setColor(Color.RED);
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LIST_EMPTY)).build()).queue();
						}
						Hashes.clearTempCache(key);
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_STAFF_NAMES.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_INSERT))) {
						message.setTitle("STAFF-NAMES "+STATIC.getTranslation(e.getMember(), Translation.FILTER_INSERT));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_WORD)).build()).queue();
						cache.updateDescription("insert-staff-names").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_STAFF_NAMES.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))) {
						message.setTitle("STAFF-NAMES "+STATIC.getTranslation(e.getMember(), Translation.FILTER_REMOVE));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_WORD)).build()).queue();
						cache.updateDescription("remove-staff-names").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_STAFF_NAMES.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_PASTEBIN))) {
						message.setTitle("STAFF-NAMES "+STATIC.getTranslation(e.getMember(), Translation.FILTER_ADD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE)).build()).queue();
						cache.updateDescription("add-load-staff-names").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_STAFF_NAMES.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_LOAD_PASTEBIN))) {
						message.setTitle("STAFF-NAMES "+STATIC.getTranslation(e.getMember(), Translation.FILTER_LOAD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE)).build()).queue();
						cache.updateDescription("load-staff-names").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_STAFF_NAMES.getColumn(), _message);
					}
				}
				case "prohibited-urls" -> {
					if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISPLAY))) {
						StringBuilder out = new StringBuilder();
						for(String word : Azrael.SQLgetURLBlacklist(e.getGuild().getIdLong())) {
							out.append(word+"\n");
						}
						if(out.length() > 0) {
							try {
								String paste_link = Pastebin.unlistedPaste("PROHIBITED-URLS", out.toString());
								message.setTitle("PROHIBITED-URLS "+STATIC.getTranslation(e.getMember(), Translation.FILTER_DISPLAY));
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LIST)+paste_link).build()).queue();
							} catch (IllegalStateException | LoginException | PasteException e2) {
								message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PASTE));
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_PASTE_ERR)).build()).queue();
								logger.warn("Error on creating pastebin with prohibited urls in guild {}", e.getGuild().getId(), e2);
							}
						}
						else {
							message.setColor(Color.RED);
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LIST_EMPTY)).build()).queue();
						}
						Hashes.clearTempCache(key);
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_PROHIBITED_URLS.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_INSERT))) {
						message.setTitle("PROHIBITED-URLS "+STATIC.getTranslation(e.getMember(), Translation.FILTER_INSERT));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_FQDN)).build()).queue();
						cache.updateDescription("insert-prohibited-urls").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_PROHIBITED_URLS.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))) {
						message.setTitle("PROHIBITED-URLS "+STATIC.getTranslation(e.getMember(), Translation.FILTER_REMOVE));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_FQDN)).build()).queue();
						cache.updateDescription("remove-prohibited-urls").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_PROHIBITED_URLS.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_PASTEBIN))) {
						message.setTitle("PROHIBITED-URLS "+STATIC.getTranslation(e.getMember(), Translation.FILTER_ADD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE_FQDN)).build()).queue();
						cache.updateDescription("add-load-prohibited-urls").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_PROHIBITED_URLS.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_LOAD_PASTEBIN))) {
						message.setTitle("PROHIBITED-URLS "+STATIC.getTranslation(e.getMember(), Translation.FILTER_LOAD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE_FQDN)).build()).queue();
						cache.updateDescription("load-prohibited-urls").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_PROHIBITED_URLS.getColumn(), _message);
					}
				}
				case "allowed-urls" -> {
					if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISPLAY))) {
						StringBuilder out = new StringBuilder();
						for(String word : Azrael.SQLgetURLWhitelist(e.getGuild().getIdLong())) {
							out.append(word+"\n");
						}
						if(out.length() > 0) {
							try {
								String paste_link = Pastebin.unlistedPaste("ALLOWED-URLS", out.toString());
								message.setTitle("ALLOWED-URLS "+STATIC.getTranslation(e.getMember(), Translation.FILTER_DISPLAY));
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LIST)+paste_link).build()).queue();
							} catch (IllegalStateException | LoginException | PasteException e2) {
								message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PASTE));
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_PASTE_ERR)).build()).queue();
								logger.warn("Error on creating pastebin with whitelisted urls in guild {}", e.getGuild().getId(), e2);
							}
						}
						else {
							message.setColor(Color.RED);
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LIST_EMPTY)).build()).queue();
						}
						Hashes.clearTempCache(key);
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_ALLOWED_URLS.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_INSERT))) {
						message.setTitle("ALLOWED-URLS "+STATIC.getTranslation(e.getMember(), Translation.FILTER_INSERT));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_FQDN)).build()).queue();
						cache.updateDescription("insert-allowed-urls").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_ALLOWED_URLS.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))) {
						message.setTitle("ALLOWED-URLS "+STATIC.getTranslation(e.getMember(), Translation.FILTER_REMOVE));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_FQDN)).build()).queue();
						cache.updateDescription("remove-allowed-urls").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_ALLOWED_URLS.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_PASTEBIN))) {
						message.setTitle("ALLOWED-URLS "+STATIC.getTranslation(e.getMember(), Translation.FILTER_ADD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE_FQDN)).build()).queue();
						cache.updateDescription("add-load-allowed-urls").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_ALLOWED_URLS.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_LOAD_PASTEBIN))) {
						message.setTitle("ALLOWED-URLS "+STATIC.getTranslation(e.getMember(), Translation.FILTER_LOAD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE_FQDN)).build()).queue();
						cache.updateDescription("load-allowed-urls").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_ALLOWED_URLS.getColumn(), _message);
					}
				}
				case "prohibited-subs" -> {
					if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISPLAY))) {
						StringBuilder out = new StringBuilder();
						for(String word : Azrael.SQLgetSubscriptionBlacklist(e.getGuild().getIdLong())) {
							out.append(word+"\n");
						}
						if(out.length() > 0) {
							try {
								String paste_link = Pastebin.unlistedPaste("PROHIBITED-SUBS", out.toString());
								message.setTitle("PROHIBITED-SUBS "+STATIC.getTranslation(e.getMember(), Translation.FILTER_DISPLAY));
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LIST)+paste_link).build()).queue();
							} catch (IllegalStateException | LoginException | PasteException e2) {
								message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PASTE));
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_PASTE_ERR)).build()).queue();
								logger.warn("Error on creating pastebin with prohibited subscriptions in guild {}!", e.getGuild().getId(), e2);
							}
						}
						else {
							message.setColor(Color.RED);
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LIST_EMPTY)).build()).queue();
						}
						Hashes.clearTempCache(key);
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_PROHIBITED_SUBS.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_INSERT))) {
						message.setTitle("PROHIBITED-SUBS "+STATIC.getTranslation(e.getMember(), Translation.FILTER_INSERT));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_USERNAME)).build()).queue();
						cache.updateDescription("insert-prohibited-subs").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_PROHIBITED_SUBS.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))) {
						message.setTitle("PROHIBITED-SUBS "+STATIC.getTranslation(e.getMember(), Translation.FILTER_REMOVE));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_USERNAME)).build()).queue();
						cache.updateDescription("remove-prohibited-subs").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_PROHIBITED_SUBS.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_PASTEBIN))) {
						message.setTitle("PROHIBITED-SUBS "+STATIC.getTranslation(e.getMember(), Translation.FILTER_ADD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE_USERNAME)).build()).queue();
						cache.updateDescription("add-load-prohibited-subs").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_PROHIBITED_SUBS.getColumn(), _message);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_LOAD_PASTEBIN))) {
						message.setTitle("PROHIBITED-SUBS "+STATIC.getTranslation(e.getMember(), Translation.FILTER_LOAD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE_USERNAME)).build()).queue();
						cache.updateDescription("load-prohibited-subs").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_PROHIBITED_SUBS.getColumn(), _message);
					}
				}
				case "display-word-filter" -> {
					callFilterLangContent(e, message, key, _message.toLowerCase(), cache);
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_WORD_FILTER.getColumn(), _message);
				}
				case "insert-word-filter" -> {
					var langInsert = _message.toLowerCase();
					final var selectedLang = langCheck(e, message, langInsert, key, cache);
					if(selectedLang != null) {
						message.setTitle("WORD-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_INSERT)+" "+langInsert.toUpperCase());
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_WORD)).build()).queue();
						cache.updateDescription("lang-insert-word-filter").updateDescription2(selectedLang.split("-")[0]).setExpiration(180000);
						Hashes.addTempCache(key, cache);
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_WORD_FILTER.getColumn(), _message);
					}
				}
				case "lang-insert-word-filter" -> {
					insertLangWord(e, message, key, cache.getAdditionalInfo2(), _message);
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_WORD_FILTER.getColumn(), _message);
				}
				case "remove-word-filter" -> {
					var langRemove = _message.toLowerCase();
					final var selectedLang = langCheck(e, message, langRemove, key, cache);
					if(selectedLang != null) {
						message.setTitle("WORD-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_REMOVE)+" "+langRemove.toUpperCase());
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_WORD)).build()).queue();
						cache.updateDescription("lang-remove-word-filter").updateDescription2(selectedLang.split("-")[0]).setExpiration(180000);
						Hashes.addTempCache(key, cache);
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_WORD_FILTER.getColumn(), _message);
					}
				}
				case "lang-remove-word-filter" -> {
					removeLangWord(e, message, key, cache.getAdditionalInfo2(), _message);
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_WORD_FILTER.getColumn(), _message);
				}
				case "add-load-word-filter" -> {
					var addLangLoad = _message.toLowerCase();
					final var selectedLang = langCheck(e, message, addLangLoad, key, cache);
					if(selectedLang != null) {
						message.setTitle("WORD-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_ADD_PASTEBIN)+" "+addLangLoad.toUpperCase());
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE)).build()).queue();
						cache.updateDescription("lang-add-load-word-filter").updateDescription2(selectedLang.split("-")[0]).setExpiration(180000);
						Hashes.addTempCache(key, cache);
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_WORD_FILTER.getColumn(), _message);
					}
				}
				case "lang-add-load-word-filter" -> {
					loadLangWords(e, message, key, cache.getAdditionalInfo2(), _message, false, cache);
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_WORD_FILTER.getColumn(), _message);
				}
				case "load-word-filter" -> {
					var langLoad = _message.toLowerCase();
					final var selectedLang = langCheck(e, message, langLoad, key, cache);
					if(selectedLang != null) {
						message.setTitle("WORD-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_LOAD_PASTEBIN)+" "+langLoad.toUpperCase());
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE)).build()).queue();
						cache.updateDescription("lang-load-word-filter").updateDescription2(selectedLang.split("-")[0]).setExpiration(180000);
						Hashes.addTempCache(key, cache);
						Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_WORD_FILTER.getColumn(), _message);
					}
				}
				case "lang-load-word-filter" -> {
					loadLangWords(e, message, key, cache.getAdditionalInfo2(), _message, true, cache);
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_WORD_FILTER.getColumn(), _message);
				}
				case "insert-name-filter" -> {
					if(Azrael.SQLInsertNameFilter(_message, false, e.getGuild().getIdLong()) >= 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_INSERT)).build()).queue();
						Hashes.removeNameFilter(e.getGuild().getIdLong());
						logger.info("User {} has inserted the word {} into the name filter in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).queue();
						logger.error("Name {} couldn't be inserted into the name filter in guild {}", _message, e.getGuild().getId());
					}
					Hashes.clearTempCache(key);
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_NAME_FILTER.getColumn(), _message);
				}
				case "remove-name-filter" -> {
					final var result = Azrael.SQLDeleteNameFilter(_message, false, e.getGuild().getIdLong()); 
					if(result > 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_REMOVE)).build()).queue();
						Hashes.removeNameFilter(e.getGuild().getIdLong());
						logger.info("User {} has removed the word {} from the name filter in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
					}
					else if(result == 0) {
						message.setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_NAME_REMOVE_ERR)).build()).queue();
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Name {} couldn't be removed from the name filter in guild {}", _message, e.getGuild().getId());
					}
					Hashes.clearTempCache(key);
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_NAME_FILTER.getColumn(), _message);
				}
				case "load-name-filter", "add-load-name-filter" -> {
					if(_message.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _message.startsWith("http")) {
						try {
							String [] words = Pastebin.readPasteLink(_message).split("[\\r\\n]+");
							var QueryResult = Azrael.SQLReplaceNameFilter(words, false, e.getGuild().getIdLong(), (cache.getAdditionalInfo().split("-")[0].equals("add") ? false : true));
							if(QueryResult == 0) {
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_ADD_PASTEBIN)).build()).queue();
								Hashes.removeNameFilter(e.getGuild().getIdLong());
								logger.info("User {} has inserted words with the pastebin url {} into the name filter in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
							}
							else if(QueryResult == 1) {
								//throw error for failing the db replacement
								message.setColor(Color.RED);
								var duplicates = checkDuplicates(words);
								if(duplicates == null || duplicates.size() == 0) {
									e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
									logger.error("The name filter couldn't be updated with the pastebin url {} in guild {}", _message, e.getGuild().getId());
								}
								else {
									StringBuilder out = new StringBuilder();
									for(var word : duplicates) {
										out.append("**"+word+"**\n");
									}
									e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_DUPLICATES)+out.toString()).build()).queue();
								}
							}
							else {
								//thow error for failing the rollback
								message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_ROLLBACK_ERR)).build()).queue();
								logger.error("Changes on the name filter couldn't be rolled back on error in guild {}", e.getGuild().getId());
							}
						} catch (MalformedURLException | RuntimeException | LoginException | ParseException e2) {
							message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_NOT_PASTE));
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
							logger.error("Reading pastebin url {} for the name filter failed in guild {}", _message, e.getGuild().getId(), e2);
						}
						Hashes.clearTempCache(key);
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PASTE_READ_ERR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_NAME_FILTER.getColumn(), _message);
				}
				case "insert-name-kick" -> {
					if(Azrael.SQLInsertNameFilter(_message, true, e.getGuild().getIdLong()) >= 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_INSERT)).build()).queue();
						Hashes.removeNameFilter(e.getGuild().getIdLong());
						logger.info("User {} has inserted the word {} into the name kick list in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Name {} couldn't be inserted into the name kick list in guild {}", _message, e.getGuild().getId());
					}
					Hashes.clearTempCache(key);
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_NAME_KICK.getColumn(), _message);
				}
				case "remove-name-kick" -> {
					final var result = Azrael.SQLDeleteNameFilter(_message, true, e.getGuild().getIdLong()); 
					if(result > 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_REMOVE)).build()).queue();
						Hashes.removeNameFilter(e.getGuild().getIdLong());
						logger.info("User {} has removed the word {} from the name kick list in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
					}
					else if(result == 0) {
						message.setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_NAME_REMOVE_ERR)).build()).queue();
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Name {} couldn't be removed from the name kick list in guild {}", _message, e.getGuild().getId());
					}
					Hashes.clearTempCache(key);
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_NAME_KICK.getColumn(), _message);
				}
				case "load-name-kick", "add-load-name-kick" -> {
					if(_message.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _message.startsWith("http")) {
						try {
							String [] words = Pastebin.readPasteLink(_message).split("[\\r\\n]+");
							var QueryResult = Azrael.SQLReplaceNameFilter(words, true, e.getGuild().getIdLong(), (cache.getAdditionalInfo().split("-")[0].equals("add") ? false : true));
							if(QueryResult == 0) {
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_ADD_PASTEBIN)).build()).queue();
								Hashes.removeNameFilter(e.getGuild().getIdLong());
								logger.info("User {} has inserted words with the pastebin url {} into the name kick list in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
							}
							else if(QueryResult == 1) {
								//throw error for failing the db replacement
								message.setColor(Color.RED);
								var duplicates = checkDuplicates(words);
								if(duplicates == null || duplicates.size() == 0) {
									message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
									e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
									logger.error("The name kick list couldn't be updated with the pastebin url {} in guild {}", _message, e.getGuild().getId());
								}
								else {
									StringBuilder out = new StringBuilder();
									for(var word : duplicates) {
										out.append("**"+word+"**\n");
									}
									e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_DUPLICATES)+out.toString()).build()).queue();
								}
							}
							else {
								//thow error for failing the rollback
								message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_ROLLBACK_ERR)).build()).queue();
								logger.error("Changes on the name kick list couldn't be rolled back on error in guild {}", e.getGuild().getId());
							}
						} catch (MalformedURLException | RuntimeException | LoginException | ParseException e2) {
							message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_NOT_PASTE));
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
							logger.error("Reading pastebin url {} for the name kick list failed in guild {}", _message, e.getGuild().getId(), e2);
						}
						Hashes.clearTempCache(key);
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PASTE_READ_ERR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_NAME_KICK.getColumn(), _message);
				}
				case "insert-funny-names" -> {
					if(Azrael.SQLInsertFunnyNames(_message, e.getGuild().getIdLong()) >= 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_INSERT)).build()).queue();
						Hashes.removeQueryResult("funny-names_"+e.getGuild().getId());
						logger.info("User {} has inserted the word {} into the funky names list in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Name {} couldn't be inserted into the funky names list in guild {}", _message, e.getGuild().getId());
					}
					Hashes.clearTempCache(key);
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_FUNNY_NAMES.getColumn(), _message);
				}
				case "remove-funny-names" -> {
					final var result = Azrael.SQLDeleteFunnyNames(_message, e.getGuild().getIdLong()); 
					if(result > 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_REMOVE)).build()).queue();
						Hashes.removeQueryResult("funny-names_"+e.getGuild().getId());
						logger.info("User {} has removed the word {} from the funky names list in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
					}
					else if(result == 0) {
						message.setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_NAME_REMOVE_ERR)).build()).queue();
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Name {} couldn't be removed from the funky names list in guild {}", _message, e.getGuild().getId());
					}
					Hashes.clearTempCache(key);
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_FUNNY_NAMES.getColumn(), _message);
				}
				case "load-funny-names", "add-load-funny-names" -> {
					if(_message.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _message.startsWith("http")) {
						try {
							String [] words = Pastebin.readPasteLink(_message).split("[\\r\\n]+");
							var QueryResult = Azrael.SQLReplaceFunnyNames(words, e.getGuild().getIdLong(), (cache.getAdditionalInfo().split("-")[0].equals("add") ? false : true));
							if(QueryResult == 0) {
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_ADD_PASTEBIN)).build()).queue();
								Hashes.removeQueryResult("funny-names_"+e.getGuild().getId());
								logger.info("User {} has inserted words with the pastebin url {} into the funky names list in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
							}
							else if(QueryResult == 1) {
								//throw error for failing the db replacement
								message.setColor(Color.RED);
								var duplicates = checkDuplicates(words);
								if(duplicates == null || duplicates.size() == 0) {
									e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
									logger.error("The funky names list couldn't be updated with the pastebin url {} in guild {}", _message, e.getGuild().getId());
								}
								else {
									StringBuilder out = new StringBuilder();
									for(var word : duplicates) {
										out.append("**"+word+"**\n");
									}
									e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_DUPLICATES)+out.toString()).build()).queue();
								}
							}
							else {
								//thow error for failing the rollback
								message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_ROLLBACK_ERR)).build()).queue();
								logger.error("Changes on the funky names list couldn't be rolled back on error in guild {}", e.getGuild().getId());
							}
						} catch (MalformedURLException | RuntimeException | LoginException | ParseException e2) {
							message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_NOT_PASTE));
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
							logger.error("Reading pastebin url {} for the funky names list failed in guild {}", _message, e.getGuild().getId(), e2);
						}
						Hashes.clearTempCache(key);
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PASTE_READ_ERR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_FUNNY_NAMES.getColumn(), _message);
				}
				case "insert-staff-names" -> {
					if(Azrael.SQLInsertStaffName(_message, e.getGuild().getIdLong()) >= 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_INSERT)).build()).queue();
						Hashes.removeQueryResult("staff-names_"+e.getGuild().getId());
						logger.info("User {} has inserted the word {} into the staff names list in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Name {} couldn't be inserted into the staff names list in guild {}", _message, e.getGuild().getId());
					}
					Hashes.clearTempCache(key);
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_STAFF_NAMES.getColumn(), _message);
				}
				case "remove-staff-names" -> {
					final var result = Azrael.SQLDeleteStaffNames(_message, e.getGuild().getIdLong());
					if(result > 0) {
						message.setTitle("Success!");
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_REMOVE)).build()).queue();
						Hashes.removeQueryResult("staff-names_"+e.getGuild().getId());
						logger.info("User {} has removed the word {} from the staff names list in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
					}
					else if(result == 0) {
						message.setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_NAME_REMOVE_ERR)).build()).queue();
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Name {} couldn't be removed from the staff names list in guild {}", _message, e.getGuild().getId());
					}
					Hashes.clearTempCache(key);
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_STAFF_NAMES.getColumn(), _message);
				}
				case "load-staff-names", "add-load-staff-names" -> {
					if(_message.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _message.startsWith("http")) {
						try {
							String [] words = Pastebin.readPasteLink(_message).split("[\\r\\n]+");
							var QueryResult = Azrael.SQLReplaceStaffNames(words, e.getGuild().getIdLong(), (cache.getAdditionalInfo().split("-")[0].equals("add") ? false : true));
							if(QueryResult == 0) {
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_ADD_PASTEBIN)).build()).queue();
								Hashes.removeQueryResult("staff-names_"+e.getGuild().getId());
								logger.info("User {} has inserted names with the pastebin url {} into the staff names list in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
							}
							else if(QueryResult == 1) {
								//throw error for failing the db replacement
								message.setColor(Color.RED);
								var duplicates = checkDuplicates(words);
								if(duplicates == null || duplicates.size() == 0) {
									e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
									logger.error("The staff names list couldn't be updated with the pastebin url {} in guild {}", _message, e.getGuild().getId());
								}
								else {
									StringBuilder out = new StringBuilder();
									for(var word : duplicates) {
										out.append("**"+word+"**\n");
									}
									e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_DUPLICATES)+out.toString()).build()).queue();
								}
							}
							else {
								//thow error for failing the rollback
								message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_ROLLBACK_ERR)).build()).queue();
								logger.error("Changes on the staff names list couldn't be rolled back on error in guild {}", e.getGuild().getId());
							}
						} catch (MalformedURLException | RuntimeException | LoginException | ParseException e2) {
							message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_NOT_PASTE));
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
							logger.error("Reading pastebin url {} for the staff names list failed in guild {}", _message, e.getGuild().getId(), e2);
						}
						Hashes.clearTempCache(key);
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PASTE_READ_ERR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_STAFF_NAMES.getColumn(), _message);
				}
				case "insert-prohibited-urls" -> {
					if((_message.startsWith("http://") || _message.startsWith("https://")) && !_message.matches("[\\s]")) {
						if(Azrael.SQLInsertURLBlacklist(_message, e.getGuild().getIdLong()) >= 0) {
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_INSERT_URL)).build()).queue();
							Hashes.removeURLBlacklist(e.getGuild().getIdLong());
							logger.info("User {} has saved the url {} as a prohibited url in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
						}
						else {
							message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("URL {} couldn't be saved as a prohibited url in guild {}", _message, e.getGuild().getId());
						}
						Hashes.clearTempCache(key);
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_URL));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.URL_INVALID)).build()).queue();
						cache.setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_PROHIBITED_URLS.getColumn(), _message);
				}
				case "remove-prohibited-urls" -> {
					final var result = Azrael.SQLDeleteURLBlacklist(_message, e.getGuild().getIdLong());
					if(result > 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_REMOVE_URL)).build()).queue();
						Hashes.removeURLBlacklist(e.getGuild().getIdLong());
						logger.info("User {} has removed the url {} from the prohibited urls in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
					}
					else if(result == 0) {
						message.setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_URL_REMOVE_ERR)).build()).queue();
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("URL {} couldn't be removed from the prohibited urls in guild {}", _message, e.getGuild().getId());
					}
					Hashes.clearTempCache(key);
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_PROHIBITED_URLS.getColumn(), _message);
				}
				case "add-load-prohibited-urls", "load-prohibited-urls" -> {
					if(_message.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _message.startsWith("http")) {
						try {
							String [] url = Pastebin.readPasteLink(_message).split("[\\r\\n]+");
							List<String> checkedURLs = new ArrayList<String>();
							for(var link : url) {
								if(link.matches("^(http:\\/\\/|https:\\/\\/)[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}$")) {
									checkedURLs.add(link);
								}
								else {
									checkedURLs.clear();
									break;
								}
							}
							if(checkedURLs.size() > 0 ) {
								var QueryResult = Azrael.SQLReplaceURLBlacklist(url, e.getGuild().getIdLong(), (cache.getAdditionalInfo().split("-")[0].equals("add") ? false : true));
								if(QueryResult == 0) {
									e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTEBIN_URL)).build()).queue();
									Hashes.removeURLBlacklist(e.getGuild().getIdLong());
									logger.info("User {} has inserted urls with the pastebin url {} into the prohibited urls in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
								}
								else if(QueryResult == 1) {
									//throw error for failing the db replacement
									message.setColor(Color.RED);
									var duplicates = checkDuplicates(url);
									if(duplicates == null || duplicates.size() == 0) {
										e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
										logger.error("The prohibited urls list couldn't be updated with the pastebin url {} in guild {}", _message, e.getGuild().getId());
									}
									else {
										StringBuilder out = new StringBuilder();
										for(var word : duplicates) {
											out.append("**"+word+"**\n");
										}
										e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_DUPLICATES)+out.toString()).build()).queue();
									}
								}
								else {
									//thow error for failing the rollback
									message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
									e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_ROLLBACK_ERR)).build()).queue();
									logger.error("Changes on the prohibited urls couldn't be rolled back on error in guild {}", e.getGuild().getId());
								}
							}
							else {
								message.setColor(Color.RED);
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_NO_URL)).build()).queue();
							}
						} catch (MalformedURLException | RuntimeException | LoginException | ParseException e2) {
							message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_NOT_PASTE));
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
							logger.error("Reading pastebin url {} for the prohibited urls failed in guild {}", _message, e.getGuild().getId(), e2);
						}
						Hashes.clearTempCache(key);
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PASTE_READ_ERR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_PROHIBITED_URLS.getColumn(), _message);
				}
				case "insert-allowed-urls" -> {
					if((_message.startsWith("http://") || _message.startsWith("https://")) && !_message.matches("[\\s]")) {
						if(Azrael.SQLInsertURLWhitelist(_message, e.getGuild().getIdLong()) >= 0) {
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_INSERT_URL)).build()).queue();
							Hashes.removeURLBlacklist(e.getGuild().getIdLong());
							logger.info("User {} has saved the url {} as an allowed url in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
						}
						else {
							message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("URL {} couldn't be saved as an allowed url in guild {}", _message, e.getGuild().getId());
						}
						Hashes.clearTempCache(key);
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_URL));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.URL_INVALID)).build()).queue();
						cache.setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_ALLOWED_URLS.getColumn(), _message);
				}
				case "remove-allowed-urls" -> {
					final var result = Azrael.SQLDeleteURLWhitelist(_message, e.getGuild().getIdLong());
					if(result > 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_REMOVE_URL)).build()).queue();
						Hashes.removeURLBlacklist(e.getGuild().getIdLong());
						logger.info("User {} has removed the url {} from the allowed urls in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
					}
					else if(result == 0) {
						message.setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_URL_REMOVE_ERR)).build()).queue();
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("URL {} couldn't be removed from the allowed urls in guild {}", _message, e.getGuild().getId());
					}
					Hashes.clearTempCache(key);
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_ALLOWED_URLS.getColumn(), _message);
				}
				case "add-load-allowed-urls", "load-allowed-urls" -> {
					if(_message.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _message.startsWith("http")) {
						try {
							String [] url = Pastebin.readPasteLink(_message).split("[\\r\\n]+");
							List<String> checkedURLs = new ArrayList<String>();
							for(var link : url) {
								if(link.matches("^(http:\\/\\/|https:\\/\\/)[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}$")) {
									checkedURLs.add(link);
								}
								else {
									checkedURLs.clear();
									break;
								}
							}
							if(checkedURLs.size() > 0) {
								var QueryResult = Azrael.SQLReplaceURLWhitelist(url, e.getGuild().getIdLong(), (cache.getAdditionalInfo().split("-")[0].equals("add") ? false : true));
								if(QueryResult == 0) {
									e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTEBIN_URL)).build()).queue();
									Hashes.removeURLWhitelist(e.getGuild().getIdLong());
									logger.info("User {} has inserted urls with the pastebin url {} into the allowed urls in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
								}
								else if(QueryResult == 1) {
									//throw error for failing the db replacement
									message.setColor(Color.RED);
									var duplicates = checkDuplicates(url);
									if(duplicates == null || duplicates.size() == 0) {
										e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
										logger.error("The allowed urls list couldn't be updated with the pastebin url {} in guild {}", _message, e.getGuild().getId());
									}
									else {
										StringBuilder out = new StringBuilder();
										for(var word : duplicates) {
											out.append("**"+word+"**\n");
										}
										e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_DUPLICATES)+out.toString()).build()).queue();
									}
								}
								else {
									//thow error for failing the rollback
									message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
									e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_ROLLBACK_ERR)).build()).queue();
									logger.error("Changes on the allowed urls couldn't be rolled back on error in guild {}", e.getGuild().getId());
								}
							}
							else {
								message.setColor(Color.RED);
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_NO_URL)).build()).queue();
							}
						} catch (MalformedURLException | RuntimeException | LoginException | ParseException e2) {
							message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_NOT_PASTE));
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
							logger.error("Reading pastebin url {} for the allowed urls failed in guild {}", _message, e.getGuild().getId(), e2);
						}
						Hashes.clearTempCache(key);
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PASTE_READ_ERR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_ALLOWED_URLS.getColumn(), _message);
				}
				case "insert-prohibited-subs" -> {
					if(Azrael.SQLInsertSubscriptionBlacklist(_message, e.getGuild().getIdLong()) >= 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_INSERT_NICK)).build()).queue();
						Hashes.removeTweetBlacklist(e.getGuild().getIdLong());
						logger.info("User {} has saved the username {} as a prohibited subscription in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Username {} couldn't be saved as a prohibited subscription in guild {}", _message, e.getGuild().getId());
					}
					Hashes.clearTempCache(key);
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_PROHIBITED_SUBS.getColumn(), _message);
				}
				case "remove-prohibited-subs" -> {
					final var result = Azrael.SQLDeleteSubscriptionBlacklist(_message, e.getGuild().getIdLong());
					if(result > 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_REMOVE_NICK)).build()).queue();
						Hashes.removeTweetBlacklist(e.getGuild().getIdLong());
						logger.info("User {} has removed the username {} and is no longer a prohibited subscription in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
					}
					else if(result == 0) {
						message.setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_TWEET_REMOVE_ERR)).build()).queue();
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Username {} couldn't be removed and is still available as a prohibited subscription in guild {}", _message, e.getGuild().getId());
					}
					Hashes.clearTempCache(key);
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_PROHIBITED_SUBS.getColumn(), _message);
				}
				case "add-load-prohibited-subs", "load-prohibited-subs" -> {
					if(_message.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _message.startsWith("http")) {
						try {
							String [] usernames = Pastebin.readPasteLink(_message).split("[\\r\\n]+");
							List<String> checkedUsernames = new ArrayList<String>();
							for(var username : usernames) {
								if(username.startsWith("@")) {
									checkedUsernames.add(username);
								}
								else {
									checkedUsernames.clear();
									break;
								}
							}
							if(checkedUsernames.size() > 0) {
								var QueryResult = Azrael.SQLReplaceTweetBlacklist(usernames, e.getGuild().getIdLong(), (cache.getAdditionalInfo().split("-")[0].equals("add") ? false : true));
								if(QueryResult == 0) {
									e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTEBIN_NICK)).build()).queue();
									Hashes.removeURLWhitelist(e.getGuild().getIdLong());
									logger.info("User {} has inserted usernames with the pastebin url {} to prohibit subscriptions from in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
								}
								else if(QueryResult == 1) {
									//throw error for failing the db replacement
									message.setColor(Color.RED);
									var duplicates = checkDuplicates(usernames);
									if(duplicates == null || duplicates.size() == 0) {
										e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
										logger.warn("The list with prohibited subscriptions couldn't be updated with the pastebin url {} in guild {}", _message, e.getGuild().getId());
									}
									else {
										StringBuilder out = new StringBuilder();
										for(var word : duplicates) {
											out.append("**"+word+"**\n");
										}
										e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_DUPLICATES)).build()).queue();
									}
								}
								else {
									//thow error for failing the rollback
									message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
									e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_ROLLBACK_ERR)).build()).queue();
									logger.error("Changes on the prohibited subscriptions couldn't be rolled back on error in guild {}", e.getGuild().getId());
								}
							}
							else {
								message.setColor(Color.RED);
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_NO_NICK)).build()).queue();
							}
						} catch (MalformedURLException | RuntimeException | LoginException | ParseException e2) {
							message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PASTE_READ_ERR));
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
							logger.error("Reading pastebin url {} for updating prohibited subscriptions failed in guild {}", _message, e.getGuild().getId(), e2);
						}
						Hashes.clearTempCache(key);
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PASTE_READ_ERR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
					Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.FILTER_PROHIBITED_SUBS.getColumn(), _message);
				}
			}
		}
		else {
			Hashes.clearTempCache(key);
		}
	}
	
	private static void callFilterLangContent(GuildMessageReceivedEvent e, EmbedBuilder message, final String key, final String lang, Cache cache) {
		final var selectedLang = langCheck(e, message, lang, key, cache);
		if(selectedLang != null) {
			var actualLang = selectedLang.split("-")[0];
			var langAbbreviation = "";
			var definitiveLang = "";
			switch(actualLang) {
				case "eng" 	-> {langAbbreviation = "eng";  definitiveLang = STATIC.getTranslation(e.getMember(), Translation.LANG_ENG);}
				case "ger" 	-> {langAbbreviation = "ger";  definitiveLang = STATIC.getTranslation(e.getMember(), Translation.LANG_GER);}
				case "fre" 	-> {langAbbreviation = "fre";  definitiveLang = STATIC.getTranslation(e.getMember(), Translation.LANG_FRE);}
				case "tur" 	-> {langAbbreviation = "tur";  definitiveLang = STATIC.getTranslation(e.getMember(), Translation.LANG_TUR);}
				case "rus" 	-> {langAbbreviation = "rus";  definitiveLang = STATIC.getTranslation(e.getMember(), Translation.LANG_RUS);}
				case "spa" 	-> {langAbbreviation = "spa";  definitiveLang = STATIC.getTranslation(e.getMember(), Translation.LANG_SPA);}
				case "por"	-> {langAbbreviation = "por";  definitiveLang = STATIC.getTranslation(e.getMember(), Translation.LANG_POR);}
				case "ita" 	-> {langAbbreviation = "ita";  definitiveLang = STATIC.getTranslation(e.getMember(), Translation.LANG_ITA);}
				case "ara" 	-> {langAbbreviation = "ara";  definitiveLang = STATIC.getTranslation(e.getMember(), Translation.LANG_ARA);}
			}
			
			if(!langAbbreviation.isBlank()) {
				StringBuilder out = new StringBuilder();
				for(String word : Azrael.SQLgetFilter(langAbbreviation, e.getGuild().getIdLong())) {
					out.append(word+"\n");
				}
				if(out.length() > 0) {
					try {
						String paste_link = Pastebin.unlistedPaste(definitiveLang, out.toString());
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LIST)+paste_link).build()).queue();
						logger.info("User {} has called the {} word filter in guild {}", e.getMember().getUser().getIdLong(), actualLang, e.getGuild().getId(), e.getGuild().getId());
					} catch (IllegalStateException | LoginException | PasteException e2) {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PASTE));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_PASTE_ERR)).build()).queue();
						logger.error("Pastebin creation for the {} word filter failed in guild {}", actualLang, e.getGuild().getId(), e2);
					}
				}
				else {
					message.setColor(Color.RED);
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LIST_EMPTY)).build()).queue();
				}
				Hashes.clearTempCache(key);
			}
			else {
				message.setColor(Color.RED);
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LIST_LANG_NA)).build()).queue();
				Hashes.addTempCache(key, cache.setExpiration(180000));
			}
		}
	}
	
	private static void insertLangWord(GuildMessageReceivedEvent e, EmbedBuilder message, final String key, final String lang, String word) {
		if(Azrael.SQLInsertWordFilter(lang, CharacterReplacer.simpleReplace(word), e.getGuild().getIdLong()) >= 0) {
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_INSERT)).build()).queue();
			clearHash(e, lang, true);
			Hashes.removeQueryResult("all_"+e.getGuild().getId());
			logger.info("User {} has inserted the word {} into the {} word filter in guild {}", e.getMember().getUser().getIdLong(), word, lang, e.getGuild().getId());
		}
		else {
			message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Word {} couldn't be inserted into the {} word filter in guild {}", word, lang, e.getGuild().getId());
		}
		Hashes.clearTempCache(key);
	}
	
	private static void removeLangWord(GuildMessageReceivedEvent e, EmbedBuilder message, final String key, final String lang, String word) {
		final var result = Azrael.SQLDeleteWordFilter(lang, CharacterReplacer.simpleReplace(word), e.getGuild().getIdLong());
		if(result > 0) {
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_REMOVE)).build()).queue();
			clearHash(e, lang, true);
			Hashes.removeQueryResult("all_"+e.getGuild().getId());
			logger.info("User {} has removed the word {} from the {} word filter in guild {}", e.getMember().getUser().getIdLong(), word, lang, e.getGuild().getId());
		}
		else if(result == 0) {
			message.setColor(Color.RED);
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WORD_REMOVE_ERR)).build()).queue();
		}
		else {
			message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Word {} couldn't be removed from the {} word filter in guild {}", word, lang, e.getGuild().getId());
		}
		Hashes.clearTempCache(key);
	}
	
	private static void loadLangWords(GuildMessageReceivedEvent e, EmbedBuilder message, final String key, final String lang, String _message, boolean replace, Cache cache) {
		if(_message.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _message.startsWith("http")) {
			var langAbbreviation = lang;
			String[] words;
			try {
				words = Pastebin.readPasteLink(_message).split("[\\r\\n]+");
				var QueryResult = Azrael.SQLReplaceWordFilter(langAbbreviation, words, e.getGuild().getIdLong(), replace);
				if(QueryResult == 0) {
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_ADD_PASTEBIN)).build()).queue();
					clearHash(e, lang, false);
					Hashes.removeQueryResult("all_"+e.getGuild().getId());
					logger.info("User {} has inserted words with the pastebin url {} into the {} word filter in guild {}", e.getMember().getUser().getIdLong(), _message, lang, e.getGuild().getId());
				}
				else if(QueryResult == 1) {
					//throw error for failing the db replacement
					message.setColor(Color.RED);
					var duplicates = checkDuplicates(words);
					if(duplicates == null || duplicates.size() == 0) {
						e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("The {} word filter couldn't be updated with the pastebin url {} in guild {}", lang, _message, e.getGuild().getId());
					}
					else {
						StringBuilder out = new StringBuilder();
						for(var word : duplicates) {
							out.append("**"+word+"**\n");
						}
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_DUPLICATES)+out.toString()).build()).queue();
					}
				}
				else {
					//throw error for failing the rollback
					message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_ROLLBACK_ERR)).build()).queue();
					logger.error("Changes on the {} word filter couldn't be rolled back on error in guild {}", lang, e.getGuild().getId());
				}
			} catch (MalformedURLException | RuntimeException | LoginException | ParseException e2) {
				message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_NOT_PASTE));
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
				logger.error("Reading pastebin url {} for the {} word filter failed in guild {}", _message, lang, e.getGuild().getId(), e2);
			}
			Hashes.clearTempCache(key);
		}
		else {
			message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PASTE_READ_ERR));
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
			Hashes.addTempCache(key, cache.setExpiration(180000));
		}
	}
	
	private static void clearHash(GuildMessageReceivedEvent e, final String lang, final boolean allowAll) {
		if(lang.equals("eng") || (allowAll && lang.equals("all")))
			Hashes.removeQueryResult("eng_"+e.getGuild().getId());
		if(lang.equals("ger") || (allowAll && lang.equals("all")))
			Hashes.removeQueryResult("ger_"+e.getGuild().getId());
		if(lang.equals("fre") || (allowAll && lang.equals("all")))
			Hashes.removeQueryResult("fre_"+e.getGuild().getId());
		if(lang.equals("tur") || (allowAll && lang.equals("all")))
			Hashes.removeQueryResult("tur_"+e.getGuild().getId());
		if(lang.equals("rus") || (allowAll && lang.equals("all")))
			Hashes.removeQueryResult("rus_"+e.getGuild().getId());
		if(lang.equals("spa") || (allowAll && lang.equals("all")))
			Hashes.removeQueryResult("spa_"+e.getGuild().getId());
		if(lang.equals("por") || (allowAll && lang.equals("all")))
			Hashes.removeQueryResult("por_"+e.getGuild().getId());
		if(lang.equals("ita") || (allowAll && lang.equals("all")))
			Hashes.removeQueryResult("ita_"+e.getGuild().getId());
		if(lang.equals("ara") || (allowAll && lang.equals("all")))
			Hashes.removeQueryResult("ara_"+e.getGuild().getId());
	}
	
	private static void printFilterActions(GuildMessageReceivedEvent e, EmbedBuilder message) {
		e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_ACTIONS)
				.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_DISPLAY))
				.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_INSERT))
				.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))
				.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_PASTEBIN))
				.replaceFirst("\\{\\}", STATIC.getTranslation(e.getMember(), Translation.PARAM_LOAD_PASTEBIN))).build()).queue();
	}
	
	private static List<String> checkDuplicates(String [] words) {
		ArrayList<String> duplicates = new ArrayList<String>();
		for(int i = 0; i < words.length; i++) {
			var word = words[i];
			for(int y = 0; y < words.length; y++) {
				final var iteration = y;
				if(i != y && word.equalsIgnoreCase(words[y]) && duplicates.parallelStream().filter(f -> f.equalsIgnoreCase(words[iteration])).findAny().orElse(null) == null) {
					duplicates.add(words[y]);
					break;
				}
			}
		}
		return duplicates;
	}
	
	private static String langCheck(final GuildMessageReceivedEvent e, final EmbedBuilder message, final String lang, final String key, Cache cache) {
		final var langs = Azrael.SQLgetLanguages(STATIC.getLanguage(e.getMember()));
		if(langs != null && langs.size() > 0) {
			final var selectedLang = langs.parallelStream().filter(f -> f.split("-")[1].equalsIgnoreCase(lang)).findAny().orElse(null);
			if(selectedLang != null) {
				return selectedLang;
			}
			else {
				e.getChannel().sendMessage(message.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LIST_LANG_NA)).build()).queue();
				Hashes.addTempCache(key, cache.setExpiration(180000));
				return null;
			}
		}
		else {
			e.getChannel().sendMessage(message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Supported languages couldn't be retrieved in guild {}", e.getGuild().getId());
			Hashes.clearTempCache(key);
			return null;
		}
	}
}