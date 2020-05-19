package commandsContainer;

import java.awt.Color;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.jpaste.exceptions.PasteException;
import org.jpaste.pastebin.exceptions.LoginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Cache;
import core.Hashes;
import core.UserPrivs;
import enums.Translation;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import sql.Azrael;
import util.Pastebin;
import util.STATIC;

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
		else if(_message.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_URL_BLACKLIST))) {
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
		else if(_message.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_URL_WHITELIST))) {
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
		else if(_message.equals(STATIC.getTranslation(e.getMember(), Translation.PARAM_TWEET_BLACKLIST))) {
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
	
	@SuppressWarnings("preview")
	public static void performAction(GuildMessageReceivedEvent e, String _message, Cache cache) {
		String key = "filter_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId();
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE);
		if(cache.getExpiration() - System.currentTimeMillis() > 0) {
			switch(cache.getAdditionalInfo()) {
				case "word-filter" -> {
					if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISPLAY))) {
						message.setTitle("WORD-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_DISPLAY));
						StringBuilder out = new StringBuilder();
						for(String lang : Azrael.SQLgetFilterLanguages()) {
							out.append(lang+"\n");
						}
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LANG_SELECTION)+"**"+(out.length() > 0 ? out.toString() : STATIC.getTranslation(e.getMember(), Translation.FILTER_NO_LANGS))+"**").build()).queue();
						cache.updateDescription("display-word-filter").setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_INSERT))) {
						message.setTitle("WORD-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_INSERT));
						StringBuilder out = new StringBuilder();
						for(String lang : Azrael.SQLgetFilterLanguages()) {
							out.append(lang+"\n");
						}
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LANG_SELECTION)+"**"+(out.length() > 0 ? out.toString() : STATIC.getTranslation(e.getMember(), Translation.FILTER_NO_LANGS))+"**").build()).queue();
						cache.updateDescription("insert-word-filter").setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))) {
						message.setTitle("WORD-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_REMOVE));
						StringBuilder out = new StringBuilder();
						for(String lang : Azrael.SQLgetFilterLanguages()) {
							out.append(lang+"\n");
						}
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LANG_SELECTION)+"**"+(out.length() > 0 ? out.toString() : STATIC.getTranslation(e.getMember(), Translation.FILTER_NO_LANGS))+"**").build()).queue();
						cache.updateDescription("remove-word-filter").setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_PASTEBIN))) {
						message.setTitle("WORD-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_ADD_PASTEBIN));
						StringBuilder out = new StringBuilder();
						for(String lang : Azrael.SQLgetFilterLanguages()) {
							out.append(lang+"\n");
						}
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LANG_SELECTION)+"**"+(out.length() > 0 ? out.toString() : STATIC.getTranslation(e.getMember(), Translation.FILTER_NO_LANGS))+"**").build()).queue();
						cache.updateDescription("add-load-word-filter").setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_LOAD_PASTEBIN))) {
						message.setTitle("WORD-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_LOAD_PASTEBIN));
						StringBuilder out = new StringBuilder();
						for(String lang : Azrael.SQLgetFilterLanguages()) {
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
								logger.warn("Error on creating paste in guild {}!", e.getGuild().getId(), e2);
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
						Hashes.addTempCache(key, cache);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))) {
						message.setTitle("NAME-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_REMOVE));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_WORD)).build()).queue();
						cache.updateDescription("remove-name-filter").setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_PASTEBIN))) {
						message.setTitle("NAME-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_ADD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTEBIN)).build()).queue();
						cache.updateDescription("add-load-name-filter").setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_LOAD_PASTEBIN))) {
						message.setTitle("NAME-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_LOAD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTEBIN)).build()).queue();
						cache.updateDescription("load-name-filter").setExpiration(180000);
						Hashes.addTempCache(key, cache);
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
								logger.warn("Error on creating paste in guild {}!", e.getGuild().getId(), e2);
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
						Hashes.addTempCache(key, cache);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))) {
						message.setTitle("NAME-KICK "+STATIC.getTranslation(e.getMember(), Translation.FILTER_REMOVE));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_WORD)).build()).queue();
						cache.updateDescription("remove-name-kick").setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_PASTEBIN))) {
						message.setTitle("NAME-KICK "+STATIC.getTranslation(e.getMember(), Translation.FILTER_ADD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTEBIN)).build()).queue();
						cache.updateDescription("add-load-name-kick").setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_LOAD_PASTEBIN))) {
						message.setTitle("NAME-KICK "+STATIC.getTranslation(e.getMember(), Translation.FILTER_LOAD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTEBIN)).build()).queue();
						cache.updateDescription("load-name-kick").setExpiration(180000);
						Hashes.addTempCache(key, cache);
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
								logger.warn("Error on creating paste in guild {}!", e.getGuild().getId(), e2);
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
						Hashes.addTempCache(key, cache);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))) {
						message.setTitle("FUNNY-NAMES "+STATIC.getTranslation(e.getMember(), Translation.FILTER_REMOVE));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_WORD)).build()).queue();
						cache.updateDescription("remove-funny-names").setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_PASTEBIN))) {
						message.setTitle("FUNNY-NAMES "+STATIC.getTranslation(e.getMember(), Translation.FILTER_ADD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTEBIN)).build()).queue();
						cache.updateDescription("add-load-funny-names").setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_LOAD_PASTEBIN))) {
						message.setTitle("FUNNY-NAMES "+STATIC.getTranslation(e.getMember(), Translation.FILTER_LOAD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTEBIN)).build()).queue();
						cache.updateDescription("load-funny-names").setExpiration(180000);
						Hashes.addTempCache(key, cache);
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
								logger.warn("Error on creating paste in guild {}!", e.getGuild().getId(), e2);
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
						Hashes.addTempCache(key, cache);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))) {
						message.setTitle("STAFF-NAMES "+STATIC.getTranslation(e.getMember(), Translation.FILTER_REMOVE));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_WORD)).build()).queue();
						cache.updateDescription("remove-staff-names").setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_PASTEBIN))) {
						message.setTitle("STAFF-NAMES "+STATIC.getTranslation(e.getMember(), Translation.FILTER_ADD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTEBIN)).build()).queue();
						cache.updateDescription("add-load-staff-names").setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_LOAD_PASTEBIN))) {
						message.setTitle("STAFF-NAMES "+STATIC.getTranslation(e.getMember(), Translation.FILTER_LOAD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTEBIN)).build()).queue();
						cache.updateDescription("load-staff-names").setExpiration(180000);
						Hashes.addTempCache(key, cache);
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
								logger.warn("Error on creating paste in guild {}!", e.getGuild().getId(), e2);
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
						Hashes.addTempCache(key, cache);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))) {
						message.setTitle("URL-BLACKLIST "+STATIC.getTranslation(e.getMember(), Translation.FILTER_REMOVE));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_FQDN)).build()).queue();
						cache.updateDescription("remove-url-blacklist").setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_PASTEBIN))) {
						message.setTitle("URL-BLACKLIST "+STATIC.getTranslation(e.getMember(), Translation.FILTER_ADD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE_FQDN)).build()).queue();
						cache.updateDescription("add-load-url-blacklist").setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_LOAD_PASTEBIN))) {
						message.setTitle("URL-BLACKLIST "+STATIC.getTranslation(e.getMember(), Translation.FILTER_LOAD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE_FQDN)).build()).queue();
						cache.updateDescription("load-url-blacklist").setExpiration(180000);
						Hashes.addTempCache(key, cache);
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
								logger.warn("Error on creating paste in guild {}!", e.getGuild().getId(), e2);
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
						Hashes.addTempCache(key, cache);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))) {
						message.setTitle("URL-WHITELIST "+STATIC.getTranslation(e.getMember(), Translation.FILTER_REMOVE));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_FQDN)).build()).queue();
						cache.updateDescription("remove-url-whitelist").setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_PASTEBIN))) {
						message.setTitle("URL-WHITELIST "+STATIC.getTranslation(e.getMember(), Translation.FILTER_ADD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE_FQDN)).build()).queue();
						cache.updateDescription("add-load-url-whitelist").setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_LOAD_PASTEBIN))) {
						message.setTitle("URL-WHITELIST "+STATIC.getTranslation(e.getMember(), Translation.FILTER_LOAD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE_FQDN)).build()).queue();
						cache.updateDescription("load-url-whitelist").setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
				}
				case "tweet-blacklist" -> {
					if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_DISPLAY))) {
						StringBuilder out = new StringBuilder();
						for(String word : Azrael.SQLgetTweetBlacklist(e.getGuild().getIdLong())) {
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
								logger.warn("Error on creating paste in guild {}!", e.getGuild().getId(), e2);
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
						Hashes.addTempCache(key, cache);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_REMOVE))) {
						message.setTitle("TWEET-BLACKLIST "+STATIC.getTranslation(e.getMember(), Translation.FILTER_REMOVE));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_USERNAME)).build()).queue();
						cache.updateDescription("remove-tweet-blacklist").setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ADD_PASTEBIN))) {
						message.setTitle("TWEET-BLACKLIST "+STATIC.getTranslation(e.getMember(), Translation.FILTER_ADD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE_USERNAME)).build()).queue();
						cache.updateDescription("add-load-tweet-blacklist").setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
					else if(_message.equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_LOAD_PASTEBIN))) {
						message.setTitle("TWEET-BLACKLIST "+STATIC.getTranslation(e.getMember(), Translation.FILTER_LOAD_PASTEBIN));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTE_USERNAME)).build()).queue();
						cache.updateDescription("load-tweet-blacklist").setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
				}
				case "display-word-filter" -> callFilterLangContent(e, message, key, _message.toLowerCase());
				case "insert-word-filter" -> {
					var langInsert = _message.toLowerCase();
					if(langInsert.equalsIgnoreCase("english") || langInsert.equalsIgnoreCase("german") || langInsert.equalsIgnoreCase("french") || langInsert.equalsIgnoreCase("turkish") || langInsert.equalsIgnoreCase("russian") || 
							langInsert.equalsIgnoreCase("spanish") || langInsert.equalsIgnoreCase("portuguese") || langInsert.equalsIgnoreCase("italian") || langInsert.equalsIgnoreCase("all")) {
						message.setTitle("WORD-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_INSERT)+" "+langInsert.toUpperCase());
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_WORD)).build()).queue();
						cache.updateDescription(langInsert+"-insert-word-filter").setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
				}
				case "english-insert-word-filter" 		-> insertLangWord(e, message, key, cache.getAdditionalInfo().split("-")[0], _message);
				case "german-insert-word-filter" 		-> insertLangWord(e, message, key, cache.getAdditionalInfo().split("-")[0], _message);
				case "french-insert-word-filter" 		-> insertLangWord(e, message, key, cache.getAdditionalInfo().split("-")[0], _message);
				case "turkish-insert-word-filter" 		-> insertLangWord(e, message, key, cache.getAdditionalInfo().split("-")[0], _message);
				case "russian-insert-word-filter" 		-> insertLangWord(e, message, key, cache.getAdditionalInfo().split("-")[0], _message);
				case "spanish-insert-word-filter" 		-> insertLangWord(e, message, key, cache.getAdditionalInfo().split("-")[0], _message);
				case "portuguese-insert-word-filter" 	-> insertLangWord(e, message, key, cache.getAdditionalInfo().split("-")[0], _message);
				case "italian-insert-word-filter" 		-> insertLangWord(e, message, key, cache.getAdditionalInfo().split("-")[0], _message);
				case "all-insert-word-filter" 			-> insertLangWord(e, message, key, cache.getAdditionalInfo().split("-")[0], _message);
				case "remove-word-filter" -> {
					var langRemove = _message.toLowerCase();
					if(langRemove.equalsIgnoreCase("english") || langRemove.equalsIgnoreCase("german") || langRemove.equalsIgnoreCase("french") || langRemove.equalsIgnoreCase("turkish") || langRemove.equalsIgnoreCase("russian") || 
							langRemove.equalsIgnoreCase("spanish") || langRemove.equalsIgnoreCase("portuguese") || langRemove.equalsIgnoreCase("italian") || langRemove.equalsIgnoreCase("all")) {
						message.setTitle("WORD-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_REMOVE)+" "+langRemove.toUpperCase());
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_WORD)).build()).queue();
						cache.updateDescription(langRemove+"-remove-word-filter").setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
				}
				case "english-remove-word-filter" 		-> removeLangWord(e, message, key, cache.getAdditionalInfo().split("-")[0], _message);
				case "german-remove-word-filter" 		-> removeLangWord(e, message, key, cache.getAdditionalInfo().split("-")[0], _message);
				case "french-remove-word-filter" 		-> removeLangWord(e, message, key, cache.getAdditionalInfo().split("-")[0], _message);
				case "turkish-remove-word-filter" 		-> removeLangWord(e, message, key, cache.getAdditionalInfo().split("-")[0], _message);
				case "russian-remove-word-filter" 		-> removeLangWord(e, message, key, cache.getAdditionalInfo().split("-")[0], _message);
				case "spanish-remove-word-filter" 		-> removeLangWord(e, message, key, cache.getAdditionalInfo().split("-")[0], _message);
				case "portuguese-remove-word-filter" 	-> removeLangWord(e, message, key, cache.getAdditionalInfo().split("-")[0], _message);
				case "italian-remove-word-filter" 		-> removeLangWord(e, message, key, cache.getAdditionalInfo().split("-")[0], _message);
				case "all-remove-word-filter" 			-> removeLangWord(e, message, key, cache.getAdditionalInfo().split("-")[0], _message);
				case "add-load-word-filter" -> {
					var addLangLoad = _message.toLowerCase();
					if(addLangLoad.equalsIgnoreCase("english") || addLangLoad.equalsIgnoreCase("german") || addLangLoad.equalsIgnoreCase("french") || addLangLoad.equalsIgnoreCase("turkish") || addLangLoad.equalsIgnoreCase("russian") || 
							addLangLoad.equalsIgnoreCase("spanish") || addLangLoad.equalsIgnoreCase("portuguese") || addLangLoad.equalsIgnoreCase("italian")) {
						message.setTitle("WORD-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_ADD_PASTEBIN)+" "+addLangLoad.toUpperCase());
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTEBIN)).build()).queue();
						cache.updateDescription(addLangLoad+"-add-load-word-filter").setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
				}
				case "english-add-load-word-filter" 	-> loadLangWords(e, message, key, cache.getAdditionalInfo().split("-")[0], _message, false);
				case "german-add-load-word-filter" 		-> loadLangWords(e, message, key, cache.getAdditionalInfo().split("-")[0], _message, false);
				case "french-add-load-word-filter" 		-> loadLangWords(e, message, key, cache.getAdditionalInfo().split("-")[0], _message, false);
				case "turkish-add-load-word-filter" 	-> loadLangWords(e, message, key, cache.getAdditionalInfo().split("-")[0], _message, false);
				case "russian-add-load-word-filter" 	-> loadLangWords(e, message, key, cache.getAdditionalInfo().split("-")[0], _message, false);
				case "spanish-add-load-word-filter" 	-> loadLangWords(e, message, key, cache.getAdditionalInfo().split("-")[0], _message, false);
				case "portuguese-add-load-word-filter" 	-> loadLangWords(e, message, key, cache.getAdditionalInfo().split("-")[0], _message, false);
				case "italian-add-load-word-filter" 	-> loadLangWords(e, message, key, cache.getAdditionalInfo().split("-")[0], _message, false);
				case "load-word-filter" -> {
					var langLoad = _message.toLowerCase();
					if(langLoad.equalsIgnoreCase("english") || langLoad.equalsIgnoreCase("german") || langLoad.equalsIgnoreCase("french") || langLoad.equalsIgnoreCase("turkish") || langLoad.equalsIgnoreCase("russian") || 
							langLoad.equalsIgnoreCase("spanish") || langLoad.equalsIgnoreCase("portuguese") || langLoad.equalsIgnoreCase("italian")) {
						message.setTitle("WORD-FILTER "+STATIC.getTranslation(e.getMember(), Translation.FILTER_LOAD_PASTEBIN)+" "+langLoad.toUpperCase());
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTEBIN)).build()).queue();
						cache.updateDescription(langLoad+"-load-word-filter").setExpiration(180000);
						Hashes.addTempCache(key, cache);
					}
				}
				case "english-load-word-filter" 		-> loadLangWords(e, message, key, cache.getAdditionalInfo().split("-")[0], _message, true);
				case "german-load-word-filter" 			-> loadLangWords(e, message, key, cache.getAdditionalInfo().split("-")[0], _message, true);
				case "french-load-word-filter" 			-> loadLangWords(e, message, key, cache.getAdditionalInfo().split("-")[0], _message, true);
				case "turkish-load-word-filter" 		-> loadLangWords(e, message, key, cache.getAdditionalInfo().split("-")[0], _message, true);
				case "russian-load-word-filter" 		-> loadLangWords(e, message, key, cache.getAdditionalInfo().split("-")[0], _message, true);
				case "spanish-load-word-filter" 		-> loadLangWords(e, message, key, cache.getAdditionalInfo().split("-")[0], _message, true);
				case "portuguese-load-word-filter" 		-> loadLangWords(e, message, key, cache.getAdditionalInfo().split("-")[0], _message, true);
				case "italian-load-word-filter" 		-> loadLangWords(e, message, key, cache.getAdditionalInfo().split("-")[0], _message, true);
				case "insert-name-filter" -> {
					if(Azrael.SQLInsertNameFilter(_message, false, e.getMember().getIdLong()) > 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_INSERT)).build()).queue();
						Hashes.removeNameFilter(e.getGuild().getIdLong());
						logger.debug("{} has inserted the word {} into the name filter", e.getMember().getUser().getIdLong(), _message);
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).queue();
						logger.error("Name {} couldn't be inserted for Azrael.name_filter table in guild {}", _message, e.getGuild().getName());
					}
					Hashes.clearTempCache(key);
				}
				case "remove-name-filter" -> {
					if(Azrael.SQLDeleteNameFilter(_message, false, e.getGuild().getIdLong()) > 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_REMOVE)).build()).queue();
						Hashes.removeNameFilter(e.getGuild().getIdLong());
						logger.debug("{} has removed the word {} from the name filter", e.getMember().getUser().getIdLong(), _message);
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Name {} couldn't be removed from Azrael.name_filter table in guild {}", _message, e.getGuild().getName());
					}
					Hashes.clearTempCache(key);
				}
				case "load-name-filter", "add-load-name-filter" -> {
					if(_message.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _message.startsWith("http")) {
						try {
							String [] words = Pastebin.readPublicPasteLink(_message).split("[\\r\\n]+");
							var querryResult = Azrael.SQLReplaceNameFilter(words, false, e.getGuild().getIdLong(), (cache.getAdditionalInfo().split("-")[0].equals("add") ? false : true));
							if(querryResult == 0) {
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_ADD_PASTEBIN)).build()).queue();
								Hashes.removeNameFilter(e.getGuild().getIdLong());
								logger.debug("{} has inserted words out of pastebin into the name filter in guild {}", e.getMember().getUser().getIdLong(), e.getGuild().getId());
							}
							else if(querryResult == 1) {
								//throw error for failing the db replacement
								message.setColor(Color.RED);
								var duplicates = checkDuplicates(words);
								if(duplicates == null || duplicates.size() == 0) {
									e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
									logger.warn("The name-filter couldn't be updated in guild {}", e.getGuild().getId());
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
								logger.error("Update on name-filter table couldn't be rolled back on error. Affected guild {}", e.getGuild().getId());
							}
						} catch (MalformedURLException | RuntimeException e2) {
							message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_NOT_PASTE));
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
							logger.error("Reading paste failed in guild {}!", e2);
						}
						Hashes.clearTempCache(key);
					}
				}
				case "insert-name-kick" -> {
					if(Azrael.SQLInsertNameFilter(_message, true, e.getGuild().getIdLong()) > 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_INSERT)).build()).queue();
						Hashes.removeNameFilter(e.getGuild().getIdLong());
						logger.debug("{} has inserted the word {} into name-kick in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Name {} couldn't be inserted into Azrael.name_filter for guild {}", _message, e.getGuild().getName());
					}
					Hashes.clearTempCache(key);
				}
				case "remove-name-kick" -> {
					if(Azrael.SQLDeleteNameFilter(_message, true, e.getGuild().getIdLong()) > 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_REMOVE)).build()).queue();
						Hashes.removeNameFilter(e.getGuild().getIdLong());
						logger.debug("{} has removed the word {} from name-kick in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Name {} couldn't be removed from Azrael.name_filter for guild {}", _message, e.getGuild().getName());
					}
					Hashes.clearTempCache(key);
				}
				case "load-name-kick", "add-load-name-kick" -> {
					if(_message.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _message.startsWith("http")) {
						try {
							String [] words = Pastebin.readPublicPasteLink(_message).split("[\\r\\n]+");
							var querryResult = Azrael.SQLReplaceNameFilter(words, true, e.getGuild().getIdLong(), (cache.getAdditionalInfo().split("-")[0].equals("add") ? false : true));
							if(querryResult == 0) {
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_ADD_PASTEBIN)).build()).queue();
								Hashes.removeNameFilter(e.getGuild().getIdLong());
								logger.debug("{} has inserted words out of pastebin into name-kick in guild {}", e.getMember().getUser().getIdLong(), e.getGuild().getId());
							}
							else if(querryResult == 1) {
								//throw error for failing the db replacement
								message.setColor(Color.RED);
								var duplicates = checkDuplicates(words);
								if(duplicates == null || duplicates.size() == 0) {
									message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
									e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
									logger.warn("The name-kick couldn't be updated in guild {}", e.getGuild().getId());
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
								logger.error("Update on name-filter table couldn't be rolled back on error. Affected guild {}", e.getGuild().getId());
							}
						} catch (MalformedURLException | RuntimeException e2) {
							message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_NOT_PASTE));
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
							logger.error("Reading paste failed in guild {}!", e2);
						}
						Hashes.clearTempCache(key);
					}
				}
				case "insert-funny-names" -> {
					if(Azrael.SQLInsertFunnyNames(_message, e.getGuild().getIdLong()) > 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_INSERT)).build()).queue();
						Hashes.removeQuerryResult("funny-names_"+e.getGuild().getId());
						logger.debug("{} has inserted the word {} into the funny names in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Name {} couldn't be inserted into Azrael.names for guild {}", _message, e.getGuild().getName());
					}
					Hashes.clearTempCache(key);
				}
				case "remove-funny-names" -> {
					if(Azrael.SQLDeleteFunnyNames(_message, e.getGuild().getIdLong()) > 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_REMOVE)).build()).queue();
						Hashes.removeQuerryResult("funny-names_"+e.getGuild().getId());
						logger.debug("{} has removed the word {} from the funny names in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Name {} couldn't be removed from Azrael.names for guild {}", _message, e.getGuild().getName());
					}
					Hashes.clearTempCache(key);
				}
				case "load-funny-names", "add-load-funny-names" -> {
					if(_message.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _message.startsWith("http")) {
						try {
							String [] words = Pastebin.readPublicPasteLink(_message).split("[\\r\\n]+");
							var querryResult = Azrael.SQLReplaceFunnyNames(words, e.getGuild().getIdLong(), (cache.getAdditionalInfo().split("-")[0].equals("add") ? false : true));
							if(querryResult == 0) {
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_ADD_PASTEBIN)).build()).queue();
								Hashes.removeQuerryResult("funny-names_"+e.getGuild().getId());
								logger.debug("{} has inserted words out of pastebin into the funny-names in guild {}", e.getMember().getUser().getIdLong(), e.getGuild().getId());
							}
							else if(querryResult == 1) {
								//throw error for failing the db replacement
								message.setColor(Color.RED);
								var duplicates = checkDuplicates(words);
								if(duplicates == null || duplicates.size() == 0) {
									e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
									logger.warn("The name-filter couldn't be updated in guild {}", e.getGuild().getId());
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
								logger.error("Update on funny-names table couldn't be rolled back on error. Affected guild {}", e.getGuild().getId());
							}
						} catch (MalformedURLException | RuntimeException e2) {
							message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_NOT_PASTE));
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
							logger.error("Reading paste failed in guild {}!", e2);
						}
						Hashes.clearTempCache(key);
					}
				}
				case "insert-staff-names" -> {
					if(Azrael.SQLInsertStaffName(_message, e.getGuild().getIdLong()) > 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_INSERT)).build()).queue();
						Hashes.removeQuerryResult("staff-names_"+e.getGuild().getId());
						logger.debug("{} has inserted the word {} into the staff names in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Name {} couldn't be inserted into Azrael.staff_name_filter for guild {}", _message, e.getGuild().getName());
					}
					Hashes.clearTempCache(key);
				}
				case "remove-staff-names" -> {
					if(Azrael.SQLDeleteStaffNames(_message, e.getGuild().getIdLong()) > 0) {
						message.setTitle("Success!");
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_REMOVE)).build()).queue();
						Hashes.removeQuerryResult("staff-names_"+e.getGuild().getId());
						logger.debug("{} has removed the word {} from the staff names in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Name {} couldn't be removed from Azrael.staff_names for guild {}", _message, e.getGuild().getName());
					}
					Hashes.clearTempCache(key);
				}
				case "load-staff-names", "add-load-staff-names" -> {
					if(_message.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _message.startsWith("http")) {
						try {
							String [] words = Pastebin.readPublicPasteLink(_message).split("[\\r\\n]+");
							var querryResult = Azrael.SQLReplaceStaffNames(words, e.getGuild().getIdLong(), (cache.getAdditionalInfo().split("-")[0].equals("add") ? false : true));
							if(querryResult == 0) {
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_ADD_PASTEBIN)).build()).queue();
								Hashes.removeQuerryResult("staff-names_"+e.getGuild().getId());
								logger.debug("{} has inserted words out of pastebin into the staff-names in guild {}", e.getMember().getUser().getIdLong(), e.getGuild().getId());
							}
							else if(querryResult == 1) {
								//throw error for failing the db replacement
								message.setColor(Color.RED);
								var duplicates = checkDuplicates(words);
								if(duplicates == null || duplicates.size() == 0) {
									e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
									
									logger.warn("The staff-names couldn't be updated in guild {}", e.getGuild().getId());
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
								logger.error("Update on staff-names table couldn't be rolled back on error. Affected guild {}", e.getGuild().getId());
							}
						} catch (MalformedURLException | RuntimeException e2) {
							message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_NOT_PASTE));
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
							logger.error("Reading paste failed in guild {}!", e.getGuild().getId(), e2);
						}
						Hashes.clearTempCache(key);
					}
				}
				case "insert-url-blacklist" -> {
					if((_message.startsWith("http://") || _message.startsWith("https://")) && !_message.matches("[\\s]")) {
						if(Azrael.SQLInsertURLBlacklist(_message, e.getGuild().getIdLong()) > 0) {
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_INSERT_URL)).build()).queue();
							Hashes.removeURLBlacklist(e.getGuild().getIdLong());
							logger.debug("{} has inserted the url {} into the url-blacklist in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
						}
						else {
							message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("URL {} couldn't be inserted into Azrael.url_blacklist for guild {}", _message, e.getGuild().getName());
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
					if(Azrael.SQLDeleteURLBlacklist(_message, e.getGuild().getIdLong()) > 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_REMOVE_URL)).build()).queue();
						Hashes.removeURLBlacklist(e.getGuild().getIdLong());
						logger.debug("{} has removed the url {} from the url blacklist in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("URL {} couldn't be removed from Azrael.url_blacklist for guild {}", _message, e.getGuild().getName());
					}
					Hashes.clearTempCache(key);
				}
				case "add-load-url-blacklist", "load-url-blacklist" -> {
					if(_message.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _message.startsWith("http")) {
						try {
							String [] url = Pastebin.readPublicPasteLink(_message).split("[\\r\\n]+");
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
								var querryResult = Azrael.SQLReplaceURLBlacklist(url, e.getGuild().getIdLong(), (cache.getAdditionalInfo().split("-")[0].equals("add") ? false : true));
								if(querryResult == 0) {
									e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTEBIN_URL)).build()).queue();
									Hashes.removeURLBlacklist(e.getGuild().getIdLong());
									logger.debug("{} has inserted urls out of pastebin into url-blacklist in guild {}", e.getMember().getUser().getIdLong());
								}
								else if(querryResult == 1) {
									//throw error for failing the db replacement
									message.setColor(Color.RED);
									var duplicates = checkDuplicates(url);
									if(duplicates == null || duplicates.size() == 0) {
										e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
										logger.warn("url-blacklist couldn't be updated in guild {}", e.getGuild().getId());
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
									logger.error("Update on url-blacklist table couldn't be rolled back on error. Affected guild {}", e.getGuild().getId());
								}
							}
							else {
								message.setColor(Color.RED);
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_NO_URL)).build()).queue();
							}
						} catch (MalformedURLException | RuntimeException e2) {
							message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_NOT_PASTE));
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
							logger.error("Reading paste failed in guild {}!", e.getGuild().getId(), e2);
						}
						Hashes.clearTempCache(key);
					}
				}
				case "insert-url-whitelist" -> {
					if((_message.startsWith("http://") || _message.startsWith("https://")) && !_message.matches("[\\s]")) {
						if(Azrael.SQLInsertURLWhitelist(_message, e.getGuild().getIdLong()) > 0) {
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_INSERT_URL)).build()).queue();
							Hashes.removeURLBlacklist(e.getGuild().getIdLong());
							logger.debug("{} has inserted the url {} into the url-whitelist in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
						}
						else {
							message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.error("URL {} couldn't be inserted into Azrael.url_whitelist for guild {}", _message, e.getGuild().getName());
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
					if(Azrael.SQLDeleteURLWhitelist(_message, e.getGuild().getIdLong()) > 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_REMOVE_URL)).build()).queue();
						Hashes.removeURLBlacklist(e.getGuild().getIdLong());
						logger.debug("{} has removed the url {} from the url whitelist in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("URL {} couldn't be removed from Azrael.url_whitelist for guild {}", _message, e.getGuild().getName());
					}
					Hashes.clearTempCache(key);
				}
				case "add-load-url-whitelist", "load-url-whitelist" -> {
					if(_message.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _message.startsWith("http")) {
						try {
							String [] url = Pastebin.readPublicPasteLink(_message).split("[\\r\\n]+");
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
								var querryResult = Azrael.SQLReplaceURLWhitelist(url, e.getGuild().getIdLong(), (cache.getAdditionalInfo().split("-")[0].equals("add") ? false : true));
								if(querryResult == 0) {
									e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTEBIN_URL)).build()).queue();
									Hashes.removeURLWhitelist(e.getGuild().getIdLong());
									logger.debug("{} has inserted urls out of pastebin into url-whitelist in guild {}", e.getMember().getUser().getIdLong(), e.getGuild().getId());
								}
								else if(querryResult == 1) {
									//throw error for failing the db replacement
									message.setColor(Color.RED);
									var duplicates = checkDuplicates(url);
									if(duplicates == null || duplicates.size() == 0) {
										e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
										logger.warn("url-whitelist couldn't be updated in guild {}", e.getGuild().getId());
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
									logger.error("Update on url-whitelist table couldn't be rolled back on error. Affected guild {}", e.getGuild().getId());
								}
							}
							else {
								message.setColor(Color.RED);
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_NO_URL)).build()).queue();
							}
						} catch (MalformedURLException | RuntimeException e2) {
							message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_NOT_PASTE));
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
							logger.error("Reading paste failed in guild {}!", e.getGuild().getId(), e2);
						}
						Hashes.clearTempCache(key);
					}
				}
				case "insert-tweet-blacklist" -> {
					if(Azrael.SQLInsertTweetBlacklist(_message, e.getGuild().getIdLong()) > 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_INSERT_NICK)).build()).queue();
						Hashes.removeTweetBlacklist(e.getGuild().getIdLong());
						logger.debug("{} has inserted the username {} into the tweet-blacklist in guild {}", e.getMember().getUser().getIdLong(), _message, e.getGuild().getId());
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Username {} couldn't be inserted into Azrael.tweet_blacklist for guild {}", _message, e.getGuild().getName());
					}
					Hashes.clearTempCache(key);
				}
				case "remove-tweet-blacklist" -> {
					if(Azrael.SQLDeleteTweetBlacklist(_message, e.getGuild().getIdLong()) > 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_REMOVE_NICK)).build()).queue();
						Hashes.removeTweetBlacklist(e.getGuild().getIdLong());
						logger.debug("{} has removed the username {} from the tweet-blacklist", e.getMember().getUser().getIdLong(), _message);
					}
					else {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
						logger.error("Username {} couldn't be removed from Azrael.tweet_blacklist for guild {}", _message, e.getGuild().getName());
					}
					Hashes.clearTempCache(key);
				}
				case "add-load-tweet-blacklist", "load-tweet-blacklist" -> {
					try {
						String [] usernames = Pastebin.readPublicPasteLink(_message).split("[\\r\\n]+");
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
							var querryResult = Azrael.SQLReplaceTweetBlacklist(usernames, e.getGuild().getIdLong(), (cache.getAdditionalInfo().split("-")[0].equals("add") ? false : true));
							if(querryResult == 0) {
								e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_PASTEBIN_NICK)).build()).queue();
								Hashes.removeURLWhitelist(e.getGuild().getIdLong());
								logger.debug("{} has inserted urls out of pastebin into tweet-blacklist in guild {}", e.getMember().getUser().getIdLong(), e.getGuild().getId());
							}
							else if(querryResult == 1) {
								//throw error for failing the db replacement
								message.setColor(Color.RED);
								var duplicates = checkDuplicates(usernames);
								if(duplicates == null || duplicates.size() == 0) {
									e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
									logger.warn("tweet-blacklist couldn't be updated in guild {}", e.getGuild().getId());
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
								logger.error("Update on tweet-blacklist table couldn't be rolled back on error. Affected guild {}", e.getGuild().getId());
							}
						}
						else {
							message.setColor(Color.RED);
							e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_NO_NICK)).build()).queue();
						}
					} catch (MalformedURLException | RuntimeException e2) {
						message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PASTE_READ_ERR));
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
						logger.error("Reading paste failed in guild {}!", e.getGuild().getId(), e2);
					}
					Hashes.clearTempCache(key);
				}
			}
		}
		else {
			Hashes.clearTempCache(key);
		}
	}
	
	@SuppressWarnings("preview")
	private static void callFilterLangContent(GuildMessageReceivedEvent e, EmbedBuilder message, final String key, final String lang) {
		var langAbbreviation = "";
		var definitiveLang = "";
		switch(lang) {
			case "english" 		-> {langAbbreviation = "eng";  definitiveLang = STATIC.getTranslation(e.getMember(), Translation.LANG_ENG);}
			case "german" 		-> {langAbbreviation = "ger";  definitiveLang = STATIC.getTranslation(e.getMember(), Translation.LANG_GER);}
			case "french" 		-> {langAbbreviation = "fre";  definitiveLang = STATIC.getTranslation(e.getMember(), Translation.LANG_FRE);}
			case "turkish" 		-> {langAbbreviation = "tur";  definitiveLang = STATIC.getTranslation(e.getMember(), Translation.LANG_TUR);}
			case "russian" 		-> {langAbbreviation = "rus";  definitiveLang = STATIC.getTranslation(e.getMember(), Translation.LANG_RUS);}
			case "spanish" 		-> {langAbbreviation = "spa";  definitiveLang = STATIC.getTranslation(e.getMember(), Translation.LANG_SPA);}
			case "portuguese" 	-> {langAbbreviation = "por";  definitiveLang = STATIC.getTranslation(e.getMember(), Translation.LANG_POR);}
			case "italian" 		-> {langAbbreviation = "ita";  definitiveLang = STATIC.getTranslation(e.getMember(), Translation.LANG_ITA);}
		}
		
		StringBuilder out = new StringBuilder();
		for(String word : Azrael.SQLgetFilter(langAbbreviation, e.getGuild().getIdLong())) {
			out.append(word+"\n");
		}
		if(out.length() > 0) {
			try {
				String paste_link = Pastebin.unlistedPaste(definitiveLang, out.toString(), e.getGuild().getIdLong());
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LIST)+paste_link).build()).queue();
				logger.debug("{} has called the {} word filter in guild {}", e.getMember().getUser().getIdLong(), lang, e.getGuild().getId(), e.getGuild().getId());
			} catch (IllegalStateException | LoginException | PasteException e2) {
				message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_PASTE));
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_PASTE_ERR)).build()).queue();
				logger.error("Error on creating paste failed in guild {}!", e.getGuild().getId(), e2);
			}
		}
		else {
			message.setColor(Color.RED);
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_LIST_EMPTY)).build()).queue();
		}
		Hashes.clearTempCache(key);
	}
	
	private static void insertLangWord(GuildMessageReceivedEvent e, EmbedBuilder message, final String key, final String lang, String word) {
		if(!word.matches("[\\w\\d\\s\\@-]*[^\\w\\d\\s\\@\\-]{1,}[\\w\\d\\s\\@-]*")) {
			if(Azrael.SQLInsertWordFilter(lang.substring(0, 3), word, e.getGuild().getIdLong()) >= 0) {
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_INSERT)).build()).queue();
				clearHash(e, lang, true);
				Hashes.removeQuerryResult("all_"+e.getGuild().getId());
				logger.debug("{} has inserted the word {} into the {} word filter in guild {}", e.getMember().getUser().getIdLong(), word, lang, e.getGuild().getId());
			}
			else {
				message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Word couldn't be inserted into Azrael.filter for guild {}", e.getGuild().getName());
			}
		}
		else {
			message.setColor(Color.RED);
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_INVALID_CHAR)).build()).queue();
		}
		Hashes.clearTempCache(key);
	}
	
	private static void removeLangWord(GuildMessageReceivedEvent e, EmbedBuilder message, final String key, final String lang, String word) {
		if(Azrael.SQLDeleteWordFilter(lang.substring(0, 3), word, e.getGuild().getIdLong()) > 0) {
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_WRITE_REMOVE)).build()).queue();
			clearHash(e, lang, true);
			Hashes.removeQuerryResult("all_"+e.getGuild().getId());
			logger.debug("{} has removed the word {} from the english word filter in guild {}", e.getMember().getUser().getIdLong(), word, e.getGuild().getId());
		}
		else {
			message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR));
			e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Word {} couldn't be removed from Azrael.filter in guild {}", word, e.getGuild().getId());
		}
		Hashes.clearTempCache(key);
	}
	
	private static void loadLangWords(GuildMessageReceivedEvent e, EmbedBuilder message, final String key, final String lang, String _message, boolean replace) {
		if(_message.matches("(https|http)[:\\\\/a-zA-Z0-9-Z.?!=#%&_+-;]*") && _message.startsWith("http")) {
			var langAbbreviation = lang.substring(0, 3);
			String[] words;
			try {
				words = Pastebin.readPublicPasteLink(_message).split("[\\r\\n]+");
				var interrupt = false;
				for(final var word : words) {
					if(word.toLowerCase().matches("[\\w\\d\\s\\@-]*[^\\w\\d\\s\\@\\-]{1,}[\\w\\d\\s\\@-]*")) {
						interrupt = true;
					}
					break;
				}
				if(!interrupt) {
					var querryResult = Azrael.SQLReplaceWordFilter(langAbbreviation, words, e.getGuild().getIdLong(), replace);
					if(querryResult == 0) {
						e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_ADD_PASTEBIN)).build()).queue();
						clearHash(e, lang, false);
						Hashes.removeQuerryResult("all_"+e.getGuild().getId());
						logger.debug("{} has inserted words from a file into the {} word filter in guild {}", e.getMember().getUser().getIdLong(), lang, e.getGuild().getId());
					}
					else if(querryResult == 1) {
						//throw error for failing the db replacement
						message.setColor(Color.RED);
						var duplicates = checkDuplicates(words);
						if(duplicates == null || duplicates.size() == 0) {
							e.getChannel().sendMessage(message.setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
							logger.warn("The {} filter couldn't be updated in guild {}", lang, e.getGuild().getId());
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
						logger.error("Update on filter table couldn't be rolled back on error. Affected language is {} for guild {}", lang, e.getGuild().getId());
					}
				}
				else {
					message.setColor(Color.RED);
					e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.FILTER_INVALID_CHAR)).build()).queue();
				}
			} catch (MalformedURLException | RuntimeException e2) {
				message.setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_NOT_PASTE));
				e.getChannel().sendMessage(message.setDescription(STATIC.getTranslation(e.getMember(), Translation.PASTEBIN_READ_ERR)).build()).queue();
				logger.error("Reading paste failed in guild {}!", e.getGuild().getId(), e2);
			}
			Hashes.clearTempCache(key);
		}
	}
	
	private static void clearHash(GuildMessageReceivedEvent e, final String lang, final boolean allowAll) {
		if(lang.equals("english") || (allowAll && lang.equals("all")))
			Hashes.removeQuerryResult("eng_"+e.getGuild().getId());
		if(lang.equals("german") || (allowAll && lang.equals("all")))
			Hashes.removeQuerryResult("ger_"+e.getGuild().getId());
		if(lang.equals("french") || (allowAll && lang.equals("all")))
			Hashes.removeQuerryResult("fre_"+e.getGuild().getId());
		if(lang.equals("turkish") || (allowAll && lang.equals("all")))
			Hashes.removeQuerryResult("tur_"+e.getGuild().getId());
		if(lang.equals("russian") || (allowAll && lang.equals("all")))
			Hashes.removeQuerryResult("rus_"+e.getGuild().getId());
		if(lang.equals("spanish") || (allowAll && lang.equals("all")))
			Hashes.removeQuerryResult("spa_"+e.getGuild().getId());
		if(lang.equals("portuguese") || (allowAll && lang.equals("all")))
			Hashes.removeQuerryResult("por_"+e.getGuild().getId());
		if(lang.equals("italian") || (allowAll && lang.equals("all")))
			Hashes.removeQuerryResult("ita_"+e.getGuild().getId());
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
}