package de.azrael.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.BotConfigs;
import de.azrael.constructors.Cache;
import de.azrael.constructors.GuildPrune;
import de.azrael.core.Hashes;
import de.azrael.enums.Command;
import de.azrael.enums.Translation;
import de.azrael.interfaces.CommandPublic;
import de.azrael.sql.Azrael;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Prune implements CommandPublic {
	private final static Logger logger = LoggerFactory.getLogger(Prune.class);

	@Override
	public boolean called(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		return STATIC.commandValidation(e, botConfig, Command.PRUNE);
	}

	@Override
	public boolean action(String[] args, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(args.length == 0) {
			//command explanation
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.BLUE).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_DETAILS)).setDescription(STATIC.getTranslation(e.getMember(), Translation.PRUNE_HELP)).build()).queue();
		}
		else {
			long tMembers = 0;
			long tExcludedMembers = 0;
			boolean excludeMode = false;
			final String exclude = STATIC.getTranslation(e.getMember(), Translation.PARAM_EXCLUDE);
			HashSet<Role> selectedRoles = new HashSet<Role>();
			HashSet<Role> excludedRoles = new HashSet<Role>();
			HashSet<Member> selectedMembers = new HashSet<Member>();
			HashSet<Member> excludedMembers = new HashSet<Member>();
			List<Member> kickMembers = new ArrayList<Member>();
			List<Member> excludeMembers = new ArrayList<Member>();
			if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ALL))) {
				//retrieve all users on the server
				kickMembers = e.getGuild().loadMembers().get();
				tMembers = kickMembers.size();
				//retrieve excluded users
				if(args.length > 2) {
					if(args[1].equalsIgnoreCase(exclude)) {
						for(int i = 2; i < args.length; i++) {
							final String arg = args[i];
							final String id = arg.replaceAll("[^0-9]*", "");
							if(id.length() > 0) {
								final var role = e.getGuild().getRoleById(id);
								if(role != null) {
									if(!excludedRoles.contains(role))
										excludedRoles.add(role);
										
									for(final Member member : e.getGuild().getMembersWithRoles(role)) {
										excludeMembers.add(member);
										tExcludedMembers ++;
									}
								}
								else {
									final Member member = e.getGuild().getMemberById(id);
									if(member != null) {
										if(!excludedMembers.contains(member))
											excludedMembers.add(member);
										excludeMembers.add(member);
										tExcludedMembers ++;
									}
									else {
										e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND_2).replace("{}", arg)).build()).queue();
										return true;
									}
								}
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND_2).replace("{}", arg)).build()).queue();
								return true;
							}
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND_2).replace("{}", args[1])).build()).queue();
					}
				}
			}
			else {
				for(int i = 0; i < args.length; i++) {
					final String arg = args[i];
					//enable exception mode but only if it's not written as first parameter
					if(i > 0 && arg.equalsIgnoreCase(exclude)) {
						excludeMode = true;
						continue;
					}
					else if(arg.equalsIgnoreCase(exclude)) {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
						return true;
					}
					
					final String id = arg.replaceAll("[^0-9]*", "");
					if(id.length() > 0) {
						final var role = e.getGuild().getRoleById(id);
						if(role != null) {
							if(!excludeMode) {
								if(!selectedRoles.contains(role))
									selectedRoles.add(role);
							}
							else {
								if(!excludedRoles.contains(role))
									excludedRoles.add(role);
							}
							
							for(final Member member : e.getGuild().getMembersWithRoles(role)) {
								if(!excludeMode) {
									kickMembers.add(member);
									tMembers ++;
								}
								else {
									excludeMembers.add(member);
									tExcludedMembers ++;
								}
							}
						}
						else {
							Member member = e.getGuild().getMemberById(id);
							if(member != null) {
								if(!excludeMode) {
									kickMembers.add(member);
									tMembers ++;
									if(!selectedMembers.contains(member))
										selectedMembers.add(member);
								}
								else {
									excludeMembers.add(member);
									tExcludedMembers ++;
									if(!excludedMembers.contains(member))
										excludedMembers.add(member);
								}
							}
							else {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND_2).replace("{}", arg)).build()).queue();
								return true;
							}
						}
					}
					else {
						e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND_2).replace("{}", arg)).build()).queue();
						return true;
					}
				}
			}
			
			if(excludeMode && excludeMembers.size() == 0) {
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(STATIC.getTranslation(e.getMember(), Translation.PARAM_NOT_FOUND)).build()).queue();
			}
			else {
				//notify user of how many members have been found and excluded. Write to cache to allow a final confirmation
				final EmbedBuilder embed = new EmbedBuilder().setColor(Color.BLUE).setDescription(STATIC.getTranslation(e.getMember(), Translation.PRUNE_CONFIRMATION).replaceFirst("\\{\\}", ""+tMembers).replace("{}", ""+tExcludedMembers));
				
				//Add fields to show the current selection and exclusions
				if(args[0].equalsIgnoreCase(STATIC.getTranslation(e.getMember(), Translation.PARAM_ALL)) || !selectedMembers.isEmpty()) {
					StringBuilder out = new StringBuilder();
					if(selectedMembers.isEmpty()) {
						out.append(STATIC.getTranslation(e.getMember(), Translation.PARAM_ALL).toUpperCase());
					}
					else {
						selectedMembers.stream().forEach(member -> {
							if((out.length()+member.getAsMention().length()+4) <= 1024)
								out.append(member.getAsMention()+" ");
							else
								out.append("...");
						});
					}
					embed.addField(STATIC.getTranslation(e.getMember(), Translation.PRUNE_SEL_MEMBERS), out.toString(), false);
				}
				if(!selectedRoles.isEmpty()) {
					StringBuilder out = new StringBuilder();
					selectedRoles.stream().forEach(role -> {
						if((out.length()+role.getAsMention().length()+4) <= 1024)
							out.append(role.getAsMention()+" ");
						else
							out.append("...");
					});
					embed.addField(STATIC.getTranslation(e.getMember(), Translation.PRUNE_SEL_ROLES), out.toString(), false);
				}
				if(!excludedMembers.isEmpty()) {
					StringBuilder out = new StringBuilder();
					excludedMembers.stream().forEach(member -> {
						if((out.length()+member.getAsMention().length()+4) <= 1024)
							out.append(member.getAsMention()+" ");
						else
							out.append("...");
					});
					embed.addField(STATIC.getTranslation(e.getMember(), Translation.PRUNE_EXC_MEMBERS), out.toString(), false);
				}
				if(!excludedRoles.isEmpty()) {
					StringBuilder out = new StringBuilder();
					excludedRoles.stream().forEach(role -> {
						if((out.length()+role.getAsMention().length()+4) <= 1024)
							out.append(role.getAsMention()+" ");
						else
							out.append("...");
					});
					embed.addField(STATIC.getTranslation(e.getMember(), Translation.PRUNE_EXC_ROLES), out.toString(), false);
				}
				
				e.getChannel().sendMessage(embed.build()).queue();
				Hashes.addTempCache("prune_gu"+e.getGuild().getId()+"ch"+e.getChannel().getId()+"us"+e.getMember().getUser().getId(), new Cache(180000).setObject(new GuildPrune(kickMembers, excludeMembers)));
			}
		}
		return true;
	}

	@Override
	public void executed(String[] args, boolean success, GuildMessageReceivedEvent e, BotConfigs botConfig) {
		if(success) {
			logger.trace("{} has used Prune command in guild {}", e.getMember().getUser().getId(), e.getGuild().getId());
			StringBuilder out = new StringBuilder();
			for(String arg : args)
				out.append(arg+" ");
			Azrael.SQLInsertCommandLog(e.getMember().getUser().getIdLong(), e.getGuild().getIdLong(), Command.PRUNE.getColumn(), out.toString().trim());
		}
	}

}
