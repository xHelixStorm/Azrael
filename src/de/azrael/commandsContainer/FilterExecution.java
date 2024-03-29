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
import de.azrael.enums.Translation;
import de.azrael.fileManagement.GuildIni;
import de.azrael.fileManagement.IniFileReader;
import de.azrael.sql.Azrael;
import de.azrael.util.CharacterReplacer;
import de.azrael.util.Pastebin;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class FilterExecution {
	private final static Logger logger = LoggerFactory.getLogger(FilterExecution.class);
	
	public static void runHelp(GuildMessageReceivedEvent e) {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS));
		e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_HELP)).build()).queue();
	}
	
	public static void runTask(GuildMessageReceivedEvent e, String _message) {
		String key = "filter_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId();
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
		
		if(_message.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_WORD_FILTER))) {
			final var wordFilterLevel = GuildIni.getFilterWordFilterLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), wordFilterLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong()) {
				message.setTitle("WORD-FILTER");
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_ACTIONS)).build()).queue();
				Hashes.addTempCache(key, new Cache(180000, "word-filter"));
			}
			else {
				e.getChannel().sendMessage(message.setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(wordFilterLevel, e.getMember())).build()).queue();
			}
		}
		else if(_message.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_NAME_FILTER))) {
			final var nameFilterLevel = GuildIni.getFilterNameFilterLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), nameFilterLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong()) {
				message.setTitle("NAME-FILTER");
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_ACTIONS)).build()).queue();
				Hashes.addTempCache(key, new Cache(180000, "name-filter"));
			}
			else {
				e.getChannel().sendMessage(message.setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(nameFilterLevel, e.getMember())).build()).queue();
			}
		}
		else if(_message.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_NAME_KICK))) {
			final var nameKickLevel = GuildIni.getFilterNameKickLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), nameKickLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong()) {
				message.setTitle("NAME-KICK");
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_ACTIONS)).build()).queue();
				Hashes.addTempCache(key, new Cache(180000, "name-kick"));
			}
			else {
				e.getChannel().sendMessage(message.setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(nameKickLevel, e.getMember())).build()).queue();
			}
		}
		else if(_message.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_FUNNY_NAMES))) {
			final var funnyNamesLevel = GuildIni.getFilterFunnyNamesLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), funnyNamesLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong()) {
				message.setTitle("FUNNY-NAMES");
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_ACTIONS)).build()).queue();
				Hashes.addTempCache(key, new Cache(180000, "funny-names"));
			}
			else {
				e.getChannel().sendMessage(message.setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(funnyNamesLevel, e.getMember())).build()).queue();
			}
		}
		else if(_message.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_STAFF_NAMES))) {
			final var staffNamesLevel = GuildIni.getFilterStaffNamesLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), staffNamesLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong()) {
				message.setTitle("STAFF-NAMES");
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_ACTIONS)).build()).queue();
				Hashes.addTempCache(key, new Cache(180000, "staff-names"));
			}
			else {
				e.getChannel().sendMessage(message.setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(staffNamesLevel, e.getMember())).build()).queue();
			}
		}
		else if(_message.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_PROHIBITED_URLS))) {
			final var urlBlacklistLevel = GuildIni.getFilterURLBlacklistLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), urlBlacklistLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong()) {
				message.setTitle("URL-BLACKLIST");
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_ACTIONS)).build()).queue();
				Hashes.addTempCache(key, new Cache(180000, "url-blacklist"));
			}
			else {
				e.getChannel().sendMessage(message.setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(urlBlacklistLevel, e.getMember())).build()).queue();
			}
		}
		else if(_message.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_ALLOWED_URLS))) {
			final var urlWhitelistLevel = GuildIni.getFilterURLWhitelistLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), urlWhitelistLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong()) {
				message.setTitle("URL-WHITELIST");
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_ACTIONS)).build()).queue();
				Hashes.addTempCache(key, new Cache(180000, "url-whitelist"));
			}
			else {
				e.getChannel().sendMessage(message.setColor(Color.RED).setThumbnail(IniFileReader.getDeniedThumbnail()).setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(urlWhitelistLevel, e.getMember())).build()).queue();
			}
		}
		else if(_message.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_PROHIBITED_SUBS))) {
			final var tweetBlacklistLevel = GuildIni.getFilterTweetBlacklistLevel(e.getGuild().getIdLong());
			if(UserPrivs.comparePrivilege(e.getMember(), tweetBlacklistLevel) || GuildIni.getAdmin(e.getGuild().getIdLong()) == e.getMember().getUser().getIdLong()) {
				message.setTitle("TWEET-BLACKLIST");
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_ACTIONS)).build()).queue();
				Hashes.addTempCache(key, new Cache(180000, "tweet-blacklist"));
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
								String paste_link = Pastebin.unlistedPaste("NAME-FILTER", out.toString(), e.getGuild().getIdLong());
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
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_INSERT))) {
						message.setTitle("NAME-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_INSERT));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_WORD)).build()).queue();
						cache.updateDescription("insert-name-filter").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))) {
						message.setTitle("NAME-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_REMOVE));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_WORD)).build()).queue();
						cache.updateDescription("remove-name-filter").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_PASTEBIN))) {
						message.setTitle("NAME-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_ADD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE)).build()).queue();
						cache.updateDescription("add-load-name-filter").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_LOAD_PASTEBIN))) {
						message.setTitle("NAME-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_LOAD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE)).build()).queue();
						cache.updateDescription("load-name-filter").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
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
								String paste_link = Pastebin.unlistedPaste("NAME-KICK", out.toString(), e.getGuild().getIdLong());
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
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_INSERT))) {
						message.setTitle("NAME-KICK "+STATIC.getTranslation(e.getMember(), Translation.FILTER_INSERT));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_WORD)).build()).queue();
						cache.updateDescription("insert-name-kick").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))) {
						message.setTitle("NAME-KICK "+STATIC.getTranslation(e.getMember(), Translation.FILTER_REMOVE));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_WORD)).build()).queue();
						cache.updateDescription("remove-name-kick").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_PASTEBIN))) {
						message.setTitle("NAME-KICK "+STATIC.getTranslation(e.getMember(), Translation.FILTER_ADD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE)).build()).queue();
						cache.updateDescription("add-load-name-kick").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_LOAD_PASTEBIN))) {
						message.setTitle("NAME-KICK "+STATIC.getTranslation(e.getMember(), Translation.FILTER_LOAD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE)).build()).queue();
						cache.updateDescription("load-name-kick").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
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
								String paste_link = Pastebin.unlistedPaste("FUNNY-NAMES", out.toString(), e.getGuild().getIdLong());
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
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_INSERT))) {
						message.setTitle("FUNNY-NAMES "+STATIC.getTranslation(e.getMember(), Translation.FILTER_INSERT));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_WORD)).build()).queue();
						cache.updateDescription("insert-funny-names").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))) {
						message.setTitle("FUNNY-NAMES "+STATIC.getTranslation(e.getMember(), Translation.FILTER_REMOVE));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_WORD)).build()).queue();
						cache.updateDescription("remove-funny-names").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_PASTEBIN))) {
						message.setTitle("FUNNY-NAMES "+STATIC.getTranslation(e.getMember(), Translation.FILTER_ADD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE)).build()).queue();
						cache.updateDescription("add-load-funny-names").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_LOAD_PASTEBIN))) {
						message.setTitle("FUNNY-NAMES "+STATIC.getTranslation(e.getMember(), Translation.FILTER_LOAD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE)).build()).queue();
						cache.updateDescription("load-funny-names").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
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
								String paste_link = Pastebin.unlistedPaste("STAFF-NAMES", out.toString(), e.getGuild().getIdLong());
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
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_INSERT))) {
						message.setTitle("STAFF-NAMES "+STATIC.getTranslation(e.getMember(), Translation.FILTER_INSERT));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_WORD)).build()).queue();
						cache.updateDescription("insert-staff-names").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))) {
						message.setTitle("STAFF-NAMES "+STATIC.getTranslation(e.getMember(), Translation.FILTER_REMOVE));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_WORD)).build()).queue();
						cache.updateDescription("remove-staff-names").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_PASTEBIN))) {
						message.setTitle("STAFF-NAMES "+STATIC.getTranslation(e.getMember(), Translation.FILTER_ADD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE)).build()).queue();
						cache.updateDescription("add-load-staff-names").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_LOAD_PASTEBIN))) {
						message.setTitle("STAFF-NAMES "+STATIC.getTranslation(e.getMember(), Translation.FILTER_LOAD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE)).build()).queue();
						cache.updateDescription("load-staff-names").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
				}
				case "url-blacklist" -> {
					if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISPLAY))) {
						StringBuilder out = new StringBuilder();
						for(String word : Azrael.SQLgetURLBlacklist(e.getGuild().getIdLong())) {
							out.append(word+"\n");
						}
						if(out.length() > 0) {
							try {
								String paste_link = Pastebin.unlistedPaste("URL-BLACKLIST", out.toString(), e.getGuild().getIdLong());
								message.setTitle("URL-BLACKLIST "+STATIC.getTranslation(e.getMember(), Translation.FILTER_DISPLAY));
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LIST)+paste_link).build()).queue();
							} catch (IllegalStateException | LoginException | PasteException e2) {
								message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PASTE));
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_PASTE_ERR)).build()).queue();
								logger.warn("Error on creating pastebin with blacklisted urls in guild {}", e.getGuild().getId(), e2);
							}
						}
						else {
							message.setColor(Color.RED);
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LIST_EMPTY)).build()).queue();
						}
						Hashes.clearTempCache(key);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_INSERT))) {
						message.setTitle("URL-BLACKLIST "+STATIC.getTranslation(e.getMember(), Translation.FILTER_INSERT));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_FQDN)).build()).queue();
						cache.updateDescription("insert-url-blacklist").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))) {
						message.setTitle("URL-BLACKLIST "+STATIC.getTranslation(e.getMember(), Translation.FILTER_REMOVE));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_FQDN)).build()).queue();
						cache.updateDescription("remove-url-blacklist").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_PASTEBIN))) {
						message.setTitle("URL-BLACKLIST "+STATIC.getTranslation(e.getMember(), Translation.FILTER_ADD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE_FQDN)).build()).queue();
						cache.updateDescription("add-load-url-blacklist").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_LOAD_PASTEBIN))) {
						message.setTitle("URL-BLACKLIST "+STATIC.getTranslation(e.getMember(), Translation.FILTER_LOAD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE_FQDN)).build()).queue();
						cache.updateDescription("load-url-blacklist").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
				}
				case "url-whitelist" -> {
					if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISPLAY))) {
						StringBuilder out = new StringBuilder();
						for(String word : Azrael.SQLgetURLWhitelist(e.getGuild().getIdLong())) {
							out.append(word+"\n");
						}
						if(out.length() > 0) {
							try {
								String paste_link = Pastebin.unlistedPaste("URL-WHITELIST", out.toString(), e.getGuild().getIdLong());
								message.setTitle("URL-WHITELIST "+STATIC.getTranslation(e.getMember(), Translation.FILTER_DISPLAY));
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
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_INSERT))) {
						message.setTitle("URL-WHITELIST "+STATIC.getTranslation(e.getMember(), Translation.FILTER_INSERT));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_FQDN)).build()).queue();
						cache.updateDescription("insert-url-whitelist").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))) {
						message.setTitle("URL-WHITELIST "+STATIC.getTranslation(e.getMember(), Translation.FILTER_REMOVE));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_FQDN)).build()).queue();
						cache.updateDescription("remove-url-whitelist").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_PASTEBIN))) {
						message.setTitle("URL-WHITELIST "+STATIC.getTranslation(e.getMember(), Translation.FILTER_ADD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE_FQDN)).build()).queue();
						cache.updateDescription("add-load-url-whitelist").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_LOAD_PASTEBIN))) {
						message.setTitle("URL-WHITELIST "+STATIC.getTranslation(e.getMember(), Translation.FILTER_LOAD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE_FQDN)).build()).queue();
						cache.updateDescription("load-url-whitelist").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
				}
				case "tweet-blacklist" -> {
					if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISPLAY))) {
						StringBuilder out = new StringBuilder();
						for(String word : Azrael.SQLgetSubscriptionBlacklist(e.getGuild().getIdLong())) {
							out.append(word+"\n");
						}
						if(out.length() > 0) {
							try {
								String paste_link = Pastebin.unlistedPaste("TWEET-BLACKLIST", out.toString(), e.getGuild().getIdLong());
								message.setTitle("TWEET-BLACKLIST "+STATIC.getTranslation(e.getMember(), Translation.FILTER_DISPLAY));
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LIST)+paste_link).build()).queue();
							} catch (IllegalStateException | LoginException | PasteException e2) {
								message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PASTE));
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_PASTE_ERR)).build()).queue();
								logger.warn("Error on creating pastebin with blacklisted tweeters in guild {}!", e.getGuild().getId(), e2);
							}
						}
						else {
							message.setColor(Color.RED);
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LIST_EMPTY)).build()).queue();
						}
						Hashes.clearTempCache(key);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_INSERT))) {
						message.setTitle("TWEET-BLACKLIST "+STATIC.getTranslation(e.getMember(), Translation.FILTER_INSERT));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_USERNAME)).build()).queue();
						cache.updateDescription("insert-tweet-blacklist").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))) {
						message.setTitle("TWEET-BLACKLIST "+STATIC.getTranslation(e.getMember(), Translation.FILTER_REMOVE));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_USERNAME)).build()).queue();
						cache.updateDescription("remove-tweet-blacklist").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_PASTEBIN))) {
						message.setTitle("TWEET-BLACKLIST "+STATIC.getTranslation(e.getMember(), Translation.FILTER_ADD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE_USERNAME)).build()).queue();
						cache.updateDescription("add-load-tweet-blacklist").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_LOAD_PASTEBIN))) {
						message.setTitle("TWEET-BLACKLIST "+STATIC.getTranslation(e.getMember(), Translation.FILTER_LOAD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE_USERNAME)).build()).queue();
						cache.updateDescription("load-tweet-blacklist").setExpiration(180000);
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
				}
				case "display-word-filter" -> callFilterLangContent(e, message, key, _message.toLowerCase(), cache);
				case "insert-word-filter" -> {
					var langInsert = _message.toLowerCase();
					final var selectedLang = langCheck(e, message, langInsert, key, cache);
					if(selectedLang != null) {
						message.setTitle("WORD-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_INSERT)+" "+langInsert.toUpperCase());
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_WORD)).build()).queue();
						cache.updateDescription("lang-insert-word-filter").updateDescription2(selectedLang.split("-")[0]).setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
				}
				case "lang-insert-word-filter" -> insertLangWord(e, message, key, cache.getAdditionalInfo2(), _message);
				case "remove-word-filter" -> {
					var langRemove = _message.toLowerCase();
					final var selectedLang = langCheck(e, message, langRemove, key, cache);
					if(selectedLang != null) {
						message.setTitle("WORD-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_REMOVE)+" "+langRemove.toUpperCase());
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_WORD)).build()).queue();
						cache.updateDescription("lang-remove-word-filter").updateDescription2(selectedLang.split("-")[0]).setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
				}
				case "lang-remove-word-filter" -> removeLangWord(e, message, key, cache.getAdditionalInfo2(), _message);
				case "add-load-word-filter" -> {
					var addLangLoad = _message.toLowerCase();
					final var selectedLang = langCheck(e, message, addLangLoad, key, cache);
					if(selectedLang != null) {
						message.setTitle("WORD-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_ADD_PASTEBIN)+" "+addLangLoad.toUpperCase());
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE)).build()).queue();
						cache.updateDescription("lang-add-load-word-filter").updateDescription2(selectedLang.split("-")[0]).setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
				}
				case "lang-add-load-word-filter" -> loadLangWords(e, message, key, cache.getAdditionalInfo2(), _message, false, cache);
				case "load-word-filter" -> {
					var langLoad = _message.toLowerCase();
					final var selectedLang = langCheck(e, message, langLoad, key, cache);
					if(selectedLang != null) {
						message.setTitle("WORD-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_LOAD_PASTEBIN)+" "+langLoad.toUpperCase());
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE)).build()).queue();
						cache.updateDescription("lang-load-word-filter").updateDescription2(selectedLang.split("-")[0]).setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
				}
				case "lang-load-word-filter" -> loadLangWords(e, message, key, cache.getAdditionalInfo2(), _message, true, cache);
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
				}
				case "load-name-filter", "add-load-name-filter" -> {
					if(_message.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _message.startsWith("http")) {
						try {
							String [] words = Pastebin.readPasteLink(_message, e.getGuild().getIdLong()).split("[\\r\\n]+");
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
				}
				case "load-name-kick", "add-load-name-kick" -> {
					if(_message.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _message.startsWith("http")) {
						try {
							String [] words = Pastebin.readPasteLink(_message, e.getGuild().getIdLong()).split("[\\r\\n]+");
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
				}
				case "load-funny-names", "add-load-funny-names" -> {
					if(_message.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _message.startsWith("http")) {
						try {
							String [] words = Pastebin.readPasteLink(_message, e.getGuild().getIdLong()).split("[\\r\\n]+");
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
				}
				case "load-staff-names", "add-load-staff-names" -> {
					if(_message.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _message.startsWith("http")) {
						try {
							String [] words = Pastebin.readPasteLink(_message, e.getGuild().getIdLong()).split("[\\r\\n]+");
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
				}
				case "insert-url-blacklist" -> {
					if((_message.startsWith("http://") || _message.startsWith("https://")) && !_message.matches("[\\s]")) {
						if(Azrael.SQLInsertURLBlacklist(_message, e.getGuild().getIdLong()) >= 0) {
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_INSERT_URL)).build()).queue();
							Hashes.removeURLBlacklist(e.getGuild().getIdLong());
							logger.info("User {} has inserted the url {} into the url blacklist in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
						}
						else {
							message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("URL {} couldn't be inserted into the url blacklist in guild {}", _message, e.getGuild().getId());
						}
						Hashes.clearTempCache(key);
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_URL));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.URL_INVALID)).build()).queue();
						cache.setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
				}
				case "remove-url-blacklist" -> {
					final var result = Azrael.SQLDeleteURLBlacklist(_message, e.getGuild().getIdLong());
					if(result > 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_REMOVE_URL)).build()).queue();
						Hashes.removeURLBlacklist(e.getGuild().getIdLong());
						logger.info("User {} has removed the url {} from the url blacklist in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
					}
					else if(result == 0) {
						message.setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_URL_REMOVE_ERR)).build()).queue();
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("URL {} couldn't be removed from the url blacklist in guild {}", _message, e.getGuild().getId());
					}
					Hashes.clearTempCache(key);
				}
				case "add-load-url-blacklist", "load-url-blacklist" -> {
					if(_message.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _message.startsWith("http")) {
						try {
							String [] url = Pastebin.readPasteLink(_message, e.getGuild().getIdLong()).split("[\\r\\n]+");
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
									logger.info("User {} has inserted urls with the pastebin url {} into the url blacklist in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
								}
								else if(QueryResult == 1) {
									//throw error for failing the db replacement
									message.setColor(Color.RED);
									var duplicates = checkDuplicates(url);
									if(duplicates == null || duplicates.size() == 0) {
										e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
										logger.error("The url blacklist couldn't be updated with the pastebin url {} in guild {}", _message, e.getGuild().getId());
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
									logger.error("Changes on the url blacklist couldn't be rolled back on error in guild {}", e.getGuild().getId());
								}
							}
							else {
								message.setColor(Color.RED);
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_NO_URL)).build()).queue();
							}
						} catch (MalformedURLException | RuntimeException | LoginException | ParseException e2) {
							message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_NOT_PASTE));
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
							logger.error("Reading pastebin url {} for the url blacklist failed in guild {}", _message, e.getGuild().getId(), e2);
						}
						Hashes.clearTempCache(key);
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PASTE_READ_ERR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
				}
				case "insert-url-whitelist" -> {
					if((_message.startsWith("http://") || _message.startsWith("https://")) && !_message.matches("[\\s]")) {
						if(Azrael.SQLInsertURLWhitelist(_message, e.getGuild().getIdLong()) >= 0) {
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_INSERT_URL)).build()).queue();
							Hashes.removeURLBlacklist(e.getGuild().getIdLong());
							logger.info("User {} has inserted the url {} into the url-whitelist in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
						}
						else {
							message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("URL {} couldn't be inserted into the url whitelist in guild {}", _message, e.getGuild().getId());
						}
						Hashes.clearTempCache(key);
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_URL));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.URL_INVALID)).build()).queue();
						cache.setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
				}
				case "remove-url-whitelist" -> {
					final var result = Azrael.SQLDeleteURLWhitelist(_message, e.getGuild().getIdLong());
					if(result > 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_REMOVE_URL)).build()).queue();
						Hashes.removeURLBlacklist(e.getGuild().getIdLong());
						logger.info("User {} has removed the url {} from the url whitelist in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
					}
					else if(result == 0) {
						message.setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_URL_REMOVE_ERR)).build()).queue();
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("URL {} couldn't be removed from the url whitelist in guild {}", _message, e.getGuild().getId());
					}
					Hashes.clearTempCache(key);
				}
				case "add-load-url-whitelist", "load-url-whitelist" -> {
					if(_message.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _message.startsWith("http")) {
						try {
							String [] url = Pastebin.readPasteLink(_message, e.getGuild().getIdLong()).split("[\\r\\n]+");
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
									logger.info("User {} has inserted urls with the pastebin url {} into the url whitelist in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
								}
								else if(QueryResult == 1) {
									//throw error for failing the db replacement
									message.setColor(Color.RED);
									var duplicates = checkDuplicates(url);
									if(duplicates == null || duplicates.size() == 0) {
										e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
										logger.error("The url whitelist couldn't be updated with the pastebin url {} in guild {}", _message, e.getGuild().getId());
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
									logger.error("Changes on the url whitelist couldn't be rolled back on error in guild {}", e.getGuild().getId());
								}
							}
							else {
								message.setColor(Color.RED);
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_NO_URL)).build()).queue();
							}
						} catch (MalformedURLException | RuntimeException | LoginException | ParseException e2) {
							message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_NOT_PASTE));
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
							logger.error("Reading pastebin url {} for the url blacklist failed in guild {}", _message, e.getGuild().getId(), e2);
						}
						Hashes.clearTempCache(key);
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PASTE_READ_ERR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
				}
				case "insert-tweet-blacklist" -> {
					if(Azrael.SQLInsertSubscriptionBlacklist(_message, e.getGuild().getIdLong()) >= 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_INSERT_NICK)).build()).queue();
						Hashes.removeTweetBlacklist(e.getGuild().getIdLong());
						logger.info("User {} has inserted the username {} into the tweet blacklist in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Username {} couldn't be inserted into the tweet blacklist in guild {}", _message, e.getGuild().getId());
					}
					Hashes.clearTempCache(key);
				}
				case "remove-tweet-blacklist" -> {
					final var result = Azrael.SQLDeleteSubscriptionBlacklist(_message, e.getGuild().getIdLong());
					if(result > 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_REMOVE_NICK)).build()).queue();
						Hashes.removeTweetBlacklist(e.getGuild().getIdLong());
						logger.info("User {} has removed the username {} from the tweet blacklist", e.getMember().getUser().getIdLong(), _message);
					}
					else if(result == 0) {
						message.setColor(Color.RED);
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_TWEET_REMOVE_ERR)).build()).queue();
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Username {} couldn't be removed from the tweet blacklist in guild {}", _message, e.getGuild().getId());
					}
					Hashes.clearTempCache(key);
				}
				case "add-load-tweet-blacklist", "load-tweet-blacklist" -> {
					if(_message.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _message.startsWith("http")) {
						try {
							String [] usernames = Pastebin.readPasteLink(_message, e.getGuild().getIdLong()).split("[\\r\\n]+");
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
									logger.info("User {} has inserted usernames with the pastebin url {} into the tweet blacklist in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
								}
								else if(QueryResult == 1) {
									//throw error for failing the db replacement
									message.setColor(Color.RED);
									var duplicates = checkDuplicates(usernames);
									if(duplicates == null || duplicates.size() == 0) {
										e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
										logger.warn("The tweet blacklist couldn't be updated with the pastebin url {} in guild {}", _message, e.getGuild().getId());
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
									logger.error("Changes on the tweet blacklist couldn't be rolled back on error in guild {}", e.getGuild().getId());
								}
							}
							else {
								message.setColor(Color.RED);
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_NO_NICK)).build()).queue();
							}
						} catch (MalformedURLException | RuntimeException | LoginException | ParseException e2) {
							message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PASTE_READ_ERR));
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
							logger.error("Reading pastebin url {} for the tweet blacklist failed in guild {}", _message, e.getGuild().getId(), e2);
						}
						Hashes.clearTempCache(key);
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PASTE_READ_ERR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
						Hashes.addTempCache(key, cache.setExpiration(180000));
					}
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
						String paste_link = Pastebin.unlistedPaste(definitiveLang, out.toString(), e.getGuild().getIdLong());
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
				words = Pastebin.readPasteLink(_message, e.getGuild().getIdLong()).split("[\\r\\n]+");
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