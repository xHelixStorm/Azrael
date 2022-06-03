package de.azrael.commandsContainer;

import java.awt.Color;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Thumbnails;
import de.azrael.core.Hashes;
import de.azrael.core.UserPrivs;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.sql.Azrael;
import de.azrael.sql.BotConfiguration;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class RegisterCategory {
	private final static Logger logger = LoggerFactory.getLogger(RegisterCategory.class);
	
	public static void runHelp(GuildMessageReceivedEvent e, Thumbnails thumbnails) {
		EmbedBuilder messageBuild = new EmbedBuilder().setColor(Color.BLUE).setThumbnail(thumbnails.getSettings());
		StringBuilder strB = new StringBuilder();
		StringBuilder strB2 = new StringBuilder();
		
		final var categoryTypes = Azrael.SQLgetCategoryTypes();
		if(categoryTypes != null) {
			for(final var category : categoryTypes) {
				strB.append("**"+category.getType()+"**\n");
				strB2.append(category.getTypeName()+"\n");
			}
			if(strB.length() > 0) {
				e.getChannel().sendMessage(messageBuild.setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_CATEGORY_HELP)).addField("", strB.toString(), true).addField("", strB2.toString(), true).build()).queue();
			}
			else {
				e.getChannel().sendMessage(messageBuild.setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_CATEGORY_NO_TYPES)).build()).queue();
			}
		}
		else {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
			logger.error("Category types couldn't be retrieved in guild {}", e.getGuild().getId());
		}
	}
	
	public static boolean runCommand(GuildMessageReceivedEvent e, String [] args, Thumbnails thumbnails) {
		final var commandLevel = STATIC.getCommandLevel(e.getGuild(), Command.REGISTER_CATEGORY);
		if(UserPrivs.comparePrivilege(e.getMember(), commandLevel)) {
			StringBuilder out = new StringBuilder();
			final var categoryTypes = Azrael.SQLgetCategoryTypes();
			if(categoryTypes != null && categoryTypes.size() > 0) {
				HashMap<String, Integer> categories = new HashMap<String, Integer>();
				for(final var category: categoryTypes) {
					if(out.length() > 0)
						out.append("|");
					out.append(category.getType());
					categories.put(category.getType(), category.getRegisterType());
				}
				Pattern pattern = Pattern.compile("("+out.toString()+")");
				Matcher matcher = pattern.matcher(args[1]);
				if(args.length == 3 && matcher.find()) {
					final String categoryType = matcher.group();
					final String category = args[2].replaceAll("[^0-9]*", "");
					if(category.length() > 0) {
						final long category_id = Long.parseLong(category);
						Category guildCategory = e.getGuild().getCategoryById(category_id);
						if(guildCategory != null) {
							int result = 0;
							switch(categories.get(categoryType)) {
								case 1 -> {
									result = Azrael.SQLInsertCategoryConf(guildCategory.getIdLong(), categoryType, e.getGuild().getIdLong());
								}
							}
							Hashes.removeCategories(e.getGuild().getIdLong());
							if(result > 0) {
								logger.info("Category {} as {} category registered in guild {}", guildCategory.getId(), categoryType, e.getGuild().getId());
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.REGISTER_CATEGORY_REGISTERED)).build()).queue();
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
								logger.error("Category {} couldn't be registered as {} category in guild {}", guildCategory.getId(), categoryType, e.getGuild().getId());
							}
						}
						else {
							e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.CATEGORY_NOT_EXISTS)).build()).queue();
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.NO_CATEGORY)).build()).queue();
					}
				}
				else {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
				}
				return true;
			}
			else {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GENERAL_ERROR)).build()).queue();
				logger.error("Category types couldn't be retrieved in guild {}", e.getGuild().getId());
			}
		}
		else {
			EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setThumbnail(BotConfiguration.SQLgetThumbnails(e.getGuild().getIdLong()).getDenied()).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DENIED));
			e.getChannel().sendMessage(denied.setDescription(e.getMember().getAsMention() + STATIC.getTranslation(e.getMember(), Translation.HIGHER_PRIVILEGES_ROLE) + UserPrivs.retrieveRequiredRoles(commandLevel, e.getMember())).build()).queue();
		}
		return false;
	}
}
