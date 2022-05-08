package de.azrael.constructors;

/**
 * Keep track of already kicked users and of how many have to be kicked in total
 * @author xHelixStorm
 *
 */

public class PruneManagement {
	private long kickCount;
	private long totalMembers;
	
	public PruneManagement(long _totalMembers) {
		this.kickCount = 0;
		this.totalMembers = _totalMembers;
	}
	
	/**
	 * Retrieve the number of already kicked users
	 * @return
	 */
	
	public long getKickCount() {
		return this.kickCount;
	}
	
	/**
	 * Retrieve the number of total users to kick
	 * @return
	 */
	
	public long getTotalMembers() {
		return this.totalMembers;
	}
	
	/**
	 * Increment the number of users that have been kicked
	 * @return
	 */
	
	public PruneManagement incrementKickCount() {
		this.kickCount++;
		return this;
	}
}
