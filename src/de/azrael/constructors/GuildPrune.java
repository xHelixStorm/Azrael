package de.azrael.constructors;

import java.util.List;

/**
 * Class used together with the prune command to save the found members to cache
 * @author xHelixStorm
 *
 */

public class GuildPrune {
	List<net.dv8tion.jda.api.entities.Member> kickMembers;
	List<net.dv8tion.jda.api.entities.Member> exludedMembers;
	
	/**
	 * Default constructor
	 * @param _kickMembers
	 * @param _excludedMembers
	 */
	
	public GuildPrune(List<net.dv8tion.jda.api.entities.Member> _kickMembers, List<net.dv8tion.jda.api.entities.Member> _excludedMembers) {
		this.kickMembers = _kickMembers;
		this.exludedMembers = _excludedMembers;
	}
	
	/**
	 * Retrieve members to kick
	 * @return
	 */
	
	public List<net.dv8tion.jda.api.entities.Member> getKickMembers() {
		return this.kickMembers;
	}
	
	/**
	 * Retrieve members to exclude from the kick
	 * @return
	 */
	
	public List<net.dv8tion.jda.api.entities.Member> getExcludedMembes() {
		return this.exludedMembers;
	}
}
