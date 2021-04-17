package de.azrael.constructors;

/**
 * Keep track of created or removed invites and how many have to be created or removed in total
 * @author xHelixStorm
 *
 */

public class InviteManagement {
	private long inviteCount;
	private long totalInvites;
	
	public InviteManagement(long _totalInvites) {
		this.inviteCount = 0;
		this.totalInvites = _totalInvites;
	}
	
	/**
	 * Retrieve the number of created or removed invites
	 * @return
	 */
	
	public long getInviteCount() {
		return this.inviteCount;
	}
	
	/**
	 * Retrieve the total number of invites to create or to remove
	 * @return
	 */
	
	public long getTotalInvites() {
		return this.totalInvites;
	}
	
	/**
	 * Increase the number of invites created or removed by one
	 * @return
	 */
	
	public InviteManagement incrementInviteCount() {
		this.inviteCount ++;
		return this;
	}
}
